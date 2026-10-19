const path = require('path');
const fs = require('fs');
const os = require('os');

// Top-level crash trap: Electron swallows main-process exceptions silently.
// Write to a temp file so we can diagnose startup failures.
process.on('uncaughtException', (err) => {
  try {
    const crashFile = path.join(os.tmpdir(), 'hrm-desktop-crash.log');
    fs.appendFileSync(crashFile, `[${new Date().toISOString()}] UNCAUGHT: ${err.stack}\n`);
  } catch (_) { /* ignore */ }
  console.error('UNCAUGHT:', err);
  process.exit(1);
});
process.on('unhandledRejection', (reason) => {
  try {
    const crashFile = path.join(os.tmpdir(), 'hrm-desktop-crash.log');
    fs.appendFileSync(crashFile, `[${new Date().toISOString()}] UNHANDLED: ${reason && reason.stack ? reason.stack : reason}\n`);
  } catch (_) { /* ignore */ }
  console.error('UNHANDLED:', reason);
});

const { app, BrowserWindow, Tray, Menu, ipcMain, shell, dialog } = require('electron');
const paths = require('./services/paths');
const logger = require('./services/logger');
const portCheck = require('./services/portCheck');
const mysqlManager = require('./services/mysqlManager');
const gateway = require('./services/gateway');
const orchestrator = require('./services/orchestrator');
const healthCheck = require('./services/healthCheck');
const pids = require('./services/pids');
const { killTree } = require('./services/processTree');

const APP_PORT = 18100;
let splashWin = null;
let mainWin = null;
let statusWin = null;
let tray = null;
let quitting = false;
let booted = false;
const minimized = process.argv.includes('--minimized');

function send(channel, payload) {
  for (const w of [splashWin, mainWin]) {
    if (w && !w.isDestroyed()) w.webContents.send(channel, payload);
  }
}

function createSplash() {
  splashWin = new BrowserWindow({
    width: 520, height: 400, frame: false, resizable: false, show: true,
    webPreferences: { preload: path.join(__dirname, 'preload.js') },
  });
  splashWin.loadFile(path.join(__dirname, 'splash.html'));
}

function showError(title, detail) {
  const w = new BrowserWindow({
    width: 640, height: 460, resizable: true, show: true,
    webPreferences: { preload: path.join(__dirname, 'preload.js'), additionalArguments: [JSON.stringify({ title, detail })] },
  });
  w.loadFile(path.join(__dirname, 'error.html'));
  if (splashWin && !splashWin.isDestroyed()) splashWin.close();
}

function createMainWindow() {
  mainWin = new BrowserWindow({
    width: 1440, height: 900, minWidth: 1100, minHeight: 700,
    show: !minimized,
    webPreferences: { preload: path.join(__dirname, 'preload.js') },
  });
  mainWin.loadURL(`http://127.0.0.1:${APP_PORT}/`);
  mainWin.on('close', (e) => {
    if (!quitting) {
      e.preventDefault();
      mainWin.hide();
    }
  });
  mainWin.on('closed', () => { mainWin = null; });
}

function openStatusWindow() {
  if (statusWin && !statusWin.isDestroyed()) { statusWin.focus(); return; }
  statusWin = new BrowserWindow({
    width: 560, height: 720, resizable: true, show: true,
    webPreferences: { preload: path.join(__dirname, 'preload.js') },
  });
  statusWin.loadFile(path.join(__dirname, 'status.html'));
  statusWin.on('closed', () => { statusWin = null; });
}

function buildTray() {
  const iconPath = path.join(__dirname, 'assets', process.platform === 'win32' ? 'icon.ico' : 'icon.png');
  tray = new Tray(iconPath);
  const menu = Menu.buildFromTemplate([
    { label: 'Open', click: () => { if (mainWin) { mainWin.show(); mainWin.focus(); } else if (booted) createMainWindow(); } },
    { label: 'Service Status', click: openStatusWindow },
    { label: 'Restart All Services', click: async () => {
      await orchestrator.stopAll();
      orchestrator.startAll((ev) => send('boot-progress', ev)).catch((e) => logger.error(e.message));
    } },
    { label: 'Open Logs Folder', click: () => shell.openPath(paths.logsDir()) },
    { type: 'separator' },
    { label: 'Quit', click: () => { quitting = true; app.quit(); } },
  ]);
  tray.setToolTip('HRM Platform');
  tray.setContextMenu(menu);
  tray.on('double-click', () => { if (mainWin) { mainWin.show(); mainWin.focus(); } });
}

async function cleanupOrphans() {
  const entries = pids.readPids();
  for (const e of entries) {
    try { await killTree(e.pid, true); } catch (_) { /* ignore */ }
  }
  pids.clear();
}

function applyInstallOptions() {
  try {
    // install-options.json written by NSIS into install dir
    const installDir = path.dirname(app.getPath('exe'));
    const optFile = path.join(installDir, 'install-options.json');
    if (fs.existsSync(optFile)) {
      const opts = JSON.parse(fs.readFileSync(optFile, 'utf8'));
      if (opts.autoStart) app.setLoginItemSettings({ openAtLogin: true, args: ['--minimized'] });
    }
  } catch (e) {
    logger.warn(`install options read failed: ${e.message}`);
  }
}

async function boot() {
  // 1. orphan cleanup from a previous crashed run
  await cleanupOrphans();

  // 2. port precheck
  if (await portCheck.isPortInUse(mysqlManager.DB_PORT)) {
    // could be our own already-running mysqld from an attached instance; single-instance lock should prevent this
    logger.warn('port 3306 already in use before mysql start');
  }
  if (await portCheck.isPortInUse(APP_PORT)) {
    showError('Port conflict', `Port ${APP_PORT} is already in use. Close the application using it and retry.`);
    return;
  }

  // 3. MySQL (init on first run)
  try {
    await mysqlManager.start((ev) => send('db-progress', ev));
  } catch (e) {
    logger.error(`mysql start failed: ${e.message}`);
    showError('Database failed to start', `${e.message}\n\nCheck logs at: ${paths.logsDir()}`);
    return;
  }

  // 4. Gateway
  try {
    await gateway.startGateway(APP_PORT);
  } catch (e) {
    logger.error(`gateway failed: ${e.message}`);
    showError('Gateway failed to start', e.message);
    return;
  }

  // 5. JVM waves
  orchestrator.startAll((ev) => send('boot-progress', ev))
    .then(async () => {
      const check = await healthCheck.fullCheck();
      logger.info(`boot complete: ${check.up}/${check.total} services up, login=${check.login.ok}`);
      send('boot-done', check);
      booted = true;
      if (splashWin && !splashWin.isDestroyed()) {
        setTimeout(() => {
          if (splashWin && !splashWin.isDestroyed()) splashWin.close();
          createMainWindow();
        }, 800);
      } else if (!minimized) {
        createMainWindow();
      }
    })
    .catch((e) => {
      logger.error(`orchestrator failed: ${e.message}`);
      showError('Services failed to start', e.message);
    });
}

async function shutdown() {
  logger.info('shutdown begin');
  try { await orchestrator.stopAll(); } catch (e) { logger.error(e.message); }
  try { await mysqlManager.stopMysqld(); } catch (e) { logger.error(e.message); }
  pids.clear();
  logger.info('shutdown done');
}

// ---- single instance ----
const gotLock = app.requestSingleInstanceLock();
if (!gotLock) {
  app.quit();
} else {
  app.on('second-instance', () => {
    if (mainWin) { mainWin.show(); mainWin.focus(); }
  });

  app.whenReady().then(() => {
    applyInstallOptions();
    createSplash();
    buildTray();
    boot();
  });

  app.on('before-quit', async (e) => {
    if (!quitting) quitting = true;
    e.preventDefault();
    await shutdown();
    app.exit(0);
  });

  app.on('window-all-closed', () => {
    // keep running in tray
  });
}

// ---- IPC ----
ipcMain.handle('get-status', async () => healthCheck.fullCheck());
ipcMain.handle('restart-service', async (_e, name) => {
  await orchestrator.restartService(name);
  return { ok: true };
});
ipcMain.handle('get-app-info', () => ({
  version: app.getVersion(),
  userData: paths.userData(),
  appPort: APP_PORT,
  logsDir: paths.logsDir(),
}));
ipcMain.on('quit-app', () => { quitting = true; app.quit(); });

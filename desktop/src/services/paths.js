const path = require('path');
const { app } = require('electron');

const isDev = !app.isPackaged;

// In dev mode, assets live under desktop/vendor and desktop/runtime/<plat>;
// in packaged mode they are flattened into process.resourcesPath by extraResources.
function platformKey() {
  if (process.platform === 'win32') return 'win';
  if (process.platform === 'linux') return 'linux';
  if (process.platform === 'darwin') return process.arch === 'arm64' ? 'darwin-arm64' : 'darwin-x64';
  return process.platform;
}

const desktopRoot = path.join(__dirname, '..', '..');

function resRoot() {
  return isDev ? desktopRoot : process.resourcesPath;
}

function runtimeDir() {
  return isDev
    ? path.join(desktopRoot, 'runtime', platformKey())
    : path.join(process.resourcesPath, 'runtime');
}

function userData() {
  return app.getPath('userData');
}

const exe = process.platform === 'win32' ? '.exe' : '';

function javaExe() {
  return path.join(runtimeDir(), 'jre', 'bin', `java${exe}`);
}

function mysqlBin() {
  return path.join(runtimeDir(), 'mysql', 'bin', `mysqld${exe}`);
}

function mysqlCli() {
  return path.join(runtimeDir(), 'mysql', 'bin', `mysql${exe}`);
}

function mysqlAdmin() {
  return path.join(runtimeDir(), 'mysql', 'bin', `mysqladmin${exe}`);
}

function mysqlDump() {
  return path.join(runtimeDir(), 'mysql', 'bin', `mysqldump${exe}`);
}

function mysqlCnf() {
  return path.join(userData(), 'my.cnf');
}

function mysqlDataDir() {
  return path.join(userData(), 'mysql-data');
}

function logsDir() {
  return path.join(userData(), 'logs');
}

function fileStoreDir() {
  return path.join(userData(), 'file-store');
}

function pidsFile() {
  return path.join(userData(), 'pids.json');
}

function initFlagFile() {
  return path.join(userData(), '.initialized');
}

function servicesJson() {
  return isDev
    ? path.join(desktopRoot, 'src', 'config', 'services.json')
    : path.join(process.resourcesPath, 'config', 'services.json');
}

function proxyRulesJson() {
  return isDev
    ? path.join(desktopRoot, 'src', 'config', 'proxy-rules.json')
    : path.join(process.resourcesPath, 'config', 'proxy-rules.json');
}

function dbDumpDir() {
  return isDev
    ? path.join(desktopRoot, 'vendor', 'db-dump')
    : path.join(process.resourcesPath, 'db-dump');
}

function frontendDist() {
  // collect-resources.ps1 copies dist/* into vendor/frontend/ (no nested dist)
  return isDev
    ? path.join(desktopRoot, 'vendor', 'frontend')
    : path.join(process.resourcesPath, 'frontend');
}

function jarsDir() {
  return isDev
    ? path.join(desktopRoot, 'vendor', 'jars')
    : path.join(process.resourcesPath, 'jars');
}

module.exports = {
  isDev,
  resRoot,
  runtimeDir,
  userData,
  javaExe,
  mysqlBin,
  mysqlCli,
  mysqlAdmin,
  mysqlDump,
  mysqlCnf,
  mysqlDataDir,
  logsDir,
  fileStoreDir,
  pidsFile,
  initFlagFile,
  servicesJson,
  proxyRulesJson,
  dbDumpDir,
  frontendDist,
  jarsDir,
};

const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');
const os = require('os');
const paths = require('./paths');
const logger = require('./logger');
const { isPortInUse } = require('./portCheck');
const { killTree, waitForExit } = require('./processTree');
const pids = require('./pids');

const isWindows = os.platform() === 'win32';

const children = new Map(); // name -> child process

function loadServices() {
  return JSON.parse(fs.readFileSync(paths.servicesJson(), 'utf8'));
}

function spawnService(svc) {
  const jar = path.join(paths.jarsDir(), `${svc.name}.jar`);
  const logFile = path.join(paths.logsDir(), `${svc.name}.out.log`);
  fs.mkdirSync(paths.logsDir(), { recursive: true });
  // spawn stdio requires an opened fd, not a WriteStream with fd:null
  const outFd = fs.openSync(logFile, 'a');
  const args = [
    `-Xmx${svc.xmx}m`, '-Xms32m', '-XX:+UseSerialGC', '-XX:TieredStopAtLevel=1',
    '-XX:MaxMetaspaceSize=72m', '-XX:ReservedCodeCacheSize=48m', '-Xss512k',
    '-Dfile.encoding=UTF-8',
    '-jar', jar,
    '--spring.sql.init.mode=never',
  ];
  if (svc.name === 'FileMgt') {
    args.push(`--file.store-root=${paths.fileStoreDir()}`);
  }
  const env = {
    ...process.env,
    DB_HOST: '127.0.0.1',
    DB_PORT: '3306',
    DB_USERNAME: 'root',
    DB_PASSWORD: 'Sun-0126King-0325',
  };
  const child = spawn(paths.javaExe(), args, {
    cwd: paths.userData(),
    env,
    detached: !isWindows,
    windowsHide: true,
    stdio: ['ignore', outFd, outFd],
  });
  child.unref();
  children.set(svc.name, child);
  pids.addPid(child.pid, svc.name, 'jvm');
  const startedAt = Date.now();
  child.on('exit', (code) => {
    children.delete(svc.name);
    pids.removePid(svc.name, 'jvm');
    const early = Date.now() - startedAt < 10000;
    logger.warn(`${svc.name} exited code=${code}${early ? ' (early failure)' : ''}`);
  });
  return child;
}

async function waitWavePorts(waveServices, timeoutMs = 150000) {
  const deadline = Date.now() + timeoutMs;
  const pending = new Map(waveServices.map((s) => [s.name, s.port]));
  while (Date.now() < deadline && pending.size > 0) {
    for (const [name, port] of [...pending]) {
      if (await isPortInUse(port, '127.0.0.1', 400)) pending.delete(name);
    }
    if (pending.size === 0) break;
    await new Promise((r) => setTimeout(r, 500));
  }
  return pending; // names still not listening
}

/**
 * Start all services wave by wave. progressCb(event) receives:
 *   {type:'wave-start', wave, total}
 *   {type:'svc', name, port, status:'launched'|'up'|'down'|'skip'|'failed'}
 */
async function startAll(progressCb) {
  const services = loadServices();
  const waves = new Map();
  for (const s of services) {
    if (!waves.has(s.wave)) waves.set(s.wave, []);
    waves.get(s.wave).push(s);
  }
  const waveNums = [...waves.keys()].sort((a, b) => a - b);
  for (const w of waveNums) {
    const svcs = waves.get(w);
    if (progressCb) progressCb({ type: 'wave-start', wave: w, total: waveNums.length, services: services.length });
    const toWait = [];
    for (const svc of svcs) {
      if (await isPortInUse(svc.port, '127.0.0.1', 400)) {
        if (progressCb) progressCb({ type: 'svc', name: svc.name, port: svc.port, status: 'skip' });
        logger.info(`${svc.name}:${svc.port} already up, skip`);
        continue;
      }
      try {
        spawnService(svc);
        toWait.push(svc);
        if (progressCb) progressCb({ type: 'svc', name: svc.name, port: svc.port, status: 'launched' });
      } catch (e) {
        logger.error(`${svc.name} spawn failed: ${e.message}`);
        if (progressCb) progressCb({ type: 'svc', name: svc.name, port: svc.port, status: 'failed' });
      }
    }
    const stillDown = await waitWavePorts(toWait);
    for (const svc of toWait) {
      const status = stillDown.has(svc.name) ? 'down' : 'up';
      if (progressCb) progressCb({ type: 'svc', name: svc.name, port: svc.port, status });
    }
  }
}

async function stopAll() {
  const entries = pids.readPids().filter((p) => p.kind === 'jvm');
  // graceful first
  await Promise.all(entries.map((e) => killTree(e.pid, false)));
  await new Promise((r) => setTimeout(r, 4000));
  // force kill survivors
  await Promise.all(entries.map((e) => killTree(e.pid, true)));
  children.clear();
  for (const e of entries) pids.removePid(e.name, 'jvm');
}

async function restartService(name) {
  const services = loadServices();
  const svc = services.find((s) => s.name === name);
  if (!svc) throw new Error(`unknown service ${name}`);
  const entry = pids.readPids().find((p) => p.name === name && p.kind === 'jvm');
  if (entry) {
    await killTree(entry.pid, false);
    await waitForExit(entry.pid, 4000);
    await killTree(entry.pid, true);
    pids.removePid(name, 'jvm');
  }
  spawnService(svc);
}

function statusSync() {
  const services = loadServices();
  return Promise.all(services.map(async (s) => ({
    name: s.name,
    port: s.port,
    wave: s.wave,
    up: await isPortInUse(s.port, '127.0.0.1', 300),
  })));
}

module.exports = { startAll, stopAll, restartService, statusSync, loadServices };

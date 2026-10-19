const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const { spawn, execFile } = require('child_process');
const os = require('os');
const paths = require('./paths');
const logger = require('./logger');
const { isPortInUse } = require('./portCheck');
const { killTree, waitForExit } = require('./processTree');
const pids = require('./pids');

const isWindows = os.platform() === 'win32';
const DB_HOST = '127.0.0.1';
const DB_PORT = 3306;
const DB_USER = 'root';
const DB_PASS = 'Sun-0126King-0325';
const PIPE_NAME = 'MySQLHRM';  // Windows named pipe (skip-name-resolve blocks TCP for root@localhost)
const UNIX_SOCKET = 'hrm-mysql.sock';  // Linux/macOS socket file (under userData)

function socketPath() {
  return path.join(paths.userData(), UNIX_SOCKET);
}

// Connection selector: during initialize-insecure (no password set yet), root@localhost only
// works via named pipe / unix socket because skip-name-resolve blocks TCP reverse-mapping.
// After setRootPassword() creates root@127.0.0.1, TCP works.
function localConnArgs(usePassword) {
  const pwd = usePassword ? [`-p${DB_PASS}`] : [];
  if (isWindows) {
    return ['--pipe', `--socket=${PIPE_NAME}`, '-u', DB_USER, ...pwd];
  }
  return [`--socket=${socketPath()}`, '-u', DB_USER, ...pwd];
}

function tcpConnArgs(usePassword) {
  const pwd = usePassword ? [`-p${DB_PASS}`] : [];
  return ['-h', DB_HOST, '-P', String(DB_PORT), '-u', DB_USER, ...pwd];
}

function toCnfPath(p) {
  // my.cnf paths must use forward slashes (Windows backslashes escape chars)
  return p.split(path.sep).join('/');
}

function buildCnf() {
  const res = paths.resRoot();
  const ud = paths.userData();
  const lines = [
    '[mysqld]',
    `basedir=${toCnfPath(path.join(paths.runtimeDir(), 'mysql'))}`,
    `datadir=${toCnfPath(paths.mysqlDataDir())}`,
    `port=${DB_PORT}`,
    'bind-address=127.0.0.1',
    'skip-name-resolve=ON',
    'lower_case_table_names=1',
    'character-set-server=utf8mb4',
    'collation-server=utf8mb4_0900_ai_ci',
    'mysql_native_password=ON',
    'innodb_buffer_pool_size=96M',
    'performance_schema=OFF',
    'mysqlx=OFF',
    'max_connections=250',
  ];
  if (isWindows) {
    lines.push('enable-named-pipe=ON', `socket=${PIPE_NAME}`);
  } else {
    lines.push(`socket=${toCnfPath(socketPath())}`);
  }
  lines.push(
    `log-error=${toCnfPath(path.join(paths.logsDir(), 'mysql.err'))}`,
    '',
    '[client]',
    `port=${DB_PORT}`,
    'default-character-set=utf8mb4',
    '',
  );
  fs.mkdirSync(ud, { recursive: true });
  fs.writeFileSync(paths.mysqlCnf(), lines.join('\n'), 'utf8');
  logger.info(`my.cnf written: ${paths.mysqlCnf()}`);
}

function run(cmd, args, opts = {}) {
  return new Promise((resolve, reject) => {
    execFile(cmd, args, { windowsHide: true, maxBuffer: 16 * 1024 * 1024, ...opts }, (err, stdout, stderr) => {
      if (err) reject(new Error(`${path.basename(cmd)} ${args.join(' ')} failed: ${stderr || err.message}`));
      else resolve({ stdout, stderr });
    });
  });
}

async function waitPing(timeoutMs = 60000) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    // Try TCP with password (post-initialize state)
    try {
      await run(paths.mysqlAdmin(), [...tcpConnArgs(true), 'ping'], {});
      return true;
    } catch (_) { /* retry */ }
    // Try local socket/pipe without password (initialize-insecure state)
    try {
      await run(paths.mysqlAdmin(), [...localConnArgs(false), 'ping'], {});
      return true;
    } catch (_) { /* retry */ }
    await new Promise((r) => setTimeout(r, 1000));
  }
  return false;
}

let mysqldProc = null;

async function startMysqld() {
  if (await isPortInUse(DB_PORT)) {
    logger.info('MySQL port 3306 already listening, reusing');
    return { reused: true };
  }
  // socket via CLI arg: my.cnf socket= not reliably honored on Windows
  const args = [`--defaults-file=${paths.mysqlCnf()}`];
  if (isWindows) {
    args.push('--enable-named-pipe', `--socket=${PIPE_NAME}`);
  } else {
    args.push(`--socket=${socketPath()}`);
  }
  const opts = {
    detached: !isWindows,
    stdio: 'ignore',
    windowsHide: true,
  };
  mysqldProc = spawn(paths.mysqlBin(), args, opts);
  mysqldProc.unref();
  pids.addPid(mysqldProc.pid, 'mysqld', 'mysql');
  const up = await waitPing(90000);
  if (!up) throw new Error('mysqld failed to start within 90s; see logs/mysql.err');
  logger.info(`mysqld started pid=${mysqldProc.pid}`);
  return { reused: false };
}

async function stopMysqld() {
  try {
    if (await isPortInUse(DB_PORT)) {
      await run(paths.mysqlAdmin(), [...tcpConnArgs(true), 'shutdown'], {});
      const entry = pids.readPids().find((p) => p.kind === 'mysql');
      if (entry) await waitForExit(entry.pid, 20000);
    }
  } catch (e) {
    logger.warn(`mysqladmin shutdown failed: ${e.message}; force killing`);
    const entry = pids.readPids().find((p) => p.kind === 'mysql');
    if (entry) await killTree(entry.pid, true);
  }
  pids.removePid('mysqld', 'mysql');
}

async function setRootPassword() {
  // Use local socket/pipe: root@localhost only connects via socket when skip-name-resolve=ON.
  // After this, root@127.0.0.1 exists for TCP.
  const sql = [
    `ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${DB_PASS}';`,
    `CREATE USER IF NOT EXISTS 'root'@'127.0.0.1' IDENTIFIED WITH mysql_native_password BY '${DB_PASS}';`,
    `GRANT ALL PRIVILEGES ON *.* TO 'root'@'127.0.0.1' WITH GRANT OPTION;`,
    'FLUSH PRIVILEGES;',
  ].join(' ');
  await run(paths.mysqlCli(), [...localConnArgs(false), '-e', sql], {});
  logger.info('root password set (localhost + 127.0.0.1)');
}

function importDumpFile(gzFile, onProgress) {
  return new Promise((resolve, reject) => {
    const mysql = spawn(paths.mysqlCli(), [
      ...tcpConnArgs(true),
      '--default-character-set=utf8mb4',
    ], { windowsHide: true });
    let stderr = '';
    mysql.stderr.on('data', (d) => { stderr += d.toString(); });
    const src = fs.createReadStream(gzFile);
    const gunzip = zlib.createGunzip();
    src.on('error', reject);
    gunzip.on('error', reject);
    src.pipe(gunzip).pipe(mysql.stdin);
    mysql.on('close', (code) => {
      if (code === 0) resolve();
      else reject(new Error(`import ${path.basename(gzFile)} exit=${code}: ${stderr.slice(-500)}`));
    });
  });
}

async function importAllDumps(progressCb) {
  const dir = paths.dbDumpDir();
  const manifest = path.join(dir, 'manifest.json');
  let files = [];
  if (fs.existsSync(manifest)) {
    const m = JSON.parse(fs.readFileSync(manifest, 'utf8'));
    files = m.databases.map((d) => ({ db: d.db, file: path.join(dir, d.file) }));
  } else {
    files = fs.readdirSync(dir).filter((f) => f.endsWith('.sql.gz'))
      .map((f) => ({ db: f.replace(/\.sql\.gz$/, ''), file: path.join(dir, f) }));
  }
  let done = 0;
  for (const { db, file } of files) {
    if (!fs.existsSync(file)) throw new Error(`missing dump file: ${file}`);
    let lastErr = null;
    for (let attempt = 1; attempt <= 2; attempt++) {
      try {
        await importDumpFile(file);
        lastErr = null;
        break;
      } catch (e) {
        lastErr = e;
        logger.warn(`import ${db} attempt ${attempt} failed: ${e.message}`);
      }
    }
    if (lastErr) throw new Error(`failed to import database ${db}: ${lastErr.message}`);
    done++;
    if (progressCb) progressCb(done, files.length, db);
    logger.info(`imported ${db} (${done}/${files.length})`);
  }
  return files.length;
}

async function verifySeed() {
  const { stdout } = await run(paths.mysqlCli(), [
    ...tcpConnArgs(true),
    '-N', '-e', "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema NOT IN ('mysql','sys','information_schema','performance_schema');",
  ], {});
  const n = parseInt(String(stdout).trim(), 10);
  if (!Number.isFinite(n) || n < 50) throw new Error(`seed verification failed: only ${n} tables`);
  logger.info(`seed verification ok: ${n} tables`);
}

/**
 * First-run initialization: write cnf, initialize datadir, start, set password, import dumps.
 */
async function initialize(progressCb) {
  const dataDir = paths.mysqlDataDir();
  if (fs.existsSync(dataDir)) {
    throw new Error('mysql-data already exists but not initialized flag; reset required');
  }
  fs.mkdirSync(paths.logsDir(), { recursive: true });
  buildCnf();
  if (progressCb) progressCb({ phase: 'init', text: 'Initializing database...' });
  await run(paths.mysqlBin(), [`--defaults-file=${paths.mysqlCnf()}`, '--initialize-insecure'], {});
  await startMysqld();
  await setRootPassword();
  const count = await importAllDumps((done, total, db) => {
    if (progressCb) progressCb({ phase: 'import', done, total, db });
  });
  await verifySeed();
  fs.writeFileSync(paths.initFlagFile(), JSON.stringify({ version: 1, at: new Date().toISOString(), dbs: count }), 'utf8');
  logger.info('database initialization complete');
}

/**
 * Regular startup: ensure cnf exists, reset broken half-init, start mysqld.
 */
async function start(progressCb) {
  const dataDir = paths.mysqlDataDir();
  const initialized = fs.existsSync(paths.initFlagFile());
  if (!fs.existsSync(paths.mysqlCnf())) buildCnf();
  if (!initialized && fs.existsSync(dataDir)) {
    // half-initialized: wipe and redo
    logger.warn('half-initialized datadir found, resetting');
    fs.rmSync(dataDir, { recursive: true, force: true });
  }
  if (!initialized) {
    await initialize(progressCb);
  } else {
    await startMysqld();
  }
}

module.exports = { start, stopMysqld, isPortInUse, DB_PORT, DB_HOST };

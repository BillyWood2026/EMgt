const fs = require('fs');
const path = require('path');
const { userData } = require('./paths');

let logStream = null;
let logPath = null;

function ensureDir(dir) {
  if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
}

function rotateIfNeeded() {
  try {
    if (logPath && fs.existsSync(logPath) && fs.statSync(logPath).size > 10 * 1024 * 1024) {
      const bak = logPath + '.1';
      if (fs.existsSync(bak)) fs.unlinkSync(bak);
      fs.renameSync(logPath, bak);
    }
  } catch (_) { /* ignore */ }
}

function write(level, msg) {
  const line = `[${new Date().toISOString()}] [${level}] ${msg}\n`;
  try {
    ensureDir(path.join(userData(), 'logs'));
    if (!logStream) {
      logPath = path.join(userData(), 'logs', 'desktop.log');
      rotateIfNeeded();
      logStream = fs.createWriteStream(logPath, { flags: 'a' });
    }
    logStream.write(line);
  } catch (_) {
    // fallback to console
  }
  const fn = level === 'ERROR' ? console.error : console.log;
  fn(`[desktop] ${line.trim()}`);
}

module.exports = {
  info: (msg) => write('INFO', msg),
  warn: (msg) => write('WARN', msg),
  error: (msg) => write('ERROR', msg),
};

const fs = require('fs');
const path = require('path');
const { pidsFile } = require('./paths');

function ensureDir(dir) {
  if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
}

function readPids() {
  try {
    const file = pidsFile();
    if (!fs.existsSync(file)) return [];
    return JSON.parse(fs.readFileSync(file, 'utf8'));
  } catch (_) {
    return [];
  }
}

function writePids(pids) {
  const file = pidsFile();
  ensureDir(path.dirname(file));
  fs.writeFileSync(file, JSON.stringify(pids, null, 2), 'utf8');
}

function addPid(pid, name, kind = 'jvm') {
  const pids = readPids();
  const idx = pids.findIndex((p) => p.name === name && p.kind === kind);
  const entry = { pid, name, kind, startedAt: Date.now() };
  if (idx >= 0) pids[idx] = entry;
  else pids.push(entry);
  writePids(pids);
}

function removePid(name, kind = 'jvm') {
  const pids = readPids();
  const next = pids.filter((p) => !(p.name === name && p.kind === kind));
  writePids(next);
}

function clear() {
  writePids([]);
}

module.exports = { readPids, writePids, addPid, removePid, clear };

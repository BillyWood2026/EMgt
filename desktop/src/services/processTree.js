const { exec } = require('child_process');
const os = require('os');

const isWindows = os.platform() === 'win32';

/**
 * Kill a process tree by PID.
 * On Windows uses taskkill /T (tree). On Unix sends signal to the process group (negative PID).
 */
function killTree(pid, force = false) {
  return new Promise((resolve) => {
    if (!pid || pid <= 0) return resolve();
    if (isWindows) {
      const cmd = force ? `taskkill /PID ${pid} /T /F` : `taskkill /PID ${pid} /T`;
      exec(cmd, { windowsHide: true }, () => resolve());
    } else {
      try {
        process.kill(-pid, force ? 'SIGKILL' : 'SIGTERM');
      } catch (_) {
        try { process.kill(pid, force ? 'SIGKILL' : 'SIGTERM'); } catch (_) { /* already dead */ }
      }
      resolve();
    }
  });
}

/**
 * Check if a PID is still alive.
 */
function isAlive(pid) {
  if (!pid || pid <= 0) return false;
  try {
    process.kill(pid, 0);
    return true;
  } catch (_) {
    return false;
  }
}

/**
 * Wait until a PID exits, up to timeoutMs.
 */
async function waitForExit(pid, timeoutMs = 5000) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    if (!isAlive(pid)) return true;
    await new Promise((r) => setTimeout(r, 250));
  }
  return !isAlive(pid);
}

module.exports = { killTree, isAlive, waitForExit };

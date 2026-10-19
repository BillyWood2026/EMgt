const net = require('net');

/**
 * Check whether a TCP port is listening on 127.0.0.1.
 * Returns a Promise<boolean>.
 */
function isPortInUse(port, host = '127.0.0.1', timeoutMs = 1000) {
  return new Promise((resolve) => {
    const socket = new net.Socket();
    let done = false;
    const finish = (inUse) => {
      if (done) return;
      done = true;
      socket.removeAllListeners();
      socket.destroy();
      resolve(inUse);
    };
    socket.setTimeout(timeoutMs);
    socket.once('connect', () => finish(true));
    socket.once('timeout', () => finish(false));
    socket.once('error', (err) => {
      // ECONNREFUSED means not listening; other errors (e.g. firewall) also treated as free
      finish(err.code !== 'ECONNREFUSED');
    });
    socket.connect(port, host);
  });
}

/**
 * Find a free port starting from the given port, scanning up to +50.
 */
async function findFreePort(startPort, host = '127.0.0.1') {
  for (let p = startPort; p < startPort + 50; p++) {
    if (!(await isPortInUse(p, host))) return p;
  }
  throw new Error(`No free port found near ${startPort}`);
}

module.exports = { isPortInUse, findFreePort };

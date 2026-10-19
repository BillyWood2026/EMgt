const http = require('http');
const orchestrator = require('./orchestrator');
const logger = require('./logger');

function tryLogin() {
  return new Promise((resolve) => {
    const body = JSON.stringify({ username: 'admin', password: 'admin123' });
    const req = http.request({
      hostname: '127.0.0.1',
      port: 8081,
      path: '/api/auth/login',
      method: 'POST',
      headers: { 'content-type': 'application/json', 'content-length': Buffer.byteLength(body) },
      timeout: 5000,
    }, (res) => {
      let data = '';
      res.on('data', (d) => { data += d; });
      res.on('end', () => {
        try {
          const j = JSON.parse(data);
          resolve(j.code === 200 && j.data && j.data.token ? { ok: true, token: j.data.token, role: j.data.role, username: j.data.username } : { ok: false });
        } catch (_) {
          resolve({ ok: false });
        }
      });
    });
    req.on('error', () => resolve({ ok: false }));
    req.on('timeout', () => { req.destroy(); resolve({ ok: false }); });
    req.write(body);
    req.end();
  });
}

async function fullCheck() {
  const statuses = await orchestrator.statusSync();
  const up = statuses.filter((s) => s.up).length;
  const login = await tryLogin();
  return { services: statuses, up, total: statuses.length, login };
}

module.exports = { fullCheck, tryLogin };

const fs = require('fs');
const http = require('http');
const path = require('path');
const zlib = require('zlib');
const paths = require('./paths');
const logger = require('./logger');

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.webp': 'image/webp',
  '.ico': 'image/x-icon',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf',
  '.map': 'application/json',
  '.txt': 'text/plain; charset=utf-8',
  '.mp4': 'video/mp4',
  '.webm': 'video/webm',
};

const TEXT_TYPES = new Set(['.html', '.js', '.css', '.json', '.svg', '.txt', '.map']);

function loadRules() {
  const raw = JSON.parse(fs.readFileSync(paths.proxyRulesJson(), 'utf8'));
  return raw.map((r) => ({
    type: r.type,
    match: r.match,
    target: r.target,
    regex: r.type === 'regex' ? new RegExp(r.match) : null,
  }));
}

function matchRule(rules, pathname) {
  for (const r of rules) {
    if (r.type === 'prefix') {
      if (pathname.startsWith(r.match)) return r;
    } else if (r.regex && r.regex.test(pathname)) {
      return r;
    }
  }
  return null;
}

function proxyRequest(rule, req, res) {
  const target = new URL(rule.target);
  const options = {
    hostname: target.hostname,
    port: target.port,
    path: req.url,
    method: req.method,
    headers: { ...req.headers, host: `${target.hostname}:${target.port}` },
    timeout: 10 * 60 * 1000,
  };
  const upstream = http.request(options, (upRes) => {
    res.writeHead(upRes.statusCode || 502, upRes.headers);
    upRes.pipe(res);
  });
  upstream.on('timeout', () => {
    upstream.destroy(new Error('upstream timeout'));
  });
  upstream.on('error', (err) => {
    logger.warn(`proxy ${req.url} -> ${rule.target} failed: ${err.code || err.message}`);
    if (!res.headersSent) {
      res.writeHead(502, { 'content-type': 'application/json; charset=utf-8' });
    }
    res.end(JSON.stringify({ code: 502, message: `service unavailable: ${rule.target}`, data: null }));
  });
  req.pipe(upstream);
}

function serveStatic(distRoot, req, res) {
  if (req.method !== 'GET' && req.method !== 'HEAD') {
    res.writeHead(405);
    return res.end();
  }
  let pathname;
  try {
    pathname = decodeURIComponent(new URL(req.url, 'http://x').pathname);
  } catch (_) {
    res.writeHead(400);
    return res.end();
  }
  let filePath = path.join(distRoot, pathname);
  if (!filePath.startsWith(distRoot)) {
    res.writeHead(403);
    return res.end();
  }
  if (!fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
    // SPA history fallback
    filePath = path.join(distRoot, 'index.html');
  }
  if (!fs.existsSync(filePath)) {
    res.writeHead(404);
    return res.end('not found');
  }
  const ext = path.extname(filePath).toLowerCase();
  const mime = MIME[ext] || 'application/octet-stream';
  const headers = { 'content-type': mime };
  const accept = req.headers['accept-encoding'] || '';
  let stream = fs.createReadStream(filePath);
  if (accept.includes('gzip') && TEXT_TYPES.has(ext)) {
    headers['content-encoding'] = 'gzip';
    stream = stream.pipe(zlib.createGzip());
  }
  res.writeHead(200, headers);
  stream.pipe(res);
  stream.on('error', () => res.destroy());
}

function startGateway(port = 18100) {
  const rules = loadRules();
  const distRoot = paths.frontendDist();
  const server = http.createServer((req, res) => {
    const pathname = req.url.split('?')[0];
    if (pathname.startsWith('/api/')) {
      const rule = matchRule(rules, pathname);
      if (rule) return proxyRequest(rule, req, res);
      res.writeHead(502, { 'content-type': 'application/json; charset=utf-8' });
      return res.end(JSON.stringify({ code: 502, message: `no route for ${pathname}`, data: null }));
    }
    serveStatic(distRoot, req, res);
  });
  // WebSocket / upgrade passthrough (future-proof; backends do not use ws currently)
  server.on('upgrade', (req, socket) => socket.destroy());
  return new Promise((resolve, reject) => {
    server.once('error', reject);
    server.listen(port, '127.0.0.1', () => {
      logger.info(`gateway listening on http://127.0.0.1:${port}`);
      resolve(server);
    });
  });
}

module.exports = { startGateway };

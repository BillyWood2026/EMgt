const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('hrm', {
  onBootProgress: (cb) => ipcRenderer.on('boot-progress', (_e, p) => cb(p)),
  onDbProgress: (cb) => ipcRenderer.on('db-progress', (_e, p) => cb(p)),
  onBootDone: (cb) => ipcRenderer.on('boot-done', (_e, p) => cb(p)),
  getStatus: () => ipcRenderer.invoke('get-status'),
  restartService: (name) => ipcRenderer.invoke('restart-service', name),
  getAppInfo: () => ipcRenderer.invoke('get-app-info'),
  quit: () => ipcRenderer.send('quit-app'),
  errorInfo: () => {
    const arg = process.argv.find((a) => a.startsWith('{'));
    try { return arg ? JSON.parse(arg) : { title: 'Error', detail: '' }; } catch (_) { return { title: 'Error', detail: '' }; }
  },
});

import axios from '@/api/axiosConfig'

// 流程引擎 FlowEngineMgt (8206)
const crud = (base) => ({
  list: (keyword) => axios.get(`${base}/list`, { params: { keyword } }),
  get: (id) => axios.get(`${base}/${id}`),
  save: (data) => axios.post(base, data),
  update: (id, data) => axios.put(`${base}/${id}`, data),
  remove: (id) => axios.delete(`${base}/${id}`)
})

export const flowApi = {
  definition: {
    ...crud('/api/flow/definition'),
    publish: (id) => axios.post(`/api/flow/definition/${id}/publish`),
    suspend: (id) => axios.post(`/api/flow/definition/${id}/suspend`)
  },
  node: crud('/api/flow/node'),
  nodeListByDef: (defId) => axios.get('/api/flow/node/list', { params: { defId } }),
  instance: {
    list: (keyword) => axios.get('/api/flow/instance/list', { params: { keyword } }),
    get: (id) => axios.get(`/api/flow/instance/${id}`),
    start: (data) => axios.post('/api/flow/instance/start', data),
    withdraw: (id, operator) => axios.post(`/api/flow/instance/${id}/withdraw`, null, { params: { operator } })
  },
  task: {
    list: (keyword) => axios.get('/api/flow/task/list', { params: { keyword } }),
    get: (id) => axios.get(`/api/flow/task/${id}`),
    todo: (assignee) => axios.get('/api/flow/task/todo', { params: { assignee } }),
    done: (assignee) => axios.get('/api/flow/task/done', { params: { assignee } }),
    complete: (id, data) => axios.post(`/api/flow/task/${id}/complete`, data)
  },
  cc: crud('/api/flow/cc'),
  callback: crud('/api/flow/callback'),
  timeout: crud('/api/flow/timeout')
}

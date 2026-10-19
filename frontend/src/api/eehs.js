import axios from '@/api/axiosConfig'

// EEHS 板块：4 个微服务统一前缀 /api/eehs（vite 按二级路径分流至 8106-8109）
const crud = (base) => ({
  list: (keyword) => axios.get(base, { params: { keyword } }),
  get: (id) => axios.get(`${base}/${id}`),
  save: (data) => axios.post(base, data),
  update: (id, data) => axios.put(`${base}/${id}`, data),
  remove: (id) => axios.delete(`${base}/${id}`)
})

// ========== 环境管理 EnvironMgt (8106) ==========
export const envApi = {
  monitors: crud('/api/eehs/env/monitors'),
  emissions: crud('/api/eehs/env/emissions'),
  wastes: crud('/api/eehs/env/wastes'),
  policies: crud('/api/eehs/env/policies'),
  targets: crud('/api/eehs/env/targets'),
  ping: () => axios.get('/api/eehs/env/ping')
}

// ========== 安全管理 SafetyMgt (8107) ==========
export const safetyApi = {
  inspections: crud('/api/eehs/safety/inspections'),
  incidents: crud('/api/eehs/safety/incidents'),
  policies: crud('/api/eehs/safety/policies'),
  trainings: crud('/api/eehs/safety/trainings'),
  ping: () => axios.get('/api/eehs/safety/ping')
}

// ========== 职业健康管理 OccHealthMgt (8108) ==========
export const healthApi = {
  examinations: crud('/api/eehs/health/examinations'),
  records: crud('/api/eehs/health/records'),
  protections: crud('/api/eehs/health/protections'),
  ping: () => axios.get('/api/eehs/health/ping')
}

// ========== 能源管理 EnergyMgt (8109) ==========
export const energyApi = {
  consumptions: crud('/api/eehs/energy/consumptions'),
  savings: crud('/api/eehs/energy/savings'),
  analyses: crud('/api/eehs/energy/analyses'),
  ping: () => axios.get('/api/eehs/energy/ping')
}

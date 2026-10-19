import axios from '@/api/axiosConfig'

// 统一中台板块：6 个中台微服务（数据中台8200/文件中台8201/消息中心8202/IOT8203/BI8204/大屏8205）
const crud = (base) => ({
  list: (keyword) => axios.get(`${base}/list`, { params: { keyword } }),
  get: (id) => axios.get(`${base}/${id}`),
  save: (data) => axios.post(base, data),
  update: (id, data) => axios.put(`${base}/${id}`, data),
  remove: (id) => axios.delete(`${base}/${id}`)
})

// ========== 数据中台 DataMgt (8200) ==========
export const dataApi = {
  standard: crud('/api/data/standard'),
  master: crud('/api/data/master'),
  metadata: crud('/api/data/metadata'),
  quality: crud('/api/data/quality'),
  integration: crud('/api/data/integration'),
  asset: crud('/api/data/asset'),
  dictType: crud('/api/data/dict/type'),
  dictItem: crud('/api/data/dict/item'),
  service: crud('/api/data/service')
}

// ========== 文件中台 FileMgt (8201) ==========
export const fileApi = {
  record: crud('/api/file/record'),
  version: crud('/api/file/version'),
  attachment: crud('/api/file/attachment'),
  log: crud('/api/file/log'),
  upload: (formData) => axios.post('/api/file/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

// ========== 消息中心 MessageMgt (8202) ==========
export const messageApi = {
  record: crud('/api/message/record'),
  template: crud('/api/message/template'),
  channel: crud('/api/message/channel'),
  stat: crud('/api/message/stat')
}

// ========== 物联网 IotMgt (8203) ==========
export const iotApi = {
  device: crud('/api/iot/device'),
  thingModel: crud('/api/iot/thing-model'),
  telemetry: crud('/api/iot/telemetry'),
  command: { ...crud('/api/iot/command'), execute: (id) => axios.post(`/api/iot/command/${id}/execute`) },
  rule: crud('/api/iot/rule'),
  alert: crud('/api/iot/alert'),
  gateway: crud('/api/iot/gateway')
}

// ========== 报表BI BiMgt (8204) ==========
export const biApi = {
  datasource: crud('/api/bi/datasource'),
  dataset: crud('/api/bi/dataset'),
  metric: crud('/api/bi/metric'),
  report: {
    ...crud('/api/bi/report'),
    publish: (id) => axios.post(`/api/bi/report/${id}/publish`),
    preview: (id) => axios.get(`/api/bi/report/${id}/preview`)
  },
  chart: crud('/api/bi/chart'),
  subscription: crud('/api/bi/subscription')
}

// ========== 大屏中台 ScreenMgt (8205) ==========
export const screenApi = {
  template: crud('/api/screen/template'),
  component: crud('/api/screen/component'),
  instance: {
    ...crud('/api/screen/instance'),
    publish: (id) => axios.post(`/api/screen/instance/${id}/publish`),
    render: (id) => axios.get(`/api/screen/instance/${id}/render`)
  },
  datasource: crud('/api/screen/datasource')
}

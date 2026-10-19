import axios from 'axios'
import { ElMessage } from 'element-plus'

const instance = axios.create({
  timeout: 30000
})

instance.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // 人力规划/异动/人才测评等模块的本地权限适配器以「用户名」作为角色映射键
    // （如 admin→HR_ADMIN 全权限、hr_manager→HR_MANAGER），数字 userId 无法命中映射表
    const userId = localStorage.getItem('username') || localStorage.getItem('userId') || 'dev-user'
    config.headers['X-User-Id'] = userId
    // 党务模块角色（R02/R03/R04/R07/R99），由页面角色切换器写入
    config.headers['X-Party-Role'] = localStorage.getItem('party_role') || 'R02'
    // 企业文化模块角色（C01-C07/C99），由页面角色切换器写入
    config.headers['X-Culture-Role'] = localStorage.getItem('culture_role') || 'C03'
    // 后端 RBAC 拦截器角色（审计8145/纪检8196/HRM 各服务），按请求模块选取：
    // 未显式切换时默认模块管理角色（与 party/culture 角色切换器同一约定）
    const url = config.url || ''
    if (url.includes('/api/audit/')) {
      config.headers['X-Role'] = localStorage.getItem('audit_role') || 'AUDIT_LEAD'
    } else if (url.includes('/api/discipline/')) {
      config.headers['X-Role'] = localStorage.getItem('discipline_role') || 'DISCIPLINE_LEAD'
    } else {
      // HRM 模块及其他已接入 RBAC 的服务：HR_ADMIN 具备写权限，EMPLOYEE 只读
      const hrmRole = localStorage.getItem('hrm_role') || 'HR_ADMIN'
      config.headers['X-Role'] = hrmRole === 'HR_ADMIN' && userId === 'admin' ? 'C99' : hrmRole
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

instance.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      window.location.href = '/login'
    } else if (error.config && error.config.url && error.config.url.includes('/api/party/')) {
      // 党务业务阻断/权限异常统一提示（[E-XXX-nn][阻断] 中文消息）
      const d = error.response && error.response.data
      if (d && (d.msg || d.message)) {
        ElMessage({
          message: d.msg || d.message,
          type: error.response.status === 403 ? 'warning' : 'error',
          duration: 4000,
          showClose: true
        })
      }
    } else if (error.config && error.config.url && error.config.url.includes('/api/culture/')) {
      // 企业文化业务阻断/权限异常统一提示（[E-CUL-nn][阻断] 中文消息）
      const d = error.response && error.response.data
      if (d && (d.msg || d.message)) {
        ElMessage({
          message: d.msg || d.message,
          type: error.response.status === 403 ? 'warning' : 'error',
          duration: 4000,
          showClose: true
        })
      }
    } else if (error.response && error.response.status === 403) {
      // 审计/纪检/HRM 后端 RBAC 拦截：统一提示信封中的中文原因
      const d = error.response.data
      if (d && (d.msg || d.message)) {
        ElMessage({
          message: d.msg || d.message,
          type: 'warning',
          duration: 4000,
          showClose: true
        })
      }
    }
    return Promise.reject(error)
  }
)

export default instance
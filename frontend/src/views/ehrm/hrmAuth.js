import { reactive } from 'vue'

/**
 * HRM 人力规划/招聘模块角色（需求规格书 IAM 角色矩阵）
 * HR_ADMIN    超级HR/系统管理员（全权限）
 * HRBP        HRBP/招聘HR/人力测算专员（本级组织配置、测算、编制、跟进）
 * HR_DIRECTOR 人力总监（审批终审、需求作废、全量数据）
 * DEPT_HEAD   业务部门负责人（查看本部门、提交基数/需求、本级评审）
 * EXEC        集团高管/党委（集团BI全景、干部规划与大规模优化终审，成本脱敏）
 * EMPLOYEE    普通员工（只读，无操作权限）
 */
export const HRM_ROLES = [
  { value: 'HR_ADMIN', label: '超级HR/管理员' },
  { value: 'HRBP', label: 'HRBP/招聘HR' },
  { value: 'HR_DIRECTOR', label: '人力总监' },
  { value: 'DEPT_HEAD', label: '业务部门负责人' },
  { value: 'EXEC', label: '集团高管/党委' },
  { value: 'EMPLOYEE', label: '普通员工' }
]

const STORAGE_KEY = 'hrm_role'

export const hrmAuth = reactive({
  role: localStorage.getItem(STORAGE_KEY) || 'HR_ADMIN'
})

export function setHrmRole(role) {
  hrmAuth.role = role
  localStorage.setItem(STORAGE_KEY, role)
}

/** 当前角色是否具备任一给定角色；HR_ADMIN 为全权限超管。 */
export function can(...roles) {
  if (hrmAuth.role === 'HR_ADMIN') return true
  return roles.includes(hrmAuth.role)
}

export function roleLabel(role) {
  const hit = HRM_ROLES.find(r => r.value === role)
  return hit ? hit.label : role
}

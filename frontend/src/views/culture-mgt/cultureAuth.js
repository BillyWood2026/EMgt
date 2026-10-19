import { reactive } from 'vue'

/**
 * CCMS 企业文化模块角色（需求 IAM 角色矩阵）
 * C01 普通员工        C02 子公司文化专员   C03 集团文化管理员
 * C04 部门负责人/干部  C05 人力负责人       C06 法务合规
 * C07 分管高管        C99 超级管理员
 */
export const CULTURE_ROLES = [
  { value: 'C01', label: '普通员工' },
  { value: 'C02', label: '子公司文化专员' },
  { value: 'C03', label: '集团文化管理员' },
  { value: 'C04', label: '部门负责人/干部' },
  { value: 'C05', label: '人力负责人' },
  { value: 'C06', label: '法务合规' },
  { value: 'C07', label: '分管高管' },
  { value: 'C99', label: '超级管理员' }
]

const STORAGE_KEY = 'culture_role'

export const cultureAuth = reactive({
  role: localStorage.getItem(STORAGE_KEY) || 'C03'
})

export function setCultureRole(role) {
  cultureAuth.role = role
  localStorage.setItem(STORAGE_KEY, role)
}

/** 当前角色是否具备任一给定角色；C99 为全权限超管。 */
export function can(...roles) {
  if (cultureAuth.role === 'C99') return true
  return roles.includes(cultureAuth.role)
}

export function roleLabel(role) {
  const hit = CULTURE_ROLES.find(r => r.value === role)
  return hit ? hit.label : role
}

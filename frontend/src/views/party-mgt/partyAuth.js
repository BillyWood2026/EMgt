import { reactive } from 'vue'

/**
 * 党务模块角色（需求 3.x 权限模型）
 * R02 集团党委管理员（党务工作机构）  R03 上级党委（审批）
 * R04 党委组织委员                    R07 党支部书记
 * R99 普通党员（只读）
 */
export const PARTY_ROLES = [
  { value: 'R02', label: '集团党委管理员' },
  { value: 'R03', label: '上级党委(审批)' },
  { value: 'R04', label: '党委组织委员' },
  { value: 'R06', label: '纪委领导' },
  { value: 'R07', label: '党支部书记' },
  { value: 'R08', label: '支部组织委员' },
  { value: 'R12', label: '党费财务' },
  { value: 'R13', label: '新闻编辑' },
  { value: 'R14', label: '舆情专员' },
  { value: 'R20', label: '纪检监察室主任' },
  { value: 'R21', label: '审查调查人员' },
  { value: 'R22', label: '审理人员' },
  { value: 'R23', label: '信访接待/监督专员' },
  { value: 'R24', label: '巡察专员' },
  { value: 'R99', label: '普通党员(只读)' }
]

const STORAGE_KEY = 'party_role'

export const partyAuth = reactive({
  role: localStorage.getItem(STORAGE_KEY) || 'R02'
})

export function setPartyRole(role) {
  partyAuth.role = role
  localStorage.setItem(STORAGE_KEY, role)
}

/** 当前角色是否具备任一给定角色；R02 为全权限管理员。 */
export function can(...roles) {
  if (partyAuth.role === 'R02') return true
  return roles.includes(partyAuth.role)
}

export function roleLabel(role) {
  const hit = PARTY_ROLES.find(r => r.value === role)
  return hit ? hit.label : role
}

import axios from '@/api/axiosConfig'

/**
 * 流程引擎统一审批封装
 * 后端: FlowEngineMgt (8206) /api/flow
 * 启动实例: POST /api/flow/instance/start  body: FlowInstance { defCode, bizCode, bizId, bizJson, title, initiator }
 * 待办:     GET  /api/flow/task/todo?assignee=xx
 * 完成:     POST /api/flow/task/{id}/complete  body: { action, comment }
 */

// 流程定义编码（对应各 HRM 模块）
export const FLOW_DEF_CODES = {
  HRM_RECRUITMENT_REQ: 'HRM_RECRUITMENT_REQ', // 招聘需求
  HRM_JOB_DESC: 'HRM_JOB_DESC',               // 岗位说明书
  HRM_TRAINING_PLAN: 'HRM_TRAINING_PLAN',     // 培训计划
  HRM_PERF_EVAL: 'HRM_PERF_EVAL',             // 绩效评估
  HRM_WORKPLAN: 'HRM_WORKPLAN',               // 工作计划
  HRM_SUCCESSION: 'HRM_SUCCESSION',           // 继任管理
  HRM_QUALIFICATION: 'HRM_QUALIFICATION',     // 任职资格
  HRM_TALENT_DEV: 'HRM_TALENT_DEV',           // 人才培养
  HRM_HR_PLAN: 'HRM_HR_PLAN'                  // 人力规划
}

// 从响应中提取业务数据（后端统一 ResponseResult { code, message, data }）
const unwrap = (response) => {
  const body = response && response.data
  if (!body) return null
  if (body.code !== undefined && body.code !== 200) {
    throw new Error(body.message || '流程引擎调用失败')
  }
  return body.data !== undefined ? body.data : body
}

/**
 * 发起流程审批
 * @param {Object} params
 * @param {string} params.defCode    流程定义编码
 * @param {string} params.bizCode    业务模块编码
 * @param {string|number} params.bizId 业务主键
 * @param {Object|string} params.bizJson 业务数据
 * @param {string} params.title      流程标题
 * @param {string} params.initiator  发起人
 * @returns {Promise<string>} 实例号 instNo
 */
export const startApproval = ({ defCode, bizCode, bizId, bizJson, title, initiator }) => {
  const payload = {
    defCode,
    bizCode,
    bizId: String(bizId),
    bizJson: typeof bizJson === 'string' ? bizJson : JSON.stringify(bizJson || {}),
    title: title || defCode,
    initiator: initiator || localStorage.getItem('username') || 'dev-user'
  }
  return axios.post('/api/flow/instance/start', payload).then(res => {
    const inst = unwrap(res)
    return inst && (inst.instNo || inst.id)
  })
}

/**
 * 查询我的待办
 * @param {string} assignee 处理人
 * @returns {Promise<Array>} 待办任务列表
 */
export const getMyTodo = (assignee) => {
  const who = assignee || localStorage.getItem('username') || 'dev-user'
  return axios.get('/api/flow/task/todo', { params: { assignee: who } }).then(res => unwrap(res) || [])
}

/**
 * 完成审批任务
 * @param {string|number} id 任务ID
 * @param {string} action   动作（如 agree/reject）
 * @param {string} comment  审批意见
 * @returns {Promise<boolean>}
 */
export const completeTask = (id, action, comment) => {
  return axios.post(`/api/flow/task/${id}/complete`, { action, comment }).then(res => unwrap(res))
}

/**
 * 查询流程实例状态
 * @param {string|number} instId 实例ID或实例号
 * @returns {Promise<Object>} 实例信息（含 status/currentNode 等）
 */
export const getFlowStatus = (instId) => {
  return axios.get(`/api/flow/instance/${instId}`).then(res => unwrap(res))
}

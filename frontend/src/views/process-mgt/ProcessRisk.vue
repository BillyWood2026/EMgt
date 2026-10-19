<template>
  <div class="epros-risk">
    <div class="risk-header">
      <div class="header-title">
        <h2>流程风险与合规管控</h2>
        <p>流程风险嵌入、合规性保障、问题闭环机制</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" @click="addRiskPoint">新增风险点</el-button>
        <el-button icon="Download" @click="exportComplianceReport">导出合规报告</el-button>
      </div>
    </div>

    <div class="risk-body">
      <div class="compliance-summary">
        <div class="summary-card">
          <div class="summary-icon compliance">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="summary-info">
            <div class="summary-value">{{ summary.complianceRate }}%</div>
            <div class="summary-label">合规率</div>
          </div>
        </div>
        <div class="summary-card">
          <div class="summary-icon risk">
            <el-icon><WarnTriangleFilled /></el-icon>
          </div>
          <div class="summary-info">
            <div class="summary-value">{{ summary.riskCount }}</div>
            <div class="summary-label">风险点总数</div>
          </div>
        </div>
        <div class="summary-card">
          <div class="summary-icon issue">
            <el-icon><CircleClose /></el-icon>
          </div>
          <div class="summary-info">
            <div class="summary-value">{{ summary.issueCount }}</div>
            <div class="summary-label">待整改问题</div>
          </div>
        </div>
        <div class="summary-card">
          <div class="summary-icon audit">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="summary-info">
            <div class="summary-value">{{ summary.auditPassRate }}%</div>
            <div class="summary-label">审核通过率</div>
          </div>
        </div>
      </div>

      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="风险点管理" name="risk">
          <div class="risk-panel">
            <div class="risk-toolbar">
              <el-input v-model="searchKey" placeholder="搜索风险点名称" class="search-input" />
              <el-select v-model="filterSeverity" placeholder="风险等级">
                <el-option label="全部" value="" />
                <el-option label="高风险" value="high" />
                <el-option label="中风险" value="medium" />
                <el-option label="低风险" value="low" />
              </el-select>
              <el-select v-model="filterSystem" placeholder="所属体系">
                <el-option label="全部" value="" />
                <el-option label="ISO14001" value="iso14001" />
                <el-option label="ISO45001" value="iso45001" />
                <el-option label="ISO50001" value="iso50001" />
                <el-option label="安全生产标准化" value="safety" />
              </el-select>
              <el-button icon="Search" @click="doSearch">搜索</el-button>
            </div>
            
            <el-table :data="riskPoints" border>
              <el-table-column prop="code" label="风险编号" width="120" />
              <el-table-column prop="name" label="风险点名称" width="180" />
              <el-table-column prop="severity" label="风险等级" width="100">
                <template #default="{ row }">
                  <el-tag :type="getSeverityTagType(row.severity)">{{ getSeverityName(row.severity) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="relatedFlow" label="关联流程" width="180" />
              <el-table-column prop="relatedNode" label="关联节点" width="120" />
              <el-table-column prop="controlMeasure" label="管控措施" width="200" />
              <el-table-column prop="complianceClause" label="合规条款" width="150" />
              <el-table-column prop="prohibitedAction" label="禁止行为" width="150" />
              <el-table-column prop="actions" label="操作" width="180">
                <template #default="{ row }">
                  <el-button size="small" @click="viewRisk(row)">查看</el-button>
                  <el-button size="small" v-if="row._src === 'db'" @click="editRisk(row)">编辑</el-button>
                  <el-button size="small" v-if="row._src === 'db'" type="danger" @click="removeRisk(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="合规条款管理" name="compliance">
          <div class="compliance-panel">
            <div class="compliance-toolbar">
              <el-button type="primary" icon="Plus" @click="addClause">新增条款</el-button>
              <el-select v-model="filterStandard" placeholder="选择标准">
                <el-option label="全部" value="" />
                <el-option label="ISO14001:2015" value="iso14001" />
                <el-option label="ISO45001:2018" value="iso45001" />
                <el-option label="ISO50001:2018" value="iso50001" />
                <el-option label="安全生产标准化" value="safety" />
              </el-select>
            </div>
            
            <div class="standard-tabs">
              <el-tabs v-model="activeStandard" type="border-card">
                <el-tab-pane label="ISO14001" name="iso14001">
                  <div class="clause-tree">
                    <div v-for="clause in iso14001Clauses" :key="clause.id" class="clause-item">
                      <div class="clause-header" @click="toggleClause(clause)">
                        <el-icon>{{ clause.expanded ? Minus : Plus }}</el-icon>
                        <span class="clause-number">{{ clause.number }}</span>
                        <span class="clause-title">{{ clause.title }}</span>
                        <el-tag size="small" :type="clause.covered ? 'success' : 'danger'">
                          {{ clause.covered ? '已覆盖' : '未覆盖' }}
                        </el-tag>
                      </div>
                      <div v-if="clause.expanded" class="clause-content">
                        <div class="clause-desc">{{ clause.description }}</div>
                        <div class="related-flows">
                          <span class="label">关联流程:</span>
                          <span v-for="flow in clause.relatedFlows" :key="flow" class="flow-tag">{{ flow }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-tab-pane>
                
                <el-tab-pane label="ISO45001" name="iso45001">
                  <div class="clause-tree">
                    <div v-for="clause in iso45001Clauses" :key="clause.id" class="clause-item">
                      <div class="clause-header" @click="toggleClause(clause)">
                        <el-icon>{{ clause.expanded ? Minus : Plus }}</el-icon>
                        <span class="clause-number">{{ clause.number }}</span>
                        <span class="clause-title">{{ clause.title }}</span>
                        <el-tag size="small" :type="clause.covered ? 'success' : 'danger'">
                          {{ clause.covered ? '已覆盖' : '未覆盖' }}
                        </el-tag>
                      </div>
                      <div v-if="clause.expanded" class="clause-content">
                        <div class="clause-desc">{{ clause.description }}</div>
                        <div class="related-flows">
                          <span class="label">关联流程:</span>
                          <span v-for="flow in clause.relatedFlows" :key="flow" class="flow-tag">{{ flow }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-tab-pane>
                
                <el-tab-pane label="ISO50001" name="iso50001">
                  <div class="clause-tree">
                    <div v-for="clause in iso50001Clauses" :key="clause.id" class="clause-item">
                      <div class="clause-header" @click="toggleClause(clause)">
                        <el-icon>{{ clause.expanded ? Minus : Plus }}</el-icon>
                        <span class="clause-number">{{ clause.number }}</span>
                        <span class="clause-title">{{ clause.title }}</span>
                        <el-tag size="small" :type="clause.covered ? 'success' : 'danger'">
                          {{ clause.covered ? '已覆盖' : '未覆盖' }}
                        </el-tag>
                      </div>
                      <div v-if="clause.expanded" class="clause-content">
                        <div class="clause-desc">{{ clause.description }}</div>
                        <div class="related-flows">
                          <span class="label">关联流程:</span>
                          <span v-for="flow in clause.relatedFlows" :key="flow" class="flow-tag">{{ flow }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-tab-pane>
                
                <el-tab-pane label="安全生产标准化" name="safety">
                  <div class="clause-tree">
                    <div v-for="clause in safetyClauses" :key="clause.id" class="clause-item">
                      <div class="clause-header" @click="toggleClause(clause)">
                        <el-icon>{{ clause.expanded ? Minus : Plus }}</el-icon>
                        <span class="clause-number">{{ clause.number }}</span>
                        <span class="clause-title">{{ clause.title }}</span>
                        <el-tag size="small" :type="clause.covered ? 'success' : 'danger'">
                          {{ clause.covered ? '已覆盖' : '未覆盖' }}
                        </el-tag>
                      </div>
                      <div v-if="clause.expanded" class="clause-content">
                        <div class="clause-desc">{{ clause.description }}</div>
                        <div class="related-flows">
                          <span class="label">关联流程:</span>
                          <span v-for="flow in clause.relatedFlows" :key="flow" class="flow-tag">{{ flow }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="问题闭环台账" name="issue">
          <div class="issue-panel">
            <div class="issue-toolbar">
              <el-button type="primary" icon="Plus" @click="addIssue">新增问题</el-button>
              <el-select v-model="filterIssueStatus" placeholder="状态筛选">
                <el-option label="全部" value="" />
                <el-option label="待整改" value="pending" />
                <el-option label="整改中" value="processing" />
                <el-option label="待验证" value="verify" />
                <el-option label="已闭环" value="closed" />
                <el-option label="复盘" value="review" />
              </el-select>
            </div>
            
            <div class="issue-stats">
              <div class="stat-item">
                <span class="stat-value">{{ issueStats.total }}</span>
                <span class="stat-label">问题总数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ issueStats.pending }}</span>
                <span class="stat-label">待整改</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ issueStats.processing }}</span>
                <span class="stat-label">整改中</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ issueStats.closed }}</span>
                <span class="stat-label">已闭环</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ issueStats.closeRate }}%</span>
                <span class="stat-label">闭环率</span>
              </div>
            </div>
            
            <el-table :data="issueList" border>
              <el-table-column prop="code" label="问题编号" width="120" />
              <el-table-column prop="type" label="问题类型" width="100">
                <template #default="{ row }">
                  <el-tag :type="getIssueTypeTagType(row.type)">{{ getIssueTypeName(row.type) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="title" label="问题标题" width="180" />
              <el-table-column prop="relatedFlow" label="关联流程" width="150" />
              <el-table-column prop="detectedTime" label="发现时间" width="140" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getIssueStatusTagType(row.status)">{{ getIssueStatusName(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="deadline" label="整改期限" width="120" />
              <el-table-column prop="actions" label="操作" width="180">
                <template #default="{ row }">
                  <el-button size="small" @click="viewIssue(row)">查看</el-button>
                  <el-button size="small" v-if="row.status === 'pending'" type="primary" @click="startFix(row)">整改</el-button>
                  <el-button size="small" v-if="row.status === 'processing'" @click="submitVerify(row)">提交验证</el-button>
                  <el-button size="small" v-if="row.status === 'verify'" type="success" @click="verify(row)">验证</el-button>
                  <el-button size="small" v-if="row.status === 'closed'" @click="review(row)">复盘</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 风险点新增/编辑弹窗 -->
    <el-dialog v-model="riskVis" :title="riskForm.id ? '编辑风险点' : '新增风险点'" width="680px">
      <el-form :model="riskForm" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="风险点名称" required>
              <el-input v-model="riskForm.name" placeholder="请输入风险点名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="风险等级" required>
              <el-select v-model="riskForm.severity" placeholder="请选择风险等级">
                <el-option label="高风险" value="high" />
                <el-option label="中风险" value="medium" />
                <el-option label="低风险" value="low" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="关联流程">
              <el-input v-model="riskForm.relatedFlow" placeholder="请输入关联流程" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联节点">
              <el-input v-model="riskForm.relatedNode" placeholder="请输入关联节点" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="管控措施">
          <el-input v-model="riskForm.controlMeasure" type="textarea" :rows="2" placeholder="请输入管控措施" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="合规条款">
              <el-input v-model="riskForm.complianceClause" placeholder="请输入合规条款" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="禁止行为">
              <el-input v-model="riskForm.prohibitedAction" placeholder="请输入禁止行为" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="riskForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="riskVis = false">取消</el-button>
        <el-button type="primary" @click="saveRisk">保存</el-button>
      </template>
    </el-dialog>

    <!-- 风险点详情弹窗 -->
    <el-dialog v-model="detailVis" title="风险点详情" width="640px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="风险编号">{{ detailRow.code || '-' }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag :type="getSeverityTagType(detailRow.severity)">{{ getSeverityName(detailRow.severity) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="风险点名称" :span="2">{{ detailRow.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="关联流程">{{ detailRow.relatedFlow || '-' }}</el-descriptions-item>
        <el-descriptions-item label="关联节点">{{ detailRow.relatedNode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="管控措施" :span="2">{{ detailRow.controlMeasure || '-' }}</el-descriptions-item>
        <el-descriptions-item label="合规条款">{{ detailRow.complianceClause || '-' }}</el-descriptions-item>
        <el-descriptions-item label="禁止行为">{{ detailRow.prohibitedAction || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailRow.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="数据来源" :span="2">
          {{ detailRow._src === 'db' ? '人工登记' : '流程实例派生' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Minus, Download, Search, Warning, WarnTriangleFilled, CircleClose, CircleCheck } from '@element-plus/icons-vue'
import axios from '@/api/axiosConfig'

const activeTab = ref('risk')
const activeStandard = ref('iso14001')
const searchKey = ref('')
const filterSeverity = ref('')
const filterSystem = ref('')
const filterStandard = ref('')
const filterIssueStatus = ref('')

const loading = ref(false)
// 后端流程实例与审批记录（用于风险/问题前端聚合）
const instances = ref([])
const approvals = ref([])
// 人工登记的风险点（process_risk_point 表）
const dbRisks = ref([])

const riskVis = ref(false)
const riskForm = reactive({
  id: null, name: '', severity: 'medium', relatedFlow: '', relatedNode: '',
  controlMeasure: '', complianceClause: '', prohibitedAction: '', remark: ''
})
const detailVis = ref(false)
const detailRow = ref({})

const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

// 风险点 = 人工登记行（_src=db，可编辑删除）+ 驳回/取消实例派生行（_src=derived，只读）
const riskPoints = computed(() => {
  let rows = dbRisks.value.map(r => ({ ...r, _src: 'db' }))
  rows = rows.concat(
    instances.value
      .filter(r => ['rejected', 'cancelled'].includes(r.status))
      .map(r => ({
        id: 'd' + r.id,
        _src: 'derived',
        code: r.processCode || ('RISK-' + r.id),
        name: (r.processName || '流程#' + r.id) + ' 风险',
        severity: r.status === 'rejected' ? 'high' : 'medium',
        relatedFlow: r.processName || '-',
        relatedNode: r.currentNode || '-',
        controlMeasure: '-',
        complianceClause: '-',
        prohibitedAction: r.status === 'rejected' ? '禁止违规审批' : '禁止无故取消流程'
      }))
  )
  if (searchKey.value) rows = rows.filter(r => (r.name || '').includes(searchKey.value))
  if (filterSeverity.value) rows = rows.filter(r => r.severity === filterSeverity.value)
  return rows
})

// 问题闭环台账：由驳回审批记录派生
const issueList = computed(() => {
  let rows = approvals.value
    .filter(a => a.status === 'rejected')
    .map(a => ({
      id: a.id,
      code: 'ISSUE-' + a.id,
      type: 'deviation',
      title: (a.processName || '流程') + ' 审批驳回',
      relatedFlow: a.processName || '-',
      detectedTime: fmtTime(a.createTime),
      status: 'pending',
      deadline: '-'
    }))
  if (filterIssueStatus.value) rows = rows.filter(r => r.status === filterIssueStatus.value)
  return rows
})

const issueStats = computed(() => {
  const total = issueList.value.length
  const pending = issueList.value.filter(r => r.status === 'pending').length
  const processing = issueList.value.filter(r => r.status === 'processing').length
  const closed = issueList.value.filter(r => r.status === 'closed').length
  return {
    total,
    pending,
    processing,
    closed,
    closeRate: total ? +((closed / total) * 100).toFixed(1) : 0
  }
})

const summary = computed(() => {
  const instTotal = instances.value.length || 1
  const rejected = instances.value.filter(r => r.status === 'rejected').length
  const cancelled = instances.value.filter(r => r.status === 'cancelled').length
  const approved = instances.value.filter(r => r.status === 'approved').length
  const apprTotal = approvals.value.length || 1
  const apprApproved = approvals.value.filter(a => a.status === 'approved').length
  return {
    complianceRate: +(((instTotal - rejected - cancelled) / instTotal) * 100).toFixed(1),
    riskCount: riskPoints.value.length,
    issueCount: issueList.value.length,
    auditPassRate: +((apprApproved / apprTotal) * 100).toFixed(1)
  }
})

// 合规条款：后端无对应实体，使用真实空态（不灌假数据）
const iso14001Clauses = ref([])
const iso45001Clauses = ref([])
const iso50001Clauses = ref([])
const safetyClauses = ref([])

const getSeverityTagType = (severity) => {
  const map = { high: 'danger', medium: 'warning', low: 'info' }
  return map[severity] || 'info'
}

const getSeverityName = (severity) => {
  const map = { high: '高风险', medium: '中风险', low: '低风险' }
  return map[severity] || severity
}

const getIssueTypeTagType = (type) => {
  const map = { deviation: 'warning', violation: 'danger', form_missing: 'warning', breakpoint: 'danger' }
  return map[type] || 'info'
}

const getIssueTypeName = (type) => {
  const map = { deviation: '执行偏差', violation: '违规操作', form_missing: '表单缺失', breakpoint: '流程断点' }
  return map[type] || type
}

const getIssueStatusTagType = (status) => {
  const map = { pending: 'danger', processing: 'warning', verify: 'info', closed: 'success', review: 'info' }
  return map[status] || 'info'
}

const getIssueStatusName = (status) => {
  const map = { pending: '待整改', processing: '整改中', verify: '待验证', closed: '已闭环', review: '复盘中' }
  return map[status] || status
}

const toggleClause = (clause) => {
  clause.expanded = !clause.expanded
}

const addRiskPoint = () => {
  Object.assign(riskForm, {
    id: null, name: '', severity: 'medium', relatedFlow: '', relatedNode: '',
    controlMeasure: '', complianceClause: '', prohibitedAction: '', remark: ''
  })
  riskVis.value = true
}

const saveRisk = async () => {
  if (!riskForm.name) { ElMessage.warning('请输入风险点名称'); return }
  if (!riskForm.severity) { ElMessage.warning('请选择风险等级'); return }
  try {
    const r = riskForm.id
      ? await axios.put(`/api/process/risk-point/${riskForm.id}`, riskForm)
      : await axios.post('/api/process/risk-point', riskForm)
    if (r.data && r.data.code === 200) {
      ElMessage.success(riskForm.id ? '风险点已更新' : '风险点已创建')
      riskVis.value = false
      loadRisks()
    } else {
      ElMessage.error((r.data && r.data.message) || '保存失败')
    }
  } catch (e) {
    const d = e.response && e.response.data
    ElMessage.error((d && (d.message || d.msg)) || '保存失败')
  }
}

const removeRisk = (row) => {
  ElMessageBox.confirm(`确定删除风险点 "${row.name}" 吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      try {
        const r = await axios.delete(`/api/process/risk-point/${row.id}`)
        if (r.data && r.data.code === 200) {
          ElMessage.success('删除成功')
          loadRisks()
        } else {
          ElMessage.error((r.data && r.data.message) || '删除失败')
        }
      } catch (e) {
        const d = e.response && e.response.data
        ElMessage.error((d && (d.message || d.msg)) || '删除失败')
      }
    }).catch(() => {})
}

const loadRisks = async () => {
  try {
    const r = await axios.get('/api/process/risk-point/list', { params: { keyword: '', severity: '' } })
    dbRisks.value = (r.data && r.data.data) || []
  } catch (e) {
    console.error('加载风险点失败', e)
  }
}

const exportComplianceReport = () => {
  ElMessage.success('正在导出合规报告...')
}

const doSearch = () => {
  ElMessage.success('搜索完成')
}

const viewRisk = (row) => {
  detailRow.value = row
  detailVis.value = true
}

const editRisk = (row) => {
  if (row._src !== 'db') {
    ElMessage.info('流程实例派生风险不可编辑，请通过流程实例维护')
    return
  }
  Object.assign(riskForm, {
    id: row.id, name: row.name, severity: row.severity, relatedFlow: row.relatedFlow,
    relatedNode: row.relatedNode, controlMeasure: row.controlMeasure,
    complianceClause: row.complianceClause, prohibitedAction: row.prohibitedAction, remark: row.remark
  })
  riskVis.value = true
}

const addClause = () => {
  ElMessage.info('新增合规条款（后端暂无条款实体）')
}

const addIssue = () => {
  ElMessage.info('新增问题（请通过审批驳回流程产生）')
}

const viewIssue = (row) => {
  ElMessage.info(`查看问题: ${row.title}`)
}

const startFix = (row) => {
  ElMessage.success(`开始整改: ${row.title}`)
  row.status = 'processing'
}

const submitVerify = (row) => {
  ElMessage.success(`提交验证: ${row.title}`)
  row.status = 'verify'
}

const verify = (row) => {
  ElMessageBox.confirm(
    `确定验证通过问题 "${row.title}" 吗？`,
    '验证确认',
    { confirmButtonText: '通过', cancelButtonText: '取消' }
  ).then(() => {
    row.status = 'closed'
    ElMessage.success('验证通过')
  }).catch(() => {})
}

const review = (row) => {
  ElMessage.info(`复盘问题: ${row.title}`)
  row.status = 'review'
}

const loadAll = async () => {
  loading.value = true
  try {
    const [instRes, apprRes, riskRes] = await Promise.all([
      axios.get('/api/process/instance/list', { params: { pageNum: 1, pageSize: 200 } }),
      axios.get('/api/process/approval/list', { params: { pageNum: 1, pageSize: 200 } }),
      axios.get('/api/process/risk-point/list', { params: { keyword: '', severity: '' } })
    ])
    const instPage = instRes.data && instRes.data.data ? instRes.data.data : {}
    const apprPage = apprRes.data && apprRes.data.data ? apprRes.data.data : {}
    instances.value = instPage.records || []
    approvals.value = apprPage.records || []
    dbRisks.value = (riskRes.data && riskRes.data.data) || []
  } catch (e) {
    console.error('加载流程风险数据失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAll()
})
</script>

<style lang="scss" scoped>
.epros-risk {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.risk-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
  color: #fff;
  
  .header-title {
    h2 {
      margin: 0 0 4px 0;
      font-size: 18px;
    }
    p {
      margin: 0;
      font-size: 12px;
      opacity: 0.8;
    }
  }
  
  .header-actions {
    display: flex;
    gap: 8px;
  }
}

.risk-body {
  flex: 1;
  padding: 16px;
  overflow: hidden;
  
  .el-tabs {
    height: calc(100% - 80px);
  }
  
  .el-tabs__content {
    height: calc(100% - 48px);
    overflow: auto;
  }
}

.compliance-summary {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  
  .summary-card {
    flex: 1;
    display: flex;
    align-items: center;
    padding: 16px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    
    .summary-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      margin-right: 12px;
      
      &.compliance { background: #e8f5e9; color: #2e7d32; }
      &.risk { background: #fff3e0; color: #e65100; }
      &.issue { background: #ffebee; color: #c62828; }
      &.audit { background: #e3f2fd; color: #1565c0; }
    }
    
    .summary-info {
      .summary-value {
        font-size: 28px;
        font-weight: 600;
        color: #333;
      }
      .summary-label {
        font-size: 12px;
        color: #666;
      }
    }
  }
}

.risk-panel, .issue-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.risk-toolbar, .issue-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  
  .search-input {
    width: 200px;
  }
}

.compliance-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.compliance-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.standard-tabs {
  flex: 1;
  overflow: auto;
}

.clause-tree {
  padding: 12px;
}

.clause-item {
  margin-bottom: 8px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  overflow: hidden;
  
  .clause-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    background: #fafafa;
    cursor: pointer;
    
    &:hover {
      background: #f5f5f5;
    }
    
    .clause-number {
      font-weight: 600;
      color: #3b82f6;
    }
    
    .clause-title {
      flex: 1;
    }
  }
  
  .clause-content {
    padding: 12px;
    border-top: 1px solid #e8e8e8;
    
    .clause-desc {
      font-size: 13px;
      color: #666;
      margin-bottom: 12px;
    }
    
    .related-flows {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      
      .label {
        font-size: 12px;
        color: #999;
      }
      
      .flow-tag {
        padding: 4px 8px;
        background: #e3f2fd;
        border-radius: 4px;
        font-size: 12px;
        color: #1565c0;
      }
    }
  }
}

.issue-stats {
  display: flex;
  gap: 24px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
  
  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .stat-value {
      font-size: 24px;
      font-weight: 600;
      color: #333;
    }
    
    .stat-label {
      font-size: 12px;
      color: #666;
    }
  }
}
</style>

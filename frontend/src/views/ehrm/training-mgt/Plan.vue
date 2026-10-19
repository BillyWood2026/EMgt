<template>
  <div class="page-card">
    <div class="page-header">
      <h2>培训计划</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新建计划
        </el-button>
      </div>
    </div>

    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="计划名称">
          <el-input v-model="searchForm.planName" placeholder="请输入计划名称" style="width:200px" />
        </el-form-item>
        <el-form-item label="培训类型">
          <el-select v-model="searchForm.trainingType" placeholder="选择类型" style="width:150px">
            <el-option label="新员工培训" value="ONBOARDING" />
            <el-option label="合规培训" value="COMPLIANCE" />
            <el-option label="岗位技能培训" value="SKILL" />
            <el-option label="管理能力培训" value="MANAGEMENT" />
            <el-option label="高管培训" value="EXECUTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="planList" border stripe size="small" v-loading="loading">
        <el-table-column prop="planCode" label="计划编号" width="140" />
        <el-table-column prop="planName" label="计划名称" min-width="200">
          <template #default="{row}">
            <el-link type="primary" @click="viewDetail(row)">{{ row.planName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="trainingType" label="培训类型" width="140">
          <template #default="{row}">
            <el-tag size="small">{{ getTypeLabel(row.trainingType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cycleStartDate" label="开始日期" width="120" />
        <el-table-column prop="cycleEndDate" label="结束日期" width="120" />
        <el-table-column prop="plannedParticipants" label="参训人数" width="90" />
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="applicableOrg" label="适用组织" width="120">
          <template #default="{row}">{{ row.applicableOrg || '—' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="plannedHours" label="计划课时" width="90">
          <template #default="{row}">{{ row.plannedHours ?? '—' }}</template>
        </el-table-column>
        <el-table-column prop="totalBudget" label="预算总额" width="110">
          <template #default="{row}">{{ row.totalBudget }}元</template>
        </el-table-column>
        <!-- 规格书字段（已用预算/剩余预算），待后端扩展；预算执行率为前端算法展示 -->
        <el-table-column label="预算执行" width="160">
          <template #default="{row}">
            <el-progress :percentage="getBudgetRate(row)" :status="getBudgetRate(row) >= 90 ? 'exception' : undefined" :stroke-width="10" />
            <div style="font-size:12px;color:#909399">已用 {{ row.usedBudget ?? 0 }} / 剩余 {{ (row.totalBudget || 0) - (row.usedBudget || 0) }} 元</div>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="approvalProgress" label="审批进度" width="110">
          <template #default="{row}">{{ row.approvalProgress || '—' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="计划状态" width="100">
          <template #default="{row}">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" type="warning" v-if="row.status === 'DRAFT'" @click="submitPlan(row)">提交审批</el-button>
            <el-button link size="small" v-if="row.status === 'PENDING_CLASS'" @click="startPlan(row)">开始执行</el-button>
            <el-button link size="small" v-if="row.status === 'PROGRESS'" @click="endPlan(row)">结束</el-button>
            <el-button link size="small" type="info" v-if="row.status === 'COMPLETED'" @click="archivePlan(row)">归档</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="950px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基础信息" name="base">
      <el-form :model="formData" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划名称" required>
              <el-input v-model="formData.planName" placeholder="请输入计划名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="培训类型" required>
              <el-select v-model="formData.trainingType" placeholder="选择类型">
                <el-option label="新员工培训" value="ONBOARDING" />
                <el-option label="合规培训" value="COMPLIANCE" />
                <el-option label="岗位技能培训" value="SKILL" />
                <el-option label="管理能力培训" value="MANAGEMENT" />
                <el-option label="高管培训" value="EXECUTIVE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" required>
              <el-date-picker v-model="formData.cycleStartDate" type="date" placeholder="选择开始日期" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" required>
              <el-date-picker v-model="formData.cycleEndDate" type="date" placeholder="选择结束日期" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预算总额">
              <el-input-number v-model="formData.totalBudget" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="参训人数">
              <el-input-number v-model="formData.plannedParticipants" :min="1" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="适用组织">
              <el-input v-model="formData.applicableOrg" placeholder="请输入适用组织" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位序列">
              <el-input v-model="formData.jobSequence" placeholder="请输入适用岗位序列" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划课时">
              <el-input-number v-model="formData.plannedHours" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="已用预算">
              <el-input-number v-model="formData.usedBudget" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="考核方式">
          <el-input type="textarea" v-model="formData.assessmentMethod" :rows="3" placeholder="请输入考核方式" />
        </el-form-item>
        <el-form-item label="完成标准">
          <el-input type="textarea" v-model="formData.completionCriteria" :rows="4" placeholder="请输入完成标准" />
        </el-form-item>
      </el-form>
        </el-tab-pane>
        <!-- EL-TR01 培训需求来源（规格书字段，待后端扩展） -->
        <el-tab-pane label="需求来源" name="source">
          <el-form :model="formData" label-width="140px">
            <el-form-item label="绩效弱项指标"><el-input v-model="formData.perfWeakness" placeholder="绩效短板指标" /></el-form-item>
            <el-form-item label="胜任力短板"><el-input v-model="formData.competencyGap" placeholder="岗位胜任力缺口" /></el-form-item>
            <el-form-item label="员工自评需求"><el-input v-model="formData.selfEvalNeeds" placeholder="员工自评培训需求" /></el-form-item>
            <el-form-item label="需求优先级">
              <el-select v-model="formData.needPriority" placeholder="选择优先级" style="width:100%">
                <el-option label="高" value="高" /><el-option label="中" value="中" /><el-option label="低" value="低" />
              </el-select>
            </el-form-item>
            <el-form-item label="是否必修">
              <el-switch v-model="formData.isMandatory" active-text="必修" inactive-text="选修" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <!-- EL-TR03 培训预算明细（规格书字段，待后端扩展） -->
        <el-tab-pane label="预算配置" name="budget">
          <el-form :model="formData" label-width="140px">
            <el-row :gutter="20">
              <el-col :span="12"><el-form-item label="讲师费"><el-input-number v-model="formData.budgetLecturer" :min="0" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="场地费"><el-input-number v-model="formData.budgetVenue" :min="0" style="width:100%" /></el-form-item></el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12"><el-form-item label="物料费"><el-input-number v-model="formData.budgetMaterial" :min="0" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="差旅住宿费"><el-input-number v-model="formData.budgetTravel" :min="0" style="width:100%" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="预算预警阈值(%)"><el-input-number v-model="formData.budgetWarnThreshold" :min="0" :max="100" style="width:200px" /></el-form-item>
          </el-form>
        </el-tab-pane>
        <!-- EL-TR05 必修管控规则（规格书字段，待后端扩展） -->
        <el-tab-pane label="管控规则" name="rule">
          <el-form :model="formData" label-width="160px">
            <el-form-item label="岗位准入必修">
              <el-switch v-model="formData.jobAccessRequired" />
            </el-form-item>
            <el-form-item label="未完成锁定权限">
              <el-switch v-model="formData.lockOnIncomplete" />
            </el-form-item>
            <el-form-item label="逾期预警周期(天)">
              <el-input-number v-model="formData.overdueWarnDays" :min="1" :max="90" style="width:200px" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <!-- 审批预览（规格书：部门级/公司级/干部专项分级审批） -->
        <el-tab-pane label="审批预览" name="approval">
          <el-steps :active="2" align-center finish-status="success">
            <el-step title="部门级审批" description="HRBP 终审" />
            <el-step title="公司级审批" description="财务 + 人力总监终审" />
            <el-step title="干部专项审批" description="党委终审（仅干部专项）" />
          </el-steps>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { startApproval, FLOW_DEF_CODES } from '@/api/flowApproval'

const loading = ref(false)
const searchForm = reactive({
  planName: '',
  trainingType: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('新建培训计划')
const activeTab = ref('base')

const formData = reactive({
  id: null,
  planCode: '',
  planName: '',
  trainingType: '',
  cycleStartDate: '',
  cycleEndDate: '',
  totalBudget: 0,
  plannedParticipants: 0,
  assessmentMethod: '',
  completionCriteria: '',
  status: 'DRAFT',
  // 规格书字段，待后端扩展
  applicableOrg: '',
  jobSequence: '',
  plannedHours: 0,
  usedBudget: 0,
  approvalProgress: '',
  perfWeakness: '',
  competencyGap: '',
  selfEvalNeeds: '',
  needPriority: '',
  isMandatory: false,
  budgetLecturer: 0,
  budgetVenue: 0,
  budgetMaterial: 0,
  budgetTravel: 0,
  budgetWarnThreshold: 90,
  jobAccessRequired: false,
  lockOnIncomplete: false,
  overdueWarnDays: 7
})

// 预算执行率算法：已用预算 / 预算总额 * 100
function getBudgetRate(row) {
  const total = Number(row.totalBudget || 0)
  if (!total) return 0
  return Math.min(100, Math.round(Number(row.usedBudget || 0) / total * 100))
}

const planList = ref([])

function getTypeLabel(type) {
  const labels = { 'ONBOARDING': '新员工培训', 'COMPLIANCE': '合规培训', 'SKILL': '岗位技能培训', 'MANAGEMENT': '管理能力培训', 'EXECUTIVE': '高管培训' }
  return labels[type] || type
}

function getStatusType(status) {
  const types = { 'DRAFT': 'info', 'APPROVAL': 'warning', '已提交': 'warning', 'PENDING_CLASS': 'warning', 'PROGRESS': 'primary', 'COMPLETED': 'success', 'ARCHIVED': 'info' }
  return types[status] || 'info'
}

function getStatusLabel(status) {
  const labels = { 'DRAFT': '草稿', 'APPROVAL': '审批中', '已提交': '审批中', 'PENDING_CLASS': '待开班', 'PROGRESS': '学习中', 'COMPLETED': '已结项', 'ARCHIVED': '已归档' }
  return labels[status] || status
}

async function loadData() {
  loading.value = true
  try {
    const params = {}
    if (searchForm.trainingType) params.trainingType = searchForm.trainingType
    if (searchForm.planName) params.planName = searchForm.planName
    const res = await axios.get('/api/training/plan', { params })
    if (res.data.code === 200) {
      planList.value = res.data.data || []
    }
  } catch (e) {
    ElMessage.error('加载培训计划失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadData()
}

function resetSearch() {
  searchForm.planName = ''
  searchForm.trainingType = ''
  loadData()
}

function openAddDialog() {
  dialogTitle.value = '新建培训计划'
  activeTab.value = 'base'
  Object.assign(formData, {
    id: null,
    planCode: 'PX-' + new Date().getFullYear() + '-Q' + Math.ceil(new Date().getMonth() / 3) + '-' + String(planList.value.length + 1).padStart(3, '0'),
    planName: '',
    trainingType: '',
    cycleStartDate: '',
    cycleEndDate: '',
    totalBudget: 0,
    plannedParticipants: 0,
    assessmentMethod: '',
    completionCriteria: '',
    status: 'DRAFT',
    applicableOrg: '',
    jobSequence: '',
    plannedHours: 0,
    usedBudget: 0,
    approvalProgress: '',
    perfWeakness: '',
    competencyGap: '',
    selfEvalNeeds: '',
    needPriority: '',
    isMandatory: false,
    budgetLecturer: 0,
    budgetVenue: 0,
    budgetMaterial: 0,
    budgetTravel: 0,
    budgetWarnThreshold: 90,
    jobAccessRequired: false,
    lockOnIncomplete: false,
    overdueWarnDays: 7
  })
  dialogVisible.value = true
}

// 提交审批（DRAFT -> 已提交，走后端 /submit 端点）
function submitPlan(row) {
  ElMessageBox.confirm(`确定将培训计划"${row.planName}"提交审批吗？`, '提交审批', { type: 'warning' })
    .then(async () => {
      try {
        const instNo = await startApproval({
          defCode: FLOW_DEF_CODES.HRM_TRAINING_PLAN,
          bizCode: 'training',
          bizId: row.id,
          bizJson: row,
          title: `培训计划-${row.planName || row.planCode}`,
          initiator: localStorage.getItem('username') || 'dev-user'
        })
        const res = await axios.post(`/api/training/plan/${row.id}/submit`)
        if (res.data.code === 200) {
          ElMessage.success('已发起流程审批，实例号：' + instNo)
          loadData()
        } else {
          ElMessage.error(res.data.message || '提交失败')
        }
      } catch (e) {
        ElMessage.warning('流程引擎不可用，已本地提交')
        try {
          const res = await axios.post(`/api/training/plan/${row.id}/submit`)
          if (res.data.code === 200) {
            ElMessage.success('已提交审批')
            loadData()
          } else {
            ElMessage.error(res.data.message || '提交失败')
          }
        } catch (e2) {
          ElMessage.error('提交失败')
          console.error(e2)
        }
      }
    })
    .catch(() => {})
}

// 结项归档（COMPLETED -> ARCHIVED，走通用更新端点）
function archivePlan(row) {
  ElMessageBox.confirm(`归档后数据仅归档不参与考核，确定归档"${row.planName}"吗？`, '结项归档', { type: 'warning', confirmButtonClass: 'el-button--danger' })
    .then(async () => {
      try {
        const res = await axios.put('/api/training/plan', { ...row, status: 'ARCHIVED' })
        if (res.data.code === 200) {
          ElMessage.success('已归档')
          loadData()
        } else {
          ElMessage.error(res.data.message || '归档失败')
        }
      } catch (e) {
        ElMessage.error('归档失败')
        console.error(e)
      }
    })
    .catch(() => {})
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/training/plan/${row.id}`)
    if (res.data.code === 200) {
      Object.assign(formData, res.data.data)
      activeTab.value = 'base'
      dialogTitle.value = '查看培训计划'
      dialogVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取培训计划详情失败')
    console.error(e)
  }
}

function startPlan(row) {
  ElMessageBox.confirm(`确定要开始执行培训计划"${row.planName}"吗？`, '开始执行', { type: 'warning' })
    .then(async () => {
      try {
        const res = await axios.post(`/api/training/plan/${row.id}/submit`)
        if (res.data.code === 200) {
          ElMessage.success('培训计划已开始执行')
          loadData()
        } else {
          ElMessage.error(res.data.message || '开始执行失败')
        }
      } catch (e) {
        ElMessage.error('开始执行失败')
        console.error(e)
      }
    })
    .catch(() => {})
}

function endPlan(row) {
  ElMessageBox.confirm(`确定要结束培训计划"${row.planName}"吗？`, '结束确认', { type: 'warning' })
    .then(async () => {
      try {
        const res = await axios.put('/api/training/plan', { ...row, status: 'COMPLETED' })
        if (res.data.code === 200) {
          ElMessage.success('培训计划已结束')
          loadData()
        } else {
          ElMessage.error(res.data.message || '结束失败')
        }
      } catch (e) {
        ElMessage.error('结束失败')
        console.error(e)
      }
    })
    .catch(() => {})
}

async function handleSubmit() {
  try {
    let res
    if (formData.id) {
      res = await axios.put('/api/training/plan', formData)
    } else {
      res = await axios.post('/api/training/plan', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success('培训计划已保存')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
    console.error(e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.search-form { margin-bottom: 0; }
</style>
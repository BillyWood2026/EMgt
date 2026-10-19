<template>
  <div class="page-container">
    <RoleBar />
    <div class="page-header">
      <h2>党费使用管理</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- ==================== 1. 年度计划 ==================== -->
      <el-tab-pane label="年度计划" name="plan">
        <div class="toolbar">
          <el-input-number v-model="planFilter.orgId" :min="0" controls-position="right" placeholder="组织ID" style="width:150px" />
          <el-input-number v-model="planFilter.year" :min="2000" :max="2100" controls-position="right" placeholder="年份" style="width:120px" />
          <el-button type="primary" @click="loadPlans">查询</el-button>
          <el-button v-if="can('R02','R04')" type="primary" style="margin-left:auto" @click="openPlanDialog">新建计划</el-button>
        </div>
        <el-table :data="plans" border stripe size="small" v-loading="planLoading">
          <el-table-column prop="orgName" label="组织名称" min-width="160" />
          <el-table-column prop="planYear" label="计划年度" width="100" align="center" />
          <el-table-column prop="plannedAmount" label="计划使用金额" width="130" align="right" />
          <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        </el-table>
      </el-tab-pane>

      <!-- ==================== 2. 使用登记 ==================== -->
      <el-tab-pane label="使用登记" name="expense">
        <div class="toolbar">
          <el-input-number v-model="expenseFilter.orgId" :min="0" controls-position="right" placeholder="组织ID" style="width:150px" />
          <el-input-number v-model="expenseFilter.year" :min="2000" :max="2100" controls-position="right" placeholder="年份" style="width:120px" />
          <el-select v-model="expenseFilter.auditStatus" placeholder="审批状态" clearable style="width:130px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待审批" value="PENDING" />
            <el-option label="已批准" value="APPROVED" />
            <el-option label="已登记" value="REGISTERED" />
            <el-option label="已核销" value="VERIFIED" />
          </el-select>
          <el-button type="primary" @click="loadExpenses">查询</el-button>
          <el-button v-if="can('R02','R07','R08','R12')" type="primary" style="margin-left:auto" @click="openExpenseDialog">新建使用单</el-button>
        </div>

        <el-row :gutter="12" class="stats-cards">
          <el-col :span="6"><el-card shadow="hover"><div class="stat-label">本年收缴</div><div class="stat-value">{{ summary.collected ?? '-' }}</div></el-card></el-col>
          <el-col :span="6"><el-card shadow="hover"><div class="stat-label">本年上缴</div><div class="stat-value">{{ summary.remitted ?? '-' }}</div></el-card></el-col>
          <el-col :span="6"><el-card shadow="hover"><div class="stat-label">本年使用</div><div class="stat-value">{{ summary.used ?? '-' }}</div></el-card></el-col>
          <el-col :span="6"><el-card shadow="hover"><div class="stat-label">可用余额</div><div class="stat-value">{{ summary.availableBalance ?? '-' }}</div></el-card></el-col>
        </el-row>

        <el-table :data="expenses" border stripe size="small" v-loading="expenseLoading">
          <el-table-column prop="title" label="使用事项" min-width="160" show-overflow-tooltip />
          <el-table-column prop="usageType" label="使用类别" min-width="220" show-overflow-tooltip />
          <el-table-column prop="amount" label="金额" width="110" align="right" />
          <el-table-column prop="expenseDate" label="使用日期" width="110" align="center" />
          <el-table-column prop="auditStatus" label="审批状态" width="100">
            <template #default="{ row }">
              <el-tag :type="expenseStatusTagType(row.auditStatus)" size="small">{{ expenseStatusLabel(row.auditStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="handler" label="经办人" width="100" />
          <el-table-column label="操作" width="170" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="can('R03') && row.auditStatus === 'PENDING'"
                size="small"
                type="success"
                link
                @click="openAuditDialog(row)"
              >审批</el-button>
              <el-button
                v-if="can('R12') && row.auditStatus === 'APPROVED'"
                size="small"
                type="primary"
                link
                @click="openRegisterDialog(row)"
              >登记执行结果</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 新建计划弹窗 -->
    <el-dialog v-model="planDialogVisible" title="新建年度使用计划" width="560px" :close-on-click-modal="false">
      <el-form :model="planForm" label-width="110px">
        <el-form-item label="组织ID" required>
          <el-input-number v-model="planForm.orgId" :min="1" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="组织名称" required>
          <el-input v-model="planForm.orgName" />
        </el-form-item>
        <el-form-item label="计划年度" required>
          <el-input-number v-model="planForm.planYear" :min="2000" :max="2100" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="计划金额" required>
          <el-input-number v-model="planForm.plannedAmount" :min="0" :precision="2" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="planForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPlan">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新建使用单弹窗 -->
    <el-dialog v-model="expenseDialogVisible" title="新建党费使用单" width="640px" :close-on-click-modal="false">
      <el-form :model="expenseForm" label-width="130px">
        <el-form-item label="组织ID" required>
          <el-input-number v-model="expenseForm.orgId" :min="1" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="组织名称" required>
          <el-input v-model="expenseForm.orgName" />
        </el-form-item>
        <el-form-item label="使用事项" required>
          <el-input v-model="expenseForm.title" />
        </el-form-item>
        <el-form-item label="使用类别" required>
          <el-select v-model="expenseForm.usageType" style="width:100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" required>
          <el-input-number v-model="expenseForm.amount" :min="0" :precision="2" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="使用日期" required>
          <el-date-picker v-model="expenseForm.expenseDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="用途说明">
          <el-input v-model="expenseForm.purpose" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="经办人" required>
          <el-input v-model="expenseForm.handler" />
        </el-form-item>
        <el-form-item label="集体讨论记录" required>
          <el-input v-model="expenseForm.discussionAttachment" placeholder="必填：支委会/党员大会讨论记录附件（E-UE-02）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="expenseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitExpense">确定</el-button>
      </template>
    </el-dialog>

    <!-- 审批弹窗 -->
    <el-dialog v-model="auditDialogVisible" title="使用单审批" width="520px" :close-on-click-modal="false">
      <el-form :model="auditForm" label-width="90px">
        <el-form-item label="使用事项">
          <el-input :model-value="auditForm.title" disabled />
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="auditForm.opinion" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="submitAudit(false)">驳回</el-button>
        <el-button type="success" @click="submitAudit(true)">通过</el-button>
      </template>
    </el-dialog>

    <!-- 登记执行结果弹窗 -->
    <el-dialog v-model="registerDialogVisible" title="登记执行结果" width="520px" :close-on-click-modal="false">
      <el-form :model="registerForm" label-width="90px">
        <el-form-item label="使用事项">
          <el-input :model-value="registerForm.title" disabled />
        </el-form-item>
        <el-form-item label="执行日期" required>
          <el-date-picker v-model="registerForm.expenseDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="执行结果" required>
          <el-input v-model="registerForm.result" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRegister">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/membership/dues-expense'

const DEFAULT_CATEGORIES = [
  '培训党员',
  '订阅购买党员教育报刊资料音像制品和设备',
  '表彰先进基层党组织优秀党员优秀党务工作者',
  '补助生活困难党员',
  '补助遭受严重自然灾害党员及修缮因灾受损教育设施'
]

const activeTab = ref('plan')

// ==================== 使用类别 ====================
const categories = ref([...DEFAULT_CATEGORIES])
async function loadCategories() {
  try {
    const res = await axios.get(`${API}/categories`)
    const list = res.data?.data || res.data
    if (Array.isArray(list) && list.length) categories.value = list
  } catch (e) {
    console.error(e)
  }
}

// ==================== 1. 年度计划 ====================
const planFilter = reactive({ orgId: null, year: new Date().getFullYear() })
const plans = ref([])
const planLoading = ref(false)
const planDialogVisible = ref(false)
const planForm = reactive({ orgId: null, orgName: '', planYear: new Date().getFullYear(), plannedAmount: null, remark: '' })

async function loadPlans() {
  planLoading.value = true
  try {
    const res = await axios.get(`${API}/plan/list`, {
      params: { orgId: planFilter.orgId || undefined, year: planFilter.year || undefined }
    })
    plans.value = res.data?.data || res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    planLoading.value = false
  }
}

function openPlanDialog() {
  Object.assign(planForm, {
    orgId: planFilter.orgId,
    orgName: '',
    planYear: planFilter.year || new Date().getFullYear(),
    plannedAmount: null,
    remark: ''
  })
  planDialogVisible.value = true
}

async function submitPlan() {
  try {
    await axios.post(`${API}/plan`, {
      orgId: planForm.orgId,
      orgName: planForm.orgName,
      planYear: planForm.planYear,
      plannedAmount: planForm.plannedAmount,
      remark: planForm.remark || undefined
    })
    ElMessage.success('保存计划成功')
    planDialogVisible.value = false
    loadPlans()
  } catch (e) {
    console.error(e)
  }
}

// ==================== 2. 使用登记 ====================
const expenseFilter = reactive({ orgId: null, year: new Date().getFullYear(), auditStatus: '' })
const expenses = ref([])
const expenseLoading = ref(false)
const summary = reactive({ collected: null, remitted: null, used: null, availableBalance: null })

const expenseDialogVisible = ref(false)
const expenseForm = reactive({
  orgId: null,
  orgName: '',
  title: '',
  usageType: '',
  amount: null,
  expenseDate: '',
  purpose: '',
  handler: '',
  discussionAttachment: ''
})

const auditDialogVisible = ref(false)
const auditForm = reactive({ id: null, title: '', opinion: '' })

const registerDialogVisible = ref(false)
const registerForm = reactive({ id: null, title: '', expenseDate: '', result: '' })

async function loadExpenses() {
  expenseLoading.value = true
  try {
    const res = await axios.get(`${API}/list`, {
      params: {
        orgId: expenseFilter.orgId || undefined,
        year: expenseFilter.year || undefined,
        auditStatus: expenseFilter.auditStatus || undefined
      }
    })
    expenses.value = res.data?.data || res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    expenseLoading.value = false
  }
  loadYearSummary()
}

async function loadYearSummary() {
  try {
    const res = await axios.get(`${API}/year-summary`, {
      params: { orgId: expenseFilter.orgId || undefined, year: expenseFilter.year || undefined }
    })
    Object.assign(summary, { collected: null, remitted: null, used: null, availableBalance: null }, res.data?.data || res.data || {})
  } catch (e) {
    console.error(e)
  }
}

function openExpenseDialog() {
  Object.assign(expenseForm, {
    orgId: expenseFilter.orgId,
    orgName: '',
    title: '',
    usageType: '',
    amount: null,
    expenseDate: '',
    purpose: '',
    handler: '',
    discussionAttachment: ''
  })
  expenseDialogVisible.value = true
}

async function submitExpense() {
  if (!expenseForm.discussionAttachment) {
    ElMessage.warning('请填写集体讨论记录附件（E-UE-02：未附讨论记录不得提交）')
    return
  }
  try {
    const res = await axios.post(API, {
      orgId: expenseForm.orgId,
      orgName: expenseForm.orgName,
      title: expenseForm.title,
      usageType: expenseForm.usageType,
      amount: expenseForm.amount,
      expenseDate: expenseForm.expenseDate || undefined,
      purpose: expenseForm.purpose || undefined,
      handler: expenseForm.handler,
      discussionAttachment: expenseForm.discussionAttachment
    })
    const d = res.data?.data || res.data || {}
    ElMessage.success('新建使用单成功')
    if (Array.isArray(d.warnings) && d.warnings.length) {
      d.warnings.forEach(w => ElMessage.warning(w))
    }
    expenseDialogVisible.value = false
    loadExpenses()
  } catch (e) {
    console.error(e)
  }
}

function openAuditDialog(row) {
  Object.assign(auditForm, { id: row.id, title: row.title, opinion: '' })
  auditDialogVisible.value = true
}

async function submitAudit(pass) {
  try {
    await axios.post(`${API}/${auditForm.id}/audit`, { pass, opinion: auditForm.opinion || undefined })
    ElMessage.success(pass ? '已通过' : '已驳回')
    auditDialogVisible.value = false
    loadExpenses()
  } catch (e) {
    console.error(e)
  }
}

function openRegisterDialog(row) {
  Object.assign(registerForm, { id: row.id, title: row.title, expenseDate: row.expenseDate || '', result: '' })
  registerDialogVisible.value = true
}

async function submitRegister() {
  try {
    await axios.post(`${API}/${registerForm.id}/register`, {
      expenseDate: registerForm.expenseDate || undefined,
      result: registerForm.result
    })
    ElMessage.success('执行结果登记成功')
    registerDialogVisible.value = false
    loadExpenses()
  } catch (e) {
    console.error(e)
  }
}

function expenseStatusLabel(s) {
  const map = { DRAFT: '草稿', PENDING: '待审批', APPROVED: '已批准', REJECTED: '已驳回', REGISTERED: '已登记', VERIFIED: '已核销' }
  return map[s] || s || ''
}
function expenseStatusTagType(s) {
  const map = { DRAFT: 'info', PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', REGISTERED: 'primary', VERIFIED: 'success' }
  return map[s] || 'info'
}

// ==================== 初始化 ====================
onMounted(() => {
  loadCategories()
  loadPlans()
  loadExpenses()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.section-title {
  margin: 16px 0 10px;
  font-size: 15px;
  color: #303133;
}

.stats-cards {
  margin-bottom: 16px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}
</style>

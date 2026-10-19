<template>
  <div class="page-card">
    <div class="page-header">
      <h2>假期政策</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>新建政策
        </el-button>
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="假期政策"
      description="维护公司各类假期政策，包括年假天数规则、请假流程、适用对象、生效周期等，作为员工请假审批的依据。"
      style="margin-bottom: 16px"
    />

    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="政策名称">
          <el-input v-model="searchForm.policyName" placeholder="请输入政策名称" style="width: 200px" />
        </el-form-item>
        <el-form-item label="假期类型">
          <el-select v-model="searchForm.holidayType" placeholder="选择类型" style="width: 150px" clearable>
            <el-option v-for="item in holidayTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用对象">
          <el-select v-model="searchForm.applyTo" placeholder="选择适用对象" style="width: 160px" clearable>
            <el-option label="全体员工" value="all" />
            <el-option label="正式员工" value="regular" />
            <el-option label="试用期员工" value="probation" />
            <el-option label="管理层" value="manager" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 120px" clearable>
            <el-option label="生效中" value="active" />
            <el-option label="已停用" value="inactive" />
            <el-option label="草稿" value="draft" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="policyList" border stripe size="small">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="政策名称" min-width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.policyName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="假期类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getHolidayTypeLabel(row.holidayType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTo" label="适用对象" width="120">
          <template #default="{ row }">{{ getApplyToLabel(row.applyTo) }}</template>
        </el-table-column>
        <el-table-column prop="dayRule" label="天数规则" min-width="160" />
        <el-table-column prop="effectiveDate" label="生效日期" width="120" />
        <el-table-column prop="expireDate" label="失效日期" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" @click="editItem(row)">编辑</el-button>
            <el-button link size="small" type="danger" @click="deleteItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; text-align: right"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px">
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="政策名称" required>
              <el-input v-model="formData.leaveTypeName" placeholder="请输入政策名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="假期类型" required>
              <el-select v-model="formData.leaveTypeCode" placeholder="选择类型" style="width: 100%">
                <el-option v-for="item in holidayTypes" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="适用对象" required>
              <el-select v-model="formData.leaveCategory" placeholder="选择适用对象" style="width: 100%">
                <el-option label="全体员工" value="all" />
                <el-option label="正式员工" value="regular" />
                <el-option label="试用期员工" value="probation" />
                <el-option label="管理层" value="manager" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" required>
              <el-select v-model="formData.status" placeholder="选择状态" style="width: 100%">
                <el-option label="生效中" value="active" />
                <el-option label="已停用" value="inactive" />
                <el-option label="草稿" value="draft" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生效日期" required>
              <el-date-picker v-model="formData.effectiveStartDate" type="date" placeholder="选择生效日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="失效日期">
              <el-date-picker v-model="formData.effectiveEndDate" type="date" placeholder="选择失效日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="年度最大天数" required>
          <el-input-number v-model="formData.maxAnnualDays" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="政策说明">
          <el-input type="textarea" v-model="formData.description" :rows="4" placeholder="请描述请假流程、审批层级、所需证明材料等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveData">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import request from '@/api/axiosConfig'

const holidayTypes = [
  { value: 'annual', label: '年假' },
  { value: 'sick', label: '病假' },
  { value: 'personal', label: '事假' },
  { value: 'marriage', label: '婚假' },
  { value: 'maternity', label: '产假' },
  { value: 'compassionate', label: '丧假' }
]

const STATUS_MAP = { ACTIVE: 'active', INACTIVE: 'inactive', DRAFT: 'draft' }

const searchForm = reactive({ policyName: '', holidayType: '', applyTo: '', status: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('新建假期政策')
const formRef = ref(null)

const formData = reactive({
  id: '', leaveTypeCode: '', leaveTypeName: '', leaveCategory: '',
  effectiveStartDate: '', effectiveEndDate: '', status: 'ACTIVE',
  maxAnnualDays: 0, salaryPaidFlag: true, description: ''
})

const policyList = ref([])

function getHolidayTypeLabel(type) {
  const item = holidayTypes.find(i => i.value === type)
  return item ? item.label : type
}
function getApplyToLabel(applyTo) {
  const labels = { all: '全体员工', regular: '正式员工', probation: '试用期员工', manager: '管理层' }
  return labels[applyTo] || applyTo
}
function getStatusLabel(status) {
  const labels = { active: '生效中', inactive: '已停用', draft: '草稿' }
  return labels[status] || status
}
function getStatusType(status) {
  const types = { active: 'success', inactive: 'info', draft: 'warning' }
  return types[status] || 'info'
}

async function loadPolicies() {
  try {
    const { data } = await request.get('/api/holiday/type-rule/type/list', {
      params: { pageNum: pagination.currentPage, pageSize: pagination.pageSize }
    })
    const records = data?.records || []
    policyList.value = records.map(r => ({
      id: r.id, policyName: r.leaveTypeName, holidayType: r.leaveTypeCode,
      applyTo: r.applicableOrgLevel || 'all',
      dayRule: `每年最多${r.maxAnnualDays || 0}天，连续不超过${r.maxConsecutiveDays || 0}天`,
      effectiveDate: r.effectiveStartDate ? String(r.effectiveStartDate).slice(0, 10) : '',
      expireDate: r.effectiveEndDate ? String(r.effectiveEndDate).slice(0, 10) : '',
      status: STATUS_MAP[r.status] || 'active', description: ''
    }))
    pagination.total = data?.total || 0
  } catch (e) {
    ElMessage.error('假期政策加载失败：' + (e.message || ''))
  }
}

function doSearch() { loadPolicies() }
function resetSearch() {
  searchForm.policyName = ''; searchForm.holidayType = ''; searchForm.applyTo = ''; searchForm.status = ''
  loadPolicies()
}
function refreshData() { loadPolicies(); ElMessage.success('数据已刷新') }

function openCreate() {
  dialogTitle.value = '新建假期政策'
  Object.assign(formData, {
    id: '', leaveTypeCode: '', leaveTypeName: '', leaveCategory: '',
    effectiveStartDate: '', effectiveEndDate: '', status: 'ACTIVE',
    maxAnnualDays: 0, salaryPaidFlag: true, description: ''
  })
  dialogVisible.value = true
}

function viewDetail(row) { ElMessage.info(`查看政策：${row.policyName}`) }

function editItem(row) {
  dialogTitle.value = '编辑假期政策'
  Object.assign(formData, {
    id: row.id, leaveTypeCode: row.holidayType, leaveTypeName: row.policyName,
    leaveCategory: row.applyTo, effectiveStartDate: row.effectiveDate,
    effectiveEndDate: row.expireDate, status: 'ACTIVE', maxAnnualDays: 0, description: ''
  })
  dialogVisible.value = true
}

async function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除政策"${row.policyName}"吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      try {
        await request.delete(`/api/holiday/type-rule/type/${row.id}`)
        ElMessage.success('已删除')
        loadPolicies()
      } catch (e) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

async function saveData() {
  if (!formData.leaveTypeName || !formData.leaveTypeCode) {
    ElMessage.warning('请填写必填项'); return
  }
  try {
    if (formData.id) {
      await request.put(`/api/holiday/type-rule/type/${formData.id}`, { ...formData })
    } else {
      await request.post('/api/holiday/type-rule/type', { ...formData })
    }
    dialogVisible.value = false
    ElMessage.success('假期政策已保存')
    loadPolicies()
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => loadPolicies())
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.search-form { margin-bottom: 0; }
</style>

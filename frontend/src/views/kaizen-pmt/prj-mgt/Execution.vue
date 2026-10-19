<template>
  <div class="page-card">
    <div class="page-header">
      <h2>项目执行</h2>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="openCreate">新建任务</el-button>
        <el-button :icon="Refresh" @click="refreshData">刷新</el-button>
      </div>
    </div>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="项目执行管理"
      description="跟踪改善项目执行过程中的任务进展、问题处理与风险预警，确保项目按计划推进。"
      style="margin-bottom: 16px;"
    />

    <el-card shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="任务名称">
          <el-input v-model="searchForm.name" placeholder="请输入任务名称" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="所属项目">
          <el-input v-model="searchForm.projectName" placeholder="请输入所属项目" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 140px;">
            <el-option label="未开始" value="pending" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已延期" value="delayed" />
            <el-option label="已阻塞" value="blocked" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="doSearch">查询</el-button>
          <el-button :icon="RefreshRight" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="executionList" border stripe size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="taskNo" label="任务编号" width="140" />
        <el-table-column prop="name" label="任务名称" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="所属项目" width="180" />
        <el-table-column prop="owner" label="负责人" width="100" />
        <el-table-column prop="type" label="任务类型" width="110">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90">
          <template #default="{ row }">
            <el-tag :type="priorityTagType(row.priority)" size="small">{{ priorityLabel(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="160">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :stroke-width="12" :color="progressColor(row.progress)" />
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="截止日期" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="viewDetail(row)">查看</el-button>
            <el-button link type="primary" :icon="Edit" @click="editItem(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="deleteItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="跟踪编号">
              <el-input v-model="formData.taskNo" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属项目" required>
              <el-select v-model="formData.projectId" placeholder="选择所属项目" filterable style="width: 100%;" @change="onProjectChange">
                <el-option v-for="p in projectOptions" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="WBS任务" required>
          <el-select v-model="formData.wbsTaskId" placeholder="选择要填报进度的WBS任务" filterable style="width: 100%;" @change="onTaskChange">
            <el-option v-for="t in wbsOptions" :key="t.id" :label="`${t.taskName}（${t.wbsCode}）`" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="任务名称">
              <el-input v-model="formData.name" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-input v-model="formData.owner" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="计划进度(%)">
              <el-slider v-model="formData.planProgress" :min="0" :max="100" show-input />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际进度(%)">
              <el-slider v-model="formData.actualProgress" :min="0" :max="100" show-input />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="已用金额">
              <el-input-number v-model="formData.usedAmount" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划截止">
              <el-input v-model="formData.dueDate" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="滞后原因/说明">
          <el-input v-model="formData.description" type="textarea" :rows="4" placeholder="进度偏差原因、问题与风险" />
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
import { Plus, Refresh, Search, RefreshRight, View, Edit, Delete } from '@element-plus/icons-vue'
import axios from '@/api/axiosConfig'

// 查询表单状态
const searchForm = reactive({
  name: '',
  projectName: '',
  status: ''
})

// 分页状态
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 弹窗状态
const dialogVisible = ref(false)
const dialogTitle = ref('新建进度填报')
const formRef = ref(null)
const isEdit = ref(false)

// 项目与 WBS 选项
const projectOptions = ref([])
const wbsAll = ref([])
const wbsOptions = ref([])

// 新建/编辑用的表单数据
const formData = reactive({
  id: null,
  trackNo: '',
  wbsTaskId: null,
  projectId: null,
  projectName: '',
  name: '',
  owner: '',
  planProgress: 0,
  actualProgress: 0,
  usedAmount: 0,
  dueDate: '',
  description: '',
  version: null
})

const allList = ref([])
const executionList = ref([])

const fmtDate = (d) => (d ? String(d).substring(0, 10) : '')
const wbsOf = (track) => wbsAll.value.find(t => t.id === track.wbsTaskId)
const projectNameOf = (track) => {
  const p = projectOptions.value.find(x => x.id === track.projectId)
  return p ? p.name : (track.projectNo || '')
}
// 由后端跟踪数据 + WBS 信息拼出页面行；状态由 lagStatus/进度推导
const fromEntity = (d) => {
  const wbs = wbsOf(d) || {}
  const actual = d.actualProgress || 0
  let status = 'pending'
  if (actual >= 100) status = 'completed'
  else if (d.lagStatus === 'lagging') status = 'delayed'
  else if (actual > 0) status = 'ongoing'
  return {
    id: d.id,
    taskNo: d.trackNo,
    wbsTaskId: d.wbsTaskId,
    projectId: d.projectId,
    projectNo: d.projectNo,
    projectName: projectNameOf(d),
    name: wbs.taskName || ('任务#' + d.wbsTaskId),
    owner: wbs.ownerName || wbs.owner || '',
    type: wbs.phase || '',
    priority: 'medium',
    progress: Math.round(Number(d.progressCompletionRate != null ? d.progressCompletionRate : actual)),
    dueDate: fmtDate(wbs.planEndDate),
    status,
    lagStatus: d.lagStatus,
    planProgress: d.planProgress || 0,
    actualProgress: actual,
    usedAmount: Number(d.usedAmount || 0),
    description: d.lagReason || '',
    version: d.version
  }
}

const applyFilterAndPage = () => {
  let rows = allList.value
  if (searchForm.name) rows = rows.filter(r => (r.name || '').includes(searchForm.name))
  if (searchForm.projectName) rows = rows.filter(r => (r.projectName || '').includes(searchForm.projectName))
  if (searchForm.status) rows = rows.filter(r => r.status === searchForm.status)
  pagination.total = rows.length
  const start = (pagination.currentPage - 1) * pagination.pageSize
  executionList.value = rows.slice(start, start + pagination.pageSize)
}

const loadProjects = async () => {
  const res = await axios.get('/api/project/pm/list')
  projectOptions.value = (res.data && res.data.data ? res.data.data : []).map(p => ({ id: p.id, name: p.name, projectNo: p.projectNo }))
}

const loadWbs = async () => {
  const res = await axios.get('/api/project/wbs/list')
  wbsAll.value = (res.data && res.data.data ? res.data.data : [])
}

const loadList = async () => {
  const res = await axios.get('/api/project/progress/list')
  allList.value = (res.data && res.data.data ? res.data.data : []).map(fromEntity)
  applyFilterAndPage()
}

const refreshData = async () => {
  await loadWbs()
  await loadList()
  ElMessage.success('数据已刷新')
}

const onProjectChange = (projectId) => {
  formData.wbsTaskId = null
  wbsOptions.value = wbsAll.value.filter(t => t.projectId === projectId)
}

const onTaskChange = (taskId) => {
  const t = wbsAll.value.find(x => x.id === taskId)
  if (!t) return
  formData.name = t.taskName
  formData.owner = t.ownerName || t.owner || ''
  formData.dueDate = fmtDate(t.planEndDate)
}

// 文案/标签辅助函数
function typeLabel(type) {
  const map = { research: '需求调研', design: '方案设计', development: '开发实施', testing: '测试验证', deployment: '上线交付' }
  return map[type] || (type || '-')
}

function typeTagType(type) {
  const map = { research: 'info', design: 'primary', development: 'warning', testing: 'success', deployment: 'danger' }
  return map[type] || 'info'
}

function priorityLabel(priority) {
  const map = { high: '高', medium: '中', low: '低' }
  return map[priority] || priority
}

function priorityTagType(priority) {
  const map = { high: 'danger', medium: 'warning', low: 'info' }
  return map[priority] || 'info'
}

function statusLabel(status) {
  const map = { pending: '未开始', ongoing: '进行中', completed: '已完成', delayed: '已延期', blocked: '已阻塞' }
  return map[status] || status
}

function statusTagType(status) {
  const map = { pending: 'info', ongoing: 'primary', completed: 'success', delayed: 'warning', blocked: 'danger' }
  return map[status] || 'info'
}

function progressColor(progress) {
  if (progress >= 100) return '#52c41a'
  if (progress >= 50) return '#1890ff'
  if (progress > 0) return '#faad14'
  return '#d9d9d9'
}

// 将表单数据重置为默认值
function resetFormData() {
  Object.assign(formData, {
    id: null,
    trackNo: '',
    wbsTaskId: null,
    projectId: null,
    projectName: '',
    name: '',
    owner: '',
    planProgress: 0,
    actualProgress: 0,
    usedAmount: 0,
    dueDate: '',
    description: '',
    version: null
  })
}

// 打开新建弹窗
function openCreate() {
  isEdit.value = false
  dialogTitle.value = '新建进度填报'
  resetFormData()
  wbsOptions.value = []
  dialogVisible.value = true
}

// 查看详情
function viewDetail(row) {
  ElMessage.info(`查看任务详情：${row.name}`)
}

// 编辑已有条目
function editItem(row) {
  isEdit.value = true
  dialogTitle.value = '编辑进度填报'
  resetFormData()
  wbsOptions.value = wbsAll.value.filter(t => t.projectId === row.projectId)
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 删除条目并二次确认
function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除任务「${row.name}」的进度填报吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await axios.delete(`/api/project/progress/${row.id}`)
        ElMessage.success('删除成功')
        await loadList()
      } catch (e) {
        ElMessage.error('删除失败：' + ((e.response && e.response.data && (e.response.data.message || e.response.data.msg)) || e.message))
      }
    })
    .catch(() => {})
}

// 查询处理
function doSearch() {
  pagination.currentPage = 1
  applyFilterAndPage()
}

// 重置查询表单
function resetSearch() {
  searchForm.name = ''
  searchForm.projectName = ''
  searchForm.status = ''
  pagination.currentPage = 1
  applyFilterAndPage()
}

// 保存新建/编辑的表单数据
async function saveData() {
  if (!formData.projectId || !formData.wbsTaskId) {
    ElMessage.warning('请选择所属项目与WBS任务')
    return
  }
  const proj = projectOptions.value.find(p => p.id === formData.projectId)
  const payload = {
    projectId: formData.projectId,
    projectNo: proj ? proj.projectNo : null,
    wbsTaskId: formData.wbsTaskId,
    planProgress: formData.planProgress,
    actualProgress: formData.actualProgress,
    usedAmount: formData.usedAmount,
    lagReason: formData.description,
    version: formData.version
  }
  try {
    if (isEdit.value) {
      await axios.put(`/api/project/progress/${formData.id}`, payload)
      ElMessage.success('修改成功')
    } else {
      await axios.post('/api/project/progress', payload)
      ElMessage.success('新建成功')
    }
    dialogVisible.value = false
    await loadList()
  } catch (e) {
    ElMessage.error('保存失败：' + ((e.response && e.response.data && (e.response.data.message || e.response.data.msg)) || e.message))
  }
}

onMounted(async () => {
  await loadProjects()
  await loadWbs()
  await loadList()
})
</script>

<style scoped>
.page-card {
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
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.search-form {
  margin-bottom: 16px;
}
</style>

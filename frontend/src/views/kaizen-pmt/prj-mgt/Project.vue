<template>
  <div class="page-card">
    <div class="page-header">
      <h2>项目管理</h2>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="openCreate">新建项目</el-button>
        <el-button :icon="Refresh" @click="refreshData">刷新</el-button>
      </div>
    </div>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="改善项目管理"
      description="面向改善项目开展立项登记、基本信息维护与项目经理指派，统一管控项目状态与优先级。"
      style="margin-bottom: 16px;"
    />

    <el-card shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="项目名称">
          <el-input v-model="searchForm.name" placeholder="请输入项目名称" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="项目类型">
          <el-select v-model="searchForm.type" placeholder="选择类型" clearable style="width: 150px;">
            <el-option label="流程改善" value="process" />
            <el-option label="设备改造" value="equipment" />
            <el-option label="质量提升" value="quality" />
            <el-option label="成本节约" value="cost" />
            <el-option label="安全改善" value="safety" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 140px;">
            <el-option label="规划中" value="planning" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已暂停" value="paused" />
            <el-option label="已结项" value="completed" />
            <el-option label="已归档" value="archived" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="doSearch">查询</el-button>
          <el-button :icon="RefreshRight" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="projectList" border stripe size="small" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="projectNo" label="项目编号" width="140" />
        <el-table-column prop="name" label="项目名称" min-width="220">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="项目类型" width="110">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="managerName" label="项目经理" width="110">
          <template #default="{ row }">{{ row.managerName || row.manager || '-' }}</template>
        </el-table-column>
        <el-table-column prop="deptCode" label="所属部门" width="120">
          <template #default="{ row }">{{ row.deptCode || '-' }}</template>
        </el-table-column>
        <el-table-column prop="totalBudget" label="预算(元)" width="130" align="right">
          <template #default="{ row }">{{ row.totalBudget != null ? '¥' + Number(row.totalBudget).toLocaleString() : '-' }}</template>
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
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="planStartDate" label="开始日期" width="120" />
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
            <el-form-item label="项目编号" required>
              <el-input v-model="formData.projectNo" placeholder="请输入项目编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目类型" required>
              <el-select v-model="formData.type" placeholder="选择项目类型" style="width: 100%;">
                <el-option label="流程改善" value="process" />
                <el-option label="设备改造" value="equipment" />
                <el-option label="质量提升" value="quality" />
                <el-option label="成本节约" value="cost" />
                <el-option label="安全改善" value="safety" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="项目名称" required>
          <el-input v-model="formData.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目经理" required>
              <el-input v-model="formData.managerName" placeholder="请输入项目经理姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门">
              <el-input v-model="formData.deptCode" placeholder="请输入所属部门编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="formData.priority" placeholder="选择优先级" style="width: 100%;">
                <el-option label="高" value="high" />
                <el-option label="中" value="medium" />
                <el-option label="低" value="low" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预算(元)">
              <el-input-number v-model="formData.totalBudget" :min="0" :step="10000" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker v-model="formData.planStartDate" type="date" placeholder="选择开始日期" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker v-model="formData.planEndDate" type="date" placeholder="选择结束日期" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="formData.status" placeholder="选择状态" style="width: 100%;">
                <el-option label="规划中" value="planning" />
                <el-option label="进行中" value="ongoing" />
                <el-option label="已暂停" value="paused" />
                <el-option label="已结项" value="completed" />
                <el-option label="已归档" value="archived" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目经理工号">
              <el-input v-model="formData.manager" placeholder="可填工号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="项目目标">
          <el-input v-model="formData.projectGoal" type="textarea" :rows="3" placeholder="请输入项目目标与范围说明" />
        </el-form-item>
        <el-form-item label="交付物">
          <el-input v-model="formData.deliverable" type="textarea" :rows="2" placeholder="请输入交付物" />
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

const loading = ref(false)

// 查询表单状态
const searchForm = reactive({
  name: '',
  type: '',
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
const dialogTitle = ref('新建改善项目')
const formRef = ref(null)
const isEdit = ref(false)

// 新建/编辑用的表单数据（对齐 PmProject 实体字段）
const formData = reactive({
  id: null,
  projectNo: '',
  name: '',
  type: 'process',
  deptCode: '',
  manager: '',
  managerName: '',
  priority: 'medium',
  status: 'planning',
  planStartDate: '',
  planEndDate: '',
  totalBudget: 0,
  projectGoal: '',
  deliverable: ''
})

// 后端全量列表（/api/project/pm/list 返回 List<PmProject>），前端做筛选与分页
const allList = ref([])
const projectList = ref([])

const fmtDate = (d) => (d ? String(d).substring(0, 10) : '')

// 根据状态推导展示进度（PmProject 无 progress 字段，按状态聚合）
const progressFromStatus = (status) => {
  const map = { planning: 10, ongoing: 50, paused: 30, completed: 100, archived: 100 }
  return map[status] != null ? map[status] : 0
}

const fromEntity = (d) => ({
  id: d.id,
  projectNo: d.projectNo,
  name: d.name,
  type: d.type,
  deptCode: d.deptCode,
  manager: d.manager,
  managerName: d.managerName,
  priority: d.priority,
  status: d.status,
  planStartDate: fmtDate(d.planStartDate),
  planEndDate: fmtDate(d.planEndDate),
  totalBudget: d.totalBudget,
  projectGoal: d.projectGoal,
  deliverable: d.deliverable,
  progress: progressFromStatus(d.status)
})

const applyFilterAndPage = () => {
  let rows = allList.value
  if (searchForm.name) rows = rows.filter(r => (r.name || '').includes(searchForm.name))
  if (searchForm.type) rows = rows.filter(r => r.type === searchForm.type)
  if (searchForm.status) rows = rows.filter(r => r.status === searchForm.status)
  pagination.total = rows.length
  const start = (pagination.currentPage - 1) * pagination.pageSize
  projectList.value = rows.slice(start, start + pagination.pageSize)
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/project/pm/list')
    allList.value = (res.data && res.data.data ? res.data.data : []).map(fromEntity)
    applyFilterAndPage()
  } catch (e) {
    ElMessage.error('加载项目列表失败：' + ((e.response && e.response.data && (e.response.data.message || e.response.data.msg)) || e.message))
    console.error(e)
  } finally {
    loading.value = false
  }
}

const refreshData = async () => {
  await loadList()
  ElMessage.success('数据已刷新')
}

// 文案/标签辅助函数
function typeLabel(type) {
  const map = { process: '流程改善', equipment: '设备改造', quality: '质量提升', cost: '成本节约', safety: '安全改善' }
  return map[type] || type
}

function typeTagType(type) {
  const map = { process: 'primary', equipment: 'warning', quality: 'success', cost: 'info', safety: 'danger' }
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
  const map = { planning: '规划中', ongoing: '进行中', paused: '已暂停', completed: '已结项', archived: '已归档' }
  return map[status] || status
}

function statusTagType(status) {
  const map = { planning: 'info', ongoing: 'warning', paused: 'danger', completed: 'success', archived: 'info' }
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
    projectNo: '',
    name: '',
    type: 'process',
    deptCode: '',
    manager: '',
    managerName: '',
    priority: 'medium',
    status: 'planning',
    planStartDate: '',
    planEndDate: '',
    totalBudget: 0,
    projectGoal: '',
    deliverable: ''
  })
}

// 打开新建弹窗
function openCreate() {
  isEdit.value = false
  dialogTitle.value = '新建改善项目'
  resetFormData()
  dialogVisible.value = true
}

// 查看详情
function viewDetail(row) {
  ElMessage.info(`查看项目详情：${row.name}`)
}

// 编辑已有条目
function editItem(row) {
  isEdit.value = true
  dialogTitle.value = '编辑改善项目'
  resetFormData()
  Object.assign(formData, {
    id: row.id,
    projectNo: row.projectNo || '',
    name: row.name || '',
    type: row.type || 'process',
    deptCode: row.deptCode || '',
    manager: row.manager || '',
    managerName: row.managerName || '',
    priority: row.priority || 'medium',
    status: row.status || 'planning',
    planStartDate: row.planStartDate || '',
    planEndDate: row.planEndDate || '',
    totalBudget: row.totalBudget != null ? Number(row.totalBudget) : 0,
    projectGoal: row.projectGoal || '',
    deliverable: row.deliverable || ''
  })
  dialogVisible.value = true
}

// 删除条目并二次确认
function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除项目「${row.name}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await axios.delete(`/api/project/pm/${row.id}`)
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
  searchForm.type = ''
  searchForm.status = ''
  pagination.currentPage = 1
  applyFilterAndPage()
}

// 保存新建/编辑的表单数据
async function saveData() {
  if (!formData.projectNo || !formData.name || !formData.managerName) {
    ElMessage.warning('请填写必填项：项目编号、项目名称、项目经理')
    return
  }
  const payload = {
    projectNo: formData.projectNo,
    name: formData.name,
    type: formData.type,
    deptCode: formData.deptCode,
    manager: formData.manager,
    managerName: formData.managerName,
    priority: formData.priority,
    status: formData.status,
    planStartDate: formData.planStartDate || null,
    planEndDate: formData.planEndDate || null,
    totalBudget: formData.totalBudget,
    projectGoal: formData.projectGoal,
    deliverable: formData.deliverable
  }
  try {
    if (isEdit.value) {
      await axios.put(`/api/project/pm/${formData.id}`, payload)
      ElMessage.success('修改成功')
    } else {
      await axios.post('/api/project/pm', payload)
      ElMessage.success('新建成功')
    }
    dialogVisible.value = false
    await loadList()
  } catch (e) {
    ElMessage.error('保存失败：' + ((e.response && e.response.data && (e.response.data.message || e.response.data.msg)) || e.message))
  }
}

onMounted(() => {
  loadList()
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

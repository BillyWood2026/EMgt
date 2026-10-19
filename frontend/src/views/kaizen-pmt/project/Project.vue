<template>
  <div class="page-container">
    <div class="page-header">
      <h2>项目管理</h2>
      <el-button type="primary" @click="handleAdd">新增项目</el-button>
    </div>

    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon blue"><el-icon><Document /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ totalCount }}</div>
            <div class="stat-label">项目总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon green"><el-icon><CheckCircle /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ completedCount }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon orange"><el-icon><Clock /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ ongoingCount }}</div>
            <div class="stat-label">进行中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon red"><el-icon><AlertFilled /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ delayedCount }}</div>
            <div class="stat-label">延期风险</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="项目名称">
        <el-input v-model="searchForm.name" placeholder="请输入项目名称" style="width:180px" />
      </el-form-item>
      <el-form-item label="项目类型">
        <el-select v-model="searchForm.type" placeholder="选择类型" style="width:150px">
          <el-option label="改善项目" value="improvement" />
          <el-option label="新建项目" value="new" />
          <el-option label="技改项目" value="tech" />
          <el-option label="研发项目" value="rd" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width:120px">
          <el-option label="准备中" value="planning" />
          <el-option label="进行中" value="ongoing" />
          <el-option label="已完成" value="completed" />
          <el-option label="已暂停" value="paused" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="projectList" border stripe size="small">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="projectNo" label="项目编号" width="140" />
      <el-table-column prop="name" label="项目名称" min-width="200">
        <template #default="{row}">
          <el-link type="primary" @click="viewDetail(row)">{{ row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="项目类型" width="120">
        <template #default="{row}">
          <el-tag size="small">{{ getTypeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="manager" label="项目经理" width="100" />
      <el-table-column prop="budget" label="预算" width="120">
        <template #default="{row}">¥{{ row.budget.toLocaleString() }}</template>
      </el-table-column>
      <el-table-column prop="progress" label="进度" width="140">
        <template #default="{row}">
          <el-progress :percentage="row.progress" :stroke-width="12" :color="getProgressColor(row.progress)" />
        </template>
      </el-table-column>
      <el-table-column prop="startDate" label="开始日期" width="120" />
      <el-table-column prop="endDate" label="结束日期" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 'planning'" size="small" type="warning" @click="handleStart(row)">启动</el-button>
          <el-button v-if="row.status === 'ongoing'" size="small" type="success" @click="handleComplete(row)">完成</el-button>
          <el-button v-if="row.status === 'ongoing'" size="small" type="danger" @click="handlePause(row)">暂停</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="pagination.currentPage"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pagination.pageSize"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top: 20px; text-align: right;"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-form-item label="项目编号" required>
          <el-input v-model="formData.projectNo" placeholder="请输入项目编号" />
        </el-form-item>
        <el-form-item label="项目名称" required>
          <el-input v-model="formData.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目类型" required>
          <el-select v-model="formData.type" placeholder="选择项目类型">
            <el-option label="改善项目" value="improvement" />
            <el-option label="新建项目" value="new" />
            <el-option label="技改项目" value="tech" />
            <el-option label="研发项目" value="rd" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目经理">
          <el-input v-model="formData.manager" placeholder="请输入项目经理" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入项目描述" />
        </el-form-item>
        <el-form-item label="预算">
          <el-input-number v-model="formData.budget" :min="0" prefix="¥" :precision="0" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="formData.startDate" type="date" placeholder="选择开始日期" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="formData.endDate" type="date" placeholder="选择结束日期" />
        </el-form-item>
        <el-form-item label="进度">
          <el-slider v-model="formData.progress" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status">
            <el-option label="准备中" value="planning" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已暂停" value="paused" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="项目详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="项目编号">{{ detailData.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="项目名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="项目类型">{{ getTypeLabel(detailData.type) }}</el-descriptions-item>
        <el-descriptions-item label="项目经理">{{ detailData.manager }}</el-descriptions-item>
        <el-descriptions-item label="项目描述" :span="2">{{ detailData.description }}</el-descriptions-item>
        <el-descriptions-item label="预算">¥{{ detailData.budget.toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="实际支出">¥{{ (detailData.budget * detailData.progress / 100 * 0.9).toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ detailData.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ detailData.endDate }}</el-descriptions-item>
        <el-descriptions-item label="进度" :span="2">
          <el-progress :percentage="detailData.progress" :stroke-width="16" :color="getProgressColor(detailData.progress)" />
          <span style="margin-left: 10px; font-weight: bold;">{{ detailData.progress }}%</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)" size="small">{{ getStatusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div style="margin-top: 20px;">
        <h4>项目成员</h4>
        <el-table :data="detailData.members" border size="small" v-if="detailData.members && detailData.members.length">
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column prop="role" label="角色" width="100" />
          <el-table-column prop="department" label="部门" width="120" />
          <el-table-column prop="responsibility" label="职责" min-width="200" />
        </el-table>
        <p v-else class="text-gray">暂无项目成员</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from '@/api/axiosConfig'

const searchForm = reactive({
  name: '',
  type: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增项目')
const formRef = ref(null)

const formData = reactive({
  id: null,
  projectNo: '',
  name: '',
  type: 'improvement',
  manager: '',
  description: '',
  budget: 0,
  startDate: '',
  endDate: '',
  progress: 0,
  status: 'planning',
  version: null,
  createTime: '',
  members: []
})

const detailData = reactive({
  members: []
})

const allList = ref([])
const projectList = ref([])

const fmtDate = (d) => {
  if (!d) return ''
  if (d instanceof Date) {
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const dd = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${dd}`
  }
  return String(d).substring(0, 10)
}
const toPayload = (f) => ({
  id: f.id,
  name: f.name,
  type: f.type,
  manager: f.manager,
  description: f.description,
  totalBudget: f.budget,
  planStartDate: f.startDate ? fmtDate(f.startDate) : null,
  planEndDate: f.endDate ? fmtDate(f.endDate) : null,
  version: f.version
})
const fromEntity = (d) => ({
  id: d.id,
  projectNo: d.projectNo,
  name: d.name,
  type: d.type,
  manager: d.manager || '',
  description: d.description || '',
  budget: Number(d.totalBudget || 0),
  startDate: fmtDate(d.planStartDate),
  endDate: fmtDate(d.planEndDate),
  progress: d.status === 'completed' ? 100 : (d.status === 'planning' ? 0 : Math.round(Number(d.budgetUsageRate || 0))),
  status: d.status,
  version: d.version,
  usedBudget: Number(d.usedBudget || 0),
  createTime: d.createdTime ? String(d.createdTime).replace('T', ' ').substring(0, 19) : '',
  members: []
})

const totalCount = computed(() => allList.value.length)
const completedCount = computed(() => allList.value.filter(p => p.status === 'completed').length)
const ongoingCount = computed(() => allList.value.filter(p => p.status === 'ongoing').length)
const delayedCount = computed(() => allList.value.filter(p => p.status === 'ongoing' && p.progress < 50).length)

const applyFilterAndPage = () => {
  let rows = allList.value
  if (searchForm.name) rows = rows.filter(p => (p.name || '').includes(searchForm.name))
  if (searchForm.type) rows = rows.filter(p => p.type === searchForm.type)
  if (searchForm.status) rows = rows.filter(p => p.status === searchForm.status)
  pagination.total = rows.length
  const start = (pagination.currentPage - 1) * pagination.pageSize
  projectList.value = rows.slice(start, start + pagination.pageSize)
}

const loadList = async () => {
  try {
    const res = await axios.get('/api/project/pm/list')
    const data = res.data && res.data.data ? res.data.data : []
    allList.value = (data || []).map(fromEntity)
    applyFilterAndPage()
  } catch (e) {
    ElMessage.error('项目列表加载失败：' + ((e.response && e.response.data && (e.response.data.message || e.response.data.msg)) || e.message))
  }
}

function getTypeLabel(type) {
  const map = { improvement: '改善项目', new: '新建项目', tech: '技改项目', rd: '研发项目' }
  return map[type] || type
}

function getStatusLabel(status) {
  const map = { planning: '准备中', ongoing: '进行中', completed: '已完成', paused: '已暂停' }
  return map[status] || status
}

function getStatusType(status) {
  const map = { planning: 'info', ongoing: 'warning', completed: 'success', paused: 'danger' }
  return map[status] || 'info'
}

function getProgressColor(progress) {
  if (progress >= 100) return '#52c41a'
  if (progress >= 50) return '#1890ff'
  if (progress > 0) return '#faad14'
  return '#d9d9d9'
}

function handleSearch() {
  pagination.currentPage = 1
  applyFilterAndPage()
}

function resetSearch() {
  searchForm.name = ''
  searchForm.type = ''
  searchForm.status = ''
  pagination.currentPage = 1
  applyFilterAndPage()
}

function handleSizeChange(val) {
  pagination.pageSize = val
  applyFilterAndPage()
}

function handleCurrentChange(val) {
  pagination.currentPage = val
  applyFilterAndPage()
}

function handleAdd() {
  dialogTitle.value = '新增项目'
  Object.assign(formData, {
    id: null,
    projectNo: '',
    name: '',
    type: 'improvement',
    manager: '',
    description: '',
    budget: 0,
    startDate: '',
    endDate: '',
    progress: 0,
    status: 'planning',
    version: null,
    createTime: '',
    members: []
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑项目'
  Object.assign(formData, row)
  dialogVisible.value = true
}

function viewDetail(row) {
  Object.assign(detailData, row)
  detailVisible.value = true
}

const transit = async (row, action, label) => {
  try {
    await axios.post(`/api/project/pm/${row.id}/${action}`)
    ElMessage.success(label)
    await loadList()
  } catch (e) {
    ElMessage.error(label + '失败：' + ((e.response && e.response.data && (e.response.data.message || e.response.data.msg)) || e.message))
  }
}

function handleStart(row) {
  transit(row, 'start', '项目已启动')
}

function handleComplete(row) {
  transit(row, 'complete', '项目已完成')
}

function handlePause(row) {
  transit(row, 'suspend', '项目已暂停')
}

async function handleSubmit() {
  try {
    const payload = toPayload(formData)
    if (formData.id) {
      await axios.put(`/api/project/pm/${formData.id}`, payload)
    } else {
      await axios.post('/api/project/pm', payload)
    }
    ElMessage.success(dialogTitle.value === '新增项目' ? '新增成功' : '修改成功')
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
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}

.stat-icon.blue { background-color: #e6f7ff; color: #1890ff; }
.stat-icon.green { background-color: #f6ffed; color: #52c41a; }
.stat-icon.orange { background-color: #fff7e6; color: #fa8c16; }
.stat-icon.red { background-color: #fff1f0; color: #f5222d; }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.search-form {
  margin-bottom: 20px;
}

.text-gray {
  color: #999;
  text-align: center;
  padding: 20px;
}
</style>
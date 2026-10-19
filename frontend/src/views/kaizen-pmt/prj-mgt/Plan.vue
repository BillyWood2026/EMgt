<template>
  <div class="page-card">
    <div class="page-header">
      <h2>项目计划</h2>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="openCreate">新建计划</el-button>
        <el-button :icon="Refresh" @click="refreshData">刷新</el-button>
      </div>
    </div>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="项目计划管理"
      description="面向改善项目开展计划编制、里程碑设定与资源配置，保障项目按节奏推进与交付。"
      style="margin-bottom: 16px;"
    />

    <el-card shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="计划名称">
          <el-input v-model="searchForm.name" placeholder="请输入计划名称" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="所属项目">
          <el-input v-model="searchForm.projectName" placeholder="请输入所属项目" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 140px;">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="执行中" value="ongoing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已变更" value="changed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="doSearch">查询</el-button>
          <el-button :icon="RefreshRight" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="planList" border stripe size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="planNo" label="计划编号" width="130" />
        <el-table-column prop="name" label="计划名称" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="所属项目" width="180" />
        <el-table-column prop="type" label="计划类型" width="110">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="owner" label="负责人" width="100" />
        <el-table-column prop="milestone" label="里程碑" width="120">
          <template #default="{ row }">
            <el-tag :type="row.milestone ? 'warning' : 'info'" size="small">{{ row.milestone ? '关键里程碑' : '常规' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="160">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :stroke-width="12" :color="progressColor(row.progress)" />
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
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
            <el-form-item label="计划编号">
              <el-input v-model="formData.planNo" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属项目" required>
              <el-select v-model="formData.projectId" placeholder="选择所属项目" filterable style="width: 100%;">
                <el-option v-for="p in projectOptions" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="计划名称" required>
          <el-input v-model="formData.name" placeholder="请输入计划名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="计划类型" required>
              <el-select v-model="formData.type" placeholder="选择计划类型" style="width: 100%;">
                <el-option label="总体计划" value="overall" />
                <el-option label="阶段计划" value="stage" />
                <el-option label="月度计划" value="monthly" />
                <el-option label="资源计划" value="resource" />
                <el-option label="风险计划" value="risk" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" required>
              <el-input v-model="formData.owner" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker v-model="formData.startDate" type="date" placeholder="选择开始日期" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker v-model="formData.endDate" type="date" placeholder="选择结束日期" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="进度(%)">
              <el-slider v-model="formData.progress" :min="0" :max="100" show-input />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关键里程碑">
              <el-switch v-model="formData.milestone" active-text="是" inactive-text="否" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="选择状态" style="width: 200px;">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="执行中" value="ongoing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已变更" value="changed" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划说明">
          <el-input v-model="formData.description" type="textarea" :rows="4" placeholder="请输入计划内容、资源与交付物说明" />
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
const dialogTitle = ref('新建项目计划')
const formRef = ref(null)
const isEdit = ref(false)

// 项目选项（WBS 任务必须挂到具体项目）
const projectOptions = ref([])
const projectNameOf = (row) => {
  const p = projectOptions.value.find(x => x.id === row.projectId)
  return p ? p.name : (row.projectNo || '')
}

// 新建/编辑用的表单数据
const formData = reactive({
  id: null,
  planNo: '',
  wbsCode: '',
  name: '',
  projectId: null,
  projectName: '',
  type: 'overall',
  owner: '',
  milestone: false,
  progress: 0,
  startDate: '',
  endDate: '',
  status: 'draft',
  description: '',
  version: null
})

// 后端 WBS 任务列表（计划视图复用 WBS 落点；planNo 列对应 taskNo 展示）
const allList = ref([])
const planList = ref([])

const fmtDate = (d) => (d ? String(d).substring(0, 10) : '')
const fromEntity = (d) => ({
  id: d.id,
  planNo: d.taskNo,
  wbsCode: d.wbsCode,
  name: d.taskName,
  projectId: d.projectId,
  projectNo: d.projectNo,
  projectName: projectNameOf(d),
  type: d.phase || 'overall',
  owner: d.ownerName || d.owner || '',
  milestone: !!d.isMilestone,
  progress: d.progress || 0,
  startDate: fmtDate(d.planStartDate),
  endDate: fmtDate(d.planEndDate),
  status: d.status || 'draft',
  description: d.remark || d.deliverableStd || '',
  version: d.version
})

const applyFilterAndPage = () => {
  let rows = allList.value
  if (searchForm.name) rows = rows.filter(r => (r.name || '').includes(searchForm.name))
  if (searchForm.projectName) rows = rows.filter(r => (r.projectName || '').includes(searchForm.projectName))
  if (searchForm.status) rows = rows.filter(r => r.status === searchForm.status)
  pagination.total = rows.length
  const start = (pagination.currentPage - 1) * pagination.pageSize
  planList.value = rows.slice(start, start + pagination.pageSize)
}

const loadProjects = async () => {
  const res = await axios.get('/api/project/pm/list')
  projectOptions.value = (res.data && res.data.data ? res.data.data : []).map(p => ({ id: p.id, name: p.name, projectNo: p.projectNo }))
}

const loadList = async () => {
  const res = await axios.get('/api/project/wbs/list')
  allList.value = (res.data && res.data.data ? res.data.data : []).map(fromEntity)
  applyFilterAndPage()
}

const refreshData = async () => {
  await loadList()
  ElMessage.success('数据已刷新')
}

// 文案/标签辅助函数
function typeLabel(type) {
  const map = { overall: '总体计划', stage: '阶段计划', monthly: '月度计划', resource: '资源计划', risk: '风险计划' }
  return map[type] || type
}

function typeTagType(type) {
  const map = { overall: 'primary', stage: 'success', monthly: 'info', resource: 'warning', risk: 'danger' }
  return map[type] || 'info'
}

function statusLabel(status) {
  const map = { draft: '草稿', published: '已发布', ongoing: '执行中', completed: '已完成', changed: '已变更' }
  return map[status] || status
}

function statusTagType(status) {
  const map = { draft: 'info', published: 'primary', ongoing: 'warning', completed: 'success', changed: 'danger' }
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
    planNo: '',
    wbsCode: '',
    name: '',
    projectId: null,
    projectName: '',
    type: 'overall',
    owner: '',
    milestone: false,
    progress: 0,
    startDate: '',
    endDate: '',
    status: 'draft',
    description: '',
    version: null
  })
}

// 打开新建弹窗
function openCreate() {
  isEdit.value = false
  dialogTitle.value = '新建项目计划'
  resetFormData()
  dialogVisible.value = true
}

// 查看详情
function viewDetail(row) {
  ElMessage.info(`查看计划详情：${row.name}`)
}

// 编辑已有条目
function editItem(row) {
  isEdit.value = true
  dialogTitle.value = '编辑项目计划'
  resetFormData()
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 删除条目并二次确认
function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除计划「${row.name}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await axios.delete(`/api/project/wbs/${row.id}`)
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
  if (!formData.name || !formData.owner || !formData.projectId) {
    ElMessage.warning('请填写必填项：计划名称、所属项目、负责人')
    return
  }
  const proj = projectOptions.value.find(p => p.id === formData.projectId)
  const payload = {
    wbsCode: formData.wbsCode || ('WBS' + Date.now()),
    projectId: formData.projectId,
    projectNo: proj ? proj.projectNo : null,
    taskName: formData.name,
    phase: formData.type,
    ownerName: formData.owner,
    isMilestone: formData.milestone,
    progress: formData.progress,
    planStartDate: formData.startDate || null,
    planEndDate: formData.endDate || null,
    status: formData.status,
    remark: formData.description,
    weight: 0,
    version: formData.version
  }
  try {
    if (isEdit.value) {
      await axios.put(`/api/project/wbs/${formData.id}`, payload)
      ElMessage.success('修改成功')
    } else {
      await axios.post('/api/project/wbs', payload)
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

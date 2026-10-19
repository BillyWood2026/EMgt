<template>
  <div class="page-card">
    <div class="page-header">
      <h2>改善实施</h2>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="openCreate">新建实施</el-button>
        <el-button :icon="Refresh" @click="refreshData">刷新</el-button>
      </div>
    </div>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="改善实施管理"
      description="对已采纳的改善提案进行实施计划编制、实施过程跟踪与阶段成果管理，确保改善方案落地见效。"
      style="margin-bottom: 16px;"
    />

    <el-card shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="实施名称">
          <el-input v-model="searchForm.name" placeholder="请输入实施名称" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="searchForm.owner" placeholder="请输入负责人" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 140px;">
            <el-option label="待启动" value="pending" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已暂停" value="paused" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="doSearch">查询</el-button>
          <el-button :icon="RefreshRight" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="implementationList" border stripe size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="implNo" label="实施编号" width="130" />
        <el-table-column prop="name" label="实施名称" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="proposalNo" label="关联提案" width="130" />
        <el-table-column prop="owner" label="负责人" width="100" />
        <el-table-column prop="department" label="所属部门" width="120" />
        <el-table-column prop="priority" label="优先级" width="90">
          <template #default="{ row }">
            <el-tag :type="priorityTagType(row.priority)" size="small">{{ priorityLabel(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="实施进度" width="160">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :stroke-width="12" :color="progressColor(row.progress)" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="endDate" label="计划完成日期" width="120" />
        <el-table-column label="操作" width="330" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="viewDetail(row)">查看</el-button>
            <el-button v-if="row.status === 'pending'" link type="success" @click="doAction(row, 'start')">启动</el-button>
            <el-button v-if="row.status === 'ongoing'" link type="warning" @click="doAction(row, 'pause')">暂停</el-button>
            <el-button v-if="row.status === 'paused'" link type="success" @click="doAction(row, 'resume')">继续</el-button>
            <el-button v-if="row.status === 'ongoing'" link type="success" @click="doAction(row, 'complete')">完成</el-button>
            <el-button v-if="row.status !== 'completed'" link type="primary" :icon="Edit" @click="editItem(row)">编辑</el-button>
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
      <el-form ref="formRef" :model="formData" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="实施编号">
              <el-input v-model="formData.implNo" placeholder="保存后自动生成（IMP 前缀）" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联提案" required>
              <el-select v-model="formData.proposalId" placeholder="选择已立项的提案" filterable style="width: 100%;" @change="onProposalChange">
                <el-option v-for="p in selectableProposals" :key="p.id" :label="`${p.proposalNo} ${p.name}`" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="实施名称" required>
          <el-input v-model="formData.name" placeholder="请输入实施名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="负责人" required>
              <el-input v-model="formData.owner" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门">
              <el-input v-model="formData.department" placeholder="请输入所属部门" />
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
            <el-form-item label="计划完成日期">
              <el-date-picker v-model="formData.endDate" type="date" placeholder="选择完成日期" value-format="YYYY-MM-DD" style="width: 100%;" />
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
          <el-col :span="12" v-if="isEdit">
            <el-form-item label="实施进度">
              <el-slider v-model="formData.progress" :min="0" :max="100" :step="5" show-input style="padding-right: 12px;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="实施说明">
          <el-input v-model="formData.description" type="textarea" :rows="4" placeholder="请输入实施计划与说明" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search, RefreshRight, View, Edit, Delete } from '@element-plus/icons-vue'
import axios from '@/api/axiosConfig'

// 查询表单状态
const searchForm = reactive({
  name: '',
  owner: '',
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
const dialogTitle = ref('新建改善实施')
const formRef = ref(null)
const isEdit = ref(false)

// 全部提案（用于编号映射与下拉）
const allProposals = ref([])
// 仅已立项(approved)/实施中(implementing)的提案可登记实施
const selectableProposals = computed(() =>
  allProposals.value.filter(p => p.status === 'approved' || p.status === 'implementing')
)

// 新建/编辑用的表单数据
const formData = reactive({
  id: null,
  implNo: '',
  proposalId: null,
  name: '',
  owner: '',
  department: '',
  startDate: '',
  endDate: '',
  priority: 'medium',
  progress: 0,
  description: ''
})

const allList = ref([])
const implementationList = ref([])

const fmtDate = (v) => {
  if (!v) return ''
  if (Array.isArray(v)) {
    const [y, mo, d] = v
    const p = (n) => String(n).padStart(2, '0')
    return `${y}-${p(mo)}-${p(d)}`
  }
  return String(v).substring(0, 10)
}

const fromEntity = (d) => ({
  id: d.id,
  implNo: d.implNo,
  proposalId: d.proposalId,
  proposalNo: (allProposals.value.find(p => p.id === d.proposalId) || {}).proposalNo || '',
  name: d.name,
  owner: d.owner || '',
  department: d.department || '',
  startDate: fmtDate(d.startDate),
  endDate: fmtDate(d.endDate),
  priority: d.priority || 'medium',
  progress: d.progress == null ? 0 : d.progress,
  status: d.status,
  description: d.description || ''
})

const applyFilterAndPage = () => {
  let rows = allList.value
  if (searchForm.name) rows = rows.filter(r => (r.name || '').includes(searchForm.name))
  if (searchForm.owner) rows = rows.filter(r => (r.owner || '').includes(searchForm.owner))
  if (searchForm.status) rows = rows.filter(r => r.status === searchForm.status)
  pagination.total = rows.length
  const start = (pagination.currentPage - 1) * pagination.pageSize
  implementationList.value = rows.slice(start, start + pagination.pageSize)
}

const loadProposals = async () => {
  const res = await axios.get('/api/kaizen/proposal/list')
  allProposals.value = res.data && res.data.data ? res.data.data : []
}

const loadList = async () => {
  const res = await axios.get('/api/kaizen/implementation/list')
  allList.value = (res.data && res.data.data ? res.data.data : []).map(fromEntity)
  applyFilterAndPage()
}

const errMsg = (e) => (e.response && e.response.data && (e.response.data.message || e.response.data.msg)) || e.message

// 选择关联提案后继承名称/负责人/部门（名称可再修改）
const onProposalChange = (proposalId) => {
  const p = allProposals.value.find(x => x.id === proposalId)
  if (!p) return
  if (!formData.name) formData.name = p.name
  formData.owner = p.proposer || ''
  formData.department = p.department || ''
}

// 文案/标签辅助函数
function priorityLabel(priority) {
  const map = { high: '高', medium: '中', low: '低' }
  return map[priority] || priority
}

function priorityTagType(priority) {
  const map = { high: 'danger', medium: 'warning', low: 'info' }
  return map[priority] || 'info'
}

function statusLabel(status) {
  const map = { pending: '待启动', ongoing: '进行中', paused: '已暂停', completed: '已完成' }
  return map[status] || status
}

function statusTagType(status) {
  const map = { pending: 'info', ongoing: 'primary', paused: 'warning', completed: 'success' }
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
    implNo: '',
    proposalId: null,
    name: '',
    owner: '',
    department: '',
    startDate: '',
    endDate: '',
    priority: 'medium',
    progress: 0,
    description: ''
  })
}

// 打开新建弹窗
function openCreate() {
  isEdit.value = false
  dialogTitle.value = '新建改善实施'
  resetFormData()
  dialogVisible.value = true
}

// 查看详情
function viewDetail(row) {
  ElMessageBox.alert(
    `实施编号：${row.implNo}\n关联提案：${row.proposalNo || '-'}\n负责人：${row.owner}（${row.department || '-'}）\n周期：${row.startDate || '-'} ~ ${row.endDate || '-'}\n进度：${row.progress}%\n实施说明：${row.description || '-'}`,
    `实施详情：${row.name}`,
    { customClass: 'impl-detail-box' }
  ).catch(() => {})
}

// 编辑已有条目（已完成不可编辑，后端强校验）
function editItem(row) {
  isEdit.value = true
  dialogTitle.value = '编辑改善实施'
  resetFormData()
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 删除条目并二次确认
function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除实施记录「${row.name}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await axios.delete(`/api/kaizen/implementation/${row.id}`)
        ElMessage.success('删除成功')
        await loadList()
      } catch (e) {
        ElMessage.error('删除失败：' + errMsg(e))
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
  searchForm.owner = ''
  searchForm.status = ''
  pagination.currentPage = 1
  applyFilterAndPage()
}

// 刷新数据
async function refreshData() {
  await loadList()
  ElMessage.success('数据已刷新')
}

// 状态机动作：start(pending→ongoing) / pause(ongoing→paused) / resume(paused→ongoing) / complete(ongoing→completed)
const actionLabel = { start: '启动', pause: '暂停', resume: '继续', complete: '完成' }
function doAction(row, action) {
  ElMessageBox.confirm(`确定${actionLabel[action]}实施「${row.name}」吗？`, '操作确认', { type: 'warning' })
    .then(async () => {
      try {
        await axios.post(`/api/kaizen/implementation/${row.id}/${action}`)
        ElMessage.success(`已${actionLabel[action]}`)
        await loadList()
      } catch (e) {
        ElMessage.error('操作失败：' + errMsg(e))
      }
    }).catch(() => {})
}

// 保存新建/编辑的表单数据
async function saveData() {
  if (!formData.proposalId) {
    ElMessage.warning('请选择关联提案')
    return
  }
  if (!formData.name || !formData.owner) {
    ElMessage.warning('请填写必填项：实施名称、负责人')
    return
  }
  const payload = {
    proposalId: formData.proposalId,
    name: formData.name,
    owner: formData.owner,
    department: formData.department,
    startDate: formData.startDate || null,
    endDate: formData.endDate || null,
    priority: formData.priority,
    description: formData.description
  }
  try {
    if (isEdit.value) {
      // 已完成不可编辑；进度 0~100 后端校验
      payload.progress = formData.progress
      await axios.put(`/api/kaizen/implementation/${formData.id}`, payload)
      ElMessage.success('修改成功')
    } else {
      // implNo(IMP)/pending/进度0 由后端生成；联动提案 approved→implementing
      await axios.post('/api/kaizen/implementation', payload)
      ElMessage.success('新建成功')
    }
    dialogVisible.value = false
    await loadList()
  } catch (e) {
    ElMessage.error('保存失败：' + errMsg(e))
  }
}

onMounted(async () => {
  await loadProposals()
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

<template>
  <div class="page-card">
    <div class="page-header">
      <h2>项目验收</h2>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="openCreate">新建验收</el-button>
        <el-button :icon="Refresh" @click="refreshData">刷新</el-button>
      </div>
    </div>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="项目验收管理"
      description="面向改善项目开展结项申请、验收评审与归档管理，确保项目交付物完整可追溯。"
      style="margin-bottom: 16px;"
    />

    <el-card shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="项目名称">
          <el-input v-model="searchForm.projectName" placeholder="请输入项目名称" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="验收类型">
          <el-select v-model="searchForm.type" placeholder="选择类型" clearable style="width: 150px;">
            <el-option label="阶段验收" value="stage" />
            <el-option label="结项验收" value="closure" />
            <el-option label="提前终止验收" value="termination" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 140px;">
            <el-option label="待申请" value="pending" />
            <el-option label="待验收" value="reviewing" />
            <el-option label="已通过" value="passed" />
            <el-option label="未通过" value="rejected" />
            <el-option label="已归档" value="archived" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="doSearch">查询</el-button>
          <el-button :icon="RefreshRight" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="closureList" border stripe size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="closureNo" label="验收编号" width="140" />
        <el-table-column prop="projectName" label="项目名称" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.projectName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="projectNo" label="项目编号" width="130" />
        <el-table-column prop="manager" label="项目经理" width="100" />
        <el-table-column prop="type" label="验收类型" width="110">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="acceptDate" label="验收日期" width="120" />
        <el-table-column prop="score" label="验收评分" width="180">
          <template #default="{ row }">
            <el-rate v-model="row.score" disabled :max="5" />
          </template>
        </el-table-column>
        <el-table-column prop="conclusion" label="验收结论" width="100">
          <template #default="{ row }">
            <el-tag :type="conclusionTagType(row.conclusion)" size="small">{{ conclusionLabel(row.conclusion) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="viewDetail(row)">查看</el-button>
            <el-button v-if="row.status === 'pending' || row.status === 'rejected'" link type="warning" @click="submitClosure(row)">提交验收</el-button>
            <el-button v-if="row.status === 'reviewing'" link type="success" @click="reviewClosure(row, true)">通过</el-button>
            <el-button v-if="row.status === 'reviewing'" link type="danger" @click="reviewClosure(row, false)">驳回</el-button>
            <el-button v-if="row.status === 'passed'" link type="primary" @click="archiveClosure(row)">归档</el-button>
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
            <el-form-item label="验收编号">
              <el-input v-model="formData.closureNo" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属项目" required>
              <el-select v-model="formData.projectId" placeholder="选择项目" filterable style="width: 100%;" @change="onProjectChange">
                <el-option v-for="p in projectOptions" :key="p.id" :label="`${p.projectNo} ${p.name}`" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="项目名称">
          <el-input v-model="formData.projectName" disabled />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目经理">
              <el-input v-model="formData.manager" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="验收类型" required>
              <el-select v-model="formData.type" placeholder="选择验收类型" style="width: 100%;">
                <el-option label="阶段验收" value="stage" />
                <el-option label="结项验收" value="closure" />
                <el-option label="提前终止验收" value="termination" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="验收日期">
              <el-date-picker v-model="formData.acceptDate" type="date" placeholder="选择验收日期" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="验收结论">
              <el-select v-model="formData.conclusion" placeholder="选择验收结论" style="width: 100%;">
                <el-option label="通过" value="pass" />
                <el-option label="有条件通过" value="conditional" />
                <el-option label="不通过" value="fail" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="验收评分">
              <el-rate v-model="formData.score" :max="5" show-text :texts="['差', '较差', '一般', '良好', '优秀']" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="formData.status" placeholder="选择状态" style="width: 100%;">
                <el-option label="待申请" value="pending" />
                <el-option label="待验收" value="reviewing" />
                <el-option label="已通过" value="passed" />
                <el-option label="未通过" value="rejected" />
                <el-option label="已归档" value="archived" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="验收意见">
          <el-input v-model="formData.opinion" type="textarea" :rows="4" placeholder="请输入验收意见与改进建议" />
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
  projectName: '',
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
const dialogTitle = ref('新建项目验收')
const formRef = ref(null)
const isEdit = ref(false)

// 项目选项
const projectOptions = ref([])

// 新建/编辑用的表单数据
const formData = reactive({
  id: null,
  closureNo: '',
  projectId: null,
  projectNo: '',
  projectName: '',
  manager: '',
  type: 'closure',
  acceptDate: '',
  score: 3,
  conclusion: 'pass',
  status: 'pending',
  opinion: '',
  version: null
})

const allList = ref([])
const closureList = ref([])

const fmtDate = (d) => (d ? String(d).substring(0, 10) : '')
const projectNameOf = (d) => {
  const p = projectOptions.value.find(x => x.id === d.projectId)
  return p ? p.name : ''
}
const fromEntity = (d) => ({
  id: d.id,
  closureNo: d.closureNo,
  projectId: d.projectId,
  projectNo: d.projectNo,
  projectName: projectNameOf(d),
  manager: d.manager || '',
  type: d.type || 'closure',
  acceptDate: fmtDate(d.acceptDate),
  score: d.score || 0,
  conclusion: d.conclusion || '',
  status: d.status,
  opinion: d.opinion || '',
  version: d.version
})

const applyFilterAndPage = () => {
  let rows = allList.value
  if (searchForm.projectName) rows = rows.filter(r => (r.projectName || '').includes(searchForm.projectName))
  if (searchForm.type) rows = rows.filter(r => r.type === searchForm.type)
  if (searchForm.status) rows = rows.filter(r => r.status === searchForm.status)
  pagination.total = rows.length
  const start = (pagination.currentPage - 1) * pagination.pageSize
  closureList.value = rows.slice(start, start + pagination.pageSize)
}

const loadProjects = async () => {
  const res = await axios.get('/api/project/pm/list')
  projectOptions.value = (res.data && res.data.data ? res.data.data : []).map(p => ({
    id: p.id, name: p.name, projectNo: p.projectNo, manager: p.manager
  }))
}

const loadList = async () => {
  const res = await axios.get('/api/project/closure/list')
  allList.value = (res.data && res.data.data ? res.data.data : []).map(fromEntity)
  applyFilterAndPage()
}

const refreshData = async () => {
  await loadList()
  ElMessage.success('数据已刷新')
}

const onProjectChange = (projectId) => {
  const p = projectOptions.value.find(x => x.id === projectId)
  if (!p) return
  formData.projectNo = p.projectNo
  formData.projectName = p.name
  formData.manager = p.manager || ''
}

// 文案/标签辅助函数
function typeLabel(type) {
  const map = { stage: '阶段验收', closure: '结项验收', termination: '提前终止验收' }
  return map[type] || type
}

function typeTagType(type) {
  const map = { stage: 'primary', closure: 'success', termination: 'danger' }
  return map[type] || 'info'
}

function conclusionLabel(conclusion) {
  const map = { pass: '通过', conditional: '有条件通过', fail: '不通过' }
  return map[conclusion] || conclusion
}

function conclusionTagType(conclusion) {
  const map = { pass: 'success', conditional: 'warning', fail: 'danger' }
  return map[conclusion] || 'info'
}

function statusLabel(status) {
  const map = { pending: '待申请', reviewing: '待验收', passed: '已通过', rejected: '未通过', archived: '已归档' }
  return map[status] || status
}

function statusTagType(status) {
  const map = { pending: 'info', reviewing: 'warning', passed: 'success', rejected: 'danger', archived: 'primary' }
  return map[status] || 'info'
}

// 将表单数据重置为默认值
function resetFormData() {
  Object.assign(formData, {
    id: null,
    closureNo: '',
    projectId: null,
    projectNo: '',
    projectName: '',
    manager: '',
    type: 'closure',
    acceptDate: '',
    score: 3,
    conclusion: 'pass',
    status: 'pending',
    opinion: '',
    version: null
  })
}

// 打开新建弹窗
function openCreate() {
  isEdit.value = false
  dialogTitle.value = '新建项目验收'
  resetFormData()
  dialogVisible.value = true
}

// 查看详情
function viewDetail(row) {
  ElMessage.info(`查看验收详情：${row.projectName}`)
}

// 编辑已有条目
function editItem(row) {
  isEdit.value = true
  dialogTitle.value = '编辑项目验收'
  resetFormData()
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 删除条目并二次确认
function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除验收记录「${row.projectName}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await axios.delete(`/api/project/closure/${row.id}`)
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
  searchForm.projectName = ''
  searchForm.type = ''
  searchForm.status = ''
  pagination.currentPage = 1
  applyFilterAndPage()
}

// 保存新建/编辑的表单数据
async function saveData() {
  if (!formData.projectId) {
    ElMessage.warning('请选择所属项目')
    return
  }
  const payload = {
    projectId: formData.projectId,
    projectNo: formData.projectNo,
    manager: formData.manager,
    type: formData.type,
    acceptDate: formData.acceptDate || null,
    score: formData.score,
    conclusion: formData.conclusion,
    opinion: formData.opinion,
    version: formData.version
  }
  try {
    if (isEdit.value) {
      await axios.put(`/api/project/closure/${formData.id}`, payload)
      ElMessage.success('修改成功')
    } else {
      await axios.post('/api/project/closure', payload)
      ElMessage.success('新建成功')
    }
    dialogVisible.value = false
    await loadList()
  } catch (e) {
    ElMessage.error('保存失败：' + ((e.response && e.response.data && (e.response.data.message || e.response.data.msg)) || e.message))
  }
}

// 状态流转：提交验收(pending/rejected -> reviewing)
const submitClosure = (row) => {
  ElMessageBox.confirm(`确定提交「${row.projectName}」的结项验收吗？`, '提交确认', { type: 'warning' })
    .then(async () => {
      try {
        await axios.post(`/api/project/closure/${row.id}/submit`)
        ElMessage.success('已提交验收')
        await loadList()
      } catch (e) {
        ElMessage.error('提交失败：' + ((e.response && e.response.data && (e.response.data.message || e.response.data.msg)) || e.message))
      }
    }).catch(() => {})
}

// 评审：passed=true 通过，false 驳回（驳回需填写意见）
const reviewClosure = async (row, passed) => {
  let opinion = passed ? '验收通过' : ''
  if (!passed) {
    try {
      const r = await ElMessageBox.prompt('请输入驳回意见', '验收驳回', {
        confirmButtonText: '确定驳回',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputValidator: (v) => (v && v.trim() ? true : '驳回意见不能为空')
      })
      opinion = r.value
    } catch {
      return
    }
  }
  try {
    await axios.post(`/api/project/closure/${row.id}/review`, { passed, opinion })
    ElMessage.success(passed ? '验收通过' : '已驳回')
    await loadList()
  } catch (e) {
    ElMessage.error('评审失败：' + ((e.response && e.response.data && (e.response.data.message || e.response.data.msg)) || e.message))
  }
}

// 归档（passed -> archived，联动项目完成归档；后端强制四项闭环标志）
const archiveClosure = (row) => {
  ElMessageBox.confirm(`确定归档「${row.projectName}」吗？归档前系统将校验任务/费用/合同/预算四项闭环。`, '归档确认', { type: 'warning' })
    .then(async () => {
      try {
        await axios.post(`/api/project/closure/${row.id}/archive`)
        ElMessage.success('已归档')
        await loadList()
      } catch (e) {
        ElMessage.error('归档失败：' + ((e.response && e.response.data && (e.response.data.message || e.response.data.msg)) || e.message))
      }
    }).catch(() => {})
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

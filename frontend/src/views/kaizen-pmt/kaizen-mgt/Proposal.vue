<template>
  <div class="page-card">
    <div class="page-header">
      <h2>提案管理</h2>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="openCreate">新建提案</el-button>
        <el-button :icon="Refresh" @click="refreshData">刷新</el-button>
      </div>
    </div>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="改善提案管理"
      description="面向员工收集改善提案，统一进行评审、立项与跟踪，驱动持续改进文化落地。"
      style="margin-bottom: 16px;"
    />

    <el-card shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="提案名称">
          <el-input v-model="searchForm.name" placeholder="请输入提案名称" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="提案类型">
          <el-select v-model="searchForm.type" placeholder="选择类型" clearable style="width: 150px;">
            <el-option label="流程优化" value="process" />
            <el-option label="质量改进" value="quality" />
            <el-option label="成本节约" value="cost" />
            <el-option label="安全改善" value="safety" />
            <el-option label="设备改善" value="equipment" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 140px;">
            <el-option label="待评审" value="pending" />
            <el-option label="已立项" value="approved" />
            <el-option label="未通过" value="rejected" />
            <el-option label="实施中" value="implementing" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="doSearch">查询</el-button>
          <el-button :icon="RefreshRight" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="proposalList" border stripe size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="proposalNo" label="提案编号" width="130" />
        <el-table-column prop="name" label="提案名称" min-width="220">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="提案类型" width="110">
          <template #default="{ row }">
            <el-tag size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="proposer" label="提案人" width="100" />
        <el-table-column prop="department" label="所属部门" width="120" />
        <el-table-column prop="estimatedSavings" label="预计收益(元)" width="120" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.estimatedSavings) }}</template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90">
          <template #default="{ row }">
            <el-tag :type="priorityTagType(row.priority)" size="small">{{ priorityLabel(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitDate" label="提交日期" width="120" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="viewDetail(row)">查看</el-button>
            <el-button v-if="row.status === 'pending'" link type="success" @click="reviewItem(row, true)">立项通过</el-button>
            <el-button v-if="row.status === 'pending'" link type="danger" @click="reviewItem(row, false)">驳回</el-button>
            <el-button v-if="row.status === 'approved'" link type="warning" @click="startImpl(row)">启动实施</el-button>
            <el-button v-if="row.status === 'implementing'" link type="success" @click="completeItem(row)">完成</el-button>
            <el-button v-if="row.status === 'pending' || row.status === 'rejected'" link type="primary" :icon="Edit" @click="editItem(row)">编辑</el-button>
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
            <el-form-item label="提案编号">
              <el-input v-model="formData.proposalNo" placeholder="保存后自动生成（IP 前缀）" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="提案类型" required>
              <el-select v-model="formData.type" placeholder="选择提案类型" style="width: 100%;">
                <el-option label="流程优化" value="process" />
                <el-option label="质量改进" value="quality" />
                <el-option label="成本节约" value="cost" />
                <el-option label="安全改善" value="safety" />
                <el-option label="设备改善" value="equipment" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="提案名称" required>
          <el-input v-model="formData.name" placeholder="请输入提案名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="提案人" required>
              <el-input v-model="formData.proposer" placeholder="请输入提案人" />
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
            <el-form-item label="预计收益">
              <el-input-number v-model="formData.estimatedSavings" :min="0" :step="1000" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="formData.priority" placeholder="选择优先级" style="width: 100%;">
                <el-option label="高" value="high" />
                <el-option label="中" value="medium" />
                <el-option label="低" value="low" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="问题描述">
          <el-input v-model="formData.problem" type="textarea" :rows="3" placeholder="请描述当前存在的问题" />
        </el-form-item>
        <el-form-item label="改善方案">
          <el-input v-model="formData.solution" type="textarea" :rows="4" placeholder="请描述改善方案与预期效果" />
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
const dialogTitle = ref('新建改善提案')
const formRef = ref(null)
const isEdit = ref(false)

// 新建/编辑用的表单数据
const formData = reactive({
  id: null,
  proposalNo: '',
  name: '',
  type: 'process',
  proposer: '',
  department: '',
  estimatedSavings: 0,
  priority: 'medium',
  problem: '',
  solution: ''
})

const allList = ref([])
const proposalList = ref([])

const fmtDate = (v) => {
  if (!v) return ''
  if (Array.isArray(v)) {
    const [y, mo, d] = v
    const p = (n) => String(n).padStart(2, '0')
    return `${y}-${p(mo)}-${p(d)}`
  }
  return String(v).replace('T', ' ').substring(0, 10)
}
const formatMoney = (v) => (v == null || v === '' ? '0' : Number(v).toLocaleString())

const fromEntity = (d) => ({
  id: d.id,
  proposalNo: d.proposalNo,
  name: d.name,
  type: d.type || 'process',
  proposer: d.proposer || '',
  department: d.department || '',
  estimatedSavings: d.estimatedSavings == null ? 0 : Number(d.estimatedSavings),
  priority: d.priority || 'medium',
  status: d.status,
  submitDate: fmtDate(d.submitTime),
  approvalComment: d.approvalComment || '',
  problem: d.problem || '',
  solution: d.solution || ''
})

const applyFilterAndPage = () => {
  let rows = allList.value
  if (searchForm.name) rows = rows.filter(r => (r.name || '').includes(searchForm.name))
  if (searchForm.type) rows = rows.filter(r => r.type === searchForm.type)
  if (searchForm.status) rows = rows.filter(r => r.status === searchForm.status)
  pagination.total = rows.length
  const start = (pagination.currentPage - 1) * pagination.pageSize
  proposalList.value = rows.slice(start, start + pagination.pageSize)
}

const loadList = async () => {
  const res = await axios.get('/api/kaizen/proposal/list')
  allList.value = (res.data && res.data.data ? res.data.data : []).map(fromEntity)
  applyFilterAndPage()
}

const errMsg = (e) => (e.response && e.response.data && (e.response.data.message || e.response.data.msg)) || e.message

// 文案/标签辅助函数
function typeLabel(type) {
  const map = { process: '流程优化', quality: '质量改进', cost: '成本节约', safety: '安全改善', equipment: '设备改善', other: '其他' }
  return map[type] || type
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
  const map = { pending: '待评审', approved: '已立项', rejected: '未通过', implementing: '实施中', completed: '已完成' }
  return map[status] || status
}

function statusTagType(status) {
  const map = { pending: 'warning', approved: 'primary', rejected: 'danger', implementing: 'info', completed: 'success' }
  return map[status] || 'info'
}

// 将表单数据重置为默认值
function resetFormData() {
  Object.assign(formData, {
    id: null,
    proposalNo: '',
    name: '',
    type: 'process',
    proposer: '',
    department: '',
    estimatedSavings: 0,
    priority: 'medium',
    problem: '',
    solution: ''
  })
}

// 打开新建弹窗
function openCreate() {
  isEdit.value = false
  dialogTitle.value = '新建改善提案'
  resetFormData()
  dialogVisible.value = true
}

// 查看详情
function viewDetail(row) {
  ElMessageBox.alert(
    `编号：${row.proposalNo}\n类型：${typeLabel(row.type)}\n提案人：${row.proposer}（${row.department || '-'}）\n预计收益：¥${formatMoney(row.estimatedSavings)}\n问题描述：${row.problem || '-'}\n改善方案：${row.solution || '-'}\n评审意见：${row.approvalComment || '-'}`,
    `提案详情：${row.name}`,
    { customClass: 'proposal-detail-box' }
  ).catch(() => {})
}

// 编辑已有条目（仅待评审/未通过可编辑，后端强校验）
function editItem(row) {
  isEdit.value = true
  dialogTitle.value = '编辑改善提案'
  resetFormData()
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 删除条目并二次确认
function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除提案「${row.name}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await axios.delete(`/api/kaizen/proposal/${row.id}`)
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
  searchForm.type = ''
  searchForm.status = ''
  pagination.currentPage = 1
  applyFilterAndPage()
}

// 刷新数据
async function refreshData() {
  await loadList()
  ElMessage.success('数据已刷新')
}

// 评审：approved=true 立项通过，false 驳回
async function reviewItem(row, approved) {
  let comment = approved ? '同意立项' : ''
  try {
    const r = await ElMessageBox.prompt(approved ? '请输入立项意见' : '请输入驳回原因', approved ? '立项评审' : '驳回归档', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputValue: approved ? comment : '',
      inputValidator: (v) => (v && v.trim() ? true : '意见不能为空')
    })
    comment = r.value
  } catch {
    return
  }
  try {
    await axios.post(`/api/kaizen/proposal/${row.id}/review`, { approved, comment })
    ElMessage.success(approved ? '已立项' : '已驳回')
    await loadList()
  } catch (e) {
    ElMessage.error('评审失败：' + errMsg(e))
  }
}

// approved -> implementing（/api/improve 同属 8142）
function startImpl(row) {
  ElMessageBox.confirm(`确定启动提案「${row.name}」的实施吗？`, '启动实施', { type: 'warning' })
    .then(async () => {
      try {
        await axios.post(`/api/improve/proposal/${row.id}/start-implementation`)
        ElMessage.success('已进入实施阶段')
        await loadList()
      } catch (e) {
        ElMessage.error('启动实施失败：' + errMsg(e))
      }
    }).catch(() => {})
}

// implementing -> completed
function completeItem(row) {
  ElMessageBox.confirm(`确定将提案「${row.name}」标记为完成吗？`, '完成确认', { type: 'warning' })
    .then(async () => {
      try {
        await axios.post(`/api/improve/proposal/${row.id}/complete`)
        ElMessage.success('提案已完成')
        await loadList()
      } catch (e) {
        ElMessage.error('操作失败：' + errMsg(e))
      }
    }).catch(() => {})
}

// 保存新建/编辑的表单数据
async function saveData() {
  if (!formData.name || !formData.type || !formData.proposer) {
    ElMessage.warning('请填写必填项：提案名称、提案类型、提案人')
    return
  }
  const payload = {
    name: formData.name,
    type: formData.type,
    proposer: formData.proposer,
    department: formData.department,
    estimatedSavings: formData.estimatedSavings,
    priority: formData.priority,
    problem: formData.problem,
    solution: formData.solution
  }
  try {
    if (isEdit.value) {
      await axios.put(`/api/kaizen/proposal/${formData.id}`, payload)
      ElMessage.success('修改成功')
    } else {
      // proposalNo(IP 前缀)/pending/submitTime 均由后端生成
      await axios.post('/api/kaizen/proposal', payload)
      ElMessage.success('新建成功')
    }
    dialogVisible.value = false
    await loadList()
  } catch (e) {
    ElMessage.error('保存失败：' + errMsg(e))
  }
}

onMounted(loadList)
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

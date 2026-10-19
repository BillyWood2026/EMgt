<template>
  <div class="page-container">
    <div class="page-header">
      <h2>改善提案</h2>
      <el-button type="primary" @click="handleAdd">提交提案</el-button>
    </div>

    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon blue"><el-icon><Lightbulb /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ totalCount }}</div>
            <div class="stat-label">提案总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon green"><el-icon><CheckCircle /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ approvedCount }}</div>
            <div class="stat-label">已采纳</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon orange"><el-icon><Clock /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ pendingCount }}</div>
            <div class="stat-label">审批中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon red"><el-icon><CircleClose /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ rejectedCount }}</div>
            <div class="stat-label">未采纳</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="提案名称">
        <el-input v-model="searchForm.name" placeholder="请输入提案名称" style="width:180px" />
      </el-form-item>
      <el-form-item label="提案类型">
        <el-select v-model="searchForm.type" placeholder="选择类型" style="width:150px">
          <el-option label="流程优化" value="process" />
          <el-option label="质量改进" value="quality" />
          <el-option label="成本节约" value="cost" />
          <el-option label="安全改善" value="safety" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width:120px">
          <el-option label="待审批" value="pending" />
          <el-option label="已采纳" value="approved" />
          <el-option label="未采纳" value="rejected" />
          <el-option label="实施中" value="implementing" />
          <el-option label="已完成" value="completed" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="proposalList" border stripe size="small">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="proposalNo" label="提案编号" width="140" />
      <el-table-column prop="name" label="提案名称" min-width="200">
        <template #default="{row}">
          <el-link type="primary" @click="viewDetail(row)">{{ row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="提案类型" width="120">
        <template #default="{row}">
          <el-tag size="small">{{ getTypeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="department" label="所属部门" width="120" />
      <el-table-column prop="proposer" label="提案人" width="100" />
      <el-table-column prop="estimatedSavings" label="预计收益" width="120">
        <template #default="{row}">¥{{ formatMoney(row.estimatedSavings) }}</template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="100">
        <template #default="{row}">
          <el-tag :type="getPriorityType(row.priority)" size="small">{{ getPriorityLabel(row.priority) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submitTime" label="提交时间" width="180" />
      <el-table-column label="操作" width="300">
        <template #default="{row}">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button v-if="row.status === 'pending' || row.status === 'rejected'" size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 'pending'" size="small" type="success" @click="handleApprove(row)">采纳</el-button>
          <el-button v-if="row.status === 'pending'" size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
          <el-button v-if="row.status === 'approved'" size="small" type="warning" @click="handleImplement(row)">实施</el-button>
          <el-button v-if="row.status === 'implementing'" size="small" type="success" @click="handleComplete(row)">完成</el-button>
          <el-button v-if="row.status === 'pending' || row.status === 'rejected'" size="small" type="danger" text @click="handleDelete(row)">删除</el-button>
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
        <el-form-item label="提案编号">
          <el-input v-model="formData.proposalNo" placeholder="提交后自动生成（IP 前缀）" disabled />
        </el-form-item>
        <el-form-item label="提案名称" required>
          <el-input v-model="formData.name" placeholder="请输入提案名称" />
        </el-form-item>
        <el-form-item label="提案类型" required>
          <el-select v-model="formData.type" placeholder="选择提案类型">
            <el-option label="流程优化" value="process" />
            <el-option label="质量改进" value="quality" />
            <el-option label="成本节约" value="cost" />
            <el-option label="安全改善" value="safety" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属部门">
          <el-input v-model="formData.department" placeholder="请输入所属部门" />
        </el-form-item>
        <el-form-item label="提案人">
          <el-input v-model="formData.proposer" placeholder="请输入提案人" />
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input v-model="formData.problem" type="textarea" :rows="3" placeholder="请描述当前存在的问题" />
        </el-form-item>
        <el-form-item label="改善方案">
          <el-input v-model="formData.solution" type="textarea" :rows="4" placeholder="请描述改善方案" />
        </el-form-item>
        <el-form-item label="预计收益">
          <el-input-number v-model="formData.estimatedSavings" :min="0" prefix="¥" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="formData.priority">
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="改善提案详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="提案编号">{{ detailData.proposalNo }}</el-descriptions-item>
        <el-descriptions-item label="提案名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="提案类型">{{ getTypeLabel(detailData.type) }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ detailData.department }}</el-descriptions-item>
        <el-descriptions-item label="提案人">{{ detailData.proposer }}</el-descriptions-item>
        <el-descriptions-item label="预计收益">¥{{ formatMoney(detailData.estimatedSavings) }}</el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityType(detailData.priority)" size="small">{{ getPriorityLabel(detailData.priority) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)" size="small">{{ getStatusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">{{ detailData.problem }}</el-descriptions-item>
        <el-descriptions-item label="改善方案" :span="2">{{ detailData.solution }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detailData.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="审批意见">{{ detailData.approvalComment || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
const dialogTitle = ref('提交改善提案')
const formRef = ref(null)

const formData = reactive({
  id: null,
  proposalNo: '',
  name: '',
  type: 'process',
  department: '',
  proposer: '',
  problem: '',
  solution: '',
  estimatedSavings: 0,
  priority: 'medium',
  status: 'pending',
  submitTime: '',
  approvalComment: ''
})

const detailData = reactive({})

const allList = ref([])
const proposalList = ref([])

// LocalDateTime 可能是 ISO 字符串或 [yyyy,M,d,H,m,s] 数组
const fmtDateTime = (v) => {
  if (!v) return ''
  if (Array.isArray(v)) {
    const [y, mo, d, h = 0, mi = 0, s = 0] = v
    const p = (n) => String(n).padStart(2, '0')
    return `${y}-${p(mo)}-${p(d)} ${p(h)}:${p(mi)}:${p(s)}`
  }
  return String(v).replace('T', ' ').substring(0, 19)
}
const formatMoney = (v) => (v == null || v === '' ? '0' : Number(v).toLocaleString())

const fromEntity = (d) => ({
  id: d.id,
  proposalNo: d.proposalNo,
  name: d.name,
  type: d.type,
  department: d.department || '',
  proposer: d.proposer || '',
  problem: d.problem || '',
  solution: d.solution || '',
  estimatedSavings: d.estimatedSavings == null ? 0 : Number(d.estimatedSavings),
  priority: d.priority || 'medium',
  status: d.status,
  submitTime: fmtDateTime(d.submitTime),
  approvalComment: d.approvalComment || ''
})

const applyFilterAndPage = () => {
  let rows = allList.value
  if (searchForm.name) rows = rows.filter(p => (p.name || '').includes(searchForm.name))
  if (searchForm.type) rows = rows.filter(p => p.type === searchForm.type)
  if (searchForm.status) rows = rows.filter(p => p.status === searchForm.status)
  pagination.total = rows.length
  const start = (pagination.currentPage - 1) * pagination.pageSize
  proposalList.value = rows.slice(start, start + pagination.pageSize)
}

const loadList = async () => {
  const res = await axios.get('/api/kaizen/proposal/list')
  allList.value = (res.data && res.data.data ? res.data.data : []).map(fromEntity)
  applyFilterAndPage()
}

const totalCount = computed(() => allList.value.length)
const approvedCount = computed(() => allList.value.filter(p => p.status === 'approved').length)
const pendingCount = computed(() => allList.value.filter(p => p.status === 'pending').length)
const rejectedCount = computed(() => allList.value.filter(p => p.status === 'rejected').length)

function getTypeLabel(type) {
  const map = { process: '流程优化', quality: '质量改进', cost: '成本节约', safety: '安全改善', other: '其他' }
  return map[type] || type
}

function getPriorityLabel(priority) {
  const map = { high: '高', medium: '中', low: '低' }
  return map[priority] || priority
}

function getPriorityType(priority) {
  const map = { high: 'danger', medium: 'warning', low: 'info' }
  return map[priority] || 'info'
}

function getStatusLabel(status) {
  const map = { pending: '待审批', approved: '已采纳', rejected: '未采纳', implementing: '实施中', completed: '已完成' }
  return map[status] || status
}

function getStatusType(status) {
  const map = { pending: 'warning', approved: 'primary', rejected: 'danger', implementing: 'info', completed: 'success' }
  return map[status] || 'info'
}

const errMsg = (e, fallback) => (e.response && e.response.data && (e.response.data.message || e.response.data.msg)) || e.message || fallback

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
  dialogTitle.value = '提交改善提案'
  Object.assign(formData, {
    id: null,
    proposalNo: '',
    name: '',
    type: 'process',
    department: '',
    proposer: '',
    problem: '',
    solution: '',
    estimatedSavings: 0,
    priority: 'medium',
    status: 'pending',
    submitTime: '',
    approvalComment: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑改善提案'
  Object.assign(formData, row)
  dialogVisible.value = true
}

function viewDetail(row) {
  Object.assign(detailData, row)
  detailVisible.value = true
}

// 评审：approved=true 采纳，false 拒绝（均提交审批意见）
async function doReview(row, approved) {
  let comment = approved ? '同意采纳，建议尽快实施' : ''
  try {
    const r = await ElMessageBox.prompt(approved ? '请输入采纳意见' : '请输入拒绝原因', approved ? '采纳提案' : '拒绝提案', {
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
    ElMessage.success(approved ? '提案已采纳' : '提案已拒绝')
    await loadList()
  } catch (e) {
    ElMessage.error('评审失败：' + errMsg(e))
  }
}

function handleApprove(row) {
  doReview(row, true)
}

function handleReject(row) {
  doReview(row, false)
}

// approved -> implementing（走 /api/improve 别名，同属 8142）
function handleImplement(row) {
  ElMessageBox.confirm(`确定启动「${row.name}」的实施吗？`, '启动实施', { type: 'warning' })
    .then(async () => {
      try {
        await axios.post(`/api/improve/proposal/${row.id}/start-implementation`)
        ElMessage.success('提案已进入实施阶段')
        await loadList()
      } catch (e) {
        ElMessage.error('启动实施失败：' + errMsg(e))
      }
    }).catch(() => {})
}

function handleComplete(row) {
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

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除提案「${row.name}」吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      try {
        await axios.delete(`/api/kaizen/proposal/${row.id}`)
        ElMessage.success('删除成功')
        await loadList()
      } catch (e) {
        ElMessage.error('删除失败：' + errMsg(e))
      }
    }).catch(() => {})
}

async function handleSubmit() {
  if (!formData.name || !formData.type) {
    ElMessage.warning('请填写提案名称与类型')
    return
  }
  const payload = {
    name: formData.name,
    type: formData.type,
    department: formData.department,
    proposer: formData.proposer,
    problem: formData.problem,
    solution: formData.solution,
    estimatedSavings: formData.estimatedSavings,
    priority: formData.priority
  }
  try {
    if (formData.id) {
      // 仅 pending/rejected 可编辑（后端强校验）
      await axios.put(`/api/kaizen/proposal/${formData.id}`, payload)
      ElMessage.success('修改成功')
    } else {
      // proposalNo / pending 状态 / submitTime 均由后端生成
      await axios.post('/api/kaizen/proposal', payload)
      ElMessage.success('提交成功')
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
</style>
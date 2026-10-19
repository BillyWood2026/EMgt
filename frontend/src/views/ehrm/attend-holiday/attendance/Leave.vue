<template>
  <div class="page-card">
    <div class="page-header">
      <h2>请假管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>新建请假
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
      title="请假管理"
      description="管理员工请假申请，覆盖事假、病假、年假、婚假、产假等类型，支持审批流程跟踪与假期余额扣减。"
      style="margin-bottom: 16px"
    />

    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="员工姓名">
          <el-input v-model="searchForm.employeeName" placeholder="请输入姓名" style="width: 180px" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.department" placeholder="选择部门" style="width: 180px" clearable>
            <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
          </el-select>
        </el-form-item>
        <el-form-item label="请假类型">
          <el-select v-model="searchForm.leaveType" placeholder="选择类型" style="width: 150px" clearable>
            <el-option v-for="item in leaveTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态">
          <el-select v-model="searchForm.approveStatus" placeholder="选择状态" style="width: 150px" clearable>
            <el-option label="待审批" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
            <el-option label="已撤销" value="canceled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="leaveList" border stripe size="small">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="leaveNo" label="请假单号" width="140" />
        <el-table-column label="员工姓名" width="110">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.employeeName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column label="请假类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getLeaveTypeLabel(row.leaveType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="duration" label="时长(天)" width="100" />
        <el-table-column label="审批状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getApproveStatusType(row.approveStatus)" size="small">{{ getApproveStatusLabel(row.approveStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button v-if="row.approveStatus === 'pending'" link size="small" type="success" @click="approveItem(row)">审批</el-button>
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
            <el-form-item label="员工姓名" required>
              <el-input v-model="formData.employeeName" placeholder="请输入员工姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门" required>
              <el-select v-model="formData.departmentId" placeholder="选择部门" style="width: 100%">
                <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="请假类型" required>
              <el-select v-model="formData.leaveTypeCode" placeholder="选择类型" style="width: 100%">
                <el-option v-for="item in leaveTypes" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="时长(天)" required>
              <el-input-number v-model="formData.totalDays" :min="0.5" :step="0.5" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" required>
              <el-date-picker v-model="formData.startDate" type="date" placeholder="选择开始日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" required>
              <el-date-picker v-model="formData.endDate" type="date" placeholder="选择结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="请假原因" required>
          <el-input type="textarea" v-model="formData.leaveReason" :rows="4" placeholder="请详细说明请假原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveData">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import request from '@/api/axiosConfig'

const departments = ['研发部', '生产部', '销售部', '财务部', '人力资源部', '行政部']

const leaveTypes = [
  { value: 'personal', label: '事假' },
  { value: 'sick', label: '病假' },
  { value: 'annual', label: '年假' },
  { value: 'marriage', label: '婚假' },
  { value: 'maternity', label: '产假' },
  { value: 'compassionate', label: '丧假' }
]

const STATUS_MAP = { DRAFT: 'pending', PENDING: 'pending', APPROVED: 'approved', REJECTED: 'rejected', CANCELLED: 'canceled' }
const STATUS_REVERSE = { pending: 'PENDING', approved: 'APPROVED', rejected: 'REJECTED', canceled: 'CANCELLED' }

const searchForm = reactive({ employeeName: '', department: '', leaveType: '', approveStatus: '' })

const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const dialogVisible = ref(false)
const dialogTitle = ref('新建请假')
const formRef = ref(null)

const formData = reactive({
  id: '', requestNo: '', employeeName: '', departmentId: '', leaveTypeCode: '',
  startDate: '', endDate: '', totalDays: 1, leaveReason: '', requestStatus: 'PENDING'
})

const leaveList = ref([])

function getLeaveTypeLabel(type) {
  const item = leaveTypes.find(i => i.value === type)
  return item ? item.label : type
}
function getApproveStatusLabel(status) {
  const labels = { pending: '待审批', approved: '已通过', rejected: '已驳回', canceled: '已撤销' }
  return labels[status] || status
}
function getApproveStatusType(status) {
  const types = { pending: 'warning', approved: 'success', rejected: 'danger', canceled: 'info' }
  return types[status] || 'info'
}

async function loadLeave() {
  try {
    const params = { pageNum: pagination.currentPage, pageSize: pagination.pageSize }
    if (searchForm.approveStatus) params.requestStatus = STATUS_REVERSE[searchForm.approveStatus]
    const { data } = await request.get('/api/holiday/request/list', { params })
    const records = data?.records || []
    leaveList.value = records.map(r => ({
      id: r.id, leaveNo: r.requestNo, employeeName: r.employeeName,
      department: r.departmentId, leaveType: r.leaveTypeCode,
      startDate: r.startDate ? String(r.startDate).slice(0, 10) : '',
      endDate: r.endDate ? String(r.endDate).slice(0, 10) : '',
      duration: r.totalDays, reason: r.leaveReason,
      approveStatus: STATUS_MAP[r.requestStatus] || 'pending'
    }))
    pagination.total = data?.total || 0
  } catch (e) {
    ElMessage.error('请假列表加载失败：' + (e.message || ''))
  }
}

function doSearch() { loadLeave() }
function resetSearch() {
  searchForm.employeeName = ''; searchForm.department = ''; searchForm.leaveType = ''; searchForm.approveStatus = ''
  loadLeave()
}
function refreshData() { loadLeave(); ElMessage.success('数据已刷新') }

function openCreate() {
  dialogTitle.value = '新建请假'
  Object.assign(formData, {
    id: '', requestNo: '', employeeName: '', departmentId: '', leaveTypeCode: '',
    startDate: '', endDate: '', totalDays: 1, leaveReason: '', requestStatus: 'PENDING'
  })
  dialogVisible.value = true
}

function viewDetail(row) { ElMessage.info(`查看 ${row.employeeName} 的请假单 ${row.leaveNo} 详情`) }

async function approveItem(row) {
  ElMessageBox.confirm(`确定通过 ${row.employeeName} 的请假申请吗？`, '审批确认', { type: 'warning' })
    .then(async () => {
      try { await request.post(`/api/holiday/request/approve/${row.id}`); ElMessage.success('已通过'); loadLeave() }
      catch (e) { ElMessage.error('审批失败：' + (e.response?.data?.message || e.message || '')) }
    }).catch(() => {})
}

async function rejectItem(row) {
  try { await request.post(`/api/holiday/request/reject/${row.id}`); ElMessage.success('已驳回'); loadLeave() }
  catch (e) { ElMessage.error('操作失败') }
}

async function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除请假单 ${row.leaveNo} 吗？`, '删除确认', { type: 'warning' })
    .then(async () => { try { await request.delete(`/api/holiday/request/${row.id}`); ElMessage.success('已删除'); loadLeave() } catch(e){ ElMessage.error('删除失败') } })
    .catch(() => {})
}

async function saveData() {
  if (!formData.employeeName || !formData.leaveTypeCode || !formData.startDate || !formData.endDate) {
    ElMessage.warning('请填写必填项'); return
  }
  try {
    await request.post('/api/holiday/request', { ...formData })
    dialogVisible.value = false
    ElMessage.success('请假申请已提交')
    loadLeave()
  } catch (e) {
    ElMessage.error('提交失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => loadLeave())
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.search-form { margin-bottom: 0; }
</style>

<template>
  <div class="page-card">
    <div class="page-header">
      <h2>加班管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>新建加班
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
      title="加班管理"
      description="登记员工加班申请，区分工作日、周末、节假日加班，支持调休或加班费两种补偿方式，自动累计加班时长。"
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
        <el-form-item label="加班类型">
          <el-select v-model="searchForm.overtimeType" placeholder="选择类型" style="width: 150px" clearable>
            <el-option label="工作日加班" value="weekday" />
            <el-option label="周末加班" value="weekend" />
            <el-option label="节假日加班" value="holiday" />
          </el-select>
        </el-form-item>
        <el-form-item label="补偿方式">
          <el-select v-model="searchForm.compensation" placeholder="选择补偿方式" style="width: 150px" clearable>
            <el-option label="调休" value="timeoff" />
            <el-option label="加班费" value="pay" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="overtimeList" border stripe size="small">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="overtimeNo" label="加班单号" width="140" />
        <el-table-column label="员工姓名" width="110">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.employeeName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column label="加班类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getOvertimeTypeTag(row.overtimeType)" size="small">{{ getOvertimeTypeLabel(row.overtimeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column prop="duration" label="时长(h)" width="100" />
        <el-table-column label="补偿方式" width="100">
          <template #default="{ row }">
            <el-tag :type="row.compensation === 'timeoff' ? 'success' : 'warning'" size="small">{{ row.compensation === 'timeoff' ? '调休' : '加班费' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button v-if="row.status === '待审批'" link size="small" type="success" @click="approveItem(row)">审批</el-button>
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
              <el-select v-model="formData.department" placeholder="选择部门" style="width: 100%">
                <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="加班类型" required>
              <el-select v-model="formData.overtimeType" placeholder="选择类型" style="width: 100%">
                <el-option label="工作日加班" value="weekday" />
                <el-option label="周末加班" value="weekend" />
                <el-option label="节假日加班" value="holiday" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="补偿方式" required>
              <el-select v-model="formData.compensation" placeholder="选择补偿方式" style="width: 100%">
                <el-option label="调休" value="timeoff" />
                <el-option label="加班费" value="pay" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" required>
              <el-date-picker v-model="formData.startDate" type="datetime" placeholder="选择开始时间" value-format="YYYY-MM-DD HH:mm" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" required>
              <el-date-picker v-model="formData.endDate" type="datetime" placeholder="选择结束时间" value-format="YYYY-MM-DD HH:mm" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="加班原因" required>
          <el-input type="textarea" v-model="formData.reason" :rows="4" placeholder="请说明加班原因及工作内容" />
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

const STATUS_MAP = { PENDING: '待审批', APPROVED: '已批准', REJECTED: '已驳回', CANCELLED: '已撤销' }

const searchForm = reactive({ employeeName: '', department: '', overtimeType: '', compensation: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('新建加班')
const formRef = ref(null)

const formData = reactive({
  employeeName: '', department: '', requestType: 'OVERTIME', overtimeType: '',
  startDate: '', endDate: '', durationHours: 0, compensation: 'timeoff', reason: '', approvalStatus: 'PENDING'
})

const overtimeList = ref([])

function getOvertimeTypeLabel(type) {
  const labels = { weekday: '工作日加班', weekend: '周末加班', holiday: '节假日加班' }
  return labels[type] || type
}
function getOvertimeTypeTag(type) {
  const types = { weekday: 'primary', weekend: 'warning', holiday: 'danger' }
  return types[type] || 'info'
}
function getStatusType(status) {
  const types = { '待审批': 'warning', '已批准': 'success', '已驳回': 'danger', '已撤销': 'info' }
  return types[status] || 'info'
}

async function loadOvertime() {
  try {
    const { data } = await request.get('/api/attendance/requests', {
      params: { pageNum: pagination.currentPage, pageSize: pagination.pageSize, requestType: 'OVERTIME' }
    })
    const records = data?.records || []
    overtimeList.value = records.map(r => ({
      id: r.id, overtimeNo: r.requestNo, employeeName: r.employeeName,
      department: '', overtimeType: r.requestType,
      startTime: r.startDate ? String(r.startDate).slice(0, 16).replace('T', ' ') : '',
      endTime: r.endDate ? String(r.endDate).slice(0, 16).replace('T', ' ') : '',
      duration: r.durationHours, compensation: '', reason: r.reason || '',
      status: STATUS_MAP[r.approvalStatus] || '待审批'
    }))
    pagination.total = data?.total || 0
  } catch (e) {
    ElMessage.error('加班列表加载失败：' + (e.message || ''))
  }
}

function doSearch() { loadOvertime() }
function resetSearch() {
  searchForm.employeeName = ''; searchForm.department = ''; searchForm.overtimeType = ''; searchForm.compensation = ''
  loadOvertime()
}
function refreshData() { loadOvertime(); ElMessage.success('数据已刷新') }

function openCreate() {
  dialogTitle.value = '新建加班'
  Object.assign(formData, {
    employeeName: '', department: '', requestType: 'OVERTIME', overtimeType: '',
    startDate: '', endDate: '', durationHours: 0, compensation: 'timeoff', reason: '', approvalStatus: 'PENDING'
  })
  dialogVisible.value = true
}

function viewDetail(row) { ElMessage.info(`查看 ${row.employeeName} 的加班单 ${row.overtimeNo} 详情`) }

async function approveItem(row) {
  ElMessageBox.confirm(`确定通过 ${row.employeeName} 的加班申请吗？`, '审批确认', { type: 'warning' })
    .then(async () => {
      try { await request.put(`/api/attendance/requests/${row.overtimeNo}/approve`); ElMessage.success('已通过'); loadOvertime() }
      catch (e) { ElMessage.error('审批失败') }
    }).catch(() => {})
}

async function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除加班单 ${row.overtimeNo} 吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      try { await request.put(`/api/attendance/requests/${row.overtimeNo}/cancel`); ElMessage.success('已撤销'); loadOvertime() }
      catch (e) { ElMessage.error('操作失败') }
    }).catch(() => {})
}

async function saveData() {
  if (!formData.employeeName || !formData.startDate || !formData.endDate) {
    ElMessage.warning('请填写必填项'); return
  }
  try {
    await request.post('/api/attendance/requests/overtime', { ...formData })
    dialogVisible.value = false
    ElMessage.success('加班申请已提交')
    loadOvertime()
  } catch (e) {
    ElMessage.error('提交失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => loadOvertime())
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.search-form { margin-bottom: 0; }
</style>

<template>
  <div class="page-card">
    <div class="page-header">
      <h2>日常打卡</h2>
      <div class="header-actions">
        <ImportExportBar
          :columns="ioColumns"
          :rows="attendanceList"
          file-name="打卡记录"
          title="打卡记录"
          import-api="/api/attendance/clock-in/fill-card-request"
          :field-map="ioFieldMap"
          @done="loadRecords"
        />
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>新建打卡
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
      title="日常打卡"
      description="记录员工每日上班/下班打卡时间，自动识别迟到、早退、缺卡等异常状态，支持手动补卡。"
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
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 150px" clearable>
            <el-option label="正常" value="normal" />
            <el-option label="迟到" value="late" />
            <el-option label="早退" value="early" />
            <el-option label="缺卡" value="missing" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="attendanceList" border stripe size="small">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="employeeNo" label="工号" width="100" />
        <el-table-column label="员工姓名" width="110">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.employeeName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="recordDate" label="日期" width="120" />
        <el-table-column prop="checkIn" label="上班打卡" width="120" />
        <el-table-column prop="checkOut" label="下班打卡" width="120" />
        <el-table-column prop="workHours" label="工时(h)" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" @click="editItem(row)">补卡</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="员工姓名" required>
              <el-input v-model="formData.employeeName" placeholder="请输入员工姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工号" required>
              <el-input v-model="formData.employeeNo" placeholder="请输入工号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部门" required>
              <el-select v-model="formData.department" placeholder="选择部门" style="width: 100%">
                <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="打卡日期" required>
              <el-date-picker
                v-model="formData.recordDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上班打卡">
              <el-time-picker v-model="formData.checkIn" placeholder="上班时间" format="HH:mm" value-format="HH:mm" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下班打卡">
              <el-time-picker v-model="formData.checkOut" placeholder="下班时间" format="HH:mm" value-format="HH:mm" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工时(h)">
              <el-input-number v-model="formData.workHours" :min="0" :max="24" :step="0.5" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="formData.status" placeholder="选择状态" style="width: 100%">
                <el-option label="正常" value="normal" />
                <el-option label="迟到" value="late" />
                <el-option label="早退" value="early" />
                <el-option label="缺卡" value="missing" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="formData.remark" :rows="3" placeholder="请输入备注说明，如补卡原因等" />
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
import { Plus, Refresh } from '@element-plus/icons-vue'
import request from '@/api/axiosConfig'
import ImportExportBar from '@/components/ImportExportBar.vue'

const ioColumns = [
  { key: 'employeeNo', title: '工号' },
  { key: 'employeeName', title: '员工姓名' },
  { key: 'department', title: '部门' },
  { key: 'recordDate', title: '日期' },
  { key: 'checkIn', title: '上班打卡' },
  { key: 'checkOut', title: '下班打卡' },
  { key: 'workHours', title: '工时(h)' },
  { key: 'status', title: '状态' },
  { key: 'remark', title: '备注' }
]
const ioFieldMap = {
  '工号': 'employeeNo',
  '员工姓名': 'employeeName',
  '部门': 'department',
  '日期': 'recordDate',
  '打卡日期': 'recordDate',
  '上班打卡': 'checkIn',
  '下班打卡': 'checkOut',
  '工时(h)': 'workHours',
  '工时': 'workHours',
  '状态': 'status',
  '备注': 'remark'
}

const departments = ['研发部', '生产部', '销售部', '财务部', '人力资源部', '行政部']

const searchForm = reactive({ employeeName: '', department: '', dateRange: [], status: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('新建打卡')
const formRef = ref(null)

const formData = reactive({
  employeeNo: '', employeeName: '', department: '', recordDate: '',
  checkIn: '', checkOut: '', workHours: 0, status: 'normal', remark: ''
})

const attendanceList = ref([])

function getStatusLabel(status) {
  const labels = { normal: '正常', late: '迟到', early: '早退', missing: '缺卡' }
  return labels[status] || status
}
function getStatusType(status) {
  const types = { normal: 'success', late: 'warning', early: 'warning', missing: 'danger' }
  return types[status] || 'info'
}

async function loadRecords() {
  try {
    const params = { pageNum: pagination.currentPage, pageSize: pagination.pageSize }
    if (searchForm.employeeName) params.employeeName = searchForm.employeeName
    if (searchForm.department) params.department = searchForm.department
    if (searchForm.status) params.status = searchForm.status
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const { data } = await request.get('/api/attendance/clock-in', { params })
    const records = data?.records || []
    const RISK_MAP = { NORMAL: 'normal', LATE: 'late', EARLY: 'early', MISSING: 'missing' }
    attendanceList.value = records.map(r => {
      const dt = r.clockInDateTime ? String(r.clockInDateTime) : ''
      const date = r.recordDate ? String(r.recordDate).slice(0, 10) : dt.slice(0, 10)
      const time = dt.slice(11, 16)
      return {
        id: r.id, employeeNo: r.employeeId, employeeName: r.employeeName,
        department: r.department || '', recordDate: date,
        checkIn: r.checkIn || (r.clockInType === 'IN' ? time : ''),
        checkOut: r.checkOut || (r.clockInType === 'OUT' ? time : ''),
        workHours: r.workHours ?? 0,
        status: RISK_MAP[r.riskFlag] || 'normal',
        remark: r.remark || r.riskDescription || ''
      }
    })
    pagination.total = data?.total || 0
  } catch (e) {
    ElMessage.error('打卡记录加载失败：' + (e.message || ''))
  }
}

function doSearch() { loadRecords() }
function resetSearch() {
  searchForm.employeeName = ''; searchForm.department = ''; searchForm.dateRange = []; searchForm.status = ''
  loadRecords()
}
function refreshData() { loadRecords(); ElMessage.success('数据已刷新') }

function openCreate() {
  dialogTitle.value = '新建打卡'
  Object.assign(formData, {
    employeeNo: '', employeeName: '', department: '', recordDate: '',
    checkIn: '', checkOut: '', workHours: 0, status: 'normal', remark: ''
  })
  dialogVisible.value = true
}

function viewDetail(row) { ElMessage.info(`查看员工 ${row.employeeName} 在 ${row.recordDate} 的打卡详情`) }

function editItem(row) {
  dialogTitle.value = '补卡'
  Object.assign(formData, {
    employeeNo: row.employeeNo, employeeName: row.employeeName, department: row.department,
    recordDate: row.recordDate, checkIn: row.checkIn, checkOut: row.checkOut,
    workHours: row.workHours, status: row.status, remark: ''
  })
  dialogVisible.value = true
}

async function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除 ${row.employeeName} 在 ${row.recordDate} 的打卡记录吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      try {
        await request.delete(`/api/attendance/clock-in/${row.id}`)
        ElMessage.success('已删除')
        loadRecords()
      } catch (e) { ElMessage.error('删除失败') }
    }).catch(() => {})
}

async function saveData() {
  if (!formData.employeeName || !formData.employeeNo || !formData.recordDate) {
    ElMessage.warning('请填写必填项'); return
  }
  try {
    if (dialogTitle.value === '新建打卡') {
      // 新建打卡记录走标准新增端点
      const riskMap = { normal: 'NORMAL', late: 'LATE', early: 'EARLY', missing: 'MISSING' }
      await request.post('/api/attendance/clock-in', {
        employeeId: formData.employeeNo,
        employeeName: formData.employeeName,
        department: formData.department,
        recordDate: formData.recordDate,
        checkIn: formData.checkIn || null,
        checkOut: formData.checkOut || null,
        workHours: formData.workHours,
        riskFlag: riskMap[formData.status] || 'NORMAL',
        remark: formData.remark || ''
      })
      ElMessage.success('打卡记录已新建')
    } else {
      // 补卡走补卡申请端点
      const requestNo = 'BK-' + Date.now()
      await request.post('/api/attendance/clock-in/fill-card-request', {}, {
        params: {
          requestNo, employeeId: formData.employeeNo, employeeName: formData.employeeName,
          missingDate: formData.recordDate,
          shouldCheckInTime: formData.checkIn || null, shouldCheckOutTime: formData.checkOut || null,
          reason: formData.remark || '补卡'
        }
      })
      ElMessage.success('补卡申请已提交')
    }
    dialogVisible.value = false
    loadRecords()
  } catch (e) {
    ElMessage.error('提交失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => loadRecords())
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; align-items: center; gap: 8px; }
.search-form { margin-bottom: 0; }
</style>

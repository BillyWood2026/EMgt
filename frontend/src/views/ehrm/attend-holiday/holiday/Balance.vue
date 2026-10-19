<template>
  <div class="page-card">
    <div class="page-header">
      <h2>假期余额</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>新建余额
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
      title="假期余额"
      description="按年度记录员工各类假期（年假、调休、病假、婚假等）的总额度、已用天数和剩余天数，支持年度结转与调整。"
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
        <el-form-item label="假期类型">
          <el-select v-model="searchForm.holidayType" placeholder="选择类型" style="width: 150px" clearable>
            <el-option v-for="item in holidayTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="年度">
          <el-select v-model="searchForm.year" placeholder="选择年度" style="width: 120px" clearable>
            <el-option label="2026" :value="2026" />
            <el-option label="2025" :value="2025" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="balanceList" border stripe size="small">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="employeeNo" label="工号" width="100" />
        <el-table-column label="员工姓名" width="110">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.employeeName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column label="假期类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getHolidayTypeLabel(row.holidayType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalDays" label="总额度(天)" width="110" />
        <el-table-column prop="usedDays" label="已用(天)" width="100" />
        <el-table-column label="剩余(天)" width="100">
          <template #default="{ row }">
            <el-tag :type="getRemainingType(row.totalDays - row.usedDays)" size="small">{{ (row.totalDays - row.usedDays).toFixed(1) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="year" label="生效年度" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '有效' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" @click="editItem(row)">调整</el-button>
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
              <el-input v-model="formData.employeeId" placeholder="请输入工号" />
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
            <el-form-item label="假期类型" required>
              <el-select v-model="formData.leaveTypeCode" placeholder="选择类型" style="width: 100%">
                <el-option v-for="item in holidayTypes" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="总额度(天)" required>
              <el-input-number v-model="formData.availableBalance" :min="0" :step="0.5" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="已用(天)">
              <el-input-number v-model="formData.usedBalance" :min="0" :step="0.5" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生效年度" required>
              <el-select v-model="formData.balanceYear" placeholder="选择年度" style="width: 100%">
                <el-option label="2026" :value="2026" />
                <el-option label="2025" :value="2025" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="formData.balanceStatus" placeholder="选择状态" style="width: 100%">
                <el-option label="有效" value="ACTIVE" />
                <el-option label="已过期" value="EXPIRED" />
                <el-option label="已结转" value="CARRYOVER" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="formData.remark" :rows="3" placeholder="请输入备注，如调整原因等" />
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

const departments = ['研发部', '生产部', '销售部', '财务部', '人力资源部', '行政部']

const holidayTypes = [
  { value: 'annual', label: '年假' },
  { value: 'timeoff', label: '调休' },
  { value: 'sick', label: '病假' },
  { value: 'marriage', label: '婚假' },
  { value: 'maternity', label: '产假' },
  { value: 'compassionate', label: '丧假' }
]

const STATUS_MAP = { ACTIVE: '有效', EXPIRED: '已过期', CARRYOVER: '已结转' }

const searchForm = reactive({ employeeName: '', department: '', holidayType: '', year: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('新建假期余额')
const formRef = ref(null)

const formData = reactive({
  id: '', employeeId: '', employeeName: '', leaveTypeCode: '',
  availableBalance: 0, usedBalance: 0, balanceYear: 2026, balanceStatus: 'ACTIVE', remark: ''
})

const balanceList = ref([])

function getHolidayTypeLabel(type) {
  const item = holidayTypes.find(i => i.value === type)
  return item ? item.label : type
}
function getRemainingType(remaining) {
  if (remaining <= 0) return 'danger'
  if (remaining < 2) return 'warning'
  return 'success'
}

async function loadBalance() {
  try {
    const params = { pageNum: pagination.currentPage, pageSize: pagination.pageSize }
    if (searchForm.year) params.balanceYear = searchForm.year
    const { data } = await request.get('/api/holiday/balance/list', { params })
    const records = data?.records || []
    balanceList.value = records.map(r => ({
      id: r.id, employeeNo: r.employeeId, employeeName: r.employeeName || r.employeeId,
      department: '', holidayType: r.leaveTypeCode,
      totalDays: r.availableBalance, usedDays: r.usedBalance,
      year: r.balanceYear, status: STATUS_MAP[r.balanceStatus] || '有效', remark: ''
    }))
    pagination.total = data?.total || 0
  } catch (e) {
    ElMessage.error('假期余额加载失败：' + (e.message || ''))
  }
}

function doSearch() { loadBalance() }
function resetSearch() {
  searchForm.employeeName = ''; searchForm.department = ''; searchForm.holidayType = ''; searchForm.year = ''
  loadBalance()
}
function refreshData() { loadBalance(); ElMessage.success('数据已刷新') }

function openCreate() {
  dialogTitle.value = '新建假期余额'
  Object.assign(formData, {
    id: '', employeeId: '', employeeName: '', leaveTypeCode: '',
    availableBalance: 0, usedBalance: 0, balanceYear: 2026, balanceStatus: 'ACTIVE', remark: ''
  })
  dialogVisible.value = true
}

function viewDetail(row) { ElMessage.info(`查看 ${row.employeeName} 的 ${getHolidayTypeLabel(row.holidayType)} 余额详情`) }

function editItem(row) {
  dialogTitle.value = '调整假期余额'
  Object.assign(formData, {
    id: row.id, employeeId: row.employeeNo, employeeName: row.employeeName, leaveTypeCode: row.holidayType,
    availableBalance: row.totalDays, usedBalance: row.usedDays, balanceYear: row.year, balanceStatus: 'ACTIVE', remark: ''
  })
  dialogVisible.value = true
}

async function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除 ${row.employeeName} 的 ${getHolidayTypeLabel(row.holidayType)} 余额记录吗？`, '删除确认', { type: 'warning' })
    .then(async () => { try { ElMessage.success('已删除'); loadBalance() } catch (e) { ElMessage.error('删除失败') } })
    .catch(() => {})
}

async function saveData() {
  if (!formData.employeeId || !formData.leaveTypeCode || !formData.balanceYear) {
    ElMessage.warning('请填写必填项'); return
  }
  try {
    await request.post('/api/holiday/balance/adjust', { ...formData })
    dialogVisible.value = false
    ElMessage.success('假期余额已保存')
    loadBalance()
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => loadBalance())
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.search-form { margin-bottom: 0; }
</style>

<template>
  <div class="page-card">
    <div class="page-header">
      <h2>月度薪酬自动核算</h2>
      <div class="header-actions">
        <ImportExportBar
          :columns="ioColumns"
          :rows="salaryList"
          file-name="月度薪资核算"
          title="月度薪资核算表"
        />
        <el-button @click="openAddDialog">
          <el-icon><Plus /></el-icon>新增核算记录
        </el-button>
        <el-button type="primary" @click="startCalc">
          <el-icon><Coin /></el-icon>开始核算
        </el-button>
      </div>
    </div>

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="5">
        <el-form-item label="核算月份">
          <el-date-picker v-model="calcMonth" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width:100%" />
        </el-form-item>
      </el-col>
      <el-col :span="5">
        <el-form-item label="核算状态">
          <el-select v-model="calcStatus" placeholder="选择状态" style="width:100%" clearable>
            <el-option label="待核算" value="pending" />
            <el-option label="核算中" value="calculating" />
            <el-option label="待审核" value="pending_audit" />
            <el-option label="已审核" value="audited" />
            <el-option label="已发放" value="paid" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="5">
        <el-form-item label="部门">
          <el-select v-model="calcDepartment" placeholder="选择部门" style="width:100%" clearable>
            <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="5">
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ employeeCount }}</div>
            <div class="stat-label">核算人数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-icon"><el-icon><Wallet /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ totalSalary }}</div>
            <div class="stat-label">应发总额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ totalDeduction }}</div>
            <div class="stat-label">扣除总额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ netSalary }}</div>
            <div class="stat-label">实发总额</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <el-table :data="salaryList" border stripe size="small" v-loading="loading">
        <el-table-column prop="calcNo" label="核算编号" width="130" />
        <el-table-column prop="employeeName" label="工号姓名" width="100" />
        <el-table-column prop="department" label="所属部门" width="110" />
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="payDays" label="计薪天数" width="90">
          <template #default="{row}">{{ row.payDays ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="basicSalary" label="基本工资" width="110">
          <template #default="{row}">{{ fmtNum(row.basicSalary) }}</template>
        </el-table-column>
        <el-table-column prop="performanceSalary" label="绩效工资" width="110">
          <template #default="{row}">{{ fmtNum(row.performanceSalary) }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="overtimePay" label="加班费" width="100">
          <template #default="{row}">{{ fmtNum(row.overtimePay) }}</template>
        </el-table-column>
        <el-table-column prop="allowance" label="各类补贴" width="100">
          <template #default="{row}">{{ fmtNum(row.allowance) }}</template>
        </el-table-column>
        <el-table-column prop="grossSalary" label="应发合计" width="110">
          <template #default="{row}">{{ fmtNum(row.grossSalary) }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="socialSecurity" label="社保公积金" width="110">
          <template #default="{row}">{{ fmtNum(row.socialSecurity) }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="tax" label="个税" width="90">
          <template #default="{row}">{{ fmtNum(row.tax) }}</template>
        </el-table-column>
        <el-table-column prop="deduction" label="考勤扣款" width="100">
          <template #default="{row}">{{ fmtNum(row.deduction) }}</template>
        </el-table-column>
        <el-table-column prop="netSalary" label="实发工资" width="110">
          <template #default="{row}">
            <span style="font-weight:600;color:#409eff">{{ fmtNum(row.netSalary) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="核算状态" width="100">
          <template #default="{row}">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button link size="small" type="warning" v-if="row.status === '待核算' || row.status === '核算中'" @click="changeStatus(row, '待审核')">提交审核</el-button>
            <el-button link size="small" type="success" v-if="row.status === '待审核'" @click="changeStatus(row, '已审核')">审核通过</el-button>
            <el-button link size="small" type="danger" v-if="row.status === '已审核'" @click="paySalary(row)">发放</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增核算记录弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增核算记录" width="950px">
      <el-form :model="formData" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="核算月份" required>
              <el-date-picker v-model="formData.month" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="员工姓名" required>
              <el-input v-model="formData.employeeName" placeholder="请输入员工姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="部门">
              <el-select v-model="formData.department" placeholder="选择部门" style="width:100%">
                <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="岗位">
              <el-input v-model="formData.position" placeholder="请输入岗位" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="计薪天数">
              <el-input-number v-model="formData.payDays" :min="0" :max="31" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="基本工资">
              <el-input-number v-model="formData.basicSalary" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="绩效工资">
              <el-input-number v-model="formData.performanceSalary" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="奖金">
              <el-input-number v-model="formData.bonus" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="加班费">
              <el-input-number v-model="formData.overtimePay" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="各类补贴">
              <el-input-number v-model="formData.allowance" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="社保公积金">
              <el-input-number v-model="formData.socialSecurity" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="个税">
              <el-input-number v-model="formData.tax" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="考勤扣款">
              <el-input-number v-model="formData.deduction" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label=" ">
              <span class="calc-preview">应发合计={{ calcPreview.gross }}；实发工资={{ calcPreview.net }}（应发-社保-公积金-专项扣除-个税-考勤扣款）</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 核算明细弹窗（EL-CA01 薪资核算明细：分步计算过程） -->
    <el-dialog v-model="detailVisible" title="薪资核算明细" width="700px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="核算编号">{{ currentRow.calcNo }}</el-descriptions-item>
        <el-descriptions-item label="核算月份">{{ currentRow.month }}</el-descriptions-item>
        <el-descriptions-item label="员工">{{ currentRow.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="部门/岗位">{{ currentRow.department }} / {{ currentRow.position }}</el-descriptions-item>
        <el-descriptions-item label="计薪天数">{{ currentRow.payDays ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="基本工资">{{ fmtNum(currentRow.basicSalary) }}</el-descriptions-item>
        <el-descriptions-item label="绩效工资">{{ fmtNum(currentRow.performanceSalary) }}</el-descriptions-item>
        <el-descriptions-item label="奖金">{{ fmtNum(currentRow.bonus) }}</el-descriptions-item>
        <el-descriptions-item label="加班费">{{ fmtNum(currentRow.overtimePay) }}</el-descriptions-item>
        <el-descriptions-item label="各类补贴">{{ fmtNum(currentRow.allowance) }}</el-descriptions-item>
        <el-descriptions-item label="应发合计">{{ fmtNum(currentRow.grossSalary) }}</el-descriptions-item>
        <el-descriptions-item label="社保公积金">{{ fmtNum(currentRow.socialSecurity) }}</el-descriptions-item>
        <el-descriptions-item label="专项扣除">{{ fmtNum(currentRow.specialDeduction) }}</el-descriptions-item>
        <el-descriptions-item label="个税">{{ fmtNum(currentRow.tax) }}</el-descriptions-item>
        <el-descriptions-item label="考勤扣款">{{ fmtNum(currentRow.deduction) }}</el-descriptions-item>
        <el-descriptions-item label="实发工资">{{ fmtNum(currentRow.netSalary) }}</el-descriptions-item>
        <el-descriptions-item label="核算状态">{{ currentRow.status }}</el-descriptions-item>
        <el-descriptions-item label="核算时间">{{ currentRow.calcTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-alert type="info" :closable="false" show-icon style="margin-top:12px"
        title="分步计算：应发合计=基本工资+绩效工资+奖金+加班费+补贴；实发工资=应发合计-社保个人-公积金个人-专项扣除-个税-考勤扣款" />
      <template #footer>
        <el-button @click="detailVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Wallet, Coin, CircleCheck, Plus } from '@element-plus/icons-vue'
import ImportExportBar from '@/components/ImportExportBar.vue'

const loading = ref(false)
const calcMonth = ref('')
const calcStatus = ref('')
const calcDepartment = ref('')
const dialogVisible = ref(false)
const detailVisible = ref(false)
const currentRow = ref({})

const departments = ['研发部', '生产部', '销售部', '财务部', '人力资源部', '行政部']

const salaryList = ref([])

const formData = reactive({
  month: '',
  employeeName: '',
  department: '',
  position: '',
  payDays: 21.75,
  basicSalary: 0,
  performanceSalary: 0,
  bonus: 0,
  overtimePay: 0,
  allowance: 0,
  socialSecurity: 0,
  specialDeduction: 0,
  tax: 0,
  deduction: 0
})

const ioColumns = [
  { key: 'calcNo', title: '核算编号' },
  { key: 'month', title: '核算月份' },
  { key: 'employeeName', title: '工号姓名' },
  { key: 'department', title: '所属部门' },
  { key: 'payDays', title: '计薪天数' },
  { key: 'basicSalary', title: '基本工资' },
  { key: 'performanceSalary', title: '绩效工资' },
  { key: 'overtimePay', title: '加班费' },
  { key: 'allowance', title: '各类补贴' },
  { key: 'grossSalary', title: '应发合计' },
  { key: 'socialSecurity', title: '社保公积金' },
  { key: 'tax', title: '个税' },
  { key: 'deduction', title: '考勤扣款' },
  { key: 'netSalary', title: '实发工资' },
  { key: 'status', title: '核算状态' }
]

function fmtNum(v) {
  return v != null ? Number(v).toLocaleString() : '-'
}

// 前端实时预计算：应发=基本+绩效+奖金+加班费+补贴；实发=应发-社保-个税-考勤扣款
const calcPreview = computed(() => {
  const gross = (Number(formData.basicSalary) || 0) + (Number(formData.performanceSalary) || 0) +
    (Number(formData.bonus) || 0) + (Number(formData.overtimePay) || 0) + (Number(formData.allowance) || 0)
  const net = gross - (Number(formData.socialSecurity) || 0) - (Number(formData.specialDeduction) || 0) -
    (Number(formData.tax) || 0) - (Number(formData.deduction) || 0)
  return { gross: gross.toLocaleString(), net: net.toLocaleString() }
})

async function loadData() {
  loading.value = true
  try {
    const params = { page: 1, size: 100 }
    if (calcMonth.value) params.month = calcMonth.value
    if (calcStatus.value) params.status = calcStatus.value
    if (calcDepartment.value) params.department = calcDepartment.value
    const res = await axios.get('/api/salary/monthly-calc/list', { params })
    const data = res.data
    if (data && data.code === 200) {
      const payload = data.data
      salaryList.value = Array.isArray(payload) ? payload : (payload && Array.isArray(payload.records) ? payload.records : [])
    } else if (data && Array.isArray(data.records)) {
      salaryList.value = data.records
    } else if (Array.isArray(data)) {
      salaryList.value = data
    } else {
      salaryList.value = []
    }
  } catch (e) {
    ElMessage.error('加载薪资列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

const employeeCount = computed(() => salaryList.value.length)
const totalSalary = computed(() => {
  const total = salaryList.value.reduce((sum, item) => sum + (Number(item.grossSalary) || 0), 0)
  return total.toLocaleString() + '元'
})
const totalDeduction = computed(() => {
  const total = salaryList.value.reduce((sum, item) => sum + (Number(item.deduction) || 0), 0)
  return total.toLocaleString() + '元'
})
const netSalary = computed(() => {
  const total = salaryList.value.reduce((sum, item) => sum + (Number(item.netSalary) || 0), 0)
  return total.toLocaleString() + '元'
})

function getStatusType(status) {
  const types = { '待核算': 'warning', '核算中': 'primary', '待审核': 'info', '已审核': 'success', '已发放': 'success' }
  return types[status] || 'info'
}

function handleSearch() {
  loadData()
}

// 触发月度核算（批量重算确认弹窗，规格书高危弹窗规范）
async function startCalc() {
  if (!calcMonth.value) {
    ElMessage.warning('请先选择核算月份')
    return
  }
  try {
    await ElMessageBox.confirm(
      `批量重算将基于员工薪资生成「${calcMonth.value}」月度核算记录，覆盖历史数据并留存新旧快照，是否继续？`,
      '批量重算薪资确认',
      { type: 'warning', confirmButtonText: '开始核算', cancelButtonText: '取消' }
    )
    loading.value = true
    const res = await axios.post('/api/salary/monthly-calc/calculate', { month: calcMonth.value })
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success('薪资核算已完成')
      loadData()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '核算失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('核算失败')
      console.error(e)
    }
  } finally {
    loading.value = false
  }
}

async function changeStatus(row, targetStatus) {
  try {
    await ElMessageBox.confirm(`确定将「${row.employeeName}」${row.month}薪资单状态流转为「${targetStatus}」吗？`, '状态流转确认', { type: 'warning' })
    const res = await axios.put(`/api/salary/monthly-calc/${row.id}`, { ...row, status: targetStatus })
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success(`已流转为「${targetStatus}」`)
      loadData()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
      console.error(e)
    }
  }
}

// 薪资发放（高危操作红色二次确认）
async function paySalary(row) {
  try {
    await ElMessageBox.confirm(
      `确定发放「${row.employeeName}」${row.month}薪资吗？实发金额 ${fmtNum(row.netSalary)} 元，发放后进入归档只读状态。`,
      '薪资发放确认',
      { type: 'error', confirmButtonText: '确认发放', cancelButtonText: '取消' }
    )
    const res = await axios.put(`/api/salary/monthly-calc/${row.id}`, { ...row, status: '已发放' })
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success('薪资已发放')
      loadData()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '发放失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('发放失败')
      console.error(e)
    }
  }
}

function openAddDialog() {
  Object.assign(formData, {
    month: calcMonth.value || '', employeeName: '', department: '', position: '',
    payDays: 21.75, basicSalary: 0, performanceSalary: 0, bonus: 0, overtimePay: 0,
    allowance: 0, socialSecurity: 0, specialDeduction: 0, tax: 0, deduction: 0
  })
  dialogVisible.value = true
}

function viewDetail(row) {
  currentRow.value = row
  detailVisible.value = true
}

async function handleSubmit() {
  if (!formData.month || !formData.employeeName) {
    ElMessage.warning('请填写核算月份与员工姓名')
    return
  }
  try {
    const res = await axios.post('/api/salary/monthly-calc', { ...formData, status: '待核算' })
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success('核算记录新增成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; align-items: center; gap: 8px; }
.calc-preview { color: #409eff; font-size: 13px; }

.stat-card { display: flex; align-items: center; gap: 16px; padding: 20px; }
.stat-icon { width: 56px; height: 56px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28px; background: #e3f2fd; color: #409eff; }
.stat-card.success .stat-icon { background: #e8f5e9; color: #67c23a; }
.stat-card.warning .stat-icon { background: #fef7e8; color: #e6a23c; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { color: #909399; font-size: 14px; margin-top: 4px; }
</style>

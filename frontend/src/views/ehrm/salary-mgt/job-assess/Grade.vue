<template>
  <div class="page-card">
    <div class="page-header">
      <h2>员工个人定薪管理</h2>
      <div class="header-actions">
        <ImportExportBar
          :columns="ioColumns"
          :rows="gradeList"
          file-name="员工定薪"
          title="员工个人定薪列表"
        />
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新增定薪
        </el-button>
      </div>
    </div>

    <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px"
      title="定薪规则：普通员工定薪强制锁定在对应职级薪酬带宽内，超带宽需总经理+人力总监双审批并全程快照留痕；试用期薪资低于法定标准强制阻断" />

    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="员工姓名">
          <el-input v-model="searchForm.employeeName" placeholder="请输入员工姓名" style="width:180px" clearable />
        </el-form-item>
        <el-form-item label="职级">
          <el-input v-model="searchForm.jobLevel" placeholder="请输入职级" style="width:150px" clearable />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="searchForm.department" placeholder="请输入部门" style="width:150px" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="gradeList" border stripe size="small" v-loading="loading">
        <el-table-column prop="employeeNo" label="工号" width="110" />
        <el-table-column prop="employeeName" label="姓名" width="90" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="position" label="岗位" width="120" />
        <el-table-column prop="jobLevel" label="岗位职级" width="90">
          <template #default="{row}">
            <el-tag v-if="row.jobLevel" size="small">{{ row.jobLevel }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="bandRange" label="薪酬带宽区间" width="140">
          <template #default="{row}">{{ row.bandRange || '-' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="settleType" label="定薪类型" width="100">
          <template #default="{row}">
            <el-tag v-if="row.settleType" size="small" type="warning">{{ row.settleType }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="basicSalary" label="基本工资" width="110">
          <template #default="{row}">{{ row.basicSalary != null ? Number(row.basicSalary).toLocaleString() : '-' }}</template>
        </el-table-column>
        <el-table-column prop="performanceSalary" label="绩效基数" width="110">
          <template #default="{row}">{{ row.performanceSalary != null ? Number(row.performanceSalary).toLocaleString() : '-' }}</template>
        </el-table-column>
        <el-table-column prop="allowance" label="各类补贴" width="100">
          <template #default="{row}">{{ row.allowance != null ? Number(row.allowance).toLocaleString() : '-' }}</template>
        </el-table-column>
        <el-table-column prop="totalSalary" label="定薪总额" width="110">
          <template #default="{row}">
            <span v-if="row.totalSalary != null" style="font-weight:600;color:#409eff">{{ Number(row.totalSalary).toLocaleString() }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveDate" label="定薪生效月" width="110" />
        <el-table-column prop="status" label="审批状态" width="90">
          <template #default="{row}">
            <el-tag v-if="row.status" :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button link size="small" @click="editRow(row)">编辑</el-button>
            <el-button link size="small" type="success" v-if="row.status !== '生效'" @click="changeStatus(row, '生效')">启用</el-button>
            <el-button link size="small" type="danger" v-if="row.status === '生效'" @click="changeStatus(row, '已停用')">停用</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; text-align: right"
      />
    </el-card>

    <!-- 定薪弹窗（规格书950px） -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="950px">
      <el-form :model="formData" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="工号" required>
              <el-input v-model="formData.employeeNo" placeholder="请输入工号" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="姓名" required>
              <el-input v-model="formData.employeeName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="部门">
              <el-input v-model="formData.department" placeholder="请输入部门" />
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
            <el-form-item label="岗位职级">
              <el-input v-model="formData.jobLevel" placeholder="例如：L4" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="定薪类型">
              <el-select v-model="formData.settleType" placeholder="选择定薪类型">
                <el-option label="入职定薪" value="入职定薪" />
                <el-option label="转正定薪" value="转正定薪" />
                <el-option label="定岗定薪" value="定岗定薪" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="基本工资">
              <el-input-number v-model="formData.basicSalary" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="绩效基数">
              <el-input-number v-model="formData.performanceSalary" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="各类补贴">
              <el-input-number v-model="formData.allowance" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="薪酬带宽区间">
              <el-input v-model="formData.bandRange" placeholder="例如：8000-15000" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="定薪生效月">
              <el-date-picker v-model="formData.effectiveDate" type="month" value-format="YYYY-MM" placeholder="选择生效月" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label=" ">
              <span class="total-preview">定薪总额={{ totalPreview }} 元</span>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 带宽溢出黄色预警 -->
        <el-alert v-if="bandOverflow" type="warning" :closable="false" show-icon
          title="定薪超职级带宽预警" :description="bandOverflow" style="margin-bottom:12px" />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="定薪详情" width="700px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="工号">{{ currentRow.employeeNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentRow.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ currentRow.department }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ currentRow.position }}</el-descriptions-item>
        <el-descriptions-item label="岗位职级">{{ currentRow.jobLevel }}</el-descriptions-item>
        <el-descriptions-item label="定薪类型">{{ currentRow.settleType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="薪酬带宽区间">{{ currentRow.bandRange || '-' }}</el-descriptions-item>
        <el-descriptions-item label="定薪生效月">{{ currentRow.effectiveDate }}</el-descriptions-item>
        <el-descriptions-item label="基本工资">{{ fmtNum(currentRow.basicSalary) }}</el-descriptions-item>
        <el-descriptions-item label="绩效基数">{{ fmtNum(currentRow.performanceSalary) }}</el-descriptions-item>
        <el-descriptions-item label="各类补贴">{{ fmtNum(currentRow.allowance) }}</el-descriptions-item>
        <el-descriptions-item label="定薪总额">{{ fmtNum(currentRow.totalSalary) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentRow.status }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentRow.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
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
import { Plus } from '@element-plus/icons-vue'
import ImportExportBar from '@/components/ImportExportBar.vue'

const loading = ref(false)
const gradeList = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增定薪')
const currentRow = ref({})

const searchForm = reactive({
  employeeName: '',
  jobLevel: '',
  department: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null,
  employeeNo: '',
  employeeName: '',
  department: '',
  position: '',
  jobLevel: '',
  settleType: '',
  bandRange: '',
  basicSalary: 0,
  performanceSalary: 0,
  allowance: 0,
  totalSalary: 0,
  effectiveDate: ''
})

const ioColumns = [
  { key: 'employeeNo', title: '工号' },
  { key: 'employeeName', title: '姓名' },
  { key: 'department', title: '部门' },
  { key: 'position', title: '岗位' },
  { key: 'jobLevel', title: '岗位职级' },
  { key: 'bandRange', title: '薪酬带宽区间' },
  { key: 'settleType', title: '定薪类型' },
  { key: 'basicSalary', title: '基本工资' },
  { key: 'performanceSalary', title: '绩效基数' },
  { key: 'allowance', title: '各类补贴' },
  { key: 'totalSalary', title: '定薪总额' },
  { key: 'effectiveDate', title: '定薪生效月' },
  { key: 'status', title: '审批状态' }
]

function fmtNum(v) {
  return v != null ? Number(v).toLocaleString() : '-'
}

const totalPreview = computed(() => {
  const total = (Number(formData.basicSalary) || 0) + (Number(formData.performanceSalary) || 0) + (Number(formData.allowance) || 0)
  return total.toLocaleString()
})

// 带宽溢出校验（规格书：超带宽黄色预警）
const bandOverflow = computed(() => {
  if (!formData.bandRange) return ''
  const m = String(formData.bandRange).match(/(\d+)\s*[-~—]\s*(\d+)/)
  if (!m) return ''
  const min = Number(m[1]), max = Number(m[2])
  const total = (Number(formData.basicSalary) || 0) + (Number(formData.performanceSalary) || 0) + (Number(formData.allowance) || 0)
  if (total > max) return `定薪总额 ${total.toLocaleString()} 元超出带宽上限 ${max.toLocaleString()} 元，需总经理+人力总监双审批`
  if (total < min) return `定薪总额 ${total.toLocaleString()} 元低于带宽下限 ${min.toLocaleString()} 元，请核查`
  return ''
})

async function loadData() {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (searchForm.employeeName) params.employeeName = searchForm.employeeName
    if (searchForm.jobLevel) params.jobLevel = searchForm.jobLevel
    if (searchForm.department) params.department = searchForm.department
    const res = await axios.get('/api/salary/employee-salary/list', { params })
    const data = res.data
    let list = []
    let total = 0
    if (data && data.code === 200) {
      const payload = data.data
      if (Array.isArray(payload)) {
        list = payload
        total = payload.length
      } else if (payload && Array.isArray(payload.records)) {
        list = payload.records
        total = payload.total != null ? payload.total : payload.records.length
      }
    } else if (data && Array.isArray(data.records)) {
      list = data.records
      total = data.total != null ? data.total : data.records.length
    } else if (Array.isArray(data)) {
      list = data
      total = data.length
    }
    gradeList.value = list
    pagination.total = total
  } catch (e) {
    ElMessage.error('加载员工定薪失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function getStatusType(status) {
  const types = { '生效': 'success', '未生效': 'warning', '已停用': 'info' }
  return types[status] || 'info'
}

function handleSearch() {
  pagination.page = 1
  loadData()
}

function resetSearch() {
  searchForm.employeeName = ''
  searchForm.jobLevel = ''
  searchForm.department = ''
  pagination.page = 1
  loadData()
}

function handleSizeChange(val) {
  pagination.size = val
  pagination.page = 1
  loadData()
}

function handleCurrentChange(val) {
  pagination.page = val
  loadData()
}

function openAddDialog() {
  dialogTitle.value = '新增定薪'
  Object.assign(formData, {
    id: null, employeeNo: '', employeeName: '', department: '', position: '', jobLevel: '',
    settleType: '', bandRange: '', basicSalary: 0, performanceSalary: 0, allowance: 0,
    totalSalary: 0, effectiveDate: ''
  })
  dialogVisible.value = true
}

function editRow(row) {
  dialogTitle.value = '编辑定薪'
  Object.assign(formData, {
    id: row.id, employeeNo: row.employeeNo, employeeName: row.employeeName,
    department: row.department, position: row.position, jobLevel: row.jobLevel,
    settleType: row.settleType || '', bandRange: row.bandRange || '',
    basicSalary: row.basicSalary ?? 0, performanceSalary: row.performanceSalary ?? 0,
    allowance: row.allowance ?? 0, totalSalary: row.totalSalary ?? 0,
    effectiveDate: row.effectiveDate
  })
  dialogVisible.value = true
}

function viewDetail(row) {
  currentRow.value = row
  detailVisible.value = true
}

async function changeStatus(row, status) {
  try {
    const res = await axios.put(`/api/salary/employee-salary/${row.id}`, { ...row, status })
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success(`已${status === '生效' ? '启用' : '停用'}`)
      loadData()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

async function handleSubmit() {
  if (!formData.employeeNo || !formData.employeeName) {
    ElMessage.warning('请填写工号与姓名')
    return
  }
  formData.totalSalary = (Number(formData.basicSalary) || 0) + (Number(formData.performanceSalary) || 0) + (Number(formData.allowance) || 0)
  try {
    let res
    if (formData.id) {
      res = await axios.put(`/api/salary/employee-salary/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/salary/employee-salary', { ...formData, status: '未生效' })
    }
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success(formData.id ? '定薪已更新' : '定薪新增成功')
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
.search-form { margin-bottom: 0; }
.total-preview { color: #409eff; font-weight: 600; }
</style>

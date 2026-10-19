<template>
  <div class="page-card">
    <div class="page-header">
      <h2>薪酬核算历史档案</h2>
      <div class="header-actions">
        <ImportExportBar
          :columns="ioColumns"
          :rows="historyList"
          file-name="薪酬核算历史"
          title="薪酬核算历史档案"
        />
      </div>
    </div>

    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="核算月份">
          <el-date-picker v-model="searchForm.month" type="month" placeholder="选择月份" style="width:160px" value-format="YYYY-MM" />
        </el-form-item>
        <el-form-item label="核算状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" style="width:150px" clearable>
            <el-option label="待核算" value="pending" />
            <el-option label="核算中" value="calculating" />
            <el-option label="待审核" value="pending_audit" />
            <el-option label="已审核" value="audited" />
            <el-option label="已发放" value="paid" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="historyList" border stripe size="small" v-loading="loading">
        <el-table-column prop="calcNo" label="核算编号" width="130" />
        <el-table-column prop="month" label="核算月份" width="100" />
        <el-table-column prop="employeeName" label="工号姓名" width="100" />
        <el-table-column prop="department" label="部门" width="110" />
        <el-table-column prop="position" label="岗位" width="110" />
        <el-table-column prop="grossSalary" label="应发金额" width="110">
          <template #default="{row}">{{ row.grossSalary != null ? Number(row.grossSalary).toLocaleString() : '-' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="socialSecurity" label="累计社保" width="100">
          <template #default="{row}">{{ row.socialSecurity != null ? Number(row.socialSecurity).toLocaleString() : '-' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="tax" label="累计个税" width="100">
          <template #default="{row}">{{ row.tax != null ? Number(row.tax).toLocaleString() : '-' }}</template>
        </el-table-column>
        <el-table-column prop="deduction" label="扣除金额" width="100">
          <template #default="{row}">{{ row.deduction != null ? Number(row.deduction).toLocaleString() : '-' }}</template>
        </el-table-column>
        <el-table-column prop="netSalary" label="实发金额" width="110">
          <template #default="{row}">
            <span v-if="row.netSalary != null" style="font-weight:600;color:#409eff">{{ Number(row.netSalary).toLocaleString() }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="calcTime" label="核算时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">详情</el-button>
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

    <!-- EL-AR01 月度薪酬明细档案弹窗 -->
    <el-dialog v-model="detailVisible" title="月度薪酬明细档案" width="700px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="核算编号">{{ currentRow.calcNo }}</el-descriptions-item>
        <el-descriptions-item label="核算月份">{{ currentRow.month }}</el-descriptions-item>
        <el-descriptions-item label="员工">{{ currentRow.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="部门/岗位">{{ currentRow.department }} / {{ currentRow.position }}</el-descriptions-item>
        <el-descriptions-item label="基本工资">{{ fmtNum(currentRow.basicSalary) }}</el-descriptions-item>
        <el-descriptions-item label="绩效工资">{{ fmtNum(currentRow.performanceSalary) }}</el-descriptions-item>
        <el-descriptions-item label="奖金">{{ fmtNum(currentRow.bonus) }}</el-descriptions-item>
        <el-descriptions-item label="津贴补贴">{{ fmtNum(currentRow.allowance) }}</el-descriptions-item>
        <el-descriptions-item label="应发合计">{{ fmtNum(currentRow.grossSalary) }}</el-descriptions-item>
        <el-descriptions-item label="扣除合计">{{ fmtNum(currentRow.deduction) }}</el-descriptions-item>
        <el-descriptions-item label="实发工资">{{ fmtNum(currentRow.netSalary) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentRow.status }}</el-descriptions-item>
        <el-descriptions-item label="核算时间">{{ currentRow.calcTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="档案更新时间">{{ currentRow.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import ImportExportBar from '@/components/ImportExportBar.vue'

const loading = ref(false)
const historyList = ref([])
const detailVisible = ref(false)
const currentRow = ref({})

const searchForm = reactive({
  month: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const ioColumns = [
  { key: 'calcNo', title: '核算编号' },
  { key: 'month', title: '核算月份' },
  { key: 'employeeName', title: '工号姓名' },
  { key: 'department', title: '部门' },
  { key: 'position', title: '岗位' },
  { key: 'grossSalary', title: '应发金额' },
  { key: 'socialSecurity', title: '累计社保' },
  { key: 'tax', title: '累计个税' },
  { key: 'deduction', title: '扣除金额' },
  { key: 'netSalary', title: '实发金额' },
  { key: 'status', title: '状态' },
  { key: 'calcTime', title: '核算时间' }
]

function fmtNum(v) {
  return v != null ? Number(v).toLocaleString() : '-'
}

async function loadData() {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (searchForm.month) params.month = searchForm.month
    if (searchForm.status) params.status = searchForm.status
    const res = await axios.get('/api/salary/monthly-calc/list', { params })
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
    historyList.value = list
    pagination.total = total
  } catch (e) {
    ElMessage.error('加载计算历史失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function getStatusType(status) {
  const types = { '待核算': 'warning', '核算中': 'primary', '待审核': 'info', '已审核': 'success', '已发放': 'success' }
  return types[status] || 'info'
}

function handleSearch() {
  pagination.page = 1
  loadData()
}

function resetSearch() {
  searchForm.month = ''
  searchForm.status = ''
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

function viewDetail(row) {
  currentRow.value = row
  detailVisible.value = true
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
</style>

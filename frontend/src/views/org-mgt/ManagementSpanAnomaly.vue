<template>
  <div class="page-container">
    <div class="page-header">
      <h2>管理幅度异常</h2>
      <div class="header-actions">
        <el-button @click="refresh">刷新</el-button>
        <el-button type="primary" @click="batchResolve">批量处理</el-button>
      </div>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="异常类型">
        <el-select v-model="searchForm.anomalyType" placeholder="选择类型" style="width: 150px">
          <el-option label="职数超配" value="QUOTA_EXCEED" />
          <el-option label="幅度过低" value="AMPLITUDE_LOW" />
          <el-option label="幅度过载" value="AMPLITUDE_HIGH" />
          <el-option label="多正职" value="MULTIPLE_CHIEFS" />
        </el-select>
      </el-form-item>
      <el-form-item label="预警级别">
        <el-select v-model="searchForm.warningLevel" placeholder="选择级别" style="width: 120px">
          <el-option label="绿色" value="GREEN" />
          <el-option label="黄色" value="YELLOW" />
          <el-option label="红色" value="RED" />
        </el-select>
      </el-form-item>
      <el-form-item label="处理状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 120px">
          <el-option label="待处理" value="PENDING" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="已解决" value="RESOLVED" />
          <el-option label="已豁免" value="EXEMPTED" />
        </el-select>
      </el-form-item>
      <el-form-item label="岗位层级">
        <el-select v-model="searchForm.positionLevel" placeholder="选择层级" style="width: 120px">
          <el-option label="一级" value="L1" />
          <el-option label="二级" value="L2" />
          <el-option label="三级" value="L3" />
          <el-option label="四级" value="L4" />
          <el-option label="五级" value="L5" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" border stripe size="small" v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="orgName" label="组织名称" width="140" />
      <el-table-column prop="positionName" label="岗位名称" width="140" />
      <el-table-column prop="positionLevel" label="层级" width="80" />
      <el-table-column prop="anomalyType" label="异常类型" width="120">
        <template #default="{ row }">
          {{ getAnomalyTypeLabel(row.anomalyType) }}
        </template>
      </el-table-column>
      <el-table-column prop="warningLevel" label="预警级别" width="100">
        <template #default="{ row }">
          <el-tag :type="getWarningLevelType(row.warningLevel)" size="small">
            {{ getWarningLevelLabel(row.warningLevel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="处理状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="currentQuota" label="当前职数" width="100" />
      <el-table-column prop="standardQuota" label="标准职数" width="100" />
      <el-table-column prop="maxQuota" label="最大职数" width="100" />
      <el-table-column prop="subordinateCount" label="下属数" width="80" />
      <el-table-column prop="standardAmplitude" label="标准幅度" width="100" />
      <el-table-column prop="occurrenceTime" label="发生时间" width="160" />
      <el-table-column prop="deadline" label="整改期限" width="160">
        <template #default="{ row }">
          <span :style="{ color: isOverdue(row) ? '#f56c6c' : 'inherit', fontWeight: isOverdue(row) ? 'bold' : 'normal' }">
            {{ row.deadline }}{{ isOverdue(row) ? '（已超期）' : '' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" v-if="row.status === 'PENDING'" @click="handleResolve(row)">
            处理
          </el-button>
          <el-button size="small" type="success" v-if="row.status === 'PENDING'" @click="handleExempt(row)">
            豁免
          </el-button>
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
      style="margin-top: 20px; text-align: right"
    />

    <el-dialog v-model="resolveVisible" title="处理异常" width="600px">
      <el-form :model="resolveForm" label-width="100px">
        <el-form-item label="处理方式">
          <el-radio-group v-model="resolveForm.resolveType">
            <el-radio label="ADJUST">调整编制</el-radio>
            <el-radio label="MERGE">合并岗位</el-radio>
            <el-radio label="OTHER">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="整改说明" required>
          <el-input v-model="resolveForm.rectificationDesc" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="整改期限">
          <el-date-picker
            v-model="resolveForm.deadline"
            type="datetime"
            placeholder="选择日期时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResolve">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="异常详情" width="700px">
      <el-descriptions v-if="currentAnomaly" :column="2" border>
        <el-descriptions-item label="组织名称">{{ currentAnomaly.orgName }}</el-descriptions-item>
        <el-descriptions-item label="岗位名称">{{ currentAnomaly.positionName }}</el-descriptions-item>
        <el-descriptions-item label="岗位层级">{{ currentAnomaly.positionLevel }}</el-descriptions-item>
        <el-descriptions-item label="异常类型">{{ getAnomalyTypeLabel(currentAnomaly.anomalyType) }}</el-descriptions-item>
        <el-descriptions-item label="预警级别">
          <el-tag :type="getWarningLevelType(currentAnomaly.warningLevel)" size="small">
            {{ getWarningLevelLabel(currentAnomaly.warningLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="getStatusType(currentAnomaly.status)" size="small">
            {{ getStatusLabel(currentAnomaly.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前职数">{{ currentAnomaly.currentQuota }}</el-descriptions-item>
        <el-descriptions-item label="标准职数">{{ currentAnomaly.standardQuota }}</el-descriptions-item>
        <el-descriptions-item label="最大职数">{{ currentAnomaly.maxQuota }}</el-descriptions-item>
        <el-descriptions-item label="下属数">{{ currentAnomaly.subordinateCount }}</el-descriptions-item>
        <el-descriptions-item label="最小幅度">{{ currentAnomaly.minAmplitude }}</el-descriptions-item>
        <el-descriptions-item label="标准幅度">{{ currentAnomaly.standardAmplitude }}</el-descriptions-item>
        <el-descriptions-item label="最大幅度">{{ currentAnomaly.maxAmplitude }}</el-descriptions-item>
        <el-descriptions-item label="发生时间">{{ currentAnomaly.occurrenceTime }}</el-descriptions-item>
        <el-descriptions-item label="整改期限">{{ currentAnomaly.deadline }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ currentAnomaly.createdBy }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentAnomaly.createTime }}</el-descriptions-item>
        <el-descriptions-item label="整改说明" :span="2">{{ currentAnomaly.rectificationDesc }}</el-descriptions-item>
        <el-descriptions-item label="解决时间">{{ currentAnomaly.resolveTime }}</el-descriptions-item>
        <el-descriptions-item label="豁免审批单号">{{ currentAnomaly.exemptionApprovalNo }}</el-descriptions-item>
        <el-descriptions-item label="豁免有效期至">{{ currentAnomaly.exemptionExpiryDate }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentAnomaly.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const resolveVisible = ref(false)
const detailVisible = ref(false)
const selectedRows = ref([])
const currentAnomaly = ref(null)

const searchForm = reactive({
  anomalyType: '',
  warningLevel: '',
  status: '',
  positionLevel: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const resolveForm = reactive({
  id: null,
  resolveType: 'ADJUST',
  rectificationDesc: '',
  deadline: ''
})

const list = ref([])

function isOverdue(row) {
  if (!row.deadline) return false
  if (row.status === 'RESOLVED' || row.status === 'EXEMPTED') return false
  const d = new Date(String(row.deadline).replace(' ', 'T'))
  if (isNaN(d.getTime())) return false
  return d.getTime() < Date.now()
}

function getAnomalyTypeLabel(type) {
  const map = {
    QUOTA_EXCEED: '职数超配',
    AMPLITUDE_LOW: '幅度过低',
    AMPLITUDE_HIGH: '幅度过载',
    MULTIPLE_CHIEFS: '多正职'
  }
  return map[type] || type
}

function getWarningLevelType(level) {
  const map = {
    GREEN: 'success',
    YELLOW: 'warning',
    RED: 'danger'
  }
  return map[level] || 'info'
}

function getWarningLevelLabel(level) {
  const map = {
    GREEN: '绿色',
    YELLOW: '黄色',
    RED: '红色'
  }
  return map[level] || level
}

function getStatusType(status) {
  const map = {
    PENDING: 'warning',
    PROCESSING: 'primary',
    RESOLVED: 'success',
    EXEMPTED: 'info'
  }
  return map[status] || 'info'
}

function getStatusLabel(status) {
  const map = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    RESOLVED: '已解决',
    EXEMPTED: '已豁免'
  }
  return map[status] || status
}

async function loadList() {
  loading.value = true
  try {
    const res = await axios.get('/api/management-span-anomaly/list', {
      params: {
        page: pagination.currentPage,
        pageSize: pagination.pageSize,
        anomalyType: searchForm.anomalyType || undefined,
        warningLevel: searchForm.warningLevel || undefined,
        status: searchForm.status || undefined,
        positionLevel: searchForm.positionLevel || undefined
      }
    })
    if (res.data.code === 200) {
      list.value = res.data.data.list || res.data.data || []
      pagination.total = res.data.data.total || list.value.length
    }
  } catch (e) {
    ElMessage.error('加载列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.currentPage = 1
  loadList()
}

function resetSearch() {
  searchForm.anomalyType = ''
  searchForm.warningLevel = ''
  searchForm.status = ''
  searchForm.positionLevel = ''
  pagination.currentPage = 1
  loadList()
}

function handleSizeChange(val) {
  pagination.pageSize = val
  loadList()
}

function handleCurrentChange(val) {
  pagination.currentPage = val
  loadList()
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function viewDetail(row) {
  currentAnomaly.value = row
  detailVisible.value = true
}

function handleResolve(row) {
  resolveForm.id = row.id
  resolveForm.resolveType = 'ADJUST'
  resolveForm.rectificationDesc = ''
  resolveForm.deadline = ''
  resolveVisible.value = true
}

async function submitResolve() {
  if (!resolveForm.rectificationDesc) {
    ElMessage.warning('请填写整改说明')
    return
  }
  try {
    const res = await axios.post(`/api/management-span-anomaly/${resolveForm.id}/resolve`, resolveForm)
    if (res.data.code === 200) {
      ElMessage.success('处理成功')
      resolveVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '处理失败')
    }
  } catch (e) {
    ElMessage.error('处理失败')
  }
}

function handleExempt(row) {
  ElMessageBox.prompt('请输入豁免原因', '豁免确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'textarea'
  }).then(async ({ value }) => {
    try {
      const res = await axios.post(`/api/management-span-anomaly/${row.id}/exempt`, {
        reason: value
      })
      if (res.data.code === 200) {
        ElMessage.success('豁免成功')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '豁免失败')
      }
    } catch (e) {
      ElMessage.error('豁免失败')
    }
  }).catch(() => {})
}

function batchResolve() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要处理的记录')
    return
  }
  ElMessageBox.confirm(`确定要批量处理选中的 ${selectedRows.value.length} 条记录吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const ids = selectedRows.value.map((item) => item.id)
      const res = await axios.post('/api/management-span-anomaly/batch-resolve', { ids })
      if (res.data.code === 200) {
        ElMessage.success('批量处理成功')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '批量处理失败')
      }
    } catch (e) {
      ElMessage.error('批量处理失败')
    }
  })
}

function refresh() {
  loadList()
}

onMounted(() => {
  loadList()
})
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

.search-form {
  margin-bottom: 20px;
}
</style>

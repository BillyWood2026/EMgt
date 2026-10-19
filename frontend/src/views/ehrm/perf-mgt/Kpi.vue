<template>
  <div class="page-card">
    <div class="page-header">
      <h2>KPI指标管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新增指标
        </el-button>
      </div>
    </div>

    <el-alert type="info" :closable="false" style="margin-bottom:12px"
      :title="`考核维度：财务/客户/内部流程/学习成长（BSC）；同一考核对象指标权重合计须=100%（当前全部指标权重合计 ${totalWeight}%）；评分梯度：保底值以下0分、保底~目标线性、目标~挑战加速。`"
      :type="totalWeight === 100 ? 'success' : 'info'" />

    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="指标名称">
          <el-input v-model="searchForm.name" placeholder="请输入指标名称" style="width:200px" />
        </el-form-item>
        <el-form-item label="所属维度">
          <el-select v-model="searchForm.dimension" placeholder="选择维度" style="width:150px">
            <el-option label="财务维度" value="finance" />
            <el-option label="客户维度" value="customer" />
            <el-option label="内部流程" value="internal" />
            <el-option label="学习成长" value="learning" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <ImportExportBar :columns="ioColumns" :rows="kpiList" file-name="KPI指标库" title="KPI指标导入导出" style="margin-bottom:12px" @done="loadData" />

    <el-card shadow="never">
      <el-table :data="kpiList" border stripe size="small" v-loading="loading">
        <el-table-column prop="kpiNo" label="指标编号" width="110" />
        <el-table-column prop="name" label="指标名称" min-width="160">
          <template #default="{row}">
            <el-link type="primary" @click="viewDetail(row)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="dimension" label="所属维度" width="110">
          <template #default="{row}">
            <el-tag :type="getDimensionType(row.dimension)" size="small">{{ getDimensionLabel(row.dimension) }}</el-tag>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="floorValue" label="保底值" width="90">
          <template #default="{row}">{{ row.floorValue || '—' }}</template>
        </el-table-column>
        <el-table-column prop="targetValue" label="目标值" width="90" />
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="challengeValue" label="挑战值" width="90">
          <template #default="{row}">{{ row.challengeValue || '—' }}</template>
        </el-table-column>
        <el-table-column prop="weight" label="权重" width="80">
          <template #default="{row}">{{ row.weight }}%</template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="70" />
        <el-table-column prop="frequency" label="考核频率" width="90">
          <template #default="{row}">
            <el-tag size="small">{{ row.frequency }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}">
            <el-tag :type="row.status === '启用' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" @click="editKpi(row)">编辑</el-button>
            <el-button v-if="row.status !== '启用'" link size="small" type="success" @click="toggleStatus(row, '启用')">启用</el-button>
            <el-button v-else link size="small" type="warning" @click="toggleStatus(row, '停用')">停用</el-button>
            <el-button link size="small" type="danger" @click="deleteKpi(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="780px">
      <el-form :model="formData" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="指标名称" required>
              <el-input v-model="formData.name" placeholder="请输入指标名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="指标编号">
              <el-input v-model="formData.kpiNo" placeholder="自动生成" readonly />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属维度" required>
              <el-select v-model="formData.dimension" placeholder="选择维度">
                <el-option label="财务维度" value="finance" />
                <el-option label="客户维度" value="customer" />
                <el-option label="内部流程" value="internal" />
                <el-option label="学习成长" value="learning" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考核频率">
              <el-select v-model="formData.frequency" placeholder="选择频率">
                <el-option label="月度" value="月度" />
                <el-option label="季度" value="季度" />
                <el-option label="年度" value="年度" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <!-- 规格书字段，待后端扩展 -->
          <el-col :span="6">
            <el-form-item label="保底值">
              <el-input v-model="formData.floorValue" placeholder="保底值" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="目标值">
              <el-input v-model="formData.targetValue" placeholder="请输入目标值" />
            </el-form-item>
          </el-col>
          <!-- 规格书字段，待后端扩展 -->
          <el-col :span="6">
            <el-form-item label="挑战值">
              <el-input v-model="formData.challengeValue" placeholder="挑战值" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="单位">
              <el-input v-model="formData.unit" placeholder="%、元" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权重%">
              <el-input-number v-model="formData.weight" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <!-- 规格书字段，待后端扩展 -->
          <el-col :span="12">
            <el-form-item label="数据来源">
              <el-input v-model="formData.dataSource" placeholder="如 ERP/财务系统/人工填报" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="评分梯度">
          <el-input v-model="formData.scoringGradient" placeholder="如 <保底0分；保底~目标线性60-100分；≥挑战120分" />
        </el-form-item>
        <el-form-item label="指标定义">
          <el-input type="textarea" v-model="formData.definition" :rows="2" placeholder="请输入指标定义" />
        </el-form-item>
        <el-form-item label="计算方法">
          <el-input type="textarea" v-model="formData.calculation" :rows="2" placeholder="请输入计算方法" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="指标详情" width="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="指标编号">{{ detailRow.kpiNo }}</el-descriptions-item>
        <el-descriptions-item label="指标名称">{{ detailRow.name }}</el-descriptions-item>
        <el-descriptions-item label="所属维度">{{ getDimensionLabel(detailRow.dimension) }}</el-descriptions-item>
        <el-descriptions-item label="考核频率">{{ detailRow.frequency }}</el-descriptions-item>
        <el-descriptions-item label="保底值">{{ detailRow.floorValue || '—' }}</el-descriptions-item>
        <el-descriptions-item label="目标值">{{ detailRow.targetValue }}</el-descriptions-item>
        <el-descriptions-item label="挑战值">{{ detailRow.challengeValue || '—' }}</el-descriptions-item>
        <el-descriptions-item label="权重">{{ detailRow.weight }}%</el-descriptions-item>
        <el-descriptions-item label="单位">{{ detailRow.unit }}</el-descriptions-item>
        <el-descriptions-item label="数据来源">{{ detailRow.dataSource || '—' }}</el-descriptions-item>
        <el-descriptions-item label="评分梯度" :span="2">{{ detailRow.scoringGradient || '—' }}</el-descriptions-item>
        <el-descriptions-item label="指标定义" :span="2">{{ detailRow.definition }}</el-descriptions-item>
        <el-descriptions-item label="计算方法" :span="2">{{ detailRow.calculation }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailRow.status === '启用' ? 'success' : 'info'" size="small">{{ detailRow.status }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
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
const searchForm = reactive({ name: '', dimension: '' })

const ioColumns = [
  { key: 'kpiNo', title: '指标编号' },
  { key: 'name', title: '指标名称' },
  { key: 'dimension', title: '所属维度' },
  { key: 'floorValue', title: '保底值' },
  { key: 'targetValue', title: '目标值' },
  { key: 'challengeValue', title: '挑战值' },
  { key: 'weight', title: '权重%' },
  { key: 'unit', title: '单位' },
  { key: 'frequency', title: '考核频率' },
  { key: 'dataSource', title: '数据来源' },
  { key: 'scoringGradient', title: '评分梯度' },
  { key: 'status', title: '状态' }
]

const dialogVisible = ref(false)
const dialogTitle = ref('新增KPI指标')
const detailVisible = ref(false)
const detailRow = ref({})

const formData = reactive({
  id: null, kpiNo: '', name: '', dimension: '', floorValue: '', targetValue: '', challengeValue: '',
  weight: 0, unit: '', frequency: '月度', dataSource: '', scoringGradient: '', definition: '', calculation: '', status: '启用'
})

const kpiList = ref([])

const totalWeight = computed(() => kpiList.value.reduce((a, r) => a + (Number(r.weight) || 0), 0))

async function loadData() {
  loading.value = true
  try {
    const params = { page: 1, size: 100 }
    if (searchForm.name) params.name = searchForm.name
    if (searchForm.dimension) params.dimension = searchForm.dimension
    const res = await axios.get('/api/perf/template/list', { params })
    const data = res.data
    if (data && data.code === 200) {
      const payload = data.data
      if (Array.isArray(payload)) kpiList.value = payload
      else if (payload && Array.isArray(payload.records)) kpiList.value = payload.records
      else kpiList.value = []
    } else if (data && Array.isArray(data.records)) kpiList.value = data.records
    else if (Array.isArray(data)) kpiList.value = data
    else kpiList.value = []
  } catch (e) {
    ElMessage.error('加载KPI列表失败')
  } finally {
    loading.value = false
  }
}

function getDimensionLabel(dimension) {
  const labels = { 'finance': '财务维度', 'customer': '客户维度', 'internal': '内部流程', 'learning': '学习成长' }
  return labels[dimension] || dimension
}
function getDimensionType(dimension) {
  const types = { 'finance': 'danger', 'customer': 'success', 'internal': 'primary', 'learning': 'warning' }
  return types[dimension] || 'info'
}

function handleSearch() { loadData() }
function resetSearch() { searchForm.name = ''; searchForm.dimension = ''; loadData() }

function openAddDialog() {
  dialogTitle.value = '新增KPI指标'
  Object.assign(formData, {
    id: null, kpiNo: 'KPI-' + String(kpiList.value.length + 1).padStart(3, '0'),
    name: '', dimension: '', floorValue: '', targetValue: '', challengeValue: '', weight: 0, unit: '',
    frequency: '月度', dataSource: '', scoringGradient: '', definition: '', calculation: '', status: '启用'
  })
  dialogVisible.value = true
}

function editKpi(row) {
  dialogTitle.value = '编辑KPI指标'
  Object.assign(formData, {
    id: row.id, kpiNo: row.kpiNo, name: row.name, dimension: row.dimension,
    floorValue: row.floorValue || '', targetValue: row.targetValue, challengeValue: row.challengeValue || '',
    weight: row.weight, unit: row.unit, frequency: row.frequency,
    dataSource: row.dataSource || '', scoringGradient: row.scoringGradient || '',
    definition: row.definition || '', calculation: row.calculation || '', status: row.status || '启用'
  })
  dialogVisible.value = true
}

function viewDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

async function toggleStatus(row, target) {
  try {
    if (target === '启用' && totalWeight.value !== 100) {
      await ElMessageBox.confirm(`当前指标权重合计为 ${totalWeight.value}%，不等于100%，仍要启用？`, '权重校验', { type: 'warning' })
    }
    const res = await axios.put(`/api/perf/template/${row.id}`, { ...row, status: target })
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success(`已${target}`)
      loadData()
    } else ElMessage.error(res.data.message || res.data.msg || '操作失败')
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

function deleteKpi(row) {
  ElMessageBox.confirm(`确定要删除KPI指标"${row.name}"吗？`, '删除确认', { type: 'danger' })
    .then(async () => {
      try {
        const res = await axios.delete(`/api/perf/template/${row.id}`)
        if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
          ElMessage.success('KPI指标已删除')
          loadData()
        } else ElMessage.error(res.data.message || res.data.msg || '删除失败')
      } catch (e) { ElMessage.error('删除失败') }
    })
    .catch(() => {})
}

async function handleSubmit() {
  if (!formData.name) { ElMessage.warning('请填写指标名称'); return }
  if (!formData.dimension) { ElMessage.warning('请选择所属维度'); return }
  if (formData.floorValue && formData.targetValue && formData.challengeValue) {
    if (!(Number(formData.floorValue) <= Number(formData.targetValue) && Number(formData.targetValue) <= Number(formData.challengeValue))) {
      ElMessage.warning('需满足：保底值 ≤ 目标值 ≤ 挑战值')
      return
    }
  }
  try {
    let res
    if (formData.id) res = await axios.put(`/api/perf/template/${formData.id}`, formData)
    else res = await axios.post('/api/perf/template', formData)
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success(dialogTitle.value === '新增KPI指标' ? 'KPI指标新增成功' : 'KPI指标已更新')
      dialogVisible.value = false
      loadData()
    } else ElMessage.error(res.data.message || res.data.msg || '操作失败')
  } catch (e) { ElMessage.error('操作失败') }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.search-form { margin-bottom: 0; }
</style>

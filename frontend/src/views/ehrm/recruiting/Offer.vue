<template>
  <div class="page-card">
    <div class="page-header">
      <h2>录用通知与入职闭环</h2>
      <div class="header-actions">
        <el-select v-model="hrmAuth.role" size="small" style="width:170px" @change="setHrmRole">
          <el-option v-for="r in HRM_ROLES" :key="r.value" :label="r.label" :value="r.value" />
        </el-select>
        <el-button v-if="can('HR_ADMIN','HRBP')" type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>生成Offer
        </el-button>
        <ImportExportBar
          v-if="can('HR_ADMIN','HRBP','HR_DIRECTOR')"
          :columns="exportCols" :rows="offerList" file-name="Offer台账" title="录用通知" />
      </div>
    </div>

    <div class="stat-cards">
      <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ offerList.length }}</div><div class="stat-label">Offer总数</div></el-card>
      <el-card shadow="hover" class="stat-card"><div class="stat-value success">{{ acceptRate }}%</div><div class="stat-label">Offer接受率</div></el-card>
      <el-card shadow="hover" class="stat-card"><div class="stat-value danger">{{ rejectRate }}%</div><div class="stat-label">拒绝/爽约率</div></el-card>
      <el-card shadow="hover" class="stat-card"><div class="stat-value primary">{{ hiredCount }}</div><div class="stat-label">已入职</div></el-card>
      <el-card shadow="hover" class="stat-card"><div class="stat-value warn">{{ avgCycleDays }}天</div><div class="stat-label">平均招聘周期</div></el-card>
    </div>

    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="候选人">
          <el-input v-model="searchForm.candidateName" placeholder="请输入候选人姓名" style="width:180px" />
        </el-form-item>
        <el-form-item label="岗位">
          <el-input v-model="searchForm.position" placeholder="请输入岗位" style="width:180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" style="width:140px" clearable>
            <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="offerList" border stripe size="small" v-loading="loading">
        <el-table-column prop="offerNo" label="Offer编号" width="140" />
        <el-table-column prop="candidateName" label="候选人" width="90" />
        <el-table-column prop="position" label="录用岗位" width="130" />
        <el-table-column prop="department" label="部门" width="100" />
        <!-- 规格书字段，待后端扩展：职级、薪资结构、试用期、工作地点、福利说明、Offer有效期、面试总分、审批意见 -->
        <el-table-column prop="salary" label="薪资" width="90" />
        <el-table-column prop="startDate" label="入职日期" width="110" />
        <el-table-column label="招聘周期" width="100" align="center">
          <template #default="{row}">
            <span v-if="cycleDays(row) != null">{{ cycleDays(row) }}天</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="发放/确认状态" width="110" align="center">
          <template #default="{row}">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" width="150" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button v-if="can('HR_ADMIN','HRBP') && row.status === '待发送'" link size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button v-if="can('HR_ADMIN','HRBP') && row.status === '待发送'" link size="small" type="success" @click="flowStatus(row, '已发送', '发送')">发送</el-button>
            <el-button v-if="can('HR_ADMIN','HRBP') && row.status === '已发送'" link size="small" type="success" @click="flowStatus(row, '已接受', '候选人确认接受')">确认接受</el-button>
            <el-button v-if="can('HR_ADMIN','HRBP') && row.status === '已发送'" link size="small" type="warning" @click="flowStatus(row, '已拒绝', '候选人拒绝')">确认拒绝</el-button>
            <el-button v-if="can('HR_ADMIN','HRBP') && row.status === '已接受'" link size="small" type="success" @click="flowStatus(row, '已入职', '入职登记')">入职登记</el-button>
            <el-button v-if="can('HR_ADMIN','HR_DIRECTOR') && row.status !== '已失效' && row.status !== '已入职'" link size="small" type="danger" @click="voidOffer(row)">作废</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top:16px; text-align:right"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 生成/编辑Offer弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <el-form :model="formData" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="候选人" required>
              <el-select v-model="formData.candidateName" placeholder="选择复试通过候选人" filterable @change="onCandidateChange">
                <el-option v-for="c in hiredCandidates" :key="c.id" :label="c.name" :value="c.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="录用岗位" required>
              <el-input v-model="formData.position" placeholder="选择候选人自动带入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部门" required>
              <el-select v-model="formData.department" placeholder="选择部门">
                <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="薪资" required>
              <el-input v-model="formData.salary" placeholder="例如：15K（自动匹配岗位薪档校验）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="职级">
              <el-input v-model="formData.jobGrade" placeholder="例如：P6 / M2" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="试用期">
              <el-select v-model="formData.probationPeriod" placeholder="选择试用期" clearable>
                <el-option label="1个月" value="1个月" />
                <el-option label="3个月" value="3个月" />
                <el-option label="6个月" value="6个月" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入职日期" required>
              <el-date-picker v-model="formData.startDate" type="date" placeholder="选择入职日期" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="Offer有效期">
              <el-date-picker v-model="formData.expiryDate" type="date" placeholder="逾期未确认自动作废" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="工作地点">
              <el-input v-model="formData.workLocation" placeholder="请输入工作地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="福利说明">
              <el-input v-model="formData.benefits" placeholder="五险一金、补贴、期权等" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="formData.remark" :rows="3" placeholder="审批意见、评审结论、面试总分等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交审批</el-button>
      </template>
    </el-dialog>

    <!-- Offer详情弹窗 -->
    <el-dialog v-model="detailVisible" title="Offer详情" width="560px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="Offer编号">{{ detailRow.offerNo }}</el-descriptions-item>
        <el-descriptions-item label="候选人">{{ detailRow.candidateName }}</el-descriptions-item>
        <el-descriptions-item label="录用岗位">{{ detailRow.position }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ detailRow.department }}</el-descriptions-item>
        <el-descriptions-item label="薪资">{{ detailRow.salary }}</el-descriptions-item>
        <el-descriptions-item label="入职日期">{{ detailRow.startDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailRow.status }}</el-descriptions-item>
        <el-descriptions-item label="招聘周期">{{ cycleDays(detailRow) != null ? cycleDays(detailRow) + '天' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailRow.remark || '-' }}</el-descriptions-item>
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
import { hrmAuth, setHrmRole, can, HRM_ROLES } from '../hrmAuth.js'

const loading = ref(false)
const searchForm = reactive({ candidateName: '', position: '', status: '' })

const departments = ['研发部', '生产部', '销售部', '财务部', '人力资源部', '行政部']
// 状态机：待发送→已发送→已接受/已拒绝→已入职；作废→已失效（拒绝或过期自动作废、人才归档储备池）
const statusOptions = ['待发送', '已发送', '已接受', '已拒绝', '已失效', '已入职']

const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const dialogVisible = ref(false)
const dialogTitle = ref('生成Offer')
const isEdit = ref(false)
const detailVisible = ref(false)
const detailRow = ref({})
const hiredCandidates = ref([])

const emptyForm = {
  id: null, offerNo: '', candidateName: '', position: '', department: '', salary: '', startDate: '', status: '待发送', remark: '',
  // 规格书字段，待后端扩展（提交时一并带上，后端多字段不报错）
  jobGrade: '', probationPeriod: '', expiryDate: '', workLocation: '', benefits: ''
}
const formData = reactive({ ...emptyForm })

const offerList = ref([])

// Offer接受率 = (已接受+已入职) / 已发放
const acceptRate = computed(() => {
  const sent = offerList.value.filter(o => o.status !== '待发送' && o.status !== '已失效').length
  const accepted = offerList.value.filter(o => o.status === '已接受' || o.status === '已入职').length
  return sent ? Math.round(accepted / sent * 100) : 0
})
const rejectRate = computed(() => {
  const sent = offerList.value.filter(o => o.status !== '待发送' && o.status !== '已失效').length
  const rejected = offerList.value.filter(o => o.status === '已拒绝').length
  return sent ? Math.round(rejected / sent * 100) : 0
})
const hiredCount = computed(() => offerList.value.filter(o => o.status === '已入职').length)

// 招聘周期统计（规格书BI：入职日期 − Offer生成日期）
function cycleDays(row) {
  if (!row.startDate || !row.createTime) return null
  const start = new Date(row.startDate)
  const created = new Date(String(row.createTime).replace('T', ' '))
  if (isNaN(start) || isNaN(created)) return null
  const days = Math.round((start - created) / 86400000)
  return days >= 0 ? days : null
}
const avgCycleDays = computed(() => {
  const days = offerList.value.map(cycleDays).filter(d => d != null)
  if (!days.length) return 0
  return Math.round(days.reduce((a, b) => a + b, 0) / days.length)
})

const exportCols = [
  { key: 'offerNo', title: 'Offer编号' }, { key: 'candidateName', title: '候选人' }, { key: 'position', title: '录用岗位' },
  { key: 'department', title: '部门' }, { key: 'salary', title: '薪资' }, { key: 'startDate', title: '入职日期' },
  { key: 'status', title: '状态' }, { key: 'createTime', title: '生成时间' }, { key: 'remark', title: '备注' }
]

function getStatusType(status) {
  const types = { '待发送': 'info', '已发送': 'primary', '已接受': 'success', '已拒绝': 'danger', '已失效': 'info', '已入职': 'success' }
  return types[status] || 'info'
}

function extractList(res) {
  const data = res.data
  if (data && typeof data === 'object' && 'code' in data) {
    const payload = data.data
    if (Array.isArray(payload)) return { list: payload, total: payload.length }
    if (payload && Array.isArray(payload.records)) return { list: payload.records, total: payload.total || 0 }
    if (payload && Array.isArray(payload.list)) return { list: payload.list, total: payload.total || 0 }
    return { list: [], total: 0 }
  }
  if (Array.isArray(data)) return { list: data, total: data.length }
  if (data && Array.isArray(data.records)) return { list: data.records, total: data.total || 0 }
  if (data && Array.isArray(data.list)) return { list: data.list, total: data.total || 0 }
  return { list: [], total: 0 }
}

async function loadData() {
  loading.value = true
  try {
    const res = await axios.get('/api/recruitment/offer/list', {
      params: {
        page: pagination.currentPage,
        size: pagination.pageSize,
        candidateName: searchForm.candidateName || undefined,
        position: searchForm.position || undefined,
        status: searchForm.status || undefined
      }
    })
    const { list, total } = extractList(res)
    offerList.value = list
    pagination.total = total
  } catch (e) {
    ElMessage.error('加载录用通知列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 复试通过（待录用）候选人作为Offer生成来源
async function loadCandidates() {
  try {
    const res = await axios.get('/api/recruitment/rec02/list')
    hiredCandidates.value = extractList(res).list.filter(c => ['复试', '待录用'].includes(c.currentStage))
  } catch (e) { /* 候选人加载失败时允许手工输入 */ }
}

function onCandidateChange(name) {
  const c = hiredCandidates.value.find(x => x.name === name)
  if (c) formData.position = c.position
}

function handleSearch() { pagination.currentPage = 1; loadData() }
function resetSearch() { searchForm.candidateName = ''; searchForm.position = ''; searchForm.status = ''; pagination.currentPage = 1; loadData() }
function handleSizeChange(size) { pagination.pageSize = size; loadData() }
function handleCurrentChange(page) { pagination.currentPage = page; loadData() }

function openAddDialog() {
  isEdit.value = false
  dialogTitle.value = '生成Offer'
  Object.assign(formData, emptyForm)
  formData.offerNo = 'OF-' + new Date().getFullYear() + '-' + String(Date.now()).slice(-3)
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  dialogTitle.value = '编辑Offer'
  Object.assign(formData, emptyForm, row)
  dialogVisible.value = true
}

function viewDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

// 状态流转（基于后端通用更新端点）
async function flowStatus(row, status, actionName) {
  try {
    await ElMessageBox.confirm(`确定执行「${actionName}」：${row.candidateName} 的Offer吗？`, '操作确认', { type: 'warning' })
    await axios.put(`/api/recruitment/offer/${row.id}`, { ...row, status })
    ElMessage.success(`Offer已流转为「${status}」`)
    // 入职登记后同步候选人阶段为已入职
    if (status === '已入职') {
      try {
        const res = await axios.get('/api/recruitment/rec02/list', { params: { name: row.candidateName } })
        const cand = extractList(res).list.find(c => c.name === row.candidateName)
        if (cand) await axios.put(`/api/recruitment/rec02/${cand.id}`, { ...cand, currentStage: '已入职' })
      } catch (e) { /* 候选人同步失败不影响Offer流转 */ }
    }
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

function voidOffer(row) {
  ElMessageBox.confirm(`确定作废"${row.candidateName}"的Offer吗？作废后候选人归档储备池。`, '作废确认', { type: 'warning' })
    .then(async () => {
      await axios.put(`/api/recruitment/offer/${row.id}`, { ...row, status: '已失效' })
      ElMessage.success('Offer已作废')
      loadData()
    })
    .catch(() => {})
}

async function handleSubmit() {
  try {
    if (isEdit.value) {
      await axios.put(`/api/recruitment/offer/${formData.id}`, formData)
      ElMessage.success('Offer已更新')
    } else {
      await axios.post('/api/recruitment/offer', formData)
      ElMessage.success('Offer已提交审批，通过后发送')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

onMounted(() => {
  loadData()
  loadCandidates()
})
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; gap: 8px; align-items: center; }
.search-form { margin-bottom: 0; }
.stat-cards { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.stat-card { flex: 1; min-width: 130px; text-align: center; }
.stat-value { font-size: 26px; font-weight: bold; color: #303133; }
.stat-value.success { color: #67c23a; }
.stat-value.danger { color: #f56c6c; }
.stat-value.primary { color: #409eff; }
.stat-value.warn { color: #e6a23c; }
.stat-label { color: #909399; font-size: 13px; margin-top: 4px; }
</style>

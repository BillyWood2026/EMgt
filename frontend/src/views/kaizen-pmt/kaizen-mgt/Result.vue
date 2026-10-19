<template>
  <div class="page-card">
    <div class="page-header">
      <h2>改善成果</h2>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="openCreate">新增成果</el-button>
        <el-button :icon="Refresh" @click="refreshData">刷新</el-button>
      </div>
    </div>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="改善成果管理"
      description="对已完成的改善实施进行成果认定、收益核算与表彰奖励，沉淀组织改善知识资产。"
      style="margin-bottom: 16px;"
    />

    <el-card shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="成果名称">
          <el-input v-model="searchForm.name" placeholder="请输入成果名称" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="成果类型">
          <el-select v-model="searchForm.type" placeholder="选择类型" clearable style="width: 150px;">
            <el-option label="效率提升" value="efficiency" />
            <el-option label="质量提升" value="quality" />
            <el-option label="成本节约" value="cost" />
            <el-option label="安全改善" value="safety" />
            <el-option label="环境改善" value="environment" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 140px;">
            <el-option label="待认定" value="pending" />
            <el-option label="已认定" value="confirmed" />
            <el-option label="已表彰" value="awarded" />
            <el-option label="已归档" value="archived" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="doSearch">查询</el-button>
          <el-button :icon="RefreshRight" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="resultList" border stripe size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="resultNo" label="成果编号" width="130" />
        <el-table-column prop="name" label="成果名称" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="implNo" label="关联实施" width="130" />
        <el-table-column prop="type" label="成果类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTagType(row.type)">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="owner" label="完成人" width="100" />
        <el-table-column prop="actualSavings" label="实际收益(元)" width="120" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.actualSavings) }}</template>
        </el-table-column>
        <el-table-column prop="roi" label="ROI(%)" width="100" align="right">
          <template #default="{ row }">{{ row.roi == null ? '-' : row.roi + '%' }}</template>
        </el-table-column>
        <el-table-column prop="rewardAmount" label="奖励(元)" width="100" align="right">
          <template #default="{ row }">{{ row.rewardAmount == null ? '-' : '¥' + formatMoney(row.rewardAmount) }}</template>
        </el-table-column>
        <el-table-column prop="score" label="成果评分" width="150">
          <template #default="{ row }">
            <el-rate :model-value="row.score" disabled :max="5" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="completeDate" label="完成日期" width="110" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="viewDetail(row)">查看</el-button>
            <el-button v-if="row.status === 'pending'" link type="success" @click="confirmResult(row)">认定</el-button>
            <el-button v-if="row.status === 'confirmed'" link type="warning" @click="awardResult(row)">发奖</el-button>
            <el-button v-if="row.status === 'pending' || row.status === 'confirmed'" link type="primary" :icon="Edit" @click="editItem(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="deleteItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="成果编号">
              <el-input v-model="formData.resultNo" placeholder="保存后自动生成（RES 前缀）" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联实施" required>
              <el-select v-model="formData.implId" placeholder="选择已完成的实施" filterable style="width: 100%;" @change="onImplChange">
                <el-option v-for="i in selectableImpls" :key="i.id" :label="`${i.implNo} ${i.name}`" :value="i.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="成果名称" required>
          <el-input v-model="formData.name" placeholder="请输入成果名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="成果类型" required>
              <el-select v-model="formData.type" placeholder="选择成果类型" style="width: 100%;">
                <el-option label="效率提升" value="efficiency" />
                <el-option label="质量提升" value="quality" />
                <el-option label="成本节约" value="cost" />
                <el-option label="安全改善" value="safety" />
                <el-option label="环境改善" value="environment" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="完成人" required>
              <el-input v-model="formData.owner" placeholder="请输入完成人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="实际收益(元)">
              <el-input-number v-model="formData.actualSavings" :min="0" :step="1000" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="投入金额(元)">
              <el-input-number v-model="formData.investmentAmount" :min="0" :step="1000" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="完成日期">
              <el-date-picker v-model="formData.completeDate" type="date" placeholder="选择完成日期" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="投资回报率">
              <el-input :model-value="previewRoi()" disabled placeholder="保存后由后端按 (收益-投入)/投入×100% 计算" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="成果评分">
              <el-rate v-model="formData.score" :max="5" show-text :texts="['很差', '较差', '一般', '良好', '优秀']" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="成果描述">
          <el-input v-model="formData.description" type="textarea" :rows="4" placeholder="请描述改善成果与收益核算依据" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search, RefreshRight, View, Edit, Delete } from '@element-plus/icons-vue'
import axios from '@/api/axiosConfig'

// 查询表单状态
const searchForm = reactive({
  name: '',
  type: '',
  status: ''
})

// 分页状态
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 弹窗状态
const dialogVisible = ref(false)
const dialogTitle = ref('新增改善成果')
const formRef = ref(null)
const isEdit = ref(false)

// 全部实施（用于编号映射与下拉）
const allImpls = ref([])
// 仅已完成(completed)的实施可登记成果
const selectableImpls = computed(() => allImpls.value.filter(i => i.status === 'completed'))

// 新建/编辑用的表单数据
const formData = reactive({
  id: null,
  resultNo: '',
  implId: null,
  name: '',
  type: 'efficiency',
  owner: '',
  actualSavings: 0,
  investmentAmount: 0,
  completeDate: '',
  score: 3,
  description: ''
})

const allList = ref([])
const resultList = ref([])

const fmtDate = (v) => {
  if (!v) return ''
  if (Array.isArray(v)) {
    const [y, mo, d] = v
    const p = (n) => String(n).padStart(2, '0')
    return `${y}-${p(mo)}-${p(d)}`
  }
  return String(v).substring(0, 10)
}
const formatMoney = (v) => (v == null || v === '' ? '0' : Number(v).toLocaleString())

const fromEntity = (d) => ({
  id: d.id,
  resultNo: d.resultNo,
  implId: d.implId,
  implNo: (allImpls.value.find(i => i.id === d.implId) || {}).implNo || '',
  name: d.name,
  type: d.type || 'efficiency',
  owner: d.owner || '',
  actualSavings: d.actualSavings == null ? 0 : Number(d.actualSavings),
  investmentAmount: d.investmentAmount == null ? 0 : Number(d.investmentAmount),
  roi: d.roi == null ? null : Number(d.roi),
  rewardAmount: d.rewardAmount == null ? null : Number(d.rewardAmount),
  rewardStatus: d.rewardStatus,
  completeDate: fmtDate(d.completeDate),
  score: d.score || 0,
  status: d.status,
  description: d.description || ''
})

const applyFilterAndPage = () => {
  let rows = allList.value
  if (searchForm.name) rows = rows.filter(r => (r.name || '').includes(searchForm.name))
  if (searchForm.type) rows = rows.filter(r => r.type === searchForm.type)
  if (searchForm.status) rows = rows.filter(r => r.status === searchForm.status)
  pagination.total = rows.length
  const start = (pagination.currentPage - 1) * pagination.pageSize
  resultList.value = rows.slice(start, start + pagination.pageSize)
}

const loadImpls = async () => {
  const res = await axios.get('/api/kaizen/implementation/list')
  allImpls.value = res.data && res.data.data ? res.data.data : []
}

const loadList = async () => {
  const res = await axios.get('/api/kaizen/result/list')
  allList.value = (res.data && res.data.data ? res.data.data : []).map(fromEntity)
  applyFilterAndPage()
}

const errMsg = (e) => (e.response && e.response.data && (e.response.data.message || e.response.data.msg)) || e.message

// 选择关联实施后继承名称/完成人（名称可再修改）
const onImplChange = (implId) => {
  const i = allImpls.value.find(x => x.id === implId)
  if (!i) return
  if (!formData.name) formData.name = i.name
  formData.owner = i.owner || ''
}

// ROI 本地预览，以后端计算落库为准：ROI(%) = (实际收益 - 投入) / 投入 * 100
const previewRoi = () => {
  const inv = Number(formData.investmentAmount)
  if (!inv || inv <= 0) return ''
  return (((Number(formData.actualSavings) || 0) - inv) / inv * 100).toFixed(2) + '%'
}

// 文案/标签辅助函数
function typeLabel(type) {
  const map = { efficiency: '效率提升', quality: '质量提升', cost: '成本节约', safety: '安全改善', environment: '环境改善' }
  return map[type] || type
}

function typeTagType(type) {
  const map = { efficiency: 'primary', quality: 'success', cost: 'warning', safety: 'danger', environment: 'info' }
  return map[type] || 'info'
}

function statusLabel(status) {
  const map = { pending: '待认定', confirmed: '已认定', awarded: '已表彰', archived: '已归档' }
  return map[status] || status
}

function statusTagType(status) {
  const map = { pending: 'warning', confirmed: 'primary', awarded: 'success', archived: 'info' }
  return map[status] || 'info'
}

// 将表单数据重置为默认值
function resetFormData() {
  Object.assign(formData, {
    id: null,
    resultNo: '',
    implId: null,
    name: '',
    type: 'efficiency',
    owner: '',
    actualSavings: 0,
    investmentAmount: 0,
    completeDate: '',
    score: 3,
    description: ''
  })
}

// 打开新建弹窗
function openCreate() {
  isEdit.value = false
  dialogTitle.value = '新增改善成果'
  resetFormData()
  dialogVisible.value = true
}

// 查看详情
function viewDetail(row) {
  ElMessageBox.alert(
    `成果编号：${row.resultNo}\n关联实施：${row.implNo || '-'}\n完成人：${row.owner}\n实际收益：¥${formatMoney(row.actualSavings)}\n投入金额：¥${formatMoney(row.investmentAmount)}\n投资回报率：${row.roi == null ? '-' : row.roi + '%'}\n奖励金额：${row.rewardAmount == null ? '-' : '¥' + formatMoney(row.rewardAmount)}\n完成日期：${row.completeDate || '-'}\n成果描述：${row.description || '-'}`,
    `成果详情：${row.name}`,
    { customClass: 'result-detail-box' }
  ).catch(() => {})
}

// 编辑已有条目（已表彰/已归档不可编辑，后端强校验）
function editItem(row) {
  isEdit.value = true
  dialogTitle.value = '编辑改善成果'
  resetFormData()
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 删除条目并二次确认
function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除成果「${row.name}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await axios.delete(`/api/kaizen/result/${row.id}`)
        ElMessage.success('删除成功')
        await loadList()
      } catch (e) {
        ElMessage.error('删除失败：' + errMsg(e))
      }
    })
    .catch(() => {})
}

// 查询处理
function doSearch() {
  pagination.currentPage = 1
  applyFilterAndPage()
}

// 重置查询表单
function resetSearch() {
  searchForm.name = ''
  searchForm.type = ''
  searchForm.status = ''
  pagination.currentPage = 1
  applyFilterAndPage()
}

// 刷新数据
async function refreshData() {
  await loadList()
  ElMessage.success('数据已刷新')
}

// 待认定 -> 已认定
function confirmResult(row) {
  ElMessageBox.confirm(`确定认定成果「${row.name}」吗？认定后可发放奖励。`, '成果认定', { type: 'warning' })
    .then(async () => {
      try {
        await axios.post(`/api/kaizen/result/${row.id}/confirm`)
        ElMessage.success('已认定')
        await loadList()
      } catch (e) {
        ElMessage.error('认定失败：' + errMsg(e))
      }
    }).catch(() => {})
}

// 已认定 -> 已表彰（奖励金额非空非负，后端写 rewardTime）
async function awardResult(row) {
  let amount
  try {
    const r = await ElMessageBox.prompt('请输入奖励金额（元，非负）', '发放奖励', {
      confirmButtonText: '确定发放',
      cancelButtonText: '取消',
      inputValue: '0',
      inputValidator: (v) => {
        const n = Number(v)
        if (v === '' || Number.isNaN(n) || n < 0) return '请输入不小于 0 的金额'
        return true
      }
    })
    amount = Number(r.value)
  } catch {
    return
  }
  try {
    await axios.post(`/api/kaizen/result/${row.id}/award`, { rewardAmount: amount })
    ElMessage.success('奖励已发放')
    await loadList()
  } catch (e) {
    ElMessage.error('发奖失败：' + errMsg(e))
  }
}

// 保存新建/编辑的表单数据
async function saveData() {
  if (!formData.implId) {
    ElMessage.warning('请选择关联实施（仅已完成的实施可登记成果）')
    return
  }
  if (!formData.name || !formData.owner) {
    ElMessage.warning('请填写必填项：成果名称、完成人')
    return
  }
  if (formData.score < 1 || formData.score > 5) {
    ElMessage.warning('成果评分必须在 1~5 之间')
    return
  }
  const payload = {
    implId: formData.implId,
    name: formData.name,
    type: formData.type,
    owner: formData.owner,
    actualSavings: formData.actualSavings,
    investmentAmount: formData.investmentAmount,
    completeDate: formData.completeDate || null,
    score: formData.score,
    description: formData.description
  }
  try {
    if (isEdit.value) {
      await axios.put(`/api/kaizen/result/${formData.id}`, payload)
      ElMessage.success('修改成功')
    } else {
      // resultNo(RES)/pending/rewardStatus/ROI 均由后端生成
      await axios.post('/api/kaizen/result', payload)
      ElMessage.success('新建成功')
    }
    dialogVisible.value = false
    await loadList()
  } catch (e) {
    ElMessage.error('保存失败：' + errMsg(e))
  }
}

onMounted(async () => {
  await loadImpls()
  await loadList()
})
</script>

<style scoped>
.page-card {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.search-form {
  margin-bottom: 16px;
}
</style>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>人才继任计划</h2>
      <el-button type="primary" @click="handleAdd">新增继任计划</el-button>
    </div>

    <el-alert type="info" :closable="false" style="margin-top:12px"
      title="继任匹配分 = 绩效×30% + 九宫格潜力×30% + 任职资格×25% + 经验×15%；匹配分<60 禁止入池。状态机：草稿 → 提交审批 → 生效 → 作废。断层风险指数 = 流失概率×60 + 储备缺口×40。" />

    <div class="filter-bar">
      <el-input v-model="queryKeyword" placeholder="岗位/继任人姓名" clearable style="width:200px" />
      <el-select v-model="queryStatus" placeholder="状态" clearable style="width:140px">
        <el-option label="草稿" value="草稿" />
        <el-option label="提交审批" value="提交审批" />
        <el-option label="生效" value="生效" />
        <el-option label="作废" value="作废" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="queryKeyword='';queryStatus='';loadData()">重置</el-button>
    </div>

    <ImportExportBar :columns="ioColumns" :rows="filteredList" file-name="人才继任计划" title="人才继任计划导入导出" style="margin-top:12px" @done="loadData" />

    <el-table :data="filteredList" border stripe size="small" v-loading="loading" style="margin-top:12px">
      <el-table-column prop="positionName" label="关键岗位" min-width="140" show-overflow-tooltip />
      <el-table-column prop="successorName" label="继任人" width="110" />
      <el-table-column prop="readiness" label="就绪度" width="110">
        <template #default="{row}">
          <el-tag :type="readinessType(row.readiness)" size="small">{{ row.readiness || '—' }}</el-tag>
        </template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="matchScore" label="匹配分" width="90">
        <template #default="{row}">
          <span v-if="row.matchScore != null" :style="{color: Number(row.matchScore) >= 80 ? '#67c23a' : Number(row.matchScore) >= 60 ? '#e6a23c' : '#f56c6c', fontWeight:'bold'}">{{ row.matchScore }}</span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="riskIndex" label="断层风险指数" width="115">
        <template #default="{row}">
          <span v-if="row.riskIndex != null" :style="{color: Number(row.riskIndex) >= 60 ? '#f56c6c' : Number(row.riskIndex) >= 30 ? '#e6a23c' : '#67c23a', fontWeight:'bold'}">{{ row.riskIndex }}</span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="developmentPlan" label="培养发展计划" min-width="180" show-overflow-tooltip />
      <el-table-column prop="reviewDate" label="复审日期" width="110" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="statusType(row.status)" size="small">{{ row.status || '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="warning" @click="openMatch(row)">匹配打分</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="!row.status || row.status === '草稿'" size="small" type="success" @click="flow(row, '提交审批')">提交审批</el-button>
          <el-button v-if="row.status === '提交审批'" size="small" type="success" @click="flow(row, '生效')">审批生效</el-button>
          <el-button v-if="row.status !== '作废'" size="small" type="danger" @click="flow(row, '作废')">作废</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form :model="formData" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="关键岗位" required>
              <el-input v-model="formData.positionName" placeholder="请输入关键岗位名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位ID">
              <el-input-number v-model="formData.positionId" :min="0" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="继任人" required>
              <el-input v-model="formData.successorName" placeholder="请输入继任人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="继任人ID">
              <el-input-number v-model="formData.successorId" :min="0" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="就绪度">
          <el-radio-group v-model="formData.readiness">
            <el-radio-button value="已就绪">已就绪</el-radio-button>
            <el-radio-button value="1年内就绪">1年内就绪</el-radio-button>
            <el-radio-button value="2-3年就绪">2-3年就绪</el-radio-button>
            <el-radio-button value="储备观察">储备观察</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="培养发展计划">
          <el-input v-model="formData.developmentPlan" type="textarea" :rows="3" placeholder="轮岗/带教/培训等培养安排" />
        </el-form-item>
        <el-form-item label="复审日期">
          <el-date-picker v-model="formData.reviewDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 匹配打分弹窗 -->
    <el-dialog v-model="matchVisible" :title="`继任匹配打分 — ${matchForm.successorName || ''} → ${matchForm.positionName || ''}`" width="720px">
      <el-form :model="matchForm" label-width="150px">
        <el-form-item label="绩效得分(权重30%)">
          <el-input-number v-model="matchForm.perfScore" :min="0" :max="100" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="九宫格潜力(权重30%)">
          <el-input-number v-model="matchForm.potentialScore" :min="0" :max="100" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="任职资格(权重25%)">
          <el-input-number v-model="matchForm.qualScore" :min="0" :max="100" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="经验匹配(权重15%)">
          <el-input-number v-model="matchForm.expScore" :min="0" :max="100" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="匹配总分">
          <el-tag :type="matchTotal >= 80 ? 'success' : matchTotal >= 60 ? 'warning' : 'danger'" style="width:100%;text-align:center;font-size:15px">
            {{ matchTotal }} 分（绩效×30%+潜力×30%+任职×25%+经验×15%）
            <template v-if="matchTotal < 60"> — 低于60分禁止入池</template>
          </el-tag>
        </el-form-item>
        <el-form-item label="流失概率%(0-100)">
          <el-input-number v-model="matchForm.attritionRate" :min="0" :max="100" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="储备缺口%(0-100)">
          <el-input-number v-model="matchForm.gapRate" :min="0" :max="100" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="断层风险指数">
          <el-tag :type="riskTotal >= 60 ? 'danger' : riskTotal >= 30 ? 'warning' : 'success'" style="width:100%;text-align:center">
            {{ riskTotal }}（流失概率×60 + 储备缺口×40）
          </el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="matchVisible=false">取消</el-button>
        <el-button type="primary" :disabled="matchTotal < 60" @click="submitMatch">保存打分</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImportExportBar from '@/components/ImportExportBar.vue'
import { startApproval, FLOW_DEF_CODES } from '@/api/flowApproval'

const loading = ref(false)
const tableData = ref([])
const queryKeyword = ref('')
const queryStatus = ref('')

const ioColumns = [
  { key: 'positionName', title: '关键岗位' },
  { key: 'successorName', title: '继任人' },
  { key: 'readiness', title: '就绪度' },
  { key: 'matchScore', title: '匹配分' },
  { key: 'riskIndex', title: '断层风险指数' },
  { key: 'developmentPlan', title: '培养发展计划' },
  { key: 'reviewDate', title: '复审日期' },
  { key: 'status', title: '状态' }
]

const filteredList = computed(() => tableData.value.filter(r => {
  if (queryKeyword.value && !((r.positionName || '').includes(queryKeyword.value) || (r.successorName || '').includes(queryKeyword.value))) return false
  if (queryStatus.value && (r.status || '草稿') !== queryStatus.value) return false
  return true
}))

const dialogVisible = ref(false)
const dialogTitle = ref('新增继任计划')
const formData = reactive({
  id: null, positionId: null, positionName: '', successorId: null, successorName: '',
  readiness: '储备观察', developmentPlan: '', reviewDate: '', status: '草稿'
})

const matchVisible = ref(false)
const matchForm = reactive({
  id: null, positionName: '', successorName: '', row: null,
  perfScore: null, potentialScore: null, qualScore: null, expScore: null,
  attritionRate: null, gapRate: null
})

const matchTotal = computed(() => Math.round(
  (Number(matchForm.perfScore) || 0) * 0.3 +
  (Number(matchForm.potentialScore) || 0) * 0.3 +
  (Number(matchForm.qualScore) || 0) * 0.25 +
  (Number(matchForm.expScore) || 0) * 0.15
))
const riskTotal = computed(() => Math.round((Number(matchForm.attritionRate) || 0) * 0.6 + (Number(matchForm.gapRate) || 0) * 0.4))

function readinessType(r) {
  return { '已就绪': 'success', '1年内就绪': 'primary', '2-3年就绪': 'warning', '储备观察': 'info' }[r] || 'info'
}
function statusType(s) {
  return { '草稿': 'info', '提交审批': 'warning', '生效': 'success', '作废': 'danger' }[s] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await axios.get('/api/talent/succession/list')
    const data = res.data
    if (data && data.code === 200) {
      const payload = data.data
      if (Array.isArray(payload)) tableData.value = payload
      else if (payload && Array.isArray(payload.records)) tableData.value = payload.records
      else tableData.value = []
    } else if (Array.isArray(data)) tableData.value = data
    else tableData.value = []
  } catch (e) {
    ElMessage.error('加载继任计划失败')
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  dialogTitle.value = '新增继任计划'
  Object.assign(formData, { id: null, positionId: null, positionName: '', successorId: null, successorName: '', readiness: '储备观察', developmentPlan: '', reviewDate: '', status: '草稿' })
  dialogVisible.value = true
}
function handleEdit(row) {
  dialogTitle.value = '编辑继任计划'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formData.positionName) { ElMessage.warning('请填写关键岗位'); return }
  if (!formData.successorName) { ElMessage.warning('请填写继任人'); return }
  try {
    let res
    if (formData.id) res = await axios.put(`/api/talent/succession/${formData.id}`, formData)
    else res = await axios.post('/api/talent/succession', formData)
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } else ElMessage.error(res.data.message || res.data.msg || '保存失败')
  } catch (e) { ElMessage.error(e.response?.data?.message || '保存失败') }
}

function openMatch(row) {
  Object.assign(matchForm, {
    id: row.id, positionName: row.positionName, successorName: row.successorName, row,
    perfScore: null, potentialScore: null, qualScore: null, expScore: null,
    attritionRate: null, gapRate: null
  })
  matchVisible.value = true
}
async function submitMatch() {
  if (matchTotal.value < 60) { ElMessage.warning('匹配分低于60分，禁止入池'); return }
  try {
    const res = await axios.put(`/api/talent/succession/${matchForm.id}`, {
      ...matchForm.row,
      matchScore: matchTotal.value, riskIndex: riskTotal.value,
      perfScore: matchForm.perfScore, potentialScore: matchForm.potentialScore,
      qualScore: matchForm.qualScore, expScore: matchForm.expScore,
      attritionRate: matchForm.attritionRate, gapRate: matchForm.gapRate
    })
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success('打分已保存')
      matchVisible.value = false
      loadData()
    } else ElMessage.error(res.data.message || res.data.msg || '保存失败')
  } catch (e) { ElMessage.error(e.response?.data?.message || '保存失败') }
}

async function flow(row, target) {
  const tipMap = { '提交审批': '确认提交审批？', '生效': '确认审批通过并生效该继任计划？', '作废': '作废后不可恢复，确认作废？' }
  try {
    await ElMessageBox.confirm(tipMap[target] || '确认操作？', '状态流转', { type: target === '作废' ? 'error' : 'warning' })
    // 提交审批时优先走流程引擎
    if (target === '提交审批') {
      try {
        const instNo = await startApproval({
          defCode: FLOW_DEF_CODES.HRM_SUCCESSION,
          bizCode: 'succession',
          bizId: row.id,
          bizJson: row,
          title: `继任计划-${row.positionName}-${row.successorName}`,
          initiator: localStorage.getItem('username') || 'dev-user'
        })
        const res = await axios.put(`/api/talent/succession/${row.id}`, { ...row, status: target })
        if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
          ElMessage.success('已发起流程审批，实例号：' + instNo)
          loadData()
        } else ElMessage.error(res.data.message || res.data.msg || '操作失败')
        return
      } catch (e) {
        ElMessage.warning('流程引擎不可用，已本地提交')
      }
    }
    const res = await axios.put(`/api/talent/succession/${row.id}`, { ...row, status: target })
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success('操作成功')
      loadData()
    } else ElMessage.error(res.data.message || res.data.msg || '操作失败')
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '操作失败') }
}

onMounted(loadData)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.filter-bar { display: flex; gap: 10px; margin-top: 14px; align-items: center; }
</style>

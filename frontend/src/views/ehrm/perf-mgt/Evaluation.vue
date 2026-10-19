<template>
  <div class="page-card">
    <div class="page-header">
      <h2>绩效评估</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openStartDialog">
          <el-icon><List /></el-icon>发起评估
        </el-button>
      </div>
    </div>

    <el-alert type="info" :closable="false" style="margin-bottom:12px"
      title="评估状态机：待评估 → 评估中 → 待审核 → 已完成；综合得分 = 自评×20% + 上级评×60% + 平级评×20%；得分低于60分须填写强制说明。" />

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="5">
        <el-form-item label="评估周期">
          <el-select v-model="evalPeriod" placeholder="选择周期" clearable style="width:100%">
            <el-option label="2026年Q2" value="2026Q2" />
            <el-option label="2026年Q1" value="2026Q1" />
            <el-option label="2025年年度" value="2025Y" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="5">
        <el-form-item label="评估状态">
          <el-select v-model="evalStatus" placeholder="选择状态" clearable style="width:100%">
            <el-option label="待评估" value="待评估" />
            <el-option label="评估中" value="评估中" />
            <el-option label="待审核" value="待审核" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="5">
        <el-form-item label="部门">
          <el-select v-model="evalDepartment" placeholder="选择部门" clearable style="width:100%">
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

    <ImportExportBar :columns="ioColumns" :rows="evaluationList" file-name="绩效评估" title="绩效评估导入导出" style="margin-bottom:12px" @done="loadData" />

    <el-card shadow="never">
      <el-table :data="evaluationList" border stripe size="small" v-loading="loading">
        <el-table-column prop="evalNo" label="评估编号" width="140" />
        <el-table-column prop="employeeName" label="员工姓名" width="100" />
        <el-table-column prop="department" label="部门" width="110" />
        <el-table-column prop="position" label="岗位" width="110" />
        <el-table-column prop="period" label="评估周期" width="110" />
        <el-table-column prop="score" label="综合得分" width="100">
          <template #default="{row}">
            <span v-if="row.score != null" :style="{color: Number(row.score) >= 80 ? '#67c23a' : Number(row.score) >= 60 ? '#e6a23c' : '#f56c6c', fontWeight:'bold'}">{{ row.score }}分</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="评级" width="80">
          <template #default="{row}">
            <el-tag v-if="row.grade" :type="getGradeType(row.grade)" size="small">{{ row.grade }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="evaluateTime" label="评估时间" width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button link size="small" type="warning" v-if="row.status === '待评估' || row.status === '评估中'" @click="evaluate(row)">评估打分</el-button>
            <el-button link size="small" type="success" v-if="row.status === '评估中'" @click="flow(row, '待审核')">提交审核</el-button>
            <el-button link size="small" type="success" v-if="row.status === '待审核'" @click="flow(row, '已完成')">审核通过</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 发起评估 -->
    <el-dialog v-model="startVisible" title="发起绩效评估" width="680px">
      <el-form :model="startForm" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="员工姓名" required>
              <el-input v-model="startForm.employeeName" placeholder="请输入员工姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评估周期" required>
              <el-select v-model="startForm.period" style="width:100%">
                <el-option label="2026年Q2" value="2026Q2" />
                <el-option label="2026年Q1" value="2026Q1" />
                <el-option label="2025年年度" value="2025Y" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="部门">
              <el-select v-model="startForm.department" style="width:100%">
                <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位">
              <el-input v-model="startForm.position" placeholder="请输入岗位" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="评估模板">
          <el-input v-model="startForm.templateName" placeholder="关联的考核模板/方案" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="考核关系">
          <el-input v-model="startForm.evalRelation" placeholder="上级/平级/下级评估人配置" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="startVisible=false">取消</el-button>
        <el-button type="primary" @click="submitStart">发起</el-button>
      </template>
    </el-dialog>

    <!-- 评估打分 -->
    <el-dialog v-model="evalVisible" :title="`评估打分 — ${evalForm.employeeName || ''}`" width="720px">
      <el-form :model="evalForm" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="自评分(20%)">
              <el-input-number v-model="evalForm.selfScore" :min="0" :max="100" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="上级评(60%)">
              <el-input-number v-model="evalForm.superiorScore" :min="0" :max="100" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="平级评(20%)">
              <el-input-number v-model="evalForm.peerScore" :min="0" :max="100" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="综合得分">
          <el-tag :type="compositeScore >= 80 ? 'success' : compositeScore >= 60 ? 'warning' : 'danger'" style="width:100%;text-align:center;font-size:15px">
            {{ compositeScore }} 分（自评×20%+上级×60%+平级×20%）→ 评级 {{ autoGrade }}
          </el-tag>
        </el-form-item>
        <el-form-item label="评级">
          <el-radio-group v-model="evalForm.grade">
            <el-radio-button value="A">A</el-radio-button>
            <el-radio-button value="B">B</el-radio-button>
            <el-radio-button value="C">C</el-radio-button>
            <el-radio-button value="D">D</el-radio-button>
          </el-radio-group>
          <span style="margin-left:10px;color:#909399;font-size:12px">建议评级 {{ autoGrade }}，可人工调整</span>
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="低分强制说明" v-if="compositeScore < 60">
          <el-input v-model="evalForm.lowScoreReason" type="textarea" :rows="2" placeholder="综合得分低于60分，必须填写说明" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="评估意见">
          <el-input v-model="evalForm.evalComment" type="textarea" :rows="3" placeholder="优势/不足/改进建议" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evalVisible=false">取消</el-button>
        <el-button type="primary" @click="submitEval">提交评估</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="评估详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="评估编号">{{ detailRow.evalNo }}</el-descriptions-item>
        <el-descriptions-item label="员工姓名">{{ detailRow.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ detailRow.department }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ detailRow.position }}</el-descriptions-item>
        <el-descriptions-item label="评估周期">{{ detailRow.period }}</el-descriptions-item>
        <el-descriptions-item label="综合得分">{{ detailRow.score != null ? detailRow.score + ' 分' : '—' }}</el-descriptions-item>
        <el-descriptions-item label="评级">
          <el-tag v-if="detailRow.grade" :type="getGradeType(detailRow.grade)" size="small">{{ detailRow.grade }}</el-tag>
          <span v-else>—</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailRow.status)" size="small">{{ detailRow.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="评估时间" :span="2">{{ detailRow.evaluateTime }}</el-descriptions-item>
        <el-descriptions-item label="评估意见" :span="2">{{ detailRow.evalComment || '—' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { List } from '@element-plus/icons-vue'
import ImportExportBar from '@/components/ImportExportBar.vue'
import { startApproval, FLOW_DEF_CODES } from '@/api/flowApproval'

const loading = ref(false)
const evalPeriod = ref('')
const evalStatus = ref('')
const evalDepartment = ref('')

const departments = ['研发部', '生产部', '销售部', '财务部', '人力资源部', '行政部']

const ioColumns = [
  { key: 'evalNo', title: '评估编号' },
  { key: 'employeeName', title: '员工姓名' },
  { key: 'department', title: '部门' },
  { key: 'position', title: '岗位' },
  { key: 'period', title: '评估周期' },
  { key: 'score', title: '综合得分' },
  { key: 'grade', title: '评级' },
  { key: 'status', title: '状态' },
  { key: 'evaluateTime', title: '评估时间' }
]

const evaluationList = ref([])

const startVisible = ref(false)
const startForm = reactive({ employeeName: '', period: '2026Q2', department: '', position: '', templateName: '', evalRelation: '' })

const evalVisible = ref(false)
const evalForm = reactive({ id: null, evalNo: '', employeeName: '', selfScore: null, superiorScore: null, peerScore: null, grade: '', lowScoreReason: '', evalComment: '', row: null })

const detailVisible = ref(false)
const detailRow = ref({})

const compositeScore = computed(() => {
  const s = Number(evalForm.selfScore) || 0
  const u = Number(evalForm.superiorScore) || 0
  const p = Number(evalForm.peerScore) || 0
  return Math.round(s * 0.2 + u * 0.6 + p * 0.2)
})
const autoGrade = computed(() => {
  const v = compositeScore.value
  if (v >= 90) return 'A'
  if (v >= 80) return 'B'
  if (v >= 60) return 'C'
  return 'D'
})

async function loadData() {
  loading.value = true
  try {
    const params = { page: 1, size: 100 }
    if (evalPeriod.value) params.period = evalPeriod.value
    if (evalStatus.value) params.status = evalStatus.value
    if (evalDepartment.value) params.department = evalDepartment.value
    const res = await axios.get('/api/perf/assessment/list', { params })
    const data = res.data
    if (data && data.code === 200) {
      const payload = data.data
      if (Array.isArray(payload)) evaluationList.value = payload
      else if (payload && Array.isArray(payload.records)) evaluationList.value = payload.records
      else evaluationList.value = []
    } else if (data && Array.isArray(data.records)) evaluationList.value = data.records
    else if (Array.isArray(data)) evaluationList.value = data
    else evaluationList.value = []
  } catch (e) {
    ElMessage.error('加载评估列表失败')
  } finally {
    loading.value = false
  }
}

function getGradeType(grade) {
  const types = { 'S': 'danger', 'A': 'success', 'B': 'primary', 'C': 'warning', 'D': 'danger' }
  return types[grade] || 'info'
}
function getStatusType(status) {
  const types = { '待评估': 'warning', '评估中': 'primary', '待审核': 'info', '已完成': 'success' }
  return types[status] || 'info'
}

function handleSearch() { loadData() }

function openStartDialog() {
  Object.assign(startForm, { employeeName: '', period: '2026Q2', department: '', position: '', templateName: '', evalRelation: '' })
  startVisible.value = true
}
async function submitStart() {
  if (!startForm.employeeName) { ElMessage.warning('请填写员工姓名'); return }
  try {
    const res = await axios.post('/api/perf/assessment', { ...startForm, status: '待评估' })
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success('绩效评估已发起')
      startVisible.value = false
      loadData()
    } else ElMessage.error(res.data.message || res.data.msg || '发起失败')
  } catch (e) { ElMessage.error(e.response?.data?.message || '发起失败') }
}

function evaluate(row) {
  Object.assign(evalForm, { id: row.id, evalNo: row.evalNo, employeeName: row.employeeName, selfScore: null, superiorScore: null, peerScore: null, grade: '', lowScoreReason: '', evalComment: '', row })
  evalVisible.value = true
}
async function submitEval() {
  if (evalForm.selfScore == null || evalForm.superiorScore == null || evalForm.peerScore == null) {
    ElMessage.warning('自评/上级评/平级评分均需填写')
    return
  }
  if (compositeScore.value < 60 && !evalForm.lowScoreReason) {
    ElMessage.warning('综合得分低于60分，低分强制说明必填')
    return
  }
  const grade = evalForm.grade || autoGrade.value
  try {
    const res = await axios.put(`/api/perf/assessment/${evalForm.id}`, {
      ...evalForm.row, score: compositeScore.value, grade, status: '评估中',
      lowScoreReason: evalForm.lowScoreReason, evalComment: evalForm.evalComment,
      selfScore: evalForm.selfScore, superiorScore: evalForm.superiorScore, peerScore: evalForm.peerScore
    })
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success('评估已提交')
      evalVisible.value = false
      loadData()
    } else ElMessage.error(res.data.message || res.data.msg || '提交失败')
  } catch (e) { ElMessage.error(e.response?.data?.message || '提交失败') }
}

async function flow(row, target) {
  try {
    await ElMessageBox.confirm(`确认将【${row.employeeName}】的评估流转为「${target}」？`, '状态流转', { type: 'warning' })
    // 提交审核时优先走流程引擎
    if (target === '待审核') {
      try {
        const instNo = await startApproval({
          defCode: FLOW_DEF_CODES.HRM_PERF_EVAL,
          bizCode: 'perf',
          bizId: row.id,
          bizJson: row,
          title: `绩效评估-${row.employeeName}-${row.period}`,
          initiator: localStorage.getItem('username') || 'dev-user'
        })
        const res = await axios.put(`/api/perf/assessment/${row.id}`, { ...row, status: target })
        if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
          ElMessage.success('已发起流程审批，实例号：' + instNo)
          loadData()
        } else ElMessage.error(res.data.message || res.data.msg || '流转失败')
        return
      } catch (e) {
        ElMessage.warning('流程引擎不可用，已本地提交')
      }
    }
    const res = await axios.put(`/api/perf/assessment/${row.id}`, { ...row, status: target })
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success('流转成功')
      loadData()
    } else ElMessage.error(res.data.message || res.data.msg || '流转失败')
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '流转失败') }
}

function viewDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

onMounted(() => { loadData() })
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
</style>

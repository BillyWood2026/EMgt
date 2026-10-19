<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">学习中心(前台)：我的课程任务进度（中台跳转模拟）+ 我的考试（防切屏监考：切窗≥3次自动交卷）；不及格冻结评优/晋升/调薪，补考通过自动解冻</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <template #header><b>我的学习任务</b></template>
      <el-table :data="studyRows" size="small" border v-loading="studyLoading">
        <el-table-column prop="record.taskName" label="任务" min-width="180" show-overflow-tooltip />
        <el-table-column prop="record.courseName" label="课程" min-width="180" show-overflow-tooltip />
        <el-table-column prop="record.progress" label="进度%" width="90" align="center">
          <template #default="{row}">
            <el-progress :percentage="row.record.progress || 0" :stroke-width="8" :format="p => p + '%'" style="width:70px" />
          </template>
        </el-table-column>
        <el-table-column prop="record.status" label="状态" width="90" align="center">
          <template #default="{row}">
            <el-tag size="small" :type="row.record.status==='已完成'?'success':'warning'">{{ row.record.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="截止日期" width="120" align="center">
          <template #default="{row}">
            <span v-if="row.deadline" :class="{ overdue: row.overdue }">{{ row.deadline }}</span>
            <span v-else>-</span>
            <el-tag v-if="row.overdue" size="small" type="danger" class="ml4">逾期</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{row}">
            <el-button size="small" type="primary" plain :disabled="row.record.status==='已完成'" @click="goMid(row)">跳转中台学习</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="tip mt">点击"跳转中台学习"模拟培训中台学完回写进度100%（进度≥80%计学时）</div>
    </el-card>

    <el-card shadow="never" class="mt">
      <template #header><b>我的考试</b></template>
      <el-table :data="examRows" size="small" border v-loading="examLoading">
        <el-table-column prop="examName" label="考试" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="150" align="center">
          <template #default="{row}">
            <el-tag size="small" :type="instType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="70" align="center">
          <template #default="{row}">{{ row.score === null ? '-' : row.score }}</template>
        </el-table-column>
        <el-table-column prop="attempt" label="第几次" width="70" align="center" />
        <el-table-column prop="switchCount" label="切屏" width="70" align="center" />
        <el-table-column label="资格" width="100" align="center">
          <template #default="{row}">
            <el-tag v-if="row.frozen===1" size="small" type="danger">冻结中</el-tag>
            <el-tag v-else size="small" type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{row}">
            <el-button
              v-if="row.status==='待考试' || row.status==='已完成不合格待补考'"
              size="small" type="danger" @click="startExam(row)">
              {{ row.status==='待考试' ? '开始考试' : '去补考' }}
            </el-button>
            <span v-else class="tip">-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 考试弹窗：全屏防切屏 -->
    <el-dialog v-model="examVis" :title="cur.paper ? cur.paper.examName : '考试'" fullscreen :close-on-click-modal="false" :close-on-press-escape="false" :show-close="false" @keydown.esc.stop>
      <template v-if="cur.paper">
        <div class="exam-head">
          <span>剩余时间：<b :class="{ danger: remainSec <= 60 }">{{ countdown }}</b></span>
          <span>切屏次数：<b :class="{ danger: switchCount >= (cur.paper.antiSwitch ? 2 : 99) }">{{ switchCount }}</b> / 上限3次</span>
          <span class="tip">防切屏监考中：切出页面将被记录，累计3次自动交卷</span>
          <span style="flex:1"></span>
          <span>及格线 {{ cur.paper.passLine }} 分 · 第 {{ cur.paper.attempt }} 次作答</span>
        </div>
        <div class="paper">
          <div v-for="(q, idx) in cur.paper.paper" :key="q.id" class="q-item">
            <div class="q-stem">{{ idx + 1 }}. [{{ q.questionType }}] {{ q.stem }}</div>
            <el-radio-group v-if="q.questionType !== '多选'" v-model="answers[q.id]">
              <div v-for="(opt, i) in q.options" :key="i" class="q-opt">
                <el-radio :value="opt">{{ letter(i) }}. {{ opt }}</el-radio>
              </div>
            </el-radio-group>
            <el-checkbox-group v-else v-model="answersMulti[q.id]">
              <div v-for="(opt, i) in q.options" :key="i" class="q-opt">
                <el-checkbox :value="opt">{{ letter(i) }}. {{ opt }}</el-checkbox>
              </div>
            </el-checkbox-group>
          </div>
        </div>
        <div class="exam-foot">
          <el-button type="danger" size="large" :loading="submitting" @click="submitExam(false)">交卷</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'

const username = localStorage.getItem('username') || 'dev-user'
const studyRows = ref([])
const studyLoading = ref(false)
const examRows = ref([])
const examLoading = ref(false)

const examVis = ref(false)
const cur = reactive({ instanceId: null, paper: null })
const answers = reactive({})
const answersMulti = reactive({})
const switchCount = ref(0)
const remainSec = ref(0)
const submitting = ref(false)
let timer = null
let onVis = null

const letter = i => String.fromCharCode(65 + i)
const countdown = computed(() => {
  const m = Math.floor(remainSec.value / 60)
  const s = remainSec.value % 60
  return (m < 10 ? '0' + m : m) + ':' + (s < 10 ? '0' + s : s)
})
function instType(s) {
  if (s === '已完成合格' || s === '补考完成合格') return 'success'
  if (s === '已完成不合格待补考') return 'warning'
  if (s === '补考仍不合格') return 'danger'
  return ''
}

async function loadStudy() {
  studyLoading.value = true
  try {
    const r = await axios.get('/api/culture/study-task/my-records')
    studyRows.value = r.data.data || []
  } catch (e) { studyRows.value = [] } finally { studyLoading.value = false }
}
async function loadExams() {
  examLoading.value = true
  try {
    const r = await axios.get('/api/culture/exam/my-instances')
    examRows.value = r.data.data || []
  } catch (e) { examRows.value = [] } finally { examLoading.value = false }
}

async function goMid(row) {
  await ElMessageBox.confirm(`模拟跳转培训中台完成课程「${row.record.courseName}」并回写进度100%？`, '中台学习模拟', { type: 'info' })
  await axios.post(`/api/culture/study-task/${row.record.taskId}/sync`, { userName: username, progress: 100 })
  ElMessage.success('中台已回写：进度100%，已完成')
  loadStudy()
}

async function startExam(row) {
  const r = await axios.post(`/api/culture/exam/instance/${row.id}/start`)
  const d = r.data.data
  cur.instanceId = row.id
  cur.paper = { ...d, examName: row.examName }
  Object.keys(answers).forEach(k => delete answers[k])
  Object.keys(answersMulti).forEach(k => delete answersMulti[k])
  d.paper.forEach(q => { if (q.questionType === '多选') answersMulti[q.id] = [] })
  switchCount.value = 0
  remainSec.value = (d.durationMin || 30) * 60
  examVis.value = true
  startTimer()
  installAntiSwitch()
}

function startTimer() {
  stopTimer()
  timer = setInterval(() => {
    remainSec.value--
    if (remainSec.value <= 0) {
      ElMessage.warning('考试时间到，系统自动交卷')
      submitExam(true)
    }
  }, 1000)
}
function stopTimer() { if (timer) { clearInterval(timer); timer = null } }

function installAntiSwitch() {
  if (onVis) document.removeEventListener('visibilitychange', onVis)
  onVis = async () => {
    if (!examVis.value || !cur.paper) return
    if (document.hidden) {
      switchCount.value++
      try {
        await axios.post(`/api/culture/exam/instance/${cur.instanceId}/switch`, { count: switchCount.value })
      } catch (e) { /* 上报失败不中断考试 */ }
      if (cur.paper.antiSwitch === 1 && switchCount.value >= 3) {
        ElMessage.error('切屏已达3次，系统强制交卷')
        submitExam(true)
      }
    }
  }
  document.addEventListener('visibilitychange', onVis)
}

async function submitExam(auto) {
  if (submitting.value) return
  if (!auto) {
    const unanswered = cur.paper.paper.filter(q => {
      if (q.questionType === '多选题') return !(answersMulti[q.id] || []).length
      return !answers[q.id]
    }).length
    if (unanswered > 0) {
      try {
        await ElMessageBox.confirm(`还有 ${unanswered} 题未作答，确认交卷？`, '交卷确认', { type: 'warning' })
      } catch (e) { return }
    }
  }
  submitting.value = true
  try {
    const final = {}
    Object.keys(answers).forEach(k => { final[k] = answers[k] })
    Object.keys(answersMulti).forEach(k => { final[k] = (answersMulti[k] || []).join(',') })
    const r = await axios.post(`/api/culture/exam/instance/${cur.instanceId}/submit`, { answers: final, autoSubmit: auto })
    const d = r.data.data
    cleanup()
    examVis.value = false
    await ElMessageBox.alert(
      `得分：${d.score} 分（及格线见考试配置）\n结果：${d.status}${d.frozen === 1 ? '\n注意：本次不合格，评优/晋升/调薪资格已冻结，补考通过后自动解冻' : ''}`,
      auto ? '自动交卷完成' : '交卷完成',
      { type: d.status.includes('合格') ? 'success' : 'error', confirmButtonText: '知道了' }
    )
    loadExams()
  } finally {
    submitting.value = false
  }
}

function cleanup() {
  stopTimer()
  if (onVis) { document.removeEventListener('visibilitychange', onVis); onVis = null }
}

onMounted(() => { loadStudy(); loadExams() })
onUnmounted(cleanup)
</script>

<style scoped>
.mt { margin-top:10px; }
.ml4 { margin-left:4px; }
.tip { color:#909399; font-size:12px; }
.overdue { color:#f56c6c; font-weight:bold; }
.danger { color:#f56c6c; }
.exam-head { display:flex; gap:24px; align-items:center; padding:0 0 12px; font-size:14px; }
.paper { max-height: calc(100vh - 230px); overflow-y:auto; padding: 4px 8px; }
.q-item { margin-bottom:18px; padding:12px 14px; border:1px solid #ebeef5; border-radius:6px; }
.q-stem { font-weight:bold; margin-bottom:8px; }
.q-opt { display:block; margin:4px 0; }
.exam-foot { text-align:center; padding-top:12px; }
</style>

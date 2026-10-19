<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">状态机：待自评→待核查→待评分→待审定→申诉期→已发布→整改中→已闭环 · 核查意见必填 · 定性分自动去极值均值</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="f.schemeId" placeholder="全部方案" clearable size="small" style="width:200px" @change="load">
          <el-option v-for="s in schemes" :key="s.id" :label="s.schemeName" :value="s.id" />
        </el-select>
        <el-select v-model="f.status" placeholder="状态" clearable size="small" style="width:110px" @change="load">
          <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
        </el-select>
        <el-button size="small" @click="load">查询</el-button>
      </div>

      <el-table :data="rows" size="small" border v-loading="loading">
        <el-table-column prop="id" label="任务ID" width="80" />
        <el-table-column prop="orgName" label="考核对象" width="130" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="80">
          <template #default="{ row }">{{ row.totalScore ?? '—' }}</template>
        </el-table-column>
        <el-table-column prop="adjustScore" label="调整分" width="80">
          <template #default="{ row }">{{ row.adjustScore ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="等级" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.grade" size="small" :type="gradeType(row.grade)">{{ row.grade }}</el-tag>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="一票否决" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.vetoTriggered===1" size="small" type="danger">命中</el-tag>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="appealDeadline" label="申诉截止" width="100" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('R02','R04','R07') && row.status==='待自评'" size="small" link type="primary" @click="openSelf(row)">自评</el-button>
            <el-button v-if="can('R02','R04') && row.status==='待核查'" size="small" link type="primary" @click="openCheck(row)">核查</el-button>
            <el-button v-if="can('R02','R04') && row.status==='待评分'" size="small" link type="primary" @click="openScore(row)">评分</el-button>
            <el-button v-if="can('R02','R03','R04') && row.status==='待审定'" size="small" link type="success" @click="openApprove(row)">审定</el-button>
            <el-button v-if="can('R02','R07') && row.status==='申诉期'" size="small" link type="warning" @click="openAppeal(row)">申诉</el-button>
            <el-button size="small" link @click="openAppeals(row)">申诉记录</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 自评抽屉 -->
    <el-drawer v-model="selfVisible" :title="`自评填报 - ${current?.orgName || ''}`" size="640px">
      <el-alert type="info" :closable="false" show-icon title="逐项填写自评分与说明，佐证材料可粘贴链接或说明；全部指标填写后可提交" style="margin-bottom:12px" />
      <el-table :data="selfRows" size="small" border>
        <el-table-column prop="indicatorName" label="指标" min-width="150" show-overflow-tooltip />
        <el-table-column prop="score" label="满分" width="70" />
        <el-table-column label="自评分" width="110">
          <template #default="{ row }">
            <el-input-number v-model="row.selfScore" :min="0" :max="row.score" :precision="2" size="small" style="width:100px" />
          </template>
        </el-table-column>
        <el-table-column label="自评说明" min-width="140">
          <template #default="{ row }"><el-input v-model="row.selfDesc" size="small" /></template>
        </el-table-column>
        <el-table-column label="佐证" min-width="120">
          <template #default="{ row }"><el-input v-model="row.evidence" size="small" placeholder="链接/说明" /></template>
        </el-table-column>
      </el-table>
      <div class="drawer-footer">
        <el-button @click="selfVisible=false">取消</el-button>
        <el-button @click="saveSelf(false)">暂存</el-button>
        <el-button type="primary" @click="saveSelf(true)">提交自评</el-button>
      </div>
    </el-drawer>

    <!-- 核查抽屉 -->
    <el-drawer v-model="checkVisible" :title="`核查 - ${current?.orgName || ''}`" size="680px">
      <el-alert type="warning" :closable="false" show-icon title="定量指标可自动取数；核查意见必填；命中一票否决将直接判定不合格" style="margin-bottom:12px" />
      <el-table :data="checkRows" size="small" border>
        <el-table-column prop="indicatorName" label="指标" min-width="140" show-overflow-tooltip />
        <el-table-column prop="scoreMode" label="方式" width="70" />
        <el-table-column label="取数" width="90">
          <template #default="{ row }">
            <el-button v-if="row.scoreMode==='定量'" size="small" link type="primary" @click="autoFetch(row)">自动取数</el-button>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="数据值" width="90">
          <template #default="{ row }"><el-input-number v-model="row.dataValue" :precision="2" size="small" style="width:80px" /></template>
        </el-table-column>
        <el-table-column label="核查分" width="90">
          <template #default="{ row }"><el-input-number v-model="row.checkScore" :min="0" :precision="2" size="small" style="width:80px" /></template>
        </el-table-column>
        <el-table-column label="否决" width="60">
          <template #default="{ row }">
            <el-checkbox v-model="row.vetoHit" :true-value="1" :false-value="0" :disabled="row.vetoFlag!==1" />
          </template>
        </el-table-column>
        <el-table-column label="核查意见(必填)" min-width="140">
          <template #default="{ row }"><el-input v-model="row.checkOpinion" size="small" /></template>
        </el-table-column>
      </el-table>
      <div class="drawer-footer">
        <el-button @click="checkVisible=false">取消</el-button>
        <el-button @click="saveCheck(false)">暂存</el-button>
        <el-button type="primary" @click="saveCheck(true)">完成核查</el-button>
      </div>
    </el-drawer>

    <!-- 评分抽屉 -->
    <el-drawer v-model="scoreVisible" :title="`评分 - ${current?.orgName || ''}`" size="700px">
      <el-alert type="info" :closable="false" show-icon title="定性指标输入评委分（逗号分隔），系统自动去极值取均值；加减分直接录入；总分与等级实时预览" style="margin-bottom:12px" />
      <el-table :data="scoreRows" size="small" border>
        <el-table-column prop="indicatorName" label="指标" min-width="140" show-overflow-tooltip />
        <el-table-column prop="scoreMode" label="方式" width="70" />
        <el-table-column prop="autoScore" label="自动分" width="80">
          <template #default="{ row }">{{ row.autoScore ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="评委分(逗号分隔)" min-width="150">
          <template #default="{ row }">
            <el-input v-if="row.scoreMode==='定性'" v-model="row.judgeScores" size="small" placeholder="如 85,90,88,92,78" @input="previewTrim(row)" />
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="去极值均值" width="90">
          <template #default="{ row }"><el-tag v-if="row.trimPreview!=null" size="small">{{ row.trimPreview }}</el-tag></template>
        </el-table-column>
        <el-table-column label="最终分" width="100">
          <template #default="{ row }"><el-input-number v-model="row.finalScore" :precision="2" size="small" style="width:90px" /></template>
        </el-table-column>
      </el-table>
      <div class="score-summary">
        <span>总分：<b>{{ totalPreview }}</b></span>
        <span>等级预览：<el-tag size="small" :type="gradeType(gradePreview)">{{ gradePreview }}</el-tag></span>
        <span>调整分：<el-input-number v-model="adjustScore" :precision="2" size="small" style="width:100px" /></span>
      </div>
      <div class="drawer-footer">
        <el-button @click="scoreVisible=false">取消</el-button>
        <el-button type="primary" @click="finishScore">完成评分</el-button>
      </div>
    </el-drawer>

    <!-- 审定 -->
    <el-dialog v-model="approveVisible" title="结果审定" width="440px">
      <el-form label-width="80px">
        <el-form-item label="对象">{{ current?.orgName }}</el-form-item>
        <el-form-item label="总分">{{ current?.totalScore }}</el-form-item>
        <el-form-item label="审定人" required><el-input v-model="approver" /></el-form-item>
        <el-form-item label="审定意见"><el-input v-model="approveOpinion" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible=false">取消</el-button>
        <el-button type="success" @click="doApprove">审定通过</el-button>
      </template>
    </el-dialog>

    <!-- 申诉 -->
    <el-dialog v-model="appealVisible" title="提交申诉" width="440px">
      <el-form label-width="80px">
        <el-form-item label="对象">{{ current?.orgName }}</el-form-item>
        <el-form-item label="申诉理由" required><el-input v-model="appealReason" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appealVisible=false">取消</el-button>
        <el-button type="warning" @click="doAppeal">提交申诉</el-button>
      </template>
    </el-dialog>

    <!-- 申诉处理 -->
    <el-dialog v-model="appealsVisible" title="申诉记录与处理" width="720px">
      <el-table :data="appeals" size="small" border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="reason" label="申诉理由" min-width="160" show-overflow-tooltip />
        <el-table-column prop="scoreBefore" label="原分" width="70" />
        <el-table-column prop="scoreAfter" label="改后" width="70" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status==='已处理'?'success':'warning'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handleResult" label="处理结果" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button v-if="can('R02','R03','R04') && row.status!=='已处理'" size="small" link type="primary" @click="openHandle(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="handleVisible" title="处理申诉" width="480px">
      <el-form label-width="90px">
        <el-form-item label="是否支持">
          <el-radio-group v-model="handleForm.upheld">
            <el-radio-button :value="true">支持(改分)</el-radio-button>
            <el-radio-button :value="false">驳回</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="新分数" v-if="handleForm.upheld"><el-input-number v-model="handleForm.newScore" :precision="2" /></el-form-item>
        <el-form-item label="处理结果" required><el-input v-model="handleForm.handleResult" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="处理人" required><el-input v-model="handleForm.handleBy" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible=false">取消</el-button>
        <el-button type="primary" @click="doHandle">提交处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/assessment/process'
const statuses = ['待自评', '待核查', '待评分', '待审定', '申诉期', '已发布', '整改中', '已闭环']

const loading = ref(false)
const rows = ref([])
const schemes = ref([])
const indicators = ref([])
const f = reactive({ schemeId: null, status: null })
const current = ref(null)

const selfVisible = ref(false)
const selfRows = ref([])
const checkVisible = ref(false)
const checkRows = ref([])
const scoreVisible = ref(false)
const scoreRows = ref([])
const adjustScore = ref(0)
const approveVisible = ref(false)
const approver = ref('')
const approveOpinion = ref('')
const appealVisible = ref(false)
const appealReason = ref('')
const appealsVisible = ref(false)
const appeals = ref([])
const handleVisible = ref(false)
const handleForm = reactive({ id: null, upheld: true, newScore: null, handleResult: '', handleBy: '' })

function statusType(s) {
  return { '待自评': 'info', '待核查': 'warning', '待评分': 'warning', '待审定': 'primary', '申诉期': 'danger', '已发布': 'success', '整改中': 'danger', '已闭环': 'info' }[s] || 'info'
}
function gradeType(g) {
  return { '优秀': 'success', '良好': 'primary', '合格': 'warning', '不合格': 'danger' }[g] || 'info'
}

async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/task/list`, { params: { ...f } })
    if (res.data.code === 200) rows.value = res.data.data
  } finally { loading.value = false }
}
async function loadMeta() {
  const [s, i] = await Promise.all([
    axios.get('/api/party/assessment/scheme/list'),
    axios.get('/api/party/assessment/indicator/list', { params: { status: '启用' } })
  ])
  schemes.value = s.data.data || []
  indicators.value = i.data.data || []
}
const currentScheme = computed(() => schemes.value.find(s => s.id === (current.value && current.value.schemeId)) || {})

// ---------- 自评 ----------
async function openSelf(row) {
  current.value = row
  const res = await axios.get(`${api}/self-eval/list`, { params: { taskId: row.id } })
  const existing = res.data.data || []
  selfRows.value = indicators.value.map(ind => {
    const ex = existing.find(e => e.indicatorId === ind.id) || {}
    return { indicatorId: ind.id, indicatorName: ind.indicatorName, score: ind.score, selfScore: ex.selfScore ?? null, selfDesc: ex.selfDesc || '', evidence: ex.evidence || '' }
  })
  selfVisible.value = true
}
async function saveSelf(submit) {
  try {
    for (const r of selfRows.value.filter(x => x.selfScore != null)) {
      await axios.post(`${api}/self-eval/save`, { indicatorId: r.indicatorId, selfScore: r.selfScore, selfDesc: r.selfDesc, evidence: r.evidence, submitBy: '自评人' }, { params: { taskId: current.value.id } })
    }
    if (submit) {
      await axios.post(`${api}/${current.value.id}/submit-self`, null, { params: { submitBy: '自评人' } })
      ElMessage.success('自评已提交，进入待核查')
    } else {
      ElMessage.success('已暂存')
    }
    selfVisible.value = false
    load()
  } catch {}
}

// ---------- 核查 ----------
async function openCheck(row) {
  current.value = row
  const res = await axios.get(`${api}/check/list`, { params: { taskId: row.id } })
  const existing = res.data.data || []
  checkRows.value = indicators.value.map(ind => {
    const ex = existing.find(e => e.indicatorId === ind.id) || {}
    return { indicatorId: ind.id, indicatorName: ind.indicatorName, scoreMode: ind.scoreMode, vetoFlag: ind.vetoFlag, dataValue: ex.dataValue ?? null, checkScore: ex.checkScore ?? null, vetoHit: ex.vetoHit ?? 0, checkOpinion: ex.checkOpinion || '' }
  })
  checkVisible.value = true
}
async function autoFetch(r) {
  try {
    const res = await axios.get(`${api}/check/auto-fetch`, { params: { taskId: current.value.id, indicatorId: r.indicatorId } })
    r.dataValue = res.data.data.value
    r.checkScore = res.data.data.score
    ElMessage.success(`已取数：${r.dataValue} → ${r.checkScore}分`)
  } catch {}
}
async function saveCheck(finish) {
  if (finish && checkRows.value.some(r => r.checkScore != null && !r.checkOpinion)) {
    ElMessage.warning('核查意见为必填项')
    return
  }
  try {
    for (const r of checkRows.value.filter(x => x.checkScore != null)) {
      await axios.post(`${api}/check/save`, { indicatorId: r.indicatorId, dataValue: r.dataValue, checkScore: r.checkScore, checkOpinion: r.checkOpinion, vetoHit: r.vetoHit, checkBy: '核查人' }, { params: { taskId: current.value.id } })
    }
    if (finish) {
      await axios.post(`${api}/${current.value.id}/finish-check`)
      ElMessage.success('核查完成，进入待评分')
    } else {
      ElMessage.success('已暂存')
    }
    checkVisible.value = false
    load()
  } catch {}
}

// ---------- 评分 ----------
async function openScore(row) {
  current.value = row
  const res = await axios.get(`${api}/score/list`, { params: { taskId: row.id } })
  const existing = res.data.data || []
  scoreRows.value = indicators.value.map(ind => {
    const ex = existing.find(e => e.indicatorId === ind.id) || {}
    return { indicatorId: ind.id, indicatorName: ind.indicatorName, scoreMode: ind.scoreMode, autoScore: ex.autoScore ?? null, judgeScores: ex.judgeScores || '', finalScore: ex.finalScore ?? null, trimPreview: null }
  })
  adjustScore.value = row.adjustScore || 0
  scoreVisible.value = true
}
async function previewTrim(r) {
  if (!r.judgeScores) { r.trimPreview = null; return }
  try {
    const res = await axios.get('/api/party/assessment/algorithm/trim-mean', { params: { values: r.judgeScores } })
    r.trimPreview = res.data.data.mean
    r.finalScore = res.data.data.mean
  } catch { r.trimPreview = null }
}
const totalPreview = computed(() => {
  const sum = scoreRows.value.reduce((a, r) => a + (Number(r.finalScore) || 0), 0) + (Number(adjustScore.value) || 0)
  return sum.toFixed(2)
})
const gradePreview = computed(() => {
  const s = Number(totalPreview.value)
  const sch = currentScheme.value
  if (sch.excellentLine != null && s >= sch.excellentLine) return '优秀'
  if (sch.goodLine != null && s >= sch.goodLine) return '良好'
  if (sch.passLine != null && s >= sch.passLine) return '合格'
  return '不合格'
})
async function finishScore() {
  try {
    const body = scoreRows.value.map(r => ({ indicatorId: r.indicatorId, scoreMode: r.scoreMode, autoScore: r.autoScore, judgeScores: r.judgeScores, finalScore: r.finalScore }))
    await axios.post(`${api}/${current.value.id}/finish-score`, body, { params: { adjustScore: adjustScore.value } })
    ElMessage.success('评分完成，进入待审定')
    scoreVisible.value = false
    load()
  } catch {}
}

// ---------- 审定 ----------
function openApprove(row) { current.value = row; approver.value = ''; approveOpinion.value = ''; approveVisible.value = true }
async function doApprove() {
  try {
    await axios.post(`${api}/${current.value.id}/approve`, null, { params: { approver: approver.value, opinion: approveOpinion.value } })
    ElMessage.success('审定完成，进入申诉期')
    approveVisible.value = false
    load()
  } catch {}
}

// ---------- 申诉 ----------
function openAppeal(row) { current.value = row; appealReason.value = ''; appealVisible.value = true }
async function doAppeal() {
  try {
    await axios.post(`${api}/${current.value.id}/appeal`, null, { params: { reason: appealReason.value } })
    ElMessage.success('申诉已提交')
    appealVisible.value = false
  } catch {}
}
async function openAppeals(row) {
  current.value = row
  const res = await axios.get(`${api}/appeal/list`, { params: { taskId: row.id } })
  appeals.value = res.data.data || []
  appealsVisible.value = true
}
function openHandle(row) { Object.assign(handleForm, { id: row.id, upheld: true, newScore: null, handleResult: '', handleBy: '' }); handleVisible.value = true }
async function doHandle() {
  try {
    await axios.post(`${api}/appeal/${handleForm.id}/handle`, null, { params: { upheld: handleForm.upheld, newScore: handleForm.newScore, handleResult: handleForm.handleResult, handleBy: handleForm.handleBy } })
    ElMessage.success('申诉已处理')
    handleVisible.value = false
    openAppeals(current.value)
    load()
  } catch {}
}

onMounted(async () => { await loadMeta(); load() })
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.muted { color: #c0c4cc; }
.drawer-footer { display: flex; justify-content: flex-end; gap: 8px; margin-top: 14px; }
.score-summary { display: flex; gap: 20px; align-items: center; margin-top: 12px; padding: 10px; background: #f4f7fb; border-radius: 6px; }
</style>

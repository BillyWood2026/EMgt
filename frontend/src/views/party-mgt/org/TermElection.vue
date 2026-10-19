<template>
  <div class="page-container">
    <RoleBar />

    <el-card shadow="never">
      <template #header>
        <div class="bar">
          <span class="title">换届选举管理</span>
          <div>
            <el-select v-model="filterOrg" placeholder="全部组织" clearable size="small" style="width:220px"
                       @change="loadList">
              <el-option v-for="o in orgOptions" :key="o.id" :label="o.name" :value="o.id" />
            </el-select>
            <el-select v-model="filterYear" placeholder="年度" clearable size="small" style="width:100px;margin-left:8px"
                       @change="loadList">
              <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
            </el-select>
            <el-button v-if="can('R02','R04')" size="small" type="primary" style="margin-left:8px"
                       @click="openWizard()">发起换届</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" size="small" border v-loading="loading">
        <el-table-column prop="id" label="编号" width="60" />
        <el-table-column prop="orgName" label="党组织" min-width="200" show-overflow-tooltip />
        <el-table-column prop="termNo" label="届次" width="70">
          <template #default="{ row }">{{ row.termNo ? '第' + row.termNo + '届' : '—' }}</template>
        </el-table-column>
        <el-table-column prop="electionMode" label="方式" width="90" />
        <el-table-column prop="electionDate" label="选举日期" width="115" />
        <el-table-column label="阶段" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="stageType(row.stage)">{{ stageLabel(row.stage) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === '进行中' ? 'warning' : 'success'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="openWizard(row.id)">
              {{ row.stage === 'FILED' ? '查看' : '继续办理' }}
            </el-button>
            <el-button v-if="row.stage === 'FILED' && can('R03')" size="small" link type="warning"
                       @click="audit(row)">上级审定</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 四步向导 -->
    <el-dialog v-model="wizardVisible" :title="election.id ? '换届选举办理向导' : '发起换届选举'"
               width="900px" :close-on-click-modal="false" top="5vh">
      <el-steps :active="step" finish-status="success" align-center class="steps">
        <el-step title="选举方案" />
        <el-step title="候选人/监票人" />
        <el-step title="会议计票" />
        <el-step title="结果备案" />
      </el-steps>

      <!-- 第一步 方案 -->
      <div v-show="step === 0" class="step-body">
        <el-form :model="plan" label-width="110px">
          <el-row :gutter="10">
            <el-col :span="12">
              <el-form-item label="党组织" required>
                <el-tree-select v-model="plan.orgId" :data="orgTree" :props="{ label: 'name', value: 'id' }"
                                check-strictly style="width:100%" :disabled="!!election.id" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="届次"><el-input-number v-model="plan.termNo" :min="1" controls-position="right" style="width:100%" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="选举方式" required>
                <el-radio-group v-model="plan.electionMode">
                  <el-radio value="差额选举">差额选举</el-radio>
                  <el-radio value="等额选举">等额选举</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="选举日期"><el-date-picker v-model="plan.electionDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
            </el-col>
            <el-col :span="24"><el-form-item label="会议地点"><el-input v-model="plan.meetingLocation" /></el-form-item></el-col>
            <el-col :span="24">
              <el-form-item label="提前换届">
                <el-switch v-model="plan.earlyApproval" />
                <span class="hint">提前换届必须已报上级党组织批准</span>
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="plan.earlyApproval">
              <el-form-item label="批准件编号/名称" required><el-input v-model="plan.earlyApprovalFile" /></el-form-item>
            </el-col>
            <el-col :span="12"><el-form-item label="公示起"><el-date-picker v-model="plan.publicityStart" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="公示止"><el-date-picker v-model="plan.publicityEnd" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
            <el-col :span="24"><el-form-item label="方案说明"><el-input v-model="plan.planData" type="textarea" :rows="3" /></el-form-item></el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 第二步 候选人/监票人 -->
      <div v-show="step === 1" class="step-body">
        <div class="sub-bar">
          <span><b>候选人名单</b>（差额选举候选人数应多于应选人数；书记候选人党龄≥1年，党委委员≥5年，系统自动校验）</span>
        </div>
        <el-table :data="candidates" size="small" border>
          <el-table-column label="序" width="50">
            <template #default="{ $index }">
              <el-button link size="small" @click="moveCandidate($index, -1)">↑</el-button>
              <el-button link size="small" @click="moveCandidate($index, 1)">↓</el-button>
            </template>
          </el-table-column>
          <el-table-column label="姓名" width="130">
            <template #default="{ row }"><el-input v-model="row.memberName" size="small" placeholder="党员姓名" /></template>
          </el-table-column>
          <el-table-column label="拟任职务" width="130">
            <template #default="{ row }">
              <el-select v-model="row.position" size="small">
                <el-option label="书记" value="书记" /><el-option label="副书记" value="副书记" /><el-option label="委员" value="委员" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="党龄(年)" width="100">
            <template #default="{ row }"><el-input-number v-model="row.partyAgeYears" :min="0" size="small" controls-position="right" style="width:90px" /></template>
          </el-table-column>
          <el-table-column label="操作" width="70">
            <template #default="{ $index }">
              <el-button link type="danger" size="small" @click="candidates.splice($index, 1)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button size="small" class="add-btn" @click="addCandidate">添加候选人</el-button>

        <div class="sub-bar" style="margin-top:14px"><b>监票人/计票人</b>（必须不是候选人，系统自动校验）</div>
        <el-table :data="witnesses" size="small" border>
          <el-table-column label="姓名" width="180">
            <template #default="{ row }"><el-input v-model="row.memberName" size="small" /></template>
          </el-table-column>
          <el-table-column label="角色" width="180">
            <template #default="{ row }">
              <el-select v-model="row.witnessType" size="small">
                <el-option label="监票人" value="监票人" /><el-option label="计票人" value="计票人" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button link type="danger" size="small" @click="witnesses.splice($index, 1)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button size="small" class="add-btn" @click="witnesses.push({ memberName: '', witnessType: '监票人' })">添加</el-button>
        <p class="hint">应到会有表决权人数：<b>{{ shouldVote ?? '—' }}</b>
          <el-button link size="small" @click="refreshShouldVote">刷新</el-button>（自动排除预备党员、暂停表决权人员）</p>
      </div>

      <!-- 第三步 计票 -->
      <div v-show="step === 2" class="step-body">
        <el-form label-width="130px" class="tally-form">
          <el-row :gutter="10">
            <el-col :span="8"><el-form-item label="应到会人数"><el-input-number v-model="tally.shouldVoteCount" disabled controls-position="right" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="实到会人数"><el-input-number v-model="tally.actualCount" :min="0" controls-position="right" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="发出选票"><el-input-number v-model="tally.ballotsIssued" :min="0" controls-position="right" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="收回选票"><el-input-number v-model="tally.ballotsReceived" :min="0" controls-position="right" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="有效票"><el-input-number v-model="tally.validVotes" :min="0" controls-position="right" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="无效票"><el-input-number v-model="tally.invalidVotes" :min="0" controls-position="right" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="弃权票"><el-input-number v-model="tally.blankVotes" :min="0" controls-position="right" /></el-form-item></el-col>
          </el-row>
        </el-form>
        <el-alert v-for="(t, i) in tallyHints" :key="i" :title="t.msg" :type="t.ok ? 'success' : 'error'"
                  :closable="false" show-icon class="warn-item" />
        <el-table :data="candidateVotes" size="small" border style="margin-top:8px">
          <el-table-column prop="memberName" label="候选人" width="140" />
          <el-table-column prop="position" label="职务" width="90" />
          <el-table-column label="得票数" width="160">
            <template #default="{ row }"><el-input-number v-model="row.votes" :min="0" controls-position="right" size="small" /></template>
          </el-table-column>
          <el-table-column label="当选校验">
            <template #default="{ row }">
              <el-tag v-if="row.position && tally.shouldVoteCount && row.votes * 2 > tally.shouldVoteCount" size="small" type="success">过半，可当选{{ row.position }}</el-tag>
              <el-tag v-else-if="row.position" size="small" type="danger">未过半数</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 第四步 备案 -->
      <div v-show="step === 3" class="step-body">
        <el-form :model="fileForm" label-width="130px">
          <el-row :gutter="10">
            <el-col :span="12"><el-form-item label="公示开始"><el-date-picker v-model="fileForm.publicityStart" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="公示结束"><el-date-picker v-model="fileForm.publicityEnd" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
            <el-col :span="24">
              <el-form-item label="公示无异议确认" required>
                <el-checkbox v-model="fileForm.confirmedPublicity">已按规定公示且公示结果无异议</el-checkbox>
              </el-form-item>
            </el-col>
            <el-col :span="12"><el-form-item label="上级批复文号" required><el-input v-model="fileForm.approvalNo" placeholder="××党发〔2026〕×号" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="备案日期" required><el-date-picker v-model="fileForm.recordDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          </el-row>
        </el-form>
        <el-alert type="info" :closable="false" show-icon class="warn-item"
                  title="备案后自动回写党组织届次、任期起止、书记/委员与正常状态，并写入组织沿革。党委任期5年、其他3年。" />
      </div>

      <template #footer>
        <el-button @click="wizardVisible = false">关闭</el-button>
        <el-button v-if="step > 0" @click="step--">上一步</el-button>
        <el-button v-if="step < 2 && election.id" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="step === 0" type="primary" @click="saveStep0">{{ election.id ? '下一步' : '创建并下一步' }}</el-button>
        <el-button v-if="step === 1" type="primary" @click="saveStep2">保存并下一步</el-button>
        <el-button v-if="step === 2" type="primary" @click="saveStep3">保存计票并下一步</el-button>
        <el-button v-if="step === 3 && can('R02')" type="success" @click="saveStep4">结果备案</el-button>
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

const API = '/api/party/org/election'
const loading = ref(false)
const list = ref([])
const filterOrg = ref(null)
const filterYear = ref(null)
const orgTree = ref([])
const orgOptions = ref([])
const yearOptions = [2024, 2025, 2026, 2027]

const wizardVisible = ref(false)
const step = ref(0)
const election = reactive({ id: null })
const plan = reactive({})
const candidates = ref([])
const witnesses = ref([])
const shouldVote = ref(null)
const tally = reactive({ shouldVoteCount: null, actualCount: null, ballotsIssued: null,
  ballotsReceived: null, validVotes: null, invalidVotes: null, blankVotes: null })
const fileForm = reactive({ publicityStart: null, publicityEnd: null, confirmedPublicity: false,
  approvalNo: '', recordDate: null })

async function loadList() {
  loading.value = true
  try {
    const res = await axios.get(`${API}/list`, {
      params: { orgId: filterOrg.value || undefined, year: filterYear.value || undefined }
    })
    if (res.data.code === 200) list.value = res.data.data
  } finally {
    loading.value = false
  }
}

async function loadOrgs() {
  const res = await axios.get('/api/party/org/organization/tree')
  if (res.data.code === 200) {
    orgTree.value = res.data.data
    const flat = []
    const walk = nodes => nodes.forEach(n => { flat.push(n); if (n.children) walk(n.children) })
    // 懒加载树根不含 children，备选列表用各组织平铺不可得，仅展示根级；选择以树选择器为主
    walk(res.data.data)
    orgOptions.value = flat
  }
}

function stageLabel(s) {
  return { PLAN: '选举方案', CANDIDATE: '候选人', MEETING: '会议计票', FILED: '已备案' }[s] || '方案'
}
function stageType(s) {
  return { PLAN: 'info', CANDIDATE: '', MEETING: 'warning', FILED: 'success' }[s] || 'info'
}
function stageIndex(s) {
  return { PLAN: 0, CANDIDATE: 1, MEETING: 2, FILED: 3 }[s] ?? 0
}

async function openWizard(id) {
  resetWizard()
  if (id) {
    const res = await axios.get(`${API}/${id}`)
    if (res.data.code !== 200) return
    const d = res.data.data
    election.id = id
    Object.assign(plan, d.election)
    candidates.value = (d.candidates || []).map(c => ({ ...c }))
    witnesses.value = (d.witnesses || []).map(w => ({ ...w }))
    shouldVote.value = d.election.shouldVoteCount
    tally.shouldVoteCount = d.election.shouldVoteCount
    tally.actualCount = d.election.actualCount
    tally.ballotsIssued = d.election.ballotsIssued
    tally.ballotsReceived = d.election.ballotsReceived
    tally.validVotes = d.election.validVotes
    tally.invalidVotes = d.election.invalidVotes
    tally.blankVotes = d.election.blankVotes
    Object.assign(fileForm, {
      publicityStart: d.election.publicityStart, publicityEnd: d.election.publicityEnd,
      confirmedPublicity: false, approvalNo: d.election.approvalNo || '', recordDate: d.election.recordDate
    })
    step.value = d.election.stage === 'FILED' ? 3 : Math.max(stageIndex(d.election.stage) - 1, 0)
    if (d.election.stage === 'PLAN') step.value = 0
  }
  wizardVisible.value = true
}

function resetWizard() {
  election.id = null
  Object.keys(plan).forEach(k => delete plan[k])
  Object.assign(plan, { electionMode: '差额选举', earlyApproval: false })
  candidates.value = []
  witnesses.value = []
  shouldVote.value = null
  Object.assign(tally, { shouldVoteCount: null, actualCount: null, ballotsIssued: null,
    ballotsReceived: null, validVotes: null, invalidVotes: null, blankVotes: null })
  Object.assign(fileForm, { publicityStart: null, publicityEnd: null, confirmedPublicity: false,
    approvalNo: '', recordDate: null })
  step.value = 0
}

function nextStep() { step.value++ }

async function saveStep0() {
  if (!plan.orgId) { ElMessage.warning('请选择党组织'); return }
  if (plan.earlyApproval && !plan.earlyApprovalFile) {
    ElMessage.error('提前换届必须登记上级批准件'); return
  }
  try {
    let res
    if (election.id) {
      res = await axios.put(`${API}/plan/${election.id}`, plan)
    } else {
      res = await axios.post(`${API}/plan`, plan)
    }
    if (res.data.code === 200) {
      election.id = res.data.data.id
      ElMessage.success('方案已保存')
      step.value = 1
    }
  } catch { /* 拦截器提示 */ }
}

function addCandidate() {
  candidates.value.push({ memberName: '', position: '委员', partyAgeYears: null, votes: 0 })
}
function moveCandidate(i, dir) {
  const j = i + dir
  if (j < 0 || j >= candidates.value.length) return
  const arr = candidates.value
  ;[arr[i], arr[j]] = [arr[j], arr[i]]
}

async function saveStep2() {
  const names = candidates.value.map(c => c.memberName?.trim()).filter(Boolean)
  if (!names.length) { ElMessage.warning('至少登记一名候选人'); return }
  if (new Set(names).size !== names.length) { ElMessage.error('候选人存在重复'); return }
  try {
    await axios.post(`${API}/candidates/${election.id}`, {
      candidates: candidates.value.map((c, i) => ({ ...c, sortOrder: i + 1 }))
    })
    await axios.post(`${API}/witnesses/${election.id}`, { witnesses: witnesses.value })
    ElMessage.success('候选人和监票人已保存')
    await refreshShouldVote()
    step.value = 2
  } catch { /* 拦截器提示，如党龄不足、监票人是候选人 */ }
}

async function refreshShouldVote() {
  const res = await axios.post(`${API}/should-vote/${election.id}`)
  if (res.data.code === 200) {
    shouldVote.value = res.data.data
    tally.shouldVoteCount = res.data.data
  }
}

const candidateVotes = computed(() => candidates.value)

const tallyHints = computed(() => {
  const t = tally
  const hints = []
  const num = v => Number(v) || 0
  if (t.shouldVoteCount) {
    hints.push({ ok: num(t.actualCount) * 2 > num(t.shouldVoteCount),
      msg: `实到会人数${num(t.actualCount)}，应到会${t.shouldVoteCount}：${num(t.actualCount) * 2 > num(t.shouldVoteCount) ? '超过半数，会议有效' : '未超过半数，选举无效（E-ELEC-02）'}` })
  }
  hints.push({ ok: num(t.ballotsReceived) <= num(t.ballotsIssued),
    msg: `收回选票${num(t.ballotsReceived)} / 发出${num(t.ballotsIssued)}：${num(t.ballotsReceived) <= num(t.ballotsIssued) ? '票箱平衡' : '收回多于发出（E-ELEC-03 阻断）'}` })
  const sum = num(t.validVotes) + num(t.invalidVotes) + num(t.blankVotes)
  hints.push({ ok: sum === num(t.ballotsReceived),
    msg: `有效${num(t.validVotes)}+无效${num(t.invalidVotes)}+弃权${num(t.blankVotes)}=${sum}，收回${num(t.ballotsReceived)}：${sum === num(t.ballotsReceived) ? '一致' : '不一致（E-ELEC-03 阻断）'}` })
  const voteSum = candidateVotes.value.reduce((s, c) => s + num(c.votes), 0)
  hints.push({ ok: voteSum <= num(t.validVotes),
    msg: `候选人得票合计${voteSum}，有效票${num(t.validVotes)}：${voteSum <= num(t.validVotes) ? '未超过有效票' : '得票合计超过有效票（E-ELEC-03 阻断）'}` })
  return hints
})

async function saveStep3() {
  try {
    await axios.post(`${API}/tally/${election.id}`, {
      actualCount: tally.actualCount, ballotsIssued: tally.ballotsIssued,
      ballotsReceived: tally.ballotsReceived, validVotes: tally.validVotes,
      invalidVotes: tally.invalidVotes, blankVotes: tally.blankVotes, tallyDone: 1,
      votes: candidates.value.map(c => ({ memberName: c.memberName, position: c.position, votes: Number(c.votes) || 0 }))
    })
    ElMessage.success('计票结果已保存')
    step.value = 3
    loadList()
  } catch { /* 拦截器提示 */ }
}

async function saveStep4() {
  if (!fileForm.confirmedPublicity) { ElMessage.warning('请确认公示无异议'); return }
  if (!fileForm.approvalNo || !fileForm.recordDate) { ElMessage.warning('批复文号和备案日期必填'); return }
  try {
    await axios.post(`${API}/file/${election.id}`, { ...fileForm })
    ElMessage.success('备案完成，组织任期与班子已回写')
    wizardVisible.value = false
    loadList()
  } catch { /* 拦截器提示 */ }
}

async function audit(row) {
  // 简化：合格即审定通过
  try {
    await axios.post(`${API}/audit/${row.id}`, { pass: true, opinion: '上级党委审定同意' })
    ElMessage.success('已审定')
    loadList()
  } catch { /* 拦截器提示 */ }
}

onMounted(() => {
  loadList()
  loadOrgs()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.bar { display: flex; justify-content: space-between; align-items: center; }
.title { font-weight: 600; }
.steps { margin: 8px 0 20px; }
.step-body { min-height: 320px; }
.sub-bar { margin-bottom: 8px; color: #5a6a7e; font-size: 13px; }
.add-btn { margin-top: 8px; }
.hint { color: #909399; font-size: 12px; margin-left: 8px; }
.warn-item { margin-bottom: 6px; }
.tally-form :deep(.el-input-number) { width: 100%; }
</style>

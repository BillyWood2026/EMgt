<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">四维加权 · 优秀比例上限 · 预备党员不定等次</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="filter.orgId" placeholder="全部组织" clearable size="small" filterable
                   style="width:230px" @change="loadPlans">
          <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
        </el-select>
        <el-select v-model="filter.year" placeholder="年度" clearable size="small" style="width:110px" @change="loadPlans">
          <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
        </el-select>
        <el-button v-if="can('R02')" size="small" type="primary" @click="openPlan()">新建评议计划</el-button>
      </div>

      <el-table :data="plans" size="small" border v-loading="loading">
        <el-table-column prop="orgName" label="评议组织" min-width="200" show-overflow-tooltip />
        <el-table-column prop="assessYear" label="年度" width="80" />
        <el-table-column prop="targetScope" label="对象范围" width="150" show-overflow-tooltip />
        <el-table-column prop="evalMode" label="方式" width="80" />
        <el-table-column label="优秀上限" width="90">
          <template #default="{ row }">{{ ratioPercent(row.excellentRatio) }}%</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="planStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '草稿' && can('R02','R07')"
                       size="small" link type="success" @click="startPlan(row)">启动评议</el-button>
            <el-button size="small" link type="primary" @click="openWorkspace(row)">进入工作台</el-button>
            <el-button v-if="can('R02')" size="small" link @click="openPlan(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- ============ 评议工作台 ============ -->
    <el-dialog v-model="wsVisible" :title="`${wsPlan.orgName || ''} ${wsPlan.assessYear || ''}年度民主评议工作台`"
               width="1180px" top="4vh" @opened="loadWorkspace">
      <div class="ws-head">
        <el-tag :type="planStatusType(wsPlan.status)">{{ wsPlan.status }}</el-tag>
        <span class="tip">优秀比例上限 {{ ratioPercent(wsPlan.excellentRatio) }}%（{{ weightsText }}）</span>
        <div class="ws-actions">
          <el-button v-if="can('R02','R07')" size="small" type="warning" @click="submitBranch">提交支部评定</el-button>
          <el-button v-if="can('R02','R07')" size="small" type="success" @click="publishPlan">结果公示</el-button>
          <el-button v-if="can('R02','R07')" size="small" @click="feedbackPlan">反馈本人</el-button>
        </div>
      </div>

      <el-row :gutter="10" class="prog-row">
        <el-col :span="5"><div class="prog"><b>{{ progress.total || 0 }}</b><span>参评对象</span></div></el-col>
        <el-col :span="5"><div class="prog"><b>{{ progress.selfRate || 0 }}%</b><span>自评完成({{ progress.selfDone || 0 }}人)</span></div></el-col>
        <el-col :span="5"><div class="prog"><b>{{ progress.mutualRate || 0 }}%</b><span>测评打分({{ progress.scored || 0 }}人)</span></div></el-col>
        <el-col :span="9">
          <div class="prog warn-prog">
            <el-tag v-for="n in progress.notParticipated" :key="'np'+n" size="small" type="info" class="mx">未参加：{{ n }}</el-tag>
            <el-tag v-for="n in progress.continuousWarnings" :key="'cw'+n" size="small" type="danger" class="mx">连续预警：{{ n }}</el-tag>
            <span v-if="!(progress.notParticipated || []).length && !(progress.continuousWarnings || []).length" class="tip">无异常</span>
          </div>
        </el-col>
      </el-row>

      <el-table :data="items" size="small" border height="430">
        <el-table-column prop="memberName" label="党员" width="90" fixed />
        <el-table-column label="身份" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isProbationary" size="small" type="warning">预备党员</el-tag>
            <span v-else>正式</span>
          </template>
        </el-table-column>
        <el-table-column label="自评" width="70">
          <template #default="{ row }">
            <el-tag size="small" :type="row.selfContent ? 'success' : (row.participated === 0 ? 'info' : 'danger')">
              {{ row.participated === 0 ? '未参加' : (row.selfContent ? '已交' : '未交') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="加权分" width="80">
          <template #default="{ row }">{{ row.weightedScore != null ? row.weightedScore : '—' }}</template>
        </el-table-column>
        <el-table-column label="支部等次" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="resultType(row.result)">{{ row.result || '待定' }}</el-tag>
            <el-tag v-if="row.adjustStatus" size="small" class="mx"
                    :type="{ 待审批: 'warning', 已批准: 'success', 已驳回: 'danger' }[row.adjustStatus]">
              调整{{ row.adjustStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stage" label="环节" width="90" />
        <el-table-column label="操作" width="330" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="openSelf(row)">自评</el-button>
            <el-button v-if="can('R02','R07')" size="small" link @click="openScores(row)">测评打分</el-button>
            <el-button v-if="can('R02','R07')" size="small" link type="success" @click="openBranch(row)">支评</el-button>
            <el-button v-if="can('R02','R07')" size="small" link type="warning" @click="openAdjust(row)">调整</el-button>
            <el-button v-if="can('R03')" size="small" link @click="openParty(row)">审定</el-button>
            <el-button v-if="can('R03') && row.adjustStatus === '待审批'"
                       size="small" link type="danger" @click="openAdjustAudit(row)">调整审批</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- ============ 计划表单 ============ -->
    <el-dialog v-model="planVisible" :title="planForm.id ? '编辑评议计划' : '新建评议计划'" width="640px">
      <el-form :model="planForm" label-width="100px">
        <el-form-item label="评议组织" required>
          <el-select v-model="planForm.orgId" filterable style="width:100%" @change="onPlanOrg">
            <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="评议年度" required>
          <el-select v-model="planForm.assessYear" style="width:160px">
            <el-option v-for="y in years" :key="y" :label="y + '年度'" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item label="对象范围"><el-input v-model="planForm.targetScope" placeholder="如：支部全体在册党员（含预备党员）" /></el-form-item>
        <el-form-item label="评议方式">
          <el-radio-group v-model="planForm.evalMode">
            <el-radio value="实名">实名</el-radio>
            <el-radio value="匿名">匿名</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优秀比例上限">
          <el-input-number v-model="planForm.ratioPct" :min="0" :max="100" :step="5" /> %
          <span class="tip">默认 33.3%，预备党员不计入优秀名额</span>
        </el-form-item>
        <el-form-item label="四维权重">
          <div class="w-grid">
            <div>政治表现<el-input-number v-model="weights.politics" :min="0" :max="1" :step="0.05" size="small" /></div>
            <div>遵规守纪<el-input-number v-model="weights.discipline" :min="0" :max="1" :step="0.05" size="small" /></div>
            <div>发挥作用<el-input-number v-model="weights.role" :min="0" :max="1" :step="0.05" size="small" /></div>
            <div>道德品行<el-input-number v-model="weights.morality" :min="0" :max="1" :step="0.05" size="small" /></div>
          </div>
          <el-tag size="small" :type="weightSum === 1 ? 'success' : 'danger'" class="mx">权重合计 {{ weightSum.toFixed(2) }}</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPlan">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 自评 ============ -->
    <el-dialog v-model="selfVisible" title="党员自评" width="560px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="党员">{{ selfForm.memberName }}<el-tag v-if="selfForm.isProbationary" size="small" type="warning" class="mx">预备党员</el-tag></el-form-item>
        <el-form-item label="参加评议">
          <el-switch v-model="selfForm.participated" :active-value="1" :inactive-value="0"
                     active-text="参加" inactive-text="不参加" />
        </el-form-item>
        <el-form-item v-if="selfForm.participated === 1" label="自评内容" required>
          <el-input v-model="selfForm.selfContent" type="textarea" :rows="5" maxlength="1000" show-word-limit
                    placeholder="政治表现、遵规守纪、发挥作用、道德品行四方面总结" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="selfVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSelf">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 测评打分 ============ -->
    <el-dialog v-model="scoreVisible" title="民主测评打分（四维）" width="560px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="党员">{{ scoreForm.memberName }}</el-form-item>
        <el-form-item v-for="f in scoreFields" :key="f.k" :label="f.label">
          <el-input-number v-model="scoreForm[f.k]" :min="0" :max="100" :precision="1" /> 分
          <span class="tip">权重 {{ (weights[f.k] * 100).toFixed(0) }}%</span>
        </el-form-item>
        <el-form-item label="加权得分">
          <el-tag size="large" type="success">{{ scorePreview }}</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scoreVisible = false">取消</el-button>
        <el-button type="primary" @click="submitScores">保存得分</el-button>
      </template>
    </el-dialog>

    <!-- ============ 支部评定 ============ -->
    <el-dialog v-model="branchVisible" title="支部评定" width="560px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="党员">{{ branchForm.memberName }}</el-form-item>
        <el-form-item label="评定等次" required>
          <el-radio-group v-model="branchForm.result" :disabled="branchForm.isProbationary === 1">
            <el-radio v-for="r in ['优秀','合格','基本合格','不合格']" :key="r" :value="r">{{ r }}</el-radio>
          </el-radio-group>
          <el-tag v-if="branchForm.isProbationary" size="small" type="warning" class="mx">预备党员不定等次（E-APP-04）</el-tag>
        </el-form-item>
        <el-form-item label="支部意见"><el-input v-model="branchForm.branchOpinion" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="branchVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBranchItem">保存评定</el-button>
      </template>
    </el-dialog>

    <!-- ============ 等次调整申请 ============ -->
    <el-dialog v-model="adjustVisible" title="申请调整评定等次" width="520px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="党员">{{ adjustForm.memberName }}（当前：{{ adjustForm.oldResult }}）</el-form-item>
        <el-form-item label="调整为" required>
          <el-select v-model="adjustForm.result" style="width:180px">
            <el-option v-for="r in ['优秀','合格','基本合格','不合格']" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整理由" required>
          <el-input v-model="adjustForm.reason" type="textarea" :rows="3" placeholder="E-APP-06：调整必须填写理由，经上级党委审批" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="warning" @click="submitAdjust">提交调整申请</el-button>
      </template>
    </el-dialog>

    <!-- ============ 上级党委：审定 / 调整审批 ============ -->
    <el-dialog v-model="partyVisible" title="上级党委审定意见" width="520px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="党员">{{ partyForm.memberName }}（等次：{{ partyForm.result }}）</el-form-item>
        <el-form-item label="审定意见"><el-input v-model="partyForm.partyOpinion" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="partyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitParty">确认审定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="auditVisible" title="等次调整审批（上级党委）" width="520px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="党员">{{ auditForm.memberName }}</el-form-item>
        <el-form-item label="申请调整">{{ auditForm.oldResult }} → {{ auditForm.result }}</el-form-item>
        <el-form-item label="调整理由">{{ auditForm.reason }}</el-form-item>
        <el-form-item label="审批意见"><el-input v-model="auditForm.opinion" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="submitAdjustAudit(false)">驳回</el-button>
        <el-button type="success" @click="submitAdjustAudit(true)">批准</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/org/democratic-appraisal'
const ORG_API = '/api/party/org/organization'
const years = [2024, 2025, 2026, 2027]

const loading = ref(false)
const plans = ref([])
const items = ref([])
const orgFlat = ref([])
const filter = reactive({ orgId: null, year: null })

async function loadOrgs() {
  const res = await axios.get(`${ORG_API}/tree`)
  if (res.data.code === 200) {
    const flat = []
    const walk = nodes => nodes.forEach(n => { flat.push(n); if (n.children) walk(n.children) })
    walk(res.data.data)
    orgFlat.value = flat
  }
}
async function loadPlans() {
  loading.value = true
  try {
    const res = await axios.get(`${API}/plan/list`, {
      params: { orgId: filter.orgId || undefined, year: filter.year || undefined }
    })
    if (res.data.code === 200) plans.value = res.data.data
  } finally { loading.value = false }
}

function ratioPercent(v) {
  const n = Number(v || 0)
  return (n <= 1 ? n * 100 : n).toFixed(1).replace(/\.0$/, '')
}
function planStatusType(s) {
  return { 草稿: 'info', 进行中: 'warning', 已公示: 'success', 已反馈: '' }[s] || 'info'
}
function resultType(r) {
  return { 优秀: 'success', 合格: '', 基本合格: 'warning', 不合格: 'danger', 不定等次: 'info' }[r] || 'info'
}

// ---------- 计划表单 ----------
const planVisible = ref(false)
const planForm = reactive({})
const weights = reactive({ politics: 0.3, discipline: 0.2, role: 0.3, morality: 0.2 })
const weightSum = computed(() =>
  Number(weights.politics || 0) + Number(weights.discipline || 0) + Number(weights.role || 0) + Number(weights.morality || 0))

function resetPlanForm() {
  Object.keys(planForm).forEach(k => delete planForm[k])
  Object.assign(planForm, { assessYear: 2026, evalMode: '实名', ratioPct: 33.3, targetScope: '' })
  Object.assign(weights, { politics: 0.3, discipline: 0.2, role: 0.3, morality: 0.2 })
}
function onPlanOrg(id) {
  const o = orgFlat.value.find(x => x.id === id)
  if (o) planForm.orgName = o.name
}
function openPlan(row) {
  resetPlanForm()
  if (row) {
    Object.assign(planForm, JSON.parse(JSON.stringify(row)))
    planForm.ratioPct = Number((Number(row.excellentRatio || 0.3333) * 100).toFixed(1))
    try {
      const w = JSON.parse(row.weightsJson || '{}')
      Object.assign(weights, {
        politics: Number(w.politics ?? 0.3), discipline: Number(w.discipline ?? 0.2),
        role: Number(w.role ?? 0.3), morality: Number(w.morality ?? 0.2)
      })
    } catch { /* 默认权重 */ }
  }
  planVisible.value = true
}
async function submitPlan() {
  if (!planForm.orgId || !planForm.assessYear) { ElMessage.warning('组织与年度必填'); return }
  if (Math.abs(weightSum.value - 1) > 0.001) { ElMessage.error('四维权重合计必须为 1'); return }
  const body = {
    id: planForm.id, orgId: planForm.orgId, orgName: planForm.orgName,
    assessYear: planForm.assessYear, targetScope: planForm.targetScope, evalMode: planForm.evalMode,
    excellentRatio: Number((planForm.ratioPct / 100).toFixed(4)),
    weightsJson: JSON.stringify({
      politics: weights.politics, discipline: weights.discipline, role: weights.role, morality: weights.morality
    })
  }
  try {
    await axios(planForm.id ? { method: 'put', url: `${API}/plan`, data: body }
                        : { method: 'post', url: `${API}/plan`, data: body })
    ElMessage.success('计划已保存')
    planVisible.value = false
    loadPlans()
  } catch { /* 拦截器提示 */ }
}
async function startPlan(row) {
  try {
    await ElMessageBox.confirm(`将按 ${row.orgName} 在册党员（含预备党员）生成评议明细，启动后不可重复启动。`, '启动评议', { type: 'info' })
    await axios.post(`${API}/plan/${row.id}/start`)
    ElMessage.success('评议已启动')
    loadPlans()
  } catch (e) { if (e !== 'cancel') { /* 业务错误已提示 */ } }
}

// ---------- 工作台 ----------
const wsVisible = ref(false)
const wsPlan = ref({})
const progress = ref({})
const weightsText = computed(() => {
  try {
    const w = JSON.parse(wsPlan.value.weightsJson || '{}')
    return `政治${Math.round((w.politics ?? .3) * 100)}% 守纪${Math.round((w.discipline ?? .2) * 100)}% 作用${Math.round((w.role ?? .3) * 100)}% 品行${Math.round((w.morality ?? .2) * 100)}%`
  } catch { return '' }
})

async function openWorkspace(row) {
  wsPlan.value = JSON.parse(JSON.stringify(row))
  wsVisible.value = true
}
async function loadWorkspace() {
  const id = wsPlan.value.id
  const [it, pg] = await Promise.all([
    axios.get(`${API}/plan/${id}/items`),
    axios.get(`${API}/plan/${id}/progress`)
  ])
  if (it.data.code === 200) items.value = it.data.data
  if (pg.data.code === 200) progress.value = pg.data.data
}
async function refreshWs() {
  await Promise.all([loadPlans(), loadWorkspace()])
  const fresh = plans.value.find(p => p.id === wsPlan.value.id)
  if (fresh) wsPlan.value = JSON.parse(JSON.stringify(fresh))
}

async function submitBranch() {
  try {
    const res = await axios.post(`${API}/plan/${wsPlan.value.id}/submit-branch`, {})
    const d = res.data.data || {}
    ElMessage.success(`支部评定已提交：优秀 ${d.excellent}/${d.graded}（${(d.ratio * 100).toFixed(1)}%）`)
    refreshWs()
  } catch (e) {
    const code = e?.response?.data?.data?.errorCode
    if (code === 'E-APP-05') {
      try {
        const { value } = await ElMessageBox.prompt(
          '优秀比例超过上限，按规定须经上级党委同意。请填写上级同意说明后放行：', '优秀比例超限（E-APP-05）',
          { confirmButtonText: '附说明放行', cancelButtonText: '返回调整', inputType: 'textarea', inputValidator: v => !!v?.trim() || '说明不能为空' })
        await axios.post(`${API}/plan/${wsPlan.value.id}/submit-branch`, { overrideNote: value })
        ElMessage.success('已附上级说明放行')
        refreshWs()
      } catch { /* 用户取消 */ }
    }
  }
}
async function publishPlan() {
  try {
    await axios.post(`${API}/plan/${wsPlan.value.id}/publish`)
    ElMessage.success('评议结果已公示')
    refreshWs()
  } catch { /* 拦截器提示 */ }
}
async function feedbackPlan() {
  try {
    await axios.post(`${API}/plan/${wsPlan.value.id}/feedback`)
    ElMessage.success('已反馈本人')
    refreshWs()
  } catch { /* 拦截器提示 */ }
}

// ---------- 自评 ----------
const selfVisible = ref(false)
const selfForm = reactive({})
function openSelf(row) {
  Object.keys(selfForm).forEach(k => delete selfForm[k])
  Object.assign(selfForm, { id: row.id, memberName: row.memberName, isProbationary: row.isProbationary,
    participated: row.participated == null ? 1 : row.participated, selfContent: row.selfContent || '' })
  selfVisible.value = true
}
async function submitSelf() {
  if (selfForm.participated === 1 && !selfForm.selfContent?.trim()) { ElMessage.error('自评内容不能为空（E-APP-03）'); return }
  try {
    await axios.post(`${API}/item/${selfForm.id}/self`,
      { selfContent: selfForm.selfContent, participated: selfForm.participated })
    ElMessage.success('自评已保存')
    selfVisible.value = false
    loadWorkspace()
  } catch { /* 拦截器提示 */ }
}

// ---------- 打分 ----------
const scoreVisible = ref(false)
const scoreForm = reactive({})
const scoreFields = [
  { k: 'scorePolitics', label: '政治表现' }, { k: 'scoreDiscipline', label: '遵规守纪' },
  { k: 'scoreRole', label: '发挥作用' }, { k: 'scoreMorality', label: '道德品行' }
]
const scorePreview = computed(() => {
  const w = (() => { try { return JSON.parse(wsPlan.value.weightsJson || '{}') } catch { return {} } })()
  const v = (scoreForm.scorePolitics || 0) * (w.politics ?? .3)
    + (scoreForm.scoreDiscipline || 0) * (w.discipline ?? .2)
    + (scoreForm.scoreRole || 0) * (w.role ?? .3)
    + (scoreForm.scoreMorality || 0) * (w.morality ?? .2)
  return v.toFixed(2)
})
function openScores(row) {
  Object.keys(scoreForm).forEach(k => delete scoreForm[k])
  Object.assign(scoreForm, { id: row.id, memberName: row.memberName,
    scorePolitics: row.scorePolitics != null ? Number(row.scorePolitics) : null,
    scoreDiscipline: row.scoreDiscipline != null ? Number(row.scoreDiscipline) : null,
    scoreRole: row.scoreRole != null ? Number(row.scoreRole) : null,
    scoreMorality: row.scoreMorality != null ? Number(row.scoreMorality) : null })
  scoreVisible.value = true
}
async function submitScores() {
  if (scoreFields.some(f => scoreForm[f.k] == null)) { ElMessage.warning('请填写四项得分'); return }
  try {
    await axios.post(`${API}/item/${scoreForm.id}/scores`, {
      scorePolitics: scoreForm.scorePolitics, scoreDiscipline: scoreForm.scoreDiscipline,
      scoreRole: scoreForm.scoreRole, scoreMorality: scoreForm.scoreMorality
    })
    ElMessage.success('得分已保存：' + scorePreview.value)
    scoreVisible.value = false
    loadWorkspace()
  } catch { /* 拦截器提示 */ }
}

// ---------- 支评 ----------
const branchVisible = ref(false)
const branchForm = reactive({})
function openBranch(row) {
  Object.keys(branchForm).forEach(k => delete branchForm[k])
  Object.assign(branchForm, { id: row.id, memberName: row.memberName, isProbationary: row.isProbationary,
    result: row.isProbationary ? '不定等次' : (row.result && row.result !== '不定等次' ? row.result : null),
    branchOpinion: row.branchOpinion || '' })
  branchVisible.value = true
}
async function submitBranchItem() {
  if (!branchForm.isProbationary && !branchForm.result) { ElMessage.warning('请选择评定等次'); return }
  try {
    await axios.post(`${API}/item/${branchForm.id}/branch`,
      { branchOpinion: branchForm.branchOpinion, result: branchForm.isProbationary ? '不定等次' : branchForm.result })
    ElMessage.success('支部评定已保存')
    branchVisible.value = false
    loadWorkspace()
  } catch { /* 拦截器提示 */ }
}

// ---------- 调整 ----------
const adjustVisible = ref(false)
const adjustForm = reactive({})
function openAdjust(row) {
  Object.keys(adjustForm).forEach(k => delete adjustForm[k])
  Object.assign(adjustForm, { id: row.id, memberName: row.memberName, oldResult: row.result,
    result: row.result && row.result !== '不定等次' ? row.result : null, reason: '' })
  adjustVisible.value = true
}
async function submitAdjust() {
  if (!adjustForm.result) { ElMessage.warning('请选择调整后等次'); return }
  if (!adjustForm.reason?.trim()) { ElMessage.error('调整理由必填（E-APP-06）'); return }
  try {
    await axios.post(`${API}/item/${adjustForm.id}/adjust`,
      { result: adjustForm.result, reason: adjustForm.reason })
    ElMessage.success('调整申请已提交，待上级党委审批')
    adjustVisible.value = false
    loadWorkspace()
  } catch { /* 拦截器提示 */ }
}

// ---------- 党委审定 / 审批 ----------
const partyVisible = ref(false)
const partyForm = reactive({})
function openParty(row) {
  Object.keys(partyForm).forEach(k => delete partyForm[k])
  Object.assign(partyForm, { id: row.id, memberName: row.memberName, result: row.result, partyOpinion: row.partyOpinion || '' })
  partyVisible.value = true
}
async function submitParty() {
  try {
    await axios.post(`${API}/item/${partyForm.id}/party`, { partyOpinion: partyForm.partyOpinion })
    ElMessage.success('审定意见已保存')
    partyVisible.value = false
    loadWorkspace()
  } catch { /* 拦截器提示 */ }
}

const auditVisible = ref(false)
const auditForm = reactive({})
function openAdjustAudit(row) {
  Object.keys(auditForm).forEach(k => delete auditForm[k])
  Object.assign(auditForm, { id: row.id, memberName: row.memberName, oldResult: row.result,
    result: row.result, reason: row.adjustReason, opinion: '' })
  auditVisible.value = true
}
async function submitAdjustAudit(pass) {
  try {
    await axios.post(`${API}/item/${auditForm.id}/adjust-audit`, { pass, opinion: auditForm.opinion })
    ElMessage.success(pass ? '已批准调整' : '已驳回调整')
    auditVisible.value = false
    loadWorkspace()
  } catch { /* 拦截器提示 */ }
}

onMounted(() => { loadOrgs().then(loadPlans) })
</script>

<style scoped>
.page-container { padding: 16px; }
.bar { display: flex; gap: 10px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }
.tip { color: #909399; font-size: 12px; }
.mx { margin-left: 8px; }
.ws-head { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; flex-wrap: wrap; }
.ws-actions { margin-left: auto; display: flex; gap: 8px; }
.prog-row { margin-bottom: 10px; }
.prog { background: #f4f7fb; border-radius: 6px; padding: 10px 12px; min-height: 62px; box-sizing: border-box; }
.prog b { display: block; font-size: 22px; color: #409eff; }
.prog span { font-size: 12px; color: #909399; }
.warn-prog { display: flex; align-items: center; flex-wrap: wrap; gap: 4px; }
.w-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px 16px; }
.w-grid div { display: flex; align-items: center; justify-content: space-between; font-size: 13px; }
</style>

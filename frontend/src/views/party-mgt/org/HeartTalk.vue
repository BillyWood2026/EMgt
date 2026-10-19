<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">年度计划覆盖 · 重复对子预警 · 问题闭环跟进</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <el-tabs v-model="tab">
        <!-- ============ 谈话台账 ============ -->
        <el-tab-pane label="谈话记录台账" name="talk">
          <div class="bar">
            <el-select v-model="filter.orgId" placeholder="全部组织" clearable size="small" filterable
                       style="width:210px" @change="loadTalks">
              <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
            </el-select>
            <el-select v-model="filter.year" placeholder="年度" clearable size="small" style="width:100px" @change="loadTalks">
              <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
            </el-select>
            <el-select v-model="filter.followStatus" placeholder="跟进状态" clearable size="small" style="width:110px" @change="loadTalks">
              <el-option label="未跟进" value="未跟进" /><el-option label="跟进中" value="跟进中" /><el-option label="已解决" value="已解决" />
            </el-select>
            <el-input v-model="filter.keyword" placeholder="谈话人/对象/主题" size="small" clearable
                      style="width:200px" @keyup.enter="loadTalks" @clear="loadTalks" />
            <el-button size="small" @click="loadTalks">查询</el-button>
            <el-button v-if="can('R02','R04','R07')" size="small" type="primary" @click="openTalk()">登记谈话</el-button>
          </div>

          <el-table :data="talks" size="small" border v-loading="loading">
            <el-table-column prop="talkDate" label="日期" width="105" />
            <el-table-column label="谈话人→对象" min-width="190">
              <template #default="{ row }">
                <b>{{ row.talkerName }}</b><span class="dim">（{{ row.talkerIdentity || '—' }}）</span>
                → <b>{{ row.intervieweeName }}</b><span class="dim">（{{ row.intervieweeIdentity || '—' }}）</span>
              </template>
            </el-table-column>
            <el-table-column label="方式" width="85">
              <template #default="{ row }">
                <el-tag size="small" :type="row.talkMode === '集体谈话' ? 'warning' : ''">{{ row.talkMode || '个别谈话' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="talkType" label="类型" width="100" />
            <el-table-column prop="topic" label="主题" min-width="150" show-overflow-tooltip />
            <el-table-column prop="orgName" label="组织" width="160" show-overflow-tooltip />
            <el-table-column label="跟进" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="followType(row.followStatus)">{{ row.followStatus || '未跟进' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R02','R04','R07')" size="small" link @click="openTalk(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 年度计划与覆盖 ============ -->
        <el-tab-pane label="年度计划与覆盖" name="plan">
          <div class="bar">
            <el-select v-model="planFilter.orgId" placeholder="全部组织" clearable size="small" filterable
                       style="width:210px" @change="loadPlans">
              <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
            </el-select>
            <el-select v-model="planFilter.year" placeholder="年度" clearable size="small" style="width:100px" @change="loadPlans">
              <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
            </el-select>
            <el-button v-if="can('R02','R07')" size="small" type="primary" @click="openPlan()">制定年度计划</el-button>
          </div>

          <el-table :data="plans" size="small" border v-loading="planLoading">
            <el-table-column prop="orgName" label="党组织" min-width="180" show-overflow-tooltip />
            <el-table-column prop="planYear" label="年度" width="80" />
            <el-table-column prop="leaderRule" label="班子成员规则" min-width="220" show-overflow-tooltip />
            <el-table-column prop="committeeRule" label="党员规则" min-width="220" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="90" />
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R02','R07')" size="small" link type="success" @click="generate(row)">生成应谈名单</el-button>
                <el-button size="small" link type="primary" @click="openCoverage(row)">覆盖情况</el-button>
                <el-button v-if="can('R02','R07')" size="small" link @click="openPlan(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 待跟进问题 ============ -->
        <el-tab-pane label="待跟进问题" name="issue">
          <div class="bar"><span class="tip">登记了“需要解决的问题”且未标记已解决的谈话，纳入闭环跟进清单。</span></div>
          <el-table :data="unresolved" size="small" border v-loading="issueLoading">
            <el-table-column prop="talkDate" label="谈话日期" width="105" />
            <el-table-column label="对子" width="200">
              <template #default="{ row }">{{ row.talkerName }} → {{ row.intervieweeName }}</template>
            </el-table-column>
            <el-table-column prop="issueToSolve" label="待解决问题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="followerName" label="跟进人" width="100" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="followType(row.followStatus)">{{ row.followStatus }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90">
              <template #default="{ row }">
                <el-button v-if="can('R02','R04','R07')" size="small" link @click="openTalk(row, 'issue')">更新跟进</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ============ 谈话表单 ============ -->
    <el-dialog v-model="talkVisible" title="谈心谈话登记" width="760px">
      <el-form :model="talkForm" label-width="100px">
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="党组织" required>
              <el-select v-model="talkForm.orgId" filterable style="width:100%" @change="onTalkOrg">
                <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="谈话日期" required>
              <el-date-picker v-model="talkForm.talkDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="谈话方式" required>
              <el-radio-group v-model="talkForm.talkMode">
                <el-radio value="个别谈话">个别谈话</el-radio>
                <el-radio value="集体谈话">集体谈话</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="谈话类型">
              <el-select v-model="talkForm.talkType" filterable allow-create style="width:100%"
                         placeholder="如：日常谈心/提醒谈话/廉政谈话/任前谈话">
                <el-option v-for="t in talkTypes" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="谈话人" required><el-input v-model="talkForm.talkerName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="谈话人职务"><el-input v-model="talkForm.talkerIdentity" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="谈话对象" required><el-input v-model="talkForm.intervieweeName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="对象身份"><el-input v-model="talkForm.intervieweeIdentity" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="谈话地点"><el-input v-model="talkForm.location" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="谈话主题"><el-input v-model="talkForm.topic" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="谈话内容"><el-input v-model="talkForm.content" type="textarea" :rows="3" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="需解决问题"><el-input v-model="talkForm.issueToSolve" type="textarea" :rows="2" placeholder="谈话中发现、需要跟进解决的问题" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="跟进状态">
              <el-select v-model="talkForm.followStatus" style="width:100%">
                <el-option label="未跟进" value="未跟进" /><el-option label="跟进中" value="跟进中" /><el-option label="已解决" value="已解决" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="跟进人"><el-input v-model="talkForm.followerName" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="talkVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTalk">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 计划表单 ============ -->
    <el-dialog v-model="planFormVisible" :title="planForm.id ? '编辑谈话计划' : '制定年度谈心谈话计划'" width="640px">
      <el-form :model="planForm" label-width="120px">
        <el-form-item label="党组织" required>
          <el-select v-model="planForm.orgId" filterable style="width:100%" @change="onPlanOrg">
            <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划年度" required>
          <el-select v-model="planForm.planYear" style="width:160px">
            <el-option v-for="y in years" :key="y" :label="y + '年度'" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item label="班子成员规则"><el-input v-model="planForm.leaderRule" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="党员覆盖规则"><el-input v-model="planForm.committeeRule" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPlanForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 覆盖情况 ============ -->
    <el-dialog v-model="covVisible" :title="`${covPlan.orgName || ''} ${covPlan.planYear || ''}年度应谈覆盖情况`"
               width="900px" top="6vh">
      <el-row :gutter="10" class="cov-cards">
        <el-col :span="6"><div class="prog"><b>{{ covStats.total || 0 }}</b><span>应谈人数</span></div></el-col>
        <el-col :span="6"><div class="prog"><b>{{ covStats.covered || 0 }}</b><span>已覆盖</span></div></el-col>
        <el-col :span="6"><div class="prog"><b>{{ covStats.coverageRate || 0 }}%</b><span>覆盖率</span></div></el-col>
        <el-col :span="6">
          <div class="prog">
            <el-tag size="small" type="danger" v-for="n in covStats.overdue" :key="'od'+n" class="mx">逾期：{{ n }}</el-tag>
            <el-tag size="small" type="warning" v-for="n in covStats.uncovered" :key="'uc'+n" class="mx">未覆盖：{{ n }}</el-tag>
            <span v-if="!(covStats.uncovered || []).length" class="tip">全部覆盖</span>
          </div>
        </el-col>
      </el-row>
      <el-alert v-if="covStats.assessmentWarn" :title="covStats.assessmentWarn" type="error" :closable="false"
                show-icon style="margin:8px 0" />
      <el-table :data="coverage" size="small" border height="380">
        <el-table-column prop="intervieweeName" label="谈话对象" width="110" />
        <el-table-column prop="requiredRule" label="适用规则" width="140" />
        <el-table-column prop="talkerName" label="应由谁谈" width="110" />
        <el-table-column prop="dueDate" label="应完成期限" width="120" />
        <el-table-column label="覆盖状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.covered ? 'success' : 'info'">{{ row.covered ? '已谈' : '未谈' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="coverDate" label="实际谈话日期" width="120" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/org/heart-talk'
const ORG_API = '/api/party/org/organization'
const years = [2024, 2025, 2026, 2027]
const talkTypes = ['日常谈心', '提醒谈话', '廉政谈话', '任前谈话', '组织约谈', '关心关爱谈话', '批评教育谈话']

const tab = ref('talk')
const orgFlat = ref([])

async function loadOrgs() {
  const res = await axios.get(`${ORG_API}/tree`)
  if (res.data.code === 200) {
    const flat = []
    const walk = nodes => nodes.forEach(n => { flat.push(n); if (n.children) walk(n.children) })
    walk(res.data.data)
    orgFlat.value = flat
  }
}
function followType(s) {
  return { 未跟进: 'info', 跟进中: 'warning', 已解决: 'success' }[s] || 'info'
}

// ---------- 谈话台账 ----------
const loading = ref(false)
const talks = ref([])
const filter = reactive({ orgId: null, year: null, followStatus: '', keyword: '' })

async function loadTalks() {
  loading.value = true
  try {
    const res = await axios.get(`${API}/list`, { params: {
      orgId: filter.orgId || undefined, year: filter.year || undefined,
      followStatus: filter.followStatus || undefined, keyword: filter.keyword || undefined
    } })
    if (res.data.code === 200) talks.value = res.data.data
  } finally { loading.value = false }
}

const talkVisible = ref(false)
const talkForm = reactive({})
let returnTab = 'talk'

function resetTalk() {
  Object.keys(talkForm).forEach(k => delete talkForm[k])
  Object.assign(talkForm, { talkMode: '个别谈话', talkDate: new Date().toISOString().slice(0, 10), followStatus: '未跟进' })
}
function onTalkOrg(id) {
  const o = orgFlat.value.find(x => x.id === id)
  if (o) talkForm.orgName = o.name
}
function openTalk(row, back) {
  resetTalk()
  if (row) Object.assign(talkForm, JSON.parse(JSON.stringify(row)))
  returnTab = back || 'talk'
  talkVisible.value = true
}
async function submitTalk() {
  if (!talkForm.talkerName || !talkForm.intervieweeName) { ElMessage.error('谈话人和谈话对象必填（E-TALK-01）'); return }
  if (!talkForm.talkDate) { ElMessage.error('谈话日期必填（E-TALK-01）'); return }
  if (talkForm.followStatus === '已解决' && !talkForm.issueToSolve?.trim()) {
    ElMessage.error('标记问题已解决前必须登记问题与跟进情况（E-TALK-02）'); return
  }
  // 同年同谈话人同对象重复对子：预警不阻断
  const dup = talks.value.some(t => t.id !== talkForm.id && t.orgId === talkForm.orgId
    && t.talkerName === talkForm.talkerName && t.intervieweeName === talkForm.intervieweeName
    && t.talkDate && String(t.talkDate).slice(0, 4) === String(talkForm.talkDate).slice(0, 4))
  if (dup) {
    try {
      await ElMessageBox.confirm('同年该谈话人与对象已存在谈话记录，属于重复对子（系统将记录审计预警）。是否仍要保存？',
        '重复谈话对子预警', { type: 'warning', confirmButtonText: '仍要保存', cancelButtonText: '取消' })
    } catch { return }
  }
  try {
    await axios(talkForm.id ? { method: 'put', url: API, data: talkForm }
                        : { method: 'post', url: API, data: talkForm })
    ElMessage.success('谈话记录已保存，已自动回写应谈名单覆盖状态')
    talkVisible.value = false
    loadTalks()
    if (returnTab === 'issue') loadUnresolved()
  } catch { /* 拦截器提示 */ }
}

// ---------- 待跟进 ----------
const issueLoading = ref(false)
const unresolved = ref([])
async function loadUnresolved() {
  issueLoading.value = true
  try {
    const res = await axios.get(`${API}/unresolved`)
    if (res.data.code === 200) unresolved.value = res.data.data
  } finally { issueLoading.value = false }
}

// ---------- 计划 ----------
const planLoading = ref(false)
const plans = ref([])
const planFilter = reactive({ orgId: null, year: null })

async function loadPlans() {
  planLoading.value = true
  try {
    const res = await axios.get(`${API}/plan/list`, { params: {
      orgId: planFilter.orgId || undefined, year: planFilter.year || undefined
    } })
    if (res.data.code === 200) plans.value = res.data.data
  } finally { planLoading.value = false }
}

const planFormVisible = ref(false)
const planForm = reactive({})
function resetPlanForm() {
  Object.keys(planForm).forEach(k => delete planForm[k])
  Object.assign(planForm, {
    planYear: 2026,
    leaderRule: '党组织书记与班子成员每年至少谈心谈话1次',
    committeeRule: '班子成员之间、班子成员与党员每年至少谈心谈话1次'
  })
}
function onPlanOrg(id) {
  const o = orgFlat.value.find(x => x.id === id)
  if (o) planForm.orgName = o.name
}
function openPlan(row) {
  resetPlanForm()
  if (row) Object.assign(planForm, JSON.parse(JSON.stringify(row)))
  planFormVisible.value = true
}
async function submitPlanForm() {
  if (!planForm.orgId || !planForm.planYear) { ElMessage.error('计划组织和年度必填'); return }
  try {
    await axios(planForm.id ? { method: 'put', url: `${API}/plan`, data: planForm }
                        : { method: 'post', url: `${API}/plan`, data: planForm })
    ElMessage.success('计划已保存')
    planFormVisible.value = false
    loadPlans()
  } catch { /* 拦截器提示 */ }
}
async function generate(row) {
  try {
    await ElMessageBox.confirm(`将按 ${row.orgName} 在册党员重新生成 ${row.planYear} 年度应谈名单（覆盖旧名单，已谈记录自动识别）。`,
      '生成应谈名单', { type: 'info' })
    const res = await axios.post(`${API}/plan/${row.id}/generate`)
    ElMessage.success(`已生成应谈名单 ${res.data.data?.length || 0} 人`)
  } catch (e) { if (e !== 'cancel') { /* 业务错误已提示 */ } }
}

// ---------- 覆盖情况 ----------
const covVisible = ref(false)
const covPlan = ref({})
const coverage = ref([])
const covStats = ref({})
async function openCoverage(row) {
  covPlan.value = row
  covVisible.value = true
  const [cov, st] = await Promise.all([
    axios.get(`${API}/plan/${row.id}/coverage`),
    axios.get(`${API}/plan/${row.id}/stats`)
  ])
  if (cov.data.code === 200) coverage.value = cov.data.data
  if (st.data.code === 200) covStats.value = st.data.data
}

onMounted(() => { loadOrgs().then(() => { loadTalks(); loadPlans() }); loadUnresolved() })
</script>

<style scoped>
.page-container { padding: 16px; }
.bar { display: flex; gap: 10px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }
.tip { color: #909399; font-size: 12px; }
.dim { color: #909399; font-size: 12px; }
.mx { margin: 2px 6px 2px 0; }
.cov-cards { margin-bottom: 8px; }
.prog { background: #f4f7fb; border-radius: 6px; padding: 10px 12px; min-height: 62px; box-sizing: border-box; }
.prog b { display: block; font-size: 22px; color: #409eff; }
.prog span { font-size: 12px; color: #909399; }
</style>

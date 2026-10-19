<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">规则配置 · 自动关联 · 手工审批 · 年度重置</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <!-- ============ 积分规则 ============ -->
        <el-tab-pane label="积分规则" name="rules">
          <div class="bar">
            <el-button v-if="can('R02','R04')" size="small" type="primary" @click="openRule()">新增规则</el-button>
            <el-button size="small" @click="loadRules">刷新</el-button>
          </div>
          <el-table :data="rules" size="small" border v-loading="ruleLoading">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="ruleKey" label="规则编码" width="160" show-overflow-tooltip />
            <el-table-column prop="ruleName" label="规则名称" min-width="180" show-overflow-tooltip />
            <el-table-column label="分值" width="90">
              <template #default="{ row }">
                <span :class="{ neg: row.scoreValue < 0 }">{{ row.scoreValue }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="period" label="周期" width="100" />
            <el-table-column label="自动关联" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="row.autoFlag === 1 ? 'success' : 'info'">
                  {{ row.autoFlag === 1 ? '自动' : '手工' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="启用" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="row.enabled === 1 ? 'success' : 'danger'">
                  {{ row.enabled === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="creator" label="创建人" width="100" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R02','R04')" size="small" link type="primary" @click="openRule(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 积分流水 ============ -->
        <el-tab-pane label="积分流水" name="flows">
          <div class="bar">
            <el-select v-model="flowFilter.memberId" placeholder="全部党员" clearable filterable size="small"
                       style="width:160px" @change="loadFlows">
              <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
            </el-select>
            <el-select v-model="flowFilter.orgId" placeholder="全部组织" clearable filterable size="small"
                       style="width:200px" @change="loadFlows">
              <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
            </el-select>
            <el-select v-model="flowFilter.year" placeholder="年度" clearable size="small" style="width:100px" @change="loadFlows">
              <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
            </el-select>
            <el-select v-model="flowFilter.month" placeholder="月度" clearable size="small" style="width:90px" @change="loadFlows">
              <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
            </el-select>
            <el-select v-model="flowFilter.ruleKey" placeholder="全部规则" clearable size="small" style="width:180px" @change="loadFlows">
              <el-option v-for="r in rules" :key="r.ruleKey" :label="r.ruleName" :value="r.ruleKey" />
            </el-select>
            <el-button v-if="can('R02','R04','R07','R08')" size="small" type="primary" @click="openFlow()">登记积分</el-button>
            <el-button size="small" @click="loadFlows">刷新</el-button>
          </div>
          <el-table :data="flows" size="small" border v-loading="flowLoading">
            <el-table-column prop="memberName" label="党员" width="90" fixed />
            <el-table-column prop="orgName" label="组织" min-width="150" show-overflow-tooltip />
            <el-table-column prop="ruleName" label="规则" min-width="140" show-overflow-tooltip />
            <el-table-column label="分值" width="80">
              <template #default="{ row }">
                <span :class="{ neg: row.score < 0 }">{{ row.score > 0 ? '+' + row.score : row.score }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="sourceType" label="来源类型" width="90" />
            <el-table-column label="审批状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="auditStatusType(row.auditStatus)">{{ row.auditStatus }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="scoreYear" label="年度" width="70" />
            <el-table-column prop="scoreMonth" label="月度" width="70" />
            <el-table-column prop="createTime" label="创建时间" width="160" />
            <el-table-column label="操作" width="130" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R03') && row.auditStatus === '待审批'"
                           size="small" link type="success" @click="openAudit(row)">审批</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 积分排名 ============ -->
        <el-tab-pane label="积分排名" name="rank">
          <div class="bar">
            <el-select v-model="rankFilter.orgId" placeholder="全部组织" clearable filterable size="small"
                       style="width:200px" @change="loadRank">
              <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
            </el-select>
            <el-select v-model="rankFilter.year" placeholder="年度" clearable size="small" style="width:100px" @change="loadRank">
              <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
            </el-select>
            <el-radio-group v-model="rankFilter.scope" size="small" @change="loadRank">
              <el-radio-button value="branch">支部</el-radio-button>
              <el-radio-button value="group">全集团</el-radio-button>
            </el-radio-group>
            <el-button size="small" @click="loadRank">刷新</el-button>
          </div>
          <el-table :data="rankList" size="small" border v-loading="rankLoading">
            <el-table-column prop="rank" label="排名" width="70" />
            <el-table-column prop="memberName" label="党员" width="100" />
            <el-table-column prop="orgName" label="组织" min-width="180" show-overflow-tooltip />
            <el-table-column label="总积分" width="100">
              <template #default="{ row }">
                <span :class="{ neg: row.score < 0 }">{{ row.score }}</span>
              </template>
            </el-table-column>
            <el-table-column label="预警" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.score < 0" size="small" type="danger">负分预警</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 我的积分 ============ -->
        <el-tab-pane label="我的积分" name="myScore">
          <div class="bar">
            <el-select v-model="myScoreFilter.memberId" placeholder="选择党员" filterable size="small"
                       style="width:180px" @change="loadMyScore">
              <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
            </el-select>
            <el-select v-model="myScoreFilter.year" placeholder="年度" clearable size="small" style="width:100px" @change="loadMyScore">
              <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
            </el-select>
            <el-button size="small" @click="loadMyScore">查询</el-button>
          </div>
          <div v-if="myScoreData" class="my-summary">
            <el-statistic title="总积分" :value="myScoreData.totalScore || 0" />
          </div>
          <el-table :data="myScoreData?.flows || []" size="small" border v-loading="myScoreLoading">
            <el-table-column prop="ruleName" label="规则" min-width="150" show-overflow-tooltip />
            <el-table-column label="分值" width="90">
              <template #default="{ row }">
                <span :class="{ neg: row.score < 0 }">{{ row.score > 0 ? '+' + row.score : row.score }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="sourceType" label="来源" width="80" />
            <el-table-column prop="scoreYear" label="年度" width="70" />
            <el-table-column prop="scoreMonth" label="月度" width="70" />
            <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" width="160" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ============ 年度重置 ============ -->
    <div class="bottom-bar" v-if="can('R02','R04')">
      <el-button size="small" type="warning" @click="openReset">年度重置</el-button>
    </div>

    <!-- ============ 规则弹窗 ============ -->
    <el-dialog v-model="ruleVisible" :title="ruleForm.id ? '编辑积分规则' : '新增积分规则'" width="520px">
      <el-form :model="ruleForm" label-width="100px">
        <el-form-item label="规则编码" required>
          <el-input v-model="ruleForm.ruleKey" placeholder="如：按时交党费" :disabled="!!ruleForm.id" />
        </el-form-item>
        <el-form-item label="规则名称" required>
          <el-input v-model="ruleForm.ruleName" placeholder="如：按时交纳党费" />
        </el-form-item>
        <el-form-item label="分值" required>
          <el-input-number v-model="ruleForm.scoreValue" :step="1" />
        </el-form-item>
        <el-form-item label="周期">
          <el-select v-model="ruleForm.period" style="width:180px">
            <el-option label="一次性" value="一次性" />
            <el-option label="月度" value="月度" />
            <el-option label="季度" value="季度" />
            <el-option label="年度" value="年度" />
          </el-select>
        </el-form-item>
        <el-form-item label="自动关联">
          <el-switch v-model="ruleForm.autoFlag" :active-value="1" :inactive-value="0" active-text="自动" inactive-text="手工" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="ruleForm.enabled" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRule">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 登记积分弹窗 ============ -->
    <el-dialog v-model="flowVisible" title="登记积分" width="560px">
      <el-form :model="flowForm" label-width="100px">
        <el-form-item label="党员" required>
          <el-select v-model="flowForm.memberId" filterable style="width:100%" @change="onFlowMember">
            <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则" required>
          <el-select v-model="flowForm.ruleKey" style="width:100%" @change="onFlowRule">
            <el-option v-for="r in rules" :key="r.ruleKey" :label="r.ruleName" :value="r.ruleKey" />
          </el-select>
        </el-form-item>
        <el-form-item label="分值" required>
          <el-input-number v-model="flowForm.score" :step="1" />
        </el-form-item>
        <el-form-item label="来源类型" required>
          <el-radio-group v-model="flowForm.sourceType">
            <el-radio value="自动">自动</el-radio>
            <el-radio value="手工">手工</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年度">
          <el-select v-model="flowForm.scoreYear" style="width:120px">
            <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item label="月度">
          <el-select v-model="flowForm.scoreMonth" style="width:120px">
            <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="flowForm.remark" type="textarea" :rows="2" placeholder="手工登记请说明事由" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="flowVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFlow">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 审批弹窗 ============ -->
    <el-dialog v-model="auditVisible" title="积分审批（上级党委）" width="520px">
      <el-form label-width="100px">
        <el-form-item label="党员">{{ auditForm.memberName }}</el-form-item>
        <el-form-item label="规则">{{ auditForm.ruleName }}</el-form-item>
        <el-form-item label="分值">
          <span :class="{ neg: auditForm.score < 0 }">{{ auditForm.score > 0 ? '+' + auditForm.score : auditForm.score }}</span>
        </el-form-item>
        <el-form-item label="来源">{{ auditForm.sourceType }}</el-form-item>
        <el-form-item label="备注">{{ auditForm.remark || '—' }}</el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="auditForm.opinion" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="submitAudit(false)">驳回</el-button>
        <el-button type="success" @click="submitAudit(true)">批准</el-button>
      </template>
    </el-dialog>

    <!-- ============ 年度重置弹窗 ============ -->
    <el-dialog v-model="resetVisible" title="年度积分重置" width="460px">
      <el-form label-width="100px">
        <el-form-item label="重置年度" required>
          <el-select v-model="resetForm.year" style="width:160px">
            <el-option v-for="y in years" :key="y" :label="y + '年度'" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item label="重置模式" required>
          <el-radio-group v-model="resetForm.mode">
            <el-radio value="reset">清零（删除该年度全部流水）</el-radio>
            <el-radio value="carryover">结转（保留流水，仅记录日志）</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="warning" @click="submitReset">确认执行</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/member/score'
const ORG_API = '/api/party/org/organization'
const MEMBER_API = '/api/party/member/info'
const years = [2024, 2025, 2026, 2027]

const activeTab = ref('rules')
const orgFlat = ref([])
const members = ref([])

// ---------- 数据加载 ----------
async function loadOrgs() {
  try {
    const res = await axios.get(`${ORG_API}/tree`)
    if (res.data.code === 200) {
      const flat = []
      const walk = nodes => nodes.forEach(n => { flat.push(n); if (n.children) walk(n.children) })
      walk(res.data.data)
      orgFlat.value = flat
    }
  } catch { /* 拦截器提示 */ }
}
async function loadMembers() {
  try {
    const res = await axios.get(`${MEMBER_API}/list`)
    if (res.data.code === 200) members.value = res.data.data || []
  } catch { /* 拦截器提示 */ }
}

// ---------- 积分规则 ----------
const rules = ref([])
const ruleLoading = ref(false)
async function loadRules() {
  ruleLoading.value = true
  try {
    const res = await axios.get(`${API}/rules`)
    if (res.data.code === 200) rules.value = res.data.data || []
  } finally { ruleLoading.value = false }
}

const ruleVisible = ref(false)
const ruleForm = reactive({})
function openRule(row) {
  Object.keys(ruleForm).forEach(k => delete ruleForm[k])
  if (row) {
    Object.assign(ruleForm, JSON.parse(JSON.stringify(row)))
  } else {
    Object.assign(ruleForm, { scoreValue: 1, period: '一次性', autoFlag: 0, enabled: 1 })
  }
  ruleVisible.value = true
}
async function submitRule() {
  if (!ruleForm.ruleKey?.trim() || !ruleForm.ruleName?.trim()) { ElMessage.warning('规则编码与名称必填'); return }
  try {
    await axios.post(`${API}/rules`, ruleForm)
    ElMessage.success('规则已保存')
    ruleVisible.value = false
    loadRules()
  } catch { /* 拦截器提示 */ }
}

// ---------- 积分流水 ----------
const flows = ref([])
const flowLoading = ref(false)
const flowFilter = reactive({ memberId: null, orgId: null, year: null, month: null, ruleKey: null })
async function loadFlows() {
  flowLoading.value = true
  try {
    const res = await axios.get(`${API}/flows`, {
      params: {
        memberId: flowFilter.memberId || undefined,
        orgId: flowFilter.orgId || undefined,
        year: flowFilter.year || undefined,
        month: flowFilter.month || undefined,
        ruleKey: flowFilter.ruleKey || undefined
      }
    })
    if (res.data.code === 200) flows.value = res.data.data || []
  } finally { flowLoading.value = false }
}

function auditStatusType(s) {
  return { 待审批: 'warning', 已批准: 'success', 已驳回: 'danger' }[s] || 'info'
}

const flowVisible = ref(false)
const flowForm = reactive({})
function openFlow() {
  Object.keys(flowForm).forEach(k => delete flowForm[k])
  const now = new Date()
  Object.assign(flowForm, {
    memberId: null, memberName: '', orgId: null, orgName: '',
    ruleKey: '', ruleName: '', score: 1, sourceType: '手工',
    scoreYear: now.getFullYear(), scoreMonth: now.getMonth() + 1, remark: ''
  })
  flowVisible.value = true
}
function onFlowMember(id) {
  const m = members.value.find(x => x.id === id)
  if (m) {
    flowForm.memberName = m.name
    flowForm.orgId = m.orgId
    flowForm.orgName = m.orgName || ''
  }
}
function onFlowRule(key) {
  const r = rules.value.find(x => x.ruleKey === key)
  if (r) {
    flowForm.ruleName = r.ruleName
    flowForm.score = r.scoreValue
  }
}
async function submitFlow() {
  if (!flowForm.memberId) { ElMessage.warning('请选择党员'); return }
  if (!flowForm.ruleKey) { ElMessage.warning('请选择规则'); return }
  if (flowForm.score == null) { ElMessage.warning('请填写分值'); return }
  try {
    await axios.post(`${API}/flows`, flowForm)
    ElMessage.success(flowForm.sourceType === '手工' ? '登记成功，待审批' : '登记成功')
    flowVisible.value = false
    loadFlows()
  } catch { /* 拦截器提示 */ }
}

// ---------- 审批 ----------
const auditVisible = ref(false)
const auditForm = reactive({})
function openAudit(row) {
  Object.keys(auditForm).forEach(k => delete auditForm[k])
  Object.assign(auditForm, { ...row, opinion: '' })
  auditVisible.value = true
}
async function submitAudit(pass) {
  try {
    await axios.post(`${API}/flows/${auditForm.id}/audit`, null, {
      params: { pass, opinion: auditForm.opinion || undefined }
    })
    ElMessage.success(pass ? '已批准' : '已驳回')
    auditVisible.value = false
    loadFlows()
  } catch { /* 拦截器提示 */ }
}

// ---------- 积分排名 ----------
const rankList = ref([])
const rankLoading = ref(false)
const rankFilter = reactive({ orgId: null, year: null, scope: 'branch' })
async function loadRank() {
  rankLoading.value = true
  try {
    const res = await axios.get(`${API}/rank`, {
      params: {
        orgId: rankFilter.orgId || undefined,
        year: rankFilter.year || undefined,
        scope: rankFilter.scope
      }
    })
    if (res.data.code === 200) rankList.value = res.data.data?.rankList || []
  } finally { rankLoading.value = false }
}

// ---------- 我的积分 ----------
const myScoreData = ref(null)
const myScoreLoading = ref(false)
const myScoreFilter = reactive({ memberId: null, year: null })
async function loadMyScore() {
  if (!myScoreFilter.memberId) { myScoreData.value = null; return }
  myScoreLoading.value = true
  try {
    const res = await axios.get(`${API}/my-score`, {
      params: { memberId: myScoreFilter.memberId, year: myScoreFilter.year || undefined }
    })
    if (res.data.code === 200) myScoreData.value = res.data.data
  } finally { myScoreLoading.value = false }
}

// ---------- 年度重置 ----------
const resetVisible = ref(false)
const resetForm = reactive({ year: new Date().getFullYear(), mode: 'carryover' })
function openReset() {
  resetForm.year = new Date().getFullYear()
  resetForm.mode = 'carryover'
  resetVisible.value = true
}
async function submitReset() {
  const modeText = resetForm.mode === 'reset' ? '清零（删除流水）' : '结转（保留流水）'
  try {
    await ElMessageBox.confirm(
      `确定对 ${resetForm.year} 年度执行【${modeText}】操作吗？`,
      '年度重置确认',
      { type: 'warning', confirmButtonText: '确认执行', cancelButtonText: '取消' }
    )
    await axios.post(`${API}/reset-year`, null, {
      params: { year: resetForm.year, mode: resetForm.mode }
    })
    ElMessage.success('年度重置已执行')
    resetVisible.value = false
    loadFlows()
  } catch (e) { if (e !== 'cancel') { /* 拦截器提示 */ } }
}

onMounted(() => {
  loadOrgs()
  loadMembers()
  loadRules()
  loadFlows()
  loadRank()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.bar { display: flex; gap: 10px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }
.tip { color: #909399; font-size: 12px; }
.mx { margin-left: 8px; }
.neg { color: #f56c6c; font-weight: 600; }
.my-summary { margin-bottom: 12px; padding: 12px; background: #f4f7fb; border-radius: 6px; }
.bottom-bar { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>

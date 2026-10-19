<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">党籍台账 · 党籍变动审批 · 党籍证明</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <el-tabs v-model="tab">
        <!-- ============ 党籍台账 ============ -->
        <el-tab-pane label="党籍台账" name="ledger">
          <div class="bar">
            <el-select v-model="ledgerFilter.orgId" placeholder="全部组织" clearable size="small" filterable
                       style="width:200px">
              <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
            </el-select>
            <el-select v-model="ledgerFilter.status" placeholder="党籍状态" clearable size="small" style="width:130px">
              <el-option v-for="s in dict.statuses" :key="s" :label="s" :value="s" />
            </el-select>
            <el-input v-model="ledgerFilter.keyword" placeholder="姓名/编号关键字" clearable size="small"
                      style="width:180px" @keyup.enter="loadLedger" />
            <el-button size="small" type="primary" @click="loadLedger">查询</el-button>
            <el-button v-if="can('R02','R04','R07','R08')" size="small" type="success" @click="openChange()">发起变动</el-button>
          </div>

          <el-table :data="ledger" size="small" border v-loading="ledgerLoading">
            <el-table-column prop="memberName" label="姓名" width="120" show-overflow-tooltip />
            <el-table-column label="党籍状态" width="110">
              <template #default="{ row }">
                <el-tag size="small" :type="statusType(row.currentStatus)">{{ row.currentStatus }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="joinDate" label="入党日期" width="110" />
            <el-table-column prop="fullMemberDate" label="转正日期" width="110" />
            <el-table-column prop="stopDate" label="停止党籍日期" width="120" />
            <el-table-column prop="restoreDate" label="恢复党籍日期" width="120" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="viewChanges(row)">变动时间线</el-button>
                <el-button v-if="can('R02','R07','R08')" size="small" link type="success" @click="openProof(row)">开具证明</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 变动审批 ============ -->
        <el-tab-pane label="变动审批" name="change">
          <div class="bar">
            <el-input v-model="changeFilter.memberId" placeholder="党员ID" clearable size="small" style="width:140px" />
            <el-select v-model="changeFilter.orgId" placeholder="全部组织" clearable size="small" filterable
                       style="width:200px">
              <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
            </el-select>
            <el-select v-model="changeFilter.auditStatus" placeholder="审批状态" clearable size="small" style="width:130px">
              <el-option v-for="s in ['待审批','已批准','已驳回']" :key="s" :label="s" :value="s" />
            </el-select>
            <el-button size="small" type="primary" @click="loadChanges">查询</el-button>
          </div>

          <el-table :data="changes" size="small" border v-loading="changeLoading">
            <el-table-column prop="memberName" label="姓名" width="110" show-overflow-tooltip />
            <el-table-column prop="changeType" label="变动类型" width="120" show-overflow-tooltip />
            <el-table-column prop="reason" label="变动原因" min-width="180" show-overflow-tooltip />
            <el-table-column prop="effectiveDate" label="生效日期" width="110" />
            <el-table-column label="审批状态" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="auditType(row.auditStatus)">{{ row.auditStatus }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="approver" label="审批人" width="100" />
            <el-table-column prop="approveDate" label="审批日期" width="110" />
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R03') && row.auditStatus === '待审批'"
                           size="small" link type="primary" @click="openAudit(row)">审批</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 党籍证明 ============ -->
        <el-tab-pane label="党籍证明" name="proof">
          <div class="bar">
            <el-input v-model="proofFilter.keyword" placeholder="证明编号/姓名关键字" clearable size="small"
                      style="width:200px" @keyup.enter="loadProofs" />
            <el-button size="small" type="primary" @click="loadProofs">查询</el-button>
            <el-button v-if="can('R02','R07','R08')" size="small" type="success" @click="openProof()">新建证明</el-button>
          </div>

          <el-table :data="proofs" size="small" border v-loading="proofLoading">
            <el-table-column prop="proofNo" label="证明编号" width="180" show-overflow-tooltip />
            <el-table-column prop="memberName" label="姓名" width="120" show-overflow-tooltip />
            <el-table-column prop="purpose" label="用途" min-width="220" show-overflow-tooltip />
            <el-table-column prop="issueDate" label="开具日期" width="120" />
            <el-table-column prop="issuer" label="开具人" width="120" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ============ 发起变动 ============ -->
    <el-dialog v-model="changeFormVisible" title="发起党籍变动" width="560px">
      <el-form :model="changeForm" label-width="110px">
        <el-form-item label="党员" required>
          <el-select v-model="changeForm.memberId" filterable style="width:100%" placeholder="选择党员">
            <el-option v-for="m in ledger" :key="m.memberId" :label="m.memberName" :value="m.memberId" />
          </el-select>
        </el-form-item>
        <el-form-item label="变动类型" required>
          <el-select v-model="changeForm.changeType" style="width:100%" placeholder="选择变动类型">
            <el-option v-for="t in dict.changeTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="变动原因" required>
          <el-input v-model="changeForm.reason" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="生效日期" required>
          <el-date-picker v-model="changeForm.effectiveDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="依据文号">
          <el-input v-model="changeForm.basisNo" placeholder="选填" />
        </el-form-item>
        <el-form-item label="支部大会日期">
          <el-date-picker v-model="changeForm.branchMeetingDate" type="date" value-format="YYYY-MM-DD" style="width:100%" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="changeFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitChange">提交</el-button>
      </template>
    </el-dialog>

    <!-- ============ 审批 ============ -->
    <el-dialog v-model="auditFormVisible" title="变动审批" width="520px">
      <el-form :model="auditForm" label-width="90px">
        <el-form-item label="审批结论" required>
          <el-radio-group v-model="auditForm.pass">
            <el-radio :value="true">批准</el-radio>
            <el-radio :value="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="auditForm.opinion" type="textarea" :rows="4" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">提交</el-button>
      </template>
    </el-dialog>

    <!-- ============ 开具证明 ============ -->
    <el-dialog v-model="proofFormVisible" title="开具党籍证明" width="520px">
      <el-form :model="proofForm" label-width="90px">
        <el-form-item label="党员" required>
          <el-select v-model="proofForm.memberId" filterable style="width:100%" placeholder="选择党员">
            <el-option v-for="m in ledger" :key="m.memberId" :label="m.memberName" :value="m.memberId" />
          </el-select>
        </el-form-item>
        <el-form-item label="用途" required>
          <el-input v-model="proofForm.purpose" type="textarea" :rows="3" maxlength="200" show-word-limit
                    placeholder="如：办理落户、子女入学、出国境审批等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="proofFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProof">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/membership'
const ORG_API = '/api/party/org/organization'

const tab = ref('ledger')
const orgFlat = ref([])
const dict = reactive({ changeTypes: [], statuses: [] })

async function loadOrgs() {
  const res = await axios.get(`${ORG_API}/tree`)
  if (res.data.code === 200) {
    const flat = []
    const walk = nodes => nodes.forEach(n => { flat.push(n); if (n.children) walk(n.children) })
    walk(res.data.data)
    orgFlat.value = flat
  }
}
async function loadDict() {
  const res = await axios.get(`${API}/dict`)
  if (res.data.code === 200) {
    const d = res.data.data || {}
    dict.changeTypes = d.changeTypes || []
    dict.statuses = d.statuses || []
  }
}
function statusType(s) {
  if (s === '正常') return 'success'
  if (s === '停止党籍') return 'warning'
  if (['除名', '开除党籍', '死亡注销', '自行脱党'].includes(s)) return 'danger'
  if (s === '转出') return 'info'
  if (s === '转入') return 'primary'
  return 'info'
}
function auditType(s) {
  return { 待审批: 'warning', 已批准: 'success', 已驳回: 'danger' }[s] || 'info'
}

// ---------- 台账 ----------
const ledgerLoading = ref(false)
const ledger = ref([])
const ledgerFilter = reactive({ orgId: null, status: '', keyword: '' })

async function loadLedger() {
  ledgerLoading.value = true
  try {
    const res = await axios.get(`${API}/ledger`, { params: {
      orgId: ledgerFilter.orgId || undefined,
      status: ledgerFilter.status || undefined,
      keyword: ledgerFilter.keyword || undefined
    } })
    if (res.data.code === 200) ledger.value = res.data.data
  } finally { ledgerLoading.value = false }
}

// ---------- 变动 ----------
const changeLoading = ref(false)
const changes = ref([])
const changeFilter = reactive({ memberId: '', orgId: null, auditStatus: '' })

async function loadChanges() {
  changeLoading.value = true
  try {
    const res = await axios.get(`${API}/changes`, { params: {
      memberId: changeFilter.memberId || undefined,
      orgId: changeFilter.orgId || undefined,
      auditStatus: changeFilter.auditStatus || undefined
    } })
    if (res.data.code === 200) changes.value = res.data.data
  } finally { changeLoading.value = false }
}

const changeFormVisible = ref(false)
const changeForm = reactive({ memberId: null, changeType: '', reason: '', effectiveDate: '', basisNo: '', branchMeetingDate: '' })

function openChange(row) {
  Object.assign(changeForm, { memberId: row ? row.memberId : null, changeType: '', reason: '', effectiveDate: '', basisNo: '', branchMeetingDate: '' })
  changeFormVisible.value = true
}
async function submitChange() {
  if (!changeForm.memberId) { ElMessage.error('请选择党员'); return }
  if (!changeForm.changeType) { ElMessage.error('请选择变动类型'); return }
  if (!changeForm.reason) { ElMessage.error('请填写变动原因'); return }
  if (!changeForm.effectiveDate) { ElMessage.error('请选择生效日期'); return }
  try {
    await axios.post(`${API}/change`, changeForm)
    ElMessage.success('已提交变动申请')
    changeFormVisible.value = false
    if (tab.value === 'change') loadChanges()
    else loadLedger()
  } catch { /* 拦截器提示 */ }
}
function viewChanges(row) {
  changeFilter.memberId = String(row.memberId)
  tab.value = 'change'
  loadChanges()
}

const auditFormVisible = ref(false)
const auditForm = reactive({ id: null, pass: true, opinion: '' })

function openAudit(row) {
  Object.assign(auditForm, { id: row.id, pass: true, opinion: '' })
  auditFormVisible.value = true
}
async function submitAudit() {
  try {
    await axios.post(`${API}/change/${auditForm.id}/audit`, { pass: auditForm.pass, opinion: auditForm.opinion })
    ElMessage.success('已提交审批')
    auditFormVisible.value = false
    loadChanges()
  } catch { /* 拦截器提示 */ }
}

// ---------- 证明 ----------
const proofLoading = ref(false)
const proofs = ref([])
const proofFilter = reactive({ keyword: '' })

async function loadProofs() {
  proofLoading.value = true
  try {
    const res = await axios.get(`${API}/proof/list`, { params: {
      keyword: proofFilter.keyword || undefined
    } })
    if (res.data.code === 200) proofs.value = res.data.data
  } finally { proofLoading.value = false }
}

const proofFormVisible = ref(false)
const proofForm = reactive({ memberId: null, purpose: '' })

function openProof(row) {
  Object.assign(proofForm, { memberId: row ? row.memberId : null, purpose: '' })
  proofFormVisible.value = true
}
async function submitProof() {
  if (!proofForm.memberId) { ElMessage.error('请选择党员'); return }
  if (!proofForm.purpose) { ElMessage.error('请填写证明用途'); return }
  try {
    await axios.post(`${API}/proof`, proofForm)
    ElMessage.success('已开具证明')
    proofFormVisible.value = false
    if (tab.value === 'proof') loadProofs()
  } catch { /* 拦截器提示 */ }
}

onMounted(() => { loadOrgs(); loadDict(); loadLedger(); loadChanges(); loadProofs() })
</script>

<style scoped>
.page-container { padding: 16px; }
.bar { display: flex; gap: 10px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }
</style>

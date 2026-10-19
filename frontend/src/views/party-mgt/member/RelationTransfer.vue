<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">申请→支部审核→党委审批→开介绍信→接收→回执归档 · 超期预警</el-tag>
    </RoleBar>

    <el-tabs v-model="activeTab" type="border-card" @tab-change="onTabChange">
      <!-- ========== 转接列表 ========== -->
      <el-tab-pane label="组织关系转接" name="transfer">
        <el-card shadow="never">
          <div class="bar">
            <el-select v-model="filter.orgId" placeholder="全部组织" clearable size="small" filterable
                       style="width:230px" @change="loadTransfers">
              <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
            </el-select>
            <el-select v-model="filter.status" placeholder="流程状态" clearable size="small" style="width:130px" @change="loadTransfers">
              <el-option label="在办" value="在办" />
              <el-option label="已驳回" value="已驳回" />
              <el-option label="已完成" value="已完成" />
            </el-select>
            <el-input v-model="filter.keyword" placeholder="姓名 / 介绍信编号" clearable size="small"
                      style="width:200px" @keyup.enter="loadTransfers" />
            <el-button size="small" @click="loadTransfers">查询</el-button>
            <el-button v-if="can('R02','R04','R07','R08')" size="small" type="primary" @click="openApply">新建申请</el-button>
          </div>

          <el-table :data="transfers" size="small" border v-loading="loading">
            <el-table-column prop="memberName" label="姓名" width="90" />
            <el-table-column prop="fromOrg" label="原组织" min-width="160" show-overflow-tooltip />
            <el-table-column prop="toOrg" label="目标组织" min-width="160" show-overflow-tooltip />
            <el-table-column prop="applyDate" label="申请日期" width="100" />
            <el-table-column prop="currentNode" label="当前节点" width="100" />
            <el-table-column label="流程状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="flowStatusType(row.flowStatus)">{{ row.flowStatus || '—' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="letterNo" label="介绍信编号" width="130" show-overflow-tooltip />
            <el-table-column label="有效期至" width="100">
              <template #default="{ row }">{{ row.expireDate || '—' }}</template>
            </el-table-column>
            <el-table-column label="超期天数" width="85">
              <template #default="{ row }">
                <el-tag v-if="row.overdueDays > 0" size="small" type="danger">{{ row.overdueDays }}天</el-tag>
                <span v-else>—</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="320" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.currentNode === '支部审核' && can('R07','R08')"
                           size="small" link type="primary" @click="openAudit(row, 'branch')">支部审核</el-button>
                <el-button v-if="row.currentNode === '党委审批' && can('R03')"
                           size="small" link type="primary" @click="openAudit(row, 'committee')">党委审批</el-button>
                <el-button v-if="row.currentNode === '开具介绍信' && can('R02','R04','R07','R08')"
                           size="small" link type="warning" @click="openIssue(row)">开具介绍信</el-button>
                <el-button v-if="row.currentNode === '接收确认' && can('R02','R04','R07','R08')"
                           size="small" link type="success" @click="openReceive(row)">接收确认</el-button>
                <el-button v-if="row.currentNode === '回执归档' && can('R02','R04','R07','R08')"
                           size="small" link type="info" @click="openReceipt(row)">回执归档</el-button>
                <el-button size="small" link @click="openFlows(row)">审批链</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- ========== 介绍信列表 ========== -->
      <el-tab-pane label="介绍信列表" name="letter">
        <el-card shadow="never">
          <div class="bar">
            <el-select v-model="letterFilter.status" placeholder="介绍信状态" clearable size="small" style="width:130px" @change="loadLetters">
              <el-option label="有效" value="有效" />
              <el-option label="已过期" value="已过期" />
              <el-option label="已接收" value="已接收" />
              <el-option label="已回执" value="已回执" />
            </el-select>
            <el-button size="small" @click="loadLetters">查询</el-button>
          </div>
          <el-table :data="letters" size="small" border v-loading="letterLoading">
            <el-table-column prop="letterNo" label="编号" min-width="140" show-overflow-tooltip />
            <el-table-column prop="memberName" label="党员" width="90" />
            <el-table-column prop="fromOrgName" label="原组织" min-width="160" show-overflow-tooltip />
            <el-table-column prop="toOrgName" label="目标组织" min-width="160" show-overflow-tooltip />
            <el-table-column prop="issueDate" label="开具日期" width="100" />
            <el-table-column prop="expireDate" label="有效期至" width="100" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="letterStatusType(row.status)">{{ row.status || '—' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- ========== 超期预警 ========== -->
      <el-tab-pane label="超期预警" name="alert">
        <el-card shadow="never">
          <el-table :data="alerts" size="small" border v-loading="alertLoading">
            <el-table-column prop="memberName" label="党员" width="90" />
            <el-table-column prop="toOrg" label="目标组织" min-width="160" show-overflow-tooltip />
            <el-table-column label="超期天数" width="90">
              <template #default="{ row }">
                <el-tag size="small" type="danger">{{ row.overdueDays }}天</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="预警级别" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="alertLevelType(row.alertLevel)">{{ row.alertLevel || '一般' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- ========== 新建申请弹窗 ========== -->
    <el-dialog v-model="applyVisible" title="发起组织关系转接申请" width="560px">
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="选择党员" required>
          <el-select v-model="applyForm.memberId" filterable style="width:100%" @change="onMemberChange">
            <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="原组织">
          <span>{{ applyForm.fromOrg || '—' }}</span>
        </el-form-item>
        <el-form-item label="目标组织" required>
          <el-select v-model="applyForm.toOrgId" filterable style="width:100%" @change="onToOrgChange">
            <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请理由" required>
          <el-input v-model="applyForm.reason" type="textarea" :rows="3" maxlength="500" show-word-limit
                    placeholder="请填写转接申请理由" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- ========== 审批弹窗（支部审核 / 党委审批） ========== -->
    <el-dialog v-model="auditVisible" :title="auditTitle" width="560px">
      <el-form label-width="100px">
        <el-form-item label="党员">{{ auditForm.memberName }}</el-form-item>
        <el-form-item label="原组织">{{ auditForm.fromOrg }}</el-form-item>
        <el-form-item label="目标组织">{{ auditForm.toOrg }}</el-form-item>
        <el-form-item label="申请理由">{{ auditForm.reason }}</el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="auditForm.opinion" type="textarea" :rows="3" maxlength="500" show-word-limit
                    placeholder="请填写审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="danger" @click="submitAudit(false)">驳回</el-button>
        <el-button type="success" @click="submitAudit(true)">通过</el-button>
      </template>
    </el-dialog>

    <!-- ========== 开具介绍信弹窗 ========== -->
    <el-dialog v-model="issueVisible" title="开具党员组织关系介绍信" width="520px">
      <el-form label-width="100px">
        <el-form-item label="党员">{{ issueForm.memberName }}</el-form-item>
        <el-form-item label="原组织">{{ issueForm.fromOrg }}</el-form-item>
        <el-form-item label="目标组织">{{ issueForm.toOrg }}</el-form-item>
        <el-form-item label="有效期天数" required>
          <el-input-number v-model="issueForm.validDays" :min="1" :max="180" :step="1" />
          <span class="tip">默认30天</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="issueVisible = false">取消</el-button>
        <el-button type="primary" @click="submitIssue">开具</el-button>
      </template>
    </el-dialog>

    <!-- ========== 接收确认弹窗 ========== -->
    <el-dialog v-model="receiveVisible" title="接收确认" width="520px">
      <el-form label-width="100px">
        <el-form-item label="党员">{{ receiveForm.memberName }}</el-form-item>
        <el-form-item label="原组织">{{ receiveForm.fromOrg }}</el-form-item>
        <el-form-item label="目标组织">{{ receiveForm.toOrg }}</el-form-item>
        <el-form-item label="介绍信编号">{{ receiveForm.letterNo }}</el-form-item>
        <el-form-item label="接收人" required>
          <el-input v-model="receiveForm.receiver" placeholder="请输入接收人姓名" maxlength="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="receiveVisible = false">取消</el-button>
        <el-button type="success" @click="submitReceive">确认接收</el-button>
      </template>
    </el-dialog>

    <!-- ========== 回执归档弹窗 ========== -->
    <el-dialog v-model="receiptVisible" title="回执归档" width="520px">
      <el-form label-width="100px">
        <el-form-item label="党员">{{ receiptForm.memberName }}</el-form-item>
        <el-form-item label="原组织">{{ receiptForm.fromOrg }}</el-form-item>
        <el-form-item label="目标组织">{{ receiptForm.toOrg }}</el-form-item>
        <el-form-item label="介绍信编号">{{ receiptForm.letterNo }}</el-form-item>
        <el-form-item label="回执编号" required>
          <el-input v-model="receiptForm.receiptNo" placeholder="请输入回执编号" maxlength="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="receiptVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReceipt">确认归档</el-button>
      </template>
    </el-dialog>

    <!-- ========== 审批链弹窗 ========== -->
    <el-dialog v-model="flowVisible" title="审批链记录" width="680px">
      <el-timeline>
        <el-timeline-item
          v-for="(f, idx) in flows"
          :key="idx"
          :type="flowItemType(f.status)"
          :timestamp="formatTime(f.auditTime)"
        >
          <div class="flow-item">
            <b>{{ f.node }}</b>
            <span class="tip">审批人：{{ f.approver || '—' }}</span>
            <span class="tip">结果：{{ f.status || '—' }}</span>
            <p v-if="f.opinion" class="opinion">意见：{{ f.opinion }}</p>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/member/relation-transfer'
const ORG_API = '/api/party/org/organization'
const MEMBER_API = '/api/party/member/info'

const activeTab = ref('transfer')
const loading = ref(false)
const letterLoading = ref(false)
const alertLoading = ref(false)

const transfers = ref([])
const letters = ref([])
const alerts = ref([])
const orgFlat = ref([])
const members = ref([])
const flows = ref([])

const filter = reactive({ orgId: null, status: '', keyword: '' })
const letterFilter = reactive({ status: '' })

// ---------- 组织与党员数据 ----------
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

// ---------- 列表加载 ----------
async function loadTransfers() {
  loading.value = true
  try {
    const res = await axios.get(`${API}/list`, {
      params: {
        orgId: filter.orgId || undefined,
        status: filter.status || undefined,
        keyword: filter.keyword || undefined
      }
    })
    if (res.data.code === 200) transfers.value = res.data.data || []
  } finally { loading.value = false }
}

async function loadLetters() {
  letterLoading.value = true
  try {
    const res = await axios.get(`${API}/letters`, {
      params: { status: letterFilter.status || undefined }
    })
    if (res.data.code === 200) letters.value = res.data.data || []
  } finally { letterLoading.value = false }
}

async function loadAlerts() {
  alertLoading.value = true
  try {
    const res = await axios.get(`${API}/alerts`)
    if (res.data.code === 200) alerts.value = res.data.data || []
  } finally { alertLoading.value = false }
}

function onTabChange(tab) {
  if (tab === 'letter') loadLetters()
  if (tab === 'alert') loadAlerts()
}

// ---------- 状态样式 ----------
function flowStatusType(s) {
  return { '在办': 'warning', '已驳回': 'danger', '已完成': 'success' }[s] || 'info'
}
function letterStatusType(s) {
  return { '有效': 'success', '已过期': 'danger', '已接收': 'primary', '已回执': 'info' }[s] || 'info'
}
function alertLevelType(l) {
  return { '严重': 'danger', '较高': 'warning', '一般': 'info' }[l] || 'info'
}
function flowItemType(s) {
  return { '通过': 'success', '驳回': 'danger', '待审批': 'warning' }[s] || 'primary'
}
function formatTime(t) {
  if (!t) return '—'
  return t.replace('T', ' ')
}

// ---------- 新建申请 ----------
const applyVisible = ref(false)
const applyForm = reactive({ memberId: null, fromOrg: '', toOrgId: null, toOrg: '', reason: '' })

function openApply() {
  Object.assign(applyForm, { memberId: null, fromOrg: '', toOrgId: null, toOrg: '', reason: '' })
  applyVisible.value = true
}
function onMemberChange(id) {
  const m = members.value.find(x => x.id === id)
  if (m) applyForm.fromOrg = m.orgName || ''
}
function onToOrgChange(id) {
  const o = orgFlat.value.find(x => x.id === id)
  if (o) applyForm.toOrg = o.name
}
async function submitApply() {
  if (!applyForm.memberId) { ElMessage.warning('请选择党员'); return }
  if (!applyForm.toOrgId) { ElMessage.warning('请选择目标组织'); return }
  if (!applyForm.reason?.trim()) { ElMessage.warning('请填写申请理由'); return }
  try {
    await axios.post(API, {
      memberId: applyForm.memberId,
      toOrgId: applyForm.toOrgId,
      toOrg: applyForm.toOrg,
      reason: applyForm.reason,
      fromOrg: applyForm.fromOrg
    })
    ElMessage.success('申请已提交')
    applyVisible.value = false
    loadTransfers()
  } catch { /* 拦截器提示 */ }
}

// ---------- 审批（支部 / 党委） ----------
const auditVisible = ref(false)
const auditType = ref('')
const auditForm = reactive({ id: null, memberName: '', fromOrg: '', toOrg: '', reason: '', opinion: '' })
const auditTitle = computed(() => auditType.value === 'branch' ? '支部审核' : '党委审批')

function openAudit(row, type) {
  auditType.value = type
  Object.assign(auditForm, {
    id: row.id, memberName: row.memberName, fromOrg: row.fromOrg,
    toOrg: row.toOrg, reason: row.reason || row.applyReason || '', opinion: ''
  })
  auditVisible.value = true
}
async function submitAudit(pass) {
  if (!auditForm.id) return
  const url = auditType.value === 'branch'
    ? `${API}/${auditForm.id}/branch-audit`
    : `${API}/${auditForm.id}/committee-audit`
  try {
    await axios.post(url, null, {
      params: { pass, opinion: auditForm.opinion || '' }
    })
    ElMessage.success(pass ? '审批已通过' : '已驳回')
    auditVisible.value = false
    loadTransfers()
  } catch { /* 拦截器提示 */ }
}

// ---------- 开具介绍信 ----------
const issueVisible = ref(false)
const issueForm = reactive({ id: null, memberName: '', fromOrg: '', toOrg: '', validDays: 30 })

function openIssue(row) {
  Object.assign(issueForm, {
    id: row.id, memberName: row.memberName, fromOrg: row.fromOrg, toOrg: row.toOrg, validDays: 30
  })
  issueVisible.value = true
}
async function submitIssue() {
  if (!issueForm.id) return
  try {
    await axios.post(`${API}/${issueForm.id}/issue-letter`, null, {
      params: { validDays: issueForm.validDays }
    })
    ElMessage.success('介绍信已开具')
    issueVisible.value = false
    loadTransfers()
  } catch { /* 拦截器提示 */ }
}

// ---------- 接收确认 ----------
const receiveVisible = ref(false)
const receiveForm = reactive({ id: null, memberName: '', fromOrg: '', toOrg: '', letterNo: '', receiver: '' })

function openReceive(row) {
  Object.assign(receiveForm, {
    id: row.id, memberName: row.memberName, fromOrg: row.fromOrg,
    toOrg: row.toOrg, letterNo: row.letterNo || '', receiver: ''
  })
  receiveVisible.value = true
}
async function submitReceive() {
  if (!receiveForm.receiver?.trim()) { ElMessage.warning('请输入接收人'); return }
  try {
    await axios.post(`${API}/${receiveForm.id}/receive`, null, {
      params: { receiver: receiveForm.receiver }
    })
    ElMessage.success('接收已确认')
    receiveVisible.value = false
    loadTransfers()
  } catch { /* 拦截器提示 */ }
}

// ---------- 回执归档 ----------
const receiptVisible = ref(false)
const receiptForm = reactive({ id: null, memberName: '', fromOrg: '', toOrg: '', letterNo: '', receiptNo: '' })

function openReceipt(row) {
  Object.assign(receiptForm, {
    id: row.id, memberName: row.memberName, fromOrg: row.fromOrg,
    toOrg: row.toOrg, letterNo: row.letterNo || '', receiptNo: ''
  })
  receiptVisible.value = true
}
async function submitReceipt() {
  if (!receiptForm.receiptNo?.trim()) { ElMessage.warning('请输入回执编号'); return }
  try {
    await axios.post(`${API}/${receiptForm.id}/receipt`, null, {
      params: { receiptNo: receiptForm.receiptNo }
    })
    ElMessage.success('回执已归档')
    receiptVisible.value = false
    loadTransfers()
  } catch { /* 拦截器提示 */ }
}

// ---------- 审批链 ----------
const flowVisible = ref(false)

async function openFlows(row) {
  flowVisible.value = true
  try {
    const res = await axios.get(`${API}/${row.id}/flows`)
    if (res.data.code === 200) flows.value = res.data.data || []
  } catch { /* 拦截器提示 */ }
}

onMounted(() => {
  loadOrgs().then(() => {
    loadTransfers()
    loadMembers()
  })
})
</script>

<style scoped>
.page-container { padding: 16px; }
.bar { display: flex; gap: 10px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }
.tip { color: #909399; font-size: 12px; margin-left: 8px; }
.flow-item { display: flex; flex-direction: column; gap: 4px; }
.flow-item b { font-size: 14px; }
.flow-item .opinion { margin: 4px 0 0; color: #606266; font-size: 13px; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">流出流入 · 双向联系 · 活动证管理 · 失联预警</el-tag>
    </RoleBar>

    <el-tabs v-model="activeTab" type="border-card" @tab-change="onTabChange">
      <!-- ========== 流动党员 ========== -->
      <el-tab-pane label="流动党员" name="member">
        <el-card shadow="never">
          <div class="bar">
            <el-select v-model="filter.orgId" placeholder="全部组织" clearable size="small" filterable
                       style="width:230px" @change="loadList">
              <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
            </el-select>
            <el-select v-model="filter.flowType" placeholder="流动类型" clearable size="small" style="width:110px" @change="loadList">
              <el-option label="流出" value="流出" />
              <el-option label="流入" value="流入" />
            </el-select>
            <el-input v-model="filter.keyword" placeholder="姓名 / 流向 / 证号" clearable size="small"
                      style="width:180px" @keyup.enter="loadList" />
            <el-button size="small" @click="loadList">查询</el-button>
            <el-button v-if="can('R02','R04','R07','R08')" size="small" type="primary" @click="openRegister('流出')">流出登记</el-button>
            <el-button v-if="can('R02','R04','R07','R08')" size="small" type="success" @click="openRegister('流入')">流入登记</el-button>
          </div>

          <el-table :data="list" size="small" border v-loading="loading">
            <el-table-column prop="memberName" label="姓名" width="90" fixed />
            <el-table-column label="流动类型" width="85">
              <template #default="{ row }">
                <el-tag size="small" :type="row.flowType === '流出' ? 'warning' : 'success'">{{ row.flowType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="orgName" label="原组织" min-width="150" show-overflow-tooltip />
            <el-table-column prop="destination" label="目标组织" min-width="150" show-overflow-tooltip />
            <el-table-column prop="startDate" label="流出日期" width="100" />
            <el-table-column prop="contactPhone" label="联系电话" width="120" />
            <el-table-column label="最后联系日期" width="105">
              <template #default="{ row }">{{ row.lastContactDate || '—' }}</template>
            </el-table-column>
            <el-table-column label="联系周期" width="85">
              <template #default="{ row }">{{ row.contactCycle != null ? row.contactCycle + '个月' : '—' }}</template>
            </el-table-column>
            <el-table-column label="失联状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="alertStatusType(row.floatAlertStatus)">{{ row.floatAlertStatus || '正常' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="活动证号" width="120">
              <template #default="{ row }">{{ row.certNo || '—' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="170" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.flowStatus === '流出中' && can('R02','R04','R07','R08')"
                           size="small" link type="success" @click="returnBack(row)">返回销号</el-button>
                <el-button size="small" link type="primary" @click="openContactFor(row)">联系记录</el-button>
                <el-button v-if="!row.certNo && row.flowStatus === '流出中' && can('R02','R04','R07','R08')"
                           size="small" link type="warning" @click="openIssueCert(row)">发活动证</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- ========== 联系记录 ========== -->
      <el-tab-pane label="联系记录" name="contact">
        <el-card shadow="never">
          <div class="bar">
            <el-select v-model="contactFloatId" placeholder="选择流动党员" clearable size="small" filterable
                       style="width:280px" @change="loadContacts">
              <el-option v-for="f in list" :key="f.id" :label="`${f.memberName}（${f.flowType}·${f.destination || ''}）`" :value="f.id" />
            </el-select>
            <el-button v-if="contactFloatId && can('R02','R04','R07','R08')" size="small" type="primary"
                       @click="openContact()">登记联系记录</el-button>
          </div>
          <el-table :data="contacts" size="small" border v-loading="contactLoading">
            <el-table-column prop="contactDate" label="联系日期" width="110" />
            <el-table-column prop="contactWay" label="联系方式" width="110" />
            <el-table-column prop="recorder" label="联系人" width="110" />
            <el-table-column prop="content" label="内容摘要" min-width="240" show-overflow-tooltip />
            <el-table-column label="下次联系日期" width="120">
              <template #default="{ row }">{{ nextContactDate(row.contactDate) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- ========== 活动证 ========== -->
      <el-tab-pane label="活动证" name="cert">
        <el-card shadow="never">
          <div class="bar">
            <el-select v-model="certFloatId" placeholder="按流动党员筛选" clearable size="small" filterable
                       style="width:280px">
              <el-option v-for="f in list" :key="f.id" :label="`${f.memberName}（${f.flowType}·${f.destination || ''}）`" :value="f.id" />
            </el-select>
            <el-select v-model="certFilter.status" placeholder="状态" clearable size="small" style="width:110px" @change="loadCerts">
              <el-option label="有效" value="有效" />
              <el-option label="已注销" value="已注销" />
            </el-select>
            <el-button size="small" @click="loadCerts">查询</el-button>
            <el-button v-if="can('R02','R04','R07','R08')" size="small" type="primary" @click="openIssueCert()">发放活动证</el-button>
          </div>
          <el-table :data="certRows" size="small" border v-loading="certLoading">
            <el-table-column prop="certNo" label="证号" width="150" />
            <el-table-column label="持证党员" width="110">
              <template #default="{ row }">{{ floatName(row.floatingId) }}</template>
            </el-table-column>
            <el-table-column prop="issueDate" label="发放日期" width="110" />
            <el-table-column label="有效期至" width="110">
              <template #default="{ row }">{{ certExpireDate(row.issueDate) }}</template>
            </el-table-column>
            <el-table-column prop="issuer" label="发放人" width="110" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === '有效' ? 'success' : 'info'">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === '有效' && can('R02','R04','R07','R08')"
                           size="small" link type="danger" @click="revokeCert(row)">注销</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- ========== 失联预警 ========== -->
      <el-tab-pane label="失联预警" name="alert">
        <el-card shadow="never">
          <el-table :data="alerts" size="small" border v-loading="alertLoading">
            <el-table-column prop="memberName" label="党员" width="100" />
            <el-table-column prop="orgName" label="原组织" min-width="150" show-overflow-tooltip />
            <el-table-column prop="destination" label="流向" min-width="130" show-overflow-tooltip />
            <el-table-column prop="contactPhone" label="联系电话" width="120" />
            <el-table-column label="最后联系日期" width="110">
              <template #default="{ row }">{{ row.lastContactDate || '—' }}</template>
            </el-table-column>
            <el-table-column label="未联系月数" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="row.alertLevel === '告警' ? 'danger' : 'warning'">{{ row.noContactMonths }}个月</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="预警级别" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="alertLevelType(row.alertLevel)">{{ row.alertLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="alertMessage" label="预警说明" min-width="220" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- ========== 流出/流入登记弹窗 ========== -->
    <el-dialog v-model="regVisible" :title="regForm.flowType === '流出' ? '流出登记' : '流入登记'" width="600px">
      <el-form :model="regForm" label-width="100px">
        <el-form-item label="选择党员" required>
          <el-select v-model="regForm.memberId" filterable style="width:100%" @change="onMemberChange">
            <el-option v-for="m in members" :key="m.id" :label="`${m.name}（${m.orgName || ''}）`" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="原组织">
          <span>{{ regForm.orgName || '—' }}</span>
        </el-form-item>
        <el-form-item :label="regForm.flowType === '流出' ? '流向单位' : '来源单位'" required>
          <el-input v-model="regForm.destination" maxlength="100"
                    :placeholder="regForm.flowType === '流出' ? '党员流向的党组织/单位' : '党员流入前的党组织/单位'" />
        </el-form-item>
        <el-form-item :label="regForm.flowType === '流出' ? '流出日期' : '流入日期'" required>
          <el-date-picker v-model="regForm.startDate" type="date" value-format="YYYY-MM-DD" style="width:180px" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="regForm.contactPhone" maxlength="20" placeholder="流动期间联系电话" />
        </el-form-item>
        <el-form-item label="联系周期">
          <el-input-number v-model="regForm.contactCycle" :min="1" :max="12" />
          <span class="tip">个月联系一次（默认3个月）</span>
        </el-form-item>
        <el-form-item label="活动情况">
          <el-input v-model="regForm.activitySituation" type="textarea" :rows="2" maxlength="500"
                    placeholder="参加组织生活情况（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="regVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRegister">保存</el-button>
      </template>
    </el-dialog>

    <!-- ========== 联系记录登记弹窗 ========== -->
    <el-dialog v-model="contactVisible" title="登记双向联系记录" width="560px">
      <el-form :model="contactForm" label-width="100px">
        <el-form-item label="流动党员">{{ contactForm.memberName }}</el-form-item>
        <el-form-item label="联系日期" required>
          <el-date-picker v-model="contactForm.contactDate" type="date" value-format="YYYY-MM-DD" style="width:180px" />
        </el-form-item>
        <el-form-item label="联系方式" required>
          <el-select v-model="contactForm.contactWay" style="width:180px">
            <el-option v-for="w in ['电话','微信','视频','走访','书信','其他']" :key="w" :label="w" :value="w" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容摘要" required>
          <el-input v-model="contactForm.content" type="textarea" :rows="3" maxlength="500" show-word-limit
                    placeholder="联系情况、思想动态、缴纳党费情况等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="contactVisible = false">取消</el-button>
        <el-button type="primary" @click="submitContact">保存</el-button>
      </template>
    </el-dialog>

    <!-- ========== 发放活动证弹窗 ========== -->
    <el-dialog v-model="certVisible" title="发放流动党员活动证" width="520px">
      <el-form :model="certForm" label-width="100px">
        <el-form-item label="流动党员" required>
          <el-select v-model="certForm.floatingId" filterable style="width:100%">
            <el-option v-for="f in issueableList" :key="f.id"
                       :label="`${f.memberName}（${f.flowType}·${f.destination || ''}）`" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动证号" required>
          <el-input v-model="certForm.certNo" maxlength="50" placeholder="请输入活动证编号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="certVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCert">发放</el-button>
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

const API = '/api/party/member/floating'
const ORG_API = '/api/party/org/organization'
const MEMBER_API = '/api/party/member/info'

const activeTab = ref('member')
const loading = ref(false)
const contactLoading = ref(false)
const certLoading = ref(false)
const alertLoading = ref(false)

const list = ref([])
const contacts = ref([])
const certs = ref([])
const alerts = ref([])
const orgFlat = ref([])
const members = ref([])

const filter = reactive({ orgId: null, flowType: '', keyword: '' })
const contactFloatId = ref(null)
const certFloatId = ref(null)
const certFilter = reactive({ status: '' })

// ---------- 基础数据 ----------
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

// ---------- 流动党员列表 ----------
async function loadList() {
  loading.value = true
  try {
    const res = await axios.get(`${API}/list`, {
      params: {
        orgId: filter.orgId || undefined,
        flowType: filter.flowType || undefined,
        keyword: filter.keyword || undefined
      }
    })
    if (res.data.code === 200) list.value = res.data.data || []
  } finally { loading.value = false }
}

function alertStatusType(s) {
  return { '正常': 'success', '预警': 'warning', '告警': 'danger' }[s] || 'success'
}
function alertLevelType(l) {
  return { '告警': 'danger', '预警': 'warning', '正常': 'success' }[l] || 'info'
}

// ---------- 流出 / 流入登记 ----------
const regVisible = ref(false)
const regForm = reactive({})

function openRegister(flowType) {
  Object.keys(regForm).forEach(k => delete regForm[k])
  Object.assign(regForm, {
    memberId: null, memberName: '', orgId: null, orgName: '',
    flowType, destination: '', startDate: '', contactPhone: '',
    contactCycle: 3, activitySituation: ''
  })
  regVisible.value = true
}
function onMemberChange(id) {
  const m = members.value.find(x => x.id === id)
  if (m) {
    regForm.memberName = m.name
    regForm.orgId = m.orgId
    regForm.orgName = m.orgName || ''
    if (!regForm.contactPhone) regForm.contactPhone = m.phone || ''
  }
}
async function submitRegister() {
  if (!regForm.memberId) { ElMessage.warning('请选择党员'); return }
  if (!regForm.destination?.trim()) { ElMessage.warning('请填写流向/来源单位'); return }
  if (!regForm.startDate) { ElMessage.warning('请选择日期'); return }
  if (!regForm.contactPhone?.trim()) { ElMessage.warning('请填写联系电话'); return }
  const url = regForm.flowType === '流出' ? `${API}/out` : `${API}/in`
  try {
    await axios.post(url, {
      memberId: regForm.memberId, memberName: regForm.memberName,
      orgId: regForm.orgId, orgName: regForm.orgName,
      destination: regForm.destination, startDate: regForm.startDate,
      contactPhone: regForm.contactPhone, contactCycle: regForm.contactCycle,
      activitySituation: regForm.activitySituation
    })
    ElMessage.success(regForm.flowType === '流出' ? '流出登记已保存' : '流入登记已保存')
    regVisible.value = false
    loadList()
  } catch { /* 拦截器提示 */ }
}

// ---------- 返回销号 ----------
async function returnBack(row) {
  try {
    await ElMessageBox.confirm(
      `确认 ${row.memberName} 已返回原组织？销号后流动状态结束，活动证将同步注销。`, '返回销号', { type: 'warning' })
    await axios.post(`${API}/${row.id}/return`)
    ElMessage.success('已返回销号')
    loadList()
  } catch (e) { if (e !== 'cancel') { /* 业务错误已提示 */ } }
}

// ---------- 联系记录 ----------
async function loadContacts() {
  if (!contactFloatId.value) { contacts.value = []; return }
  contactLoading.value = true
  try {
    const res = await axios.get(`${API}/${contactFloatId.value}/contacts`)
    if (res.data.code === 200) contacts.value = res.data.data || []
  } finally { contactLoading.value = false }
}

const contactVisible = ref(false)
const contactForm = reactive({})

function openContactFor(row) {
  activeTab.value = 'contact'
  contactFloatId.value = row.id
  loadContacts()
}
function openContact() {
  const f = list.value.find(x => x.id === contactFloatId.value)
  if (!f) return
  Object.keys(contactForm).forEach(k => delete contactForm[k])
  Object.assign(contactForm, {
    floatingId: f.id, memberId: f.memberId, memberName: f.memberName,
    contactDate: new Date().toISOString().slice(0, 10), contactWay: '', content: ''
  })
  contactVisible.value = true
}
async function submitContact() {
  if (!contactForm.contactDate) { ElMessage.warning('请选择联系日期'); return }
  if (!contactForm.contactWay) { ElMessage.warning('请选择联系方式'); return }
  if (!contactForm.content?.trim()) { ElMessage.warning('请填写内容摘要'); return }
  try {
    await axios.post(`${API}/contacts`, {
      floatingId: contactForm.floatingId, memberId: contactForm.memberId,
      contactDate: contactForm.contactDate, contactWay: contactForm.contactWay,
      content: contactForm.content
    })
    ElMessage.success('联系记录已登记')
    contactVisible.value = false
    loadContacts()
    loadList()
  } catch { /* 拦截器提示 */ }
}
function nextContactDate(contactDate) {
  const f = list.value.find(x => x.id === contactFloatId.value)
  if (!contactDate || !f || f.contactCycle == null) return '—'
  const d = new Date(contactDate)
  d.setMonth(d.getMonth() + Number(f.contactCycle))
  return d.toISOString().slice(0, 10)
}

// ---------- 活动证 ----------
async function loadCerts() {
  certLoading.value = true
  try {
    const res = await axios.get(`${API}/certs`, {
      params: { status: certFilter.status || undefined }
    })
    if (res.data.code === 200) certs.value = res.data.data || []
  } finally { certLoading.value = false }
}
const certRows = computed(() =>
  certFloatId.value ? certs.value.filter(c => c.floatingId === certFloatId.value) : certs.value)

function floatName(floatingId) {
  const f = list.value.find(x => x.id === floatingId)
  return f ? f.memberName : '—'
}
function certExpireDate(issueDate) {
  if (!issueDate) return '—'
  const d = new Date(issueDate)
  d.setFullYear(d.getFullYear() + 1)
  return d.toISOString().slice(0, 10)
}

const certVisible = ref(false)
const certForm = reactive({ floatingId: null, certNo: '' })
const issueableList = computed(() => list.value.filter(f => f.flowStatus === '流出中' && !f.certNo))

function openIssueCert(row) {
  certForm.floatingId = row ? row.id : null
  certForm.certNo = ''
  certVisible.value = true
}
async function submitCert() {
  if (!certForm.floatingId) { ElMessage.warning('请选择流动党员'); return }
  if (!certForm.certNo?.trim()) { ElMessage.warning('请输入活动证号'); return }
  try {
    await axios.post(`${API}/${certForm.floatingId}/cert`, null, {
      params: { certNo: certForm.certNo }
    })
    ElMessage.success('活动证已发放')
    certVisible.value = false
    loadCerts()
    loadList()
  } catch { /* 拦截器提示 */ }
}
async function revokeCert(row) {
  try {
    await ElMessageBox.confirm(`确认注销活动证 ${row.certNo}？`, '注销活动证', { type: 'warning' })
    await axios.post(`${API}/certs/${row.id}/revoke`)
    ElMessage.success('活动证已注销')
    loadCerts()
    loadList()
  } catch (e) { if (e !== 'cancel') { /* 业务错误已提示 */ } }
}

// ---------- 失联预警 ----------
async function loadAlerts() {
  alertLoading.value = true
  try {
    const res = await axios.get(`${API}/alerts`)
    if (res.data.code === 200) alerts.value = res.data.data || []
  } finally { alertLoading.value = false }
}

function onTabChange(tab) {
  if (tab === 'cert') loadCerts()
  if (tab === 'alert') loadAlerts()
}

onMounted(() => {
  loadOrgs()
  loadMembers()
  loadList()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.bar { display: flex; gap: 10px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }
.tip { color: #909399; font-size: 12px; margin-left: 8px; }
</style>

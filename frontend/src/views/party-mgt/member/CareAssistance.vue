<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">困难台账 · 走访慰问 · 帮扶申请 · 公示合规</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <!-- ============ 困难台账 ============ -->
        <el-tab-pane label="困难台账" name="difficulty">
          <div class="bar">
            <el-input v-model="diffFilter.keyword" placeholder="姓名/原因关键字" clearable size="small"
                      style="width:220px" @change="loadDifficulties" />
            <el-select v-model="diffFilter.status" placeholder="状态" clearable size="small" style="width:120px"
                       @change="loadDifficulties">
              <el-option label="在困" value="在困" />
              <el-option label="已退出" value="已退出" />
            </el-select>
            <el-button v-if="can('R02','R04','R07','R08')" size="small" type="primary" @click="openDifficulty()">登记困难</el-button>
          </div>
          <el-table :data="difficulties" size="small" border v-loading="diffLoading">
            <el-table-column prop="memberName" label="姓名" width="100" />
            <el-table-column prop="difficultyType" label="困难类型" width="110" />
            <el-table-column prop="reason" label="困难原因" min-width="200" show-overflow-tooltip />
            <el-table-column prop="registerDate" label="登记日期" width="110" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === '在困' ? 'danger' : 'success'">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="帮扶金额(累计)" width="120">
              <template #default="{ row }">￥{{ money(row.totalGrantAmount) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === '在困' && can('R02','R04','R07','R08')"
                           size="small" link type="warning" @click="exitDifficulty(row)">退出困难</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 走访慰问 ============ -->
        <el-tab-pane label="走访慰问" name="visit">
          <div class="bar">
            <el-input v-model="visitFilter.keyword" placeholder="姓名/内容关键字" clearable size="small"
                      style="width:220px" @change="loadVisits" />
            <el-select v-model="visitFilter.node" placeholder="慰问节点" clearable size="small" style="width:150px"
                       @change="loadVisits">
              <el-option v-for="n in visitNodes" :key="n" :label="n" :value="n" />
            </el-select>
            <el-button v-if="can('R02','R04','R07','R08')" size="small" type="primary" @click="openVisit()">登记慰问</el-button>
          </div>
          <el-table :data="visits" size="small" border v-loading="visitLoading">
            <el-table-column prop="memberName" label="姓名" width="100" />
            <el-table-column prop="node" label="慰问节点" width="120" />
            <el-table-column prop="visitDate" label="慰问日期" width="110" />
            <el-table-column prop="visitor" label="慰问人" width="120" />
            <el-table-column prop="content" label="慰问内容" min-width="200" show-overflow-tooltip />
            <el-table-column label="慰问金额" width="100">
              <template #default="{ row }">￥{{ money(row.amount) }}</template>
            </el-table-column>
            <el-table-column label="重复" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.duplicate" size="small" type="warning">重复</el-tag>
                <span v-else class="tip">—</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="openVisit(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 帮扶申请 ============ -->
        <el-tab-pane label="帮扶申请" name="help">
          <div class="bar">
            <el-input v-model="helpFilter.keyword" placeholder="姓名/理由关键字" clearable size="small"
                      style="width:220px" @change="loadHelps" />
            <el-select v-model="helpFilter.approveStatus" placeholder="审批状态" clearable size="small" style="width:130px"
                       @change="loadHelps">
              <el-option label="待审批" value="待审批" />
              <el-option label="已批准" value="已批准" />
              <el-option label="已驳回" value="已驳回" />
            </el-select>
            <el-button v-if="can('R02','R04','R07','R08')" size="small" type="primary" @click="openHelp()">登记帮扶</el-button>
          </div>
          <el-table :data="helps" size="small" border v-loading="helpLoading">
            <el-table-column prop="memberName" label="姓名" width="100" />
            <el-table-column prop="helpType" label="帮扶类型" width="110" />
            <el-table-column label="申请金额" width="110">
              <template #default="{ row }">￥{{ money(row.applyAmount) }}</template>
            </el-table-column>
            <el-table-column prop="reason" label="申请理由" min-width="200" show-overflow-tooltip />
            <el-table-column label="审批状态" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="approveType(row.approveStatus)">{{ row.approveStatus || '待审批' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="发放状态" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="row.grantStatus === '已发放' ? 'success' : 'info'">{{ row.grantStatus || '未发放' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R03') && (row.approveStatus === '待审批' || !row.approveStatus)"
                           size="small" link type="warning" @click="openApprove(row)">审批</el-button>
                <el-button v-if="can('R02','R04','R07','R08') && row.approveStatus === '已批准' && row.grantStatus !== '已发放'"
                           size="small" link type="success" @click="grantHelp(row)">发放</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 公示合规 ============ -->
        <el-tab-pane label="公示合规" name="publicity">
          <div class="bar">
            <el-button size="small" type="primary" @click="loadPublicity">刷新合规检查</el-button>
            <span class="tip">金额 &gt; ￥10,000 视为不合规，需按规定补充公示程序</span>
          </div>
          <el-table :data="publicity" size="small" border v-loading="publicityLoading">
            <el-table-column prop="memberName" label="姓名" width="110" />
            <el-table-column label="帮扶金额" width="130">
              <template #default="{ row }">￥{{ money(row.amount) }}</template>
            </el-table-column>
            <el-table-column label="是否合规" width="110">
              <template #default="{ row }">
                <el-tag size="small" :type="row.compliant ? 'success' : 'danger'">
                  {{ row.compliant ? '合规' : '不合规' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="不合规原因" min-width="240" show-overflow-tooltip>
              <template #default="{ row }">{{ row.compliant ? '—' : (row.reason || '—') }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ============ 困难登记弹窗 ============ -->
    <el-dialog v-model="diffVisible" title="登记困难" width="560px">
      <el-form :model="diffForm" label-width="100px">
        <el-form-item label="党员" required>
          <el-select v-model="diffForm.memberId" filterable style="width:100%" @change="onDiffMemberChange">
            <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="困难类型" required>
          <el-select v-model="diffForm.difficultyType" style="width:100%">
            <el-option v-for="t in difficultyTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="困难原因" required>
          <el-input v-model="diffForm.reason" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="登记日期" required>
          <el-date-picker v-model="diffForm.registerDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="帮扶金额">
          <el-input-number v-model="diffForm.grantAmount" :min="0" :precision="2" :step="100" />
          <span class="tip">（首次帮扶金额，可为 0）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="diffVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDifficulty">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 慰问登记/查看弹窗 ============ -->
    <el-dialog v-model="visitVisible" :title="visitForm.id ? '查看慰问' : '登记慰问'" width="560px">
      <el-form :model="visitForm" label-width="100px">
        <el-form-item label="党员" required>
          <el-select v-model="visitForm.memberId" filterable style="width:100%" :disabled="!!visitForm.id"
                     @change="onVisitMemberChange">
            <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="慰问节点" required>
          <el-select v-model="visitForm.node" style="width:100%" :disabled="!!visitForm.id"
                     @change="checkDuplicate">
            <el-option v-for="n in visitNodes" :key="n" :label="n" :value="n" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="duplicateWarning" label="提醒">
          <el-tag size="small" type="warning">该党员在同节点已存在慰问记录，可继续登记（不阻断）</el-tag>
        </el-form-item>
        <el-form-item label="慰问日期" required>
          <el-date-picker v-model="visitForm.visitDate" type="date" value-format="YYYY-MM-DD"
                          style="width:100%" :disabled="!!visitForm.id" />
        </el-form-item>
        <el-form-item label="慰问人">
          <el-input v-model="visitForm.visitor" :disabled="!!visitForm.id" />
        </el-form-item>
        <el-form-item label="慰问内容">
          <el-input v-model="visitForm.content" type="textarea" :rows="3" :disabled="!!visitForm.id" />
        </el-form-item>
        <el-form-item label="慰问金额">
          <el-input-number v-model="visitForm.amount" :min="0" :precision="2" :step="100" :disabled="!!visitForm.id" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visitVisible = false">关闭</el-button>
        <el-button v-if="!visitForm.id" type="primary" @click="submitVisit">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 帮扶申请登记弹窗 ============ -->
    <el-dialog v-model="helpVisible" title="登记帮扶申请" width="560px">
      <el-form :model="helpForm" label-width="100px">
        <el-form-item label="党员" required>
          <el-select v-model="helpForm.memberId" filterable style="width:100%" @change="onHelpMemberChange">
            <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="帮扶类型" required>
          <el-select v-model="helpForm.helpType" style="width:100%">
            <el-option v-for="t in helpTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请金额" required>
          <el-input-number v-model="helpForm.applyAmount" :min="0" :precision="2" :step="100" />
        </el-form-item>
        <el-form-item label="申请理由" required>
          <el-input v-model="helpForm.reason" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="申请日期" required>
          <el-date-picker v-model="helpForm.applyDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="helpVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHelp">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 审批弹窗 ============ -->
    <el-dialog v-model="approveVisible" title="帮扶申请审批（上级党委）" width="520px">
      <el-form label-width="90px">
        <el-form-item label="党员">{{ approveForm.memberName }}</el-form-item>
        <el-form-item label="申请金额">￥{{ money(approveForm.applyAmount) }}</el-form-item>
        <el-form-item label="申请理由">{{ approveForm.reason }}</el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="approveForm.opinion" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="submitApprove(false)">驳回</el-button>
        <el-button type="success" @click="submitApprove(true)">批准</el-button>
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

const API = '/api/party/member/care'
const MEMBER_API = '/api/party/member/info'

const visitNodes = ['重要节日', '功勋', '伤残', '老党员', '困难', '殉职家庭']
const difficultyTypes = ['因病', '因学', '因残', '失业', '重大变故', '其他']
const helpTypes = ['医疗救助', '教育资助', '生活补助', '就业帮扶', '其他']

const activeTab = ref('difficulty')
const members = ref([])

const diffLoading = ref(false)
const visitLoading = ref(false)
const helpLoading = ref(false)
const publicityLoading = ref(false)

const difficulties = ref([])
const visits = ref([])
const helps = ref([])
const publicity = ref([])

const diffFilter = reactive({ keyword: '', status: '' })
const visitFilter = reactive({ keyword: '', node: '' })
const helpFilter = reactive({ keyword: '', approveStatus: '' })

function money(v) {
  const n = Number(v || 0)
  return n.toFixed(2)
}
function approveType(s) {
  return { 待审批: 'warning', 已批准: 'success', 已驳回: 'danger' }[s] || 'warning'
}

async function loadMembers() {
  try {
    const res = await axios.get(`${MEMBER_API}/list`)
    if (res.data.code === 200) members.value = res.data.data || []
  } catch { /* 拦截器提示 */ }
}

// ---------- 困难台账 ----------
async function loadDifficulties() {
  diffLoading.value = true
  try {
    const res = await axios.get(`${API}/difficulties`, {
      params: { keyword: diffFilter.keyword || undefined, status: diffFilter.status || undefined }
    })
    if (res.data.code === 200) difficulties.value = res.data.data || []
  } finally { diffLoading.value = false }
}

const diffVisible = ref(false)
const diffForm = reactive({})
function openDifficulty() {
  Object.keys(diffForm).forEach(k => delete diffForm[k])
  Object.assign(diffForm, {
    memberId: null, memberName: '', difficultyType: null, reason: '',
    registerDate: new Date().toISOString().slice(0, 10), grantAmount: 0
  })
  diffVisible.value = true
}
function onDiffMemberChange(id) {
  const m = members.value.find(x => x.id === id)
  if (m) diffForm.memberName = m.name
}
async function submitDifficulty() {
  if (!diffForm.memberId) { ElMessage.warning('请选择党员'); return }
  if (!diffForm.difficultyType) { ElMessage.warning('请选择困难类型'); return }
  if (!diffForm.reason?.trim()) { ElMessage.warning('请填写困难原因'); return }
  if (!diffForm.registerDate) { ElMessage.warning('请选择登记日期'); return }
  try {
    await axios.post(`${API}/difficulties`, {
      memberId: diffForm.memberId, memberName: diffForm.memberName,
      difficultyType: diffForm.difficultyType, reason: diffForm.reason,
      registerDate: diffForm.registerDate, grantAmount: diffForm.grantAmount || 0
    })
    ElMessage.success('困难登记成功')
    diffVisible.value = false
    loadDifficulties()
  } catch { /* 拦截器提示 */ }
}
async function exitDifficulty(row) {
  try {
    await ElMessageBox.confirm(`确认将「${row.memberName}」从困难台账退出？`, '退出困难', { type: 'warning' })
    await axios.post(`${API}/difficulties/${row.id}/exit`)
    ElMessage.success('已退出困难台账')
    loadDifficulties()
  } catch (e) { if (e !== 'cancel') { /* 业务错误已提示 */ } }
}

// ---------- 走访慰问 ----------
async function loadVisits() {
  visitLoading.value = true
  try {
    const res = await axios.get(`${API}/visits`, {
      params: { keyword: visitFilter.keyword || undefined, node: visitFilter.node || undefined }
    })
    if (res.data.code === 200) visits.value = res.data.data || []
  } finally { visitLoading.value = false }
}

const visitVisible = ref(false)
const visitForm = reactive({})
const duplicateWarning = ref(false)

function openVisit(row) {
  Object.keys(visitForm).forEach(k => delete visitForm[k])
  duplicateWarning.value = false
  if (row) {
    Object.assign(visitForm, JSON.parse(JSON.stringify(row)))
  } else {
    Object.assign(visitForm, {
      memberId: null, memberName: '', node: null, visitDate: new Date().toISOString().slice(0, 10),
      visitor: '', content: '', amount: 0
    })
  }
  visitVisible.value = true
}
function onVisitMemberChange(id) {
  const m = members.value.find(x => x.id === id)
  if (m) visitForm.memberName = m.name
  checkDuplicate()
}
function checkDuplicate() {
  if (!visitForm.memberId || !visitForm.node) { duplicateWarning.value = false; return }
  duplicateWarning.value = visits.value.some(v => v.memberId === visitForm.memberId && v.node === visitForm.node)
}
async function submitVisit() {
  if (!visitForm.memberId) { ElMessage.warning('请选择党员'); return }
  if (!visitForm.node) { ElMessage.warning('请选择慰问节点'); return }
  if (!visitForm.visitDate) { ElMessage.warning('请选择慰问日期'); return }
  try {
    await axios.post(`${API}/visits`, {
      memberId: visitForm.memberId, memberName: visitForm.memberName,
      node: visitForm.node, visitDate: visitForm.visitDate,
      visitor: visitForm.visitor, content: visitForm.content, amount: visitForm.amount || 0
    })
    ElMessage.success('慰问登记成功')
    visitVisible.value = false
    loadVisits()
  } catch { /* 拦截器提示 */ }
}

// ---------- 帮扶申请 ----------
async function loadHelps() {
  helpLoading.value = true
  try {
    const res = await axios.get(`${API}/helps`, {
      params: { keyword: helpFilter.keyword || undefined, approveStatus: helpFilter.approveStatus || undefined }
    })
    if (res.data.code === 200) helps.value = res.data.data || []
  } finally { helpLoading.value = false }
}

const helpVisible = ref(false)
const helpForm = reactive({})
function openHelp() {
  Object.keys(helpForm).forEach(k => delete helpForm[k])
  Object.assign(helpForm, {
    memberId: null, memberName: '', helpType: null, applyAmount: 0,
    reason: '', applyDate: new Date().toISOString().slice(0, 10)
  })
  helpVisible.value = true
}
function onHelpMemberChange(id) {
  const m = members.value.find(x => x.id === id)
  if (m) helpForm.memberName = m.name
}
async function submitHelp() {
  if (!helpForm.memberId) { ElMessage.warning('请选择党员'); return }
  if (!helpForm.helpType) { ElMessage.warning('请选择帮扶类型'); return }
  if (!helpForm.applyAmount || helpForm.applyAmount <= 0) { ElMessage.warning('请填写申请金额'); return }
  if (!helpForm.reason?.trim()) { ElMessage.warning('请填写申请理由'); return }
  try {
    await axios.post(`${API}/helps`, {
      memberId: helpForm.memberId, memberName: helpForm.memberName,
      helpType: helpForm.helpType, applyAmount: helpForm.applyAmount,
      reason: helpForm.reason, applyDate: helpForm.applyDate
    })
    ElMessage.success('帮扶申请已登记')
    helpVisible.value = false
    loadHelps()
  } catch { /* 拦截器提示 */ }
}

const approveVisible = ref(false)
const approveForm = reactive({})
function openApprove(row) {
  Object.keys(approveForm).forEach(k => delete approveForm[k])
  Object.assign(approveForm, {
    id: row.id, memberName: row.memberName, applyAmount: row.applyAmount,
    reason: row.reason, opinion: ''
  })
  approveVisible.value = true
}
async function submitApprove(pass) {
  try {
    await axios.post(`${API}/helps/${approveForm.id}/approve`, null, {
      params: { pass, opinion: approveForm.opinion || '' }
    })
    ElMessage.success(pass ? '已批准' : '已驳回')
    approveVisible.value = false
    loadHelps()
  } catch { /* 拦截器提示 */ }
}
async function grantHelp(row) {
  try {
    await ElMessageBox.confirm(
      `确认向「${row.memberName}」发放帮扶资金 ￥${money(row.applyAmount)}？将关联党费使用记录。`,
      '帮扶资金发放', { type: 'warning' }
    )
    await axios.post(`${API}/helps/${row.id}/grant`)
    ElMessage.success('发放成功，已关联党费使用')
    loadHelps()
  } catch (e) { if (e !== 'cancel') { /* 业务错误已提示 */ } }
}

// ---------- 公示合规 ----------
async function loadPublicity() {
  publicityLoading.value = true
  try {
    const res = await axios.get(`${API}/publicity-check`)
    if (res.data.code === 200) publicity.value = res.data.data || []
  } finally { publicityLoading.value = false }
}

onMounted(() => {
  loadMembers()
  loadDifficulties()
  loadVisits()
  loadHelps()
  loadPublicity()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.bar { display: flex; gap: 10px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }
.tip { color: #909399; font-size: 12px; }
</style>

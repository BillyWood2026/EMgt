<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">草稿→待审(人力C05→法务C06→高管C07)→生效｜同组织旧版自动作废归档</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-select v-model="q.status" placeholder="状态" clearable size="small" style="width:120px" @change="load">
          <el-option v-for="s in STATUS" :key="s" :label="s" :value="s" />
        </el-select>
        <el-input v-model="q.applyOrg" placeholder="适用组织" size="small" clearable style="width:160px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('C02','C03')" size="small" type="primary" @click="openCreate">编制纲领</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="460">
        <el-table-column prop="creedName" label="体系名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{row}"><el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="当前节点" width="110">
          <template #default="{row}">{{ nodeText(row) }}</template>
        </el-table-column>
        <el-table-column prop="applyOrg" label="适用组织" min-width="150" show-overflow-tooltip />
        <el-table-column prop="effectDate" label="生效日期" width="100" />
        <el-table-column prop="creator" label="编制人" width="100" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="openDetail(row)">查看</el-button>
            <el-button v-if="can('C02','C03') && row.status==='草稿'" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('C02','C03') && row.status==='草稿'" size="small" type="primary" @click="doSubmit(row)">提交</el-button>
            <el-button v-if="canApprove(row)" size="small" type="warning" @click="openApprove(row)">审批</el-button>
            <el-button v-if="can('C07','C99') && row.status==='生效'" size="small" type="danger" @click="doArchive(row)">作废</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编制/编辑 -->
    <el-dialog v-model="formVis" :title="form.id?'编辑纲领':'编制纲领'" width="760px" top="5vh">
      <el-form :model="form" label-width="92px" size="small">
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="体系名称" required><el-input v-model="form.creedName" maxlength="120" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="适用组织" required><el-input v-model="form.applyOrg" placeholder="多个组织逗号分隔" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="生效日期" required><el-date-picker v-model="form.effectDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="失效日期"><el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="企业精神"><el-input v-model="form.spirit" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="品牌口号"><el-input v-model="form.slogan" /></el-form-item></el-col>
          <el-col :span="24">
            <el-form-item label="企业使命" required>
              <el-input v-model="form.mission" type="textarea" :rows="2" maxlength="500" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="企业愿景" required>
              <el-input v-model="form.vision" type="textarea" :rows="2" maxlength="500" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">核心价值观分组 <el-button size="small" @click="addGroup">+ 分组</el-button></el-divider>
        <div v-for="(g,gi) in vgRows" :key="gi" class="vgbox">
          <el-row :gutter="8">
            <el-col :span="6"><el-input v-model="g.title" placeholder="分组标题，如 以客户为中心" /></el-col>
            <el-col :span="16"><el-input v-model="g.clausesText" type="textarea" :rows="2" placeholder="子条款，每行一条" /></el-col>
            <el-col :span="2"><el-button size="small" type="danger" @click="vgRows.splice(gi,1)">删</el-button></el-col>
          </el-row>
        </div>
      </el-form>
      <template #footer>
        <el-button size="small" @click="formVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doSave">保存草稿</el-button>
      </template>
    </el-dialog>

    <!-- 审批 -->
    <el-dialog v-model="apVis" title="纲领审批" width="520px">
      <el-alert :title="'当前节点：' + nodeText(apRow)" type="info" :closable="false" class="mb" />
      <el-input v-model="opinion" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="审批意见（驳回时必填，不少于5字）" />
      <template #footer>
        <el-button size="small" @click="apVis=false">取消</el-button>
        <el-button size="small" type="danger" @click="doApprove(false)">驳回</el-button>
        <el-button size="small" type="primary" @click="doApprove(true)">通过</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVis" size="62%" :title="dRow ? dRow.creedName + ' ' + dRow.version : ''">
      <template v-if="dRow">
        <el-tabs v-model="tab">
          <el-tab-pane label="基础与全文" name="base">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="状态"><el-tag size="small" :type="statusType(dRow.status)">{{ dRow.status }}</el-tag></el-descriptions-item>
              <el-descriptions-item label="适用组织">{{ dRow.applyOrg }}</el-descriptions-item>
              <el-descriptions-item label="生效/失效">{{ dRow.effectDate }} ~ {{ dRow.expireDate || '长期' }}</el-descriptions-item>
              <el-descriptions-item label="编制人">{{ dRow.creator }}</el-descriptions-item>
              <el-descriptions-item label="企业使命" :span="2">{{ dRow.mission }}</el-descriptions-item>
              <el-descriptions-item label="企业愿景" :span="2">{{ dRow.vision }}</el-descriptions-item>
              <el-descriptions-item label="精神/口号" :span="2">{{ dRow.spirit }} ｜ {{ dRow.slogan }}</el-descriptions-item>
              <el-descriptions-item label="核心价值观" :span="2">
                <div v-for="g in parseGroups(dRow.valueGroups)" :key="g.title" class="dvg">
                  <b>{{ g.title }}</b>
                  <span v-for="(c,i) in arrOf(g.clauses)" :key="i" class="chip">{{ c }}</span>
                </div>
              </el-descriptions-item>
              <el-descriptions-item v-if="dRow.archiveReason" label="作废原因" :span="2">{{ dRow.archiveReason }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="审批日志" name="log">
            <el-timeline>
              <el-timeline-item v-for="(e,i) in parseLog(dRow.approvalLog)" :key="i" :type="e.pass?'success':'danger'" :timestamp="e.time">
                节点{{ e.stage }} · {{ e.pass ? '通过' : '驳回' }} · {{ e.by }}
                <div v-if="e.opinion" style="color:#606266">意见：{{ e.opinion }}</div>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
          <el-tab-pane label="版本对比" name="cmp">
            <div class="bar mb">
              <el-select v-model="cmpA" placeholder="选择版本A" size="small" style="width:200px">
                <el-option v-for="r in rows" :key="r.id" :label="r.version + ' ' + r.status" :value="r.id" />
              </el-select>
              <span>VS</span>
              <el-select v-model="cmpB" placeholder="选择版本B" size="small" style="width:200px">
                <el-option v-for="r in rows" :key="r.id" :label="r.version + ' ' + r.status" :value="r.id" />
              </el-select>
            </div>
            <el-row v-if="cmpRowA || cmpRowB" :gutter="10">
              <el-col :span="12"><div class="cmp-h">{{ cmpRowA ? cmpRowA.version : '—' }}</div>
                <div class="cmp-box"><b>使命</b><p>{{ cmpRowA ? cmpRowA.mission : '' }}</p><b>愿景</b><p>{{ cmpRowA ? cmpRowA.vision : '' }}</p></div>
              </el-col>
              <el-col :span="12"><div class="cmp-h">{{ cmpRowB ? cmpRowB.version : '—' }}</div>
                <div class="cmp-box"><b>使命</b><p>{{ cmpRowB ? cmpRowB.mission : '' }}</p><b>愿景</b><p>{{ cmpRowB ? cmpRowB.vision : '' }}</p></div>
              </el-col>
            </el-row>
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const STATUS = ['草稿', '待审', '生效', '作废归档']
const NODE = { 1: '人力负责人初审(C05)', 2: '法务合规审核(C06)', 3: '分管高管终审(C07)' }

const rows = ref([])
const loading = ref(false)
const q = reactive({ status: '', applyOrg: '' })

const formVis = ref(false)
const form = reactive({ id: null, creedName: '', applyOrg: '', effectDate: '', expireDate: '', mission: '', vision: '', spirit: '', slogan: '' })
const vgRows = ref([])

const apVis = ref(false)
const apRow = ref(null)
const opinion = ref('')

const detailVis = ref(false)
const dRow = ref(null)
const tab = ref('base')
const cmpA = ref(null)
const cmpB = ref(null)

function statusType(s) {
  if (s === '生效') return 'success'
  if (s === '待审') return 'warning'
  if (s === '作废归档') return 'info'
  return ''
}
function nodeText(row) {
  if (row.status !== '待审') return '—'
  return NODE[row.stage] || ('节点' + row.stage)
}
function canApprove(row) {
  if (row.status !== '待审') return false
  if (row.stage === 1) return can('C05')
  if (row.stage === 2) return can('C06')
  if (row.stage === 3) return can('C07')
  return false
}
function parseGroups(s) {
  try { return JSON.parse(s || '[]') } catch { return [] }
}
function arrOf(a) {
  return Array.isArray(a) ? a : []
}
function parseLog(s) {
  try { return JSON.parse(s || '[]') } catch { return [] }
}

async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/foundation/creed/list', { params: { status: q.status || undefined, applyOrg: q.applyOrg || undefined } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}

function resetForm() {
  Object.assign(form, { id: null, creedName: '', applyOrg: '', effectDate: '', expireDate: '', mission: '', vision: '', spirit: '', slogan: '' })
  vgRows.value = [{ title: '', clausesText: '' }]
}
function openCreate() {
  resetForm()
  formVis.value = true
}
function openEdit(row) {
  Object.assign(form, {
    id: row.id, creedName: row.creedName, applyOrg: row.applyOrg,
    effectDate: row.effectDate, expireDate: row.expireDate,
    mission: row.mission, vision: row.vision, spirit: row.spirit, slogan: row.slogan
  })
  vgRows.value = parseGroups(row.valueGroups).map(g => ({
    title: g.title,
    clausesText: arrOf(g.clauses).join('\n')
  }))
  formVis.value = true
}
function addGroup() {
  vgRows.value.push({ title: '', clausesText: '' })
}
async function doSave() {
  const groups = vgRows.value
    .filter(g => g.title && g.title.trim())
    .map(g => ({ title: g.title.trim(), clauses: g.clausesText.split('\n').map(x => x.trim()).filter(Boolean) }))
  if (!groups.length) return ElMessage.warning('请至少填写一个价值观分组（含标题）')
  const payload = { ...form, valueGroups: JSON.stringify(groups) }
  const url = form.id ? '/api/culture/foundation/creed/' + form.id : '/api/culture/foundation/creed'
  await axios({ url, method: form.id ? 'put' : 'post', data: payload })
  ElMessage.success('已保存草稿')
  formVis.value = false
  load()
}
async function doSubmit(row) {
  await ElMessageBox.confirm('确认提交审批？提交后进入三级审批链路', '提交', { type: 'info' })
  await axios.post(`/api/culture/foundation/creed/${row.id}/submit`)
  ElMessage.success('已提交，当前节点：' + NODE[1])
  load()
}
function openApprove(row) {
  apRow.value = row
  opinion.value = ''
  apVis.value = true
}
async function doApprove(pass) {
  await axios.post(`/api/culture/foundation/creed/${apRow.value.id}/approve`, null, { params: { pass, opinion: opinion.value } })
  ElMessage.success(pass ? '审批通过' : '已驳回至草稿')
  apVis.value = false
  load()
}
async function doArchive(row) {
  const { value } = await ElMessageBox.prompt('请输入作废原因（不少于5字）', '作废归档', {
    confirmButtonText: '确认作废', cancelButtonText: '取消', inputPattern: /.{5,}/, inputErrorMessage: '作废原因不少于5字'
  })
  await axios.post(`/api/culture/foundation/creed/${row.id}/archive`, null, { params: { reason: value } })
  ElMessage.success('已作废归档')
  load()
}
function openDetail(row) {
  dRow.value = row
  cmpA.value = row.id
  cmpB.value = null
  tab.value = 'base'
  detailVis.value = true
}
const cmpRowA = computed(() => rows.value.find(r => r.id === cmpA.value) || null)
const cmpRowB = computed(() => rows.value.find(r => r.id === cmpB.value) || null)

onMounted(load)
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; }
.mb { margin-bottom:12px; }
.vgbox { margin-bottom:8px; }
.dvg { margin-bottom:6px; }
.chip { display:inline-block; background:#f0f9eb; border:1px solid #b7eb8f; color:#2f9e44; border-radius:10px; padding:1px 10px; margin:2px 4px 2px 8px; font-size:12px; }
.cmp-h { font-weight:600; text-align:center; margin-bottom:6px; }
.cmp-box { border:1px solid #ebeef5; border-radius:6px; padding:10px; background:#fafafa; min-height:160px; }
.cmp-box p { color:#606266; white-space:pre-wrap; }
</style>

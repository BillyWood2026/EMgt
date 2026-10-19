<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">草稿→文化部门初审(C02/C03)→人力复核(C05)→法务(C06)→高管终审(C07)→生效｜修订升Vx.y｜合规覆盖率=已学/应学</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-select v-model="q.status" placeholder="状态" clearable size="small" style="width:110px" @change="load">
          <el-option v-for="s in STATUS" :key="s" :label="s" :value="s" />
        </el-select>
        <el-select v-model="q.docType" placeholder="分类" clearable size="small" style="width:150px" @change="load">
          <el-option v-for="t in DOC_TYPES" :key="t" :label="t" :value="t" />
        </el-select>
        <el-input v-model="q.docName" placeholder="制度名称" size="small" clearable style="width:170px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('C02','C03')" size="small" type="primary" @click="openCreate">编制制度</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="470">
        <el-table-column prop="docName" label="制度名称" min-width="170" show-overflow-tooltip />
        <el-table-column prop="docType" label="分类" width="110" />
        <el-table-column prop="version" label="版本" width="76" />
        <el-table-column label="状态" width="92">
          <template #default="{row}"><el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="节点" width="120">
          <template #default="{row}">{{ nodeText(row) }}</template>
        </el-table-column>
        <el-table-column label="合规覆盖率" width="150">
          <template #default="{row}">
            <el-progress :percentage="rateOf(row)" :stroke-width="10" :status="rateOf(row)>=80?'success':''" />
          </template>
        </el-table-column>
        <el-table-column prop="effectDate" label="生效日" width="96" />
        <el-table-column label="操作" width="330" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="openDetail(row)">详情/学习</el-button>
            <el-button v-if="can('C02','C03') && row.status==='草稿'" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('C02','C03') && row.status==='草稿'" size="small" type="primary" @click="doSubmit(row)">提交</el-button>
            <el-button v-if="canApprove(row)" size="small" type="warning" @click="openApprove(row)">审批</el-button>
            <el-button v-if="can('C02','C03') && row.status==='生效'" size="small" type="success" @click="openRevise(row)">修订</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编制/编辑 -->
    <el-dialog v-model="formVis" :title="form.id?'编辑制度':'编制制度'" width="720px" top="5vh">
      <el-form :model="form" label-width="96px" size="small">
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="制度名称" required><el-input v-model="form.docName" maxlength="160" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="制度分类" required>
            <el-select v-model="form.docType" style="width:100%"><el-option v-for="t in DOC_TYPES" :key="t" :label="t" :value="t" /></el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="适用部门"><el-input v-model="form.applyOrg" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="适用岗位"><el-input v-model="form.applyPost" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="生效日期" required><el-date-picker v-model="form.effectDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="失效日期"><el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="编制部门"><el-input v-model="form.dept" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="应学习人数"><el-input-number v-model="form.shouldCount" :min="0" :max="100000" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="原文附件" required><el-input v-model="form.fileUrl" placeholder="PDF / Word 文件地址（复用集团存储中台）" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="制度摘要" required><el-input v-model="form.summary" type="textarea" :rows="3" maxlength="800" show-word-limit /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="关键条款" required><el-input v-model="form.keyClause" type="textarea" :rows="3" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button size="small" @click="formVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doSave">保存草稿</el-button>
      </template>
    </el-dialog>

    <!-- 审批 -->
    <el-dialog v-model="apVis" title="制度审批" width="520px">
      <el-alert :title="'当前节点：' + nodeText(apRow)" type="info" :closable="false" class="mb" />
      <el-input v-model="opinion" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="审批意见（驳回必填，不少于5字）" />
      <template #footer>
        <el-button size="small" @click="apVis=false">取消</el-button>
        <el-button size="small" type="danger" @click="doApprove(false)">驳回</el-button>
        <el-button size="small" type="primary" @click="doApprove(true)">通过</el-button>
      </template>
    </el-dialog>

    <!-- 修订 -->
    <el-dialog v-model="rvVis" title="发起修订（复制新版本，原版本保持生效）" width="480px">
      <el-alert :title="rvRow ? '源版本：' + rvRow.version + '，新版本号自动升为 ' + bump(rvRow.version) : ''" type="success" :closable="false" class="mb" />
      <el-input v-model="rvReason" type="textarea" :rows="4" placeholder="修订原因（不少于5字）" />
      <template #footer>
        <el-button size="small" @click="rvVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doRevise">生成修订草稿</el-button>
      </template>
    </el-dialog>

    <!-- 详情/学习抽屉 -->
    <el-drawer v-model="detailVis" size="58%" :title="dRow ? dRow.docName + ' ' + dRow.version : ''">
      <template v-if="dRow">
        <div class="bar mb">
          <el-tag size="small" :type="statusType(dRow.status)">{{ dRow.status }}</el-tag>
          <el-tag size="small" type="info">{{ dRow.docType }}</el-tag>
          <span style="flex:1"></span>
          <el-button v-if="dRow.status==='生效'" size="small" type="primary" @click="doLearn(dRow)">我已学习</el-button>
          <el-button size="small" @click="loadLearners(dRow)">学习明细({{ dRow.learnedCount }})</el-button>
        </div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="适用部门">{{ dRow.applyOrg }}</el-descriptions-item>
          <el-descriptions-item label="适用岗位">{{ dRow.applyPost }}</el-descriptions-item>
          <el-descriptions-item label="生效/失效">{{ dRow.effectDate }} ~ {{ dRow.expireDate || '长期' }}</el-descriptions-item>
          <el-descriptions-item label="编制部门">{{ dRow.dept }}</el-descriptions-item>
          <el-descriptions-item label="应学/已学">{{ dRow.shouldCount }} / {{ dRow.learnedCount }}（{{ rateOf(dRow) }}%）</el-descriptions-item>
          <el-descriptions-item label="原文附件"><a :href="dRow.fileUrl" target="_blank">{{ dRow.fileUrl }}</a></el-descriptions-item>
          <el-descriptions-item label="制度摘要" :span="2">{{ dRow.summary }}</el-descriptions-item>
          <el-descriptions-item label="关键条款" :span="2">{{ dRow.keyClause }}</el-descriptions-item>
          <el-descriptions-item v-if="dRow.reviseReason" label="修订原因" :span="2">{{ dRow.reviseReason }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">审批链路</el-divider>
        <el-timeline>
          <el-timeline-item v-for="(e,i) in parseLog(dRow.approvalLog)" :key="i" :type="e.pass?'success':'danger'" :timestamp="e.time">
            节点{{ e.stage }}（{{ nodeNameOf(e.stage) }}）· {{ e.pass ? '通过' : '驳回' }} · {{ e.by }}
            <div v-if="e.opinion" style="color:#606266">意见：{{ e.opinion }}</div>
          </el-timeline-item>
        </el-timeline>

        <el-divider content-position="left">学习完成明细</el-divider>
        <el-table :data="learners" size="small" border max-height="240">
          <el-table-column prop="userName" label="学习人" width="140" />
          <el-table-column prop="orgName" label="组织" />
          <el-table-column prop="learnTime" label="学习时间" width="180" />
        </el-table>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const STATUS = ['草稿', '待审', '生效', '修订中', '归档']
const DOC_TYPES = ['文化建设类', '行为规范类', '廉洁合规类', '员工关怀类', '荣誉激励类', '培训管理类', '舆情管理类', '其他类']
const NODE = { 1: '文化部门初审(C02/C03)', 2: '人力复核(C05)', 3: '法务审核(C06)', 4: '高管终审(C07)' }

const rows = ref([])
const loading = ref(false)
const q = reactive({ status: '', docType: '', docName: '' })

const formVis = ref(false)
const form = reactive({})
const apVis = ref(false)
const apRow = ref(null)
const opinion = ref('')
const rvVis = ref(false)
const rvRow = ref(null)
const rvReason = ref('')
const detailVis = ref(false)
const dRow = ref(null)
const learners = ref([])

function statusType(s) {
  if (s === '生效') return 'success'
  if (s === '待审') return 'warning'
  if (s === '归档') return 'info'
  if (s === '修订中') return 'warning'
  return ''
}
function nodeNameOf(st) {
  return NODE[st] || ('节点' + st)
}
function nodeText(row) {
  return row.status === '待审' ? nodeNameOf(row.stage) : '—'
}
function canApprove(row) {
  if (row.status !== '待审') return false
  if (row.stage === 1) return can('C02', 'C03')
  if (row.stage === 2) return can('C05')
  if (row.stage === 3) return can('C06')
  if (row.stage === 4) return can('C07')
  return false
}
function rateOf(d) {
  if (!d.shouldCount || d.shouldCount <= 0) return 0
  return Math.round(d.learnedCount * 10000 / d.shouldCount) / 100
}
function parseLog(s) {
  try { return JSON.parse(s || '[]') } catch { return [] }
}
function bump(v) {
  const m = /^V?(\d+)\.(\d+)$/.exec(v || 'V1.0')
  return m ? 'V' + m[1] + '.' + (Number(m[2]) + 1) : 'V1.1'
}

async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/foundation/institution/list', { params: { status: q.status || undefined, docType: q.docType || undefined, docName: q.docName || undefined } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}
function blank() {
  return { id: null, docName: '', docType: '', applyOrg: '', applyPost: '', effectDate: '', expireDate: '', dept: '', summary: '', keyClause: '', fileUrl: '', shouldCount: 0 }
}
function openCreate() {
  Object.assign(form, blank())
  formVis.value = true
}
function openEdit(row) {
  Object.assign(form, JSON.parse(JSON.stringify(row)))
  formVis.value = true
}
async function doSave() {
  await axios({ url: form.id ? '/api/culture/foundation/institution/' + form.id : '/api/culture/foundation/institution', method: form.id ? 'put' : 'post', data: { ...form } })
  ElMessage.success('已保存草稿')
  formVis.value = false
  load()
}
async function doSubmit(row) {
  await ElMessageBox.confirm('确认提交四级审批链路？', '提交', { type: 'info' })
  await axios.post(`/api/culture/foundation/institution/${row.id}/submit`)
  ElMessage.success('已提交')
  load()
}
function openApprove(row) {
  apRow.value = row
  opinion.value = ''
  apVis.value = true
}
async function doApprove(pass) {
  await axios.post(`/api/culture/foundation/institution/${apRow.value.id}/approve`, null, { params: { pass, opinion: opinion.value } })
  ElMessage.success(pass ? '审批通过' : '已驳回至草稿')
  apVis.value = false
  load()
}
function openRevise(row) {
  rvRow.value = row
  rvReason.value = ''
  rvVis.value = true
}
async function doRevise() {
  const r = await axios.post(`/api/culture/foundation/institution/${rvRow.value.id}/revise`, null, { params: { reason: rvReason.value } })
  ElMessage.success('已生成修订草稿 ' + r.data.data.version)
  rvVis.value = false
  load()
}
async function openDetail(row) {
  const r = await axios.get('/api/culture/foundation/institution/' + row.id)
  dRow.value = r.data.data
  learners.value = []
  detailVis.value = true
  loadLearners(dRow.value)
}
async function loadLearners(row) {
  const r = await axios.get(`/api/culture/foundation/institution/${row.id}/learners`)
  learners.value = r.data.data || []
}
async function doLearn(row) {
  await axios.post(`/api/culture/foundation/institution/${row.id}/learn`, {})
  ElMessage.success('学习完成已记录')
  await openDetail(row)
  load()
}
onMounted(load)
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; }
.mb { margin-bottom:12px; }
</style>

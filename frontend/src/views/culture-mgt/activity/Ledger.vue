<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">活动全流程：草稿→待审批(①部门负责人C04→②文化专员C02/C03)→已发布→进行中→待复盘→已闭环；驳回意见≥5字</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="q.status" placeholder="状态" clearable size="small" style="width:130px" @change="load">
          <el-option v-for="s in STATUS" :key="s" :label="s" :value="s" />
        </el-select>
        <el-select v-model="q.type" placeholder="类型" clearable size="small" style="width:140px" @change="load">
          <el-option v-for="t in TYPES" :key="t" :label="t" :value="t" />
        </el-select>
        <el-input v-model="q.keyword" placeholder="活动名称关键词" size="small" clearable style="width:200px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button size="small" type="primary" @click="openCreate">新建活动</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" class="mt" max-height="520">
        <el-table-column prop="actName" label="活动名称" min-width="220" show-overflow-tooltip />
        <el-table-column prop="actType" label="类型" width="110" />
        <el-table-column prop="organizerDept" label="主办部门" width="150" show-overflow-tooltip />
        <el-table-column prop="manager" label="经办人" width="90" />
        <el-table-column label="活动时间" width="160">
          <template #default="{row}">{{ fmt(row.planStart) }}</template>
        </el-table-column>
        <el-table-column label="报名/签到" width="100" align="center">
          <template #default="{row}">{{ row.signedCount }} / {{ row.checkedCount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{row}"><el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status==='草稿' && canManage(row)" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status==='草稿' && canManage(row)" size="small" type="primary" @click="doSubmit(row)">提交审批</el-button>
            <el-button v-if="row.status==='待审批' && myNode(row)!==null" size="small" type="warning" @click="openApprove(row)">审批</el-button>
            <el-button v-if="row.status==='已发布'" size="small" type="success" @click="doStart(row)">开始</el-button>
            <el-button v-if="row.status==='进行中'" size="small" type="warning" @click="doFinish(row)">结束归档</el-button>
            <el-button v-if="row.status==='已发布' && can('C02','C03','C99')" size="small" type="danger" @click="doCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVis" :title="form.id ? '编辑活动' : '新建活动'" width="720px" top="6vh">
      <el-alert v-if="form.id && form.rejectReason" type="error" :closable="false" class="mb"
        :title="'上次驳回/取消原因：' + form.rejectReason" />
      <el-form :model="form" label-width="100px" size="small">
        <el-form-item label="活动名称" required>
          <el-input v-model="form.actName" maxlength="100" show-word-limit placeholder="10-100字" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="8">
            <el-form-item label="活动类型" required>
              <el-select v-model="form.actType" style="width:100%">
                <el-option v-for="t in TYPES" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="9">
            <el-form-item label="主办部门"><el-input v-model="form.organizerDept" /></el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="应参与人数"><el-input-number v-model="form.shouldCount" :min="0" style="width:100%" controls-position="right" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="8">
            <el-form-item label="开始时间" required>
              <el-date-picker v-model="form.planStart" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="结束时间" required>
              <el-date-picker v-model="form.planEnd" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="报名截止">
              <el-date-picker v-model="form.signDeadline" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="8">
            <el-form-item label="报名上限">
              <el-input-number v-model="form.maxSign" :min="0" style="width:100%" controls-position="right" />
              <div class="tip">0 = 不限</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="活动简介"><el-input v-model="form.intro" type="textarea" :rows="4" maxlength="2000" show-word-limit /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="formVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doSave">保存草稿</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="apVis" :title="'活动审批：节点' + apRow.stage" width="520px">
      <div class="tip mb">{{ apRow.actName }}</div>
      <el-radio-group v-model="apPass">
        <el-radio :label="true">通过（{{ apRow.stage===1 ? '转文化专员复审' : '发布并开放报名' }}）</el-radio>
        <el-radio :label="false">驳回（退回经办人草稿）</el-radio>
      </el-radio-group>
      <el-input v-model="opinion" type="textarea" :rows="4" maxlength="500" show-word-limit class="mt"
        :placeholder="apPass ? '意见（选填）' : '驳回意见不少于5字（必填）'" />
      <template #footer>
        <el-button size="small" @click="apVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doApprove">确认</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVis" size="520px" :title="cur.actName || '活动详情'">
      <div v-if="cur.id">
        <div class="d-row">
          <el-tag size="small">{{ cur.actType }}</el-tag>
          <el-tag size="small" :type="statusType(cur.status)">{{ cur.status }}</el-tag>
        </div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="主办部门">{{ cur.organizerDept }}</el-descriptions-item>
          <el-descriptions-item label="经办人">{{ cur.manager }}</el-descriptions-item>
          <el-descriptions-item label="开始">{{ fmt(cur.planStart) }}</el-descriptions-item>
          <el-descriptions-item label="结束">{{ fmt(cur.planEnd) }}</el-descriptions-item>
          <el-descriptions-item label="报名截止">{{ fmt(cur.signDeadline) }}</el-descriptions-item>
          <el-descriptions-item label="限额">{{ cur.maxSign > 0 ? cur.maxSign : '不限' }}</el-descriptions-item>
        </el-descriptions>
        <div class="sec-title">简介</div>
        <div class="d-content">{{ cur.intro }}</div>
        <template v-if="cur.summary">
          <div class="sec-title">复盘总结</div>
          <div class="d-content">{{ cur.summary }}</div>
          <div class="sec-title">成效评估</div>
          <div class="d-content">{{ cur.resultEval }}</div>
          <div class="sec-title">归档素材</div>
          <div v-for="(f,i) in fileList(cur.archiveFiles)" :key="i" class="att">{{ f }}</div>
        </template>
        <div class="sec-title">审批记录</div>
        <el-timeline v-if="logs(cur.approvalLog).length">
          <el-timeline-item v-for="(l,i) in logs(cur.approvalLog)" :key="i" :type="l.pass?'success':'danger'" :timestamp="fmt(l.time)">
            节点{{ l.stage }} · {{ l.pass?'通过':'驳回' }} · {{ l.by }}
            <div v-if="l.opinion" class="opinion">{{ l.opinion }}</div>
          </el-timeline-item>
        </el-timeline>
        <div v-else class="tip">暂无审批记录</div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const TYPES = ['文化宣讲', '价值观读书会', '奋斗者分享', '团建', '竞赛', '周年纪念', '新人融入']
const STATUS = ['草稿', '待审批', '已发布', '进行中', '待复盘', '已闭环', '已取消']
const rows = ref([])
const loading = ref(false)
const q = reactive({ status: '', type: '', keyword: '' })
const formVis = ref(false)
const detailVis = ref(false)
const apVis = ref(false)
const cur = ref({})
const apRow = ref({})
const apPass = ref(true)
const opinion = ref('')
const form = reactive({ id: null, actName: '', actType: '文化宣讲', organizerDept: '', planStart: '', planEnd: '', signDeadline: '', maxSign: 0, shouldCount: 0, intro: '', rejectReason: '' })

function fmt(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '—' }
function logs(json) { try { const v = JSON.parse(json || '[]'); return Array.isArray(v) ? v : [] } catch { return [] } }
function fileList(json) { try { const v = JSON.parse(json || '[]'); return Array.isArray(v) ? v : [] } catch { return [] } }
function statusType(s) {
  if (s === '已发布') return 'success'
  if (s === '待审批' || s === '进行中') return 'warning'
  if (s === '待复盘') return 'danger'
  if (s === '已闭环') return 'info'
  if (s === '已取消') return 'info'
  return ''
}
function canManage(row) { return row.manager === (window.__emgt_user || '') || can('C02', 'C03', 'C99') }
function myNode(row) {
  if (row.status !== '待审批') return null
  if (row.stage === 1 && can('C04', 'C99')) return 1
  if (row.stage === 2 && can('C02', 'C03', 'C99')) return 2
  return null
}
async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/activity/list', { params: { status: q.status || undefined, type: q.type || undefined, keyword: q.keyword || undefined } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}
function resetForm() {
  Object.assign(form, { id: null, actName: '', actType: '文化宣讲', organizerDept: '', planStart: '', planEnd: '', signDeadline: '', maxSign: 0, shouldCount: 0, intro: '', rejectReason: '' })
}
function openCreate() { resetForm(); formVis.value = true }
function openEdit(row) {
  Object.assign(form, { ...row, rejectReason: row.rejectReason })
  formVis.value = true
}
async function doSave() {
  const n = (form.actName || '').length
  if (n < 10 || n > 100) return ElMessage.warning('活动名称需10-100字')
  if (!form.planStart || !form.planEnd) return ElMessage.warning('请选择开始/结束时间')
  await axios({ url: form.id ? '/api/culture/activity/' + form.id : '/api/culture/activity', method: form.id ? 'put' : 'post', data: { ...form } })
  ElMessage.success('已保存')
  formVis.value = false
  load()
}
async function doSubmit(row) {
  await ElMessageBox.confirm('提交后进入两级审批（部门负责人→文化专员），期间不可编辑。确认提交？', '提交审批', { type: 'warning' })
  await axios.post(`/api/culture/activity/${row.id}/submit`)
  ElMessage.success('已提交审批')
  load()
}
function openApprove(row) { apRow.value = row; apPass.value = true; opinion.value = ''; apVis.value = true }
async function doApprove() {
  if (!apPass.value && (opinion.value || '').trim().length < 5) return ElMessage.warning('驳回必须填写不少于5字的意见')
  await axios.post(`/api/culture/activity/${apRow.value.id}/approve`, null, { params: { pass: apPass.value, opinion: opinion.value } })
  ElMessage.success(apPass.value ? '已通过' : '已驳回')
  apVis.value = false
  load()
}
async function doStart(row) {
  await axios.post(`/api/culture/activity/${row.id}/start`)
  ElMessage.success('活动已开始（进行中）')
  load()
}
async function doFinish(row) {
  await ElMessageBox.confirm('结束后活动进入待复盘，请先在签到页完成现场签到登记。确认结束？', '结束活动', { type: 'warning' })
  await axios.post(`/api/culture/activity/${row.id}/finish`)
  ElMessage.success('已结束，请在复盘管理中归档素材并闭环')
  load()
}
async function doCancel(row) {
  const { value } = await ElMessageBox.prompt('请输入取消原因（不少于5字）', '取消活动', { inputPattern: /.{5,}/, inputErrorMessage: '原因不少于5字' })
  await axios.post(`/api/culture/activity/${row.id}/cancel`, null, { params: { reason: value } })
  ElMessage.success('活动已取消')
  load()
}
function openDetail(row) { cur.value = row; detailVis.value = true }
onMounted(load)
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.mt { margin-top:10px; }
.mb { margin-bottom:10px; }
.tip { color:#909399; font-size:12px; }
.sec-title { font-weight:600; margin:14px 0 8px; }
.d-content { white-space:pre-wrap; line-height:1.8; background:#f7f9fc; padding:10px; border-radius:6px; }
.d-row { display:flex; gap:8px; margin-bottom:12px; }
.att { font-size:13px; color:var(--el-color-primary); padding:2px 0; }
.opinion { color:#606266; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">短板整改闭环：待方案提交→方案待审核→整改实施中→待验收→验收闭环（驳回可重整改）；红色为已超期任务</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-radio-group v-model="filter.status" size="small" @change="load">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button v-for="s in STATUS" :key="s" :value="s">{{ s }}</el-radio-button>
        </el-radio-group>
        <el-input v-model="filter.dept" placeholder="部门筛选" clearable size="small" style="width:150px" @keyup.enter="load" />
        <el-checkbox v-model="filter.overdueOnly" border size="small" @change="load">仅看超期</el-checkbox>
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-tag size="small" type="info" effect="plain">当前账号：{{ me }}</el-tag>
        <el-button v-if="can('C02','C03','C99')" size="small" type="primary" @click="openCreate">创建整改任务</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" class="mt">
        <el-table-column prop="title" label="整改任务" min-width="210" show-overflow-tooltip />
        <el-table-column prop="dept" label="责任部门" width="140" show-overflow-tooltip />
        <el-table-column prop="sourceTag" label="短板标签" width="170" show-overflow-tooltip>
          <template #default="{row}">
            <el-tag v-for="t in splitTags(row.sourceTag)" :key="t" size="small" type="danger" effect="plain" class="score-tag">{{ t }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="owner" label="责任人" width="100" />
        <el-table-column label="截止日期" width="170">
          <template #default="{row}">
            <span :class="{ 'rate-bad': isOverdue(row) }">{{ row.deadline || '-' }}</span>
            <el-tag v-if="isOverdue(row)" size="small" type="danger" effect="dark" class="ml">已超期</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="140">
          <template #default="{row}"><el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{row}">
            <el-button v-if="row.status === '待方案提交' && ownerOrStaff(row)" size="small" type="primary" link @click="openPlan(row)">提交方案</el-button>
            <el-button v-if="row.status === '方案待审核' && staff" size="small" type="warning" link @click="openReview(row, 'plan')">审核方案</el-button>
            <el-button v-if="(row.status === '整改实施中' || row.status === '验收驳回重整改') && ownerOrStaff(row)"
                       size="small" type="primary" link @click="openEvidence(row)">
              {{ row.status === '验收驳回重整改' ? '补材料再提交' : '提交验收' }}
            </el-button>
            <el-button v-if="row.status === '待验收' && staff" size="small" type="success" link @click="openReview(row, 'accept')">验收</el-button>
            <el-button size="small" link @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建整改 -->
    <el-dialog v-model="createVis" title="创建氛围短板整改任务" width="560px">
      <el-form :model="form" label-width="96px" size="small">
        <el-form-item label="任务名称" required>
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="如 客户服务部行为层短板专项整改" />
        </el-form-item>
        <el-form-item label="责任部门" required>
          <el-input v-model="form.dept" placeholder="如 客户服务部" />
        </el-form-item>
        <el-form-item label="短板标签">
          <el-input v-model="form.sourceTag" placeholder="如 行为层践行,精神层认同" />
        </el-form-item>
        <el-form-item label="责任人" required>
          <el-input v-model="form.owner" placeholder="责任人账号" />
        </el-form-item>
        <el-form-item label="整改截止">
          <el-date-picker v-model="form.deadline" type="date" value-format="YYYY-MM-DD" placeholder="选择截止日期" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="createVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doCreate">创建并下发</el-button>
      </template>
    </el-dialog>

    <!-- 提交方案 -->
    <el-dialog v-model="planVis" title="提交整改方案" width="600px">
      <el-alert type="info" :closable="false" class="mb">方案须包含整改措施、责任人分工与时间节点（不少于20字），提交后由文化专员审核。</el-alert>
      <el-input v-model="planText" type="textarea" :rows="7" maxlength="1000" show-word-limit
                placeholder="一、整改措施：……&#10;二、分工与节点：……" />
      <template #footer>
        <el-button size="small" @click="planVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doPlan">提交方案</el-button>
      </template>
    </el-dialog>

    <!-- 方案审核 / 验收 -->
    <el-dialog v-model="reviewVis" :title="reviewKind === 'plan' ? '审核整改方案' : '整改验收'" width="560px">
      <el-form label-width="92px" size="small">
        <el-form-item label="审核结论" required>
          <el-radio-group v-model="reviewPass">
            <el-radio :value="true">{{ reviewKind === 'plan' ? '方案通过，进入实施' : '验收通过，闭环' }}</el-radio>
            <el-radio :value="false">{{ reviewKind === 'plan' ? '驳回，退回修改方案' : '验收不通过，退回重整改' }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" required>
          <el-input v-model="reviewOpinion" type="textarea" :rows="4" maxlength="300" show-word-limit
                    placeholder="审核/验收意见不少于5字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="reviewVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doReview">确认提交</el-button>
      </template>
    </el-dialog>

    <!-- 提交验收材料 -->
    <el-dialog v-model="evidenceVis" title="提交验收材料" width="640px">
      <el-alert type="info" :closable="false" class="mb">请填写实施情况说明（不少于10字），并至少上传1份佐证材料（文件名/链接）。</el-alert>
      <el-form label-width="100px" size="small">
        <el-form-item label="实施情况说明" required>
          <el-input v-model="implementNote" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="佐证材料" required>
          <div v-for="(f, i) in files" :key="i" class="file-line">
            <el-input v-model="f.name" size="small" placeholder="材料文件名，如 整改报告.pdf" style="flex:1" />
            <el-input v-model="f.url" size="small" placeholder="材料链接（可空）" style="flex:1" />
            <el-button size="small" type="danger" plain @click="files.splice(i, 1)">删</el-button>
          </div>
          <el-button size="small" class="mt" @click="files.push({ name: '', url: '' })">+ 添加材料</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="evidenceVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doEvidence">提交验收</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVis" :title="cur && cur.title" width="680px">
      <el-descriptions v-if="cur" :column="2" border size="small">
        <el-descriptions-item label="责任部门">{{ cur.dept }}</el-descriptions-item>
        <el-descriptions-item label="责任人">{{ cur.owner }}</el-descriptions-item>
        <el-descriptions-item label="短板标签">{{ cur.sourceTag || '-' }}</el-descriptions-item>
        <el-descriptions-item label="截止日期">{{ cur.deadline || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag size="small" :type="statusType(cur.status)">{{ cur.status }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="闭环时间">{{ cur.closeTime ? fmt(cur.closeTime) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="整改方案" :span="2">{{ cur.plan || '（尚未提交）' }}</el-descriptions-item>
        <el-descriptions-item label="实施说明" :span="2">{{ cur.implementNote || '（尚未提交）' }}</el-descriptions-item>
        <el-descriptions-item label="审核/验收意见" :span="2">{{ cur.reviewOpinion || '（暂无）' }}</el-descriptions-item>
        <el-descriptions-item label="验收材料" :span="2">
          <div v-for="(f, i) in parseFiles(cur.evidence)" :key="i" class="ev-line">{{ i + 1 }}. {{ f.name }}<span v-if="f.url">（{{ f.url }}）</span></div>
          <span v-if="!parseFiles(cur.evidence).length">（无）</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const STATUS = ['待方案提交', '方案待审核', '整改实施中', '待验收', '验收驳回重整改', '验收闭环']
const me = localStorage.getItem('username') || 'dev-user'
const staff = computed(() => can('C02', 'C03', 'C99'))

const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const filter = reactive({ status: '', dept: '', overdueOnly: false })

const createVis = ref(false)
const form = reactive({ title: '', dept: '', sourceTag: '', owner: '', deadline: '' })

const planVis = ref(false)
const planText = ref('')
const reviewVis = ref(false)
const reviewKind = ref('plan')
const reviewPass = ref(true)
const reviewOpinion = ref('')
const evidenceVis = ref(false)
const implementNote = ref('')
const files = ref([])
const detailVis = ref(false)
const cur = ref(null)

function fmt(s) { return s ? String(s).replace('T', ' ').slice(0, 16) : '-' }
function splitTags(s) { return (s || '').split(',').map(x => x.trim()).filter(Boolean) }
function parseFiles(s) { try { return JSON.parse(s || '[]') } catch (e) { return [] } }
function statusType(s) {
  return { 待方案提交: 'info', 方案待审核: 'warning', 整改实施中: '', 待验收: 'warning', 验收闭环: 'success', 验收驳回重整改: 'danger' }[s] || 'info'
}
function isOverdue(row) {
  if (row.status === '验收闭环' || !row.deadline) return false
  const today = new Date()
  const d = new Date(row.deadline + 'T23:59:59')
  return d < today
}
function ownerOrStaff(row) { return staff.value || row.owner === me }

async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/survey-rectify/list', {
      params: {
        status: filter.status || undefined,
        dept: filter.dept || undefined,
        overdueOnly: filter.overdueOnly ? true : undefined
      }
    })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}

function openCreate() {
  Object.assign(form, { title: '', dept: '', sourceTag: '', owner: me === 'dev-user' ? '' : me, deadline: '' })
  createVis.value = true
}
async function doCreate() {
  if (!form.title.trim() || !form.dept.trim() || !form.owner.trim()) return ElMessage.warning('任务名称、责任部门、责任人必填')
  saving.value = true
  try {
    await axios.post('/api/culture/survey-rectify', {
      title: form.title.trim(), dept: form.dept.trim(), sourceTag: form.sourceTag || null,
      owner: form.owner.trim(), deadline: form.deadline || null
    })
    ElMessage.success('整改任务已创建')
    createVis.value = false
    load()
  } finally { saving.value = false }
}

function openPlan(row) {
  cur.value = row
  planText.value = row.plan || ''
  planVis.value = true
}
async function doPlan() {
  if (planText.value.trim().length < 20) return ElMessage.warning('整改方案不少于20字')
  saving.value = true
  try {
    await axios.post(`/api/culture/survey-rectify/${cur.value.id}/plan`, { plan: planText.value.trim() })
    ElMessage.success('方案已提交，等待文化专员审核')
    planVis.value = false
    load()
  } finally { saving.value = false }
}

function openReview(row, kind) {
  cur.value = row
  reviewKind.value = kind
  reviewPass.value = kind === 'accept'
  reviewOpinion.value = row.reviewOpinion || ''
  reviewVis.value = true
}
async function doReview() {
  if (reviewOpinion.value.trim().length < 5) return ElMessage.warning('审核意见不少于5字')
  saving.value = true
  try {
    const url = reviewKind.value === 'plan' ? 'plan-review' : 'accept'
    await axios.post(`/api/culture/survey-rectify/${cur.value.id}/${url}`, null, {
      params: { pass: reviewPass.value, opinion: reviewOpinion.value.trim() }
    })
    ElMessage.success(reviewPass.value ? '已通过' : '已驳回')
    reviewVis.value = false
    load()
  } finally { saving.value = false }
}

function openEvidence(row) {
  cur.value = row
  implementNote.value = row.implementNote || ''
  files.value = parseFiles(row.evidence).length ? parseFiles(row.evidence) : [{ name: '', url: '' }]
  evidenceVis.value = true
}
async function doEvidence() {
  if (implementNote.value.trim().length < 10) return ElMessage.warning('实施情况说明不少于10字')
  const fs = files.value.filter(f => f.name && f.name.trim())
  if (!fs.length) return ElMessage.warning('请至少添加1份佐证材料')
  saving.value = true
  try {
    await axios.post(`/api/culture/survey-rectify/${cur.value.id}/evidence`, {
      implementNote: implementNote.value.trim(),
      evidence: JSON.stringify(fs.map(f => ({ name: f.name.trim(), url: (f.url || '').trim() })))
    })
    ElMessage.success('验收材料已提交，等待验收')
    evidenceVis.value = false
    load()
  } finally { saving.value = false }
}

function openDetail(row) { cur.value = row; detailVis.value = true }

onMounted(load)
</script>

<style scoped>
.bar { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.mt { margin-top: 10px; }
.ml { margin-left: 6px; }
.mb { margin-bottom: 12px; }
.rate-bad { color: #f56c6c; font-weight: 600; }
.score-tag { margin: 2px 4px 2px 0; }
.file-line { display: flex; gap: 8px; margin-bottom: 8px; }
.ev-line { line-height: 1.9; color: #444; }
</style>

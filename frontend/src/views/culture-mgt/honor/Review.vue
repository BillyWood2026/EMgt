<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">评审链：C04部门初审 → C02/C03文化专员复核(自动读取文化综合得分) → 发起公示(全员异议) → C07高管终审表彰</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <el-tabs v-model="tab" @tab-change="load">
        <el-tab-pane v-for="t in TABS" :key="t.key" :name="t.key">
          <template #label>{{ t.label }}<el-badge v-if="counts[t.key]" :value="counts[t.key]" class="tabbadge" /></template>
        </el-tab-pane>
      </el-tabs>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="500">
        <el-table-column prop="candidate" label="申报对象" width="120" />
        <el-table-column prop="projectName" label="荣誉项目" width="130" />
        <el-table-column label="类型" width="70">
          <template #default="{row}">
            <el-tag size="small" :type="row.applyType === '个人' ? '' : 'success'">{{ row.applyType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userDept" label="部门" width="120" show-overflow-tooltip />
        <el-table-column prop="deeds" label="先进事迹" min-width="220" show-overflow-tooltip />
        <el-table-column label="文化得分" width="90" align="center">
          <template #default="{row}">{{ row.cultureScore == null ? '-' : row.cultureScore }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{row}">
            <el-button size="small" text type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === '已申报' && can('C04')" size="small" type="primary" @click="openFirst(row)">初审</el-button>
            <el-button v-if="row.status === '部门初审' && can('C02', 'C03')" size="small" type="primary" @click="openReview(row)">复核</el-button>
            <el-button v-if="row.status === '评审中' && can('C02', 'C03')" size="small" type="warning" @click="openPublicize(row)">发起公示</el-button>
            <el-button v-if="row.status === '公示中' && can('C07')" size="small" type="success" @click="openFinal(row)">终审表彰</el-button>
            <el-button v-if="canReject(row) && hasRejectRole(row)" size="small" type="danger" plain @click="openReject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="detailVis" size="560px" title="申报详情">
      <template v-if="cur">
        <h4>{{ cur.candidate }} · {{ cur.projectName }}（{{ cur.status }}）</h4>
        <p class="deeds-full">{{ cur.deeds }}</p>
        <div class="att" v-for="(a, i) in arr(cur.attachments)" :key="i">📎 {{ a.name }}（{{ a.size }}）</div>
        <el-divider>评审记录</el-divider>
        <el-timeline>
          <el-timeline-item v-for="(e, i) in arr(cur.approvalLog)" :key="i"
            :type="e.pass ? 'success' : 'danger'" :timestamp="e.time">
            <b>{{ e.by }}</b>（{{ e.roles }}）
            <div class="op">{{ e.opinion }}</div>
          </el-timeline-item>
        </el-timeline>
        <el-divider>公示与异议</el-divider>
        <p>公示期：{{ cur.publicityStart || '-' }} ~ {{ cur.publicityEnd || '-' }}</p>
        <el-alert v-for="(o, i) in arr(cur.objections)" :key="'o' + i" class="mb"
          :title="'异议人 ' + o.by + '：' + o.reason" type="warning" :closable="false" />
      </template>
    </el-drawer>

    <el-dialog v-model="opVis" :title="opTitle" width="520px">
      <el-input v-model="opinion" type="textarea" :rows="4" maxlength="500" show-word-limit
        placeholder="通过或驳回均需填写不少于5字的评审意见（留痕）" />
      <div v-if="opMode === 'review'" class="tip">通过后系统自动读取该对象已发布案例积分合计作为文化综合得分</div>
      <div v-if="opMode === 'publicize'" class="formrow">
        <el-date-picker v-model="pubStart" type="date" value-format="YYYY-MM-DD" placeholder="公示开始" style="width:48%" />
        <el-date-picker v-model="pubEnd" type="date" value-format="YYYY-MM-DD" placeholder="公示结束" style="width:48%" />
      </div>
      <div v-if="opMode === 'final'" class="finalbox">
        <el-form label-width="90px" size="small">
          <el-form-item label="表彰时间" required>
            <el-date-picker v-model="awardTime" type="date" value-format="YYYY-MM-DD" style="width:100%" />
          </el-form-item>
          <el-form-item label="证书附件">
            <div v-for="(c, i) in certs" :key="i" class="filerow">
              <el-input v-model="c.name" size="small" placeholder="证书文件名" style="width:55%" />
              <el-input v-model="c.size" size="small" placeholder="大小" style="width:25%" />
              <el-button size="small" text type="danger" @click="certs.splice(i, 1)">删</el-button>
            </div>
            <el-button size="small" @click="certs.push({ name: '', size: '' })">+ 添加证书</el-button>
          </el-form-item>
          <el-form-item label="同步案例库">
            <el-switch v-model="syncCase" />
            <span class="tip" v-if="cur && cur.autoSyncCase === 0">当前项目未开启自动同步配置</span>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button size="small" @click="opVis = false">取消</el-button>
        <el-button v-if="opMode === 'first' || opMode === 'review'" size="small" type="danger" @click="doOp(false)">驳回</el-button>
        <el-button v-if="opMode === 'first' || opMode === 'review'" size="small" type="primary" @click="doOp(true)">通过</el-button>
        <el-button v-if="opMode === 'publicize'" size="small" type="warning" @click="doPublicize">发起公示</el-button>
        <el-button v-if="opMode === 'final'" size="small" type="success" @click="doFinal">确认表彰</el-button>
        <el-button v-if="opMode === 'reject'" size="small" type="danger" @click="doReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const TABS = [
  { key: 'all', label: '全部' },
  { key: '已申报', label: '待初审' },
  { key: '部门初审', label: '待复核' },
  { key: '评审中', label: '待公示' },
  { key: '公示中', label: '公示中' },
  { key: '终审表彰', label: '已表彰' },
  { key: '评审驳回', label: '已驳回' }
]
const tab = ref('all')
const rows = ref([])
const all = ref([])
const loading = ref(false)
const detailVis = ref(false)
const cur = ref(null)
const opVis = ref(false)
const opMode = ref('first')
const opTitle = ref('')
const opinion = ref('')
const pubStart = ref('')
const pubEnd = ref('')
const awardTime = ref('')
const certs = ref([])
const syncCase = ref(false)

const counts = computed(() => {
  const c = {}
  for (const t of TABS) {
    if (t.key !== 'all') c[t.key] = all.value.filter(r => r.status === t.key).length
  }
  return c
})
function arr(json) {
  if (!json) return []
  try { return JSON.parse(json) } catch (e) { return [] }
}
function statusType(s) {
  if (s === '终审表彰') return 'success'
  if (s === '公示中') return 'warning'
  if (s === '评审驳回') return 'danger'
  if (s === '草稿') return 'info'
  return ''
}
function canReject(row) {
  return ['已申报', '部门初审', '评审中', '公示中'].includes(row.status)
}
function hasRejectRole(row) {
  if (row.status === '已申报') return can('C04')
  if (row.status === '公示中') return can('C07')
  return can('C02', 'C03')
}
async function load() {
  loading.value = true
  try {
    const params = {}
    if (tab.value !== 'all') params.status = tab.value
    const r = await axios.get('/api/culture/honor/list', { params })
    rows.value = r.data.data || []
    if (tab.value === 'all') {
      all.value = rows.value
    } else {
      const r2 = await axios.get('/api/culture/honor/list')
      all.value = r2.data.data || []
    }
  } finally { loading.value = false }
}
function openDetail(row) {
  cur.value = row
  detailVis.value = true
}
function openFirst(row) {
  cur.value = row; opMode.value = 'first'; opTitle.value = '部门初审 - ' + row.candidate; opinion.value = ''; opVis.value = true
}
function openReview(row) {
  cur.value = row; opMode.value = 'review'; opTitle.value = '文化专员复核 - ' + row.candidate; opinion.value = ''; opVis.value = true
}
function openPublicize(row) {
  cur.value = row; opMode.value = 'publicize'; opTitle.value = '发起公示 - ' + row.candidate
  opinion.value = ''; pubStart.value = ''; pubEnd.value = ''; opVis.value = true
}
function openFinal(row) {
  cur.value = row; opMode.value = 'final'; opTitle.value = '高管终审表彰 - ' + row.candidate
  awardTime.value = ''; certs.value = []; syncCase.value = row.autoSyncCase === 1; opVis.value = true
}
function openReject(row) {
  cur.value = row; opMode.value = 'reject'; opTitle.value = '评审驳回 - ' + row.candidate + '（' + row.status + '）'; opinion.value = ''; opVis.value = true
}
async function doOp(pass) {
  if (opinion.value.trim().length < 5) { ElMessage.warning('评审意见不少于5字'); return }
  const url = opMode.value === 'first' ? 'first-review' : 'review'
  await axios.post(`/api/culture/honor/${cur.value.id}/${url}`, null, { params: { pass, opinion: opinion.value } })
  ElMessage.success(pass ? '已通过' : '已驳回')
  opVis.value = false
  load()
}
async function doPublicize() {
  if (!pubStart.value || !pubEnd.value) { ElMessage.warning('公示起止时间必填'); return }
  if (pubEnd.value < pubStart.value) { ElMessage.warning('公示结束不得早于开始'); return }
  await axios.post(`/api/culture/honor/${cur.value.id}/publicize`, null, { params: { start: pubStart.value, end: pubEnd.value } })
  ElMessage.success('已发起公示')
  opVis.value = false
  load()
}
async function doFinal() {
  if (!awardTime.value) { ElMessage.warning('表彰时间必填'); return }
  try {
    await ElMessageBox.confirm('确认对该申报对象进行终审表彰？表彰后自动公开至榜样风采。', '表彰确认', { type: 'success' })
  } catch (e) { return }
  await axios.post(`/api/culture/honor/${cur.value.id}/finalize`, { awardTime: awardTime.value, certFiles: certs.value, syncCase: syncCase.value })
  ElMessage.success('已终审表彰')
  opVis.value = false
  load()
}
async function doReject() {
  if (opinion.value.trim().length < 5) { ElMessage.warning('驳回意见不少于5字'); return }
  await axios.post(`/api/culture/honor/${cur.value.id}/reject`, null, { params: { opinion: opinion.value } })
  ElMessage.success('已驳回')
  opVis.value = false
  load()
}
onMounted(load)
</script>

<style scoped>
.mb { margin-bottom: 12px; }
.tabbadge { margin-left: 4px; }
.deeds-full { white-space: pre-wrap; font-size: 13px; line-height: 1.7; }
.att { font-size: 12px; color: #409eff; margin: 4px 0; }
.op { font-size: 12px; color: #606266; margin-top: 2px; }
.tip { font-size: 12px; color: #909399; }
.formrow { display: flex; justify-content: space-between; margin-top: 10px; }
.filerow { display: flex; gap: 6px; margin-bottom: 6px; }
.finalbox { margin-top: 6px; }
</style>

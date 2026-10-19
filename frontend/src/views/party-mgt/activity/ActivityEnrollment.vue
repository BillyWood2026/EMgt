<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">一人一活动一次签到 · 定位半径500米超差标待核实 · 离线补签须填原因并审批 · 签到通道随活动开闭</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="activityId" placeholder="选择已发布活动" size="small" filterable style="width:340px" @change="load">
          <el-option v-for="p in plans" :key="p.id" :label="`[${p.orgName}] ${p.planDate} ${p.title}`" :value="p.id" />
        </el-select>
        <el-button size="small" @click="loadPlans">刷新活动</el-button>
        <span style="flex:1"></span>
        <el-button size="small" type="success" :disabled="!activityId" @click="statVisible=true; loadStat()">签到统计</el-button>
      </div>

      <div v-if="activityId" class="bar">
        <el-input v-model="newMember.memberName" placeholder="党员姓名" size="small" style="width:130px" />
        <el-input-number v-model="newMember.memberId" :min="1" size="small" controls-position="right" placeholder="党员ID" style="width:120px" />
        <el-button v-if="can('R02','R07','R08')" size="small" type="primary" @click="enroll">报名</el-button>
        <el-select v-model="signMethod" size="small" style="width:120px">
          <el-option v-for="m in ['现场扫码','定位','人脸','人工代签']" :key="m" :label="m" :value="m" />
        </el-select>
        <el-input-number v-model="distanceM" :min="0" size="small" controls-position="right" placeholder="定位距离(米)" style="width:140px" />
      </div>

      <el-table :data="rows" size="small" border v-loading="loading">
        <el-table-column prop="memberId" label="党员ID" width="80" />
        <el-table-column prop="memberName" label="姓名" width="110" />
        <el-table-column prop="enrollTime" label="报名时间" width="160">
          <template #default="{ row }">{{ fmt(row.enrollTime) }}</template>
        </el-table-column>
        <el-table-column prop="signMethod" label="签到方式" width="90" />
        <el-table-column prop="signInTime" label="签到时间" width="160">
          <template #default="{ row }">{{ fmt(row.signInTime) }}</template>
        </el-table-column>
        <el-table-column label="距离" width="80">
          <template #default="{ row }">{{ row.distanceM != null ? row.distanceM + '米' : '—' }}</template>
        </el-table-column>
        <el-table-column label="签到状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.signStatus==='已签到'?'success':(row.signStatus==='待核实'?'warning':'info')">{{ row.signStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="makeUpReason" label="补签原因" min-width="140" show-overflow-tooltip />
        <el-table-column label="补签审核" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.makeUpAudit" size="small" :type="row.makeUpAudit==='通过'?'success':(row.makeUpAudit==='驳回'?'danger':'warning')">{{ row.makeUpAudit }}</el-tag>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('R02','R07','R08') && row.signStatus!=='已签到'" size="small" link type="primary" @click="sign(row)">签到</el-button>
            <el-button v-if="can('R02','R07','R08') && row.signStatus!=='已签到'" size="small" link type="warning" @click="openMakeup(row)">补签</el-button>
            <el-button v-if="can('R02','R07') && row.makeUpAudit==='待审'" size="small" link type="success" @click="audit(row,true)">补签通过</el-button>
            <el-button v-if="can('R02','R07') && row.makeUpAudit==='待审'" size="small" link type="danger" @click="audit(row,false)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="makeupVisible" title="离线补签（须注明原因）" width="440px">
      <el-alert title="未注明原因的补签将被阻断（E-AC-05）" type="warning" :closable="false" style="margin-bottom:10px" />
      <el-input v-model="makeupReason" type="textarea" :rows="3" placeholder="请填写补签原因，如：现场网络故障、因公请假" />
      <template #footer>
        <el-button @click="makeupVisible=false">取消</el-button>
        <el-button type="primary" @click="submitMakeup">提交补签</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statVisible" title="签到统计（到会率）" width="460px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="活动">{{ stat.activityTitle }}</el-descriptions-item>
        <el-descriptions-item label="到会率">{{ stat.attendRate }}%</el-descriptions-item>
        <el-descriptions-item label="应参加">{{ stat.expectCount }}</el-descriptions-item>
        <el-descriptions-item label="已报名">{{ stat.enrolledCount }}</el-descriptions-item>
        <el-descriptions-item label="已签到">{{ stat.signedCount }}</el-descriptions-item>
        <el-descriptions-item label="待核实">{{ stat.pendingCount }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/activity/enrollment'
const loading = ref(false)
const plans = ref([])
const activityId = ref(null)
const rows = ref([])
const newMember = reactive({ memberId: null, memberName: '' })
const signMethod = ref('现场扫码')
const distanceM = ref()
const makeupVisible = ref(false)
const makeupReason = ref('')
const makeupRow = ref(null)
const statVisible = ref(false)
const stat = ref({})

function fmt(t) { return t ? String(t).replace('T', ' ').slice(0, 19) : '—' }

async function loadPlans() {
  const res = await axios.get('/api/party/activity/plan/list', { params: { publishStatus: '已发布' } })
  if (res.data.code === 200) plans.value = res.data.data
}
async function load() {
  if (!activityId.value) { rows.value = []; return }
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { activityId: activityId.value } })
    if (res.data.code === 200) rows.value = res.data.data
  } finally { loading.value = false }
}
async function enroll() {
  if (!newMember.memberId || !newMember.memberName) { ElMessage.warning('请填写党员ID与姓名'); return }
  try {
    await axios.post(`${api}/enroll`, null, { params: { activityId: activityId.value, memberId: newMember.memberId, memberName: newMember.memberName } })
    ElMessage.success('报名成功')
    newMember.memberId = null; newMember.memberName = ''
    load()
  } catch {}
}
async function sign(row) {
  try {
    // force=true：演示环境活动非当天时模拟现场通道开启
    await axios.post(`${api}/sign`, null, { params: { activityId: activityId.value, memberId: row.memberId, method: signMethod.value, distanceM: distanceM.value, force: true } })
    ElMessage.success('签到成功')
    load()
  } catch {}
}
function openMakeup(row) { makeupRow.value = row; makeupReason.value = ''; makeupVisible.value = true }
async function submitMakeup() {
  try {
    await axios.post(`${api}/${makeupRow.value.id}/makeup`, null, { params: { reason: makeupReason.value, directPass: false } })
    ElMessage.success('补签申请已提交，待审批')
    makeupVisible.value = false
    load()
  } catch {}
}
async function audit(row, pass) {
  try {
    await axios.post(`${api}/${row.id}/audit`, null, { params: { pass } })
    ElMessage.success(pass ? '补签已通过' : '补签已驳回')
    load()
  } catch {}
}
async function loadStat() {
  const res = await axios.get(`${api}/stat/${activityId.value}`)
  if (res.data.code === 200) stat.value = res.data.data
}
onMounted(async () => {
  await loadPlans()
  // 默认优先选中已有报名数据的活动，其次第一个已发布活动
  let defaultId = plans.value.length ? plans.value[0].id : null
  try {
    const all = await axios.get(`${api}/list`)
    if (all.data.code === 200 && all.data.data.length) {
      const cnt = {}
      all.data.data.forEach(e => { cnt[e.activityId] = (cnt[e.activityId] || 0) + 1 })
      defaultId = Number(Object.keys(cnt).sort((a, b) => cnt[b] - cnt[a])[0])
    }
  } catch {}
  if (defaultId) { activityId.value = defaultId; load() }
})
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.muted { color: #c0c4cc; }
</style>

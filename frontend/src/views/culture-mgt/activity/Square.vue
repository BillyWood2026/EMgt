<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">活动广场：经两级审批发布的价值观活动，报名中/进行中状态可参与；报名截止后不可再报名</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-radio-group v-model="q.type" size="small" @change="load">
          <el-radio-button label="">全部类型</el-radio-button>
          <el-radio-button v-for="t in TYPES" :key="t" :label="t">{{ t }}</el-radio-button>
        </el-radio-group>
        <span style="flex:1"></span>
        <el-button size="small" @click="loadMy">我的报名</el-button>
        <el-button size="small" @click="load">刷新</el-button>
      </div>
    </el-card>

    <div v-loading="loading" class="wall">
      <el-card v-for="a in rows" :key="a.id" shadow="hover" class="act-card" @click="openDetail(a)">
        <div class="ac-head">
          <el-tag size="small">{{ a.actType }}</el-tag>
          <el-tag size="small" :type="a.status==='已发布' ? 'success' : 'warning'">{{ a.status==='已发布' ? '报名中' : a.status }}</el-tag>
          <span v-if="a.maxSign>0" class="quota">{{ a.signedCount }}/{{ a.maxSign }}</span>
          <span v-else class="quota">{{ a.signedCount }} 人已报</span>
        </div>
        <div class="ac-title">{{ a.actName }}</div>
        <div class="ac-meta">
          <div><el-icon><Calendar /></el-icon>{{ fmt(a.planStart) }}</div>
          <div><el-icon><Timer /></el-icon>报名截止 {{ fmt(a.signDeadline) }}</div>
          <div><el-icon><OfficeBuilding /></el-icon>{{ a.organizerDept }}</div>
        </div>
        <div class="ac-btn">
          <el-button v-if="!joined(a.id)" size="small" type="primary" @click.stop="doSign(a)">立即报名</el-button>
          <el-button v-else size="small" @click.stop="doCancelSign(a)">取消报名</el-button>
        </div>
      </el-card>
      <el-empty v-if="!loading && rows.length===0" description="暂无进行中的活动" />
    </div>

    <el-drawer v-model="detailVis" size="520px" :title="cur.actName || '活动详情'">
      <div v-if="cur.id">
        <div class="d-row">
          <el-tag size="small">{{ cur.actType }}</el-tag>
          <el-tag size="small" :type="cur.status==='已发布' ? 'success':'warning'">{{ cur.status==='已发布' ? '报名中' : cur.status }}</el-tag>
        </div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="主办部门">{{ cur.organizerDept }}</el-descriptions-item>
          <el-descriptions-item label="经办人">{{ cur.manager }}</el-descriptions-item>
          <el-descriptions-item label="活动时间">{{ fmt(cur.planStart) }} ~ {{ fmt(cur.planEnd) }}</el-descriptions-item>
          <el-descriptions-item label="报名截止">{{ fmt(cur.signDeadline) }}</el-descriptions-item>
          <el-descriptions-item label="报名情况">{{ cur.signedCount }}<span v-if="cur.maxSign>0">/{{ cur.maxSign }}</span></el-descriptions-item>
          <el-descriptions-item label="签到/应参与">{{ cur.checkedCount }}/{{ cur.shouldCount }}</el-descriptions-item>
        </el-descriptions>
        <div class="sec-title">活动简介</div>
        <div class="d-content">{{ cur.intro }}</div>
        <template v-if="cur.status==='已闭环'">
          <div class="sec-title">复盘总结</div>
          <div class="d-content">{{ cur.summary }}</div>
          <div class="sec-title">成效评估</div>
          <div class="d-content">{{ cur.resultEval }}</div>
        </template>
        <div v-if="cur.status==='已发布'" class="d-foot">
          <el-button v-if="!joined(cur.id)" type="primary" size="small" @click="doSign(cur)">立即报名</el-button>
          <el-button v-else size="small" @click="doCancelSign(cur)">取消报名</el-button>
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="myVis" title="我的报名记录" width="640px">
      <el-table :data="mySigns" size="small" border max-height="360">
        <el-table-column label="活动" min-width="200">
          <template #default="{row}">{{ actNameOf(row.activityId) }}</template>
        </el-table-column>
        <el-table-column prop="signStatus" label="报名状态" width="90" />
        <el-table-column prop="checkStatus" label="签到状态" width="90" />
        <el-table-column label="报名时间" width="150">
          <template #default="{row}">{{ fmt(row.signTime) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'

const TYPES = ['文化宣讲', '价值观读书会', '奋斗者分享', '团建', '竞赛', '周年纪念', '新人融入']
const rows = ref([])
const mySigns = ref([])
const allActs = ref([])
const loading = ref(false)
const q = reactive({ type: '' })
const detailVis = ref(false)
const myVis = ref(false)
const cur = ref({})

function fmt(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '—' }
function joined(actId) { return mySigns.value.some(s => s.activityId === actId && s.signStatus === '已报名') }
function actNameOf(id) { const a = allActs.value.find(x => x.id === id); return a ? a.actName : ('活动#' + id) }

async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/activity/square', { params: { type: q.type || undefined } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}
async function loadMy() {
  const r = await axios.get('/api/culture/activity-sign/mine')
  mySigns.value = r.data.data || []
  const lr = await axios.get('/api/culture/activity/list')
  allActs.value = lr.data.data || []
  myVis.value = true
}
function openDetail(a) { cur.value = a; detailVis.value = true }
async function doSign(a) {
  await axios.post(`/api/culture/activity-sign/${a.id}/sign`, {})
  ElMessage.success('报名成功')
  load()
}
async function doCancelSign(a) {
  await ElMessageBox.confirm('确认取消报名该活动？截止后不可再报名。', '取消报名', { type: 'warning' })
  await axios.post(`/api/culture/activity-sign/${a.id}/cancel`)
  ElMessage.success('已取消报名')
  load()
}
onMounted(async () => {
  load()
  try { const r = await axios.get('/api/culture/activity-sign/mine'); mySigns.value = r.data.data || [] } catch { mySigns.value = [] }
})
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:10px; flex-wrap:wrap; }
.mb { margin-bottom:12px; }
.wall { display:grid; grid-template-columns:repeat(auto-fill,minmax(330px,1fr)); gap:12px; }
.act-card { cursor:pointer; }
.ac-head { display:flex; align-items:center; gap:6px; }
.quota { margin-left:auto; font-weight:700; color:var(--el-color-primary); font-size:13px; }
.ac-title { font-size:15px; font-weight:600; margin:8px 0; line-height:1.5; }
.ac-meta { color:#606266; font-size:12px; display:grid; gap:4px; }
.ac-meta div { display:flex; align-items:center; gap:5px; }
.ac-btn { margin-top:10px; text-align:right; }
.sec-title { font-weight:600; margin:14px 0 8px; }
.d-content { white-space:pre-wrap; line-height:1.8; background:#f7f9fc; padding:10px; border-radius:6px; }
.d-row { display:flex; gap:8px; margin-bottom:12px; }
.d-foot { margin-top:16px; text-align:right; }
</style>

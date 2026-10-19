<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">报名与签到：活动进行中由部门负责人(C04)/文化专员(C02/C03)登记现场签到；参与率=签到/应参与</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="actId" placeholder="选择活动" filterable size="small" style="width:420px" @change="loadRoster">
          <el-option v-for="a in acts" :key="a.id" :label="`[${a.status}] ${a.actName}`" :value="a.id" />
        </el-select>
        <span style="flex:1"></span>
        <el-tag v-if="curAct" size="small" :type="curAct.status==='进行中' ? 'warning':'info'">{{ curAct.status }}</el-tag>
        <el-tag v-if="curAct" size="small" type="success" effect="plain">参与率 {{ rate }}%</el-tag>
      </div>

      <template v-if="actId">
        <el-descriptions v-if="curAct" :column="3" border size="small" class="mt">
          <el-descriptions-item label="活动时间">{{ fmt(curAct.planStart) }} ~ {{ fmt(curAct.planEnd) }}</el-descriptions-item>
          <el-descriptions-item label="已报名/限额">{{ curAct.signedCount }}<span v-if="curAct.maxSign>0">/{{ curAct.maxSign }}</span></el-descriptions-item>
          <el-descriptions-item label="签到/应参与">{{ curAct.checkedCount }}/{{ curAct.shouldCount }}</el-descriptions-item>
        </el-descriptions>

        <el-table :data="roster" size="small" border v-loading="rosterLoading" class="mt" max-height="440">
          <el-table-column prop="userName" label="员工" width="130" />
          <el-table-column prop="userDept" label="部门" min-width="150" show-overflow-tooltip />
          <el-table-column label="报名时间" width="150">
            <template #default="{row}">{{ fmt(row.signTime) }}</template>
          </el-table-column>
          <el-table-column label="报名状态" width="90">
            <template #default="{row}">
              <el-tag size="small" :type="row.signStatus==='已报名' ? 'success':'info'">{{ row.signStatus }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="签到状态" width="90">
            <template #default="{row}">
              <el-tag size="small" :type="checkType(row.checkStatus)">{{ row.checkStatus }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="签到时间/登记人" width="170">
            <template #default="{row}">{{ row.checkTime ? fmt(row.checkTime) : '—' }}<span v-if="row.checkBy"> · {{ row.checkBy }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="230" fixed="right">
            <template #default="{row}">
              <template v-if="can('C04','C02','C03','C99') && row.signStatus==='已报名'">
                <el-button size="small" type="success" @click="doCheck(row, '已签到')">签到</el-button>
                <el-button size="small" type="warning" @click="doCheck(row, '迟到')">迟到</el-button>
                <el-button size="small" @click="doCheck(row, '请假')">请假</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!rosterLoading && roster.length===0" description="该活动暂无报名记录" :image-size="70" />
      </template>
      <el-empty v-else description="请先选择活动" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const acts = ref([])
const actId = ref(null)
const curAct = ref(null)
const roster = ref([])
const rosterLoading = ref(false)

const rate = computed(() => {
  if (!curAct.value || !curAct.value.shouldCount) return 0
  return Math.round((curAct.value.checkedCount || 0) / curAct.value.shouldCount * 1000) / 10
})
function fmt(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '—' }
function checkType(s) {
  if (s === '已签到') return 'success'
  if (s === '迟到') return 'warning'
  if (s === '请假') return 'info'
  return ''
}
async function loadActs() {
  const r = await axios.get('/api/culture/activity/list')
  acts.value = (r.data.data || []).filter(a => ['已发布', '进行中', '待复盘'].includes(a.status))
  if (acts.value.length && !actId.value) {
    const running = acts.value.find(a => a.status === '进行中')
    actId.value = (running || acts.value[0]).id
    loadRoster()
  }
}
async function loadRoster() {
  if (!actId.value) return
  rosterLoading.value = true
  try {
    curAct.value = acts.value.find(a => a.id === actId.value) || null
    const r = await axios.get('/api/culture/activity-sign/roster', { params: { activityId: actId.value } })
    roster.value = r.data.data || []
  } finally { rosterLoading.value = false }
}
async function doCheck(row, status) {
  await axios.post(`/api/culture/activity-sign/${actId.value}/check`, null, { params: { userName: row.userName, checkStatus: status } })
  ElMessage.success(`已登记：${row.userName} ${status}`)
  loadRoster()
}
onMounted(loadActs)
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.mt { margin-top:10px; }
</style>

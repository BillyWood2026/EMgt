<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">榜样风采：终审表彰自动公开；公示期全员可提异议；重大红线违规自动拦截申报</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <span class="title">榜样风采</span>
        <el-input v-model="keyword" placeholder="搜索榜样/项目" size="small" clearable style="width:200px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button size="small" type="primary" @click="goMine">我要申报荣誉</el-button>
      </div>
      <div v-if="rows.length" class="cards">
        <el-card v-for="r in rows" :key="r.id" shadow="hover" class="hcard">
          <div class="chead">
            <span class="cname">{{ r.candidate }}</span>
            <el-tag size="small" type="warning" effect="dark">{{ r.projectName }}</el-tag>
          </div>
          <div class="cdate">表彰时间：{{ r.awardTime }}<span v-if="r.userDept"> · {{ r.userDept }}</span></div>
          <div class="cdeeds">{{ brief(r.deeds) }}</div>
          <div class="cfoot">
            <el-tag v-if="r.syncCase === 1" size="small" type="success" effect="plain">事迹已入库案例库</el-tag>
            <el-tag v-if="countArr(r.certFiles) > 0" size="small" effect="plain">证书 {{ countArr(r.certFiles) }} 份</el-tag>
            <el-button size="small" text type="primary" @click="openDetail(r)">查看全流程</el-button>
          </div>
        </el-card>
      </div>
      <el-empty v-else description="暂无表彰榜样" :image-size="70" />
    </el-card>

    <el-card shadow="never">
      <div class="bar mb0">
        <span class="title">公示中名单（公示期内可提交异议）</span>
      </div>
      <el-table :data="pubs" size="small" border v-loading="pubLoading" max-height="300">
        <el-table-column prop="candidate" label="申报对象" width="140" />
        <el-table-column prop="projectName" label="荣誉项目" width="150" />
        <el-table-column label="公示期" width="200">
          <template #default="{row}">{{ row.publicityStart }} ~ {{ row.publicityEnd }}</template>
        </el-table-column>
        <el-table-column prop="reviewOpinion" label="复核意见" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{row}">
            <el-button size="small" type="warning" plain @click="openObjection(row)">提出异议</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="detailVis" size="560px" title="榜样荣誉全流程">
      <template v-if="cur">
        <h4>{{ cur.candidate }} · {{ cur.projectName }}</h4>
        <el-tabs>
          <el-tab-pane label="申报事迹">
            <p class="deeds-full">{{ cur.deeds }}</p>
            <div v-if="arr(cur.attachments).length">
              <div class="att" v-for="(a, i) in arr(cur.attachments)" :key="i">📎 {{ a.name }}（{{ a.size }}）</div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="评审记录">
            <el-timeline>
              <el-timeline-item v-for="(e, i) in arr(cur.approvalLog)" :key="i"
                :type="e.pass ? 'success' : 'danger'" :timestamp="e.time">
                <b>{{ stageName(e.stage) }}</b>（{{ e.by }} / {{ e.roles }}）
                <div class="op">{{ e.opinion }}</div>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
          <el-tab-pane label="公示与异议">
            <p>公示期：{{ cur.publicityStart || '-' }} ~ {{ cur.publicityEnd || '-' }}</p>
            <el-alert v-if="!arr(cur.objections).length" title="公示期内未收到异议" type="success" :closable="false" />
            <el-alert v-for="(o, i) in arr(cur.objections)" :key="'o' + i" class="mb"
              :title="'异议人 ' + o.by + '：' + o.reason" :type="o.handled ? 'info' : 'warning'" :closable="false" />
          </el-tab-pane>
          <el-tab-pane label="表彰凭证">
            <p>表彰时间：{{ cur.awardTime || '待表彰' }}</p>
            <div v-if="arr(cur.certFiles).length">
              <div class="att" v-for="(a, i) in arr(cur.certFiles)" :key="'c' + i">🏅 {{ a.name }}（{{ a.size }}）</div>
            </div>
            <p v-if="cur.caseId">关联案例库案例ID：{{ cur.caseId }}</p>
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-drawer>

    <el-dialog v-model="objVis" title="公示异议申诉" width="480px">
      <el-input v-model="objReason" type="textarea" :rows="4" maxlength="300" show-word-limit
        placeholder="公示期内对该申报提出异议，理由不少于5字（必填）" />
      <template #footer>
        <el-button size="small" @click="objVis = false">取消</el-button>
        <el-button size="small" type="warning" @click="doObjection">提交异议</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import RoleBar from '../CultureRoleBar.vue'

const router = useRouter()
const rows = ref([])
const pubs = ref([])
const keyword = ref('')
const loading = ref(false)
const pubLoading = ref(false)
const detailVis = ref(false)
const cur = ref(null)
const objVis = ref(false)
const objId = ref(null)
const objReason = ref('')

function brief(s) {
  if (!s) return ''
  return s.length > 90 ? s.slice(0, 90) + '…' : s
}
function countArr(json) {
  return arr(json).length
}
function arr(json) {
  if (!json) return []
  try { return JSON.parse(json) } catch (e) { return [] }
}
function stageName(st) {
  if (st === 0) return '提交申报'
  if (st === 1) return '部门初审'
  if (st === 2) return '文化专员复核'
  if (st === 3) return '发起公示'
  if (st === 5) return '高管终审表彰'
  return '节点 ' + st
}
async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/honor/square', { params: { keyword: keyword.value || undefined } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}
async function loadPubs() {
  pubLoading.value = true
  try {
    const r = await axios.get('/api/culture/honor/publicity')
    pubs.value = r.data.data || []
  } finally { pubLoading.value = false }
}
function openDetail(row) {
  cur.value = row
  detailVis.value = true
}
function openObjection(row) {
  objId.value = row.id
  objReason.value = ''
  objVis.value = true
}
async function doObjection() {
  if (objReason.value.trim().length < 5) { ElMessage.warning('异议理由不少于5字'); return }
  await axios.post(`/api/culture/honor/${objId.value}/objection`, { reason: objReason.value })
  ElMessage.success('异议已提交，待文化部门核实')
  objVis.value = false
  loadPubs()
}
function goMine() {
  router.push('/culture-mgt/honor/mine')
}
onMounted(() => { load(); loadPubs() })
</script>

<style scoped>
.bar { display: flex; align-items: center; gap: 8px; }
.mb { margin-bottom: 12px; }
.mb0 { margin-bottom: 8px; }
.title { font-weight: bold; font-size: 15px; }
.cards { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 10px; }
.hcard { width: 320px; }
.chead { display: flex; align-items: center; gap: 8px; }
.cname { font-weight: bold; font-size: 15px; }
.cdate { color: #909399; font-size: 12px; margin: 6px 0; }
.cdeeds { font-size: 12px; color: #606266; min-height: 54px; }
.cfoot { display: flex; align-items: center; gap: 6px; margin-top: 6px; }
.deeds-full { white-space: pre-wrap; font-size: 13px; line-height: 1.7; }
.att { font-size: 12px; color: #409eff; margin: 4px 0; }
.op { font-size: 12px; color: #606266; margin-top: 2px; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">档案只增不改全程留痕(E-SV-08) · 覆盖率&lt;95%橙色(E-SV-06) · 意见回复5工作日 · 材料缺失阻断任用(E-SV-09)</el-tag>
    </RoleBar>

    <el-alert v-if="stat.coverageAlert" :title="`建档覆盖率 ${stat.coverageRate}% 低于95%，待补材料 ${stat.waitingMaterials} 份（E-SV-06）`" type="warning" show-icon :closable="false" class="mb" />

    <el-row :gutter="10" class="mb">
      <el-col :span="6"><el-card shadow="hover" class="kpi"><div class="n">{{ stat.total||0 }}</div><div class="l">应建档</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="kpi"><div class="n">{{ stat.archived||0 }}</div><div class="l">已建档</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="kpi"><div class="n" :class="{orange: stat.coverageAlert}">{{ stat.coverageRate||0 }}%</div><div class="l">覆盖率</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="kpi"><div class="n" :class="{red: stat.replyOverdue>0}">{{ stat.replyOverdue||0 }}</div><div class="l">意见回复超期</div></el-card></el-col>
    </el-row>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="archiveStatus" placeholder="建档状态" clearable size="small" style="width:130px" @change="load">
          <el-option label="已建档" value="已建档" />
          <el-option label="待补材料" value="待补材料" />
        </el-select>
        <el-input v-model="keyword" placeholder="编号/姓名/职务" size="small" clearable style="width:180px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R23','R06','R02')" size="small" type="primary" @click="openCreate">建档</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="430">
        <el-table-column prop="profileNo" label="档案编号" width="120" />
        <el-table-column prop="personName" label="姓名" width="90" />
        <el-table-column prop="orgName" label="单位" width="130" show-overflow-tooltip />
        <el-table-column prop="position" label="职务" width="120" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{row}"><el-tag size="small" :type="row.archiveStatus==='已建档'?'success':'warning'">{{ row.archiveStatus }}</el-tag></template>
        </el-table-column>
        <el-table-column label="完整度" width="130">
          <template #default="{row}"><el-progress :percentage="Number(row.completeRate||0)" :stroke-width="10" :status="row.completeRate>=95?'success':''" /></template>
        </el-table-column>
        <el-table-column prop="opinionResult" label="最近意见" width="90">
          <template #default="{row}"><el-tag v-if="row.opinionResult" size="small" :type="row.opinionResult==='无影响'?'success':'danger'">{{ row.opinionResult }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="missingMaterials" label="缺失材料" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="openDetail(row)">档案</el-button>
            <el-button v-if="can('R02','R04','R06','R23')" size="small" type="warning" @click="appointCheck(row)">任用把关</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="createVis" title="廉政档案建档" width="460px">
      <el-form :model="form" label-width="80px" size="small">
        <el-form-item label="姓名"><el-input v-model="form.personName" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="form.orgName" /></el-form-item>
        <el-form-item label="职务"><el-input v-model="form.position" /></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="createVis=false">取消</el-button><el-button size="small" type="primary" @click="doCreate">建档</el-button></template>
    </el-dialog>

    <el-drawer v-model="detailVis" size="640px" :title="`廉政档案 ${cur?.profileNo||''} ${cur?.personName||''}`">
      <div v-if="cur" class="detail">
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="单位">{{ cur.orgName }}</el-descriptions-item>
          <el-descriptions-item label="职务">{{ cur.position }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag size="small" :type="cur.archiveStatus==='已建档'?'success':'warning'">{{ cur.archiveStatus }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="完整度">{{ cur.completeRate }}%</el-descriptions-item>
          <el-descriptions-item label="缺失材料" :span="2">{{ cur.missingMaterials||'无' }}</el-descriptions-item>
        </el-descriptions>

        <el-card v-if="can('R23','R06')" shadow="never" class="mt">
          <template #header><b>材料追加（只增不改 · 写留痕 E-SV-08）</b></template>
          <el-form :model="matForm" label-width="90px" size="small">
            <el-form-item label="材料名称">
              <el-select v-model="matForm.materialName" filterable allow-create style="width:100%">
                <el-option v-for="m in materials" :key="m" :label="m" :value="m" />
              </el-select>
            </el-form-item>
            <el-form-item label="内容摘要"><el-input v-model="matForm.content" type="textarea" :rows="2" /></el-form-item>
            <el-button size="small" type="primary" @click="addMaterial">追加并留痕</el-button>
          </el-form>
        </el-card>

        <el-card v-if="can('R23','R06')" shadow="never" class="mt">
          <template #header><div class="bar" style="margin:0"><b>廉政意见回复（5工作日）</b><el-button size="small" @click="openReply">发起回复</el-button></div></template>
          <el-table :data="replies" size="small" border max-height="200">
            <el-table-column prop="requestOrg" label="来函单位" width="110" />
            <el-table-column prop="matter" label="事项" min-width="120" show-overflow-tooltip />
            <el-table-column prop="deadline" label="时限" width="95" />
            <el-table-column prop="replyOpinion" label="意见" width="80">
              <template #default="{row}"><el-tag v-if="row.replyOpinion" size="small" :type="row.replyOpinion==='无影响'?'success':'danger'">{{ row.replyOpinion }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="70" />
            <el-table-column label="操作" width="80">
              <template #default="{row}"><el-button v-if="can('R06') && !row.replyOpinion" size="small" @click="openIssue(row)">出具</el-button></template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card shadow="never" class="mt">
          <template #header><b>变更留痕</b></template>
          <el-timeline>
            <el-timeline-item v-for="log in logs" :key="log.id" :timestamp="(log.createTime||'').replace('T',' ').slice(0,19)">
              [{{ log.action }}] {{ log.materialName }} — {{ log.content }} <span class="hint">（{{ log.operator }}）</span>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>
    </el-drawer>

    <el-dialog v-model="replyVis" title="发起廉政意见回复" width="420px">
      <el-form :model="replyForm" label-width="80px" size="small">
        <el-form-item label="来函单位"><el-input v-model="replyForm.requestOrg" /></el-form-item>
        <el-form-item label="征求事项"><el-input v-model="replyForm.matter" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="replyVis=false">取消</el-button><el-button size="small" type="primary" @click="doReply">发起</el-button></template>
    </el-dialog>

    <el-dialog v-model="issueVis" title="出具回复意见（R06 审批）" width="420px">
      <el-form :model="issueForm" label-width="80px" size="small">
        <el-form-item label="回复意见">
          <el-radio-group v-model="issueForm.replyOpinion">
            <el-radio value="无影响">无影响</el-radio><el-radio value="有影响">有影响</el-radio><el-radio value="暂缓">暂缓</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="核查情况"><el-input v-model="issueForm.recheckResult" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="issueVis=false">取消</el-button><el-button size="small" type="primary" @click="doIssue">出具</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/supervision/profile'
const materials = ['个人有关事项报告','廉政意见回复记录','信访举报反映','谈话函询情况','党纪政务处分情况','经济责任审计结果']

const rows = ref([]); const loading = ref(false)
const archiveStatus = ref(''); const keyword = ref('')
const stat = ref({})
const createVis = ref(false); const form = ref({})
const detailVis = ref(false); const cur = ref(null)
const matForm = ref({ materialName:'', content:'' })
const logs = ref([]); const replies = ref([])
const replyVis = ref(false); const replyForm = ref({})
const issueVis = ref(false); const issueForm = ref({}); const replyCur = ref(null)

async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { archiveStatus: archiveStatus.value||undefined, keyword: keyword.value||undefined } })
    rows.value = res.data.data || []
  } finally { loading.value = false }
}
async function loadStat() { try { stat.value = (await axios.get(`${api}/stats`)).data.data || {} } catch {} }
function openCreate() { form.value = {}; createVis.value = true }
async function doCreate() {
  try { await axios.post(api, form.value); createVis.value=false; ElMessage.success('建档成功'); load(); loadStat() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function openDetail(row) {
  cur.value = row; detailVis.value = true
  matForm.value = { materialName:'', content:'' }
  try {
    logs.value = (await axios.get(`${api}/${row.id}/logs`)).data.data || []
    replies.value = (await axios.get(`${api}/reply/list`, { params: { profileId: row.id } })).data.data || []
  } catch {}
}
async function addMaterial() {
  if (!matForm.value.materialName) { ElMessage.warning('请选择材料'); return }
  try {
    await axios.post(`${api}/${cur.value.id}/material`, matForm.value)
    ElMessage.success('已追加留痕'); openDetail(cur.value); load(); loadStat()
  } catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openReply() { replyForm.value = {}; replyVis.value = true }
async function doReply() {
  try { await axios.post(`${api}/${cur.value.id}/reply`, replyForm.value); replyVis.value=false; ElMessage.success('已发起'); openDetail(cur.value) }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openIssue(row) { replyCur.value = row; issueForm.value = { replyOpinion:'无影响', recheckResult:'' }; issueVis.value = true }
async function doIssue() {
  try {
    await axios.post(`${api}/reply/${replyCur.value.id}/issue`, { ...issueForm.value, approveBy:'R06' })
    issueVis.value=false; ElMessage.success('意见已出具'); openDetail(cur.value); load()
  } catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function appointCheck(row) {
  try {
    const r = await axios.get(`${api}/${row.id}/appointment-check`)
    ElMessage.success(`任用把关通过：${r.data.data.personName} 可进入任用流程`)
  } catch(e) { ElMessage.error(e.response?.data?.message || '任用把关阻断') }
}
onMounted(() => { load(); loadStat() })
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.mb { margin-bottom: 12px; }
.mt { margin-top: 12px; }
.kpi { text-align: center; }
.kpi .n { font-size: 24px; font-weight: 700; color: #1a56db; }
.kpi .l { font-size: 12px; color: #666; margin-top: 2px; }
.kpi .n.red { color: #c45656; }
.kpi .n.orange { color: #e6a23c; }
.hint { color: #999; font-size: 12px; }
.detail { padding: 0 12px; }
</style>

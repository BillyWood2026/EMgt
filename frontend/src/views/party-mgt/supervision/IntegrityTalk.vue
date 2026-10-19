<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">计划→审批(R06)→实施→回访(+60日 E-SV-14)→销号 · 任前谈话未完成阻断任用(E-SV-13) / 编号 TH+年+3位</el-tag>
    </RoleBar>

    <el-row :gutter="10" class="mb">
      <el-col :span="6"><el-card shadow="hover" class="kpi"><div class="n">{{ stat.total||0 }}</div><div class="l">谈话总数</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="kpi"><div class="n" :class="{orange: stat.pendingVisit>0}">{{ stat.pendingVisit||0 }}</div><div class="l">待回访</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="kpi"><div class="n" :class="{red: stat.visitOverdue>0}">{{ stat.visitOverdue||0 }}</div><div class="l">回访超期</div></el-card></el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="kpi">
          <div class="l" v-for="(v,k) in (stat.byType||{})" :key="k">{{ k }} {{ v }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="talkType" placeholder="谈话类型" clearable size="small" style="width:140px" @change="load">
          <el-option v-for="t in types" :key="t" :label="t" :value="t" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="状态" clearable size="small" style="width:110px" @change="load">
          <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
        </el-select>
        <el-input v-model="keyword" placeholder="编号/对象/谈话人" size="small" clearable style="width:170px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R23','R06')" size="small" type="primary" @click="openPlan">谈话计划</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="430">
        <el-table-column prop="talkNo" label="编号" width="100" />
        <el-table-column prop="talkType" label="类型" width="110">
          <template #default="{row}">
            <el-tag size="small" :type="row.preAppointment===1?'danger':''">{{ row.talkType }}</el-tag>
            <el-tag v-if="row.preAppointment===1 && row.appointmentDone!==1 && row.status!=='已完成' && row.status!=='已销号'" size="small" type="danger" effect="plain">任用阻断</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="intervieweeName" label="谈话对象" width="100" />
        <el-table-column prop="talker" label="谈话人" width="90" />
        <el-table-column prop="planDate" label="计划日期" width="95" />
        <el-table-column prop="talkDate" label="实施日期" width="95" />
        <el-table-column label="状态/跟踪" width="150">
          <template #default="{row}">
            <el-tag size="small" :type="row.status==='已销号'||row.status==='已完成'?'success':'warning'">{{ row.status }}</el-tag>
            <el-tag v-if="row.trackStatus && row.trackStatus!=='已销号'" size="small" :type="visitTag(row)">{{ row.trackStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="visitDeadline" label="回访期限" width="95" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('R06') && row.status==='待审批'" size="small" type="primary" @click="approve(row,true)">审批</el-button>
            <el-button v-if="can('R23','R06') && row.status==='已计划'" size="small" @click="openComplete(row)">实施记录</el-button>
            <el-button v-if="can('R23','R06') && row.status==='回访中'" size="small" type="warning" @click="openVisit(row)">回访</el-button>
            <el-button v-if="can('R06') && row.status==='已完成'" size="small" type="success" @click="close(row)">销号</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="planVis" title="廉政谈话计划" width="480px">
      <el-form :model="form" label-width="90px" size="small">
        <el-form-item label="谈话类型"><el-select v-model="form.talkType" style="width:100%"><el-option v-for="t in types" :key="t" :label="t" :value="t" /></el-select></el-form-item>
        <el-form-item label="谈话对象"><el-input v-model="form.intervieweeName" /></el-form-item>
        <el-form-item label="谈话人"><el-input v-model="form.talker" /></el-form-item>
        <el-form-item label="所在单位"><el-input v-model="form.orgName" /></el-form-item>
        <el-form-item label="计划时间"><el-date-picker v-model="form.planDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="planVis=false">取消</el-button><el-button size="small" type="primary" @click="doPlan">提交审批</el-button></template>
    </el-dialog>

    <el-dialog v-model="compVis" title="谈话实施记录" width="500px">
      <el-form :model="compForm" label-width="90px" size="small">
        <el-form-item label="谈话要点"><el-input v-model="compForm.keypoints" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="对象表态"><el-input v-model="compForm.attitude" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="记录人"><el-input v-model="compForm.recorder" /></el-form-item>
        <el-form-item label="签字件编号"><el-input v-model="compForm.attachment" /></el-form-item>
        <el-form-item label="需要整改"><el-switch v-model="compForm.needRectify" :active-value="1" :inactive-value="0" /><span class="hint">需整改则回访期限+60日</span></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="compVis=false">取消</el-button><el-button size="small" type="primary" @click="doComplete">提交</el-button></template>
    </el-dialog>

    <el-dialog v-model="visitVis" title="回访跟踪（超60日告警 E-SV-14）" width="440px">
      <el-form :model="visitForm" label-width="80px" size="small">
        <el-form-item label="回访情况"><el-input v-model="visitForm.visitNote" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="visitVis=false">取消</el-button><el-button size="small" type="warning" @click="doVisit">回访完成</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/supervision/talk'
const types = ['任前廉政谈话','提醒谈话','诫勉谈话','日常谈心谈话']
const statuses = ['待审批','已计划','回访中','已完成','已销号']

const rows = ref([]); const loading = ref(false)
const talkType = ref(''); const statusFilter = ref(''); const keyword = ref('')
const stat = ref({})
const planVis = ref(false); const form = ref({})
const compVis = ref(false); const compForm = ref({}); const cur = ref(null)
const visitVis = ref(false); const visitForm = ref({})

function visitTag(row) {
  if (row.trackStatus==='已回访') return 'success'
  if (row.visitDeadline && new Date(row.visitDeadline) < new Date()) return 'danger'
  return 'warning'
}
async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { talkType: talkType.value||undefined, status: statusFilter.value||undefined, keyword: keyword.value||undefined } })
    rows.value = res.data.data || []
  } finally { loading.value = false }
}
async function loadStat() { try { stat.value = (await axios.get(`${api}/stats`)).data.data || {} } catch {} }
function openPlan() { form.value = { talkType:'提醒谈话', planDate:new Date().toISOString().slice(0,10) }; planVis.value = true }
async function doPlan() {
  try { await axios.post(api, form.value); planVis.value=false; ElMessage.success('计划已提交审批'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function approve(row, pass) {
  try { await axios.post(`${api}/${row.id}/approve?pass=${pass}`); ElMessage.success(pass?'审批通过':'已驳回'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openComplete(row) { cur.value=row; compForm.value={ keypoints:'', attitude:'', recorder:'', attachment:'', needRectify:0 }; compVis.value=true }
async function doComplete() {
  try { await axios.post(`${api}/${cur.value.id}/complete`, compForm.value); compVis.value=false; ElMessage.success('实施记录已提交'); load(); loadStat() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openVisit(row) { cur.value=row; visitForm.value={ visitNote:'' }; visitVis.value=true }
async function doVisit() {
  try { await axios.post(`${api}/${cur.value.id}/visit`, visitForm.value); visitVis.value=false; ElMessage.success('回访完成'); load(); loadStat() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function close(row) {
  try { await axios.post(`${api}/${row.id}/close`); ElMessage.success('已销号'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
onMounted(() => { load(); loadStat() })
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.mb { margin-bottom: 12px; }
.kpi { text-align: center; min-height: 78px; }
.kpi .n { font-size: 24px; font-weight: 700; color: #1a56db; }
.kpi .l { font-size: 12px; color: #666; margin-top: 2px; }
.kpi .n.red { color: #c45656; }
.kpi .n.orange { color: #e6a23c; }
.hint { color: #999; font-size: 12px; margin-left: 8px; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">监督中→整改中(默认30日,前7日橙/超期红)→复核→销号 / 编号 JD+年月+4位</el-tag>
    </RoleBar>

    <el-row :gutter="10" class="mb">
      <el-col :span="4"><el-card shadow="hover" class="kpi"><div class="n">{{ stat.total||0 }}</div><div class="l">本年监督</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover" class="kpi"><div class="n">{{ stat.problems||0 }}</div><div class="l">发现问题</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover" class="kpi"><div class="n">{{ stat.closed||0 }}</div><div class="l">已销号</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover" class="kpi"><div class="n" :class="{red: stat.overdue>0}">{{ stat.overdue||0 }}</div><div class="l">超期</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover" class="kpi"><div class="n" :class="{orange: stat.repeat>0}">{{ stat.repeat||0 }}</div><div class="l">屡查屡犯</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover" class="kpi"><div class="n">{{ stat.closeRate||0 }}%</div><div class="l">销号率</div></el-card></el-col>
    </el-row>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="status" placeholder="状态" clearable size="small" style="width:120px" @change="load">
          <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
        </el-select>
        <el-input v-model="targetOrg" placeholder="被监督单位" size="small" clearable style="width:150px" @keyup.enter="load" />
        <el-input v-model="keyword" placeholder="编号/事项/对象/问题" size="small" clearable style="width:180px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R23','R06','R02')" size="small" type="primary" @click="openCreate">新建监督</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="430">
        <el-table-column prop="superviseNo" label="编号" width="120" />
        <el-table-column prop="title" label="监督事项" min-width="150" show-overflow-tooltip />
        <el-table-column prop="targetOrg" label="对象单位" width="120" show-overflow-tooltip />
        <el-table-column prop="superviseMethod" label="方式" width="80" />
        <el-table-column prop="problemNature" label="问题性质" width="100" show-overflow-tooltip />
        <el-table-column prop="rectifyOwner" label="整改责任人" width="100" />
        <el-table-column prop="rectifyDeadline" label="整改期限" width="100" />
        <el-table-column label="催办" width="60"><template #default="{row}">{{ row.urgeCount||0 }}</template></el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{row}">
            <el-tag size="small" :type="tagType(row)">{{ row.status }}</el-tag>
            <el-tag v-if="row.repeatFlag===1" size="small" type="warning" effect="plain">屡查屡犯</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('R23','R06') && row.status==='监督中'" size="small" @click="openProblem(row)">问题登记</el-button>
            <el-button v-if="can('R07','R23','R06') && (row.status==='整改中'||row.status==='超期')" size="small" @click="openFeedback(row)">整改反馈</el-button>
            <el-button v-if="can('R06','R23') && (row.status==='复核中'||row.status==='超期')" size="small" type="success" @click="openClose(row)">销号</el-button>
            <el-button v-if="can('R23','R06') && row.status!=='已销号'" size="small" @click="urge(row)">催办</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="createVis" title="新建监督事项" width="480px">
      <el-form :model="form" label-width="90px" size="small">
        <el-form-item label="监督事项"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="监督类型"><el-select v-model="form.supervisionType" style="width:100%"><el-option v-for="t in types" :key="t" :label="t" :value="t" /></el-select></el-form-item>
        <el-form-item label="计划时间"><el-date-picker v-model="form.superviseDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="对象单位"><el-input v-model="form.targetOrg" /></el-form-item>
        <el-form-item label="对象人员"><el-input v-model="form.targetPerson" /></el-form-item>
        <el-form-item label="监督方式"><el-input v-model="form.superviseMethod" placeholder="列席会议/专项检查/谈心谈话" /></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="createVis=false">取消</el-button><el-button size="small" type="primary" @click="doCreate">提交</el-button></template>
    </el-dialog>

    <el-dialog v-model="probVis" title="问题登记（整改期限默认30日）" width="500px">
      <el-form :model="probForm" label-width="90px" size="small">
        <el-form-item label="问题描述"><el-input v-model="probForm.finding" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="问题性质"><el-input v-model="probForm.problemNature" placeholder="同对象同性质将标记屡查屡犯" /></el-form-item>
        <el-form-item label="整改责任人"><el-input v-model="probForm.rectifyOwner" /></el-form-item>
        <el-form-item label="整改措施"><el-input v-model="probForm.rectifyMeasure" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="整改天数"><el-input-number v-model="probForm.deadlineDays" :min="1" :max="180" /> <span class="hint">默认30日</span></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="probVis=false">取消</el-button><el-button size="small" type="primary" @click="doProblem">提交</el-button></template>
    </el-dialog>

    <el-dialog v-model="fbVis" title="整改反馈（R07 被监督单位可反馈）" width="460px">
      <el-form :model="fbForm" label-width="90px" size="small">
        <el-form-item label="整改情况"><el-input v-model="fbForm.rectifyFeedback" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="佐证材料"><el-input v-model="fbForm.evidence" type="textarea" :rows="2" placeholder="销号必备：凭证/照片/文件编号" /></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="fbVis=false">取消</el-button><el-button size="small" type="primary" @click="doFeedback">提交</el-button></template>
    </el-dialog>

    <el-dialog v-model="closeVis" title="复核销号（复核人≠整改责任人）" width="460px">
      <el-form :model="closeForm" label-width="90px" size="small">
        <el-form-item label="复核人"><el-input v-model="closeForm.reviewer" /></el-form-item>
        <el-form-item label="复核意见"><el-input v-model="closeForm.reviewOpinion" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="closeVis=false">取消</el-button><el-button size="small" type="success" @click="doClose">确认销号</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/supervision/daily'
const statuses = ['监督中','整改中','复核中','已销号','超期']
const types = ['日常监督','专项监督','节点监督','巡察整改监督','其他']

const rows = ref([]); const loading = ref(false)
const status = ref(''); const targetOrg = ref(''); const keyword = ref('')
const stat = ref({})
const createVis = ref(false); const form = ref({})
const probVis = ref(false); const probForm = ref({}); const cur = ref(null)
const fbVis = ref(false); const fbForm = ref({})
const closeVis = ref(false); const closeForm = ref({})

function tagType(row) {
  if (row.status === '已销号') return 'success'
  if (row.status === '超期') return 'danger'
  if (row.status === '复核中') return 'warning'
  if (row.rectifyDeadline) {
    const days = (new Date(row.rectifyDeadline) - new Date()) / 86400000
    if (days < 0) return 'danger'
    if (days <= 10) return 'warning'
  }
  return 'info'
}
async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { status: status.value||undefined, targetOrg: targetOrg.value||undefined, keyword: keyword.value||undefined } })
    rows.value = res.data.data || []
  } finally { loading.value = false }
}
async function loadStat() { try { const r = await axios.get(`${api}/stats`); stat.value = r.data.data || {} } catch {} }
function openCreate() { form.value = { supervisionType:'日常监督', superviseDate:new Date().toISOString().slice(0,10) }; createVis.value = true }
async function doCreate() {
  try { await axios.post(api, form.value); createVis.value=false; ElMessage.success('已新建'); load(); loadStat() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openProblem(row) { cur.value=row; probForm.value={ deadlineDays:30 }; probVis.value=true }
async function doProblem() {
  try { await axios.post(`${api}/${cur.value.id}/problem`, probForm.value); probVis.value=false; ElMessage.success('问题已登记'); load(); loadStat() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openFeedback(row) { cur.value=row; fbForm.value={}; fbVis.value=true }
async function doFeedback() {
  try { await axios.post(`${api}/${cur.value.id}/feedback`, fbForm.value); fbVis.value=false; ElMessage.success('反馈已提交'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openClose(row) { cur.value=row; closeForm.value={ reviewer:'' }; closeVis.value=true }
async function doClose() {
  try { await axios.post(`${api}/${cur.value.id}/close`, closeForm.value); closeVis.value=false; ElMessage.success('已销号'); load(); loadStat() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function urge(row) {
  try { await axios.post(`${api}/${row.id}/urge`); ElMessage.success('已催办'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
onMounted(() => { load(); loadStat() })
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.mb { margin-bottom: 12px; }
.kpi { text-align: center; }
.kpi .n { font-size: 24px; font-weight: 700; color: #1a56db; }
.kpi .l { font-size: 12px; color: #666; margin-top: 2px; }
.kpi .n.red { color: #c45656; }
.kpi .n.orange { color: #e6a23c; }
.hint { color: #999; font-size: 12px; margin-left: 8px; }
</style>

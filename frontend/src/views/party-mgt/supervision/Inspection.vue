<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">R24巡察专员经办 · 问题清单锁定后反馈(整改≤3月) · 销号须佐证+复核人(E-SV-15/16) · 线索移交13章 / 编号 XC+年+2位</el-tag>
    </RoleBar>

    <el-row :gutter="10" class="mb">
      <el-col :span="4"><el-card shadow="hover" class="kpi"><div class="n">{{ stat.total||0 }}</div><div class="l">巡察对象({{ stat.year }})</div></el-card></el-col>
      <el-col :span="5"><el-card shadow="hover" class="kpi"><div class="n">{{ stat.issues||0 }}</div><div class="l">发现问题</div></el-card></el-col>
      <el-col :span="5"><el-card shadow="hover" class="kpi"><div class="n">{{ stat.closed||0 }}</div><div class="l">已销号问题</div></el-card></el-col>
      <el-col :span="5"><el-card shadow="hover" class="kpi"><div class="n">{{ stat.finishRate||0 }}%</div><div class="l">整改完成率</div></el-card></el-col>
      <el-col :span="5"><el-card shadow="hover" class="kpi"><div class="n" :class="{red: stat.overdue>0}">{{ stat.transferred||0 }} / {{ stat.overdue||0 }}</div><div class="l">线索移交 / 超期</div></el-card></el-col>
    </el-row>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="status" placeholder="状态" clearable size="small" style="width:110px" @change="load">
          <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
        </el-select>
        <el-input v-model="keyword" placeholder="编号/轮次/对象" size="small" clearable style="width:180px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R24','R06','R02')" size="small" type="primary" @click="openCreate">新建巡察</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="430">
        <el-table-column prop="inspectNo" label="编号" width="95" />
        <el-table-column prop="roundNo" label="轮次" width="120" show-overflow-tooltip />
        <el-table-column prop="inspectedOrg" label="巡察对象" min-width="130" show-overflow-tooltip />
        <el-table-column prop="team" label="巡察组" width="100" />
        <el-table-column label="清单" width="80">
          <template #default="{row}"><el-tag size="small" :type="row.issueLocked===1?'success':'info'">{{ row.issueLocked===1?'已锁定':'未锁定' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="问题销号" width="90"><template #default="{row}">{{ row.closedCount||0 }}/{{ row.issueCount||0 }}</template></el-table-column>
        <el-table-column label="完成率" width="130">
          <template #default="{row}"><el-progress :percentage="Number(row.finishRate||0)" :stroke-width="10" :status="row.finishRate>=100?'success':''" /></template>
        </el-table-column>
        <el-table-column prop="clueTransferCount" label="线索移交" width="70" />
        <el-table-column prop="rectifyDeadline" label="整改期限" width="95" />
        <el-table-column label="状态" width="80">
          <template #default="{row}"><el-tag size="small" :type="row.status==='已销号'?'success':row.status==='超期'?'danger':'warning'">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="openIssues(row)">问题清单</el-button>
            <el-button v-if="can('R24','R06') && row.issueLocked!==1" size="small" type="warning" @click="openLock(row)">锁定反馈</el-button>
            <el-button v-if="can('R06') && row.issueLocked===1 && row.status!=='已销号'" size="small" type="success" @click="closeAll(row)">整体销号</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="createVis" title="新建巡察计划" width="460px">
      <el-form :model="form" label-width="80px" size="small">
        <el-form-item label="巡察对象"><el-input v-model="form.inspectedOrg" /></el-form-item>
        <el-form-item label="轮次"><el-input v-model="form.roundNo" placeholder="如：2026第一轮" /></el-form-item>
        <el-form-item label="巡察名称"><el-input v-model="form.inspectionName" /></el-form-item>
        <el-form-item label="巡察组"><el-input v-model="form.team" /></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="createVis=false">取消</el-button><el-button size="small" type="primary" @click="doCreate">提交</el-button></template>
    </el-dialog>

    <el-drawer v-model="issueVis" size="720px" :title="`问题清单 ${cur?.inspectNo||''} ${cur?.inspectedOrg||''}`">
      <div v-if="cur" class="detail">
        <div class="bar">
          <el-button v-if="can('R24','R06') && cur.issueLocked!==1" size="small" type="primary" @click="addIssueRow">登记问题</el-button>
          <el-button v-if="can('R24','R06')" size="small" @click="transferClue">线索移交</el-button>
          <el-tag v-if="cur.issueLocked===1" size="small" type="success">清单已锁定（不可新增，销号须佐证+复核人）</el-tag>
          <el-tag v-else size="small" type="info">未锁定：可登记问题，锁定后才能销号</el-tag>
        </div>
        <el-table :data="issues" size="small" border max-height="460">
          <el-table-column type="index" label="#" width="40" />
          <el-table-column prop="issueDesc" label="问题描述" min-width="180" show-overflow-tooltip />
          <el-table-column prop="category" label="类别" width="80" />
          <el-table-column prop="owner" label="责任人" width="90" />
          <el-table-column prop="measure" label="整改措施" min-width="120" show-overflow-tooltip />
          <el-table-column label="状态" width="75">
            <template #default="{row}"><el-tag size="small" :type="row.status==='已销号'?'success':'warning'">{{ row.status }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{row}"><el-button v-if="can('R24','R06') && row.status!=='已销号'" size="small" type="success" @click="openCloseIssue(row)">销号</el-button></template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>

    <el-dialog v-model="issueFormVis" title="巡察发现问题登记" width="460px">
      <el-form :model="issueForm" label-width="80px" size="small">
        <el-form-item label="问题描述"><el-input v-model="issueForm.issueDesc" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="类别"><el-input v-model="issueForm.category" /></el-form-item>
        <el-form-item label="责任人"><el-input v-model="issueForm.owner" /></el-form-item>
        <el-form-item label="整改措施"><el-input v-model="issueForm.measure" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="issueFormVis=false">取消</el-button><el-button size="small" type="primary" @click="doAddIssue">提交</el-button></template>
    </el-dialog>

    <el-dialog v-model="lockVis" title="锁定问题清单并反馈（整改期≤3个月）" width="460px">
      <el-form :model="lockForm" label-width="90px" size="small">
        <el-form-item label="反馈意见"><el-input v-model="lockForm.feedbackIssue" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="整改月数"><el-input-number v-model="lockForm.deadlineMonths" :min="1" :max="3" /><span class="hint">最长3个月</span></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="lockVis=false">取消</el-button><el-button size="small" type="warning" @click="doLock">锁定并反馈</el-button></template>
    </el-dialog>

    <el-dialog v-model="closeIssueVis" title="问题销号（佐证+复核人双条件）" width="440px">
      <el-form :model="closeIssueForm" label-width="80px" size="small">
        <el-form-item label="佐证材料"><el-input v-model="closeIssueForm.evidence" type="textarea" :rows="2" placeholder="必填" /></el-form-item>
        <el-form-item label="复核人"><el-input v-model="closeIssueForm.reviewer" placeholder="必填" /></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="closeIssueVis=false">取消</el-button><el-button size="small" type="success" @click="doCloseIssue">确认销号</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/supervision/inspection'
const statuses = ['整改中','超期','已销号']

const rows = ref([]); const loading = ref(false)
const status = ref(''); const keyword = ref('')
const stat = ref({})
const createVis = ref(false); const form = ref({})
const issueVis = ref(false); const cur = ref(null); const issues = ref([])
const issueFormVis = ref(false); const issueForm = ref({})
const lockVis = ref(false); const lockForm = ref({})
const closeIssueVis = ref(false); const closeIssueForm = ref({}); const issueCur = ref(null)

async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { status: status.value||undefined, keyword: keyword.value||undefined } })
    rows.value = res.data.data || []
  } finally { loading.value = false }
}
async function loadStat() { try { stat.value = (await axios.get(`${api}/stats`)).data.data || {} } catch {} }
function openCreate() { form.value = {}; createVis.value = true }
async function doCreate() {
  try { await axios.post(api, form.value); createVis.value=false; ElMessage.success('已创建'); load(); loadStat() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function openIssues(row) {
  cur.value = row; issueVis.value = true
  try { issues.value = (await axios.get(`${api}/${row.id}/issues`)).data.data || [] } catch {}
}
function addIssueRow() { issueForm.value = {}; issueFormVis.value = true }
async function doAddIssue() {
  try {
    await axios.post(`${api}/${cur.value.id}/issue`, issueForm.value)
    issueFormVis.value=false; ElMessage.success('问题已登记'); openIssues(cur.value); load()
  } catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openLock(row) { cur.value = row; lockForm.value = { feedbackIssue:'', deadlineMonths:3 }; lockVis.value = true }
async function doLock() {
  try {
    await axios.post(`${api}/${cur.value.id}/lock-feedback`, lockForm.value)
    lockVis.value=false; ElMessage.success('清单已锁定并反馈'); openIssues(cur.value); load()
  } catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openCloseIssue(row) {
  issueCur.value = row; closeIssueForm.value = { evidence:'', reviewer:'' }; closeIssueVis.value = true
}
async function doCloseIssue() {
  try {
    await axios.post(`${api}/issue/${issueCur.value.id}/close`, closeIssueForm.value)
    closeIssueVis.value=false; ElMessage.success('问题已销号'); openIssues(cur.value); load(); loadStat()
  } catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function transferClue() {
  try {
    const { value } = await ElMessageBox.prompt('移交问题线索数量（转第13章 R20/R21 办理）','线索移交',{ inputValue:'1', confirmButtonText:'移交' })
    await axios.post(`${api}/${cur.value.id}/transfer-clue`, { count: Number(value)||1 })
    ElMessage.success('已登记移交'); openIssues(cur.value); load(); loadStat()
  } catch(e) { if (e!=='cancel') ElMessage.error(e.response?.data?.message || e.message) }
}
async function closeAll(row) {
  try {
    await ElMessageBox.confirm('确认全部问题已销号，整体销号该巡察记录？','整体销号',{ type:'warning' })
    await axios.post(`${api}/${row.id}/close`, { reviewer:'R06' })
    ElMessage.success('整体销号完成'); load(); loadStat()
  } catch(e) { if (e!=='cancel') ElMessage.error(e.response?.data?.message || e.message) }
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
.hint { color: #999; font-size: 12px; margin-left: 8px; }
.detail { padding: 0 12px; }
</style>

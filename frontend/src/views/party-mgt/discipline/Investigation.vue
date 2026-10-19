<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">初步核实→立案审查→谈话取证→审查报告→移送审理 / 移送后只读</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-select v-model="stage" placeholder="阶段" clearable size="small" style="width:130px" @change="load">
          <el-option label="初步核实" value="初步核实" />
          <el-option label="立案审查" value="立案审查" />
          <el-option label="移送审理" value="移送审理" />
          <el-option label="已审结" value="已审结" />
        </el-select>
        <el-input v-model="keyword" placeholder="案号/案件名称/被审查人" size="small" clearable style="width:200px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R21','R20','R06')" size="small" type="primary" @click="openCreate">新建案件</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="440">
        <el-table-column prop="caseNo" label="案号" width="110" />
        <el-table-column prop="caseName" label="案件名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="targetName" label="被审查人" width="90" />
        <el-table-column prop="stage" label="阶段" width="90">
          <template #default="{row}">
            <el-tag size="small" :type="stageType(row.stage)">{{ row.stage }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileDate" label="立案日期" width="100" />
        <el-table-column prop="deadline" label="审查时限" width="100" />
        <el-table-column prop="extendMonths" label="已延期" width="80" />
        <el-table-column label="操作" width="310" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('R06','R20') && row.stage==='初步核实'" size="small" @click="openApprove(row)">立案审批</el-button>
            <el-button v-if="can('R21','R20') && row.stage!=='移送审理' && row.stage!=='已审结' && !row.locked" size="small" @click="openTalk(row)">谈话</el-button>
            <el-button v-if="can('R21','R20') && row.stage!=='移送审理' && row.stage!=='已审结' && !row.locked" size="small" @click="openReport(row)">报告</el-button>
            <el-button v-if="can('R06','R20') && row.stage!=='移送审理' && row.stage!=='已审结' && !row.locked" size="small" @click="openExtend(row)">延期</el-button>
            <el-button v-if="can('R06','R20') && row.stage==='立案审查' && !row.locked" size="small" type="warning" @click="transfer(row)">移送审理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建 -->
    <el-dialog v-model="createVis" title="新建案件" width="520px">
      <el-form :model="form" label-width="80px" size="small">
        <el-form-item label="案件名称"><el-input v-model="form.caseName" /></el-form-item>
        <el-form-item label="被审查人"><el-input v-model="form.targetName" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="form.targetOrg" /></el-form-item>
        <el-form-item label="来源"><el-select v-model="form.source" style="width:100%"><el-option label="初核转立案" value="初核转立案" /><el-option label="巡察移交" value="巡察移交" /><el-option label="审计移送" value="审计移送" /><el-option label="信访举报" value="信访举报" /></el-select></el-form-item>
        <el-form-item label="承办部门"><el-input v-model="form.dept" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="createVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doCreate">提交</el-button>
      </template>
    </el-dialog>

    <!-- 立案审批 -->
    <el-dialog v-model="apVis" title="立案审批" width="440px">
      <el-form :model="apForm" label-width="80px" size="small">
        <el-form-item label="依据条款"><el-input v-model="apForm.fileBasis" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="审批人"><el-input v-model="apForm.approveBy" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="apVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doApprove">提交</el-button>
      </template>
    </el-dialog>

    <!-- 谈话 -->
    <el-dialog v-model="talkVis" title="谈话记录" width="520px">
      <el-form :model="talkForm" label-width="80px" size="small">
        <el-form-item label="谈话对象"><el-input v-model="talkForm.targetName" /></el-form-item>
        <el-form-item label="地点"><el-input v-model="talkForm.place" /></el-form-item>
        <el-form-item label="笔录编号"><el-input v-model="talkForm.talkNo" /></el-form-item>
        <el-form-item label="录像编号"><el-input v-model="talkForm.videoNo" /></el-form-item>
        <el-form-item label="笔录正文"><el-input v-model="talkForm.note" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="记录人"><el-input v-model="talkForm.recorder" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="talkVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doTalk">提交</el-button>
      </template>
    </el-dialog>

    <!-- 审查报告 -->
    <el-dialog v-model="repVis" title="审查报告" width="560px">
      <el-form :model="repForm" label-width="80px" size="small">
        <el-form-item label="审查报告"><el-input v-model="repForm.reviewReport" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="财物清单"><el-input v-model="repForm.propertyList" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="违纪事实"><el-input v-model="repForm.facts" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="处理建议"><el-input v-model="repForm.result" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="repVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doReport">提交</el-button>
      </template>
    </el-dialog>

    <!-- 延期 -->
    <el-dialog v-model="extVis" title="审查延期" width="380px">
      <el-form label-width="80px" size="small">
        <el-form-item label="延长月数"><el-input-number v-model="extMonths" :min="1" :max="6" /></el-form-item>
        <el-form-item label="理由"><el-input v-model="extReason" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="extVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doExtend">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/discipline/investigation'

const stage = ref('')
const keyword = ref('')
const rows = ref([])
const loading = ref(false)

const createVis = ref(false); const form = ref({})
const apVis = ref(false); const apForm = ref({})
const talkVis = ref(false); const talkForm = ref({})
const repVis = ref(false); const repForm = ref({})
const extVis = ref(false); const extMonths = ref(1); const extReason = ref('')
const cur = ref(null)

function stageType(s) {
  if (s==='已审结') return 'success'
  if (s==='移送审理') return 'info'
  return 'warning'
}
async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { stage: stage.value, keyword: keyword.value||undefined } })
    rows.value = res.data.data || []
  } finally { loading.value = false }
}
function openCreate() { form.value = { source:'初核转立案' }; createVis.value = true }
async function doCreate() {
  try { await axios.post(api, form.value); createVis.value=false; ElMessage.success('创建成功'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openApprove(row) { cur.value = row; apForm.value = {}; apVis.value = true }
async function doApprove() {
  try { await axios.post(`${api}/${cur.value.id}/approve-file`, apForm.value); apVis.value=false; ElMessage.success('立案审批完成'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openTalk(row) { cur.value = row; talkForm.value = { targetName: row.targetName }; talkVis.value = true }
async function doTalk() {
  try { await axios.post(`${api}/${cur.value.id}/talks`, talkForm.value); talkVis.value=false; ElMessage.success('谈话记录已保存'); }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openReport(row) { cur.value = row; repForm.value = { reviewReport: row.reviewReport, propertyList: row.propertyList, facts: row.facts, result: row.result }; repVis.value = true }
async function doReport() {
  try { await axios.post(`${api}/${cur.value.id}/report`, repForm.value); repVis.value=false; ElMessage.success('报告已提交'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openExtend(row) { cur.value = row; extMonths.value = 1; extReason.value = ''; extVis.value = true }
async function doExtend() {
  try { await axios.post(`${api}/${cur.value.id}/extend`, { months: extMonths.value, reason: extReason.value }); extVis.value=false; ElMessage.success('延期成功'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function transfer(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入处分依据条款', '移送审理', { inputPlaceholder: '如：《中国共产党纪律处分条例》第X条' })
    await axios.post(`${api}/${row.id}/transfer`, { sanctionBasis: value })
    ElMessage.success('已移送审理'); load()
  } catch(e) { if(e!=='cancel') ElMessage.error(e.response?.data?.message || e.message) }
}
onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.mb { margin-bottom: 12px; }
</style>

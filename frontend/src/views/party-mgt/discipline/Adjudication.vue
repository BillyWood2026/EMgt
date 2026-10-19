<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">审理→处分决定→宣布→执行→以案促改→销号 / 影响期自动计算</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-select v-model="status" placeholder="状态" clearable size="small" style="width:120px" @change="load">
          <el-option label="待审理" value="待审理" />
          <el-option label="审理中" value="审理中" />
          <el-option label="已决定" value="已决定" />
          <el-option label="已宣布" value="已宣布" />
          <el-option label="执行中" value="执行中" />
          <el-option label="以案促改中" value="以案促改中" />
          <el-option label="已结案" value="已结案" />
        </el-select>
        <el-select v-model="punishmentType" placeholder="处分类型" clearable size="small" style="width:130px" @change="load">
          <el-option v-for="t in punishTypes" :key="t" :label="t" :value="t" />
        </el-select>
        <el-input v-model="keyword" placeholder="案号/被处分人/文号" size="small" clearable style="width:180px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R20','R06')" size="small" type="primary" @click="openSuggest">由案件生成教育</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="440">
        <el-table-column prop="caseNo" label="案号" width="110" />
        <el-table-column prop="targetName" label="被处分人" width="90" />
        <el-table-column prop="punishmentType" label="处分类型" width="100" />
        <el-table-column prop="decisionNo" label="决定文号" width="140" />
        <el-table-column prop="influenceStart" label="影响期起" width="100" />
        <el-table-column prop="influenceEnd" label="影响期止" width="100" />
        <el-table-column prop="executeStatus" label="执行" width="80" />
        <el-table-column prop="reformStatus" label="促改" width="80" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('R22') && row.status==='待审理'" size="small" @click="openReview(row)">审理</el-button>
            <el-button v-if="can('R06') && row.status==='审理中'" size="small" @click="openDecide(row)">决定</el-button>
            <el-button v-if="can('R06','R22') && row.status==='已决定'" size="small" @click="announce(row)">宣布</el-button>
            <el-button v-if="can('R22','R06') && row.status==='已宣布'" size="small" @click="openExecute(row)">执行</el-button>
            <el-button v-if="can('R22','R06') && row.status==='执行中'" size="small" @click="openReform(row)">促改</el-button>
            <el-button v-if="can('R06','R22') && row.status==='以案促改中'" size="small" @click="close(row)">销号</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 审理 -->
    <el-dialog v-model="revVis" title="审理" width="520px">
      <el-form :model="revForm" label-width="80px" size="small">
        <el-form-item label="审理报告"><el-input v-model="revForm.reviewReport" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="集体审议"><el-input v-model="revForm.reviewMeeting" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="审理意见"><el-input v-model="revForm.adjudicationOpinion" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="revVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doReview">提交</el-button>
      </template>
    </el-dialog>

    <!-- 处分决定 -->
    <el-dialog v-model="decVis" title="处分决定" width="440px">
      <el-form :model="decForm" label-width="80px" size="small">
        <el-form-item label="处分类型"><el-select v-model="decForm.punishmentType" style="width:100%"><el-option v-for="t in punishTypes" :key="t" :label="t" :value="t" /></el-select></el-form-item>
        <el-form-item label="依据条款"><el-input v-model="decForm.sanctionBasis" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="decVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doDecide">提交</el-button>
      </template>
    </el-dialog>

    <!-- 执行 -->
    <el-dialog v-model="exeVis" title="执行" width="440px">
      <el-form :model="exeForm" label-width="80px" size="small">
        <el-form-item label="执行单位"><el-input v-model="exeForm.executeOrg" /></el-form-item>
        <el-form-item label="执行人"><el-input v-model="exeForm.executor" /></el-form-item>
        <el-form-item label="执行回执"><el-input v-model="exeForm.executeReceipt" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="exeVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doExecute">提交</el-button>
      </template>
    </el-dialog>

    <!-- 以案促改 -->
    <el-dialog v-model="refVis" title="以案促改" width="440px">
      <el-form :model="refForm" label-width="80px" size="small">
        <el-form-item label="整改措施"><el-input v-model="refForm.reformMeasure" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="refVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doReform">提交</el-button>
      </template>
    </el-dialog>

    <!-- 由案件生成教育 -->
    <el-dialog v-model="sugVis" title="由处分案件生成警示教育" width="440px">
      <el-form :model="sugForm" label-width="80px" size="small">
        <el-form-item label="案件ID"><el-input v-model="sugForm.adjudicationId" placeholder="输入已结案处分案件ID" /></el-form-item>
        <el-form-item label="对象范围"><el-input v-model="sugForm.targetScope" placeholder="默认：全体党员" /></el-form-item>
        <el-form-item label="计划日期"><el-input v-model="sugForm.planDate" placeholder="yyyy-MM-dd" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="sugVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doSuggest">生成</el-button>
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

const api = '/api/party/discipline/adjudication'
const eduApi = '/api/party/discipline/education'
const punishTypes = ['警告','严重警告','撤销党内职务','留党察看','开除党籍']

const status = ref('')
const punishmentType = ref('')
const keyword = ref('')
const rows = ref([])
const loading = ref(false)

const revVis = ref(false); const revForm = ref({})
const decVis = ref(false); const decForm = ref({})
const exeVis = ref(false); const exeForm = ref({})
const refVis = ref(false); const refForm = ref({})
const sugVis = ref(false); const sugForm = ref({})
const cur = ref(null)

async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { status: status.value, punishmentType: punishmentType.value, keyword: keyword.value||undefined } })
    rows.value = res.data.data || []
  } finally { loading.value = false }
}
function openReview(row) { cur.value = row; revForm.value = {}; revVis.value = true }
async function doReview() {
  try { await axios.post(`${api}/${cur.value.id}/review`, revForm.value); revVis.value=false; ElMessage.success('审理完成'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openDecide(row) { cur.value = row; decForm.value = {}; decVis.value = true }
async function doDecide() {
  try { await axios.post(`${api}/${cur.value.id}/decide`, decForm.value); decVis.value=false; ElMessage.success('处分决定已作出'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function announce(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入宣布人', '处分宣布', { inputPlaceholder: '宣布人姓名' })
    await axios.post(`${api}/${row.id}/announce`, { announceBy: value })
    ElMessage.success('已宣布'); load()
  } catch(e) { if(e!=='cancel') ElMessage.error(e.response?.data?.message || e.message) }
}
function openExecute(row) { cur.value = row; exeForm.value = {}; exeVis.value = true }
async function doExecute() {
  try { await axios.post(`${api}/${cur.value.id}/execute`, exeForm.value); exeVis.value=false; ElMessage.success('执行完成'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openReform(row) { cur.value = row; refForm.value = {}; refVis.value = true }
async function doReform() {
  try { await axios.post(`${api}/${cur.value.id}/reform`, refForm.value); refVis.value=false; ElMessage.success('以案促改启动'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function close(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入销号佐证材料', '复核销号', { inputPlaceholder: '如：整改报告/照片/文件号' })
    await axios.post(`${api}/${row.id}/close`, { reformEvidence: value, closeBy: '' })
    ElMessage.success('已销号'); load()
  } catch(e) { if(e!=='cancel') ElMessage.error(e.response?.data?.message || e.message) }
}
function openSuggest() { sugForm.value = {}; sugVis.value = true }
async function doSuggest() {
  try { await axios.post(`${eduApi}/suggest-from-case`, sugForm.value); sugVis.value=false; ElMessage.success('已生成警示教育计划'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.mb { margin-bottom: 12px; }
</style>

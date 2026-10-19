<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">登记→研判→处置（谈话函询/初步核实/暂存待查/予以了结）→转立案</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-select v-model="status" placeholder="状态" clearable size="small" style="width:120px" @change="load">
          <el-option label="待研判" value="待研判" />
          <el-option label="函询中" value="函询中" />
          <el-option label="初核中" value="初核中" />
          <el-option label="暂存待查" value="暂存待查" />
          <el-option label="已了结" value="已了结" />
          <el-option label="转立案" value="转立案" />
        </el-select>
        <el-select v-model="disposeMethod" placeholder="处置方式" clearable size="small" style="width:130px" @change="load">
          <el-option v-for="m in methods" :key="m" :label="m" :value="m" />
        </el-select>
        <el-input v-model="keyword" placeholder="编号/被反映人/摘要" size="small" clearable style="width:180px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R20','R06','R02')" size="small" type="primary" @click="openReg">登记</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="440">
        <el-table-column prop="clueNo" label="编号" width="110" />
        <el-table-column prop="targetName" label="被反映人" width="90" />
        <el-table-column prop="targetOrg" label="单位" width="120" />
        <el-table-column prop="source" label="来源" width="90" />
        <el-table-column prop="disposeMethod" label="处置方式" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{row}">
            <el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="disposeDeadline" label="处置时限" width="100" />
        <el-table-column prop="alert" label="预警" width="80">
          <template #default="{row}">
            <el-tag v-if="alertLevel(row)==='红色'" size="small" type="danger">红色</el-tag>
            <el-tag v-else-if="alertLevel(row)==='橙色'" size="small" type="warning">橙色</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('R06','R20') && row.status==='待研判'" size="small" @click="openJudge(row)">研判</el-button>
            <el-button v-if="can('R21','R20') && ['函询中','初核中'].includes(row.status)" size="small" @click="openReport(row)">处置报告</el-button>
            <el-button v-if="can('R06','R20') && ['函询中','初核中','暂存待查'].includes(row.status)" size="small" @click="openExtend(row)">延期</el-button>
            <el-button v-if="can('R06','R20') && ['函询中','初核中'].includes(row.status)" size="small" type="warning" @click="toCase(row)">转立案</el-button>
            <el-button v-if="can('R20','R06') && row.status==='已了结'" size="small" @click="archive(row)">归档</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 登记 -->
    <el-dialog v-model="regVis" title="线索登记" width="520px">
      <el-form :model="form" label-width="80px" size="small">
        <el-form-item label="被反映人"><el-input v-model="form.targetName" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="form.targetOrg" /></el-form-item>
        <el-form-item label="来源"><el-select v-model="form.source" style="width:100%"><el-option label="信访举报" value="信访举报" /><el-option label="巡察移交" value="巡察移交" /><el-option label="审计移送" value="审计移送" /><el-option label="监督检查发现" value="监督检查发现" /></el-select></el-form-item>
        <el-form-item v-if="form.source==='信访举报'" label="信访件ID"><el-input v-model="form.petitionId" placeholder="关联原举报编号ID" /></el-form-item>
        <el-form-item label="问题摘要"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="涉及金额"><el-input v-model="form.amount" placeholder="万元" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="regVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doReg">提交</el-button>
      </template>
    </el-dialog>

    <!-- 研判 -->
    <el-dialog v-model="judgeVis" title="集体研判" width="460px">
      <el-form :model="judgeForm" label-width="80px" size="small">
        <el-form-item label="处置方式"><el-select v-model="judgeForm.disposeMethod" style="width:100%"><el-option v-for="m in methods" :key="m" :label="m" :value="m" /></el-select></el-form-item>
        <el-form-item label="会议纪要"><el-input v-model="judgeForm.judgeMeeting" type="textarea" :rows="2" /></el-form-item>
        <el-form-item v-if="judgeForm.disposeMethod==='予以了结'" label="了结理由"><el-input v-model="judgeForm.closeReason" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="研判人"><el-input v-model="judgeForm.judgeBy" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="judgeVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doJudge">提交</el-button>
      </template>
    </el-dialog>

    <!-- 处置报告 -->
    <el-dialog v-model="repVis" title="处置报告" width="500px">
      <el-input v-model="repText" type="textarea" :rows="5" placeholder="填写处置报告" />
      <template #footer>
        <el-button size="small" @click="repVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doReport">提交</el-button>
      </template>
    </el-dialog>

    <!-- 延期 -->
    <el-dialog v-model="extVis" title="延期" width="380px">
      <el-form label-width="80px" size="small">
        <el-form-item label="延长月数"><el-input-number v-model="extMonths" :min="1" :max="3" /></el-form-item>
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

const api = '/api/party/discipline/clue'
const methods = ['谈话函询','初步核实','暂存待查','予以了结']

const status = ref('')
const disposeMethod = ref('')
const keyword = ref('')
const rows = ref([])
const loading = ref(false)

const regVis = ref(false); const form = ref({})
const judgeVis = ref(false); const judgeForm = ref({})
const repVis = ref(false); const repText = ref('')
const extVis = ref(false); const extMonths = ref(1)
const cur = ref(null)

function statusType(s) {
  if (s==='转立案'||s==='已了结') return 'success'
  if (s==='已归档') return 'info'
  return 'warning'
}
function alertLevel(row) {
  if (!row.disposeDeadline || ['已了结','已归档','转立案'].includes(row.status)) return '正常'
  const today = new Date().toISOString().slice(0,10)
  const deadline = row.disposeDeadline
  if (today > deadline) return '红色'
  const orange = new Date(new Date(deadline).getTime() - 10*86400000).toISOString().slice(0,10)
  if (today > orange) return '橙色'
  return '正常'
}
async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { status: status.value, disposeMethod: disposeMethod.value, keyword: keyword.value||undefined } })
    rows.value = res.data.data || []
  } finally { loading.value = false }
}
function openReg() { form.value = { source:'监督检查发现' }; regVis.value = true }
async function doReg() {
  try { await axios.post(api, form.value); regVis.value=false; ElMessage.success('登记成功'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openJudge(row) { cur.value = row; judgeForm.value = { disposeMethod:'初步核实' }; judgeVis.value = true }
async function doJudge() {
  try { await axios.post(`${api}/${cur.value.id}/judge`, judgeForm.value); judgeVis.value=false; ElMessage.success('研判完成'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openReport(row) { cur.value = row; repText.value = ''; repVis.value = true }
async function doReport() {
  try { await axios.post(`${api}/${cur.value.id}/report`, { disposeReport: repText.value }); repVis.value=false; ElMessage.success('报告已提交'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openExtend(row) { cur.value = row; extMonths.value = 1; extVis.value = true }
async function doExtend() {
  try { await axios.post(`${api}/${cur.value.id}/extend`, { months: extMonths.value }); extVis.value=false; ElMessage.success('延期成功'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function toCase(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入立案依据条款', '转立案', { inputPlaceholder: '如：《中国共产党纪律处分条例》第四十条' })
    await axios.post(`${api}/${row.id}/to-case`, { fileBasis: value })
    ElMessage.success('已转立案'); load()
  } catch(e) { if(e!=='cancel') ElMessage.error(e.response?.data?.message || e.message) }
}
async function archive(row) {
  try {
    await ElMessageBox.confirm('确认归档该线索？','提示',{type:'warning'})
    await axios.post(`${api}/${row.id}/archive`)
    ElMessage.success('归档完成'); load()
  } catch {}
}
onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.mb { margin-bottom: 12px; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">登记→研判→转办→办理→归档 / 编号规则 JB+年月+4位</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-select v-model="status" placeholder="状态" clearable size="small" style="width:130px" @change="load">
          <el-option label="待受理研判" value="待受理研判" />
          <el-option label="已受理" value="已受理" />
          <el-option label="转办中" value="转办中" />
          <el-option label="已办结" value="已办结" />
          <el-option label="已归档" value="已归档" />
        </el-select>
        <el-select v-model="problemType" placeholder="问题类别" clearable size="small" style="width:130px" @change="load">
          <el-option v-for="t in problemTypes" :key="t" :label="t" :value="t" />
        </el-select>
        <el-input v-model="keyword" placeholder="编号/被举报人/内容" size="small" clearable style="width:180px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R23','R02')" size="small" type="primary" @click="openReg">登记</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="420">
        <el-table-column prop="reportNo" label="编号" width="120" />
        <el-table-column prop="reportedName" label="被举报人" width="90" />
        <el-table-column prop="problemType" label="问题类别" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{row}">
            <el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="receiveDate" label="收到日期" width="100" />
        <el-table-column prop="deadline" label="办理时限" width="100" />
        <el-table-column prop="handleResult" label="预警/结果" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('R20','R06') && row.status==='待受理研判'" size="small" @click="openJudge(row)">研判</el-button>
            <el-button v-if="can('R20','R06') && row.status==='已受理'" size="small" @click="openTransfer(row)">转办</el-button>
            <el-button v-if="can('R20','R21') && row.status==='转办中'" size="small" @click="openFeedback(row)">反馈</el-button>
            <el-button v-if="can('R20','R06') && (row.status==='已办结'||row.status==='不予受理'||row.status==='留存备查')" size="small" @click="archive(row)">归档</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" small layout="total,prev,pager,next" class="mt" @current-change="load" />
    </el-card>

    <!-- 登记 -->
    <el-dialog v-model="regVis" title="信访登记" width="520px">
      <el-form :model="form" label-width="80px" size="small">
        <el-form-item label="被举报人"><el-input v-model="form.reportedName" /></el-form-item>
        <el-form-item label="问题类别"><el-select v-model="form.problemType" style="width:100%"><el-option v-for="t in problemTypes" :key="t" :label="t" :value="t" /></el-select></el-form-item>
        <el-form-item label="渠道"><el-select v-model="form.channel" style="width:100%"><el-option label="来信" value="来信" /><el-option label="来访" value="来访" /><el-option label="网络" value="网络" /><el-option label="电话" value="电话" /></el-select></el-form-item>
        <el-form-item label="是否匿名"><el-radio-group v-model="form.isAnonymous"><el-radio value="是">是</el-radio><el-radio value="否">否</el-radio></el-radio-group></el-form-item>
        <el-form-item label="举报人"><el-input v-model="form.reporterName" :disabled="form.isAnonymous==='是'" /></el-form-item>
        <el-form-item label="联系方式"><el-input v-model="form.contact" :disabled="form.isAnonymous==='是'" /></el-form-item>
        <el-form-item label="主要内容"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="regVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doReg">提交</el-button>
      </template>
    </el-dialog>

    <!-- 研判 -->
    <el-dialog v-model="judgeVis" title="受理研判" width="440px">
      <el-form :model="judgeForm" label-width="80px" size="small">
        <el-form-item label="结论"><el-select v-model="judgeForm.conclusion" style="width:100%"><el-option label="予以受理" value="予以受理" /><el-option label="不予受理" value="不予受理" /><el-option label="转办" value="转办" /><el-option label="交办" value="交办" /><el-option label="留存备查" value="留存备查" /></el-select></el-form-item>
        <el-form-item v-if="judgeForm.conclusion==='不予受理'" label="告知理由"><el-input v-model="judgeForm.rejectReason" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="研判人"><el-input v-model="judgeForm.judgeBy" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="judgeVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doJudge">提交</el-button>
      </template>
    </el-dialog>

    <!-- 转办 -->
    <el-dialog v-model="transVis" title="转办" width="400px">
      <el-form :model="transForm" label-width="80px" size="small">
        <el-form-item label="承办单位"><el-select v-model="transForm.transferOrg" style="width:100%"><el-option label="第一党支部纪检组" value="第一党支部纪检组" /><el-option label="第二党支部纪检组" value="第二党支部纪检组" /><el-option label="纪检监察室" value="纪检监察室" /></el-select></el-form-item>
        <el-form-item label="办理时限"><el-input v-model="transForm.deadline" placeholder="yyyy-MM-dd" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="transVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doTransfer">提交</el-button>
      </template>
    </el-dialog>

    <!-- 反馈 -->
    <el-dialog v-model="fbVis" title="办理反馈" width="460px">
      <el-form :model="fbForm" label-width="80px" size="small">
        <el-form-item label="办理结果"><el-input v-model="fbForm.handleResult" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="反馈情况"><el-input v-model="fbForm.feedback" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="fbVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doFeedback">提交</el-button>
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

const api = '/api/party/discipline/petition'
const problemTypes = ['贪污受贿','违规吃喝','形式主义','失职渎职','违反八项规定','其他']

const status = ref('')
const problemType = ref('')
const keyword = ref('')
const rows = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const regVis = ref(false)
const form = ref({})
const judgeVis = ref(false)
const judgeForm = ref({})
const cur = ref(null)
const transVis = ref(false)
const transForm = ref({})
const fbVis = ref(false)
const fbForm = ref({})

function statusType(s) {
  if (s==='已归档') return 'success'; if (s==='超期未办结') return 'danger'
  if (['已办结','不予受理','留存备查'].includes(s)) return 'info'
  return 'warning'
}
async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/page`, { params: { status: status.value, problemType: problemType.value, keyword: keyword.value||undefined, pageNum: pageNum.value, pageSize: pageSize.value } })
    const d = res.data.data
    rows.value = d?.records || []
    total.value = d?.total || 0
  } finally { loading.value = false }
}
function openReg() { form.value = { isAnonymous:'否', channel:'来信' }; regVis.value = true }
async function doReg() {
  try { await axios.post(api, form.value); regVis.value=false; ElMessage.success('登记成功'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openJudge(row) { cur.value = row; judgeForm.value = { conclusion:'予以受理', judgeBy:'' }; judgeVis.value = true }
async function doJudge() {
  try { await axios.post(`${api}/${cur.value.id}/judge`, judgeForm.value); judgeVis.value=false; ElMessage.success('研判完成'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openTransfer(row) { cur.value = row; transForm.value = { transferOrg:'', deadline:'' }; transVis.value = true }
async function doTransfer() {
  try { await axios.post(`${api}/${cur.value.id}/transfer`, transForm.value); transVis.value=false; ElMessage.success('转办完成'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openFeedback(row) { cur.value = row; fbForm.value = { handleResult:'', feedback:'' }; fbVis.value = true }
async function doFeedback() {
  try { await axios.post(`${api}/${cur.value.id}/feedback`, fbForm.value); fbVis.value=false; ElMessage.success('反馈完成'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function archive(row) {
  try {
    await ElMessageBox.confirm('确认归档该信访件？','提示',{type:'warning'})
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
.mt { margin-top: 8px; }
</style>

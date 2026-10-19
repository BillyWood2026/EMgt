<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">部门行为稽核：登记→告知→确认/申诉→整改→验收→生效扣分</el-tag>
    </RoleBar>

    <el-tabs v-model="tab" class="mt">
      <el-tab-pane v-if="can('C02','C03','C99')" label="稽核管理" name="admin">
        <div class="bar">
          <el-select v-model="filter.status" placeholder="状态" clearable size="small" style="width:130px" @change="loadAdmin">
            <el-option label="已登记" value="已登记" />
            <el-option label="已告知" value="已告知" />
            <el-option label="已确认" value="已确认" />
            <el-option label="申诉中" value="申诉中" />
            <el-option label="整改实施中" value="整改实施中" />
            <el-option label="待验收" value="待验收" />
            <el-option label="已生效" value="已生效" />
            <el-option label="申诉驳回已归档" value="申诉驳回已归档" />
          </el-select>
          <el-input v-model="filter.targetUser" placeholder="当事人" clearable size="small" style="width:120px" @change="loadAdmin" />
          <el-input v-model="filter.dept" placeholder="部门" clearable size="small" style="width:140px" @change="loadAdmin" />
          <span style="flex:1"></span>
          <el-button size="small" type="primary" @click="openCreate">登记稽核</el-button>
          <el-button size="small" @click="loadAdmin">刷新</el-button>
        </div>
        <el-table :data="adminRows" size="small" border v-loading="loading" class="mt">
          <el-table-column prop="auditNo" label="编号" width="150" />
          <el-table-column prop="targetUser" label="当事人" width="90" />
          <el-table-column prop="targetDept" label="部门" width="130" show-overflow-tooltip />
          <el-table-column prop="violateDimension" label="违规维度" width="90" align="center" />
          <el-table-column prop="violateScene" label="场景" width="110" show-overflow-tooltip />
          <el-table-column prop="violateDetail" label="违规详情" min-width="180" show-overflow-tooltip />
          <el-table-column prop="deductScore" label="扣分" width="70" align="center" />
          <el-table-column prop="rectifyDeadline" label="整改期限" width="100" align="center" />
          <el-table-column prop="status" label="状态" width="110" align="center">
            <template #default="{row}">
              <el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{row}">
              <el-button v-if="row.status === '待验收'" size="small" type="primary" link @click="openAccept(row)">验收</el-button>
              <el-button v-if="row.status === '申诉中'" size="small" type="warning" link @click="openVerdict(row)">裁决申诉</el-button>
              <el-button size="small" link @click="openDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="我的被稽核" name="mine">
        <div class="bar">
          <el-button size="small" @click="loadMine">刷新</el-button>
        </div>
        <el-table :data="mineRows" size="small" border v-loading="loading" class="mt">
          <el-table-column prop="auditNo" label="编号" width="150" />
          <el-table-column prop="violateDimension" label="违规维度" width="90" align="center" />
          <el-table-column prop="violateScene" label="场景" width="110" show-overflow-tooltip />
          <el-table-column prop="violateDetail" label="违规详情" min-width="180" show-overflow-tooltip />
          <el-table-column prop="deductScore" label="扣分" width="70" align="center" />
          <el-table-column prop="rectifyDeadline" label="整改期限" width="100" align="center" />
          <el-table-column prop="status" label="状态" width="110" align="center">
            <template #default="{row}">
              <el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{row}">
              <el-button v-if="row.status === '已告知'" size="small" type="primary" link @click="doConfirm(row)">确认</el-button>
              <el-button v-if="row.status === '已告知'" size="small" type="warning" link @click="openAppeal(row)">申诉</el-button>
              <el-button v-if="row.status === '已确认' || row.status === '整改实施中'" size="small" type="primary" link @click="openRectify(row)">提交整改</el-button>
              <el-button size="small" link @click="openDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 登记稽核弹窗 -->
    <el-dialog v-model="createVis" title="登记稽核" width="620px" :close-on-click-modal="false">
      <el-form label-width="100px" size="small">
        <el-form-item label="当事人" required>
          <el-input v-model="createRow.targetUser" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="所在部门" required>
          <el-input v-model="createRow.targetDept" placeholder="部门名称" />
        </el-form-item>
        <el-form-item label="违规维度" required>
          <el-select v-model="createRow.violateDimension" style="width:100%">
            <el-option label="客户导向" value="客户导向" />
            <el-option label="奋斗担当" value="奋斗担当" />
            <el-option label="艰苦奋斗" value="艰苦奋斗" />
            <el-option label="自我批判" value="自我批判" />
          </el-select>
        </el-form-item>
        <el-form-item label="违规场景" required>
          <el-input v-model="createRow.violateScene" placeholder="如 客户投诉/项目延期" />
        </el-form-item>
        <el-form-item label="违规详情" required>
          <el-input v-model="createRow.violateDetail" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="不少于10字" />
        </el-form-item>
        <el-form-item label="发生时间" required>
          <el-date-picker v-model="createRow.occurTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" :disabled-date="disabledFuture" />
        </el-form-item>
        <el-form-item label="红线编码">
          <el-input v-model="createRow.redlineCode" placeholder="可选，如 HX2026001" />
        </el-form-item>
        <el-form-item label="扣分" required>
          <el-input-number v-model="createRow.deductScore" :min="0" :max="10" :step="0.5" controls-position="right" />
        </el-form-item>
        <el-form-item label="整改要求" required>
          <el-input v-model="createRow.rectifyRequirement" placeholder="不少于5字" />
        </el-form-item>
        <el-form-item label="整改期限" required>
          <el-date-picker v-model="createRow.rectifyDeadline" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="佐证附件" required>
          <el-input v-model="createRow.attachments" placeholder='JSON数组，如 [{"name":"记录.png","url":"/files/a.png"}]' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="createVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doCreate">登记并告知</el-button>
      </template>
    </el-dialog>

    <!-- 申诉弹窗 -->
    <el-dialog v-model="appealVis" title="申诉" width="500px">
      <el-input v-model="appealReason" type="textarea" :rows="4" placeholder="申诉理由不少于10字" />
      <template #footer>
        <el-button size="small" @click="appealVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="submitting" @click="doAppeal">提交申诉</el-button>
      </template>
    </el-dialog>

    <!-- 裁决申诉弹窗 -->
    <el-dialog v-model="verdictVis" title="申诉裁决" width="500px">
      <el-form label-width="80px" size="small">
        <el-form-item label="裁决结果">
          <el-radio-group v-model="verdictUphold">
            <el-radio :value="true">维持原判</el-radio>
            <el-radio :value="false">申诉成立</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="裁决意见">
          <el-input v-model="verdictOpinion" type="textarea" :rows="3" placeholder="不少于5字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="verdictVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="submitting" @click="doVerdict">确认裁决</el-button>
      </template>
    </el-dialog>

    <!-- 整改材料弹窗 -->
    <el-dialog v-model="rectifyVis" title="提交整改材料" width="520px">
      <el-form label-width="90px" size="small">
        <el-form-item label="整改说明" required>
          <el-input v-model="rectifyNote" type="textarea" :rows="3" placeholder="不少于10字" />
        </el-form-item>
        <el-form-item label="佐证材料" required>
          <el-input v-model="rectifyEvidence" placeholder='JSON数组，如 [{"name":"报告.pdf","url":"/files/r.pdf"}]' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="rectifyVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="submitting" @click="doRectify">提交验收</el-button>
      </template>
    </el-dialog>

    <!-- 验收弹窗 -->
    <el-dialog v-model="acceptVis" title="验收稽核" width="500px">
      <el-form label-width="80px" size="small">
        <el-form-item label="验收结果">
          <el-radio-group v-model="acceptPass">
            <el-radio :value="true">通过</el-radio>
            <el-radio :value="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="验收意见">
          <el-input v-model="acceptOpinion" type="textarea" :rows="3" placeholder="不少于5字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="acceptVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="submitting" @click="doAccept">确认</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVis" title="稽核详情" size="580px">
      <template v-if="detailRow">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="编号">{{ detailRow.auditNo }}</el-descriptions-item>
          <el-descriptions-item label="当事人">{{ detailRow.targetUser }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ detailRow.targetDept }}</el-descriptions-item>
          <el-descriptions-item label="维度">{{ detailRow.violateDimension }}</el-descriptions-item>
          <el-descriptions-item label="扣分">{{ detailRow.deductScore }}</el-descriptions-item>
          <el-descriptions-item label="整改期限">{{ detailRow.rectifyDeadline }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag size="small" :type="statusType(detailRow.status)">{{ detailRow.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="登记人">{{ detailRow.registrar }}</el-descriptions-item>
        </el-descriptions>
        <div class="mt"><strong>违规详情：</strong>{{ detailRow.violateDetail }}</div>
        <div class="mt"><strong>整改要求：</strong>{{ detailRow.rectifyRequirement }}</div>
        <div class="mt" v-if="detailRow.rectifyNote"><strong>整改说明：</strong>{{ detailRow.rectifyNote }}</div>
        <div class="mt" v-if="detailRow.acceptOpinion"><strong>验收意见：</strong>{{ detailRow.acceptOpinion }}</div>
        <div class="mt"><strong>审批日志：</strong><pre class="log-pre">{{ formatLog(detailRow.approvalLog) }}</pre></div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const tab = ref(can('C02','C03','C99') ? 'admin' : 'mine')
const loading = ref(false)
const adminRows = ref([])
const mineRows = ref([])
const filter = ref({ status: '', targetUser: '', dept: '' })

const createVis = ref(false)
const saving = ref(false)
const createRow = ref({})

const appealVis = ref(false)
const appealReason = ref('')
const appealRow = ref({})

const verdictVis = ref(false)
const verdictUphold = ref(true)
const verdictOpinion = ref('')
const verdictRow = ref({})

const rectifyVis = ref(false)
const rectifyNote = ref('')
const rectifyEvidence = ref('')
const rectifyRow = ref({})

const acceptVis = ref(false)
const acceptPass = ref(true)
const acceptOpinion = ref('')
const acceptRow = ref({})

const detailVis = ref(false)
const detailRow = ref(null)
const submitting = ref(false)

function statusType(s) {
  if (s === '已生效') return 'success'
  if (s === '申诉驳回已归档') return 'info'
  if (s === '申诉中') return 'warning'
  if (s === '待验收') return 'warning'
  return 'danger'
}
function fmt(t) { return t ? t.substring(0, 16) : '-' }
function formatLog(log) {
  try { return JSON.stringify(JSON.parse(log), null, 2) } catch { return log }
}
function disabledFuture(d) { return d && d.getTime() > Date.now() }

async function loadAdmin() {
  loading.value = true
  try { const r = await axios.get('/api/culture/behavior/audit/list', { params: filter.value }); adminRows.value = r.data.data || [] } catch { adminRows.value = [] } finally { loading.value = false }
}
async function loadMine() {
  loading.value = true
  try { const r = await axios.get('/api/culture/behavior/audit/mine'); mineRows.value = r.data.data || [] } catch { mineRows.value = [] } finally { loading.value = false }
}

function openCreate() {
  createRow.value = { targetUser: '', targetDept: '', violateDimension: '', violateScene: '', violateDetail: '', occurTime: '', redlineCode: '', deductScore: 3, rectifyRequirement: '', rectifyDeadline: '', attachments: '' }
  createVis.value = true
}
async function doCreate() {
  saving.value = true
  try {
    await axios.post('/api/culture/behavior/audit', createRow.value)
    ElMessage.success('登记成功')
    createVis.value = false
    loadAdmin()
  } catch (e) { ElMessage.error(e.response?.data?.message || '登记失败') } finally { saving.value = false }
}

async function doConfirm(row) {
  try {
    await ElMessageBox.confirm('确认已知晓该稽核记录？', '提示', { type: 'warning' })
    await axios.post(`/api/culture/behavior/audit/${row.id}/confirm`)
    ElMessage.success('已确认')
    loadMine()
  } catch { /* cancelled */ }
}

function openAppeal(row) {
  appealRow.value = row
  appealReason.value = ''
  appealVis.value = true
}
async function doAppeal() {
  submitting.value = true
  try {
    await axios.post(`/api/culture/behavior/audit/${appealRow.value.id}/appeal`, { reason: appealReason.value })
    ElMessage.success('申诉已提交')
    appealVis.value = false
    loadMine()
  } catch (e) { ElMessage.error(e.response?.data?.message || '申诉失败') } finally { submitting.value = false }
}

function openVerdict(row) {
  verdictRow.value = row
  verdictUphold.value = true
  verdictOpinion.value = ''
  verdictVis.value = true
}
async function doVerdict() {
  submitting.value = true
  try {
    await axios.post(`/api/culture/behavior/audit/${verdictRow.value.id}/appeal-verdict`, null, { params: { uphold: verdictUphold.value, opinion: verdictOpinion.value } })
    ElMessage.success('裁决完成')
    verdictVis.value = false
    loadAdmin()
  } catch (e) { ElMessage.error(e.response?.data?.message || '裁决失败') } finally { submitting.value = false }
}

function openRectify(row) {
  rectifyRow.value = row
  rectifyNote.value = ''
  rectifyEvidence.value = ''
  rectifyVis.value = true
}
async function doRectify() {
  submitting.value = true
  try {
    await axios.post(`/api/culture/behavior/audit/${rectifyRow.value.id}/rectify`, { note: rectifyNote.value, evidence: rectifyEvidence.value })
    ElMessage.success('整改材料已提交')
    rectifyVis.value = false
    loadMine()
  } catch (e) { ElMessage.error(e.response?.data?.message || '提交失败') } finally { submitting.value = false }
}

function openAccept(row) {
  acceptRow.value = row
  acceptPass.value = true
  acceptOpinion.value = ''
  acceptVis.value = true
}
async function doAccept() {
  submitting.value = true
  try {
    await axios.post(`/api/culture/behavior/audit/${acceptRow.value.id}/accept`, null, { params: { pass: acceptPass.value, opinion: acceptOpinion.value } })
    ElMessage.success('验收完成')
    acceptVis.value = false
    loadAdmin()
  } catch (e) { ElMessage.error(e.response?.data?.message || '验收失败') } finally { submitting.value = false }
}

function openDetail(row) {
  detailRow.value = row
  detailVis.value = true
}

onMounted(() => {
  if (can('C02','C03','C99')) loadAdmin()
  loadMine()
})
</script>

<style scoped>
.bar { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.mt { margin-top: 10px; }
.mb { margin-bottom: 10px; }
.tip { color: #999; font-size: 12px; margin-left: 8px; }
.log-pre { font-size: 12px; background: #f5f7fa; padding: 8px; border-radius: 4px; max-height: 300px; overflow: auto; }
</style>

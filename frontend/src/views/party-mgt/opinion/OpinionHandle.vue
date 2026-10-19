<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">七环节闭环：发现→研判→转办→处置→反馈→复核→归档 · 超时自动升级</el-tag>
    </RoleBar>

    <el-row :gutter="8" class="stat-row">
      <el-col :span="4"><el-card shadow="never"><div class="stat"><div class="num">{{ dash.total || 0 }}</div><div class="lbl">工单总数</div></div></el-card></el-col>
      <el-col :span="4"><el-card shadow="never"><div class="stat"><div class="num red">{{ dash.overdue || 0 }}</div><div class="lbl">处置超时(自动升级)</div></div></el-card></el-col>
      <el-col :span="4"><el-card shadow="never"><div class="stat"><div class="num orange">{{ dash.unclaimedLate || 0 }}</div><div class="lbl">超4小时未认领</div></div></el-card></el-col>
      <el-col :span="4"><el-card shadow="never"><div class="stat"><div class="num">{{ dash.closed || 0 }}</div><div class="lbl">已归档</div></div></el-card></el-col>
      <el-col :span="4"><el-card shadow="never"><div class="stat"><div class="num green">{{ dash.closeRate || 0 }}%</div><div class="lbl">闭环率</div></div></el-card></el-col>
      <el-col :span="4"><el-card shadow="never"><div class="stat"><div class="num">{{ dash.byLevel?.['红色'] || 0 }}</div><div class="lbl">红色预警单</div></div></el-card></el-col>
    </el-row>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="f.level" placeholder="全部等级" clearable size="small" style="width:110px" @change="load">
          <el-option v-for="l in ['红色','橙色','黄色','蓝色']" :key="l" :label="l" :value="l" />
        </el-select>
        <el-select v-model="f.flowStatus" placeholder="全部环节" clearable size="small" style="width:110px" @change="load">
          <el-option v-for="s in flows" :key="s" :label="s" :value="s" />
        </el-select>
        <el-input v-model="f.keyword" placeholder="标题" size="small" style="width:150px" clearable @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
      </div>
      <el-table :data="list" size="small" border v-loading="loading">
        <el-table-column prop="title" label="舆情标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="alertLevel" label="等级" width="80">
          <template #default="{ row }"><el-tag size="small" :type="levelType(row.alertLevel)" effect="dark">{{ row.alertLevel }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="flowStatus" label="当前环节" width="90">
          <template #default="{ row }"><el-tag size="small">{{ row.flowStatus }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="unit" label="承办单位" width="110" />
        <el-table-column prop="owner" label="责任人" width="90" />
        <el-table-column prop="deadline" label="处置时限" width="160" />
        <el-table-column label="时限状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.archiveStatus==='已归档'" size="small" type="success">已闭环</el-tag>
            <el-tag v-else-if="row.deadline && row.deadline < now" size="small" type="danger" effect="dark">已超时</el-tag>
            <el-tag v-else size="small" type="info">进行中</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="archiveStatus" label="归档" width="80" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="showRecords(row)">处置记录</el-button>
            <el-button v-if="can('R02','R04','R14') && row.flowStatus==='发现'" size="small" link type="primary" @click="flow(row,'research')">研判</el-button>
            <el-button v-if="can('R02','R04') && row.flowStatus==='研判'" size="small" link type="primary" @click="flow(row,'transfer')">转办</el-button>
            <el-button v-if="can('R02','R04','R14') && row.flowStatus==='转办' && !row.claimTime" size="small" link type="success" @click="doAct(row,'claim',{})">认领</el-button>
            <el-button v-if="can('R02','R14') && ['转办','处置'].includes(row.flowStatus)" size="small" link type="primary" @click="flow(row,'handle')">处置</el-button>
            <el-button v-if="can('R02','R14') && row.flowStatus==='处置'" size="small" link type="primary" @click="flow(row,'feedback')">反馈</el-button>
            <el-button v-if="can('R02','R03') && row.flowStatus==='反馈'" size="small" link type="warning" @click="review(row)">复核</el-button>
            <el-button v-if="can('R02','R03') && row.flowStatus==='复核'" size="small" link type="success" @click="doAct(row,'archive',{})">归档</el-button>
            <el-button v-if="can('R02','R04') && row.flowStatus!=='归档'" size="small" link type="danger" @click="flow(row,'level')">升降级</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="flowVisible" :title="flowTitle" width="560px">
      <el-form :model="flowForm" label-width="100px">
        <template v-if="flowKind==='research'">
          <el-form-item label="研判结论" required><el-input v-model="flowForm.conclusion" type="textarea" :rows="3" placeholder="属实性、影响面、风险等级" /></el-form-item>
          <el-form-item label="影响评估"><el-input v-model="flowForm.impact" /></el-form-item>
          <el-form-item label="风险等级">
            <el-select v-model="flowForm.riskLevel" style="width:100%"><el-option v-for="r in ['高','中','低']" :key="r" :label="r" :value="r" /></el-select>
          </el-form-item>
          <el-form-item label="启动应急预案"><el-switch v-model="flowForm.emergency" :active-value="1" :inactive-value="0" /></el-form-item>
        </template>
        <template v-else-if="flowKind==='transfer'">
          <el-form-item label="承办单位" required><el-input v-model="flowForm.unit" /></el-form-item>
          <el-form-item label="责任人" required><el-input v-model="flowForm.owner" /></el-form-item>
          <el-form-item label="处置时限" required>
            <el-date-picker v-model="flowForm.deadline" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" placeholder="红24/橙48/黄72小时" />
          </el-form-item>
        </template>
        <template v-else-if="flowKind==='handle'">
          <el-form-item label="处置措施" required><el-input v-model="flowForm.measure" type="textarea" :rows="4" placeholder="正面回应、澄清、整改安排" /></el-form-item>
        </template>
        <template v-else-if="flowKind==='feedback'">
          <el-form-item label="处置结果" required><el-input v-model="flowForm.result" type="textarea" :rows="3" /></el-form-item>
          <el-form-item label="反馈材料" required><el-input v-model="flowForm.feedbackFile" placeholder="佐证材料URL（缺失阻断归档）" /></el-form-item>
        </template>
        <template v-else-if="flowKind==='level'">
          <el-form-item label="调整为" required>
            <el-select v-model="flowForm.newLevel" style="width:100%"><el-option v-for="l in ['红色','橙色','黄色','蓝色']" :key="l" :label="l" :value="l" /></el-select>
          </el-form-item>
          <el-alert type="warning" :closable="false" title="升级/降级须有研判记录，无记录将阻断（E-OP-07）" />
        </template>
      </el-form>
      <template #footer>
        <el-button @click="flowVisible=false">取消</el-button>
        <el-button type="primary" @click="submitFlow">提交</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="recVisible" title="处置记录" size="42%">
      <el-timeline>
        <el-timeline-item v-for="r in records" :key="r.id" :timestamp="r.recordTime" :type="r.node==='升级' ? 'danger' : 'primary'">
          <b>{{ r.node }}</b>（{{ r.operator }}）
          <div>{{ r.conclusion }}</div>
          <div v-if="r.remark" class="remark">{{ r.remark }}</div>
        </el-timeline-item>
      </el-timeline>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/opinion/handle'
const flows = ['发现','研判','转办','处置','反馈','复核','归档']
const now = Date.now()

const loading = ref(false)
const list = ref([])
const dash = ref({})
const f = reactive({ level: null, flowStatus: null, keyword: '' })
const flowVisible = ref(false); const flowKind = ref(''); const flowTitle = ref(''); const flowRow = ref(null); const flowForm = ref({})
const recVisible = ref(false); const records = ref([])

function levelType(l) { return { 红色: 'danger', 橙色: 'warning', 黄色: 'warning', 蓝色: 'info' }[l] || 'info' }

async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${API}/list`, { params: { ...f, keyword: f.keyword || undefined } })
    if (res.data.code === 200) list.value = res.data.data
    const d = await axios.get(`${API}/dashboard`)
    if (d.data.code === 200) dash.value = d.data.data
  } finally { loading.value = false }
}

function flow(row, kind) {
  flowRow.value = row; flowKind.value = kind
  flowTitle.value = { research: '研判', transfer: '转办', handle: '处置', feedback: '反馈', level: '舆情升级/降级' }[kind]
  flowForm.value = kind === 'research' ? { conclusion: '', impact: '', riskLevel: '中', emergency: 0 }
    : kind === 'transfer' ? { unit: '', owner: '', deadline: '' }
    : kind === 'handle' ? { measure: '' }
    : kind === 'feedback' ? { result: '', feedbackFile: '' }
    : { newLevel: '橙色' }
  flowVisible.value = true
}
async function submitFlow() {
  const id = flowRow.value.id
  const fm = flowForm.value
  try {
    if (flowKind.value === 'research') {
      if (!fm.conclusion) { ElMessage.warning('研判结论必填'); return }
      await axios.post(`${API}/${id}/research`, fm)
    } else if (flowKind.value === 'transfer') {
      if (!fm.unit || !fm.owner || !fm.deadline) { ElMessage.warning('承办单位/责任人/处置时限必填'); return }
      await axios.post(`${API}/${id}/transfer`, fm)
    } else if (flowKind.value === 'handle') {
      if (!fm.measure) { ElMessage.warning('处置措施必填'); return }
      await axios.post(`${API}/${id}/handle`, fm)
    } else if (flowKind.value === 'feedback') {
      if (!fm.result || !fm.feedbackFile) { ElMessage.warning('处置结果与反馈材料必填'); return }
      await axios.post(`${API}/${id}/feedback`, fm)
    } else if (flowKind.value === 'level') {
      await axios.post(`${API}/${id}/change-level`, null, { params: { newLevel: fm.newLevel } })
    }
    ElMessage.success('已提交'); flowVisible.value = false; load()
  } catch {}
}
async function doAct(row, act, body) {
  try {
    await axios.post(`${API}/${row.id}/${act}`, body)
    ElMessage.success('操作成功'); load()
  } catch {}
}
async function review(row) {
  try {
    const { value } = await ElMessageBox.prompt('复核意见（退回将回到处置环节）', '复核', {
      inputPlaceholder: '复核意见',
      confirmButtonText: '通过', cancelButtonText: '退回',
      distinguishCancelAndClose: true
    })
    await axios.post(`${API}/${row.id}/review`, null, { params: { passed: true, opinion: value || '' } })
    ElMessage.success('复核通过'); load()
  } catch (e) {
    if (e === 'cancel') {
      try {
        await axios.post(`${API}/${row.id}/review`, null, { params: { passed: false, opinion: '退回补充' } })
        ElMessage.success('已退回处置'); load()
      } catch {}
    }
  }
}
async function showRecords(row) {
  const res = await axios.get(`${API}/records/${row.id}`)
  if (res.data.code === 200) { records.value = res.data.data; recVisible.value = true }
}
onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.stat-row { margin-bottom: 12px; }
.stat { text-align: center; }
.stat .num { font-size: 22px; font-weight: 600; }
.stat .num.red { color: #f56c6c; } .stat .num.orange { color: #e6a23c; }
.stat .num.green { color: #67c23a; }
.stat .lbl { font-size: 12px; color: #909399; }
.remark { color: #f56c6c; font-size: 12px; }
</style>

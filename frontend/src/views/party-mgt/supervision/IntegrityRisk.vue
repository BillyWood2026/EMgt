<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">排查→定级(纪委集体评估)→防控措施→定期检查(季/半年/年)→重新评估30日 / 编号 FX+年+3位</el-tag>
    </RoleBar>

    <el-row :gutter="10" class="mb">
      <el-col :span="5"><el-card shadow="hover" class="kpi"><div class="n">{{ stat.total||0 }}</div><div class="l">在册风险点</div></el-card></el-col>
      <el-col :span="5"><el-card shadow="hover" class="kpi"><div class="n red">{{ stat.high||0 }}</div><div class="l">高风险</div></el-card></el-col>
      <el-col :span="5"><el-card shadow="hover" class="kpi"><div class="n orange">{{ stat.mid||0 }}</div><div class="l">中风险</div></el-card></el-col>
      <el-col :span="5"><el-card shadow="hover" class="kpi"><div class="n" :class="{red: stat.dueUnchecked>0}">{{ stat.dueUnchecked||0 }}</div><div class="l">到期未检(E-SV-11)</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover" class="kpi"><div class="n">{{ stat.highControlRate||0 }}%</div><div class="l">高风险管控率</div></el-card></el-col>
    </el-row>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="riskLevel" placeholder="等级" clearable size="small" style="width:100px" @change="load">
          <el-option label="高" value="高" /><el-option label="中" value="中" /><el-option label="低" value="低" />
        </el-select>
        <el-select v-model="checkStatus" placeholder="检查状态" clearable size="small" style="width:120px" @change="load">
          <el-option label="正常" value="正常" /><el-option label="到期未检" value="到期未检" /><el-option label="发生问题" value="发生问题" />
        </el-select>
        <el-input v-model="keyword" placeholder="编号/风险点/部门" size="small" clearable style="width:170px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R23','R06','R02')" size="small" type="primary" @click="openCreate">排查登记</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="430">
        <el-table-column prop="riskNo" label="编号" width="100" />
        <el-table-column prop="riskPoint" label="风险点" min-width="140" show-overflow-tooltip />
        <el-table-column prop="domain" label="领域" width="90" />
        <el-table-column label="等级" width="70">
          <template #default="{row}">
            <el-tag size="small" :type="row.riskLevel==='高'?'danger':row.riskLevel==='中'?'warning':'info'">{{ row.riskLevel }}</el-tag>
            <el-tag v-if="row.levelConfirmed===1" size="small" type="success" effect="plain">已定级</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dept" label="责任部门" width="100" show-overflow-tooltip />
        <el-table-column prop="checkFreq" label="频次" width="60" />
        <el-table-column prop="lastCheckDate" label="上次检查" width="95" />
        <el-table-column prop="nextCheckDate" label="下次检查" width="95" />
        <el-table-column label="检查状态" width="90">
          <template #default="{row}"><el-tag size="small" :type="row.checkStatus==='正常'?'success':row.checkStatus==='发生问题'?'danger':'warning'">{{ row.checkStatus }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="preventionMeasure" label="防控措施" min-width="130" show-overflow-tooltip />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('R06') && row.levelConfirmed!==1" size="small" @click="openLevel(row)">定级</el-button>
            <el-button v-if="can('R23','R06')" size="small" @click="openCheck(row)">检查</el-button>
            <el-button v-if="can('R06') && row.checkStatus==='发生问题'" size="small" type="warning" @click="openReeval(row)">重新评估</el-button>
            <el-button size="small" @click="openHistory(row)">记录</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="createVis" title="风险点排查登记（高风险须措施 E-SV-10）" width="500px">
      <el-form :model="form" label-width="90px" size="small">
        <el-form-item label="风险点"><el-input v-model="form.riskPoint" /></el-form-item>
        <el-form-item label="所属领域"><el-input v-model="form.domain" placeholder="工程建设/采购/人事/财务" /></el-form-item>
        <el-form-item label="风险等级"><el-radio-group v-model="form.riskLevel"><el-radio value="高">高</el-radio><el-radio value="中">中</el-radio><el-radio value="低">低</el-radio></el-radio-group></el-form-item>
        <el-form-item label="责任部门"><el-input v-model="form.dept" /></el-form-item>
        <el-form-item label="检查频次"><el-select v-model="form.checkFreq" style="width:100%"><el-option label="季度" value="季度" /><el-option label="半年" value="半年" /><el-option label="年度" value="年度" /></el-select></el-form-item>
        <el-form-item label="防控措施"><el-input v-model="form.preventionMeasure" type="textarea" :rows="2" placeholder="高风险必填，否则阻断发布" /></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="createVis=false">取消</el-button><el-button size="small" type="primary" @click="doCreate">提交</el-button></template>
    </el-dialog>

    <el-dialog v-model="levelVis" title="纪委集体评估定级（R06）" width="440px">
      <el-form :model="levelForm" label-width="80px" size="small">
        <el-form-item label="风险等级"><el-radio-group v-model="levelForm.riskLevel"><el-radio value="高">高</el-radio><el-radio value="中">中</el-radio><el-radio value="低">低</el-radio></el-radio-group></el-form-item>
        <el-form-item label="防控措施"><el-input v-model="levelForm.measure" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="levelVis=false">取消</el-button><el-button size="small" type="primary" @click="doLevel">确认定级</el-button></template>
    </el-dialog>

    <el-dialog v-model="checkVis" title="定期检查登记" width="460px">
      <el-form :model="checkForm" label-width="90px" size="small">
        <el-form-item label="发现问题"><el-input v-model="checkForm.finding" type="textarea" :rows="2" placeholder="留空=本次检查无问题；填写则触发30日重新评估" /></el-form-item>
        <el-form-item label="措施调整"><el-input v-model="checkForm.measureAdjust" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="checkVis=false">取消</el-button><el-button size="small" type="primary" @click="doCheck">提交检查</el-button></template>
    </el-dialog>

    <el-dialog v-model="reevalVis" title="重新评估（问题发生后30日内 E-SV-12）" width="440px">
      <el-form :model="reevalForm" label-width="90px" size="small">
        <el-form-item label="新等级"><el-radio-group v-model="reevalForm.newLevel"><el-radio value="高">高</el-radio><el-radio value="中">中</el-radio><el-radio value="低">低</el-radio></el-radio-group></el-form-item>
        <el-form-item label="新防控措施"><el-input v-model="reevalForm.newMeasure" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="reevalVis=false">取消</el-button><el-button size="small" type="warning" @click="doReeval">确认评估</el-button></template>
    </el-dialog>

    <el-dialog v-model="histVis" title="检查记录" width="520px">
      <el-timeline>
        <el-timeline-item v-for="c in checks" :key="c.id" :timestamp="c.checkDate">
          {{ c.checker }}：{{ c.finding || '检查无问题' }}
          <div v-if="c.measureAdjust" class="hint">措施调整：{{ c.measureAdjust }}</div>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/supervision/risk'
const rows = ref([]); const loading = ref(false)
const riskLevel = ref(''); const checkStatus = ref(''); const keyword = ref('')
const stat = ref({})
const createVis = ref(false); const form = ref({})
const levelVis = ref(false); const levelForm = ref({}); const cur = ref(null)
const checkVis = ref(false); const checkForm = ref({})
const reevalVis = ref(false); const reevalForm = ref({})
const histVis = ref(false); const checks = ref([])

async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { riskLevel: riskLevel.value||undefined, checkStatus: checkStatus.value||undefined, keyword: keyword.value||undefined } })
    rows.value = res.data.data || []
  } finally { loading.value = false }
}
async function loadStat() { try { stat.value = (await axios.get(`${api}/stats`)).data.data || {} } catch {} }
function openCreate() { form.value = { riskLevel:'中', checkFreq:'季度' }; createVis.value = true }
async function doCreate() {
  try { await axios.post(api, form.value); createVis.value=false; ElMessage.success('已登记'); load(); loadStat() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openLevel(row) { cur.value=row; levelForm.value={ riskLevel:row.riskLevel, measure:row.preventionMeasure||'' }; levelVis.value=true }
async function doLevel() {
  try { await axios.post(`${api}/${cur.value.id}/confirm-level`, levelForm.value); levelVis.value=false; ElMessage.success('定级完成'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openCheck(row) { cur.value=row; checkForm.value={ finding:'', measureAdjust:'' }; checkVis.value=true }
async function doCheck() {
  try { await axios.post(`${api}/${cur.value.id}/check`, { ...checkForm.value, checker:'R23' }); checkVis.value=false; ElMessage.success('检查已登记'); load(); loadStat() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
function openReeval(row) { cur.value=row; reevalForm.value={ newLevel:row.riskLevel, newMeasure:'' }; reevalVis.value=true }
async function doReeval() {
  try { await axios.post(`${api}/${cur.value.id}/reevaluate`, reevalForm.value); reevalVis.value=false; ElMessage.success('重新评估完成'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function openHistory(row) {
  histVis.value = true
  try { checks.value = (await axios.get(`${api}/${row.id}/checks`)).data.data || [] } catch { checks.value=[] }
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
.hint { color: #999; font-size: 12px; }
</style>

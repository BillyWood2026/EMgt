<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">方案(权重合计=100% E-SV-19)→发布(R06)→支部自评(E-SV-18)→纪委核查→等次(≥90优/80良/70合格) · 一票否决二次确认(E-SV-20)</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <template #header>
        <div class="bar" style="margin:0">
          <b>考核方案</b>
          <el-select v-model="year" size="small" style="width:100px" @change="reload">
            <el-option :value="2026" label="2026" /><el-option :value="2025" label="2025" />
          </el-select>
          <span style="flex:1"></span>
          <el-button v-if="can('R06','R02')" size="small" type="primary" @click="openScheme">创建方案</el-button>
        </div>
      </template>
      <el-table :data="schemes" size="small" border max-height="220">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="schemeName" label="方案名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="assessYear" label="年度" width="70" />
        <el-table-column label="状态" width="90">
          <template #default="{row}"><el-tag size="small" :type="row.schemeStatus==='已发布'?'success':row.schemeStatus==='已结束'?'info':'warning'">{{ row.schemeStatus }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160">
          <template #default="{row}">{{ row.publishTime ? row.publishTime.replace('T',' ').slice(0,19) : '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{row}">
            <el-button size="small" @click="viewIndicators(row)">指标</el-button>
            <el-button v-if="can('R06') && row.schemeStatus==='草稿'" size="small" type="primary" @click="publish(row)">发布</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never">
      <template #header><b>组织考核记录（{{ year }}年度）</b></template>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="330">
        <el-table-column prop="orgName" label="考核组织" min-width="150" show-overflow-tooltip />
        <el-table-column prop="schemeName" label="方案" min-width="150" show-overflow-tooltip />
        <el-table-column label="方案状态" width="90">
          <template #default="{row}"><el-tag size="small" :type="row.schemeStatus==='已发布'?'success':'info'">{{ row.schemeStatus }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="selfScore" label="自评分" width="80" />
        <el-table-column prop="checkScore" label="核查分" width="80" />
        <el-table-column prop="finalScore" label="最终分" width="80" />
        <el-table-column label="等次" width="80">
          <template #default="{row}">
            <el-tag v-if="row.grade" size="small" :type="gradeType(row.grade)">{{ row.grade }}</el-tag>
            <el-tag v-if="row.vetoFlag===1" size="small" type="danger" effect="plain">一票否决</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="阶段" width="80" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('R07','R06','R23') && row.schemeStatus==='已发布' && !row.selfScore" size="small" @click="openSelf(row)">自评</el-button>
            <el-button v-if="can('R06','R23') && row.selfScore && !row.checkScore" size="small" @click="openCheck(row)">核查</el-button>
            <el-button v-if="can('R06') && row.checkScore && !row.grade" size="small" type="warning" @click="openFinalize(row)">评定</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="schemeVis" title="创建考核方案（指标权重合计须=100% E-SV-19）" width="600px">
      <el-form :model="schemeForm" label-width="90px" size="small">
        <el-form-item label="方案名称"><el-input v-model="schemeForm.schemeName" /></el-form-item>
        <el-form-item label="年度"><el-input-number v-model="schemeForm.year" :min="2024" :max="2030" /></el-form-item>
        <el-form-item label="考核组织">
          <el-select v-model="schemeForm.orgNames" multiple filterable allow-create default-first-option style="width:100%" placeholder="输入组织名后回车">
            <el-option label="第一党支部" value="第一党支部" /><el-option label="第二党支部" value="第二党支部" /><el-option label="机关党支部" value="机关党支部" />
          </el-select>
        </el-form-item>
        <el-form-item label="考核指标">
          <div style="width:100%">
            <div v-for="(ind,i) in schemeForm.indicators" :key="i" class="bar" style="margin:0 0 6px">
              <el-input v-model="ind.indicatorName" placeholder="指标名称" style="width:240px" />
              <el-input-number v-model="ind.weight" :min="0" :max="100" :precision="1" style="width:120px" />
              <span class="hint">%</span>
              <el-button size="small" link type="danger" @click="schemeForm.indicators.splice(i,1)">删除</el-button>
            </div>
            <el-button size="small" @click="schemeForm.indicators.push({indicatorName:'',weight:0})">+ 增加指标</el-button>
            <el-tag :type="weightTotal===100?'success':'danger'" size="small" style="margin-left:10px">权重合计 {{ weightTotal }}%</el-tag>
          </div>
        </el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="schemeVis=false">取消</el-button><el-button size="small" type="primary" @click="doCreateScheme">创建（草稿）</el-button></template>
    </el-dialog>

    <el-dialog v-model="indVis" title="考核指标" width="480px">
      <el-table :data="indicators" size="small" border>
        <el-table-column prop="indicatorName" label="指标" min-width="160" />
        <el-table-column prop="weight" label="权重" width="80"><template #default="{row}">{{ row.weight }}%</template></el-table-column>
        <el-table-column prop="standard" label="标准" min-width="120" show-overflow-tooltip />
      </el-table>
    </el-dialog>

    <el-dialog v-model="selfVis" title="支部自评（方案已发布方可提交）" width="480px">
      <el-form label-width="150px" size="small">
        <el-form-item v-for="ind in indicators" :key="ind.id" :label="ind.indicatorName+' ('+ind.weight+'%)'">
          <el-input-number v-model="scoreForm[ind.indicatorName]" :min="0" :max="100" :precision="1" />
        </el-form-item>
        <el-form-item label="自评佐证材料"><el-input v-model="selfEvidence" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="selfVis=false">取消</el-button><el-button size="small" type="primary" @click="doSelf">提交自评</el-button></template>
    </el-dialog>

    <el-dialog v-model="checkVis" title="纪委核查评分" width="480px">
      <el-form label-width="150px" size="small">
        <el-form-item v-for="ind in indicators" :key="ind.id" :label="ind.indicatorName+' ('+ind.weight+'%)'">
          <el-input-number v-model="scoreForm[ind.indicatorName]" :min="0" :max="100" :precision="1" />
        </el-form-item>
        <el-form-item label="扣分说明"><el-input v-model="deductions" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="一票否决"><el-switch v-model="vetoFlag" :active-value="1" :inactive-value="0" /><span class="hint">重大违纪违法案件处置不力</span></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="checkVis=false">取消</el-button><el-button size="small" type="primary" @click="doCheck">提交核查</el-button></template>
    </el-dialog>

    <el-dialog v-model="finalVis" title="等次评定（R06）" width="420px">
      <el-descriptions :column="1" size="small" border>
        <el-descriptions-item label="核查得分">{{ cur?.checkScore }}</el-descriptions-item>
        <el-descriptions-item label="建议等次">
          <el-tag size="small" :type="gradeType(suggestGrade)">{{ suggestGrade }}</el-tag>
          <el-tag v-if="cur?.vetoFlag===1" size="small" type="danger">命中一票否决：直接降为不合格，须二次确认</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-form label-width="100px" size="small" style="margin-top:10px">
        <el-form-item label="结果运用"><el-input v-model="resultApplication" type="textarea" :rows="2" placeholder="通报/评优/任用挂钩" /></el-form-item>
      </el-form>
      <template #footer><el-button size="small" @click="finalVis=false">取消</el-button><el-button size="small" type="warning" @click="doFinalize">确认评定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/supervision/assessment'
const year = ref(2026)
const schemes = ref([]); const rows = ref([]); const loading = ref(false)
const schemeVis = ref(false)
const schemeForm = ref({ schemeName:'', year:2026, orgNames:[], indicators:[] })
const indVis = ref(false); const indicators = ref([])
const selfVis = ref(false); const checkVis = ref(false); const finalVis = ref(false)
const cur = ref(null)
const scoreForm = ref({}); const selfEvidence = ref('')
const deductions = ref(''); const vetoFlag = ref(0)
const resultApplication = ref('')

const weightTotal = computed(() => schemeForm.value.indicators.reduce((s,i)=>s+(Number(i.weight)||0),0))
const suggestGrade = computed(() => {
  if (!cur.value) return ''
  if (cur.value.vetoFlag===1) return '不合格'
  const s = Number(cur.value.checkScore||0)
  return s>=90?'优秀':s>=80?'良好':s>=70?'合格':'不合格'
})
function gradeType(g) {
  if (g==='优秀') return 'success'
  if (g==='不合格') return 'danger'
  if (g==='良好') return ''
  if (g==='合格') return 'warning'
  return 'info'
}

async function loadSchemes() { try { schemes.value = (await axios.get(`${api}/schemes`,{params:{year:year.value}})).data.data||[] } catch {} }
async function loadRows() {
  loading.value = true
  try { rows.value = (await axios.get(`${api}/list`,{params:{year:year.value}})).data.data||[] } finally { loading.value=false }
}
function reload() { loadSchemes(); loadRows() }
function openScheme() {
  schemeForm.value = { schemeName:`${year.value}年度党风廉政建设考核`, year:year.value, orgNames:[], indicators:[{indicatorName:'',weight:100}] }
  schemeVis.value = true
}
async function doCreateScheme() {
  if (weightTotal.value!==100) { ElMessage.error('E-SV-19 指标权重合计须=100%'); return }
  if (!schemeForm.value.orgNames.length) { ElMessage.error('请选择考核组织'); return }
  try {
    await axios.post(`${api}/scheme`, schemeForm.value)
    schemeVis.value=false; ElMessage.success('方案草稿已创建'); reload()
  } catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function publish(row) {
  try {
    await ElMessageBox.confirm(`确认发布方案「${row.schemeName}」？发布后支部方可自评`,'发布方案',{type:'warning'})
    await axios.post(`${api}/scheme/${row.id}/publish`)
    ElMessage.success('方案已发布'); reload()
  } catch(e) { if (e!=='cancel') ElMessage.error(e.response?.data?.message || e.message) }
}
async function loadIndicatorsOf(row) {
  const scheme = schemes.value.find(s => s.schemeName===row.schemeName && s.assessYear===row.assessYear)
  if (!scheme) { indicators.value=[]; return }
  indicators.value = (await axios.get(`${api}/scheme/${scheme.id}/indicators`)).data.data || []
}
async function viewIndicators(row) {
  indicators.value = (await axios.get(`${api}/scheme/${row.id}/indicators`)).data.data || []
  indVis.value = true
}
async function openSelf(row) {
  cur.value=row; await loadIndicatorsOf(row)
  scoreForm.value={}; selfEvidence.value=''; selfVis.value=true
}
async function doSelf() {
  try {
    await axios.post(`${api}/${cur.value.id}/self`, { scores: scoreForm.value, evidence: selfEvidence.value })
    selfVis.value=false; ElMessage.success('自评已提交'); loadRows()
  } catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function openCheck(row) {
  cur.value=row; await loadIndicatorsOf(row)
  const init = {}
  indicators.value.forEach(i => init[i.indicatorName] = i.selfScore!=null?Number(i.selfScore):null)
  scoreForm.value=init; deductions.value=''; vetoFlag.value=0; checkVis.value=true
}
async function doCheck() {
  try {
    await axios.post(`${api}/${cur.value.id}/check`, { scores: scoreForm.value, deductions: deductions.value, vetoFlag: vetoFlag.value })
    checkVis.value=false; ElMessage.success('核查完成'); loadRows()
  } catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function openFinalize(row) {
  cur.value=row; resultApplication.value=''; finalVis.value=true
}
async function doFinalize() {
  try {
    const confirmed = cur.value.vetoFlag===1
    await ElMessageBox.confirm(confirmed?'命中一票否决，确认直接定为不合格（R06 二次确认 E-SV-20）？':`确认评定为「${suggestGrade.value}」？`,'等次评定',{type:'warning',confirmButtonText:'确认'})
    await axios.post(`${api}/${cur.value.id}/finalize?vetoConfirmed=${confirmed}`, { resultApplication: resultApplication.value })
    finalVis.value=false; ElMessage.success('评定完成'); loadRows()
  } catch(e) { if (e!=='cancel') ElMessage.error(e.response?.data?.message || e.message) }
}
onMounted(reload)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.mb { margin-bottom: 12px; }
.hint { color: #999; font-size: 12px; margin-left: 8px; }
</style>

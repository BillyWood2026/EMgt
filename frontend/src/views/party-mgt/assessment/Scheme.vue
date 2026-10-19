<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">草稿→已发布→已结束 · 发布前权重合计须=100% · 时间重叠预警 · 发布后自动生成考核任务</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="f.status" placeholder="状态" clearable size="small" style="width:110px" @change="load">
          <el-option v-for="s in ['草稿','已发布','已结束']" :key="s" :label="s" :value="s" />
        </el-select>
        <el-input-number v-model="f.year" :min="2020" :max="2030" size="small" controls-position="right" style="width:110px" @change="load" />
        <el-input v-model="f.keyword" placeholder="方案名称" size="small" clearable style="width:160px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R02','R04')" size="small" type="primary" @click="edit()">新建方案</el-button>
      </div>

      <el-table :data="rows" size="small" border v-loading="loading">
        <el-table-column prop="schemeName" label="方案名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="assessYear" label="年度" width="70" />
        <el-table-column prop="periodType" label="周期" width="80" />
        <el-table-column prop="targetScope" label="考核对象" width="110" />
        <el-table-column prop="targetOrgNames" label="覆盖组织" min-width="140" show-overflow-tooltip />
        <el-table-column label="分数线(合格/良好/优秀)" width="160">
          <template #default="{ row }">{{ row.passLine }} / {{ row.goodLine }} / {{ row.excellentLine }}</template>
        </el-table-column>
        <el-table-column prop="appealDays" label="申诉天数" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status==='已发布'?'success':(row.status==='已结束'?'info':'warning')">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="150">
          <template #default="{ row }">{{ (row.publishTime||'').replace('T',' ').slice(0,16) || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('R02','R04') && row.status==='草稿'" size="small" link @click="edit(row)">编辑</el-button>
            <el-button v-if="can('R02','R04') && row.status==='草稿'" size="small" link type="success" @click="openPublish(row)">发布</el-button>
            <el-button v-if="can('R02','R04') && row.status==='已发布'" size="small" link type="warning" @click="finish(row)">结束</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑方案' : '新建方案'" width="640px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="方案名称" required><el-input v-model="form.schemeName" /></el-form-item>
        <el-form-item label="考核年度" required><el-input-number v-model="form.assessYear" :min="2020" :max="2030" /></el-form-item>
        <el-form-item label="周期类型">
          <el-radio-group v-model="form.periodType">
            <el-radio-button value="年度">年度</el-radio-button>
            <el-radio-button value="半年">半年</el-radio-button>
            <el-radio-button value="季度">季度</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="考核对象" required>
          <el-radio-group v-model="form.targetScope">
            <el-radio-button value="党组织">党组织</el-radio-button>
            <el-radio-button value="党组织书记">党组织书记</el-radio-button>
            <el-radio-button value="党员">党员</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="覆盖组织">
          <el-select v-model="form.targetOrgIds" multiple style="width:100%" @change="onOrgsChange">
            <el-option v-for="o in orgs" :key="o.id" :label="o.name" :value="String(o.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="分数线">
          <div class="score-line">
            <span>合格</span><el-input-number v-model="form.passLine" :min="0" :max="100" size="small" />
            <span>良好</span><el-input-number v-model="form.goodLine" :min="0" :max="100" size="small" />
            <span>优秀</span><el-input-number v-model="form.excellentLine" :min="0" :max="100" size="small" />
          </div>
        </el-form-item>
        <el-form-item label="优秀比例上限%"><el-input-number v-model="form.excellentRatio" :min="0" :max="100" :precision="2" /></el-form-item>
        <el-form-item label="调整分上限±"><el-input-number v-model="form.adjustCap" :min="0" :max="20" :precision="2" /></el-form-item>
        <el-form-item label="申诉期(天)"><el-input-number v-model="form.appealDays" :min="0" :max="30" /></el-form-item>
        <el-form-item label="自评截止"><el-date-picker v-model="form.selfDeadline" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="核查开始"><el-date-picker v-model="form.checkStart" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="实地考核日"><el-date-picker v-model="form.onsiteDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="结果反馈日"><el-date-picker v-model="form.feedbackDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible=false">取消</el-button>
        <el-button type="primary" @click="save">保存草稿</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="publishVisible" title="发布考核方案" width="680px">
      <el-alert type="warning" :closable="false" show-icon title="发布前请选择纳入本方案的指标，系统将校验权重合计=100%，并检测同期已发布方案时间重叠" style="margin-bottom:12px" />
      <div class="bar">
        <el-input v-model="indKeyword" placeholder="搜索指标" size="small" clearable style="width:180px" />
        <el-button size="small" @click="doWeightCheck" :loading="checking">权重合规校验</el-button>
        <el-tag v-if="weightResult" size="small" :type="weightResult.valid ? 'success' : 'danger'">
          合计 {{ weightResult.total }}% {{ weightResult.valid ? '✓ 合规' : '✗ 须=100%' }}
        </el-tag>
      </div>
      <el-table ref="indTable" :data="filteredIndicators" size="small" border height="300" @selection-change="onIndSelect">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="indicatorCode" label="编码" width="90" />
        <el-table-column prop="indicatorName" label="指标名称" min-width="170" show-overflow-tooltip />
        <el-table-column prop="level" label="层级" width="60" />
        <el-table-column prop="weight" label="权重%" width="80" />
        <el-table-column label="否决" width="60">
          <template #default="{ row }"><el-tag v-if="row.vetoFlag===1" size="small" type="danger">否决</el-tag></template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="publishVisible=false">取消</el-button>
        <el-button type="danger" plain @click="doPublish(true)">强制发布</el-button>
        <el-button type="primary" :disabled="!weightResult || !weightResult.valid" @click="doPublish(false)">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/assessment/scheme'
const orgs = [{ id: 1, name: '第一党支部' }, { id: 2, name: '第二党支部' }]

const loading = ref(false)
const rows = ref([])
const f = reactive({ status: null, year: new Date().getFullYear(), keyword: '' })
const formVisible = ref(false)
const form = ref({})
const publishVisible = ref(false)
const publishing = ref(null)
const indicators = ref([])
const indKeyword = ref('')
const selectedInds = ref([])
const weightResult = ref(null)
const checking = ref(false)

const filteredIndicators = computed(() => {
  const k = indKeyword.value.trim()
  if (!k) return indicators.value
  return indicators.value.filter(i => (i.indicatorName || '').includes(k) || (i.indicatorCode || '').includes(k))
})

async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { ...f } })
    if (res.data.code === 200) rows.value = res.data.data
  } finally { loading.value = false }
}
function onOrgsChange(ids) {
  form.value.targetOrgNames = ids.map(id => (orgs.find(o => String(o.id) === id) || {}).name).filter(Boolean).join(',')
}
function edit(row) {
  form.value = row
    ? { ...row, targetOrgIds: (row.targetOrgIds || '').split(',').filter(Boolean) }
    : { assessYear: new Date().getFullYear(), periodType: '年度', targetScope: '党组织', targetOrgIds: [], passLine: 60, goodLine: 80, excellentLine: 90, excellentRatio: 30, adjustCap: 5, appealDays: 5, status: '草稿' }
  formVisible.value = true
}
async function save() {
  try {
    await axios.post(api, { ...form.value, targetOrgIds: (form.value.targetOrgIds || []).join(',') })
    ElMessage.success('已保存')
    formVisible.value = false
    load()
  } catch {}
}
async function openPublish(row) {
  publishing.value = row
  weightResult.value = null
  indKeyword.value = ''
  const res = await axios.get('/api/party/assessment/indicator/list', { params: { status: '启用' } })
  indicators.value = (res.data.data || []).filter(i => i.level >= 1)
  publishVisible.value = true
}
function onIndSelect(sel) {
  selectedInds.value = sel
  weightResult.value = null
}
async function doWeightCheck() {
  if (!selectedInds.value.length) { ElMessage.warning('请先勾选指标'); return }
  checking.value = true
  try {
    const res = await axios.post(`${api}/weight-check`, selectedInds.value.map(i => i.id))
    weightResult.value = res.data.data
  } finally { checking.value = false }
}
async function doPublish(force) {
  try {
    await axios.post(`${api}/${publishing.value.id}/publish`, selectedInds.value.map(i => i.id), { params: { force } })
    ElMessage.success('方案已发布，考核任务已自动生成')
    publishVisible.value = false
    load()
  } catch {}
}
async function finish(row) {
  await ElMessageBox.confirm(`确认结束方案「${row.schemeName}」？结束后任务不可再流转。`, '提示', { type: 'warning' })
  try {
    await axios.post(`${api}/${row.id}/finish`)
    ElMessage.success('方案已结束')
    load()
  } catch {}
}
onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.score-line { display: flex; gap: 8px; align-items: center; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">申诉期满方可发布排名(E-AS-09/10) · 不合格对象自动生成整改台账 · 超期未销号红色预警</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-select v-model="schemeId" placeholder="选择方案" size="small" style="width:220px" @change="onSchemeChange">
          <el-option v-for="s in schemes" :key="s.id" :label="s.schemeName" :value="s.id" />
        </el-select>
        <el-button v-if="can('R02','R03','R04')" size="small" type="success" :disabled="!schemeId" @click="publishResults(false)">发布排名</el-button>
        <el-button v-if="can('R02','R03','R04')" size="small" type="danger" plain :disabled="!schemeId" @click="publishResults(true)">强制发布</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R02','R04')" size="small" type="warning" @click="overdueCheck">超期整改检查</el-button>
      </div>

      <el-table :data="results" size="small" border v-loading="loadingResults">
        <el-table-column prop="rankNo" label="排名" width="70">
          <template #default="{ row }">{{ row.rankNo ?? '—' }}</template>
        </el-table-column>
        <el-table-column prop="orgName" label="考核对象" width="140" show-overflow-tooltip />
        <el-table-column prop="totalScore" label="总分" width="90" />
        <el-table-column prop="adjustScore" label="调整分" width="80">
          <template #default="{ row }">{{ row.adjustScore ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="等级" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.grade" size="small" :type="{ '优秀':'success','良好':'primary','合格':'warning','不合格':'danger' }[row.grade]">{{ row.grade }}</el-tag>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="一票否决" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.vetoTriggered===1" size="small" type="danger">命中</el-tag>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status==='已闭环'?'info':(row.status==='整改中'?'danger':'success')">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="150">
          <template #default="{ row }">{{ (row.publishTime||'').replace('T',' ').slice(0,16) || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('R02','R04') && row.grade==='不合格'" size="small" link type="warning" @click="openAddRectify(row)">建整改项</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="bar" style="margin-bottom:0">
          <b>整改台账</b>
          <el-select v-model="rectifyStatus" placeholder="全部状态" clearable size="small" style="width:120px" @change="loadRectify">
            <el-option v-for="s in ['整改中','待验收','已销号']" :key="s" :label="s" :value="s" />
          </el-select>
          <el-button size="small" @click="loadRectify">刷新</el-button>
        </div>
      </template>
      <el-table :data="rectifies" size="small" border v-loading="loadingRectify">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="taskId" label="任务" width="70" />
        <el-table-column prop="problem" label="问题描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="dutyPerson" label="责任人" width="90" />
        <el-table-column label="整改期限" width="110">
          <template #default="{ row }">
            <span :class="{ overdue: isOverdue(row) }">{{ row.deadline }}</span>
            <el-tag v-if="isOverdue(row)" size="small" type="danger" style="margin-left:4px">超期</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status==='已销号'?'info':(row.status==='待验收'?'warning':'danger')">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="finishProof" label="佐证" min-width="130" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('R02','R04','R07') && row.status==='整改中'" size="small" link type="primary" @click="openFinish(row)">上传佐证</el-button>
            <el-button v-if="can('R02','R04') && row.status==='待验收'" size="small" link type="success" @click="closeRectify(row)">销号</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="addVisible" title="新建整改项" width="480px">
      <el-form :model="rectifyForm" label-width="90px">
        <el-form-item label="考核对象">{{ rectifyForm.orgName }}</el-form-item>
        <el-form-item label="问题描述" required><el-input v-model="rectifyForm.problem" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="责任人" required><el-input v-model="rectifyForm.dutyPerson" /></el-form-item>
        <el-form-item label="整改期限" required><el-date-picker v-model="rectifyForm.deadline" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible=false">取消</el-button>
        <el-button type="primary" @click="addRectify">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="finishVisible" title="完成整改(上传佐证)" width="440px">
      <el-form label-width="80px">
        <el-form-item label="问题">{{ finishTarget?.problem }}</el-form-item>
        <el-form-item label="佐证材料" required><el-input v-model="proof" type="textarea" :rows="3" placeholder="整改完成佐证链接或说明" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finishVisible=false">取消</el-button>
        <el-button type="primary" @click="finishRectify">提交验收</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/assessment/result'

const schemes = ref([])
const schemeId = ref(null)
const results = ref([])
const loadingResults = ref(false)
const rectifies = ref([])
const loadingRectify = ref(false)
const rectifyStatus = ref(null)
const addVisible = ref(false)
const rectifyForm = reactive({ taskId: null, orgName: '', problem: '', dutyPerson: '', deadline: '' })
const finishVisible = ref(false)
const finishTarget = ref(null)
const proof = ref('')

function isOverdue(row) {
  return row.status !== '已销号' && row.deadline && row.deadline < new Date().toISOString().slice(0, 10)
}

async function loadSchemes() {
  const res = await axios.get('/api/party/assessment/scheme/list')
  schemes.value = res.data.data || []
}
async function onSchemeChange() { loadResults() }
async function loadResults() {
  if (!schemeId.value) { results.value = []; return }
  loadingResults.value = true
  try {
    const res = await axios.get('/api/party/assessment/process/task/list', { params: { schemeId: schemeId.value } })
    results.value = (res.data.data || []).sort((a, b) => (a.rankNo || 999) - (b.rankNo || 999))
  } finally { loadingResults.value = false }
}
async function publishResults(force) {
  if (force) await ElMessageBox.confirm('强制发布将跳过申诉期校验，确认继续？', '提示', { type: 'warning' })
  try {
    const res = await axios.post(`${api}/${schemeId.value}/publish`, null, { params: { force } })
    ElMessage.success(`已发布 ${res.data.data.published ?? ''} 个对象排名`.trim())
    loadResults()
    loadRectify()
  } catch {}
}
async function loadRectify() {
  loadingRectify.value = true
  try {
    const res = await axios.get(`${api}/rectify/list`, { params: { status: rectifyStatus.value } })
    rectifies.value = res.data.data || []
  } finally { loadingRectify.value = false }
}
function openAddRectify(row) {
  Object.assign(rectifyForm, { taskId: row.id, orgName: row.orgName, problem: '', dutyPerson: '', deadline: '' })
  addVisible.value = true
}
async function addRectify() {
  try {
    await axios.post(`${api}/rectify/add`, { taskId: rectifyForm.taskId, problem: rectifyForm.problem, dutyPerson: rectifyForm.dutyPerson, deadline: rectifyForm.deadline, status: '整改中' })
    ElMessage.success('整改项已创建')
    addVisible.value = false
    loadRectify()
    loadResults()
  } catch {}
}
function openFinish(row) { finishTarget.value = row; proof.value = ''; finishVisible.value = true }
async function finishRectify() {
  try {
    await axios.post(`${api}/rectify/${finishTarget.value.id}/finish`, null, { params: { proof: proof.value } })
    ElMessage.success('已提交验收')
    finishVisible.value = false
    loadRectify()
  } catch {}
}
async function closeRectify(row) {
  await ElMessageBox.confirm('确认验收通过并销号？', '提示', { type: 'warning' })
  try {
    await axios.post(`${api}/rectify/${row.id}/close`, null, { params: { closeBy: '党委组织委员' } })
    ElMessage.success('已销号')
    loadRectify()
    loadResults()
  } catch {}
}
async function overdueCheck() {
  try {
    const res = await axios.post(`${api}/rectify/overdue-check`)
    ElMessage.warning(`发现 ${res.data.data.overdue} 项超期整改`)
    loadRectify()
  } catch {}
}
onMounted(async () => { await loadSchemes(); loadRectify() })
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
.mb { margin-bottom: 12px; }
.muted { color: #c0c4cc; }
.overdue { color: #f56c6c; font-weight: 600; }
</style>

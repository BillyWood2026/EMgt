<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">计划→审批→实施→完成 / 纪法测试参与率<90%橙色预警（E-DI-22）</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-select v-model="year" placeholder="年度" clearable size="small" style="width:100px" @change="load">
          <el-option label="2026" value="2026" /><el-option label="2025" value="2025" />
        </el-select>
        <el-select v-model="formType" placeholder="形式" clearable size="small" style="width:130px" @change="load">
          <el-option v-for="f in forms" :key="f" :label="f" :value="f" />
        </el-select>
        <el-select v-model="status" placeholder="状态" clearable size="small" style="width:120px" @change="load">
          <el-option label="待审批" value="待审批" />
          <el-option label="已审批" value="已审批" />
          <el-option label="已完成" value="已完成" />
          <el-option label="已退回" value="已退回" />
        </el-select>
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R20','R06')" size="small" type="primary" @click="openPlan">新增计划</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="440">
        <el-table-column prop="planNo" label="编号" width="120" />
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="form" label="形式" width="110" />
        <el-table-column prop="targetScope" label="对象范围" width="110" />
        <el-table-column prop="planDate" label="计划日期" width="100" />
        <el-table-column prop="attendCount" label="签到人数" width="80" />
        <el-table-column prop="testRate" label="测试率" width="80" />
        <el-table-column prop="alert" label="预警" width="140" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}"><el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('R06') && row.status==='待审批'" size="small" @click="approve(row)">审批</el-button>
            <el-button v-if="can('R20','R06') && row.status==='已审批'" size="small" @click="openFinish(row)">完成</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增 -->
    <el-dialog v-model="planVis" title="教育计划" width="520px">
      <el-form :model="form" label-width="80px" size="small">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="形式"><el-select v-model="form.form" style="width:100%"><el-option v-for="f in forms" :key="f" :label="f" :value="f" /></el-select></el-form-item>
        <el-form-item label="教育类型"><el-input v-model="form.educationType" /></el-form-item>
        <el-form-item label="对象范围"><el-input v-model="form.targetScope" /></el-form-item>
        <el-form-item label="计划日期"><el-input v-model="form.planDate" placeholder="yyyy-MM-dd" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="planVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doPlan">提交</el-button>
      </template>
    </el-dialog>

    <!-- 完成 -->
    <el-dialog v-model="finVis" title="完成登记" width="460px">
      <el-form :model="finForm" label-width="90px" size="small">
        <el-form-item label="签到人数"><el-input-number v-model="finForm.attendCount" :min="0" style="width:120px" /></el-form-item>
        <template v-if="cur && cur.form==='纪法测试'">
          <el-form-item label="应到人数"><el-input-number v-model="finForm.testTotal" :min="1" style="width:120px" /></el-form-item>
          <el-form-item label="实到人数"><el-input-number v-model="finForm.testActual" :min="0" style="width:120px" /></el-form-item>
          <el-form-item label="平均分"><el-input v-model="finForm.avgScore" placeholder="0~100" style="width:120px" /></el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button size="small" @click="finVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doFinish">提交</el-button>
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

const api = '/api/party/discipline/education'
const forms = ['集中学习','专题党课','警示教育大会','纪法测试']

const year = ref('2026')
const formType = ref('')
const status = ref('')
const rows = ref([])
const loading = ref(false)

const planVis = ref(false); const form = ref({})
const finVis = ref(false); const finForm = ref({})
const cur = ref(null)

function statusType(s) { if (s==='已完成') return 'success'; if (s==='已退回') return 'danger'; if (s==='已审批') return 'info'; return 'warning' }
async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { year: year.value||undefined, form: formType.value, status: status.value } })
    rows.value = res.data.data || []
  } finally { loading.value = false }
}
function openPlan() { form.value = { form:'警示教育大会', educationType:'警示教育', targetScope:'全体党员' }; planVis.value = true }
async function doPlan() {
  try { await axios.post(api, form.value); planVis.value=false; ElMessage.success('计划已提交'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function approve(row) {
  try {
    await ElMessageBox.confirm('是否通过该计划？','审批')
    await axios.post(`${api}/${row.id}/approve`, { approveBy: 'R06书记', pass: true })
    ElMessage.success('已审批通过'); load()
  } catch(e) { if(e!=='cancel') ElMessage.error(e.response?.data?.message || e.message) }
}
function openFinish(row) { cur.value = row; finForm.value = { attendCount: 0, testTotal: 100, testActual: 0, avgScore: null }; finVis.value = true }
async function doFinish() {
  try { await axios.post(`${api}/${cur.value.id}/finish`, finForm.value); finVis.value=false; ElMessage.success('完成登记'); load() }
  catch(e) { ElMessage.error(e.response?.data?.message || e.message) }
}
onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.mb { margin-bottom: 12px; }
</style>

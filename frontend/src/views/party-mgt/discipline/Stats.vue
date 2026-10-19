<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">六维驾驶舱 · 台账CSV导出（须二次确认 E-DI-25） · 仅 R02/R06/R20 可见</el-tag>
    </RoleBar>

    <el-row :gutter="12" class="mb">
      <el-col :span="4"><el-card shadow="hover" class="kpi"><div class="kpi-num">{{ data.petitionTotal }}</div><div class="kpi-label">信访举报</div><div class="kpi-sub">在办 {{ data.petitionPending }}</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover" class="kpi"><div class="kpi-num">{{ data.clueTotal }}</div><div class="kpi-label">问题线索</div><div class="kpi-sub">在办 {{ data.clueInProgress }}</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover" class="kpi"><div class="kpi-num">{{ data.investigationTotal }}</div><div class="kpi-label">审查案件</div><div class="kpi-sub">在办 {{ data.investigationInProgress }} 超期 {{ data.investigationOverdue }}</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover" class="kpi"><div class="kpi-num">{{ data.adjudicationTotal }}</div><div class="kpi-label">审理处分</div><div class="kpi-sub">已执行 {{ data.adjudicationExecuted }}</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover" class="kpi"><div class="kpi-num">{{ data.inInfluence }}</div><div class="kpi-label">影响期内在册</div><div class="kpi-sub">党纪处分</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover" class="kpi"><div class="kpi-num">{{ data.educationTotal }}</div><div class="kpi-label">纪律教育({{ data.educationYear }})</div><div class="kpi-sub">已完成 {{ data.educationFinished }}</div></el-card></el-col>
    </el-row>

    <el-card shadow="never" class="mb" v-if="can('R06','R20','R02')">
      <template #header>
        <div class="bar" style="margin-bottom:0">
          <b>台账导出（CSV · 含BOM）</b>
          <el-tag size="small" type="warning">导出须二次确认 E-DI-25</el-tag>
        </div>
      </template>
      <div class="bar">
        <el-select v-model="exportType" size="small" style="width:160px">
          <el-option label="信访举报台账" value="petition" />
          <el-option label="问题线索台账" value="clue" />
          <el-option label="审查案件台账" value="investigation" />
          <el-option label="审理处分台账" value="adjudication" />
          <el-option label="纪律教育台账" value="education" />
        </el-select>
        <el-input v-model="exportBy" size="small" placeholder="导出操作人" style="width:140px" />
        <el-button size="small" type="primary" @click="doExport" :loading="exporting">导出CSV</el-button>
      </div>
    </el-card>

    <el-card shadow="never">
      <template #header><b>导出审计日志</b></template>
      <el-table :data="logs" size="small" border max-height="300">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="reportName" label="报表" width="140" />
        <el-table-column prop="format" label="格式" width="70" />
        <el-table-column prop="rowCount" label="行数" width="70" />
        <el-table-column prop="exportMode" label="模式" width="80" />
        <el-table-column prop="exportBy" label="导出角色" width="100" />
        <el-table-column prop="createTime" label="导出时间" width="160">
          <template #default="{row}">{{ (row.createTime||'').replace('T',' ').slice(0,19) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/discipline/stats'
const drillApi = '/api/party/stats/drill'

const data = ref({})
const exportType = ref('petition')
const exportBy = ref('')
const exporting = ref(false)
const logs = ref([])

async function load() {
  try { const res = await axios.get(`${api}/dashboard`); data.value = res.data.data || {} } catch(e) { ElMessage.error('驾驶舱加载失败') }
}
async function loadLogs() {
  try { const res = await axios.get(`${drillApi}/export-logs`); logs.value = (res.data.data||[]).filter(l => l.reportKey && l.reportKey.startsWith('discipline-')) } catch {}
}
async function doExport() {
  if (!can('R06','R20','R02')) { ElMessage.error('当前角色无导出权限'); return }
  try {
    await ElMessageBox.confirm('导出包含敏感数据，确认继续？','二次确认(E-DI-25)',{type:'warning'})
  } catch { return }
  exporting.value = true
  try {
    const res = await axios.get(`${api}/export`, { params: { type: exportType.value, confirm: true, exportBy: exportBy.value||'system' }, responseType: 'blob' })
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = `discipline_${exportType.value}.csv`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
    loadLogs()
  } catch(e) { ElMessage.error(e.response?.data?.message || '导出失败') }
  finally { exporting.value = false }
}
onMounted(() => { load(); loadLogs() })
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.mb { margin-bottom: 12px; }
.kpi { text-align: center; }
.kpi-num { font-size: 28px; font-weight: 700; color: #1a56db; line-height: 1.2; }
.kpi-label { font-size: 13px; color: #333; margin-top: 4px; }
.kpi-sub { font-size: 12px; color: #666; margin-top: 4px; }
</style>

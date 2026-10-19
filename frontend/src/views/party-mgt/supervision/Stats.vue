<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">七维监察驾驶舱 · 台账CSV导出须二次确认(E-SV-21)并写审计 · 仅 R02/R06/R24 可导出</el-tag>
    </RoleBar>

    <el-row :gutter="10" class="mb">
      <el-col :span="6"><el-card shadow="hover" class="kpi"><div class="n">{{ data.supervisionTotal||0 }}</div><div class="l">日常监督事项</div><div class="s">问题销号率 {{ data.problemCloseRate||0 }}% · 超期 {{ data.supervisionOverdue||0 }}</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="kpi"><div class="n" :class="{orange: (data.profileCoverage||0)<95}">{{ data.profileCoverage||0 }}%</div><div class="l">廉政档案覆盖率</div><div class="s">低于95%橙色预警</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="kpi"><div class="n" :class="{red: (data.riskDueUnchecked||0)>0}">{{ data.riskDueUnchecked||0 }}</div><div class="l">风险点到期未检</div><div class="s">在册 {{ data.riskTotal||0 }} · 按期率 {{ data.riskOnTimeRate||0 }}%</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="kpi"><div class="n">{{ data.talkDoneRate||0 }}%</div><div class="l">廉政谈话完成率</div><div class="s">谈话 {{ data.talkTotal||0 }} 人次</div></el-card></el-col>
    </el-row>
    <el-row :gutter="10" class="mb">
      <el-col :span="6"><el-card shadow="hover" class="kpi"><div class="n">{{ data.inspectionFinishRate||0 }}%</div><div class="l">巡察整改完成率</div><div class="s">{{ data.inspectionTotal||0 }} 个巡察对象</div></el-card></el-col>
      <el-col :span="9">
        <el-card shadow="hover" class="kpi">
          <div class="l">{{ data.assessYear }}年度考核等次分布</div>
          <div class="grades">
            <el-tag v-for="item in gradeList" :key="item.name" size="large" :type="gradeTagType(item.name)" effect="plain">
              {{ item.name }} {{ item.count }}
            </el-tag>
            <span v-if="!gradeList.length" class="s">暂无评定结果</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="9">
        <el-card shadow="hover" class="kpi">
          <div class="l">预警一览</div>
          <div class="s">整改超期 <b :class="{'red':(data.supervisionOverdue||0)>0}">{{ data.supervisionOverdue||0 }}</b>
            ｜风险到期未检 <b :class="{'red':(data.riskDueUnchecked||0)>0}">{{ data.riskDueUnchecked||0 }}</b>
            ｜档案覆盖率 <b :class="{'orange':(data.profileCoverage||0)<95}">{{ data.profileCoverage||0 }}%</b>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="mb" v-if="can('R06','R02','R24')">
      <template #header>
        <div class="bar" style="margin-bottom:0">
          <b>监督台账导出（CSV · 含BOM）</b>
          <el-tag size="small" type="warning">导出须二次确认 E-SV-21 · 写审计日志</el-tag>
        </div>
      </template>
      <div class="bar">
        <el-select v-model="exportType" size="small" style="width:170px">
          <el-option label="日常监督台账" value="daily" />
          <el-option label="廉政档案台账" value="profile" />
          <el-option label="廉政风险点台账" value="risk" />
          <el-option label="巡察整改台账" value="inspection" />
          <el-option label="党风廉政考核台账" value="assessment" />
        </el-select>
        <el-input v-model="exportBy" size="small" placeholder="导出操作人" style="width:140px" />
        <el-button size="small" type="primary" @click="doExport" :loading="exporting">导出CSV</el-button>
      </div>
    </el-card>

    <el-card shadow="never">
      <template #header><b>导出审计日志（supervision-*）</b></template>
      <el-table :data="logs" size="small" border max-height="300">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="reportName" label="报表" width="150" />
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
import { ref, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/supervision/stats'
const drillApi = '/api/party/stats/drill'

const data = ref({})
const gradeList = computed(() => Object.entries(data.value.gradeDistribution || {}).map(([name, count]) => ({ name, count })))
function gradeTagType(g) {
  if (g==='优秀') return 'success'
  if (g==='不合格') return 'danger'
  if (g==='良好') return ''
  if (g==='合格') return 'warning'
  return 'info'
}
const exportType = ref('daily')
const exportBy = ref('')
const exporting = ref(false)
const logs = ref([])

async function load() {
  try { data.value = (await axios.get(`${api}/dashboard`)).data.data || {} } catch {}
}
async function loadLogs() {
  try { logs.value = ((await axios.get(`${drillApi}/export-logs`)).data.data||[]).filter(l => l.reportKey && l.reportKey.startsWith('supervision-')) } catch {}
}
async function doExport() {
  if (!can('R06','R02','R24')) { ElMessage.error('当前角色无导出权限'); return }
  try { await ElMessageBox.confirm('监督台账含敏感信息，确认导出？','二次确认(E-SV-21)',{type:'warning'}) } catch { return }
  exporting.value = true
  try {
    const res = await axios.get(`${api}/export`, { params: { type: exportType.value, confirm: true, exportBy: exportBy.value||'system' }, responseType: 'blob' })
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url; a.download = `supervision_${exportType.value}.csv`; a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功'); loadLogs()
  } catch(e) {
    if (e.response?.data instanceof Blob) {
      ElMessage.error('导出被阻断（E-SV-21 或无权限）')
    } else ElMessage.error(e.response?.data?.message || '导出失败')
  } finally { exporting.value = false }
}
onMounted(() => { load(); loadLogs() })
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.mb { margin-bottom: 12px; }
.kpi { text-align: center; min-height: 108px; }
.kpi .n { font-size: 28px; font-weight: 700; color: #1a56db; line-height: 1.2; }
.kpi .l { font-size: 13px; color: #333; margin-top: 4px; }
.kpi .s { font-size: 12px; color: #666; margin-top: 4px; }
.kpi .n.red, .red { color: #c45656; }
.kpi .n.orange, .orange { color: #e6a23c; }
.grades { margin-top: 8px; display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; }
</style>

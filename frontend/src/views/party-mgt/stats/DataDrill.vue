<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">逐级下钻至组织/党员/单据明细 · 敏感字段（身份证/联系方式）自动脱敏 · 导出仅 R02/R03/R04</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-select v-model="type" size="small" style="width:150px" @change="load">
          <el-option v-for="t in types" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
        <el-select v-model="orgId" placeholder="全部组织" clearable size="small" style="width:140px" @change="load">
          <el-option v-for="o in orgs" :key="o.id" :label="o.name" :value="o.id" />
        </el-select>
        <el-input v-model="keyword" placeholder="关键词" size="small" clearable style="width:160px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R02','R03','R04')" size="small" type="primary" @click="exportCsv">导出明细</el-button>
      </div>

      <el-table :data="rows" size="small" border v-loading="loading" max-height="440">
        <el-table-column v-for="col in columns" :key="col" :prop="col" :label="col" min-width="110" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ masked: isMasked(col) }">{{ row[col] ?? '—' }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="bar" style="margin-bottom:0">
          <b>导出审计日志</b>
          <el-button size="small" @click="loadLogs">刷新</el-button>
        </div>
      </template>
      <el-table :data="logs" size="small" border max-height="240">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="reportName" label="报表" width="140" />
        <el-table-column prop="format" label="格式" width="70" />
        <el-table-column prop="rowCount" label="行数" width="70" />
        <el-table-column label="模式" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.exportMode==='ASYNC'?'warning':'success'">{{ row.exportMode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maskedFields" label="脱敏字段" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.maskedFields || '—' }}</template>
        </el-table-column>
        <el-table-column prop="exportBy" label="导出角色" width="100" />
        <el-table-column prop="createTime" label="导出时间" width="160">
          <template #default="{ row }">{{ (row.createTime||'').replace('T',' ').slice(0,19) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/stats/drill'
const types = [
  { value: 'member', label: '党员明细' },
  { value: 'activity', label: '活动记录' },
  { value: 'dues', label: '党费单据' },
  { value: 'news', label: '新闻稿件' },
  { value: 'opinion', label: '舆情信息' },
  { value: 'assess', label: '考核任务' },
  { value: 'rectify', label: '整改台账' }
]
const orgs = [{ id: 1, name: '第一党支部' }, { id: 2, name: '第二党支部' }]
const sensitive = ['idCard', 'phone', 'cardNo', 'contactPhone']

const type = ref('member')
const orgId = ref(null)
const keyword = ref('')
const rows = ref([])
const logs = ref([])
const loading = ref(false)

const columns = computed(() => rows.value.length ? Object.keys(rows.value[0]) : [])
const isMasked = col => sensitive.some(s => col.toLowerCase() === s.toLowerCase())

async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { type: type.value, orgId: orgId.value, keyword: keyword.value || undefined } })
    rows.value = res.data.data || []
  } finally { loading.value = false }
}
async function loadLogs() {
  const res = await axios.get(`${api}/export-logs`)
  logs.value = res.data.data || []
}
async function exportCsv() {
  try {
    const res = await axios.get(`${api}/export`, { params: { report: type.value, orgId: orgId.value }, responseType: 'blob' })
    if (res.data.type && res.data.type.includes('json')) {
      const text = JSON.parse(await res.data.text())
      if (text.data && text.data.async) { ElMessage.warning(text.msg); loadLogs(); return }
    }
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = `${type.value}_drill.csv`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功（已记审计日志）')
    loadLogs()
  } catch {}
}
onMounted(() => { load(); loadLogs() })
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.mb { margin-bottom: 12px; }
.masked { color: #d48806; font-family: monospace; }
</style>

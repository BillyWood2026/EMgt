<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">7张预置报表 · 导出按角色控制（R02/R03/R04）· 敏感字段强制脱敏 · 超阈值转异步</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <el-tabs v-model="active" @tab-change="load">
        <el-tab-pane v-for="c in catalog" :key="c.key" :label="c.name" :name="c.key" />
      </el-tabs>
      <div class="bar">
        <span class="muted">共 {{ total }} 行</span>
        <span style="flex:1"></span>
        <el-button size="small" @click="load">刷新</el-button>
        <el-button v-if="can('R02','R03','R04')" size="small" type="primary" @click="exportCsv">导出Excel(CSV)</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="560">
        <el-table-column v-for="col in columns" :key="col" :prop="col" :label="col" min-width="110" show-overflow-tooltip>
          <template #default="{ row }">
            <template v-if="col==='result'">
              <el-tag size="small" :type="row[col]==='达标'?'success':'danger'">{{ row[col] }}</el-tag>
            </template>
            <template v-else-if="col==='grade'">
              <el-tag v-if="row[col]" size="small" :type="{ '优秀':'success','良好':'primary','合格':'warning','不合格':'danger' }[row[col]]">{{ row[col] }}</el-tag>
            </template>
            <template v-else>{{ row[col] ?? '—' }}</template>
          </template>
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

const api = '/api/party/stats/report'
const catalog = ref([])
const active = ref('member')
const rows = ref([])
const total = ref(0)
const loading = ref(false)

const columns = computed(() => rows.value.length ? Object.keys(rows.value[0]) : [])

async function loadCatalog() {
  const res = await axios.get(`${api}/catalog`)
  catalog.value = res.data.data || []
}
async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/${active.value}`)
    rows.value = res.data.data.rows || []
    total.value = res.data.data.total || 0
  } finally { loading.value = false }
}
async function exportCsv() {
  try {
    const res = await axios.get(`/api/party/stats/drill/export`, { params: { report: active.value }, responseType: 'blob' })
    if (res.data.type && res.data.type.includes('json')) {
      const text = JSON.parse(await res.data.text())
      if (text.data && text.data.async) { ElMessage.warning(text.msg); return }
    }
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = `${active.value}_report.csv`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功（已记审计日志，敏感字段已脱敏）')
  } catch {}
}
onMounted(async () => { await loadCatalog(); load() })
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 10px; align-items: center; }
.muted { color: #8a97a8; font-size: 13px; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">备份恢复：全量/增量触发 · 恢复演练 RPO≤15分钟 / RTO≤120分钟，不达标红色标记（E-SY-15）· 写操作仅 R02</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <template #header>
        <div class="bar" style="margin-bottom:0">
          <b>备份记录</b>
          <el-select v-model="q.backupType" placeholder="全部类型" clearable size="small" style="width:120px" @change="load">
            <el-option label="全量" value="全量" />
            <el-option label="增量" value="增量" />
          </el-select>
          <el-button size="small" @click="load">刷新</el-button>
          <span style="flex:1"></span>
          <template v-if="can('R02')">
            <el-button size="small" type="primary" :loading="triggering" @click="trigger('全量')">触发全量备份</el-button>
            <el-button size="small" :loading="triggering" @click="trigger('增量')">触发增量备份</el-button>
          </template>
        </div>
      </template>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="320">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.backupType === '全量' ? 'primary' : 'info'">{{ row.backupType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileName" label="备份文件" min-width="200" show-overflow-tooltip>
          <template #default="{ row }"><span class="mono">{{ row.fileName }}</span></template>
        </el-table-column>
        <el-table-column label="大小" width="100">
          <template #default="{ row }">{{ fmtSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === '成功' ? 'success' : row.status === '失败' ? 'danger' : 'warning'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column label="开始时间" width="150">
          <template #default="{ row }">{{ fmt(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="150">
          <template #default="{ row }">{{ fmt(row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="location" label="存储位置" min-width="160" show-overflow-tooltip />
      </el-table>
      <el-pagination
        v-model:current-page="q.pageNum" v-model:page-size="q.pageSize"
        :total="total" layout="total, prev, pager, next" size="small"
        style="margin-top:10px; justify-content:flex-end" @current-change="load" />
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="bar" style="margin-bottom:0">
          <b>恢复演练记录</b>
          <span style="flex:1"></span>
          <el-button v-if="can('R02')" size="small" type="primary" @click="openDrill">新增演练</el-button>
        </div>
      </template>
      <el-table :data="drills" size="small" border v-loading="drillLoading" max-height="280">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="drillDate" label="演练日期" width="110" />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column label="RPO(分钟)" width="100">
          <template #default="{ row }">
            <span :class="{ bad: row.rpoMinutes > 15 }">{{ row.rpoMinutes }}</span>
          </template>
        </el-table-column>
        <el-table-column label="RTO(分钟)" width="100">
          <template #default="{ row }">
            <span :class="{ bad: row.rtoMinutes > 120 }">{{ row.rtoMinutes }}</span>
          </template>
        </el-table-column>
        <el-table-column label="结果" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.result === '达标' ? 'success' : 'danger'">{{ row.result }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="notes" label="演练说明" min-width="180" show-overflow-tooltip />
        <el-table-column label="登记时间" width="150">
          <template #default="{ row }">{{ fmt(row.createTime) }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="drillVisible" title="新增恢复演练" width="440px" destroy-on-close>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom:10px"
        title="RPO > 15 分钟或 RTO > 120 分钟将判定不达标并触发预警（E-SY-15），但不阻断登记" />
      <el-form :model="drillForm" label-width="100px" size="small">
        <el-form-item label="演练日期" required>
          <el-date-picker v-model="drillForm.drillDate" type="date" value-format="YYYY-MM-DD" style="width:180px" />
        </el-form-item>
        <el-form-item label="RPO(分钟)" required><el-input-number v-model="drillForm.rpoMinutes" :min="0" style="width:180px" /></el-form-item>
        <el-form-item label="RTO(分钟)" required><el-input-number v-model="drillForm.rtoMinutes" :min="0" style="width:180px" /></el-form-item>
        <el-form-item label="演练说明"><el-input v-model="drillForm.notes" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="drillVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="saveDrill">登记</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/system/backup'
const rows = ref([])
const drills = ref([])
const total = ref(0)
const loading = ref(false)
const drillLoading = ref(false)
const triggering = ref(false)
const saving = ref(false)
const drillVisible = ref(false)
const q = reactive({ backupType: '', pageNum: 1, pageSize: 10 })
const drillForm = reactive({ drillDate: '', rpoMinutes: 10, rtoMinutes: 60, notes: '' })

const fmt = t => (t || '').replace('T', ' ').slice(0, 19) || '—'
const fmtSize = s => {
  if (s == null) return '—'
  if (s >= 1073741824) return (s / 1073741824).toFixed(1) + ' GB'
  if (s >= 1048576) return (s / 1048576).toFixed(1) + ' MB'
  if (s >= 1024) return (s / 1024).toFixed(1) + ' KB'
  return s + ' B'
}

async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { backupType: q.backupType || undefined, pageNum: q.pageNum, pageSize: q.pageSize } })
    rows.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  } finally { loading.value = false }
}
async function loadDrills() {
  drillLoading.value = true
  try {
    const res = await axios.get(`${api}/drill/list`)
    drills.value = res.data.data.records || res.data.data || []
  } finally { drillLoading.value = false }
}
async function trigger(type) {
  triggering.value = true
  try {
    await axios.post(`${api}/trigger`, null, { params: { type } })
    ElMessage.success(`${type}备份已触发`)
    load()
  } catch {} finally { triggering.value = false }
}
function openDrill() {
  drillForm.drillDate = new Date().toISOString().slice(0, 10)
  drillForm.rpoMinutes = 10
  drillForm.rtoMinutes = 60
  drillForm.notes = ''
  drillVisible.value = true
}
async function saveDrill() {
  saving.value = true
  try {
    const res = await axios.post(`${api}/drill`, { ...drillForm })
    if (res.data.data.warn) ElMessage.warning(res.data.data.warn)
    else ElMessage.success('演练已登记（达标）')
    drillVisible.value = false
    loadDrills()
  } catch {} finally { saving.value = false }
}
onMounted(() => { load(); loadDrills() })
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.mb { margin-bottom: 12px; }
.mono { font-family: monospace; color: #606266; }
.bad { color: #f56c6c; font-weight: 700; }
</style>

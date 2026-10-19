<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">参数配置按分类分组 · 修改留痕（E-SY-12）· 支持版本回滚（E-SY-13）· 写操作仅 R02</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-input v-model="keywordLike" placeholder="参数名/键关键词" size="small" clearable style="width:200px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
      </div>
    </el-card>

    <el-card v-for="(list, cat) in groups" :key="cat" shadow="never" class="mb">
      <template #header><b>{{ cat }}</b><span style="color:#909399; font-size:12px; margin-left:8px">{{ list.length }} 项</span></template>
      <el-table :data="list" size="small" border>
        <el-table-column prop="paramKey" label="参数键" width="200" show-overflow-tooltip>
          <template #default="{ row }"><span class="mono">{{ row.paramKey }}</span></template>
        </el-table-column>
        <el-table-column prop="paramName" label="参数名" width="160" show-overflow-tooltip />
        <el-table-column prop="paramValue" label="当前值" min-width="140" show-overflow-tooltip>
          <template #default="{ row }"><b>{{ row.paramValue }}</b></template>
        </el-table-column>
        <el-table-column prop="valueType" label="类型" width="80" />
        <el-table-column prop="version" label="版本" width="70" />
        <el-table-column prop="updatedBy" label="修改人" width="100" />
        <el-table-column label="更新时间" width="150">
          <template #default="{ row }">{{ fmt(row.updateTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('R02')" size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" link @click="openHistory(row)">历史</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editVisible" title="修改参数" width="440px" destroy-on-close>
      <el-form label-width="90px" size="small">
        <el-form-item label="参数键"><span class="mono">{{ cur.paramKey }}</span></el-form-item>
        <el-form-item label="参数名">{{ cur.paramName }}</el-form-item>
        <el-form-item label="新值" required><el-input v-model="editValue" /></el-form-item>
        <el-form-item label="变更说明"><el-input v-model="editRemark" placeholder="可选" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="editVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="historyVisible" :title="`变更历史：${cur.paramKey || ''}`" size="520px">
      <el-table :data="history" size="small" border v-loading="historyLoading">
        <el-table-column prop="version" label="版本" width="60" />
        <el-table-column prop="oldValue" label="旧值" width="90" show-overflow-tooltip />
        <el-table-column prop="newValue" label="新值" width="90" show-overflow-tooltip />
        <el-table-column prop="changedBy" label="修改人" width="90" />
        <el-table-column label="时间" width="150">
          <template #default="{ row }">{{ fmt(row.changeTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="70">
          <template #default="{ row }">
            <el-button v-if="can('R02')" size="small" link type="warning" @click="rollback(row)">回滚</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/system/param'
const rows = ref([])
const keywordLike = ref('')
const editVisible = ref(false)
const historyVisible = ref(false)
const historyLoading = ref(false)
const saving = ref(false)
const cur = ref({})
const editValue = ref('')
const editRemark = ref('')
const history = ref([])

const fmt = t => (t || '').replace('T', ' ').slice(0, 19) || '—'
const groups = computed(() => {
  const g = {}
  for (const r of rows.value) { (g[r.category || '未分类'] = g[r.category || '未分类'] || []).push(r) }
  return g
})

async function load() {
  const res = await axios.get(`${api}/list`, { params: { keywordLike: keywordLike.value || undefined } })
  rows.value = res.data.data || []
}
function openEdit(row) { cur.value = row; editValue.value = row.paramValue; editRemark.value = ''; editVisible.value = true }
async function save() {
  saving.value = true
  try {
    await axios.put(`${api}/${encodeURIComponent(cur.value.paramKey)}`, { paramValue: editValue.value }, { params: { remark: editRemark.value || undefined } })
    ElMessage.success('已保存并留痕')
    editVisible.value = false
    load()
  } catch {} finally { saving.value = false }
}
async function openHistory(row) {
  cur.value = row
  historyVisible.value = true
  historyLoading.value = true
  try {
    const res = await axios.get(`${api}/${encodeURIComponent(row.paramKey)}/history`)
    history.value = res.data.data || []
  } finally { historyLoading.value = false }
}
async function rollback(h) {
  await ElMessageBox.confirm(`确认将「${cur.value.paramKey}」回滚到版本 v${h.version}（值=${h.oldValue}）？`, '回滚确认', { type: 'warning' })
  await axios.post(`${api}/${encodeURIComponent(cur.value.paramKey)}/rollback`, null, { params: { version: h.version } })
  ElMessage.success('已回滚')
  historyVisible.value = false
  load()
}
onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.mb { margin-bottom: 12px; }
.mono { font-family: monospace; color: #606266; }
</style>

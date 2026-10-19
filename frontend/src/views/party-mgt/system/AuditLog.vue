<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">审计日志只读，禁止修改/删除（E-SY-14）· 查询权限仅 R02/R03/R04</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="q.opType" placeholder="全部类型" clearable size="small" style="width:120px">
          <el-option label="操作" value="操作" />
          <el-option label="敏感" value="敏感" />
          <el-option label="登录" value="登录" />
        </el-select>
        <el-input v-model="q.operator" placeholder="操作人" size="small" clearable style="width:120px" @keyup.enter="load" />
        <el-input v-model="q.actionLike" placeholder="动作关键词" size="small" clearable style="width:140px" @keyup.enter="load" />
        <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" size="small" style="width:240px" />
        <el-button size="small" @click="load">查询</el-button>
      </div>

      <el-table :data="rows" size="small" border v-loading="loading" row-key="id">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-box">
              <p><b>变更前：</b><span class="mono">{{ row.beforeValue || '—' }}</span></p>
              <p><b>变更后：</b><span class="mono">{{ row.afterValue || '—' }}</span></p>
              <p><b>明细：</b>{{ row.detail || '—' }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.opType === '敏感' ? 'danger' : row.opType === '登录' ? 'warning' : 'info'">{{ row.opType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="110" />
        <el-table-column prop="roleCode" label="角色" width="80" />
        <el-table-column prop="action" label="动作" width="140" show-overflow-tooltip />
        <el-table-column prop="targetType" label="对象类型" width="130" />
        <el-table-column prop="targetId" label="对象ID" width="90" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="120" show-overflow-tooltip />
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ fmt(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="q.pageNum" v-model:page-size="q.pageSize"
        :total="total" layout="total, prev, pager, next" size="small"
        style="margin-top:10px; justify-content:flex-end" @current-change="load" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import RoleBar from '../PartyRoleBar.vue'

const api = '/api/party/system/audit'
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const range = ref([])
const q = reactive({ opType: '', operator: '', actionLike: '', pageNum: 1, pageSize: 10 })

const fmt = t => (t || '').replace('T', ' ').slice(0, 19) || '—'

async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, {
      params: {
        opType: q.opType || undefined,
        operator: q.operator || undefined,
        actionLike: q.actionLike || undefined,
        dateFrom: range.value && range.value[0] || undefined,
        dateTo: range.value && range.value[1] || undefined,
        pageNum: q.pageNum, pageSize: q.pageSize
      }
    })
    rows.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  } finally { loading.value = false }
}
onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.expand-box { padding: 8px 16px; font-size: 12px; }
.expand-box p { margin: 4px 0; }
.mono { font-family: monospace; color: #606266; }
</style>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>回调日志</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索关键词" clearable style="width:200px;margin-right:8px" @keyup.enter="loadData" />
        <el-button @click="loadData">搜索</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="instId" label="实例ID" width="100" />
      <el-table-column prop="bizCode" label="业务编码" />
      <el-table-column prop="bizId" label="业务ID" />
      <el-table-column prop="event" label="事件" width="110">
        <template #default="{row}">
          <el-tag :type="eventTag(row.event)">{{ eventText(row.event) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="payload" label="负载" show-overflow-tooltip />
      <el-table-column prop="callbackTime" label="回调时间" width="160" />
      <el-table-column prop="status" label="状态" width="90" />
    </el-table>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { flowApi } from '@/api/flow'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const keyword = ref('')

function eventTag(e) {
  return { approved: 'success', rejected: 'danger', withdrawn: 'info', terminated: 'danger' }[e] || 'info'
}
function eventText(e) {
  return { approved: '已通过', rejected: '已驳回', withdrawn: '已撤回', terminated: '已终止' }[e] || e
}

async function loadData() {
  loading.value = true
  try {
    const res = await flowApi.callback.list(keyword.value)
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

onMounted(loadData)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; gap: 8px; flex-wrap: wrap; }
</style>

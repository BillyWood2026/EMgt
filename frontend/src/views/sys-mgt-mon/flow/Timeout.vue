<template>
  <div class="page-container">
    <div class="page-header">
      <h2>超时提醒</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索处理人" clearable style="width:200px;margin-right:8px" @keyup.enter="loadData" />
        <el-button @click="loadData">搜索</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="taskId" label="任务ID" width="100" />
      <el-table-column prop="instId" label="实例ID" width="100" />
      <el-table-column prop="assignee" label="处理人" />
      <el-table-column prop="timeoutHours" label="超时(小时)" width="100" />
      <el-table-column prop="remindTime" label="提醒时间" width="160" />
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

async function loadData() {
  loading.value = true
  try {
    const res = await flowApi.timeout.list(keyword.value)
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

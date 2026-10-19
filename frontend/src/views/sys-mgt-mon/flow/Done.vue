<template>
  <div class="page-container">
    <div class="page-header">
      <h2>已办任务</h2>
      <div>
        <el-input v-model="assignee" placeholder="请输入处理人" clearable style="width:200px;margin-right:8px" @keyup.enter="loadData" />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="instId" label="实例ID" width="100" />
      <el-table-column prop="nodeKey" label="节点Key" />
      <el-table-column prop="nodeName" label="节点名称" />
      <el-table-column prop="assignee" label="处理人" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="comment" label="处理意见" show-overflow-tooltip />
      <el-table-column prop="handleTime" label="处理时间" width="160" />
    </el-table>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { flowApi } from '@/api/flow'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const assignee = ref('')

async function loadData() {
  if (!assignee.value) { ElMessage.warning('请输入处理人'); return }
  loading.value = true
  try {
    const res = await flowApi.task.done(assignee.value)
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; gap: 8px; flex-wrap: wrap; }
</style>

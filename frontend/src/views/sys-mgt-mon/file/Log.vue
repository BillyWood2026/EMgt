<template>
  <div class="page-container">
    <div class="page-header">
      <h2>操作日志</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索关键词" clearable style="width:200px;margin-right:8px" @keyup.enter="loadData" />
        <el-button style="margin-right:8px" @click="loadData">搜索</el-button>

      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="fileId" label="文件ID" />
      <el-table-column prop="opType" label="操作类型" />
      <el-table-column prop="operator" label="操作人" />
      <el-table-column prop="ip" label="IP地址" />
      <el-table-column prop="opTime" label="操作时间" />
      <el-table-column prop="detail" label="详情" />
    </el-table>

  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { fileApi } from '@/api/platform'
import { ElMessage } from 'element-plus'

const api = fileApi.log
const loading = ref(false)
const tableData = ref([])
const keyword = ref('')

async function loadData() {
  loading.value = true
  try {
    const res = await api.list(keyword.value)
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

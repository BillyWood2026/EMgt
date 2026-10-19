<template>
  <div class="page-container">
    <div class="page-header">
      <h2>抄送我的</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索抄送人" clearable style="width:200px;margin-right:8px" @keyup.enter="loadData" />
        <el-button @click="loadData">搜索</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="instId" label="实例ID" width="100" />
      <el-table-column prop="ccUser" label="抄送人" />
      <el-table-column prop="readFlag" label="已读" width="80">
        <template #default="{row}">
          <el-tag :type="row.readFlag === 1 ? 'success' : 'info'">{{ row.readFlag === 1 ? '已读' : '未读' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
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
    const res = await flowApi.cc.list(keyword.value)
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

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>待办任务</h2>
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
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleApprove(row)">审批</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="审批" width="500px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="操作">
          <el-radio-group v-model="formData.action">
            <el-radio value="approve">同意</el-radio>
            <el-radio value="reject">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="意见">
          <el-input v-model="formData.comment" type="textarea" :rows="4" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive } from 'vue'
import { flowApi } from '@/api/flow'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const assignee = ref('')
const dialogVisible = ref(false)
const formData = reactive({
  id: null,
  action: 'approve',
  comment: ''
})

async function loadData() {
  if (!assignee.value) { ElMessage.warning('请输入处理人'); return }
  loading.value = true
  try {
    const res = await flowApi.task.todo(assignee.value)
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleApprove(row) {
  formData.id = row.id
  formData.action = 'approve'
  formData.comment = ''
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    const res = await flowApi.task.complete(formData.id, { action: formData.action, comment: formData.comment })
    if (res.data.code === 200) {
      ElMessage.success('提交成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message || '提交失败')
    }
  } catch (e) { ElMessage.error('提交失败') }
}
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; gap: 8px; flex-wrap: wrap; }
</style>

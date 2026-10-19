<template>
  <div class="page-container">
    <div class="page-header">
      <h2>消息统计</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索关键词" clearable style="width:200px;margin-right:8px" @keyup.enter="loadData" />
        <el-button style="margin-right:8px" @click="loadData">搜索</el-button>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="statDate" label="统计日期" />
      <el-table-column prop="channel" label="渠道" />
      <el-table-column prop="sendCount" label="发送数" />
      <el-table-column prop="successCount" label="成功数" />
      <el-table-column prop="failCount" label="失败数" />
      <el-table-column prop="successRate" label="成功率" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="统计日期">
          <el-date-picker v-model="formData.statDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择统计日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="渠道">
          <el-select v-model="formData.channel" placeholder="请选择渠道" style="width:100%">
            <el-option label="站内信" value="站内信" />
            <el-option label="短信" value="短信" />
            <el-option label="邮件" value="邮件" />
            <el-option label="企业微信" value="企业微信" />
            <el-option label="Webhook" value="Webhook" />
          </el-select>
        </el-form-item>
        <el-form-item label="发送数">
          <el-input-number v-model="formData.sendCount" style="width:100%" :controls="false" />
        </el-form-item>
        <el-form-item label="成功数">
          <el-input-number v-model="formData.successCount" style="width:100%" :controls="false" />
        </el-form-item>
        <el-form-item label="失败数">
          <el-input-number v-model="formData.failCount" style="width:100%" :controls="false" />
        </el-form-item>
        <el-form-item label="成功率(%)">
          <el-input-number v-model="formData.successRate" style="width:100%" :controls="false" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { messageApi } from '@/api/platform'
import { ElMessage, ElMessageBox } from 'element-plus'

const api = messageApi.stat
const loading = ref(false)
const tableData = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增消息统计')
const formData = reactive({
  id: null,
  statDate: '',
  channel: '',
  sendCount: null,
  successCount: null,
  failCount: null,
  successRate: null
})

function resetForm() {
  formData.id = null
  formData.statDate = ''
  formData.channel = ''
  formData.sendCount = null
  formData.successCount = null
  formData.failCount = null
  formData.successRate = null
}

async function loadData() {
  loading.value = true
  try {
    const res = await api.list(keyword.value)
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增消息统计'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑消息统计'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该条记录？', '提示', { type: 'warning' })
    const res = await api.remove(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await api.update(formData.id, formData) : await api.save(formData)
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message || '保存失败')
    }
  } catch (e) { ElMessage.error('保存失败') }
}

onMounted(loadData)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; gap: 8px; flex-wrap: wrap; }
</style>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>流程实例</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索关键词" clearable style="width:200px;margin-right:8px" @keyup.enter="loadData" />
        <el-button style="margin-right:8px" @click="loadData">搜索</el-button>
        <el-button type="primary" @click="handleStart">发起流程</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="instNo" label="实例编号" width="180" />
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column prop="defCode" label="定义编码" />
      <el-table-column prop="bizCode" label="业务编码" />
      <el-table-column prop="bizId" label="业务ID" />
      <el-table-column prop="initiator" label="发起人" width="90" />
      <el-table-column prop="currentNode" label="当前节点" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="发起时间" width="160" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{row}">
          <el-button v-if="row.status==='running'" size="small" type="warning" @click="handleWithdraw(row)">撤回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="发起流程" width="700px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="流程定义">
          <el-select v-model="formData.defCode" placeholder="请选择流程定义" style="width:100%">
            <el-option v-for="d in defOptions" :key="d.defCode" :label="d.defName + '(' + d.defCode + ')'" :value="d.defCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务编码">
          <el-input v-model="formData.bizCode" placeholder="请输入业务编码" />
        </el-form-item>
        <el-form-item label="业务ID">
          <el-input v-model="formData.bizId" placeholder="请输入业务ID" />
        </el-form-item>
        <el-form-item label="业务数据">
          <el-input v-model="formData.bizJson" type="textarea" :rows="5" placeholder="请输入业务数据 JSON" />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="formData.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="发起人">
          <el-input v-model="formData.initiator" placeholder="请输入发起人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">发起</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { flowApi } from '@/api/flow'
import { ElMessage, ElMessageBox } from 'element-plus'

const api = flowApi.instance
const loading = ref(false)
const tableData = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const defOptions = ref([])
const formData = reactive({
  defCode: '',
  bizCode: '',
  bizId: '',
  bizJson: '',
  title: '',
  initiator: ''
})

function statusTag(s) {
  return { running: 'primary', approved: 'success', rejected: 'danger', withdrawn: 'info', terminated: 'danger' }[s] || 'info'
}
function statusText(s) {
  return { running: '运行中', approved: '已通过', rejected: '已驳回', withdrawn: '已撤回', terminated: '已终止' }[s] || s
}

function resetForm() {
  formData.defCode = ''
  formData.bizCode = ''
  formData.bizId = ''
  formData.bizJson = ''
  formData.title = ''
  formData.initiator = ''
}

async function loadDefs() {
  try {
    const res = await flowApi.definition.list('')
    if (res.data.code === 200) defOptions.value = (res.data.data || []).filter(d => d.status === 'published')
  } catch (e) {}
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

function handleStart() {
  resetForm()
  dialogVisible.value = true
}

async function handleWithdraw(row) {
  try {
    await ElMessageBox.confirm('确认撤回该流程实例？', '提示', { type: 'warning' })
    const res = await api.withdraw(row.id, row.initiator || '')
    if (res.data.code === 200) { ElMessage.success('撤回成功'); loadData() }
    else ElMessage.error(res.data.message || '撤回失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = await api.start(formData)
    if (res.data.code === 200) {
      ElMessage.success('发起成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message || '发起失败')
    }
  } catch (e) { ElMessage.error('发起失败') }
}

onMounted(() => { loadData(); loadDefs() })
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; gap: 8px; flex-wrap: wrap; }
</style>

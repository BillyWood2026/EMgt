<template>
  <div class="page-container">
    <div class="page-header">
      <h2>节点配置</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索关键词" clearable style="width:200px;margin-right:8px" @keyup.enter="loadData" />
        <el-button style="margin-right:8px" @click="loadData">搜索</el-button>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="defId" label="流程定义" width="160">
        <template #default="{row}">{{ defNameOf(row.defId) }}</template>
      </el-table-column>
      <el-table-column prop="nodeKey" label="节点Key" />
      <el-table-column prop="nodeName" label="节点名称" />
      <el-table-column prop="nodeType" label="节点类型" width="100">
        <template #default="{row}">{{ nodeTypeText(row.nodeType) }}</template>
      </el-table-column>
      <el-table-column prop="approverType" label="审批人类型" width="110">
        <template #default="{row}">{{ approverTypeText(row.approverType) }}</template>
      </el-table-column>
      <el-table-column prop="approverValue" label="审批人值" show-overflow-tooltip />
      <el-table-column prop="timeoutHours" label="超时(小时)" width="90" />
      <el-table-column prop="sortOrder" label="排序" width="70" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="流程定义">
          <el-select v-model="formData.defId" placeholder="请选择流程定义" style="width:100%">
            <el-option v-for="d in defOptions" :key="d.id" :label="d.defName + '(' + d.defCode + ')'" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="节点Key">
          <el-input v-model="formData.nodeKey" placeholder="请输入节点Key" />
        </el-form-item>
        <el-form-item label="节点名称">
          <el-input v-model="formData.nodeName" placeholder="请输入节点名称" />
        </el-form-item>
        <el-form-item label="节点类型">
          <el-select v-model="formData.nodeType" placeholder="请选择节点类型" style="width:100%">
            <el-option label="开始" value="start" />
            <el-option label="审批" value="approval" />
            <el-option label="条件" value="condition" />
            <el-option label="结束" value="end" />
            <el-option label="抄送" value="cc" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批人类型">
          <el-select v-model="formData.approverType" placeholder="请选择审批人类型" style="width:100%">
            <el-option label="用户" value="user" />
            <el-option label="角色" value="role" />
            <el-option label="部门负责人" value="deptLeader" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批人值">
          <el-input v-model="formData.approverValue" placeholder="请输入审批人值" />
        </el-form-item>
        <el-form-item label="超时(小时)">
          <el-input-number v-model="formData.timeoutHours" style="width:100%" :controls="false" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" style="width:100%" :controls="false" />
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
import { flowApi } from '@/api/flow'
import { ElMessage, ElMessageBox } from 'element-plus'

const api = flowApi.node
const loading = ref(false)
const tableData = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增节点')
const defOptions = ref([])
const formData = reactive({
  id: null,
  defId: null,
  nodeKey: '',
  nodeName: '',
  nodeType: 'approval',
  approverType: 'user',
  approverValue: '',
  timeoutHours: null,
  sortOrder: null
})

function nodeTypeText(t) {
  return { start: '开始', approval: '审批', condition: '条件', end: '结束', cc: '抄送' }[t] || t
}
function approverTypeText(t) {
  return { user: '用户', role: '角色', deptLeader: '部门负责人' }[t] || t
}
function defNameOf(id) {
  const d = defOptions.value.find(x => x.id === id)
  return d ? d.defName : id
}

function resetForm() {
  formData.id = null
  formData.defId = null
  formData.nodeKey = ''
  formData.nodeName = ''
  formData.nodeType = 'approval'
  formData.approverType = 'user'
  formData.approverValue = ''
  formData.timeoutHours = null
  formData.sortOrder = null
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

function handleAdd() {
  dialogTitle.value = '新增节点'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑节点'
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

onMounted(() => { loadData(); loadDefs() })
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; gap: 8px; flex-wrap: wrap; }
</style>

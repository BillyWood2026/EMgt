<template>
  <div class="page-container">
    <div class="page-header">
      <h2>流程定义</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索关键词" clearable style="width:200px;margin-right:8px" @keyup.enter="loadData" />
        <el-button style="margin-right:8px" @click="loadData">搜索</el-button>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="defCode" label="定义编码" />
      <el-table-column prop="defName" label="定义名称" />
      <el-table-column prop="category" label="分类" />
      <el-table-column prop="version" label="版本" width="70" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{row}">
          <el-button v-if="row.status==='draft'" size="small" type="success" @click="handlePublish(row)">发布</el-button>
          <el-button v-if="row.status==='published'" size="small" type="warning" @click="handleSuspend(row)">停用</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="定义编码">
          <el-input v-model="formData.defCode" placeholder="请输入定义编码" />
        </el-form-item>
        <el-form-item label="定义名称">
          <el-input v-model="formData.defName" placeholder="请输入定义名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="formData.category" placeholder="请输入分类" />
        </el-form-item>
        <el-form-item label="版本">
          <el-input-number v-model="formData.version" style="width:100%" :controls="false" />
        </el-form-item>
        <el-form-item label="BPMN XML">
          <el-input v-model="formData.bpmnXml" type="textarea" :rows="6" placeholder="请输入 BPMN XML" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width:100%">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="停用" value="suspended" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" placeholder="请输入备注" />
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

const api = flowApi.definition
const loading = ref(false)
const tableData = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增流程定义')
const formData = reactive({
  id: null,
  defCode: '',
  defName: '',
  category: '',
  version: 1,
  bpmnXml: '',
  status: 'draft',
  remark: ''
})

function statusTag(s) {
  return { draft: 'info', published: 'success', suspended: 'warning' }[s] || 'info'
}
function statusText(s) {
  return { draft: '草稿', published: '已发布', suspended: '停用' }[s] || s
}

function resetForm() {
  formData.id = null
  formData.defCode = ''
  formData.defName = ''
  formData.category = ''
  formData.version = 1
  formData.bpmnXml = ''
  formData.status = 'draft'
  formData.remark = ''
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
  dialogTitle.value = '新增流程定义'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑流程定义'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handlePublish(row) {
  try {
    const res = await api.publish(row.id)
    if (res.data.code === 200) { ElMessage.success('发布成功'); loadData() }
    else ElMessage.error(res.data.message || '发布失败')
  } catch (e) { ElMessage.error('发布失败') }
}

async function handleSuspend(row) {
  try {
    await ElMessageBox.confirm('确认停用该流程定义？', '提示', { type: 'warning' })
    const res = await api.suspend(row.id)
    if (res.data.code === 200) { ElMessage.success('停用成功'); loadData() }
    else ElMessage.error(res.data.message || '停用失败')
  } catch (e) {}
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

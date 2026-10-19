<template>
  <div class="page-container">
    <div class="page-header">
      <h2>数据集</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索关键词" clearable style="width:200px;margin-right:8px" @keyup.enter="loadData" />
        <el-button style="margin-right:8px" @click="loadData">搜索</el-button>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="datasetCode" label="数据集编码" />
      <el-table-column prop="datasetName" label="数据集名称" />
      <el-table-column prop="datasourceId" label="数据源ID" />
      <el-table-column prop="fieldDesc" label="字段说明" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="数据集编码">
          <el-input v-model="formData.datasetCode" placeholder="请输入数据集编码" />
        </el-form-item>
        <el-form-item label="数据集名称">
          <el-input v-model="formData.datasetName" placeholder="请输入数据集名称" />
        </el-form-item>
        <el-form-item label="数据源ID">
          <el-input-number v-model="formData.datasourceId" style="width:100%" :controls="false" />
        </el-form-item>
        <el-form-item label="模型定义">
          <el-input v-model="formData.modelDef" type="textarea" :rows="3" placeholder="请输入模型定义" />
        </el-form-item>
        <el-form-item label="字段说明">
          <el-input v-model="formData.fieldDesc" type="textarea" :rows="3" placeholder="请输入字段说明" />
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
import { biApi } from '@/api/platform'
import { ElMessage, ElMessageBox } from 'element-plus'

const api = biApi.dataset
const loading = ref(false)
const tableData = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增数据集')
const formData = reactive({
  id: null,
  datasetCode: '',
  datasetName: '',
  datasourceId: null,
  modelDef: '',
  fieldDesc: ''
})

function resetForm() {
  formData.id = null
  formData.datasetCode = ''
  formData.datasetName = ''
  formData.datasourceId = null
  formData.modelDef = ''
  formData.fieldDesc = ''
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
  dialogTitle.value = '新增数据集'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑数据集'
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

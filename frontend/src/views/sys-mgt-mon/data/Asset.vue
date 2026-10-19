<template>
  <div class="page-container">
    <div class="page-header">
      <h2>数据资产</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索关键词" clearable style="width:200px;margin-right:8px" @keyup.enter="loadData" />
        <el-button style="margin-right:8px" @click="loadData">搜索</el-button>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="assetName" label="资产名称" />
      <el-table-column prop="category" label="分类" />
      <el-table-column prop="owner" label="负责人" />
      <el-table-column prop="secretLevel" label="密级" />
      <el-table-column prop="lineageDesc" label="血缘说明" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="资产名称">
          <el-input v-model="formData.assetName" placeholder="请输入资产名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="formData.category" placeholder="请输入分类" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="formData.owner" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="密级">
          <el-select v-model="formData.secretLevel" placeholder="请选择密级" style="width:100%">
            <el-option label="公开" value="公开" />
            <el-option label="内部" value="内部" />
            <el-option label="秘密" value="秘密" />
            <el-option label="机密" value="机密" />
          </el-select>
        </el-form-item>
        <el-form-item label="血缘说明">
          <el-input v-model="formData.lineageDesc" type="textarea" :rows="3" placeholder="请输入血缘说明" />
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
import { dataApi } from '@/api/platform'
import { ElMessage, ElMessageBox } from 'element-plus'

const api = dataApi.asset
const loading = ref(false)
const tableData = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增数据资产')
const formData = reactive({
  id: null,
  assetName: '',
  category: '',
  owner: '',
  secretLevel: '',
  lineageDesc: ''
})

function resetForm() {
  formData.id = null
  formData.assetName = ''
  formData.category = ''
  formData.owner = ''
  formData.secretLevel = ''
  formData.lineageDesc = ''
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
  dialogTitle.value = '新增数据资产'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑数据资产'
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

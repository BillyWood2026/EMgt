<template>
  <div class="page-container">
    <div class="page-header">
      <h2>附件绑定</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索关键词" clearable style="width:200px;margin-right:8px" @keyup.enter="loadData" />
        <el-button style="margin-right:8px" @click="loadData">搜索</el-button>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="serviceCode" label="业务服务" />
      <el-table-column prop="businessBillId" label="业务单据ID" />
      <el-table-column prop="fileId" label="文件ID" />
      <el-table-column prop="readOnly" label="只读" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="业务服务">
          <el-input v-model="formData.serviceCode" placeholder="请输入业务服务" />
        </el-form-item>
        <el-form-item label="业务单据ID">
          <el-input v-model="formData.businessBillId" placeholder="请输入业务单据ID" />
        </el-form-item>
        <el-form-item label="文件ID">
          <el-input-number v-model="formData.fileId" style="width:100%" :controls="false" />
        </el-form-item>
        <el-form-item label="只读">
          <el-select v-model="formData.readOnly" placeholder="请选择只读" style="width:100%">
            <el-option label="否" :value="0" />
            <el-option label="是" :value="1" />
          </el-select>
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
import { fileApi } from '@/api/platform'
import { ElMessage, ElMessageBox } from 'element-plus'

const api = fileApi.attachment
const loading = ref(false)
const tableData = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增附件绑定')
const formData = reactive({
  id: null,
  serviceCode: '',
  businessBillId: '',
  fileId: null,
  readOnly: ''
})

function resetForm() {
  formData.id = null
  formData.serviceCode = ''
  formData.businessBillId = ''
  formData.fileId = null
  formData.readOnly = ''
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
  dialogTitle.value = '新增附件绑定'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑附件绑定'
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

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>指令下发</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索关键词" clearable style="width:200px;margin-right:8px" @keyup.enter="loadData" />
        <el-button style="margin-right:8px" @click="loadData">搜索</el-button>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="deviceCode" label="设备编码" />
      <el-table-column prop="commandType" label="指令类型" />
      <el-table-column prop="paramsJson" label="参数JSON" />
      <el-table-column prop="issueTime" label="下发时间" />
      <el-table-column prop="execStatus" label="执行状态" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="warning" @click="handleExecute(row)">执行</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="设备编码">
          <el-input v-model="formData.deviceCode" placeholder="请输入设备编码" />
        </el-form-item>
        <el-form-item label="指令类型">
          <el-input v-model="formData.commandType" placeholder="请输入指令类型" />
        </el-form-item>
        <el-form-item label="参数JSON">
          <el-input v-model="formData.paramsJson" type="textarea" :rows="3" placeholder="请输入参数JSON" />
        </el-form-item>
        <el-form-item label="下发时间">
          <el-date-picker v-model="formData.issueTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择下发时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="执行状态">
          <el-select v-model="formData.execStatus" placeholder="请选择执行状态" style="width:100%">
            <el-option label="已下发" value="已下发" />
            <el-option label="已执行" value="已执行" />
            <el-option label="失败" value="失败" />
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
import { iotApi } from '@/api/platform'
import { ElMessage, ElMessageBox } from 'element-plus'

const api = iotApi.command
const loading = ref(false)
const tableData = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增指令下发')
const formData = reactive({
  id: null,
  deviceCode: '',
  commandType: '',
  paramsJson: '',
  issueTime: '',
  execStatus: ''
})

function resetForm() {
  formData.id = null
  formData.deviceCode = ''
  formData.commandType = ''
  formData.paramsJson = ''
  formData.issueTime = ''
  formData.execStatus = ''
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
  dialogTitle.value = '新增指令下发'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑指令下发'
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

async function handleExecute(row) {
  try {
    const res = await api.execute(row.id)
    if (res.data.code === 200) { ElMessage.success('执行成功'); loadData() }
    else ElMessage.error(res.data.message || '执行失败')
  } catch (e) { ElMessage.error('执行失败') }
}

onMounted(loadData)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; gap: 8px; flex-wrap: wrap; }
</style>

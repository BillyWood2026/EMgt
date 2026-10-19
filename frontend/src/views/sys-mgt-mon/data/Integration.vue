<template>
  <div class="page-container">
    <div class="page-header">
      <h2>数据集成</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索关键词" clearable style="width:200px;margin-right:8px" @keyup.enter="loadData" />
        <el-button style="margin-right:8px" @click="loadData">搜索</el-button>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="taskName" label="任务名称" />
      <el-table-column prop="sourceDb" label="源库" />
      <el-table-column prop="targetDb" label="目标库" />
      <el-table-column prop="syncMode" label="同步模式" />
      <el-table-column prop="scheduleCycle" label="调度周期" />
      <el-table-column prop="lastExecStatus" label="最近状态" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="任务名称">
          <el-input v-model="formData.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="源库">
          <el-input v-model="formData.sourceDb" placeholder="请输入源库" />
        </el-form-item>
        <el-form-item label="目标库">
          <el-input v-model="formData.targetDb" placeholder="请输入目标库" />
        </el-form-item>
        <el-form-item label="同步模式">
          <el-select v-model="formData.syncMode" placeholder="请选择同步模式" style="width:100%">
            <el-option label="全量" value="全量" />
            <el-option label="增量" value="增量" />
          </el-select>
        </el-form-item>
        <el-form-item label="调度周期">
          <el-input v-model="formData.scheduleCycle" placeholder="请输入调度周期" />
        </el-form-item>
        <el-form-item label="最近状态">
          <el-input v-model="formData.lastExecStatus" placeholder="请输入最近状态" />
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

const api = dataApi.integration
const loading = ref(false)
const tableData = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增数据集成')
const formData = reactive({
  id: null,
  taskName: '',
  sourceDb: '',
  targetDb: '',
  syncMode: '',
  scheduleCycle: '',
  lastExecStatus: ''
})

function resetForm() {
  formData.id = null
  formData.taskName = ''
  formData.sourceDb = ''
  formData.targetDb = ''
  formData.syncMode = ''
  formData.scheduleCycle = ''
  formData.lastExecStatus = ''
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
  dialogTitle.value = '新增数据集成'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑数据集成'
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

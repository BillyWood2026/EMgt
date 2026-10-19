<template>
  <div class="page-container">
    <div class="page-header">
      <h2>任务进度监控</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="taskId" label="任务ID" width="100" />
      <el-table-column prop="reportPeriod" label="报告周期" width="120" />
      <el-table-column prop="plannedProgress" label="计划进度" width="100" />
      <el-table-column prop="actualProgress" label="实际进度" width="100" />
      <el-table-column prop="progressDeviation" label="偏差" width="100" />
      <el-table-column prop="progressStatus" label="状态" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="任务ID">
          <el-input-number v-model="formData.taskId" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="报告周期">
          <el-input v-model="formData.reportPeriod" placeholder="请输入报告周期" />
        </el-form-item>
        <el-form-item label="计划进度%">
          <el-input-number v-model="formData.plannedProgress" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="实际进度%">
          <el-input-number v-model="formData.actualProgress" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="完成说明">
          <el-input v-model="formData.completionNote" type="textarea" :rows="3" placeholder="请输入完成说明" />
        </el-form-item>
        <el-form-item label="风险说明">
          <el-input v-model="formData.riskNote" type="textarea" :rows="3" placeholder="请输入风险说明" />
        </el-form-item>
        <el-form-item label="整改措施">
          <el-input v-model="formData.rectifyMeasure" type="textarea" :rows="3" placeholder="请输入整改措施" />
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
import { blmApi, dsteApi, sixsApi, budgetApi, workplanApi } from '@/api/strategy'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增任务进度监控')
const formData = reactive({
  id: null,
  taskId: null,
  reportPeriod: "",
  plannedProgress: null,
  actualProgress: null,
  completionNote: "",
  riskNote: "",
  rectifyMeasure: "",
})

function resetForm() {
  Object.keys(formData).forEach(k => {
    if (k === 'id') formData[k] = null
    else if (typeof formData[k] === 'number') formData[k] = null
    else formData[k] = ''
  })
}

async function loadData() {
  loading.value = true
  try {
    const res = await dsteApi.listProgress()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增任务进度监控'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑任务进度监控'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await dsteApi.deleteProgress(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await dsteApi.updateProgress(formData.id, formData) : await dsteApi.saveProgress(formData)
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
.page-header { display: flex; justify-content: space-between; align-items: center; }
</style>

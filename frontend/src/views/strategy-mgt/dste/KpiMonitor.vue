<template>
  <div class="page-container">
    <div class="page-header">
      <h2>KPI实时监控</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="kpiId" label="KPI ID" width="100" />
      <el-table-column prop="monitorPeriod" label="监控周期" width="120" />
      <el-table-column prop="targetValue" label="目标值" width="110" />
      <el-table-column prop="actualValue" label="实际值" width="110" />
      <el-table-column prop="achievementRate" label="达成率%" width="100" />
      <el-table-column prop="kpiStatus" label="状态" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="KPI ID">
          <el-input-number v-model="formData.kpiId" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="监控周期">
          <el-input v-model="formData.monitorPeriod" placeholder="请输入监控周期" />
        </el-form-item>
        <el-form-item label="目标值">
          <el-input-number v-model="formData.targetValue" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="实际值">
          <el-input-number v-model="formData.actualValue" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="异常原因">
          <el-input v-model="formData.anomalyReason" type="textarea" :rows="3" placeholder="请输入异常原因" />
        </el-form-item>
        <el-form-item label="整改方案">
          <el-input v-model="formData.rectifyPlan" type="textarea" :rows="3" placeholder="请输入整改方案" />
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
const dialogTitle = ref('新增KPI实时监控')
const formData = reactive({
  id: null,
  kpiId: null,
  monitorPeriod: "",
  targetValue: null,
  actualValue: null,
  anomalyReason: "",
  rectifyPlan: "",
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
    const res = await dsteApi.listKpiMonitors()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增KPI实时监控'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑KPI实时监控'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await dsteApi.deleteKpiMonitor(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await dsteApi.updateKpiMonitor(formData.id, formData) : await dsteApi.saveKpiMonitor(formData)
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

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>S3管理报告监控</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="monitorCode" label="报告编码" width="120" />
      <el-table-column prop="reportPeriod" label="报告周期" width="120" />
      <el-table-column prop="reportType" label="报告类型" width="100" />
      <el-table-column prop="businessPerf" label="经营业绩" width="240" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="报告编码">
          <el-input v-model="formData.monitorCode" placeholder="请输入报告编码" />
        </el-form-item>
        <el-form-item label="报告周期">
          <el-input v-model="formData.reportPeriod" placeholder="请输入报告周期" />
        </el-form-item>
        <el-form-item label="报告类型">
          <el-select v-model="formData.reportType" placeholder="请选择报告类型" style="width:100%">
            <el-option label="月度" value="月度" />
            <el-option label="季度" value="季度" />
            <el-option label="半年度" value="半年度" />
            <el-option label="年度" value="年度" />
          </el-select>
        </el-form-item>
        <el-form-item label="经营业绩">
          <el-input v-model="formData.businessPerf" type="textarea" :rows="3" placeholder="请输入经营业绩" />
        </el-form-item>
        <el-form-item label="KPI完成情况">
          <el-input v-model="formData.kpiCompletion" type="textarea" :rows="3" placeholder="请输入KPI完成情况" />
        </el-form-item>
        <el-form-item label="重点任务进展">
          <el-input v-model="formData.taskProgress" type="textarea" :rows="3" placeholder="请输入重点任务进展" />
        </el-form-item>
        <el-form-item label="风险事项">
          <el-input v-model="formData.riskEvent" type="textarea" :rows="3" placeholder="请输入风险事项" />
        </el-form-item>
        <el-form-item label="下期计划">
          <el-input v-model="formData.nextPeriodPlan" type="textarea" :rows="3" placeholder="请输入下期计划" />
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
const dialogTitle = ref('新增S3管理报告监控')
const formData = reactive({
  id: null,
  monitorCode: "",
  reportPeriod: "",
  reportType: "月度",
  businessPerf: "",
  kpiCompletion: "",
  taskProgress: "",
  riskEvent: "",
  nextPeriodPlan: "",
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
    const res = await sixsApi.listS3()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增S3管理报告监控'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑S3管理报告监控'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await sixsApi.deleteS3(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await sixsApi.updateS3(formData.id, formData) : await sixsApi.saveS3(formData)
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

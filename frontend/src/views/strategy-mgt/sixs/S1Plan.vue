<template>
  <div class="page-container">
    <div class="page-header">
      <h2>S1战略规划监控</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="monitorCode" label="监控编码" width="120" />
      <el-table-column prop="monitorPeriod" label="监控周期" width="120" />
      <el-table-column prop="strategyFocus" label="战略重点" width="240" />
      <el-table-column prop="planProgress" label="规划进度%" width="110" />
      <el-table-column prop="planStatus" label="状态" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="监控编码">
          <el-input v-model="formData.monitorCode" placeholder="请输入监控编码" />
        </el-form-item>
        <el-form-item label="监控周期">
          <el-input v-model="formData.monitorPeriod" placeholder="请输入监控周期" />
        </el-form-item>
        <el-form-item label="战略重点">
          <el-input v-model="formData.strategyFocus" type="textarea" :rows="3" placeholder="请输入战略重点" />
        </el-form-item>
        <el-form-item label="战略目标">
          <el-input v-model="formData.strategyGoal" type="textarea" :rows="3" placeholder="请输入战略目标" />
        </el-form-item>
        <el-form-item label="规划进度%">
          <el-input-number v-model="formData.planProgress" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.planStatus" placeholder="请选择状态" style="width:100%">
            <el-option label="正常" value="正常" />
            <el-option label="预警" value="预警" />
            <el-option label="滞后" value="滞后" />
          </el-select>
        </el-form-item>
        <el-form-item label="偏差原因">
          <el-input v-model="formData.deviationReason" type="textarea" :rows="3" placeholder="请输入偏差原因" />
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
const dialogTitle = ref('新增S1战略规划监控')
const formData = reactive({
  id: null,
  monitorCode: "",
  monitorPeriod: "",
  strategyFocus: "",
  strategyGoal: "",
  planProgress: null,
  planStatus: "正常",
  deviationReason: "",
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
    const res = await sixsApi.listS1()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增S1战略规划监控'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑S1战略规划监控'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await sixsApi.deleteS1(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await sixsApi.updateS1(formData.id, formData) : await sixsApi.saveS1(formData)
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

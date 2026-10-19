<template>
  <div class="page-container">
    <div class="page-header">
      <h2>CSF关键成功因素</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="csfCode" label="CSF编码" width="120" />
      <el-table-column prop="relatedGoalId" label="关联目标" width="100" />
      <el-table-column prop="csfDefinition" label="CSF定义" width="240" />
      <el-table-column prop="priority" label="优先级" width="80" />
      <el-table-column prop="responsibleDept" label="责任部门" width="120" />
      <el-table-column prop="completionStatus" label="完成状态" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="CSF编码">
          <el-input v-model="formData.csfCode" placeholder="请输入CSF编码" />
        </el-form-item>
        <el-form-item label="关联目标ID">
          <el-input-number v-model="formData.relatedGoalId" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="CSF定义">
          <el-input v-model="formData.csfDefinition" type="textarea" :rows="3" placeholder="请输入CSF定义" />
        </el-form-item>
        <el-form-item label="成功条件">
          <el-input v-model="formData.successCriteria" type="textarea" :rows="3" placeholder="请输入成功条件" />
        </el-form-item>
        <el-form-item label="关键因素">
          <el-input v-model="formData.keyFactors" type="textarea" :rows="3" placeholder="请输入关键因素" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="formData.priority" placeholder="请选择优先级" style="width:100%">
            <el-option label="高" value="高" />
            <el-option label="中" value="中" />
            <el-option label="低" value="低" />
          </el-select>
        </el-form-item>
        <el-form-item label="责任部门">
          <el-input v-model="formData.responsibleDept" placeholder="请输入责任部门" />
        </el-form-item>
        <el-form-item label="完成状态">
          <el-select v-model="formData.completionStatus" placeholder="请选择完成状态" style="width:100%">
            <el-option label="未开始" value="未开始" />
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
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
import { blmApi, dsteApi, sixsApi, budgetApi, workplanApi } from '@/api/strategy'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增CSF关键成功因素')
const formData = reactive({
  id: null,
  csfCode: "",
  relatedGoalId: null,
  csfDefinition: "",
  successCriteria: "",
  keyFactors: "",
  priority: "高",
  responsibleDept: "",
  completionStatus: "未开始",
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
    const res = await dsteApi.listCsfs()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增CSF关键成功因素'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑CSF关键成功因素'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await dsteApi.deleteCsf(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await dsteApi.updateCsf(formData.id, formData) : await dsteApi.saveCsf(formData)
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

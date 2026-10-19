<template>
  <div class="page-container">
    <div class="page-header">
      <h2>SP/BP重点任务</h2>
      <div>
        <el-button type="warning" @click="handleRefreshStatus">刷新状态</el-button>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="taskCode" label="任务编码" width="120" />
      <el-table-column prop="taskLevel" label="任务层级" width="110" />
      <el-table-column prop="taskName" label="任务名称" width="220" />
      <el-table-column prop="strategyWeight" label="战略权重" width="100" />
      <el-table-column prop="primaryDept" label="主责部门" width="120" />
      <el-table-column prop="assignee" label="负责人" width="100" />
      <el-table-column prop="taskStatus" label="状态" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="任务编码">
          <el-input v-model="formData.taskCode" placeholder="请输入任务编码" />
        </el-form-item>
        <el-form-item label="任务层级">
          <el-select v-model="formData.taskLevel" placeholder="请选择任务层级" style="width:100%">
            <el-option label="SP中长期" value="SP中长期" />
            <el-option label="BP年度" value="BP年度" />
            <el-option label="季度攻坚" value="季度攻坚" />
            <el-option label="月度执行" value="月度执行" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务名称">
          <el-input v-model="formData.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务目标">
          <el-input v-model="formData.taskGoal" type="textarea" :rows="3" placeholder="请输入任务目标" />
        </el-form-item>
        <el-form-item label="战略权重%">
          <el-input-number v-model="formData.strategyWeight" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="任务权重%">
          <el-input-number v-model="formData.taskWeight" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="主责部门">
          <el-input v-model="formData.primaryDept" placeholder="请输入主责部门" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="formData.assignee" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="formData.priority" placeholder="请选择优先级" style="width:100%">
            <el-option label="高" value="高" />
            <el-option label="中" value="中" />
            <el-option label="低" value="低" />
          </el-select>
        </el-form-item>
        <el-form-item label="战略标签">
          <el-select v-model="formData.strategyTag" placeholder="请选择战略标签" style="width:100%">
            <el-option label="存量优化" value="存量优化" />
            <el-option label="增量创新" value="增量创新" />
            <el-option label="颠覆突破" value="颠覆突破" />
            <el-option label="能力建设" value="能力建设" />
            <el-option label="风险整改" value="风险整改" />
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
const dialogTitle = ref('新增SP/BP重点任务')
const formData = reactive({
  id: null,
  taskCode: "",
  taskLevel: "SP中长期",
  taskName: "",
  taskGoal: "",
  strategyWeight: null,
  taskWeight: null,
  primaryDept: "",
  assignee: "",
  priority: "高",
  strategyTag: "存量优化",
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
    const res = await dsteApi.listTasks()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增SP/BP重点任务'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑SP/BP重点任务'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await dsteApi.deleteTask(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await dsteApi.updateTask(formData.id, formData) : await dsteApi.saveTask(formData)
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message || '保存失败')
    }
  } catch (e) { ElMessage.error('保存失败') }
}

async function handleRefreshStatus() {
  try {
    const res = await dsteApi.refreshTaskStatus()
    if (res.data.code === 200) { ElMessage.success('任务状态已刷新'); loadData() }
    else ElMessage.error(res.data.message || '刷新失败')
  } catch (e) { ElMessage.error('刷新失败') }
}

onMounted(loadData)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
</style>

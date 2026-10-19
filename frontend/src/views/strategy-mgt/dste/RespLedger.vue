<template>
  <div class="page-container">
    <div class="page-header">
      <h2>解码责任台账</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="responsibleSubject" label="责任主体" width="140" />
      <el-table-column prop="strategyLevel" label="战略层级" width="100" />
      <el-table-column prop="responsibilityWeight" label="责任权重%" width="110" />
      <el-table-column prop="performanceScore" label="绩效得分" width="100" />
      <el-table-column prop="dutyStatus" label="状态" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="责任主体">
          <el-input v-model="formData.responsibleSubject" placeholder="请输入责任主体" />
        </el-form-item>
        <el-form-item label="战略层级">
          <el-input v-model="formData.strategyLevel" placeholder="请输入战略层级" />
        </el-form-item>
        <el-form-item label="责任目标">
          <el-input v-model="formData.responsibleGoal" type="textarea" :rows="3" placeholder="请输入责任目标" />
        </el-form-item>
        <el-form-item label="责任KPI">
          <el-input v-model="formData.responsibleKpi" placeholder="请输入责任KPI" />
        </el-form-item>
        <el-form-item label="责任任务">
          <el-input v-model="formData.responsibleTask" placeholder="请输入责任任务" />
        </el-form-item>
        <el-form-item label="责任权重%">
          <el-input-number v-model="formData.responsibilityWeight" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="考核标准">
          <el-input v-model="formData.assessmentStandard" type="textarea" :rows="3" placeholder="请输入考核标准" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.dutyStatus" placeholder="请选择状态" style="width:100%">
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
const dialogTitle = ref('新增解码责任台账')
const formData = reactive({
  id: null,
  responsibleSubject: "",
  strategyLevel: "",
  responsibleGoal: "",
  responsibleKpi: "",
  responsibleTask: "",
  responsibilityWeight: null,
  assessmentStandard: "",
  dutyStatus: "未开始",
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
    const res = await dsteApi.listRespLedgers()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增解码责任台账'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑解码责任台账'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await dsteApi.deleteRespLedger(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await dsteApi.updateRespLedger(formData.id, formData) : await dsteApi.saveRespLedger(formData)
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

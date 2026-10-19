<template>
  <div class="page-container">
    <div class="page-header">
      <h2>调整追加调剂</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="changeCode" label="变更编码" width="120" />
      <el-table-column prop="changeType" label="变更类型" width="100" />
      <el-table-column prop="beforeAmount" label="变更前" width="120" />
      <el-table-column prop="changeAmount" label="变更额" width="120" />
      <el-table-column prop="afterAmount" label="变更后" width="120" />
      <el-table-column prop="approvalStatus" label="审批状态" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="变更编码">
          <el-input v-model="formData.changeCode" placeholder="请输入变更编码" />
        </el-form-item>
        <el-form-item label="变更类型">
          <el-select v-model="formData.changeType" placeholder="请选择变更类型" style="width:100%">
            <el-option label="调整" value="调整" />
            <el-option label="追加" value="追加" />
            <el-option label="调剂" value="调剂" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联预算ID">
          <el-input-number v-model="formData.relatedBudgetId" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="变更前金额">
          <el-input-number v-model="formData.beforeAmount" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="变更金额">
          <el-input-number v-model="formData.changeAmount" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="变更原因">
          <el-input v-model="formData.reason" type="textarea" :rows="3" placeholder="请输入变更原因" />
        </el-form-item>
        <el-form-item label="审批状态">
          <el-select v-model="formData.approvalStatus" placeholder="请选择审批状态" style="width:100%">
            <el-option label="待审批" value="待审批" />
            <el-option label="已通过" value="已通过" />
            <el-option label="已驳回" value="已驳回" />
          </el-select>
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker v-model="formData.effectiveTime" type="date" value-format="YYYY-MM-DD" style="width:100%" />
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
const dialogTitle = ref('新增调整追加调剂')
const formData = reactive({
  id: null,
  changeCode: "",
  changeType: "调整",
  relatedBudgetId: null,
  beforeAmount: null,
  changeAmount: null,
  reason: "",
  approvalStatus: "待审批",
  effectiveTime: "",
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
    const res = await budgetApi.listChanges()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增调整追加调剂'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑调整追加调剂'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await budgetApi.deleteChange(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await budgetApi.updateChange(formData.id, formData) : await budgetApi.saveChange(formData)
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

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>内控审计</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="auditCode" label="审计编码" width="120" />
      <el-table-column prop="auditPeriod" label="审计周期" width="120" />
      <el-table-column prop="auditScope" label="审计范围" width="180" />
      <el-table-column prop="problemCount" label="问题数" width="90" />
      <el-table-column prop="rectifyStatus" label="整改状态" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="审计编码">
          <el-input v-model="formData.auditCode" placeholder="请输入审计编码" />
        </el-form-item>
        <el-form-item label="审计周期">
          <el-input v-model="formData.auditPeriod" placeholder="请输入审计周期" />
        </el-form-item>
        <el-form-item label="审计范围">
          <el-input v-model="formData.auditScope" placeholder="请输入审计范围" />
        </el-form-item>
        <el-form-item label="审计发现">
          <el-input v-model="formData.auditFindings" type="textarea" :rows="3" placeholder="请输入审计发现" />
        </el-form-item>
        <el-form-item label="问题数量">
          <el-input-number v-model="formData.problemCount" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="整改状态">
          <el-select v-model="formData.rectifyStatus" placeholder="请选择整改状态" style="width:100%">
            <el-option label="未整改" value="未整改" />
            <el-option label="整改中" value="整改中" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
        <el-form-item label="审计人">
          <el-input v-model="formData.auditor" placeholder="请输入审计人" />
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
const dialogTitle = ref('新增内控审计')
const formData = reactive({
  id: null,
  auditCode: "",
  auditPeriod: "",
  auditScope: "",
  auditFindings: "",
  problemCount: null,
  rectifyStatus: "未整改",
  auditor: "",
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
    const res = await budgetApi.listAudits()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增内控审计'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑内控审计'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await budgetApi.deleteAudit(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await budgetApi.updateAudit(formData.id, formData) : await budgetApi.saveAudit(formData)
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

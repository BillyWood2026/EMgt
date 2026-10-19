<template>
  <div class="page-container">
    <div class="page-header">
      <h2>业务设计配置</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="designCode" label="设计编码" width="120" />
      <el-table-column prop="strategyCycle" label="战略周期" width="120" />
      <el-table-column prop="targetCustomers" label="目标客户" width="200" />
      <el-table-column prop="valueProposition" label="价值主张" width="200" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="设计编码">
          <el-input v-model="formData.designCode" placeholder="请输入设计编码" />
        </el-form-item>
        <el-form-item label="战略周期">
          <el-input v-model="formData.strategyCycle" placeholder="请输入战略周期" />
        </el-form-item>
        <el-form-item label="目标客户">
          <el-input v-model="formData.targetCustomers" type="textarea" :rows="3" placeholder="请输入目标客户" />
        </el-form-item>
        <el-form-item label="价值主张">
          <el-input v-model="formData.valueProposition" type="textarea" :rows="3" placeholder="请输入价值主张" />
        </el-form-item>
        <el-form-item label="盈利模式">
          <el-input v-model="formData.profitModel" type="textarea" :rows="3" placeholder="请输入盈利模式" />
        </el-form-item>
        <el-form-item label="核心活动">
          <el-input v-model="formData.coreActivities" type="textarea" :rows="3" placeholder="请输入核心活动" />
        </el-form-item>
        <el-form-item label="战略控制点">
          <el-input v-model="formData.strategicControl" type="textarea" :rows="3" placeholder="请输入战略控制点" />
        </el-form-item>
        <el-form-item label="差异化优势">
          <el-input v-model="formData.differentiation" type="textarea" :rows="3" placeholder="请输入差异化优势" />
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
const dialogTitle = ref('新增业务设计配置')
const formData = reactive({
  id: null,
  designCode: "",
  strategyCycle: "",
  targetCustomers: "",
  valueProposition: "",
  profitModel: "",
  coreActivities: "",
  strategicControl: "",
  differentiation: "",
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
    const res = await blmApi.listDesigns()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增业务设计配置'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑业务设计配置'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await blmApi.deleteDesign(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await blmApi.updateDesign(formData.id, formData) : await blmApi.saveDesign(formData)
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

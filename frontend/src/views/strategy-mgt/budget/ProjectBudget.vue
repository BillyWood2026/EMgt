<template>
  <div class="page-container">
    <div class="page-header">
      <h2>项目四算管理</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="projectCode" label="项目编码" width="120" />
      <el-table-column prop="projectName" label="项目名称" width="200" />
      <el-table-column prop="estimateAmount" label="概算" width="120" />
      <el-table-column prop="budgetAmount" label="预算" width="120" />
      <el-table-column prop="finalAmount" label="决算" width="120" />
      <el-table-column prop="varianceAmount" label="差异" width="120" />
      <el-table-column prop="projectStatus" label="状态" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="项目编码">
          <el-input v-model="formData.projectCode" placeholder="请输入项目编码" />
        </el-form-item>
        <el-form-item label="项目名称">
          <el-input v-model="formData.projectName" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目类型">
          <el-input v-model="formData.projectType" placeholder="请输入项目类型" />
        </el-form-item>
        <el-form-item label="概算金额">
          <el-input-number v-model="formData.estimateAmount" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="预算金额">
          <el-input-number v-model="formData.budgetAmount" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="核算金额">
          <el-input-number v-model="formData.accountingAmount" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="决算金额">
          <el-input-number v-model="formData.finalAmount" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="项目状态">
          <el-select v-model="formData.projectStatus" placeholder="请选择项目状态" style="width:100%">
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已终止" value="已终止" />
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
const dialogTitle = ref('新增项目四算管理')
const formData = reactive({
  id: null,
  projectCode: "",
  projectName: "",
  projectType: "",
  estimateAmount: null,
  budgetAmount: null,
  accountingAmount: null,
  finalAmount: null,
  projectStatus: "进行中",
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
    const res = await budgetApi.listProjects()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增项目四算管理'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑项目四算管理'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await budgetApi.deleteProject(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await budgetApi.updateProject(formData.id, formData) : await budgetApi.saveProject(formData)
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

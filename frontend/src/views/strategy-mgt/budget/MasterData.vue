<template>
  <div class="page-container">
    <div class="page-header">
      <h2>预算主数据</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="subjectCode" label="主体编码" width="120" />
      <el-table-column prop="subjectName" label="主体名称" width="180" />
      <el-table-column prop="subjectType" label="主体类型" width="120" />
      <el-table-column prop="legalEntity" label="法人" width="120" />
      <el-table-column prop="orgLevel" label="组织层级" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="主体编码">
          <el-input v-model="formData.subjectCode" placeholder="请输入主体编码" />
        </el-form-item>
        <el-form-item label="主体名称">
          <el-input v-model="formData.subjectName" placeholder="请输入主体名称" />
        </el-form-item>
        <el-form-item label="主体类型">
          <el-select v-model="formData.subjectType" placeholder="请选择主体类型" style="width:100%">
            <el-option label="法人主体" value="法人主体" />
            <el-option label="成本中心" value="成本中心" />
            <el-option label="利润中心" value="利润中心" />
            <el-option label="投资中心" value="投资中心" />
          </el-select>
        </el-form-item>
        <el-form-item label="上级编码">
          <el-input v-model="formData.parentSubjectCode" placeholder="请输入上级编码" />
        </el-form-item>
        <el-form-item label="层级">
          <el-input-number v-model="formData.level" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="法人主体">
          <el-input v-model="formData.legalEntity" placeholder="请输入法人主体" />
        </el-form-item>
        <el-form-item label="组织层级">
          <el-input v-model="formData.orgLevel" placeholder="请输入组织层级" />
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
const dialogTitle = ref('新增预算主数据')
const formData = reactive({
  id: null,
  subjectCode: "",
  subjectName: "",
  subjectType: "法人主体",
  parentSubjectCode: "",
  level: null,
  legalEntity: "",
  orgLevel: "",
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
    const res = await budgetApi.listSubjects()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增预算主数据'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑预算主数据'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await budgetApi.deleteSubject(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await budgetApi.updateSubject(formData.id, formData) : await budgetApi.saveSubject(formData)
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

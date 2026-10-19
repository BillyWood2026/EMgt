<template>
  <div class="page-container">
    <div class="page-header">
      <h2>三上三下编制</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="versionCode" label="版本编码" width="120" />
      <el-table-column prop="budgetYear" label="预算年度" width="100" />
      <el-table-column prop="versionType" label="版本类型" width="100" />
      <el-table-column prop="versionNo" label="版本号" width="100" />
      <el-table-column prop="totalBudget" label="总预算" width="140" />
      <el-table-column prop="approveStatus" label="审批状态" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="版本编码">
          <el-input v-model="formData.versionCode" placeholder="请输入版本编码" />
        </el-form-item>
        <el-form-item label="预算年度">
          <el-input-number v-model="formData.budgetYear" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="版本类型">
          <el-select v-model="formData.versionType" placeholder="请选择版本类型" style="width:100%">
            <el-option label="一上" value="一上" />
            <el-option label="一下" value="一下" />
            <el-option label="二上" value="二上" />
            <el-option label="二下" value="二下" />
            <el-option label="三上" value="三上" />
            <el-option label="三下" value="三下" />
            <el-option label="调整版" value="调整版" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本号">
          <el-input v-model="formData.versionNo" placeholder="请输入版本号" />
        </el-form-item>
        <el-form-item label="总预算">
          <el-input-number v-model="formData.totalBudget" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="审批状态">
          <el-select v-model="formData.approveStatus" placeholder="请选择审批状态" style="width:100%">
            <el-option label="待审批" value="待审批" />
            <el-option label="已通过" value="已通过" />
            <el-option label="已驳回" value="已驳回" />
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
const dialogTitle = ref('新增三上三下编制')
const formData = reactive({
  id: null,
  versionCode: "",
  budgetYear: null,
  versionType: "一上",
  versionNo: "",
  totalBudget: null,
  approveStatus: "待审批",
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
    const res = await budgetApi.listVersions()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增三上三下编制'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑三上三下编制'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await budgetApi.deleteVersion(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await budgetApi.updateVersion(formData.id, formData) : await budgetApi.saveVersion(formData)
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

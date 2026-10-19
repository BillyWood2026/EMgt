<template>
  <div class="page-container">
    <div class="page-header">
      <h2>战略迭代管理</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="iterationCode" label="迭代编码" width="120" />
      <el-table-column prop="iterationReason" label="迭代原因" width="240" />
      <el-table-column prop="iterationContent" label="迭代内容" width="200" />
      <el-table-column prop="approvalStatus" label="审批状态" width="100" />
      <el-table-column prop="effectiveTime" label="生效时间" width="120" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="迭代编码">
          <el-input v-model="formData.iterationCode" placeholder="请输入迭代编码" />
        </el-form-item>
        <el-form-item label="迭代原因">
          <el-input v-model="formData.iterationReason" type="textarea" :rows="3" placeholder="请输入迭代原因" />
        </el-form-item>
        <el-form-item label="迭代依据">
          <el-input v-model="formData.iterationBasis" type="textarea" :rows="3" placeholder="请输入迭代依据" />
        </el-form-item>
        <el-form-item label="迭代内容">
          <el-input v-model="formData.iterationContent" type="textarea" :rows="3" placeholder="请输入迭代内容" />
        </el-form-item>
        <el-form-item label="调整前">
          <el-input v-model="formData.beforeCompare" type="textarea" :rows="3" placeholder="请输入调整前" />
        </el-form-item>
        <el-form-item label="调整后">
          <el-input v-model="formData.afterCompare" type="textarea" :rows="3" placeholder="请输入调整后" />
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
const dialogTitle = ref('新增战略迭代管理')
const formData = reactive({
  id: null,
  iterationCode: "",
  iterationReason: "",
  iterationBasis: "",
  iterationContent: "",
  beforeCompare: "",
  afterCompare: "",
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
    const res = await dsteApi.listIterations()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增战略迭代管理'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑战略迭代管理'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await dsteApi.deleteIteration(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await dsteApi.updateIteration(formData.id, formData) : await dsteApi.saveIteration(formData)
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

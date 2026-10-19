<template>
  <div class="page-container">
    <div class="page-header">
      <h2>创新焦点管理</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="innovationCode" label="创新编码" width="120" />
      <el-table-column prop="innovationType" label="创新类型" width="120" />
      <el-table-column prop="innovationTrack" label="创新赛道" width="120" />
      <el-table-column prop="expectedBenefit" label="预期收益" width="120" />
      <el-table-column prop="priority" label="优先级" width="80" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="创新编码">
          <el-input v-model="formData.innovationCode" placeholder="请输入创新编码" />
        </el-form-item>
        <el-form-item label="规划年度">
          <el-input-number v-model="formData.planYear" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="创新类型">
          <el-select v-model="formData.innovationType" placeholder="请选择创新类型" style="width:100%">
            <el-option label="存量优化" value="存量优化" />
            <el-option label="增量创新" value="增量创新" />
            <el-option label="颠覆式创新" value="颠覆式创新" />
          </el-select>
        </el-form-item>
        <el-form-item label="创新赛道">
          <el-select v-model="formData.innovationTrack" placeholder="请选择创新赛道" style="width:100%">
            <el-option label="产品" value="产品" />
            <el-option label="技术" value="技术" />
            <el-option label="模式" value="模式" />
            <el-option label="渠道" value="渠道" />
            <el-option label="管理" value="管理" />
          </el-select>
        </el-form-item>
        <el-form-item label="核心内容">
          <el-input v-model="formData.coreContent" type="textarea" :rows="3" placeholder="请输入核心内容" />
        </el-form-item>
        <el-form-item label="预期收益">
          <el-input-number v-model="formData.expectedBenefit" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="落地风险">
          <el-input v-model="formData.landingRisk" type="textarea" :rows="3" placeholder="请输入落地风险" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="formData.priority" placeholder="请选择优先级" style="width:100%">
            <el-option label="高" value="高" />
            <el-option label="中" value="中" />
            <el-option label="低" value="低" />
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
const dialogTitle = ref('新增创新焦点管理')
const formData = reactive({
  id: null,
  innovationCode: "",
  planYear: null,
  innovationType: "存量优化",
  innovationTrack: "产品",
  coreContent: "",
  expectedBenefit: null,
  landingRisk: "",
  priority: "高",
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
    const res = await blmApi.listInnovations()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增创新焦点管理'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑创新焦点管理'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await blmApi.deleteInnovation(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await blmApi.updateInnovation(formData.id, formData) : await blmApi.saveInnovation(formData)
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

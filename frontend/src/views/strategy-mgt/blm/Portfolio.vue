<template>
  <div class="page-container">
    <div class="page-header">
      <h2>投资组合管理</h2>
      <div>
        <el-button type="warning" @click="handleRank">组合排序</el-button>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="portfolioCode" label="组合编码" width="120" />
      <el-table-column prop="investmentType" label="投资类型" width="120" />
      <el-table-column prop="investmentName" label="投资名称" width="200" />
      <el-table-column prop="investmentAmount" label="投资金额" width="120" />
      <el-table-column prop="expectedReturn" label="预期收益" width="120" />
      <el-table-column prop="riskLevel" label="风险等级" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="组合编码">
          <el-input v-model="formData.portfolioCode" placeholder="请输入组合编码" />
        </el-form-item>
        <el-form-item label="关联SP">
          <el-input-number v-model="formData.spId" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="投资类型">
          <el-select v-model="formData.investmentType" placeholder="请选择投资类型" style="width:100%">
            <el-option label="技术研发" value="技术研发" />
            <el-option label="市场扩张" value="市场扩张" />
            <el-option label="产能建设" value="产能建设" />
            <el-option label="人才梯队" value="人才梯队" />
            <el-option label="数字化建设" value="数字化建设" />
          </el-select>
        </el-form-item>
        <el-form-item label="投资名称">
          <el-input v-model="formData.investmentName" placeholder="请输入投资名称" />
        </el-form-item>
        <el-form-item label="投资金额">
          <el-input-number v-model="formData.investmentAmount" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="预期收益">
          <el-input-number v-model="formData.expectedReturn" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="formData.riskLevel" placeholder="请选择风险等级" style="width:100%">
            <el-option label="高" value="高" />
            <el-option label="中" value="中" />
            <el-option label="低" value="低" />
          </el-select>
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
    <el-dialog v-model="rankedVisible" title="组合优先级排序" width="700px">
      <el-table :data="rankedData" border stripe size="small">
        <el-table-column type="index" label="排序" width="70" />
        <el-table-column prop="portfolioCode" label="组合编码" width="130" />
        <el-table-column prop="investmentName" label="投资名称" />
        <el-table-column prop="priority" label="优先级" width="100" />
        <el-table-column prop="investmentAmount" label="投资金额" width="130" />
      </el-table>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { blmApi, dsteApi, sixsApi, budgetApi, workplanApi } from '@/api/strategy'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const rankedVisible = ref(false)
const rankedData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增投资组合管理')
const formData = reactive({
  id: null,
  portfolioCode: "",
  spId: null,
  investmentType: "技术研发",
  investmentName: "",
  investmentAmount: null,
  expectedReturn: null,
  riskLevel: "高",
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
    const res = await blmApi.listPortfolios()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增投资组合管理'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑投资组合管理'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await blmApi.deletePortfolio(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await blmApi.updatePortfolio(formData.id, formData) : await blmApi.savePortfolio(formData)
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message || '保存失败')
    }
  } catch (e) { ElMessage.error('保存失败') }
}

async function handleRank() {
  try {
    const res = await blmApi.rankedPortfolios()
    if (res.data.code === 200) {
      rankedData.value = res.data.data || []
      rankedVisible.value = true
    } else ElMessage.error(res.data.message || '排序查询失败')
  } catch (e) { ElMessage.error('排序查询失败') }
}

onMounted(loadData)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
</style>

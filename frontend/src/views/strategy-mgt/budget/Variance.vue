<template>
  <div class="page-container">
    <div class="page-header">
      <h2>预算差异分析</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="varianceCode" label="差异编码" width="120" />
      <el-table-column prop="budgetId" label="预算ID" width="100" />
      <el-table-column prop="period" label="期间" width="100" />
      <el-table-column prop="budgetAmount" label="预算" width="120" />
      <el-table-column prop="actualAmount" label="实际" width="120" />
      <el-table-column prop="varianceAmount" label="差异额" width="120" />
      <el-table-column prop="varianceRate" label="差异率%" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="差异编码">
          <el-input v-model="formData.varianceCode" placeholder="请输入差异编码" />
        </el-form-item>
        <el-form-item label="预算ID">
          <el-input-number v-model="formData.budgetId" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="期间">
          <el-input v-model="formData.period" placeholder="请输入期间" />
        </el-form-item>
        <el-form-item label="预算金额">
          <el-input-number v-model="formData.budgetAmount" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="实际金额">
          <el-input-number v-model="formData.actualAmount" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="差异类型">
          <el-select v-model="formData.varianceType" placeholder="请选择差异类型" style="width:100%">
            <el-option label="量差" value="量差" />
            <el-option label="价差" value="价差" />
            <el-option label="结构差" value="结构差" />
            <el-option label="时间差" value="时间差" />
          </el-select>
        </el-form-item>
        <el-form-item label="差异原因">
          <el-input v-model="formData.reason" type="textarea" :rows="3" placeholder="请输入差异原因" />
        </el-form-item>
        <el-form-item label="整改措施">
          <el-input v-model="formData.rectifyMeasure" type="textarea" :rows="3" placeholder="请输入整改措施" />
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
const dialogTitle = ref('新增预算差异分析')
const formData = reactive({
  id: null,
  varianceCode: "",
  budgetId: null,
  period: "",
  budgetAmount: null,
  actualAmount: null,
  varianceType: "量差",
  reason: "",
  rectifyMeasure: "",
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
    const res = await budgetApi.listVariances()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增预算差异分析'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑预算差异分析'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await budgetApi.deleteVariance(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await budgetApi.updateVariance(formData.id, formData) : await budgetApi.saveVariance(formData)
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

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>战略KPI指标库</h2>
      <div>
        <el-button type="warning" @click="handleDimStats">维度统计</el-button>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="kpiCode" label="KPI编码" width="120" />
      <el-table-column prop="kpiName" label="指标名称" width="200" />
      <el-table-column prop="kpiLevel" label="层级" width="100" />
      <el-table-column prop="kpiDim" label="维度" width="100" />
      <el-table-column prop="targetValue" label="目标值" width="120" />
      <el-table-column prop="kpiWeight" label="权重%" width="80" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="KPI编码">
          <el-input v-model="formData.kpiCode" placeholder="请输入KPI编码" />
        </el-form-item>
        <el-form-item label="指标名称">
          <el-input v-model="formData.kpiName" placeholder="请输入指标名称" />
        </el-form-item>
        <el-form-item label="指标层级">
          <el-input v-model="formData.kpiLevel" placeholder="请输入指标层级" />
        </el-form-item>
        <el-form-item label="指标维度">
          <el-input v-model="formData.kpiDim" placeholder="请输入指标维度" />
        </el-form-item>
        <el-form-item label="指标定义">
          <el-input v-model="formData.kpiDefinition" type="textarea" :rows="3" placeholder="请输入指标定义" />
        </el-form-item>
        <el-form-item label="计算公式">
          <el-input v-model="formData.calcFormula" placeholder="请输入计算公式" />
        </el-form-item>
        <el-form-item label="目标值">
          <el-input-number v-model="formData.targetValue" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="预警阈值">
          <el-input-number v-model="formData.warnThreshold" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="红线阈值">
          <el-input-number v-model="formData.redlineThreshold" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="权重%">
          <el-input-number v-model="formData.kpiWeight" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="dimStatsVisible" title="KPI维度分布统计" width="500px">
      <el-table :data="dimStatsData" border stripe size="small">
        <el-table-column prop="dim" label="维度" />
        <el-table-column prop="count" label="指标数量" width="140" />
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
const dimStatsVisible = ref(false)
const dimStatsData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增战略KPI指标库')
const formData = reactive({
  id: null,
  kpiCode: "",
  kpiName: "",
  kpiLevel: "",
  kpiDim: "",
  kpiDefinition: "",
  calcFormula: "",
  targetValue: null,
  warnThreshold: null,
  redlineThreshold: null,
  kpiWeight: null,
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
    const res = await dsteApi.listKpis()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增战略KPI指标库'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑战略KPI指标库'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await dsteApi.deleteKpi(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await dsteApi.updateKpi(formData.id, formData) : await dsteApi.saveKpi(formData)
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message || '保存失败')
    }
  } catch (e) { ElMessage.error('保存失败') }
}

async function handleDimStats() {
  try {
    const res = await dsteApi.kpiDimStats()
    if (res.data.code === 200) {
      const stats = res.data.data || {}
      dimStatsData.value = Object.keys(stats).map(k => ({ dim: k, count: stats[k] }))
      dimStatsVisible.value = true
    } else ElMessage.error(res.data.message || '维度统计失败')
  } catch (e) { ElMessage.error('维度统计失败') }
}

onMounted(loadData)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
</style>

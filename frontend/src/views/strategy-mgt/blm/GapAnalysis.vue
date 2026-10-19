<template>
  <div class="page-container">
    <div class="page-header">
      <h2>差距分析台账</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="gapCode" label="差距编码" width="120" />
      <el-table-column prop="gapType" label="差距类型" width="100" />
      <el-table-column prop="currentValue" label="现状" width="100" />
      <el-table-column prop="targetValue" label="目标" width="100" />
      <el-table-column prop="gapValue" label="差距值" width="100" />
      <el-table-column prop="gapRate" label="差距率%" width="100" />
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
        <el-form-item label="规划年度">
          <el-input-number v-model="formData.planYear" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="差距类型">
          <el-select v-model="formData.gapType" placeholder="请选择差距类型" style="width:100%">
            <el-option label="业绩差距" value="业绩差距" />
            <el-option label="机会差距" value="机会差距" />
            <el-option label="对标差距" value="对标差距" />
          </el-select>
        </el-form-item>
        <el-form-item label="对标维度">
          <el-input v-model="formData.benchmarkDim" placeholder="请输入对标维度" />
        </el-form-item>
        <el-form-item label="现状数据">
          <el-input-number v-model="formData.currentValue" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="标杆数据">
          <el-input-number v-model="formData.benchmarkValue" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="目标数据">
          <el-input-number v-model="formData.targetValue" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="差距成因">
          <el-input v-model="formData.gapCause" type="textarea" :rows="3" placeholder="请输入差距成因" />
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
const dialogTitle = ref('新增差距分析台账')
const formData = reactive({
  id: null,
  planYear: null,
  gapType: "业绩差距",
  benchmarkDim: "",
  currentValue: null,
  benchmarkValue: null,
  targetValue: null,
  gapCause: "",
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
    const res = await blmApi.listGaps()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增差距分析台账'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑差距分析台账'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await blmApi.deleteGap(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await blmApi.updateGap(formData.id, formData) : await blmApi.saveGap(formData)
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

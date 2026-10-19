<template>
  <div class="page-container">
    <div class="page-header">
      <h2>SP战略规划台账</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="spCode" label="SP编号" width="140" />
      <el-table-column prop="spCycle" label="战略周期" width="100" />
      <el-table-column prop="baselineVersion" label="基线版本" width="100" />
      <el-table-column prop="businessTrack" label="核心赛道" width="120" />
      <el-table-column prop="totalResourceBudget" label="资源总预算" width="140" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="success" @click="handlePublish(row)" :disabled="row.status === '已发布'">发布</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="SP编号">
          <el-input v-model="formData.spCode" placeholder="请输入SP编号" />
        </el-form-item>
        <el-form-item label="战略周期">
          <el-select v-model="formData.spCycle" placeholder="请选择战略周期" style="width:100%">
            <el-option label="3年" value="3年" />
            <el-option label="5年" value="5年" />
            <el-option label="自定义" value="自定义" />
          </el-select>
        </el-form-item>
        <el-form-item label="基线版本">
          <el-input v-model="formData.baselineVersion" placeholder="请输入基线版本" />
        </el-form-item>
        <el-form-item label="核心赛道">
          <el-input v-model="formData.businessTrack" placeholder="请输入核心赛道" />
        </el-form-item>
        <el-form-item label="投资方向">
          <el-input v-model="formData.investmentDirection" type="textarea" :rows="3" placeholder="请输入投资方向" />
        </el-form-item>
        <el-form-item label="能力建设目标">
          <el-input v-model="formData.capabilityGoals" type="textarea" :rows="3" placeholder="请输入能力建设目标" />
        </el-form-item>
        <el-form-item label="中长期营收">
          <el-input-number v-model="formData.ltRevenue" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="资源总预算">
          <el-input-number v-model="formData.totalResourceBudget" :min="0" controls-position="right" style="width:100%" />
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
const dialogTitle = ref('新增SP战略规划台账')
const formData = reactive({
  id: null,
  spCode: "",
  spCycle: "3年",
  baselineVersion: "",
  businessTrack: "",
  investmentDirection: "",
  capabilityGoals: "",
  ltRevenue: null,
  totalResourceBudget: null,
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
    const res = await blmApi.listSpPlans()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增SP战略规划台账'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑SP战略规划台账'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await blmApi.deleteSpPlan(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handlePublish(row) {
  try {
    const res = await blmApi.publishSp(row.id)
    if (res.data.code === 200) { ElMessage.success('发布成功'); loadData() }
    else ElMessage.error(res.data.message || '发布失败')
  } catch (e) { ElMessage.error('发布失败') }
}

async function handleSubmit() {
  try {
    const res = formData.id ? await blmApi.updateSpPlan(formData.id, formData) : await blmApi.saveSpPlan(formData)
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

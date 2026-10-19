<template>
  <div class="page-container">
    <div class="page-header">
      <h2>战略意图定义</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="intentCode" label="意图编码" width="120" />
      <el-table-column prop="strategyCycle" label="战略周期" width="120" />
      <el-table-column prop="mission" label="使命" width="200" />
      <el-table-column prop="ltRevenue" label="中长期营收" width="140" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="意图编码">
          <el-input v-model="formData.intentCode" placeholder="请输入意图编码" />
        </el-form-item>
        <el-form-item label="战略周期">
          <el-input v-model="formData.strategyCycle" placeholder="请输入战略周期" />
        </el-form-item>
        <el-form-item label="企业使命">
          <el-input v-model="formData.mission" type="textarea" :rows="3" placeholder="请输入企业使命" />
        </el-form-item>
        <el-form-item label="企业愿景">
          <el-input v-model="formData.vision" type="textarea" :rows="3" placeholder="请输入企业愿景" />
        </el-form-item>
        <el-form-item label="核心价值观">
          <el-input v-model="formData.coreValues" type="textarea" :rows="3" placeholder="请输入核心价值观" />
        </el-form-item>
        <el-form-item label="中长期营收">
          <el-input-number v-model="formData.ltRevenue" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="中长期利润">
          <el-input-number v-model="formData.ltProfit" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="年度定位">
          <el-input v-model="formData.annualPosition" placeholder="请输入年度定位" />
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
const dialogTitle = ref('新增战略意图定义')
const formData = reactive({
  id: null,
  intentCode: "",
  strategyCycle: "",
  mission: "",
  vision: "",
  coreValues: "",
  ltRevenue: null,
  ltProfit: null,
  annualPosition: "",
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
    const res = await blmApi.listIntents()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增战略意图定义'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑战略意图定义'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await blmApi.deleteIntent(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await blmApi.updateIntent(formData.id, formData) : await blmApi.saveIntent(formData)
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

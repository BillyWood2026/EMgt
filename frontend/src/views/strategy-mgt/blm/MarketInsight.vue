<template>
  <div class="page-container">
    <div class="page-header">
      <h2>市场洞察分析</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="insightCode" label="洞察编码" width="120" />
      <el-table-column prop="insightDim" label="洞察维度" width="100" />
      <el-table-column prop="insightTopic" label="洞察主题" width="200" />
      <el-table-column prop="marketSize" label="市场规模" width="120" />
      <el-table-column prop="growthRate" label="增速%" width="100" />
      <el-table-column prop="opportunityScore" label="机会评分" width="100" />
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
        <el-form-item label="洞察维度">
          <el-input v-model="formData.insightDim" placeholder="请输入洞察维度" />
        </el-form-item>
        <el-form-item label="洞察主题">
          <el-input v-model="formData.insightTopic" placeholder="请输入洞察主题" />
        </el-form-item>
        <el-form-item label="环境分析">
          <el-input v-model="formData.envAnalysis" type="textarea" :rows="3" placeholder="请输入环境分析" />
        </el-form-item>
        <el-form-item label="市场规模">
          <el-input-number v-model="formData.marketSize" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="增速%">
          <el-input-number v-model="formData.growthRate" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="竞争对手">
          <el-input v-model="formData.competitors" type="textarea" :rows="3" placeholder="请输入竞争对手" />
        </el-form-item>
        <el-form-item label="市场机会">
          <el-input v-model="formData.opportunity" type="textarea" :rows="3" placeholder="请输入市场机会" />
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
const dialogTitle = ref('新增市场洞察分析')
const formData = reactive({
  id: null,
  planYear: null,
  insightDim: "",
  insightTopic: "",
  envAnalysis: "",
  marketSize: null,
  growthRate: null,
  competitors: "",
  opportunity: "",
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
    const res = await blmApi.listInsights()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增市场洞察分析'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑市场洞察分析'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await blmApi.deleteInsight(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await blmApi.updateInsight(formData.id, formData) : await blmApi.saveInsight(formData)
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

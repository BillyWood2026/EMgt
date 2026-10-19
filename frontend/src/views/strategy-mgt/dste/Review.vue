<template>
  <div class="page-container">
    <div class="page-header">
      <h2>阶段性复盘</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="reviewCode" label="复盘编码" width="120" />
      <el-table-column prop="reviewCycle" label="复盘周期" width="100" />
      <el-table-column prop="reviewScope" label="复盘范围" width="180" />
      <el-table-column prop="overallAchievement" label="总体达成" width="200" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="复盘编码">
          <el-input v-model="formData.reviewCode" placeholder="请输入复盘编码" />
        </el-form-item>
        <el-form-item label="复盘周期">
          <el-select v-model="formData.reviewCycle" placeholder="请选择复盘周期" style="width:100%">
            <el-option label="月度" value="月度" />
            <el-option label="季度" value="季度" />
            <el-option label="年度" value="年度" />
          </el-select>
        </el-form-item>
        <el-form-item label="复盘范围">
          <el-input v-model="formData.reviewScope" placeholder="请输入复盘范围" />
        </el-form-item>
        <el-form-item label="总体达成">
          <el-input v-model="formData.overallAchievement" type="textarea" :rows="3" placeholder="请输入总体达成" />
        </el-form-item>
        <el-form-item label="亮点">
          <el-input v-model="formData.highlights" type="textarea" :rows="3" placeholder="请输入亮点" />
        </el-form-item>
        <el-form-item label="问题">
          <el-input v-model="formData.problems" type="textarea" :rows="3" placeholder="请输入问题" />
        </el-form-item>
        <el-form-item label="根因">
          <el-input v-model="formData.rootCause" type="textarea" :rows="3" placeholder="请输入根因" />
        </el-form-item>
        <el-form-item label="改进措施">
          <el-input v-model="formData.improvementMeasures" type="textarea" :rows="3" placeholder="请输入改进措施" />
        </el-form-item>
        <el-form-item label="迭代建议">
          <el-input v-model="formData.iterationSuggestion" type="textarea" :rows="3" placeholder="请输入迭代建议" />
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
const dialogTitle = ref('新增阶段性复盘')
const formData = reactive({
  id: null,
  reviewCode: "",
  reviewCycle: "月度",
  reviewScope: "",
  overallAchievement: "",
  highlights: "",
  problems: "",
  rootCause: "",
  improvementMeasures: "",
  iterationSuggestion: "",
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
    const res = await dsteApi.listReviews()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增阶段性复盘'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑阶段性复盘'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await dsteApi.deleteReview(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await dsteApi.updateReview(formData.id, formData) : await dsteApi.saveReview(formData)
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

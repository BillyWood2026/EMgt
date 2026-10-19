<template>
  <div class="page-container">
    <div class="page-header">
      <h2>资源匹配管理</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="resourceCode" label="资源编码" width="120" />
      <el-table-column prop="resourceType" label="资源类型" width="100" />
      <el-table-column prop="requiredAmount" label="需求量" width="110" />
      <el-table-column prop="matchedAmount" label="已匹配" width="110" />
      <el-table-column prop="gapAmount" label="缺口" width="110" />
      <el-table-column prop="matchStatus" label="匹配状态" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="资源编码">
          <el-input v-model="formData.resourceCode" placeholder="请输入资源编码" />
        </el-form-item>
        <el-form-item label="关联任务ID">
          <el-input-number v-model="formData.relatedTaskId" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="资源类型">
          <el-select v-model="formData.resourceType" placeholder="请选择资源类型" style="width:100%">
            <el-option label="资金" value="资金" />
            <el-option label="人力" value="人力" />
            <el-option label="技术" value="技术" />
            <el-option label="物料" value="物料" />
            <el-option label="渠道" value="渠道" />
          </el-select>
        </el-form-item>
        <el-form-item label="需求量">
          <el-input-number v-model="formData.requiredAmount" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="已匹配量">
          <el-input-number v-model="formData.matchedAmount" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="资源来源">
          <el-input v-model="formData.resourceSource" placeholder="请输入资源来源" />
        </el-form-item>
        <el-form-item label="资源负责人">
          <el-input v-model="formData.resourceOwner" placeholder="请输入资源负责人" />
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
const dialogTitle = ref('新增资源匹配管理')
const formData = reactive({
  id: null,
  resourceCode: "",
  relatedTaskId: null,
  resourceType: "资金",
  requiredAmount: null,
  matchedAmount: null,
  resourceSource: "",
  resourceOwner: "",
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
    const res = await dsteApi.listResources()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增资源匹配管理'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑资源匹配管理'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await dsteApi.deleteResource(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await dsteApi.updateResource(formData.id, formData) : await dsteApi.saveResource(formData)
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

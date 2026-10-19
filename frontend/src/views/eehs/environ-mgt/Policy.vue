<template>
  <div class="page-container">
    <div class="page-header">
      <h2>环保政策</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="policyNo" label="文号" />
      <el-table-column prop="title" label="制度标题" />
      <el-table-column prop="publishOrg" label="发布机构" />
      <el-table-column prop="publishDate" label="发布日期" />
      <el-table-column prop="effectiveDate" label="生效日期" />
      <el-table-column label="合规状态" width="100"><!-- 规格书字段，待后端扩展 -->
        <template #default="{row}">
          <el-tag :type="row.complianceStatus === 'compliant' ? 'success' : (row.complianceStatus === 'non_compliant' ? 'danger' : 'warning')" size="small">
            {{ complianceLabel(row.complianceStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="relatedProcess" label="关联流程制度" /><!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="制度标题">
          <el-input v-model="formData.title" placeholder="请输入制度标题" />
        </el-form-item>
        <el-form-item label="文号">
          <el-input v-model="formData.policyNo" placeholder="请输入文号" />
        </el-form-item>
        <el-form-item label="发布机构">
          <el-input v-model="formData.publishOrg" placeholder="请输入发布机构" />
        </el-form-item>
        <el-form-item label="发布日期">
          <el-date-picker v-model="formData.publishDate" type="date" value-format="YYYY-MM-DD" placeholder="选择发布日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="生效日期">
          <el-date-picker v-model="formData.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择生效日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="formData.content" type="textarea" :rows="3" placeholder="请输入正文" />
        </el-form-item>
        <el-form-item label="合规状态"><!-- 规格书字段，待后端扩展 -->
          <el-select v-model="formData.complianceStatus" placeholder="请选择合规状态" style="width:100%">
            <el-option label="合规" value="compliant" />
            <el-option label="不合规" value="non_compliant" />
            <el-option label="待复核" value="pending" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联流程制度"><!-- 规格书字段，待后端扩展 -->
          <el-input v-model="formData.relatedProcess" placeholder="请输入关联流程制度" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width:100%">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="已废止" value="abolished" />
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
import { envApi } from '@/api/eehs'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增环保政策')
const formData = reactive({
  id: null,
  title: "",
  policyNo: "",
  publishOrg: "",
  publishDate: "",
  effectiveDate: "",
  content: "",
  status: "",
  complianceStatus: "",
  relatedProcess: ""
})

function complianceLabel(s) {
  if (s === 'compliant') return '合规'
  if (s === 'non_compliant') return '不合规'
  if (s === 'pending') return '待复核'
  return '-'
}

function resetForm() {
  Object.keys(formData).forEach(k => { formData[k] = (k === 'id') ? null : null })
}

async function loadData() {
  loading.value = true
  try {
    const res = await envApi.policies.list()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增环保政策'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑环保政策'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该条记录？', '提示', { type: 'warning' })
    const res = await envApi.policies.remove(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id
      ? await envApi.policies.update(formData.id, formData)
      : await envApi.policies.save(formData)
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

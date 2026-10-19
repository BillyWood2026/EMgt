<template>
  <div class="page-container">
    <div class="page-header">
      <h2>大屏实例</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索关键词" clearable style="width:200px;margin-right:8px" @keyup.enter="loadData" />
        <el-button style="margin-right:8px" @click="loadData">搜索</el-button>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="screenName" label="大屏名称" />
      <el-table-column prop="templateId" label="模板ID" />
      <el-table-column prop="accessToken" label="访问令牌" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="publishTime" label="发布时间" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="success" @click="handlePublish(row)">发布</el-button>
          <el-button size="small" type="warning" @click="handlePreview(row)">预览渲染</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="大屏名称">
          <el-input v-model="formData.screenName" placeholder="请输入大屏名称" />
        </el-form-item>
        <el-form-item label="模板ID">
          <el-input-number v-model="formData.templateId" style="width:100%" :controls="false" />
        </el-form-item>
        <el-form-item label="组件JSON">
          <el-input v-model="formData.componentsJson" type="textarea" :rows="3" placeholder="请输入组件JSON" />
        </el-form-item>
        <el-form-item label="访问令牌">
          <el-input v-model="formData.accessToken" placeholder="请输入访问令牌" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width:100%">
            <el-option label="设计中" value="设计中" />
            <el-option label="已发布" value="已发布" />
            <el-option label="下线" value="下线" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker v-model="formData.publishTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择发布时间" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="previewVisible" title="预览渲染" width="700px">
      <pre style="max-height:400px;overflow:auto">{{ previewJson }}</pre>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { screenApi } from '@/api/platform'
import { ElMessage, ElMessageBox } from 'element-plus'

const api = screenApi.instance
const loading = ref(false)
const tableData = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增大屏实例')
const formData = reactive({
  id: null,
  screenName: '',
  templateId: null,
  componentsJson: '',
  accessToken: '',
  status: '',
  publishTime: ''
})

function resetForm() {
  formData.id = null
  formData.screenName = ''
  formData.templateId = null
  formData.componentsJson = ''
  formData.accessToken = ''
  formData.status = ''
  formData.publishTime = ''
}
const previewVisible = ref(false)
const previewJson = ref('')

async function loadData() {
  loading.value = true
  try {
    const res = await api.list(keyword.value)
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增大屏实例'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑大屏实例'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该条记录？', '提示', { type: 'warning' })
    const res = await api.remove(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await api.update(formData.id, formData) : await api.save(formData)
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message || '保存失败')
    }
  } catch (e) { ElMessage.error('保存失败') }
}

async function handlePublish(row) {
  try {
    const res = await api.publish(row.id)
    if (res.data.code === 200) { ElMessage.success('发布成功'); loadData() }
    else ElMessage.error(res.data.message || '发布失败')
  } catch (e) { ElMessage.error('发布失败') }
}

async function handlePreview(row) {
  try {
    const res = await api.render(row.id)
    if (res.data.code === 200) {
      previewJson.value = JSON.stringify(res.data.data, null, 2)
      previewVisible.value = true
    } else ElMessage.error(res.data.message || '预览失败')
  } catch (e) { ElMessage.error('预览失败') }
}

onMounted(loadData)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; gap: 8px; flex-wrap: wrap; }
</style>

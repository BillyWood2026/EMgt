<template>
  <div class="page-container">
    <div class="page-header">
      <h2>文件记录</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索关键词" clearable style="width:200px;margin-right:8px" @keyup.enter="loadData" />
        <el-button style="margin-right:8px" @click="loadData">搜索</el-button>
        <el-upload :show-file-list="false" :http-request="handleUpload" style="display:inline-block;margin-right:8px">
          <el-button type="success">上传</el-button>
        </el-upload>
        <el-button type="primary" @click="handleAdd">新增</el-button>
        <ImportExportBar
          :columns="ioColumns"
          :rows="tableData"
          file-name="文件记录"
          title="文件记录"
          style="margin-left:8px"
        />
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="fileName" label="文件名" />
      <el-table-column prop="fileExt" label="扩展名" />
      <el-table-column prop="fileSize" label="大小(B)" />
      <el-table-column prop="mimeType" label="类型" />
      <el-table-column prop="storagePath" label="存储路径" />
      <el-table-column prop="md5" label="MD5" />
      <el-table-column prop="uploader" label="上传人" />
      <el-table-column prop="serviceCode" label="所属服务" />
      <el-table-column prop="status" label="状态">
        <template #default="{row}">
          <el-tag :type="row.status === '正常' ? 'success' : 'info'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="上传时间" width="160" />
      <el-table-column prop="updateTime" label="更新时间" width="160" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="success" @click="handleDownload(row)">下载</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="文件名">
          <el-input v-model="formData.fileName" placeholder="请输入文件名" />
        </el-form-item>
        <el-form-item label="扩展名">
          <el-input v-model="formData.fileExt" placeholder="请输入扩展名" />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="formData.mimeType" placeholder="请输入类型" />
        </el-form-item>
        <el-form-item label="存储路径">
          <el-input v-model="formData.storagePath" placeholder="请输入存储路径" />
        </el-form-item>
        <el-form-item label="上传人">
          <el-input v-model="formData.uploader" placeholder="请输入上传人" />
        </el-form-item>
        <el-form-item label="所属服务">
          <el-input v-model="formData.serviceCode" placeholder="请输入所属服务" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width:100%">
            <el-option label="正常" value="正常" />
            <el-option label="已删除" value="已删除" />
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
import { fileApi } from '@/api/platform'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImportExportBar from '@/components/ImportExportBar.vue'

const ioColumns = [
  { key: 'fileName', title: '文件名' },
  { key: 'fileExt', title: '扩展名' },
  { key: 'fileSize', title: '大小(B)' },
  { key: 'mimeType', title: '类型' },
  { key: 'storagePath', title: '存储路径' },
  { key: 'md5', title: 'MD5' },
  { key: 'uploader', title: '上传人' },
  { key: 'serviceCode', title: '所属服务' },
  { key: 'status', title: '状态' },
  { key: 'createTime', title: '上传时间' },
  { key: 'updateTime', title: '更新时间' }
]

const api = fileApi.record
const loading = ref(false)
const tableData = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增文件记录')
const formData = reactive({
  id: null,
  fileName: '',
  fileExt: '',
  mimeType: '',
  storagePath: '',
  uploader: '',
  serviceCode: '',
  status: ''
})

function resetForm() {
  formData.id = null
  formData.fileName = ''
  formData.fileExt = ''
  formData.mimeType = ''
  formData.storagePath = ''
  formData.uploader = ''
  formData.serviceCode = ''
  formData.status = ''
}

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
  dialogTitle.value = '新增文件记录'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑文件记录'
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

async function handleUpload({ file }) {
  const fd = new FormData()
  fd.append('file', file)
  try {
    const res = await fileApi.upload(fd)
    if (res.data.code === 200) { ElMessage.success('上传成功'); loadData() }
    else ElMessage.error(res.data.message || '上传失败')
  } catch (e) { ElMessage.error('上传失败') }
}

function handleDownload(row) {
  window.open('/api/file/download/' + row.id)
}

onMounted(loadData)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; gap: 8px; flex-wrap: wrap; }
</style>

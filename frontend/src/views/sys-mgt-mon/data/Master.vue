<template>
  <div class="page-container">
    <div class="page-header">
      <h2>主数据</h2>
      <div>
        <el-input v-model="keyword" placeholder="搜索关键词" clearable style="width:200px;margin-right:8px" @keyup.enter="loadData" />
        <el-button style="margin-right:8px" @click="loadData">搜索</el-button>
        <el-button type="primary" @click="handleAdd">新增</el-button>
        <ImportExportBar
          :columns="ioColumns"
          :rows="tableData"
          file-name="主数据"
          title="主数据"
          import-api="/api/data/master"
          :field-map="ioFieldMap"
          style="margin-left:8px"
          @done="loadData"
        />
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="dataCode" label="数据编码" />
      <el-table-column prop="dataName" label="数据名称" />
      <el-table-column prop="dataType" label="数据类型" />
      <el-table-column prop="version" label="版本" />
      <el-table-column prop="status" label="状态">
        <template #default="{row}">
          <el-tag :type="row.status === '启用' ? 'success' : 'info'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="effectiveDate" label="生效日期" />
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column prop="updateTime" label="更新时间" width="160" />
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{row}">
          <el-button size="small" :type="row.status === '启用' ? 'warning' : 'success'" @click="handleToggle(row)">{{ row.status === '启用' ? '停用' : '启用' }}</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="数据编码">
          <el-input v-model="formData.dataCode" placeholder="请输入数据编码" />
        </el-form-item>
        <el-form-item label="数据名称">
          <el-input v-model="formData.dataName" placeholder="请输入数据名称" />
        </el-form-item>
        <el-form-item label="数据类型">
          <el-select v-model="formData.dataType" placeholder="请选择数据类型" style="width:100%">
            <el-option label="组织" value="组织" />
            <el-option label="人员" value="人员" />
            <el-option label="物料" value="物料" />
            <el-option label="科目" value="科目" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本">
          <el-input v-model="formData.version" placeholder="请输入版本" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width:100%">
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
        <el-form-item label="生效日期">
          <el-date-picker v-model="formData.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择生效日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
import { dataApi } from '@/api/platform'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImportExportBar from '@/components/ImportExportBar.vue'

const ioColumns = [
  { key: 'dataCode', title: '数据编码' },
  { key: 'dataName', title: '数据名称' },
  { key: 'dataType', title: '数据类型' },
  { key: 'version', title: '版本' },
  { key: 'status', title: '状态' },
  { key: 'effectiveDate', title: '生效日期' },
  { key: 'remark', title: '备注' },
  { key: 'createTime', title: '创建时间' },
  { key: 'updateTime', title: '更新时间' }
]
const ioFieldMap = {
  '数据编码': 'dataCode',
  '数据名称': 'dataName',
  '数据类型': 'dataType',
  '版本': 'version',
  '状态': 'status',
  '生效日期': 'effectiveDate',
  '备注': 'remark'
}

const api = dataApi.master
const loading = ref(false)
const tableData = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增主数据')
const formData = reactive({
  id: null,
  dataCode: '',
  dataName: '',
  dataType: '',
  version: '',
  status: '',
  effectiveDate: '',
  remark: ''
})

function resetForm() {
  formData.id = null
  formData.dataCode = ''
  formData.dataName = ''
  formData.dataType = ''
  formData.version = ''
  formData.status = ''
  formData.effectiveDate = ''
  formData.remark = ''
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
  dialogTitle.value = '新增主数据'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑主数据'
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

async function handleToggle(row) {
  const target = row.status === '启用' ? '停用' : '启用'
  try {
    const res = await api.update(row.id, { status: target })
    if (res.data.code === 200) { ElMessage.success(target + '成功'); loadData() }
    else ElMessage.error(res.data.message || '操作失败')
  } catch (e) { ElMessage.error('操作失败') }
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

onMounted(loadData)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; gap: 8px; flex-wrap: wrap; }
</style>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>排污管理</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="outletCode" label="排口编码" />
      <el-table-column prop="outletName" label="排口名称" />
      <el-table-column prop="pollutantType" label="污染物类型" />
      <el-table-column prop="permitValue" label="许可排放量" />
      <el-table-column prop="actualValue" label="实际排放量" />
      <el-table-column prop="dischargePeriod" label="排放周期" />
      <el-table-column label="全流程状态" width="120">
        <template #default="{row}">
          <el-tag :type="flowTagType(row.status)" size="small">{{ flowLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="排口编码">
          <el-input v-model="formData.outletCode" placeholder="请输入排口编码" />
        </el-form-item>
        <el-form-item label="排口名称">
          <el-input v-model="formData.outletName" placeholder="请输入排口名称" />
        </el-form-item>
        <el-form-item label="污染物类型">
          <el-select v-model="formData.pollutantType" placeholder="请选择污染物类型" style="width:100%">
            <el-option label="生产废水" value="生产废水" />
            <el-option label="工艺废气" value="工艺废气" />
            <el-option label="设备噪音" value="设备噪音" />
            <el-option label="车间粉尘" value="车间粉尘" />
            <el-option label="一般固废" value="一般固废" />
            <el-option label="涉密危废" value="涉密危废" />
            <el-option label="化学品废液" value="化学品废液" />
          </el-select>
        </el-form-item>
        <el-form-item label="许可排放量">
          <el-input-number v-model="formData.permitValue" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="实际排放量">
          <el-input-number v-model="formData.actualValue" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="formData.unit" placeholder="请输入单位" />
        </el-form-item>
        <el-form-item label="排放周期">
          <el-input v-model="formData.dischargePeriod" placeholder="请输入排放周期" />
        </el-form-item>
        <el-form-item label="全流程状态">
          <el-select v-model="formData.status" placeholder="请选择全流程状态" style="width:100%">
            <el-option label="申报" value="declared" />
            <el-option label="登记" value="registered" />
            <el-option label="暂存" value="stored" />
            <el-option label="转运" value="transferred" />
            <el-option label="处置" value="disposed" />
            <el-option label="归档" value="archived" />
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
const dialogTitle = ref('新增排污管理')
const formData = reactive({
  id: null,
  outletCode: "",
  outletName: "",
  pollutantType: "",
  permitValue: null,
  actualValue: null,
  unit: "",
  dischargePeriod: "",
  status: ""
})

// 全流程：申报→登记→暂存→转运→处置→归档
const flowMap = {
  declared: '申报',
  registered: '登记',
  stored: '暂存',
  transferred: '转运',
  disposed: '处置',
  archived: '归档',
  normal: '正常',
  exceeded: '超许可'
}
function flowLabel(s) { return flowMap[s] || s || '-' }
function flowTagType(s) {
  if (s === 'exceeded') return 'danger'
  if (s === 'archived') return 'success'
  if (s === 'disposed') return 'success'
  if (s === 'stored') return 'warning'
  return 'info'
}

function resetForm() {
  Object.keys(formData).forEach(k => { formData[k] = null })
}

async function loadData() {
  loading.value = true
  try {
    const res = await envApi.emissions.list()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增排污管理'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑排污管理'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该条记录？', '提示', { type: 'warning' })
    const res = await envApi.emissions.remove(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id
      ? await envApi.emissions.update(formData.id, formData)
      : await envApi.emissions.save(formData)
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

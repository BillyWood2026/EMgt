<template>
  <div class="page-container">
    <div class="page-header">
      <h2>执行督办管理</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="supervisionCode" label="督办编码" width="120" />
      <el-table-column prop="supervisionTopic" label="督办主题" width="220" />
      <el-table-column prop="supervisionLevel" label="督办级别" width="100" />
      <el-table-column prop="supervisionTarget" label="督办对象" width="120" />
      <el-table-column prop="rectifyDeadline" label="整改期限" width="120" />
      <el-table-column prop="closeStatus" label="状态" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="督办编码">
          <el-input v-model="formData.supervisionCode" placeholder="请输入督办编码" />
        </el-form-item>
        <el-form-item label="督办主题">
          <el-input v-model="formData.supervisionTopic" placeholder="请输入督办主题" />
        </el-form-item>
        <el-form-item label="督办来源">
          <el-input v-model="formData.supervisionSource" placeholder="请输入督办来源" />
        </el-form-item>
        <el-form-item label="督办级别">
          <el-select v-model="formData.supervisionLevel" placeholder="请选择督办级别" style="width:100%">
            <el-option label="重大" value="重大" />
            <el-option label="重要" value="重要" />
            <el-option label="一般" value="一般" />
          </el-select>
        </el-form-item>
        <el-form-item label="督办对象">
          <el-input v-model="formData.supervisionTarget" placeholder="请输入督办对象" />
        </el-form-item>
        <el-form-item label="督办内容">
          <el-input v-model="formData.supervisionContent" type="textarea" :rows="3" placeholder="请输入督办内容" />
        </el-form-item>
        <el-form-item label="整改期限">
          <el-date-picker v-model="formData.rectifyDeadline" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="整改反馈">
          <el-input v-model="formData.rectifyFeedback" type="textarea" :rows="3" placeholder="请输入整改反馈" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.closeStatus" placeholder="请选择状态" style="width:100%">
            <el-option label="待反馈" value="待反馈" />
            <el-option label="整改中" value="整改中" />
            <el-option label="已销号" value="已销号" />
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
import { blmApi, dsteApi, sixsApi, budgetApi, workplanApi } from '@/api/strategy'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增执行督办管理')
const formData = reactive({
  id: null,
  supervisionCode: "",
  supervisionTopic: "",
  supervisionSource: "",
  supervisionLevel: "重大",
  supervisionTarget: "",
  supervisionContent: "",
  rectifyDeadline: "",
  rectifyFeedback: "",
  closeStatus: "待反馈",
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
    const res = await dsteApi.listSupervisions()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增执行督办管理'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑执行督办管理'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await dsteApi.deleteSupervision(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await dsteApi.updateSupervision(formData.id, formData) : await dsteApi.saveSupervision(formData)
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

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>S6经理人考评</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="evalCode" label="考评编码" width="120" />
      <el-table-column prop="evalPeriod" label="考评周期" width="120" />
      <el-table-column prop="managerName" label="经理人" width="100" />
      <el-table-column prop="position" label="职位" width="140" />
      <el-table-column prop="unitPerfScore" label="单元业绩" width="100" />
      <el-table-column prop="totalScore" label="综合得分" width="100" />
      <el-table-column prop="perfLevel" label="等级" width="80" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="考评编码">
          <el-input v-model="formData.evalCode" placeholder="请输入考评编码" />
        </el-form-item>
        <el-form-item label="考评周期">
          <el-input v-model="formData.evalPeriod" placeholder="请输入考评周期" />
        </el-form-item>
        <el-form-item label="经理人姓名">
          <el-input v-model="formData.managerName" placeholder="请输入经理人姓名" />
        </el-form-item>
        <el-form-item label="职位">
          <el-input v-model="formData.position" placeholder="请输入职位" />
        </el-form-item>
        <el-form-item label="单元业绩得分">
          <el-input-number v-model="formData.unitPerfScore" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="履职得分">
          <el-input-number v-model="formData.dutyScore" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="合规得分">
          <el-input-number v-model="formData.complianceScore" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="能力得分">
          <el-input-number v-model="formData.capabilityScore" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="改进建议">
          <el-input v-model="formData.improvementSuggestion" type="textarea" :rows="3" placeholder="请输入改进建议" />
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
const dialogTitle = ref('新增S6经理人考评')
const formData = reactive({
  id: null,
  evalCode: "",
  evalPeriod: "",
  managerName: "",
  position: "",
  unitPerfScore: null,
  dutyScore: null,
  complianceScore: null,
  capabilityScore: null,
  improvementSuggestion: "",
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
    const res = await sixsApi.listS6()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增S6经理人考评'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑S6经理人考评'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await sixsApi.deleteS6(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await sixsApi.updateS6(formData.id, formData) : await sixsApi.saveS6(formData)
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

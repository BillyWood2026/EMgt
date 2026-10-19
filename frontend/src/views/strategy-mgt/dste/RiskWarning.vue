<template>
  <div class="page-container">
    <div class="page-header">
      <h2>战略风险预警</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="riskCode" label="风险编码" width="120" />
      <el-table-column prop="riskSource" label="风险来源" width="120" />
      <el-table-column prop="riskType" label="风险类型" width="120" />
      <el-table-column prop="riskLevel" label="风险等级" width="100" />
      <el-table-column prop="handler" label="处理人" width="100" />
      <el-table-column prop="closeStatus" label="闭环状态" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="风险编码">
          <el-input v-model="formData.riskCode" placeholder="请输入风险编码" />
        </el-form-item>
        <el-form-item label="风险来源">
          <el-select v-model="formData.riskSource" placeholder="请选择风险来源" style="width:100%">
            <el-option label="任务滞后" value="任务滞后" />
            <el-option label="指标异常" value="指标异常" />
            <el-option label="资源不足" value="资源不足" />
            <el-option label="市场变化" value="市场变化" />
            <el-option label="人才缺口" value="人才缺口" />
            <el-option label="组织适配不足" value="组织适配不足" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险类型">
          <el-input v-model="formData.riskType" placeholder="请输入风险类型" />
        </el-form-item>
        <el-form-item label="风险描述">
          <el-input v-model="formData.riskDesc" type="textarea" :rows="3" placeholder="请输入风险描述" />
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="formData.riskLevel" placeholder="请选择风险等级" style="width:100%">
            <el-option label="重大" value="重大" />
            <el-option label="较大" value="较大" />
            <el-option label="一般" value="一般" />
            <el-option label="轻微" value="轻微" />
          </el-select>
        </el-form-item>
        <el-form-item label="影响范围">
          <el-input v-model="formData.impactScope" type="textarea" :rows="3" placeholder="请输入影响范围" />
        </el-form-item>
        <el-form-item label="处理人">
          <el-input v-model="formData.handler" placeholder="请输入处理人" />
        </el-form-item>
        <el-form-item label="处理措施">
          <el-input v-model="formData.handleMeasure" type="textarea" :rows="3" placeholder="请输入处理措施" />
        </el-form-item>
        <el-form-item label="闭环状态">
          <el-select v-model="formData.closeStatus" placeholder="请选择闭环状态" style="width:100%">
            <el-option label="未处置" value="未处置" />
            <el-option label="处置中" value="处置中" />
            <el-option label="已闭环" value="已闭环" />
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
const dialogTitle = ref('新增战略风险预警')
const formData = reactive({
  id: null,
  riskCode: "",
  riskSource: "任务滞后",
  riskType: "",
  riskDesc: "",
  riskLevel: "重大",
  impactScope: "",
  handler: "",
  handleMeasure: "",
  closeStatus: "未处置",
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
    const res = await dsteApi.listRisks()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增战略风险预警'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑战略风险预警'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await dsteApi.deleteRisk(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id ? await dsteApi.updateRisk(formData.id, formData) : await dsteApi.saveRisk(formData)
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

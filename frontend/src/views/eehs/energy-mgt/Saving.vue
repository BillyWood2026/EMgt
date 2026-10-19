<template>
  <div class="page-container">
    <div class="page-header">
      <h2>节能项目</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="projectNo" label="项目编号" width="110" />
      <el-table-column prop="projectName" label="项目名称" min-width="140" />
      <el-table-column prop="investAmount" label="投资额(元)" width="110" />
      <el-table-column prop="annualSaving" label="年节约(元)" width="110" />
      <!-- 规格书字段：节能效益测算（computed） -->
      <el-table-column label="预计节能效益" width="120">
        <template #default="{row}">
          <span class="benefit">¥{{ estimatedBenefit(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="投资回报率" width="100">
        <template #default="{row}">
          <el-tag :type="roiTagType(roiRate(row))" size="small">{{ roiRate(row) }}%</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="responsible" label="责任人" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <!-- 规格书字段：状态机流转 -->
      <el-table-column label="流转" width="120" fixed="right">
        <template #default="{row}">
          <el-button v-if="nextStatus(row.status)" size="small" type="primary" link @click="handleFlow(row)">
            →{{ statusText(nextStatus(row.status)) }}
          </el-button>
          <span v-else class="muted">已归档</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="项目名称">
          <el-input v-model="formData.projectName" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目编号">
          <el-input v-model="formData.projectNo" placeholder="请输入项目编号" />
        </el-form-item>
        <el-form-item label="节能措施">
          <el-input v-model="formData.measure" type="textarea" :rows="3" placeholder="请输入节能措施" />
        </el-form-item>
        <el-form-item label="投资额(元)">
          <el-input-number v-model="formData.investAmount" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="年节约金额(元)">
          <el-input-number v-model="formData.annualSaving" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="投资回收期(年)">
          <el-input-number v-model="formData.paybackPeriod" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="formData.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择开始日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="formData.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择结束日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="责任人">
          <el-input v-model="formData.responsible" placeholder="请输入责任人" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width:100%">
            <el-option label="立项" value="planning" />
            <el-option label="实施" value="implementing" />
            <el-option label="验收" value="acceptance" />
            <el-option label="效益评估" value="benefit_evaluation" />
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
import { energyApi } from '@/api/eehs'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增节能项目')
const formData = reactive({
  id: null,
  projectName: "",
  projectNo: "",
  measure: "",
  investAmount: null,
  annualSaving: null,
  paybackPeriod: null,
  startDate: "",
  endDate: "",
  responsible: "",
  status: "planning"
})

// 状态机：立项→实施→验收→效益评估→归档
const STATUS_FLOW = ['planning', 'implementing', 'acceptance', 'benefit_evaluation', 'archived']
function nextStatus(s) {
  const i = STATUS_FLOW.indexOf(s)
  if (i < 0 || i >= STATUS_FLOW.length - 1) return null
  return STATUS_FLOW[i + 1]
}
function statusText(s) {
  return {
    planning: '立项', implementing: '实施', acceptance: '验收',
    benefit_evaluation: '效益评估', archived: '归档'
  }[s] || s
}
function statusTagType(s) {
  return {
    planning: 'info', implementing: 'warning', acceptance: 'primary',
    benefit_evaluation: 'warning', archived: 'success'
  }[s] || 'info'
}

// 节能效益测算：预计节能效益 = 年节约金额 × 投资回收期
function estimatedBenefit(row) {
  const annual = Number(row.annualSaving) || 0
  const period = Number(row.paybackPeriod) || 0
  return (annual * period).toFixed(2)
}
// 投资回报率 ROI = 年节约 / 投资额 × 100%
function roiRate(row) {
  const invest = Number(row.investAmount) || 0
  const annual = Number(row.annualSaving) || 0
  if (invest <= 0) return '0.0'
  return ((annual / invest) * 100).toFixed(1)
}
function roiTagType(rate) {
  const r = Number(rate)
  if (r >= 30) return 'success'
  if (r >= 10) return 'warning'
  return 'danger'
}

function resetForm() {
  Object.keys(formData).forEach(k => { formData[k] = (k === 'id') ? null : null })
  formData.status = "planning"
}

async function loadData() {
  loading.value = true
  try {
    const res = await energyApi.savings.list()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增节能项目'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑节能项目'
  resetForm()
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 状态机流转
async function handleFlow(row) {
  const next = nextStatus(row.status)
  if (!next) return
  try {
    await ElMessageBox.confirm(`确认将项目流转至「${statusText(next)}」？`, '状态流转', { type: 'warning' })
    const payload = { ...row, status: next }
    const res = await energyApi.savings.update(row.id, payload)
    if (res.data.code === 200) { ElMessage.success('流转成功'); loadData() }
    else ElMessage.error(res.data.message || '流转失败')
  } catch (e) {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该条记录？', '提示', { type: 'warning' })
    const res = await energyApi.savings.remove(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id
      ? await energyApi.savings.update(formData.id, formData)
      : await energyApi.savings.save(formData)
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
.benefit { color: #52c41a; font-weight: bold; }
.muted { color: #999; }
</style>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>事故管理</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="incidentNo" label="事故编号" />
      <el-table-column prop="incidentName" label="事故名称" />
      <!-- 分级：一般隐患/重大隐患/未遂事件/一般事故/重大事故 -->
      <el-table-column label="事故分级" width="110">
        <template #default="{row}">
          <el-tag :type="levelTagType(row.level)" size="small">{{ row.level || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="incidentType" label="类型" />
      <el-table-column prop="occurTime" label="发生时间" />
      <el-table-column prop="location" label="发生地点" />
      <el-table-column prop="rootCauseAnalysis" label="根因分析" /><!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="correctiveMeasure" label="纠正预防措施" /><!-- 规格书字段，待后端扩展 -->
      <!-- 重复隐患预警算法 -->
      <el-table-column label="重复隐患预警" width="120">
        <template #default="{row}">
          <el-tag v-if="isRepeatHazard(row)" type="danger" size="small">重复隐患</el-tag>
          <span v-else style="color:#999">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="事故编号">
          <el-input v-model="formData.incidentNo" placeholder="请输入事故编号" />
        </el-form-item>
        <el-form-item label="事故名称">
          <el-input v-model="formData.incidentName" placeholder="请输入事故名称" />
        </el-form-item>
        <el-form-item label="事故分级">
          <el-select v-model="formData.level" placeholder="请选择事故分级" style="width:100%">
            <el-option label="一般隐患" value="一般隐患" />
            <el-option label="重大隐患" value="重大隐患" />
            <el-option label="未遂事件" value="未遂事件" />
            <el-option label="一般事故" value="一般事故" />
            <el-option label="重大事故" value="重大事故" />
          </el-select>
        </el-form-item>
        <el-form-item label="事故类型">
          <el-input v-model="formData.incidentType" placeholder="请输入事故类型" />
        </el-form-item>
        <el-form-item label="发生时间">
          <el-date-picker v-model="formData.occurTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择发生时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="发生地点">
          <el-input v-model="formData.location" placeholder="请输入发生地点" />
        </el-form-item>
        <el-form-item label="伤亡情况">
          <el-input v-model="formData.casualty" placeholder="请输入伤亡情况" />
        </el-form-item>
        <el-form-item label="经济损失(元)">
          <el-input-number v-model="formData.economicLoss" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="事故描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入事故描述" />
        </el-form-item>
        <el-form-item label="根因分析"><!-- 规格书字段，待后端扩展 -->
          <el-input v-model="formData.rootCauseAnalysis" type="textarea" :rows="3" placeholder="请输入根因分析" />
        </el-form-item>
        <el-form-item label="纠正预防措施"><!-- 规格书字段，待后端扩展 -->
          <el-input v-model="formData.correctiveMeasure" type="textarea" :rows="3" placeholder="请输入纠正预防措施" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width:100%">
            <el-option label="已上报" value="reported" />
            <el-option label="调查中" value="investigating" />
            <el-option label="已结案" value="closed" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { safetyApi } from '@/api/eehs'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增事故管理')
const formData = reactive({
  id: null,
  incidentNo: "",
  incidentName: "",
  level: "",
  incidentType: "",
  occurTime: "",
  location: "",
  casualty: "",
  economicLoss: null,
  description: "",
  rootCauseAnalysis: "",
  correctiveMeasure: "",
  status: ""
})

function levelTagType(level) {
  if (level === '重大事故' || level === '重大隐患') return 'danger'
  if (level === '一般事故') return 'warning'
  if (level === '未遂事件') return 'info'
  return 'success'
}

// 重复隐患预警算法：同一地点+同类型事故出现2次及以上判定为重复隐患
function isRepeatHazard(row) {
  if (!row.location || !row.incidentType) return false
  return tableData.value.filter(r => r.id !== row.id && r.location === row.location && r.incidentType === row.incidentType).length >= 1
}

// computed：重大事故/隐患数量
const majorIncidentCount = computed(() => tableData.value.filter(r => r.level === '重大事故' || r.level === '重大隐患').length)

function resetForm() {
  Object.keys(formData).forEach(k => { formData[k] = null })
}

async function loadData() {
  loading.value = true
  try {
    const res = await safetyApi.incidents.list()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增事故管理'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑事故管理'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该条记录？', '提示', { type: 'warning' })
    const res = await safetyApi.incidents.remove(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id
      ? await safetyApi.incidents.update(formData.id, formData)
      : await safetyApi.incidents.save(formData)
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

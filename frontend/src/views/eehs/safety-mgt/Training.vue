<template>
  <div class="page-container">
    <div class="page-header">
      <h2>安全培训</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="trainingName" label="培训名称" />
      <el-table-column prop="trainer" label="讲师" />
      <el-table-column prop="trainDate" label="培训日期" />
      <el-table-column prop="location" label="地点" />
      <el-table-column prop="participantCount" label="参训人数" />
      <el-table-column prop="qualValidUntil" label="资质有效期" /><!-- 规格书字段，待后端扩展 -->
      <!-- 联动资质到期预警 -->
      <el-table-column label="资质到期预警" width="130">
        <template #default="{row}">
          <el-tag :type="qualWarningType(row)" size="small" v-if="qualWarning(row)">{{ qualWarning(row) }}</el-tag>
          <span v-else style="color:#999">正常</span>
        </template>
      </el-table-column>
      <el-table-column prop="result" label="考核结果" />
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
        <el-form-item label="培训名称">
          <el-input v-model="formData.trainingName" placeholder="请输入培训名称" />
        </el-form-item>
        <el-form-item label="培训讲师">
          <el-input v-model="formData.trainer" placeholder="请输入培训讲师" />
        </el-form-item>
        <el-form-item label="培训日期">
          <el-date-picker v-model="formData.trainDate" type="date" value-format="YYYY-MM-DD" placeholder="选择培训日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="培训地点">
          <el-input v-model="formData.location" placeholder="请输入培训地点" />
        </el-form-item>
        <el-form-item label="参训人数">
          <el-input-number v-model="formData.participantCount" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="资质有效期"><!-- 规格书字段，待后端扩展 -->
          <el-date-picker v-model="formData.qualValidUntil" type="date" value-format="YYYY-MM-DD" placeholder="选择资质有效期" style="width:100%" />
        </el-form-item>
        <el-form-item label="培训内容">
          <el-input v-model="formData.content" type="textarea" :rows="3" placeholder="请输入培训内容" />
        </el-form-item>
        <el-form-item label="考核结果">
          <el-input v-model="formData.result" placeholder="请输入考核结果" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width:100%">
            <el-option label="计划中" value="planned" />
            <el-option label="已完成" value="done" />
            <el-option label="已取消" value="cancelled" />
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
const dialogTitle = ref('新增安全培训')
const formData = reactive({
  id: null,
  trainingName: "",
  trainer: "",
  trainDate: "",
  location: "",
  participantCount: null,
  qualValidUntil: "",
  content: "",
  result: "",
  status: ""
})

// 联动资质到期预警算法：距到期30天内预警，已过期标红
function qualWarning(row) {
  if (!row.qualValidUntil) return ''
  const expire = new Date(row.qualValidUntil)
  const now = new Date()
  const diffDays = Math.ceil((expire - now) / (1000 * 60 * 60 * 24))
  if (diffDays < 0) return '已过期'
  if (diffDays <= 30) return `${diffDays}天后到期`
  return ''
}
function qualWarningType(row) {
  const msg = qualWarning(row)
  if (msg === '已过期') return 'danger'
  if (msg) return 'warning'
  return 'success'
}

// computed：即将到期资质数量
const expiringQualCount = computed(() => tableData.value.filter(r => qualWarning(r)).length)

function resetForm() {
  Object.keys(formData).forEach(k => { formData[k] = null })
}

async function loadData() {
  loading.value = true
  try {
    const res = await safetyApi.trainings.list()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增安全培训'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑安全培训'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该条记录？', '提示', { type: 'warning' })
    const res = await safetyApi.trainings.remove(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id
      ? await safetyApi.trainings.update(formData.id, formData)
      : await safetyApi.trainings.save(formData)
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

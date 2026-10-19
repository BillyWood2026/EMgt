<template>
  <div class="page-container">
    <div class="page-header">
      <h2>职业健康检查</h2>
      <div>
        <el-button type="primary" @click="handleAdd">新增</el-button>
        <el-button type="success" @click="hazardDrawerVisible = true">职业危害辨识与岗位分级</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="employeeName" label="员工姓名" width="100" />
      <el-table-column prop="dept" label="部门" width="110" />
      <el-table-column prop="post" label="岗位" width="110" />
      <el-table-column prop="examType" label="体检类型" width="100" />
      <el-table-column prop="examDate" label="检查日期" width="110" />
      <el-table-column prop="hospital" label="检查机构" width="120" />
      <el-table-column label="检查结论" min-width="120">
        <template #default="{row}">
          <span :class="{ 'text-danger': isAbnormal(row.result) }">{{ row.result }}</span>
        </template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column label="异常标记" width="80">
        <template #default="{row}">
          <el-tag v-if="isAbnormal(row.result)" type="danger" size="small">异常</el-tag>
          <el-tag v-else type="success" size="small">正常</el-tag>
        </template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column label="复查状态" width="90">
        <template #default="{row}">
          <el-tag :type="recheckTagType(row.recheckStatus)" size="small">
            {{ recheckStatusText(row.recheckStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column label="调岗建议" width="110">
        <template #default="{row}">
          <el-tag v-if="transferSuggestion(row) === '建议调岗'" type="warning" size="small">建议调岗</el-tag>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="nextExamDate" label="下次检查日期" width="120" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="员工姓名">
          <el-input v-model="formData.employeeName" placeholder="请输入员工姓名" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="formData.dept" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="岗位">
          <el-input v-model="formData.post" placeholder="请输入岗位" />
        </el-form-item>
        <el-form-item label="体检类型">
          <el-select v-model="formData.examType" placeholder="请选择体检类型" style="width:100%">
            <el-option label="岗前" value="岗前" />
            <el-option label="岗中" value="岗中" />
            <el-option label="离岗" value="离岗" />
          </el-select>
        </el-form-item>
        <el-form-item label="检查日期">
          <el-date-picker v-model="formData.examDate" type="date" value-format="YYYY-MM-DD" placeholder="选择检查日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="检查机构">
          <el-input v-model="formData.hospital" placeholder="请输入检查机构" />
        </el-form-item>
        <el-form-item label="检查结论">
          <el-input v-model="formData.result" placeholder="请输入检查结论（含异常/疑似职业病将自动标红并触发调岗）" />
        </el-form-item>
        <el-form-item label="危害因素">
          <el-input v-model="formData.hazardFactor" placeholder="请输入危害因素" />
        </el-form-item>
        <el-form-item label="下次检查日期">
          <el-date-picker v-model="formData.nextExamDate" type="date" value-format="YYYY-MM-DD" placeholder="选择下次检查日期" style="width:100%" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="复查状态">
          <el-select v-model="formData.recheckStatus" placeholder="请选择复查状态" style="width:100%">
            <el-option label="无需复查" value="none" />
            <el-option label="待复查" value="pending" />
            <el-option label="已复查" value="done" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 职业危害辨识与岗位分级 抽屉 -->
    <el-drawer v-model="hazardDrawerVisible" title="职业危害辨识与岗位分级" size="40%">
      <div class="hazard-tip">
        <el-alert type="warning" :closable="false" show-icon>
          高危害岗位（噪音/粉尘/化学试剂/辐射）强制周期体检 + 强制防护配置
        </el-alert>
      </div>
      <el-form :model="hazardForm" label-width="120px" style="margin-top:16px">
        <el-form-item label="岗位名称">
          <el-input v-model="hazardForm.post" placeholder="请输入岗位名称" />
        </el-form-item>
        <el-form-item label="危害类型">
          <el-select v-model="hazardForm.hazardType" placeholder="请选择危害类型" style="width:100%">
            <el-option v-for="t in hazardTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="接触时长(h/日)">
          <el-input-number v-model="hazardForm.exposureHours" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="岗位危害等级">
          <el-tag :type="hazardLevelTag(hazardLevel).type" size="large">{{ hazardLevelText(hazardLevel) }}</el-tag>
          <span class="muted" style="margin-left:12px">{{ hazardLevelDesc }}</span>
        </el-form-item>
        <el-form-item v-if="hazardLevel === 'high'" label="强制管控">
          <el-tag type="danger">强制周期体检</el-tag>
          <el-tag type="warning" style="margin-left:8px">强制防护配置</el-tag>
        </el-form-item>
      </el-form>
      <div class="hazard-list-title">岗位危害分级清单</div>
      <el-table :data="hazardList" border size="small" max-height="360">
        <el-table-column prop="post" label="岗位" />
        <el-table-column prop="hazardType" label="危害类型" width="100" />
        <el-table-column prop="exposureHours" label="接触时长" width="90" />
        <el-table-column label="危害等级" width="100">
          <template #default="{row}">
            <el-tag :type="hazardLevelTag(calcHazardLevel(row)).type" size="small">
              {{ hazardLevelText(calcHazardLevel(row)) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { healthApi } from '@/api/eehs'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增职业健康检查')
const formData = reactive({
  id: null,
  employeeName: "",
  dept: "",
  post: "",
  examType: "",
  examDate: "",
  hospital: "",
  result: "",
  hazardFactor: "",
  nextExamDate: "",
  recheckStatus: "none"
})

function resetForm() {
  Object.keys(formData).forEach(k => { formData[k] = (k === 'id') ? null : null })
  formData.recheckStatus = "none"
}

// ===== 算法：体检异常 / 调岗建议 =====
const HIGH_HAZARD_TYPES = ['噪音', '粉尘', '化学试剂', '辐射']
function isAbnormal(result) {
  return !!result && (result.includes('异常') || result.includes('职业病'))
}
// 疑似职业病触发调岗提示
function transferSuggestion(row) {
  if (row.result && (row.result.includes('疑似职业病') || row.result.includes('职业病疑似'))) {
    return '建议调岗'
  }
  return ''
}
function recheckStatusText(s) {
  return { none: '无需复查', pending: '待复查', done: '已复查' }[s] || '未设置'
}
function recheckTagType(s) {
  return { none: 'info', pending: 'warning', done: 'success' }[s] || 'info'
}

// ===== 职业危害辨识与岗位分级 =====
const hazardDrawerVisible = ref(false)
const hazardTypes = ['噪音', '粉尘', '化学试剂', '辐射', '高温', '久坐劳损', '视觉疲劳']
const hazardForm = reactive({ post: '', hazardType: '', exposureHours: 0 })
const hazardList = ref([
  { post: 'SMT回流焊岗', hazardType: '高温', exposureHours: 8 },
  { post: '化学品仓管岗', hazardType: '化学试剂', exposureHours: 6 },
  { post: '冲压岗', hazardType: '噪音', exposureHours: 8 },
  { post: '外观质检岗', hazardType: '视觉疲劳', exposureHours: 8 }
])

// 岗位危害等级算法：高危害类型 + 接触时长≥4 → 高；中危害类型 + 时长≥6 → 中；否则低
function calcHazardLevel(row) {
  const isHighType = HIGH_HAZARD_TYPES.includes(row.hazardType)
  const hours = Number(row.exposureHours) || 0
  if (isHighType && hours >= 4) return 'high'
  if (isHighType) return 'medium'
  if (hours >= 6) return 'medium'
  return 'low'
}
const hazardLevel = computed(() => calcHazardLevel(hazardForm))
function hazardLevelTag(level) {
  return { high: { type: 'danger' }, medium: { type: 'warning' }, low: { type: 'success' } }[level] || { type: 'info' }
}
function hazardLevelText(level) {
  return { high: '高危害', medium: '中危害', low: '低危害' }[level] || '未分级'
}
const hazardLevelDesc = computed(() => {
  const m = {
    high: '强制周期体检 + 强制防护配置',
    medium: '建议年度体检 + 配备防护用品',
    low: '常规健康监护'
  }
  return m[hazardLevel.value] || ''
})

async function loadData() {
  loading.value = true
  try {
    const res = await healthApi.examinations.list()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增职业健康检查'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑职业健康检查'
  resetForm()
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该条记录？', '提示', { type: 'warning' })
    const res = await healthApi.examinations.remove(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id
      ? await healthApi.examinations.update(formData.id, formData)
      : await healthApi.examinations.save(formData)
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
.text-danger { color: #f5222d; font-weight: bold; }
.muted { color: #999; }
.hazard-tip { margin-bottom: 8px; }
.hazard-list-title { margin: 16px 0 8px; font-weight: bold; }
</style>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>安全检查</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="inspectionName" label="检查名称" />
      <el-table-column prop="inspectionType" label="类型" />
      <el-table-column prop="location" label="地点" />
      <el-table-column prop="inspectDate" label="检查日期" />
      <el-table-column prop="hazardDesc" label="隐患描述" /><!-- 规格书字段，待后端扩展 -->
      <!-- 算法：安全风险评级=危害程度×发生概率×暴露频次，红橙黄蓝四级 -->
      <el-table-column label="风险等级" width="100">
        <template #default="{row}">
          <el-tag :type="riskTagType(riskLevelOf(row))" size="small">{{ riskLevelOf(row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rectifyMeasure" label="整改措施" /><!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="rectifyDeadline" label="整改时限" />
      <el-table-column prop="responsiblePerson" label="责任人" /><!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="evidenceAttachment" label="佐证附件"><!-- 规格书字段，待后端扩展 --></el-table-column>
      <el-table-column label="状态机" width="130">
        <template #default="{row}">
          <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="检查名称">
          <el-input v-model="formData.inspectionName" placeholder="请输入检查名称" />
        </el-form-item>
        <el-form-item label="检查类型">
          <el-select v-model="formData.inspectionType" placeholder="请选择检查类型" style="width:100%">
            <el-option label="日常检查" value="daily" />
            <el-option label="专项检查" value="special" />
            <el-option label="季度检查" value="quarterly" />
            <el-option label="年度检查" value="yearly" />
          </el-select>
        </el-form-item>
        <el-form-item label="检查人">
          <el-input v-model="formData.inspector" placeholder="请输入检查人" />
        </el-form-item>
        <el-form-item label="检查日期">
          <el-date-picker v-model="formData.inspectDate" type="date" value-format="YYYY-MM-DD" placeholder="选择检查日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="检查地点">
          <el-input v-model="formData.location" placeholder="请输入检查地点" />
        </el-form-item>
        <el-form-item label="隐患描述"><!-- 规格书字段，待后端扩展 -->
          <el-input v-model="formData.hazardDesc" type="textarea" :rows="2" placeholder="请输入隐患描述" />
        </el-form-item>
        <el-form-item label="检查结果">
          <el-select v-model="formData.result" placeholder="请选择检查结果" style="width:100%">
            <el-option label="合格" value="qualified" />
            <el-option label="整改中" value="rectifying" />
            <el-option label="不合格" value="unqualified" />
          </el-select>
        </el-form-item>
        <!-- 算法三要素输入：危害程度、发生概率、暴露频次 -->
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="危害程度"><!-- 规格书字段，待后端扩展 -->
              <el-input-number v-model="formData.hazardDegree" :min="1" :max="5" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="发生概率"><!-- 规格书字段，待后端扩展 -->
              <el-input-number v-model="formData.occurProbability" :min="1" :max="5" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="暴露频次"><!-- 规格书字段，待后端扩展 -->
              <el-input-number v-model="formData.exposureFrequency" :min="1" :max="5" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="风险评级（算法）">
          <el-tag :type="riskTagType(riskLevelOf(formData))" size="large">{{ riskLevelOf(formData) }}</el-tag>
          <span style="margin-left:12px;color:#999">综合指数：{{ riskScoreOf(formData) }}</span>
        </el-form-item>
        <el-form-item label="整改措施"><!-- 规格书字段，待后端扩展 -->
          <el-input v-model="formData.rectifyMeasure" type="textarea" :rows="2" placeholder="请输入整改措施" />
        </el-form-item>
        <el-form-item label="整改期限">
          <el-date-picker v-model="formData.rectifyDeadline" type="date" value-format="YYYY-MM-DD" placeholder="选择整改期限" style="width:100%" />
        </el-form-item>
        <el-form-item label="责任人"><!-- 规格书字段，待后端扩展 -->
          <el-input v-model="formData.responsiblePerson" placeholder="请输入责任人" />
        </el-form-item>
        <el-form-item label="佐证附件"><!-- 规格书字段，待后端扩展 -->
          <el-input v-model="formData.evidenceAttachment" placeholder="请输入附件链接或名称" />
        </el-form-item>
        <!-- 状态机：排查发现→待整改→整改中→待复查→闭环/复检不合格→重新整改 -->
        <el-form-item label="状态机">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width:100%">
            <el-option label="排查发现" value="discovered" />
            <el-option label="待整改" value="pending_rectify" />
            <el-option label="整改中" value="rectifying" />
            <el-option label="待复查" value="pending_review" />
            <el-option label="闭环" value="closed" />
            <el-option label="复检不合格" value="review_failed" />
            <el-option label="重新整改" value="re_rectify" />
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
const dialogTitle = ref('新增安全检查')
const formData = reactive({
  id: null,
  inspectionName: "",
  inspectionType: "",
  inspector: "",
  inspectDate: "",
  location: "",
  result: "",
  issueDesc: "",
  hazardDesc: "",
  rectifyDeadline: "",
  responsiblePerson: "",
  rectifyMeasure: "",
  evidenceAttachment: "",
  hazardDegree: null,
  occurProbability: null,
  exposureFrequency: null,
  status: ""
})

// 算法：安全风险评级 = 危害程度 × 发生概率 × 暴露频次
// 综合指数分级：>=40 红，20-39 橙，8-19 黄，<8 蓝
function riskScoreOf(row) {
  const d = Number(row.hazardDegree) || 0
  const p = Number(row.occurProbability) || 0
  const f = Number(row.exposureFrequency) || 0
  return d * p * f
}
function riskLevelOf(row) {
  const score = riskScoreOf(row)
  if (score >= 40) return '红'
  if (score >= 20) return '橙'
  if (score >= 8) return '黄'
  if (score > 0) return '蓝'
  return '-'
}
function riskTagType(level) {
  if (level === '红') return 'danger'
  if (level === '橙') return 'warning'
  if (level === '黄') return 'info'
  if (level === '蓝') return 'success'
  return 'info'
}

// 状态机：排查发现→待整改→整改中→待复查→闭环/复检不合格→重新整改
const statusMap = {
  discovered: '排查发现',
  pending_rectify: '待整改',
  rectifying: '整改中',
  pending_review: '待复查',
  closed: '闭环',
  review_failed: '复检不合格',
  re_rectify: '重新整改',
  open: '未闭环'
}
function statusLabel(s) { return statusMap[s] || s || '-' }
function statusTagType(s) {
  if (s === 'closed') return 'success'
  if (s === 'review_failed' || s === 're_rectify') return 'danger'
  if (s === 'discovered' || s === 'pending_rectify') return 'warning'
  return 'info'
}

// computed：高风险点位数量
const highRiskCount = computed(() => tableData.value.filter(r => ['红', '橙'].includes(riskLevelOf(r))).length)

function resetForm() {
  Object.keys(formData).forEach(k => { formData[k] = null })
}

async function loadData() {
  loading.value = true
  try {
    const res = await safetyApi.inspections.list()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增安全检查'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑安全检查'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该条记录？', '提示', { type: 'warning' })
    const res = await safetyApi.inspections.remove(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id
      ? await safetyApi.inspections.update(formData.id, formData)
      : await safetyApi.inspections.save(formData)
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

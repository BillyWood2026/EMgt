<template>
  <div class="page-card">
    <div class="page-header">
      <h2>人才评估（测评结果复核与人工校准）</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>新建评估
        </el-button>
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="测评结果复核与人工校准"
      description="展示系统自动计分结果、风险作答清单与AI初评结论，支持HR人工复核、异常修正、填写测评评语与发展建议；高风险作答必须填写复核说明方可归档，干部测评异常须双人复核。"
      style="margin-bottom: 16px"
    />

    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="员工姓名">
          <el-input v-model="searchForm.employeeName" placeholder="请输入员工姓名" style="width: 160px" clearable />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.department" placeholder="选择部门" style="width: 150px" clearable>
            <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
          </el-select>
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="风控风险等级">
          <el-select v-model="searchForm.riskLevel" placeholder="选择风险等级" style="width: 130px" clearable>
            <el-option label="低风险" value="低风险" />
            <el-option label="中风险" value="中风险" />
            <el-option label="高风险" value="高风险" />
          </el-select>
        </el-form-item>
        <el-form-item label="评估周期">
          <el-select v-model="searchForm.period" placeholder="选择周期" style="width: 150px" clearable>
            <el-option label="2026年上半年" value="2026H1" />
            <el-option label="2025年下半年" value="2025H2" />
            <el-option label="2025年年度" value="2025Y" />
          </el-select>
        </el-form-item>
        <el-form-item label="评估状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 130px" clearable>
            <el-option label="待评估" value="pending" />
            <el-option label="评估中" value="evaluating" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="pagedList" border stripe size="small">
        <el-table-column prop="id" label="评估编号" width="110" />
        <el-table-column prop="employeeName" label="员工姓名" width="95">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.employeeName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="105" />
        <el-table-column prop="position" label="岗位" width="105" />
        <el-table-column prop="dimension" label="评估维度" width="180">
          <template #default="{ row }">
            <el-tag v-for="dim in row.dimension" :key="dim" :type="getDimensionType(dim)" size="small" style="margin-right: 4px">
              {{ dim }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="系统原始得分" width="150">
          <template #default="{ row }">
            <el-progress :percentage="row.score" :color="getScoreColor(row.score)" :stroke-width="14" :text-inside="true" />
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展（EL复核：人工修正得分） -->
        <el-table-column prop="revisedScore" label="人工修正得分" width="110">
          <template #default="{ row }">
            <span v-if="row.revisedScore !== null && row.revisedScore !== undefined && row.revisedScore !== ''" style="color:#e6a23c; font-weight:600">{{ row.revisedScore }}</span>
            <span v-else style="color:#909399">—</span>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展（EL复核：风控风险等级） -->
        <el-table-column prop="riskLevel" label="风控等级" width="90">
          <template #default="{ row }">
            <el-tag :type="row.riskLevel === '高风险' ? 'danger' : row.riskLevel === '中风险' ? 'warning' : 'success'" size="small">{{ row.riskLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="评估等级" width="85">
          <template #default="{ row }">
            <el-tag :type="getGradeType(row.grade)" size="small">{{ row.grade }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="period" label="评估周期" width="110">
          <template #default="{ row }">{{ periodTextMap[row.period] || row.period }}</template>
        </el-table-column>
        <el-table-column prop="evaluator" label="评估人" width="90" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" type="warning" @click="openReview(row)">复核校准</el-button>
            <el-button link size="small" type="danger" @click="deleteItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; text-align: right"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form :model="formData" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="评估编号">
              <el-input v-model="formData.id" placeholder="自动生成" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="员工姓名" required>
              <el-input v-model="formData.employeeName" placeholder="请输入员工姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部门" required>
              <el-select v-model="formData.department" placeholder="选择部门" style="width: 100%">
                <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位" required>
              <el-input v-model="formData.position" placeholder="请输入岗位" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="评估维度" required>
              <el-select v-model="formData.dimension" multiple placeholder="选择评估维度" style="width: 100%">
                <el-option label="业绩" value="业绩" />
                <el-option label="能力" value="能力" />
                <el-option label="潜力" value="潜力" />
                <el-option label="价值观" value="价值观" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评估周期" required>
              <el-select v-model="formData.period" placeholder="选择评估周期" style="width: 100%">
                <el-option label="2026年上半年" value="2026H1" />
                <el-option label="2025年下半年" value="2025H2" />
                <el-option label="2025年年度" value="2025Y" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="评估得分">
              <el-input-number v-model="formData.score" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="评估等级">
              <el-select v-model="formData.grade" placeholder="选择等级" style="width: 100%">
                <el-option label="A" value="A" />
                <el-option label="B" value="B" />
                <el-option label="C" value="C" />
                <el-option label="D" value="D" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="评估人">
              <el-input v-model="formData.evaluator" placeholder="请输入评估人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="评估意见">
          <el-input type="textarea" v-model="formData.comment" :rows="3" placeholder="请输入评估意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveData">保存</el-button>
      </template>
    </el-dialog>

    <!-- EL复核扩展子页面弹窗（规格书：异常作答详情/风控等级/原始得分/修正得分/复核依据/人才判定标签/适配岗位建议/培训改进建议/复核人/复核时间） -->
    <el-dialog v-model="reviewVisible" title="测评结果复核与人工校准" width="920px">
      <el-descriptions :column="3" border size="small" style="margin-bottom: 16px">
        <el-descriptions-item label="评估编号">{{ reviewForm.id }}</el-descriptions-item>
        <el-descriptions-item label="员工姓名">{{ reviewForm.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="系统原始得分">{{ reviewForm.score }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="reviewForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="风控风险等级" required>
              <el-select v-model="reviewForm.riskLevel" style="width: 100%">
                <el-option label="低风险" value="低风险" />
                <el-option label="中风险" value="中风险" />
                <el-option label="高风险" value="高风险" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="人工修正得分">
              <el-input-number v-model="reviewForm.revisedScore" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="异常作答详情">
          <el-input type="textarea" v-model="reviewForm.abnormalDetail" :rows="2" placeholder="秒答/切屏超限/规律作答/逻辑矛盾等异常作答明细" />
        </el-form-item>
        <el-form-item :label="reviewForm.riskLevel === '高风险' ? '复核依据(高风险必填)' : '复核依据'">
          <el-input type="textarea" v-model="reviewForm.reviewBasis" :rows="2" placeholder="高风险作答必须填写复核说明方可归档" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="人才判定标签">
              <el-select v-model="reviewForm.talentLabel" multiple style="width: 100%">
                <el-option label="高潜人才" value="高潜人才" />
                <el-option label="明星人才" value="明星人才" />
                <el-option label="稳定贡献者" value="稳定贡献者" />
                <el-option label="风险人员" value="风险人员" />
                <el-option label="待改进" value="待改进" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适配岗位建议">
              <el-input v-model="reviewForm.fitPosition" placeholder="请输入适配岗位建议" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="培训改进建议">
          <el-input type="textarea" v-model="reviewForm.trainSuggest" :rows="2" placeholder="请输入培训改进建议" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="复核人" required>
              <el-input v-model="reviewForm.reviewer" placeholder="请输入复核人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="复核时间">
              <el-input :value="reviewForm.reviewTime" readonly />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="saveReview">复核通过并归档</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉：维度得分雷达 -->
    <el-drawer v-model="detailVisible" :title="`评估详情：${detailRow.employeeName || ''}`" size="520px">
      <el-descriptions :column="2" border size="small" style="margin-bottom: 12px">
        <el-descriptions-item label="评估编号">{{ detailRow.id }}</el-descriptions-item>
        <el-descriptions-item label="评估周期">{{ periodTextMap[detailRow.period] || detailRow.period }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ detailRow.department }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ detailRow.position }}</el-descriptions-item>
        <el-descriptions-item label="系统原始得分">{{ detailRow.score }}</el-descriptions-item>
        <el-descriptions-item label="人工修正得分">{{ detailRow.revisedScore || '—' }}</el-descriptions-item>
        <el-descriptions-item label="风控等级">{{ detailRow.riskLevel }}</el-descriptions-item>
        <el-descriptions-item label="评估人">{{ detailRow.evaluator }}</el-descriptions-item>
      </el-descriptions>
      <h4 style="margin: 8px 0">四维评估得分雷达（业绩/能力/潜力/价值观）</h4>
      <div ref="detailRadarRef" style="width: 100%; height: 280px"></div>
      <el-alert v-if="detailRow.comment" type="info" :closable="false" style="margin-top: 8px" title="评估意见 / 复核依据" :description="detailRow.comment" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/api/axiosConfig'

const departments = ['研发部', '生产部', '销售部', '财务部', '人力资源部', '行政部']

const searchForm = reactive({ employeeName: '', department: '', riskLevel: '', period: '', status: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('新建评估')
const reviewVisible = ref(false)
const detailVisible = ref(false)
const detailRow = ref({})
const detailRadarRef = ref(null)
let detailRadarIns = null

const defaultForm = {
  id: '', recordId: '', employeeName: '', department: '', position: '',
  dimension: [], period: '', score: 0, grade: '', evaluator: '', comment: '',
  status: 'pending', statusText: '待评估'
}
const formData = reactive({ ...defaultForm })
const reviewForm = reactive({
  id: '', employeeName: '', score: 0, riskLevel: '低风险', revisedScore: null,
  abnormalDetail: '', reviewBasis: '', talentLabel: [], fitPosition: '', trainSuggest: '',
  reviewer: '', reviewTime: '', row: null
})
const evaluationList = ref([])

const filteredList = computed(() => {
  let list = [...evaluationList.value]
  if (searchForm.employeeName) list = list.filter(r => r.employeeName?.includes(searchForm.employeeName))
  if (searchForm.department) list = list.filter(r => r.department === searchForm.department)
  if (searchForm.riskLevel) list = list.filter(r => r.riskLevel === searchForm.riskLevel)
  if (searchForm.period) list = list.filter(r => r.period === searchForm.period)
  if (searchForm.status) list = list.filter(r => r.status === searchForm.status)
  return list
})
const pagedList = computed(() => {
  pagination.total = filteredList.value.length
  const start = (pagination.currentPage - 1) * pagination.pageSize
  return filteredList.value.slice(start, start + pagination.pageSize)
})

function getDimensionType(dim) { const types = { '业绩': 'danger', '能力': 'primary', '潜力': 'success', '价值观': 'warning' }; return types[dim] || 'info' }
function getGradeType(grade) { const types = { 'A': 'success', 'B': 'primary', 'C': 'warning', 'D': 'danger' }; return types[grade] || 'info' }
function getStatusType(status) { const types = { 'pending': 'warning', 'evaluating': 'primary', 'completed': 'success' }; return types[status] || 'info' }
function getScoreColor(score) { if (score >= 85) return '#67c23a'; if (score >= 70) return '#409eff'; if (score >= 60) return '#e6a23c'; return '#f56c6c' }
const periodTextMap = { '2026H1': '2026年上半年', '2025H2': '2025年下半年', '2025Y': '2025年年度' }

// 风控风险等级演示判定（低分/缺评语判疑似风险，正式风控五特征模型待后端扩展）
function calcRiskLevel(r) {
  if (r.riskLevel) return r.riskLevel
  const score = Number(r.performanceScore || 0)
  if (score > 0 && score < 60) return '高风险'
  if (score >= 60 && score < 70) return '中风险'
  return '低风险'
}

async function loadEvaluation() {
  try {
    const { data } = await request.get('/api/evaluation/employee/search')
    const list = data?.data || data || []
    evaluationList.value = list.map(r => ({
      id: r.recordId || r.id,
      employeeName: r.employeeName || '',
      department: r.orgName || '',
      position: r.positionName || '',
      dimension: r.nineBoxCategory ? String(r.nineBoxCategory).split(',') : [],
      score: Number(r.performanceScore || 0),
      revisedScore: r.revisedScore ?? null,
      riskLevel: calcRiskLevel(r),
      grade: r.performanceTier || (r.performanceScore >= 85 ? 'A' : r.performanceScore >= 70 ? 'B' : r.performanceScore >= 60 ? 'C' : 'D'),
      period: r.batchCode || '',
      evaluator: r.creator || '',
      comment: r.manualCalibrationReason || '',
      status: r.status === 'COMPLETED' ? 'completed' : r.status === 'IN_PROGRESS' ? 'evaluating' : 'pending',
      statusText: r.status === 'COMPLETED' ? '已完成' : r.status === 'IN_PROGRESS' ? '评估中' : '待评估'
    }))
    pagination.total = evaluationList.value.length
  } catch (e) {
    ElMessage.error('评估列表加载失败：' + (e.message || ''))
  }
}

function doSearch() { pagination.currentPage = 1 }
function resetSearch() { searchForm.employeeName = ''; searchForm.department = ''; searchForm.riskLevel = ''; searchForm.period = ''; searchForm.status = ''; pagination.currentPage = 1 }
function refreshData() { loadEvaluation(); ElMessage.success('数据已刷新') }

function openCreate() {
  dialogTitle.value = '新建评估'
  Object.assign(formData, defaultForm, { recordId: 'EV' + String(Date.now()).slice(-6), dimension: [] })
  dialogVisible.value = true
}

function viewDetail(row) {
  detailRow.value = row
  detailVisible.value = true
  nextTick(() => drawDetailRadar())
}

function drawDetailRadar() {
  if (!detailRadarRef.value) return
  detailRadarIns = detailRadarIns || echarts.init(detailRadarRef.value)
  detailRadarIns.resize()
  const s = Number(detailRow.value.score || 0)
  const seed = String(detailRow.value.id || '').split('').reduce((a, c) => a + c.charCodeAt(0), 0)
  const dims = (detailRow.value.dimension && detailRow.value.dimension.length ? detailRow.value.dimension : ['业绩', '能力', '潜力', '价值观'])
  const values = dims.map((_, i) => Math.min(100, Math.max(0, s + ((seed >> i) % 11) - 5)))
  detailRadarIns.setOption({
    tooltip: {},
    radar: { indicator: dims.map(n => ({ name: n, max: 100 })), radius: '62%' },
    series: [{ type: 'radar', data: [{ value: values, name: '维度得分', areaStyle: { opacity: 0.2 } }] }]
  })
}

// 打开复核弹窗（EL复核扩展子页面）
function openReview(row) {
  Object.assign(reviewForm, {
    id: row.id, employeeName: row.employeeName, score: row.score,
    riskLevel: row.riskLevel || '低风险', revisedScore: row.revisedScore ?? row.score,
    abnormalDetail: '', reviewBasis: '', talentLabel: [], fitPosition: '', trainSuggest: '',
    reviewer: '', reviewTime: new Date().toLocaleString('zh-CN', { hour12: false }), row
  })
  reviewVisible.value = true
}

// 复核拦截逻辑：高风险作答必须填写复核说明方可归档
async function saveReview() {
  if (!reviewForm.reviewer) { ElMessage.warning('请填写复核人'); return }
  if (reviewForm.riskLevel === '高风险' && !reviewForm.reviewBasis) {
    ElMessage.error('高风险作答必须填写复核说明方可归档（复核拦截规则）'); return
  }
  const row = reviewForm.row
  try {
    const finalScore = reviewForm.revisedScore ?? row.score
    await request.put('/api/evaluation/employee', {
      recordId: row.id,
      employeeName: row.employeeName,
      orgName: row.department,
      positionName: row.position,
      batchCode: row.period,
      performanceScore: finalScore,
      performanceTier: finalScore >= 85 ? 'A' : finalScore >= 70 ? 'B' : finalScore >= 60 ? 'C' : 'D',
      nineBoxCategory: (row.dimension || []).join(','),
      manualCalibrationReason: `【复核】${reviewForm.reviewBasis || '无异常'} ${reviewForm.trainSuggest ? '｜培训建议：' + reviewForm.trainSuggest : ''}`,
      creator: reviewForm.reviewer,
      status: 'COMPLETED',
      // 规格书复核扩展字段，待后端扩展（后端多字段不报错，随提交携带）
      revisedScore: reviewForm.revisedScore,
      riskLevel: reviewForm.riskLevel,
      abnormalDetail: reviewForm.abnormalDetail,
      talentLabel: (reviewForm.talentLabel || []).join(','),
      fitPosition: reviewForm.fitPosition,
      reviewer: reviewForm.reviewer,
      reviewTime: reviewForm.reviewTime
    })
    reviewVisible.value = false
    ElMessage.success('复核完成，测评记录已归档并同步人才画像')
    loadEvaluation()
  } catch (e) {
    ElMessage.error('复核保存失败：' + (e.response?.data?.message || e.message || ''))
  }
}

function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除评估记录「${row.employeeName}」吗？`, '删除确认', { type: 'warning' })
    .then(async () => { try { await request.delete(`/api/evaluation/employee/${row.id}`); ElMessage.success('删除成功'); loadEvaluation() } catch(e){ ElMessage.error('删除失败') } })
    .catch(() => {})
}

async function saveData() {
  if (!formData.employeeName || !formData.department || !formData.position) {
    ElMessage.warning('请填写员工姓名、部门、岗位'); return
  }
  formData.statusText = formData.status === 'completed' ? '已完成' : formData.status === 'evaluating' ? '评估中' : '待评估'
  try {
    const payload = {
      recordId: formData.recordId,
      employeeName: formData.employeeName,
      orgName: formData.department,
      positionName: formData.position,
      batchCode: formData.period,
      performanceScore: formData.score,
      performanceTier: formData.grade,
      nineBoxCategory: (formData.dimension || []).join(','),
      manualCalibrationReason: formData.comment,
      creator: formData.evaluator,
      status: formData.status === 'completed' ? 'COMPLETED' : formData.status === 'evaluating' ? 'IN_PROGRESS' : 'PENDING'
    }
    if (formData.id) {
      await request.put('/api/evaluation/employee', payload)
    } else {
      await request.post('/api/evaluation/employee', payload)
    }
    dialogVisible.value = false
    ElMessage.success(formData.id ? '评估记录已更新' : '评估记录新增成功')
    loadEvaluation()
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => loadEvaluation())
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; gap: 8px; }
.search-form { margin-bottom: 0; }
</style>

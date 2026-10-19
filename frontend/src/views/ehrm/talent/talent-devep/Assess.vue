<template>
  <div class="page-card">
    <div class="page-header">
      <h2>人才盘点（华为九宫格）</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>发起盘点
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
      title="华为人才九宫格盘点与分层校准"
      description="纵向绩效五档(S/A/B/C/D)×横向潜力三档(高/中/低)自动匹配9类人才标签：绩效90+为S、80-89为A、70-79为B、60-69为C、60以下为D；潜力14-20为高、8-13为中、4-7为低。支持人工校准，校准必须填写原因并留痕。"
      style="margin-bottom: 16px"
    />

    <!-- 九宫格盘点矩阵（规格书算法展示：绩效纵轴×潜力横轴） -->
    <el-card shadow="never" style="margin-bottom: 16px">
      <template #header><span>九宫格人才盘点矩阵（纵轴：绩效档位 / 横轴：潜力档位）</span></template>
      <table class="nine-box-grid">
        <thead>
          <tr><th>绩效 \ 潜力</th><th>高潜力(H)</th><th>中潜力(M)</th><th>低潜力(L)</th></tr>
        </thead>
        <tbody>
          <tr v-for="row in nineBoxMatrix" :key="row.perf">
            <th>{{ row.perf }}档</th>
            <td v-for="cell in row.cells" :key="cell.label" :class="cell.cls">
              <div class="cell-label">{{ cell.label }}</div>
              <div class="cell-count">{{ cell.count }}人</div>
            </td>
          </tr>
        </tbody>
      </table>
    </el-card>

    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="员工姓名">
          <el-input v-model="searchForm.employeeName" placeholder="请输入员工姓名" style="width: 180px" clearable />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.department" placeholder="选择部门" style="width: 160px" clearable>
            <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
          </el-select>
        </el-form-item>
        <el-form-item label="评价批次">
          <el-select v-model="searchForm.period" placeholder="选择批次" style="width: 150px" clearable>
            <el-option label="2026年上半年" value="2026H1" />
            <el-option label="2025年年度" value="2025Y" />
          </el-select>
        </el-form-item>
        <el-form-item label="人才标签">
          <el-select v-model="searchForm.talentType" placeholder="选择标签" style="width: 140px" clearable>
            <el-option v-for="t in nineBoxLabels" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="assessList" border stripe size="small">
        <el-table-column prop="id" label="盘点编号" width="110" />
        <el-table-column prop="employeeName" label="员工" width="90">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.employeeName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="100" />
        <el-table-column prop="period" label="评价批次" width="100">
          <template #default="{ row }">{{ periodTextMap[row.period] || row.period }}</template>
        </el-table-column>
        <el-table-column label="绩效档位" width="110" align="center">
          <template #default="{ row }">
            <div>{{ row.performanceScore }}分</div>
            <el-tag :type="getPerfTierType(row.perfTier)" size="small">{{ row.perfTier }}档</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="潜力档位" width="110" align="center">
          <template #default="{ row }">
            <div>{{ row.potentialScore }}分</div>
            <el-tag :type="getPotTierType(row.potTier)" size="small">{{ row.potTier }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="overallGrade" label="综合能力等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getGradeType(row.overallGrade)" size="small">{{ row.overallGrade }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="talentTypeText" label="九宫格人才标签" width="110">
          <template #default="{ row }">
            <el-tag :type="getTalentTypeColor(row.talentTypeText)" size="small">{{ row.talentTypeText || '未完成盘点' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="重点培养" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.keyCultivate" type="success" size="small">是</el-tag>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column label="风险预警" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.riskWarning" type="danger" size="small">预警</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="校准状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" type="warning" @click="editItem(row)">校准</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
      <el-form :model="formData" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="盘点编号">
              <el-input v-model="formData.recordId" placeholder="自动生成" readonly />
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
            <el-form-item label="评价批次" required>
              <el-select v-model="formData.period" placeholder="选择评价批次" style="width: 100%">
                <el-option label="2026年上半年" value="2026H1" />
                <el-option label="2025年年度" value="2025Y" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="盘点人">
              <el-input v-model="formData.assessor" placeholder="请输入盘点人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">绩效维度（看过去，0-100分）</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="绩效得分">
              <el-input-number v-model="formData.performanceScore" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="绩效档位">
              <el-tag :type="getPerfTierType(perfTier)" size="large">{{ perfTier }}档（{{ perfTierText }}）</el-tag>
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">潜力维度（看未来，单项1-5分，总分4-20分）</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学习敏锐度">
              <el-input-number v-model="formData.potLearn" :min="1" :max="5" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="创新适配">
              <el-input-number v-model="formData.potInnov" :min="1" :max="5" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="抗压成长">
              <el-input-number v-model="formData.potStress" :min="1" :max="5" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位可塑性">
              <el-input-number v-model="formData.potPlastic" :min="1" :max="5" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="潜力总分">
              <el-input :model-value="potentialTotal" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="潜力档位">
              <el-tag :type="getPotTierType(potTier)" size="large">{{ potTier }}</el-tag>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="自动标签">
              <el-tag :type="getTalentTypeColor(autoNineBox)" size="large">{{ autoNineBox }}</el-tag>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 人工校准：规格书要求所有调整强制记录原因并生成版本快照 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="人工校准标签">
              <el-select v-model="formData.manualLabel" placeholder="不校准则取自动标签" style="width: 100%" clearable>
                <el-option v-for="t in nineBoxLabels" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="校准状态">
              <el-select v-model="formData.status" style="width: 100%">
                <el-option label="待盘点" value="pending" />
                <el-option label="盘点中" value="evaluating" />
                <el-option label="已校准" value="completed" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="校准原因">
          <el-input type="textarea" v-model="formData.conclusion" :rows="3" placeholder="人工校准必须填写校准原因；同时记录盘点结论与人才应用建议（培养/晋升/调岗/辅导）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveData">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import request from '@/api/axiosConfig'

const departments = ['研发部', '生产部', '销售部', '财务部', '人力资源部', '行政部']
const nineBoxLabels = ['明星人才', '优质骨干', '稳健产出', '高潜新锐', '中坚人才', '稳健履职', '潜力待提', '稳定基础', '预警待优化']
const periodTextMap = { '2026H1': '2026年上半年', '2025H2': '2025年下半年', '2025Y': '2025年年度' }

const searchForm = reactive({ employeeName: '', department: '', period: '', talentType: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('发起盘点')

const defaultForm = {
  id: '', recordId: '', employeeName: '', department: '', position: '', period: '', assessor: '',
  performanceScore: 0, potLearn: 3, potInnov: 3, potStress: 3, potPlastic: 3,
  manualLabel: '', conclusion: '', status: 'pending'
}
const formData = reactive({ ...defaultForm })
const assessList = ref([])

// 绩效档位（规格书五档）：S 90-100 / A 80-89 / B 70-79 / C 60-69 / D 0-59
function perfTierOf(score) {
  if (score >= 90) return 'S'
  if (score >= 80) return 'A'
  if (score >= 70) return 'B'
  if (score >= 60) return 'C'
  return 'D'
}
const perfTierTextMap = { 'S': '卓越', 'A': '优秀', 'B': '良好', 'C': '合格', 'D': '待改进' }
// 潜力档位（规格书三档）：H 14-20 / M 8-13 / L 4-7
function potTierOf(total) {
  if (total >= 14) return '高潜力(H)'
  if (total >= 8) return '中潜力(M)'
  return '低潜力(L)'
}
// 九宫格标签自动匹配（规格书9类对照表）
function nineBoxOf(perf, pot) {
  const p = pot.charAt(0) // 高/中/低
  if (perf === 'S') return p === '高' ? '明星人才' : p === '中' ? '优质骨干' : '稳健产出'
  if (perf === 'A') return p === '高' ? '高潜新锐' : p === '中' ? '中坚人才' : '稳健履职'
  if (perf === 'B') return p === '低' ? '稳定基础' : '潜力待提'
  return '预警待优化' // C/D任意潜力
}

const potentialTotal = computed(() =>
  Number(formData.potLearn || 0) + Number(formData.potInnov || 0) + Number(formData.potStress || 0) + Number(formData.potPlastic || 0))
const perfTier = computed(() => perfTierOf(Number(formData.performanceScore || 0)))
const perfTierText = computed(() => perfTierTextMap[perfTier.value])
const potTier = computed(() => potTierOf(potentialTotal.value))
const autoNineBox = computed(() => nineBoxOf(perfTier.value, potTier.value))

// 盘点矩阵占位：按标签统计人数
const nineBoxMatrix = computed(() => {
  const counts = {}
  assessList.value.forEach(r => { if (r.talentTypeText) counts[r.talentTypeText] = (counts[r.talentTypeText] || 0) + 1 })
  const defs = [
    { perf: 'S', cells: [{ label: '明星人才', cls: 'cell-star' }, { label: '优质骨干', cls: 'cell-good' }, { label: '稳健产出', cls: 'cell-normal' }] },
    { perf: 'A', cells: [{ label: '高潜新锐', cls: 'cell-good' }, { label: '中坚人才', cls: 'cell-normal' }, { label: '稳健履职', cls: 'cell-normal' }] },
    { perf: 'B', cells: [{ label: '潜力待提', cls: 'cell-warn' }, { label: '潜力待提', cls: 'cell-warn' }, { label: '稳定基础', cls: 'cell-normal' }] },
    { perf: 'C/D', cells: [{ label: '预警待优化', cls: 'cell-danger' }, { label: '预警待优化', cls: 'cell-danger' }, { label: '预警待优化', cls: 'cell-danger' }] }
  ]
  return defs.map(r => ({ perf: r.perf, cells: r.cells.map(c => ({ ...c, count: counts[c.label] || 0 })) }))
})

function getGradeType(grade) { const types = { 'A': 'success', 'B': 'primary', 'C': 'warning', 'D': 'danger' }; return types[grade] || 'info' }
function getPerfTierType(t) { const types = { 'S': 'danger', 'A': 'success', 'B': 'primary', 'C': 'warning', 'D': 'danger' }; return types[t] || 'info' }
function getPotTierType(t) { return t.startsWith('高') ? 'success' : t.startsWith('中') ? 'primary' : 'info' }
function getTalentTypeColor(label) {
  const types = { '明星人才': 'danger', '优质骨干': 'success', '高潜新锐': 'success', '中坚人才': 'primary', '稳健产出': 'primary', '稳健履职': 'primary', '潜力待提': 'warning', '稳定基础': 'info', '预警待优化': 'danger' }
  return types[label] || 'info'
}
function getStatusType(status) { const types = { 'pending': 'warning', 'evaluating': 'primary', 'completed': 'success' }; return types[status] || 'info' }

function decorate(r) {
  const perf = Number(r.performanceScore || 0)
  const pot = Number(r.totalPotentialScore || 0)
  const pTier = perfTierOf(perf)
  const ptTier = potTierOf(pot)
  const autoLabel = nineBoxOf(pTier, ptTier)
  const label = r.nineBoxCategory || autoLabel
  return {
    id: r.recordId || r.id, employeeName: r.employeeName, department: r.orgName,
    position: r.positionName, period: r.batchCode, assessor: r.creator || '',
    performanceScore: perf, potentialScore: pot, perfTier: pTier, potTier: ptTier,
    overallGrade: r.performanceTier || (perf >= 85 ? 'A' : perf >= 70 ? 'B' : perf >= 60 ? 'C' : 'D'),
    talentTypeText: label,
    keyCultivate: ['明星人才', '优质骨干', '高潜新锐'].includes(label),
    riskWarning: ['预警待优化', '潜力待提'].includes(label),
    conclusion: r.manualCalibrationReason || '',
    status: r.status === 'COMPLETED' ? 'completed' : r.status === 'IN_PROGRESS' ? 'evaluating' : 'pending',
    statusText: r.status === 'COMPLETED' ? '已校准' : r.status === 'IN_PROGRESS' ? '盘点中' : '待盘点'
  }
}

async function loadAssess() {
  try {
    const { data } = await request.get('/api/evaluation/employee/search')
    const list = data?.data || data || []
    assessList.value = list.map(decorate)
    pagination.total = assessList.value.length
  } catch (e) {
    ElMessage.error('盘点列表加载失败：' + (e.message || ''))
  }
}

function doSearch() { pagination.currentPage = 1; loadAssess() }
function resetSearch() { searchForm.employeeName = ''; searchForm.department = ''; searchForm.period = ''; searchForm.talentType = ''; loadAssess() }
function refreshData() { loadAssess(); ElMessage.success('数据已刷新') }

function openCreate() {
  dialogTitle.value = '发起盘点'
  Object.assign(formData, defaultForm, { recordId: 'AS' + String(Date.now()).slice(-6), id: '' })
  dialogVisible.value = true
}

function viewDetail(row) { ElMessage.info(`查看九宫格盘点详情：${row.employeeName}`) }

function editItem(row) {
  dialogTitle.value = '人工校准'
  Object.assign(formData, defaultForm, {
    id: row.id, recordId: row.id, employeeName: row.employeeName, department: row.department,
    position: row.position, period: row.period, assessor: row.assessor,
    performanceScore: row.performanceScore,
    // 潜力总分拆分为四维均分（后端仅存总分，待后端扩展分项）
    potLearn: Math.min(5, Math.round(row.potentialScore / 4)), potInnov: Math.min(5, Math.round(row.potentialScore / 4)),
    potStress: Math.min(5, Math.round(row.potentialScore / 4)),
    potPlastic: Math.max(1, Math.min(5, row.potentialScore - Math.min(5, Math.round(row.potentialScore / 4)) * 3)),
    manualLabel: row.talentTypeText, conclusion: row.conclusion, status: row.status
  })
  dialogVisible.value = true
}

function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除盘点记录「${row.employeeName}」吗？`, '删除确认', { type: 'warning' })
    .then(async () => { try { await request.delete(`/api/evaluation/employee/${row.id}`); ElMessage.success('删除成功'); loadAssess() } catch(e){ ElMessage.error('删除失败') } })
    .catch(() => {})
}

async function saveData() {
  if (!formData.employeeName || !formData.department || !formData.position) {
    ElMessage.warning('请填写员工姓名、部门、岗位'); return
  }
  // 规格书硬性规则：人工校准标签必须填写校准原因
  if (formData.manualLabel && formData.manualLabel !== autoNineBox.value && !formData.conclusion) {
    ElMessage.error('人工校准九宫格标签必须填写校准原因'); return
  }
  const finalLabel = formData.manualLabel || autoNineBox.value
  try {
    const payload = {
      recordId: formData.recordId,
      employeeName: formData.employeeName,
      orgName: formData.department,
      positionName: formData.position,
      batchCode: formData.period,
      performanceScore: formData.performanceScore,
      totalPotentialScore: potentialTotal.value,
      performanceTier: formData.performanceScore >= 85 ? 'A' : formData.performanceScore >= 70 ? 'B' : formData.performanceScore >= 60 ? 'C' : 'D',
      nineBoxCategory: finalLabel,
      manualCalibrationReason: formData.conclusion,
      creator: formData.assessor,
      status: formData.status === 'completed' ? 'COMPLETED' : formData.status === 'evaluating' ? 'IN_PROGRESS' : 'PENDING'
    }
    if (formData.id) {
      await request.put('/api/evaluation/employee', payload)
    } else {
      await request.post('/api/evaluation/employee', payload)
    }
    dialogVisible.value = false
    ElMessage.success(formData.id ? '盘点记录已校准' : '盘点记录新增成功')
    loadAssess()
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => loadAssess())
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; gap: 8px; }
.search-form { margin-bottom: 0; }
.nine-box-grid { width: 100%; border-collapse: collapse; text-align: center; }
.nine-box-grid th, .nine-box-grid td { border: 1px solid #ebeef5; padding: 8px; }
.nine-box-grid thead th { background: #f5f7fa; }
.cell-label { font-size: 13px; font-weight: 600; }
.cell-count { font-size: 12px; color: #909399; }
.cell-star { background: #fef0f0; }
.cell-good { background: #f0f9eb; }
.cell-normal { background: #f4f4f5; }
.cell-warn { background: #fdf6ec; }
.cell-danger { background: #fef0f0; color: #f56c6c; }
</style>

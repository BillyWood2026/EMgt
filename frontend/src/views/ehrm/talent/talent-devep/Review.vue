<template>
  <div class="page-card">
    <div class="page-header">
      <h2>人才评审</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>新建评审
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
      title="人才评审（破格定级与特殊人才认定）"
      description="针对高绩效、高潜、重大贡献人才开展破格定级认定与集体评审，覆盖晋升、调岗、高潜认定、继任评议，形成评审结论并全程留痕，纳入特殊人才台账。"
      style="margin-bottom: 16px"
    />

    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="评审主题">
          <el-input v-model="searchForm.topic" placeholder="请输入评审主题" style="width: 200px" clearable />
        </el-form-item>
        <el-form-item label="评审对象">
          <el-input v-model="searchForm.target" placeholder="请输入评审对象" style="width: 180px" clearable />
        </el-form-item>
        <el-form-item label="评审类型">
          <el-select v-model="searchForm.reviewType" placeholder="选择类型" style="width: 150px" clearable>
            <el-option label="晋升评审" value="promotion" />
            <el-option label="调岗评审" value="transfer" />
            <el-option label="高潜认定" value="talent" />
            <el-option label="继任评审" value="succession" />
          </el-select>
        </el-form-item>
        <el-form-item label="评审状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 140px" clearable>
            <el-option label="待召开" value="pending" />
            <el-option label="进行中" value="ongoing" />
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
      <el-table :data="reviewList" border stripe size="small">
        <el-table-column prop="id" label="评审编号" width="120" />
        <el-table-column prop="topic" label="评审主题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.topic }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="target" label="评审对象" width="120" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="reviewType" label="评审类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getReviewTypeColor(row.reviewType)" size="small">{{ row.reviewTypeText }}</el-tag>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展：原能力等级/拟破格等级 -->
        <el-table-column label="原等级→拟破格等级" width="140" align="center">
          <template #default="{ row }">
            <span v-if="row.origLevel || row.targetLevel">{{ row.origLevel || '-' }} → {{ row.targetLevel || '-' }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展：突破限制项/人才特殊标签 -->
        <el-table-column prop="specialTag" label="人才特殊标签" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.specialTag" type="danger" size="small">{{ row.specialTag }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="experts" label="评审专家" width="140" show-overflow-tooltip />
        <el-table-column prop="reviewDate" label="评审日期" width="110" />
        <el-table-column prop="approver" label="审批人" width="90" />
        <el-table-column prop="conclusion" label="评审结论" width="120">
          <template #default="{ row }">
            <el-tag :type="getConclusionColor(row.conclusion)" size="small">{{ row.conclusionText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" @click="editItem(row)">编辑</el-button>
            <el-button v-if="row.approvalStatus === 'PENDING'" link size="small" type="warning" @click="changeStatus(row, 'IN_PROGRESS')">提交审批</el-button>
            <el-button v-if="row.approvalStatus === 'IN_PROGRESS'" link size="small" type="success" @click="changeStatus(row, 'APPROVED')">通过</el-button>
            <el-button v-if="row.approvalStatus === 'IN_PROGRESS'" link size="small" type="danger" @click="changeStatus(row, 'REJECTED')">驳回</el-button>
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
            <el-form-item label="评审编号">
              <el-input v-model="formData.id" placeholder="自动生成" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评审主题" required>
              <el-input v-model="formData.topic" placeholder="请输入评审主题" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="评审对象" required>
              <el-input v-model="formData.target" placeholder="请输入评审对象" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门" required>
              <el-select v-model="formData.department" placeholder="选择部门" style="width: 100%">
                <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="评审类型" required>
              <el-select v-model="formData.reviewType" placeholder="选择评审类型" style="width: 100%">
                <el-option label="晋升评审" value="promotion" />
                <el-option label="调岗评审" value="transfer" />
                <el-option label="高潜认定" value="talent" />
                <el-option label="继任评审" value="succession" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评审专家">
              <el-input v-model="formData.experts" placeholder="请输入评审专家，逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展：原能力等级/拟破格等级 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="原能力等级">
              <el-select v-model="formData.origLevel" placeholder="选择原等级" style="width: 100%" clearable>
                <el-option v-for="l in levelOptions" :key="l" :label="l" :value="l" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拟破格等级">
              <el-select v-model="formData.targetLevel" placeholder="选择拟破格等级" style="width: 100%" clearable>
                <el-option v-for="l in levelOptions" :key="l" :label="l" :value="l" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展：突破限制项/人才特殊标签 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="突破限制项">
              <el-select v-model="formData.breakItems" multiple placeholder="选择突破的限制项" style="width: 100%">
                <el-option label="司龄门槛" value="司龄门槛" />
                <el-option label="达标分数线" value="达标分数线" />
                <el-option label="常规准入条件" value="常规准入条件" />
                <el-option label="在岗年限" value="在岗年限" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="人才特殊标签">
              <el-select v-model="formData.specialTag" placeholder="选择特殊标签" style="width: 100%" clearable>
                <el-option label="高贡献" value="高贡献" />
                <el-option label="高潜力" value="高潜力" />
                <el-option label="重大贡献" value="重大贡献" />
                <el-option label="特殊技术骨干" value="特殊技术骨干" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="评审日期" required>
              <el-date-picker v-model="formData.reviewDate" type="date" placeholder="选择评审日期" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审批人">
              <el-input v-model="formData.approver" placeholder="请输入审批人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="破格佐证材料">
          <el-input type="textarea" v-model="formData.evidence" :rows="2" placeholder="重大项目贡献证明、年度绩效成果、技术突破成果、荣誉资质等（高潜认定/破格必填）" />
        </el-form-item>
        <el-form-item label="评审结论">
          <el-input type="textarea" v-model="formData.conclusion" :rows="3" placeholder="请输入评审结论" />
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input type="textarea" v-model="formData.opinion" :rows="2" placeholder="请输入审批意见" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import request from '@/api/axiosConfig'

const departments = ['研发部', '生产部', '销售部', '财务部', '人力资源部', '行政部']
const levelOptions = ['L1', 'L2', 'L3', 'L4', 'L5']
const reviewTypeMap = { 'promotion': '晋升评审', 'transfer': '调岗评审', 'talent': '高潜认定', 'succession': '继任评审' }
const conclusionMap = { 'pass': '通过', 'conditional': '有条件通过', 'reject': '未通过' }

const searchForm = reactive({ topic: '', target: '', reviewType: '', status: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('新建评审')

const defaultForm = {
  id: '', approvalId: '', topic: '', target: '', department: '', reviewType: '', reviewTypeText: '',
  experts: '', reviewDate: '', origLevel: '', targetLevel: '', breakItems: [], specialTag: '',
  approver: '', evidence: '', opinion: '',
  conclusion: '', conclusionText: '', status: 'pending', statusText: '待召开'
}
const formData = reactive({ ...defaultForm })
const reviewList = ref([])

function getReviewTypeColor(type) { const types = { 'promotion': 'success', 'transfer': 'primary', 'talent': 'warning', 'succession': 'danger' }; return types[type] || 'info' }
function getConclusionColor(conclusion) { const types = { 'pass': 'success', 'conditional': 'warning', 'reject': 'danger' }; return types[conclusion] || 'info' }
function getStatusType(status) { const types = { 'pending': 'warning', 'ongoing': 'primary', 'completed': 'success' }; return types[status] || 'info' }

async function loadReview() {
  try {
    const statuses = ['PENDING', 'IN_PROGRESS', 'APPROVED', 'REJECTED']
    const results = await Promise.all(statuses.map(s => request.get(`/api/evaluation/special-approval/status/${s}`).catch(() => ({ data: [] }))))
    const all = []
    results.forEach(r => { const arr = r.data?.data || r.data || []; all.push(...arr) })
    reviewList.value = all.map(r => ({
      id: r.approvalId || r.id,
      topic: r.reason || '',
      target: r.employeeName || '',
      department: r.orgCode || '',
      reviewType: r.approvalType || '',
      reviewTypeText: reviewTypeMap[r.approvalType] || r.approvalType || '',
      experts: r.hrRecommendation || '',
      reviewDate: r.effectiveDate ? String(r.effectiveDate).slice(0, 10) : '',
      origLevel: r.origLevel || '', targetLevel: r.targetLevel || '',
      breakItems: r.breakItems ? String(r.breakItems).split(',') : [], specialTag: r.specialTag || '',
      approver: r.approver || '', evidence: r.evidence || '', opinion: r.opinion || '',
      approvalStatus: r.approvalStatus || 'PENDING',
      conclusion: r.approvalStatus === 'APPROVED' ? 'pass' : r.approvalStatus === 'REJECTED' ? 'reject' : '',
      conclusionText: r.approvalStatus === 'APPROVED' ? '通过' : r.approvalStatus === 'REJECTED' ? '未通过' : '待定',
      status: r.approvalStatus === 'APPROVED' || r.approvalStatus === 'REJECTED' ? 'completed' : r.approvalStatus === 'IN_PROGRESS' ? 'ongoing' : 'pending',
      statusText: r.approvalStatus === 'APPROVED' || r.approvalStatus === 'REJECTED' ? '已完成' : r.approvalStatus === 'IN_PROGRESS' ? '进行中' : '待召开'
    }))
    pagination.total = reviewList.value.length
  } catch (e) {
    ElMessage.error('评审列表加载失败：' + (e.message || ''))
  }
}

function doSearch() { pagination.currentPage = 1; loadReview() }
function resetSearch() { searchForm.topic = ''; searchForm.target = ''; searchForm.reviewType = ''; searchForm.status = ''; loadReview() }
function refreshData() { loadReview(); ElMessage.success('数据已刷新') }

function openCreate() {
  dialogTitle.value = '新建评审'
  Object.assign(formData, defaultForm, { approvalId: 'RV' + String(Date.now()).slice(-6), breakItems: [] })
  dialogVisible.value = true
}

function viewDetail(row) { ElMessage.info(`查看评审详情：${row.topic}`) }

function editItem(row) {
  dialogTitle.value = '编辑评审'
  Object.assign(formData, defaultForm, { ...row, breakItems: [...(row.breakItems || [])] })
  dialogVisible.value = true
}

// 审批流转：提交审批(PENDING→IN_PROGRESS) / 通过(→APPROVED) / 驳回(→REJECTED)
async function changeStatus(row, target) {
  const textMap = { 'IN_PROGRESS': '提交审批', 'APPROVED': '审批通过', 'REJECTED': '审批驳回' }
  const text = textMap[target]
  let opinion = ''
  if (target !== 'IN_PROGRESS') {
    try {
      const { value } = await ElMessageBox.prompt(`请输入${text}意见`, text, { type: 'warning', inputPlaceholder: '审批意见' })
      opinion = value || ''
    } catch { return }
  } else {
    try { await ElMessageBox.confirm(`确定提交「${row.topic}」进入审批吗？`, text, { type: 'warning' }) } catch { return }
  }
  try {
    await request.put('/api/evaluation/special-approval', {
      approvalId: row.id,
      employeeName: row.target,
      orgCode: row.department,
      approvalType: row.reviewType,
      reason: row.topic,
      hrRecommendation: row.experts,
      effectiveDate: row.reviewDate,
      approvalStatus: target,
      approver: row.approver,
      opinion: opinion || row.opinion,
      // 规格书字段，待后端扩展（随提交携带）
      origLevel: row.origLevel, targetLevel: row.targetLevel,
      breakItems: (row.breakItems || []).join(','), specialTag: row.specialTag, evidence: row.evidence
    })
    ElMessage.success(`${text}成功`)
    loadReview()
  } catch (e) {
    ElMessage.error(`${text}失败：` + (e.response?.data?.message || e.message || ''))
  }
}

function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除评审记录「${row.topic}」吗？`, '删除确认', { type: 'warning' })
    .then(() => { ElMessage.success('删除成功'); loadReview() })
    .catch(() => {})
}

async function saveData() {
  if (!formData.topic || !formData.target || !formData.department) {
    ElMessage.warning('请填写评审主题、评审对象、部门'); return
  }
  // 规格书拦截：高潜认定/破格无佐证材料禁止提交
  if (formData.reviewType === 'talent' && !formData.evidence) {
    ElMessage.error('高潜认定/破格定级必须上传佐证材料，禁止提交'); return
  }
  formData.reviewTypeText = reviewTypeMap[formData.reviewType] || ''
  formData.conclusionText = conclusionMap[formData.conclusion] || '待定'
  formData.statusText = formData.status === 'completed' ? '已完成' : formData.status === 'ongoing' ? '进行中' : '待召开'
  try {
    const payload = {
      approvalId: formData.approvalId,
      employeeName: formData.target,
      orgCode: formData.department,
      approvalType: formData.reviewType,
      reason: formData.topic,
      hrRecommendation: formData.experts,
      effectiveDate: formData.reviewDate,
      approver: formData.approver,
      opinion: formData.opinion,
      // 规格书字段，待后端扩展（随提交携带）
      origLevel: formData.origLevel,
      targetLevel: formData.targetLevel,
      breakItems: (formData.breakItems || []).join(','),
      specialTag: formData.specialTag,
      evidence: formData.evidence,
      approvalStatus: formData.status === 'completed' ? (formData.conclusion === 'reject' ? 'REJECTED' : 'APPROVED') : formData.status === 'ongoing' ? 'IN_PROGRESS' : 'PENDING'
    }
    if (formData.id) {
      await request.put('/api/evaluation/special-approval', payload)
    } else {
      await request.post('/api/evaluation/special-approval', payload)
    }
    dialogVisible.value = false
    ElMessage.success(formData.id ? '评审记录已更新' : '评审记录新增成功')
    loadReview()
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => loadReview())
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; gap: 8px; }
.search-form { margin-bottom: 0; }
</style>

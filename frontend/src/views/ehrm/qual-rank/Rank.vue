<template>
  <div class="page-card">
    <div class="page-header">
      <h2>等级评定（申报-审核-评定-定级全流程）</h2>
      <div class="header-actions">
        <ImportExportBar
          :columns="ioColumns"
          :rows="reviews"
          file-name="等级评定申报"
          title="等级评定申报"
          import-api="/api/review"
          :field-map="ioFieldMap"
          @done="fetchReviews"
        />
        <el-button type="primary" @click="openApply">
          <el-icon><Plus /></el-icon>发起申报
        </el-button>
        <el-button @click="fetchReviews">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="任职资格等级评定（对标华为五级加权评审）"
      description="综合评审算法：基础条件20% + 知识技能25% + 行为履职25% + 绩效贡献20% + 综合素质10%；流程：个人申报 → 部门审核 → HR复审 → 专家评定 → 审批通过；L4/L5高阶等级受集团名额配额管控，资格有效期2年到期需复核。"
      style="margin-bottom: 16px"
    />

    <!-- 算法展示：申报等级分布（L1-L5） -->
    <el-card shadow="never" style="margin-bottom: 16px">
      <div class="level-dist">
        <div v-for="lv in levelDist" :key="lv.level" class="level-block" :style="{ borderTopColor: lv.color }">
          <div class="lv-name">{{ lv.level }}</div>
          <div class="lv-count">{{ lv.count }}</div>
          <div class="lv-text">{{ lv.text }}</div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" class="search-form">
        <el-form-item label="员工姓名">
          <el-input v-model="searchQuery" placeholder="搜索员工姓名或工号" style="width: 170px" clearable @input="fetchReviews" />
        </el-form-item>
        <el-form-item label="申报等级">
          <el-select v-model="levelFilter" placeholder="全部等级" style="width: 130px" clearable @change="fetchReviews">
            <el-option v-for="l in levelOptions" :key="l" :label="l" :value="l" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="statusFilter" placeholder="全部状态" style="width: 140px" clearable @change="fetchReviews">
            <el-option v-for="(v, k) in statusMap" :key="k" :label="v.text" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchReviews">搜索</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="reviews" border stripe size="small">
        <el-table-column prop="reviewNo" label="申报单号" width="130" />
        <el-table-column prop="employeeName" label="员工" width="100">
          <template #default="{ row }">
            <el-link type="primary" @click="openEdit(row)">{{ row.employeeName }}</el-link>
            <div style="font-size:12px;color:#909399">{{ row.employeeNo }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="qualificationName" label="资格标准" min-width="140" show-overflow-tooltip />
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="currentLevel" label="当前等级" width="85">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.currentLevel || '—' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetLevel" label="申报等级" width="85">
          <template #default="{ row }">
            <el-tag size="small" type="warning">{{ row.targetLevel }}</el-tag>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="workYears" label="司龄" width="70">
          <template #default="{ row }">{{ row.workYears ?? '—' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="perfResult" label="绩效结果" width="85">
          <template #default="{ row }">
            <el-tag size="small" :type="row.perfResult === 'A' || row.perfResult === 'S' ? 'success' : 'info'">{{ row.perfResult || '—' }}</el-tag>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展：自评/部门/HR/专家四方评分 -->
        <el-table-column label="自评" width="70">
          <template #default="{ row }">{{ row.selfScore ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="部门" width="70">
          <template #default="{ row }">{{ row.deptScore ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="HR" width="70">
          <template #default="{ row }">{{ row.hrScore ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="专家" width="70">
          <template #default="{ row }">{{ row.expertScore ?? '—' }}</template>
        </el-table-column>
        <el-table-column prop="totalScore" label="综合得分" width="130">
          <template #default="{ row }">
            <el-progress v-if="row.totalScore != null" :percentage="Math.min(100, Number(row.totalScore))" :stroke-width="14" :text-inside="true" :status="row.totalScore >= 60 ? 'success' : 'exception'" />
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="reviewStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusMap[row.reviewStatus]?.type || 'info'">{{ statusMap[row.reviewStatus]?.text || row.reviewStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="290" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.reviewStatus === 'IN_REVIEW'" link size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.reviewStatus === 'IN_REVIEW'" link size="small" type="warning" @click="changeStatus(row, 'PENDING_APPROVAL')">提交审批</el-button>
            <el-button v-if="row.reviewStatus === 'PENDING_APPROVAL' || row.reviewStatus === 'PENDING_CALIBRATION'" link size="small" type="success" @click="approveReview(row)">通过</el-button>
            <el-button v-if="row.reviewStatus === 'PENDING_APPROVAL' || row.reviewStatus === 'PENDING_CALIBRATION'" link size="small" type="danger" @click="rejectReview(row)">驳回</el-button>
            <el-button v-if="row.reviewStatus === 'IN_REVIEW'" link size="small" type="danger" @click="removeReview(row)">删除</el-button>
            <el-button link size="small" @click="viewProcess(row)">查看流程</el-button>
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
        @size-change="fetchReviews"
        @current-change="fetchReviews"
      />
    </el-card>

    <!-- 申报/编辑弹窗（规格书申报评审字段 + 五维加权算法） -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="860px">
      <el-form :model="formData" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="申报单号">
              <el-input v-model="formData.reviewNo" placeholder="自动生成" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="员工工号">
              <el-input v-model="formData.employeeNo" placeholder="请输入工号" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="员工姓名" required>
              <el-input v-model="formData.employeeName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资格标准" required>
              <el-input v-model="formData.qualificationName" placeholder="请输入资格标准名称" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="当前等级">
              <el-select v-model="formData.currentLevel" placeholder="选择" style="width:100%">
                <el-option v-for="l in levelOptions" :key="l" :label="l" :value="l" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="申报等级" required>
              <el-select v-model="formData.targetLevel" placeholder="选择" style="width:100%">
                <el-option v-for="l in levelOptions" :key="l" :label="l" :value="l" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <!-- 规格书字段，待后端扩展 -->
          <el-col :span="8">
            <el-form-item label="司龄(年)">
              <el-input-number v-model="formData.workYears" :min="0" :max="50" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="在岗年限">
              <el-input-number v-model="formData.postYears" :min="0" :max="50" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="绩效结果">
              <el-select v-model="formData.perfResult" placeholder="近1-2年绩效" style="width:100%">
                <el-option v-for="p in ['S','A','B','C','D']" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="举证材料">
          <el-input v-model="formData.evidence" placeholder="项目成果、绩效证明、证书资质、专利等举证材料清单" />
        </el-form-item>
        <el-form-item label="申请理由">
          <el-input type="textarea" v-model="formData.applyReason" :rows="2" placeholder="请输入申请理由" />
        </el-form-item>
        <!-- 规格书算法：五维加权评审（基础20%+知识技能25%+行为履职25%+绩效贡献20%+综合素质10%） -->
        <el-divider content-position="left">五维加权评审打分（权重固定 20/25/25/20/10）</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="基础条件">
              <el-input-number v-model="formData.baseScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="知识技能">
              <el-input-number v-model="formData.knowledgeScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="行为履职">
              <el-input-number v-model="formData.behaviorScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="绩效贡献">
              <el-input-number v-model="formData.perfScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="综合素质">
              <el-input-number v-model="formData.qualityScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="综合得分">
              <el-input :model-value="compositeScore" readonly />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展：四方评审打分 -->
        <el-divider content-position="left">评审打分（自评/部门/HR/专家）</el-divider>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="自评得分">
              <el-input-number v-model="formData.selfScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="部门评分">
              <el-input-number v-model="formData.deptScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="HR复审">
              <el-input-number v-model="formData.hrScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="专家评审">
              <el-input-number v-model="formData.expertScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="评审人">
              <el-input v-model="formData.reviewer" placeholder="请输入评审人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="formData.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveData">保存</el-button>
      </template>
    </el-dialog>

    <!-- 审批流程详情弹窗 -->
    <el-dialog v-model="showProcessModal" title="审批流程详情" width="640px">
      <div class="process-info">
        <p><strong>流程ID：</strong>{{ selectedReview?.processInstanceId || '无流程' }}</p>
        <p><strong>当前节点：</strong>{{ selectedReview?.currentNode || '—' }}</p>
        <p><strong>状态：</strong>
          <el-tag size="small" :type="statusMap[selectedReview?.reviewStatus]?.type || 'info'">{{ statusMap[selectedReview?.reviewStatus]?.text || selectedReview?.reviewStatus }}</el-tag>
        </p>
      </div>
      <div v-if="approvalHistory.length > 0" class="history-section">
        <h4>审批历史</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(node, index) in approvalHistory"
            :key="index"
            :timestamp="formatTime(node.operateTime)"
            :type="node.action === 'APPROVED' ? 'success' : node.action === 'REJECTED' ? 'danger' : 'primary'"
          >
            <strong>{{ node.nodeName }}</strong>
            <el-tag size="small" style="margin-left:8px">{{ getActionText(node.action) }}</el-tag>
            <div style="color:#606266; margin-top:4px">{{ node.opinion }}</div>
          </el-timeline-item>
        </el-timeline>
      </div>
      <el-empty v-else description="暂无审批历史" :image-size="60" />
      <template #footer>
        <el-button @click="showProcessModal = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import ImportExportBar from '@/components/ImportExportBar.vue'
import { startApproval, FLOW_DEF_CODES } from '@/api/flowApproval'

const ioColumns = [
  { key: 'reviewNo', title: '申报单号' },
  { key: 'employeeNo', title: '员工工号' },
  { key: 'employeeName', title: '员工姓名' },
  { key: 'qualificationName', title: '资格标准' },
  { key: 'currentLevel', title: '当前等级' },
  { key: 'targetLevel', title: '申报等级' },
  { key: 'workYears', title: '司龄' },
  { key: 'postYears', title: '在岗年限' },
  { key: 'perfResult', title: '绩效结果' },
  { key: 'selfScore', title: '自评得分' },
  { key: 'deptScore', title: '部门评分' },
  { key: 'hrScore', title: 'HR复审得分' },
  { key: 'expertScore', title: '专家评审得分' },
  { key: 'totalScore', title: '综合得分' },
  { key: 'reviewStatus', title: '申报状态' }
]
const ioFieldMap = {
  '申报单号': 'reviewNo',
  '员工工号': 'employeeNo',
  '员工姓名': 'employeeName',
  '资格标准': 'qualificationName',
  '当前等级': 'currentLevel',
  '申报等级': 'targetLevel',
  '司龄': 'workYears',
  '在岗年限': 'postYears',
  '绩效结果': 'perfResult',
  '自评得分': 'selfScore',
  '部门评分': 'deptScore',
  'HR复审得分': 'hrScore',
  '专家评审得分': 'expertScore',
  '综合得分': 'totalScore',
  '申报状态': 'reviewStatus',
  '申请理由': 'applyReason',
  '举证材料': 'evidence'
}

const searchQuery = ref('')
const statusFilter = ref('')
const levelFilter = ref('')
const reviews = ref([])
const showProcessModal = ref(false)
const selectedReview = ref(null)
const approvalHistory = ref([])

const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const dialogVisible = ref(false)
const dialogTitle = ref('发起申报')
const isEdit = ref(false)

const levelOptions = ['L1', 'L2', 'L3', 'L4', 'L5']

const statusMap = {
  IN_REVIEW: { text: '评审中', type: 'warning' },
  PENDING_CALIBRATION: { text: '待校准', type: 'info' },
  PENDING_APPROVAL: { text: '待审批', type: 'primary' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已拒绝', type: 'danger' }
}

const defaultForm = () => ({
  id: null,
  reviewNo: '',
  employeeId: null,
  employeeNo: '',
  employeeName: '',
  qualificationName: '',
  currentLevel: '',
  targetLevel: '',
  workYears: null,
  postYears: null,
  perfResult: '',
  evidence: '',
  applyReason: '',
  baseScore: null,
  knowledgeScore: null,
  behaviorScore: null,
  perfScore: null,
  qualityScore: null,
  selfScore: null,
  deptScore: null,
  hrScore: null,
  expertScore: null,
  totalScore: null,
  reviewer: '',
  remark: '',
  reviewStatus: 'IN_REVIEW'
})

const formData = reactive(defaultForm())

const num = (v) => (v === null || v === undefined || v === '' ? 0 : Number(v) || 0)

// 规格书算法：五维加权综合得分 = 基础20% + 知识技能25% + 行为履职25% + 绩效贡献20% + 综合素质10%
const compositeScore = computed(() => {
  const v = num(formData.baseScore) * 0.2 + num(formData.knowledgeScore) * 0.25 +
    num(formData.behaviorScore) * 0.25 + num(formData.perfScore) * 0.2 + num(formData.qualityScore) * 0.1
  return v ? Math.round(v * 10) / 10 : 0
})

// 算法展示：申报等级分布（L1-L5）
const levelDist = computed(() => {
  const texts = { L1: '入门级', L2: '基础级', L3: '专业级', L4: '资深级', L5: '专家/管理级' }
  const colors = { L1: '#909399', L2: '#409eff', L3: '#67c23a', L4: '#e6a23c', L5: '#f56c6c' }
  return levelOptions.map(l => ({
    level: l,
    text: texts[l],
    color: colors[l],
    count: reviews.value.filter(r => r.targetLevel === l).length
  }))
})

function extractList(res) {
  const data = res.data
  if (data && typeof data === 'object' && 'code' in data) {
    const payload = data.data
    if (Array.isArray(payload)) return { list: payload, total: payload.length }
    if (payload && Array.isArray(payload.records)) return { list: payload.records, total: payload.total || 0 }
    if (payload && Array.isArray(payload.list)) return { list: payload.list, total: payload.total || 0 }
    return { list: [], total: 0 }
  }
  if (Array.isArray(data)) return { list: data, total: data.length }
  if (data && Array.isArray(data.records)) return { list: data.records, total: data.total || 0 }
  if (data && Array.isArray(data.list)) return { list: data.list, total: data.total || 0 }
  return { list: [], total: 0 }
}

async function fetchReviews() {
  try {
    const params = { page: pagination.currentPage, size: pagination.pageSize }
    if (searchQuery.value) params.employeeName = searchQuery.value
    if (statusFilter.value) params.reviewStatus = statusFilter.value
    if (levelFilter.value) params.targetLevel = levelFilter.value
    const response = await axios.get('/api/review/list', { params })
    const { list, total } = extractList(response)
    reviews.value = list
    pagination.total = total
  } catch (error) {
    ElMessage.error('获取评审列表失败')
    console.error('获取评审列表失败:', error)
  }
}

function openApply() {
  isEdit.value = false
  dialogTitle.value = '发起申报'
  Object.assign(formData, defaultForm())
  formData.reviewNo = 'RV-' + new Date().getFullYear() + '-' + String(Date.now()).slice(-5)
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑申报'
  Object.assign(formData, defaultForm(), row)
  dialogVisible.value = true
}

async function saveData() {
  if (!formData.employeeName || !formData.targetLevel || !formData.qualificationName) {
    ElMessage.warning('员工姓名、资格标准、申报等级为必填项')
    return
  }
  formData.totalScore = compositeScore.value
  try {
    if (isEdit.value && formData.id) {
      await axios.put(`/api/review/${formData.id}`, formData)
      ElMessage.success('申报已更新')
    } else {
      await axios.post('/api/review', formData)
      ElMessage.success('申报已提交')
    }
    dialogVisible.value = false
    fetchReviews()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('保存申报失败:', error)
  }
}

async function changeStatus(row, target) {
  try {
    // 提交审批时优先走流程引擎
    if (target === 'PENDING_APPROVAL') {
      try {
        const instNo = await startApproval({
          defCode: FLOW_DEF_CODES.HRM_QUALIFICATION,
          bizCode: 'qualification',
          bizId: row.id,
          bizJson: row,
          title: `任职资格-${row.employeeName}-${row.targetLevel}`,
          initiator: localStorage.getItem('username') || 'dev-user'
        })
        await axios.put(`/api/review/${row.id}`, { ...row, reviewStatus: target })
        ElMessage.success('已发起流程审批，实例号：' + instNo)
        fetchReviews()
        return
      } catch (e) {
        ElMessage.warning('流程引擎不可用，已本地提交')
      }
    }
    await axios.put(`/api/review/${row.id}`, { ...row, reviewStatus: target })
    ElMessage.success('已提交审批')
    fetchReviews()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('状态流转失败:', error)
  }
}

async function approveReview(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入审批意见（通过）', '审批通过', {
      confirmButtonText: '确认通过',
      cancelButtonText: '取消',
      inputPlaceholder: '如：五维评审达标，同意定级'
    })
    // 后端端点无意见参数：先PUT留痕意见，再调审批端点
    await axios.put(`/api/review/${row.id}`, { ...row, remark: value })
    await axios.post(`/api/review/${row.id}/approve`)
    ElMessage.success('已通过')
    fetchReviews()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审批通过失败')
      console.error('审批通过失败:', error)
    }
  }
}

async function rejectReview(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回意见（必填）', '驳回申报', {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      inputValidator: (v) => (v && v.trim().length > 0) || '驳回意见不能为空'
    })
    await axios.put(`/api/review/${row.id}`, { ...row, remark: value })
    await axios.post(`/api/review/${row.id}/reject`)
    ElMessage.success('已驳回')
    fetchReviews()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('驳回失败')
      console.error('驳回失败:', error)
    }
  }
}

function removeReview(row) {
  ElMessageBox.confirm(`确定删除申报单"${row.reviewNo}"吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      try {
        await axios.delete(`/api/review/${row.id}`)
        ElMessage.success('删除成功')
        fetchReviews()
      } catch (error) {
        ElMessage.error('删除失败')
        console.error('删除失败:', error)
      }
    })
    .catch(() => {})
}

async function viewProcess(row) {
  selectedReview.value = row
  showProcessModal.value = true
  approvalHistory.value = []
  if (!row.id) return
  try {
    const historyRes = await axios.get(`/api/review/${row.id}/process/history`)
    const payload = historyRes.data
    approvalHistory.value = Array.isArray(payload?.data) ? payload.data : Array.isArray(payload) ? payload : []
  } catch (error) {
    console.error('获取流程历史失败:', error)
  }
}

function getActionText(action) {
  const map = { APPROVED: '通过', REJECTED: '拒绝', SUBMIT: '提交', PENDING: '待处理' }
  return map[action] || action
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchReviews()
})
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; align-items: center; gap: 8px; }
.search-form { margin-bottom: 0; }
.level-dist { display: flex; gap: 12px; }
.level-block {
  flex: 1; text-align: center; padding: 10px 0; border: 1px solid #ebeef5;
  border-top: 3px solid #409eff; border-radius: 4px; background: #fafafa;
}
.lv-name { font-weight: 700; font-size: 15px; }
.lv-count { font-size: 22px; font-weight: 700; color: #303133; margin: 4px 0; }
.lv-text { font-size: 12px; color: #909399; }
.process-info p { margin: 6px 0; }
.history-section h4 { margin: 12px 0 8px; }
</style>

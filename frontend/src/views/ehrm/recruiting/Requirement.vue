<template>
  <div class="page-card">
    <div class="page-header">
      <h2>招聘需求管控</h2>
      <div class="header-actions">
        <el-select v-model="hrmAuth.role" size="small" style="width:170px" @change="setHrmRole">
          <el-option v-for="r in HRM_ROLES" :key="r.value" :label="r.label" :value="r.value" />
        </el-select>
        <el-button v-if="can('HR_ADMIN','HRBP','DEPT_HEAD')" type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新增需求
        </el-button>
        <ImportExportBar
          v-if="can('HR_ADMIN','HRBP','HR_DIRECTOR')"
          :columns="exportCols" :rows="requirementList" file-name="招聘需求台账" title="招聘需求"
          :import-api="can('HR_ADMIN','HRBP') ? '/api/recruitment/requirement' : ''"
          :field-map="fieldMap" @done="loadData" />
      </div>
    </div>

    <div class="stat-cards">
      <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ requirementList.length }}</div><div class="stat-label">需求单数</div></el-card>
      <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ sumNum }}</div><div class="stat-label">需求人数合计</div></el-card>
      <el-card shadow="hover" class="stat-card"><div class="stat-value warn">{{ countByStatus('待审批') }}</div><div class="stat-label">待审批</div></el-card>
      <el-card shadow="hover" class="stat-card"><div class="stat-value primary">{{ countByStatus('招聘中') }}</div><div class="stat-label">招聘中</div></el-card>
      <el-card shadow="hover" class="stat-card"><div class="stat-value success">{{ countByStatus('已完成') }}</div><div class="stat-label">已完成</div></el-card>
    </div>

    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="岗位名称">
          <el-input v-model="searchForm.position" placeholder="请输入岗位名称" style="width:180px" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.department" placeholder="选择部门" style="width:150px" clearable>
            <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" style="width:130px" clearable>
            <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="requirementList" border stripe size="small" v-loading="loading">
        <el-table-column prop="requirementNo" label="需求编号" width="140" />
        <el-table-column prop="position" label="岗位名称" width="140" />
        <el-table-column prop="department" label="部门" width="110" />
        <!-- 规格书字段，待后端扩展：招聘类型(社招/校招/内推/补录)、现有编制、剩余编制、需求提出人 -->
        <el-table-column prop="requirementNum" label="需求人数" width="90" align="right" />
        <el-table-column prop="salaryRange" label="薪资范围" width="120" />
        <el-table-column prop="urgency" label="紧急程度" width="90" align="center">
          <template #default="{row}">
            <el-tag :type="urgencyType(row.urgency)" size="small">{{ row.urgency }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expectedDate" label="期望到岗" width="110" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{row}">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提出时间" width="160" />
        <el-table-column label="操作" width="330" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button v-if="can('HR_ADMIN','HRBP','DEPT_HEAD') && (row.status === '草稿' || row.status === '已驳回')" link size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button v-if="can('HR_ADMIN','HRBP','DEPT_HEAD') && (row.status === '草稿' || row.status === '已驳回')" link size="small" type="warning" @click="submitApproval(row)">提交审批</el-button>
            <el-button v-if="can('HR_ADMIN','HR_DIRECTOR','DEPT_HEAD') && row.status === '待审批'" link size="small" type="success" @click="flowStatus(row, '已通过')">审批通过</el-button>
            <el-button v-if="can('HR_ADMIN','HR_DIRECTOR') && row.status === '待审批'" link size="small" type="danger" @click="flowStatus(row, '已驳回')">驳回</el-button>
            <el-button v-if="can('HR_ADMIN','HRBP') && row.status === '已通过'" link size="small" type="success" @click="flowStatus(row, '招聘中')">发布岗位</el-button>
            <el-button v-if="can('HR_ADMIN','HRBP') && row.status === '招聘中'" link size="small" @click="flowStatus(row, '已完成')">完成</el-button>
            <el-button link size="small" v-if="row.status === '招聘中'" @click="viewCandidates(row)">候选人</el-button>
            <el-button v-if="can('HR_ADMIN','HR_DIRECTOR') && row.status !== '已取消'" link size="small" type="danger" @click="voidRequirement(row)">作废</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top:16px; text-align:right"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 新增/编辑需求弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <el-form :model="formData" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位名称" required>
              <el-input v-model="formData.position" placeholder="请输入岗位名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需求人数" required>
              <el-input-number v-model="formData.requirementNum" :min="1" :max="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属部门" required>
              <el-select v-model="formData.department" placeholder="选择部门">
                <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="招聘类型">
              <el-select v-model="formData.recruitType" placeholder="选择类型">
                <el-option label="社会招聘" value="社招" />
                <el-option label="校园招聘" value="校招" />
                <el-option label="内部推荐" value="内推" />
                <el-option label="补录" value="补录" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="现有编制">
              <el-input-number v-model="formData.existingHeadcount" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="剩余编制">
              <el-input-number v-model="formData.remainingHeadcount" :min="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="紧急程度">
              <el-radio-group v-model="formData.urgency">
                <el-radio label="常规" />
                <el-radio label="一般" />
                <el-radio label="紧急" />
                <el-radio label="特急" />
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="薪资范围">
              <el-input v-model="formData.salaryRange" placeholder="例如：15K-25K" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="期望到岗时间">
              <el-date-picker v-model="formData.expectedDate" type="date" placeholder="选择到岗时间" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="招聘截止时间">
              <el-date-picker v-model="formData.deadline" type="date" placeholder="选择截止时间" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="学历要求">
              <el-select v-model="formData.educationReq" placeholder="选择学历" clearable>
                <el-option label="大专" value="大专" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="工作年限">
              <el-input v-model="formData.workYears" placeholder="例如：3年以上" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="专业要求">
              <el-input v-model="formData.majorReq" placeholder="例如：计算机相关专业" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="技能标签">
              <el-input v-model="formData.skillTags" placeholder="逗号分隔，如 Java,微服务" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="工作地点">
              <el-input v-model="formData.workLocation" placeholder="请输入工作地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="试用期时长">
              <el-select v-model="formData.probationPeriod" placeholder="选择试用期" clearable>
                <el-option label="1个月" value="1个月" />
                <el-option label="3个月" value="3个月" />
                <el-option label="6个月" value="6个月" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="岗位职责">
          <el-input type="textarea" v-model="formData.responsibilities" :rows="3" placeholder="请输入岗位职责" />
        </el-form-item>
        <el-form-item label="任职要求">
          <el-input type="textarea" v-model="formData.requirements" :rows="3" placeholder="请输入任职要求" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="申请说明">
          <el-input type="textarea" v-model="formData.applyReason" :rows="2" placeholder="招聘原因（岗位空缺/业务扩张/替补等）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button @click="handleSubmit('草稿')">保存草稿</el-button>
        <el-button type="primary" @click="handleSubmit('待审批')">提交审批</el-button>
      </template>
    </el-dialog>

    <!-- 需求详情弹窗 -->
    <el-dialog v-model="detailVisible" title="需求详情" width="640px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="需求编号">{{ detailRow.requirementNo }}</el-descriptions-item>
        <el-descriptions-item label="岗位名称">{{ detailRow.position }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ detailRow.department }}</el-descriptions-item>
        <el-descriptions-item label="需求人数">{{ detailRow.requirementNum }}</el-descriptions-item>
        <el-descriptions-item label="薪资范围">{{ detailRow.salaryRange }}</el-descriptions-item>
        <el-descriptions-item label="紧急程度">{{ detailRow.urgency }}</el-descriptions-item>
        <el-descriptions-item label="期望到岗">{{ detailRow.expectedDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailRow.status }}</el-descriptions-item>
        <el-descriptions-item label="岗位职责" :span="2">{{ detailRow.responsibilities || '-' }}</el-descriptions-item>
        <el-descriptions-item label="任职要求" :span="2">{{ detailRow.requirements || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 候选人（按岗位过滤）弹窗 -->
    <el-dialog v-model="candDialogVisible" :title="`候选人 - ${candPosition}`" width="700px">
      <el-table :data="candList" border stripe size="small" v-loading="candLoading">
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="source" label="来源" width="100">
          <template #default="{row}">{{ sourceLabel(row.source) }}</template>
        </el-table-column>
        <el-table-column prop="education" label="学历" width="90" />
        <el-table-column prop="currentStage" label="当前阶段" width="110">
          <template #default="{row}">
            <el-tag size="small" :type="stageType(row.currentStage)">{{ row.currentStage }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="录入时间" width="160" />
      </el-table>
      <el-empty v-if="!candLoading && candList.length === 0" description="该岗位暂无候选人" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import ImportExportBar from '@/components/ImportExportBar.vue'
import { hrmAuth, setHrmRole, can, HRM_ROLES } from '../hrmAuth.js'
import { startApproval, FLOW_DEF_CODES } from '@/api/flowApproval'

const loading = ref(false)
const searchForm = reactive({ position: '', department: '', status: '' })

const departments = ['研发部', '生产部', '销售部', '财务部', '人力资源部', '行政部']
// 规格书状态机：草稿、审批中(待审批)、已通过、已驳回、已作废(已取消)、已发布(招聘中)、已完成
const statusOptions = ['草稿', '待审批', '已通过', '已驳回', '招聘中', '已完成', '已取消']

const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const dialogVisible = ref(false)
const dialogTitle = ref('新增招聘需求')
const isEdit = ref(false)
const detailVisible = ref(false)
const detailRow = ref({})
const candDialogVisible = ref(false)
const candPosition = ref('')
const candList = ref([])
const candLoading = ref(false)

const emptyForm = {
  id: null, requirementNo: '', position: '', department: '', requirementNum: 1,
  salaryRange: '', urgency: '一般', expectedDate: '', responsibilities: '', requirements: '', status: '草稿',
  // 规格书字段，待后端扩展（提交时一并带上，后端多字段不报错）
  recruitType: '社招', existingHeadcount: 0, remainingHeadcount: 0, deadline: '',
  educationReq: '', workYears: '', majorReq: '', skillTags: '', workLocation: '', probationPeriod: '', applyReason: ''
}
const formData = reactive({ ...emptyForm })

const requirementList = ref([])

const sumNum = computed(() => requirementList.value.reduce((a, r) => a + (Number(r.requirementNum) || 0), 0))
const countByStatus = (s) => requirementList.value.filter(r => r.status === s).length

const exportCols = [
  { key: 'requirementNo', title: '需求编号' }, { key: 'position', title: '岗位名称' }, { key: 'department', title: '部门' },
  { key: 'requirementNum', title: '需求人数' }, { key: 'salaryRange', title: '薪资范围' }, { key: 'urgency', title: '紧急程度' },
  { key: 'expectedDate', title: '期望到岗' }, { key: 'status', title: '状态' }, { key: 'createTime', title: '提出时间' }
]
const fieldMap = { '需求编号': 'requirementNo', '岗位名称': 'position', '部门': 'department', '需求人数': 'requirementNum', '薪资范围': 'salaryRange', '紧急程度': 'urgency', '期望到岗': 'expectedDate', '状态': 'status' }

function getStatusType(status) {
  const types = { '草稿': 'info', '待审批': 'warning', '已通过': 'success', '已驳回': 'danger', '招聘中': 'primary', '已完成': 'success', '已取消': 'info' }
  return types[status] || 'info'
}
function urgencyType(u) {
  return { '特急': 'danger', '紧急': 'danger', '一般': 'warning', '常规': 'info' }[u] || 'info'
}
function sourceLabel(s) {
  return { zhilian: '智联', '51job': '前程无忧', boss: 'BOSS直聘', internal: '内推', headhunter: '猎头' }[s] || s
}
function stageType(stage) {
  return { '简历筛选': 'info', '初试': 'primary', '复试': 'warning', '待录用': 'success', '已入职': 'success', '已淘汰': 'danger' }[stage] || 'info'
}

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

async function loadData() {
  loading.value = true
  try {
    const res = await axios.get('/api/recruitment/requirement/list', {
      params: {
        page: pagination.currentPage,
        size: pagination.pageSize,
        position: searchForm.position || undefined,
        department: searchForm.department || undefined,
        status: searchForm.status || undefined
      }
    })
    const { list, total } = extractList(res)
    requirementList.value = list
    pagination.total = total
  } catch (e) {
    ElMessage.error('加载招聘需求列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() { pagination.currentPage = 1; loadData() }
function resetSearch() { searchForm.position = ''; searchForm.department = ''; searchForm.status = ''; pagination.currentPage = 1; loadData() }
function handleSizeChange(size) { pagination.pageSize = size; loadData() }
function handleCurrentChange(page) { pagination.currentPage = page; loadData() }

function openAddDialog() {
  isEdit.value = false
  dialogTitle.value = '新增招聘需求'
  Object.assign(formData, emptyForm)
  formData.requirementNo = 'ZR-' + new Date().getFullYear() + '-' + String(Date.now()).slice(-3)
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  dialogTitle.value = '编辑招聘需求'
  Object.assign(formData, emptyForm, row)
  dialogVisible.value = true
}

function viewDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

async function viewCandidates(row) {
  candPosition.value = row.position
  candDialogVisible.value = true
  candLoading.value = true
  try {
    const res = await axios.get('/api/recruitment/rec02/list', { params: { position: row.position } })
    candList.value = extractList(res).list
  } catch (e) {
    ElMessage.error('加载候选人失败')
  } finally {
    candLoading.value = false
  }
}

// 状态流转（基于后端通用更新端点）
async function flowStatus(row, status) {
  try {
    await axios.put(`/api/recruitment/requirement/${row.id}`, { ...row, status })
    ElMessage.success(`已流转为「${status}」`)
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 提交审批：优先走流程引擎，失败兜底本地提交
async function submitApproval(row) {
  try {
    const instNo = await startApproval({
      defCode: FLOW_DEF_CODES.HRM_RECRUITMENT_REQ,
      bizCode: 'recruitment',
      bizId: row.id,
      bizJson: row,
      title: `招聘需求-${row.position || row.requirementNo}`,
      initiator: localStorage.getItem('username') || 'dev-user'
    })
    await axios.put(`/api/recruitment/requirement/${row.id}`, { ...row, status: '待审批' })
    ElMessage.success('已发起流程审批，实例号：' + instNo)
    loadData()
  } catch (e) {
    ElMessage.warning('流程引擎不可用，已本地提交')
    flowStatus(row, '待审批')
  }
}

function voidRequirement(row) {
  ElMessageBox.confirm(`确定要作废招聘需求"${row.position}"吗？`, '作废确认', { type: 'warning' })
    .then(() => flowStatus(row, '已取消'))
    .catch(() => {})
}

async function handleSubmit(targetStatus) {
  // 编制刚性校验：需求人数超剩余编制时拦截，仅战略特批可继续
  const remaining = Number(formData.remainingHeadcount)
  if (!isEdit.value && remaining >= 0 && formData.requirementNum > remaining && remaining > 0) {
    try {
      await ElMessageBox.confirm(
        `需求人数 ${formData.requirementNum} 超过剩余编制 ${remaining}，超编岗位仅战略特批需求可提交，是否继续？`,
        '编制校验拦截', { type: 'error', confirmButtonText: '战略特批提交', cancelButtonText: '返回调整' }
      )
    } catch (e) { return }
  }
  try {
    const payload = { ...formData, status: targetStatus || formData.status || '草稿' }
    let saved = payload
    if (isEdit.value) {
      await axios.put(`/api/recruitment/requirement/${formData.id}`, payload)
      ElMessage.success('招聘需求已更新')
    } else {
      const res = await axios.post('/api/recruitment/requirement', payload)
      saved = (res.data && res.data.data) || payload
      ElMessage.success(targetStatus === '待审批' ? '招聘需求已提交审批' : '草稿已保存')
    }
    // 提交审批时尝试走流程引擎
    if (targetStatus === '待审批') {
      try {
        const instNo = await startApproval({
          defCode: FLOW_DEF_CODES.HRM_RECRUITMENT_REQ,
          bizCode: 'recruitment',
          bizId: saved.id || formData.id,
          bizJson: saved,
          title: `招聘需求-${saved.position || saved.requirementNo}`,
          initiator: localStorage.getItem('username') || 'dev-user'
        })
        ElMessage.success('已发起流程审批，实例号：' + instNo)
      } catch (e) {
        ElMessage.warning('流程引擎不可用，已本地提交')
      }
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; gap: 8px; align-items: center; }
.search-form { margin-bottom: 0; }
.stat-cards { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.stat-card { flex: 1; min-width: 130px; text-align: center; }
.stat-value { font-size: 26px; font-weight: bold; color: #303133; }
.stat-value.warn { color: #e6a23c; }
.stat-value.primary { color: #409eff; }
.stat-value.success { color: #67c23a; }
.stat-label { color: #909399; font-size: 13px; margin-top: 4px; }
</style>

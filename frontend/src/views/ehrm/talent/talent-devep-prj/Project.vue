<template>
  <div class="page-card">
    <div class="page-header">
      <h2>人才培养工程立项管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>项目立项
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
      title="鹰系人才培养工程立项"
      description="对标TCL鹰系四级梯队（雏鹰/飞鹰/精鹰/雄鹰），按721培养法则（70%岗位实践+20%导师帮带+10%课程学习）立项，状态机：草稿立项→待审批→培养实施中→阶段复盘→待结项→已结项。"
      style="margin-bottom: 16px"
    />

    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="项目名称">
          <el-input v-model="searchForm.projectName" placeholder="请输入项目名称" style="width: 200px" clearable />
        </el-form-item>
        <el-form-item label="鹰系梯队">
          <el-select v-model="searchForm.projectType" placeholder="选择梯队" style="width: 150px" clearable>
            <el-option v-for="t in eagleTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 150px" clearable>
            <el-option v-for="(v, k) in statusMap" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="pagedList" border stripe size="small" v-loading="loading">
        <el-table-column prop="id" label="项目编码" width="100" />
        <el-table-column prop="projectName" label="项目名称" min-width="190">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.projectName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="projectType" label="鹰系梯队" width="100">
          <template #default="{ row }">
            <el-tag :type="getEagleTypeColor(row.projectType)" size="small">{{ row.projectType || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="year" label="立项年度" width="90" align="center" />
        <el-table-column prop="duration" label="培养周期" width="190" />
        <!-- 规格书字段，待后端扩展：计划人数 -->
        <el-table-column prop="planCount" label="计划人数" width="90" align="center">
          <template #default="{ row }">{{ row.planCount || '-' }}</template>
        </el-table-column>
        <el-table-column prop="participantCount" label="实际纳入" width="90" align="center" />
        <!-- 规格书字段，待后端扩展：项目负责人/导师组长 -->
        <el-table-column prop="manager" label="项目负责人" width="100">
          <template #default="{ row }">{{ row.manager || '-' }}</template>
        </el-table-column>
        <el-table-column prop="mentorLead" label="导师组长" width="100">
          <template #default="{ row }">{{ row.mentorLead || '-' }}</template>
        </el-table-column>
        <el-table-column prop="budget" label="预算(元)" width="110" align="right">
          <template #default="{ row }">{{ row.budget != null ? Number(row.budget).toLocaleString() : '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="项目状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ statusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" @click="editItem(row)">编辑</el-button>
            <el-button v-if="row.status === 'DRAFT'" link size="small" type="warning" @click="changeStatus(row, 'PENDING_APPROVAL')">提交审批</el-button>
            <el-button v-if="row.status === 'PENDING_APPROVAL'" link size="small" type="success" @click="changeStatus(row, 'IN_PROGRESS')">审批通过</el-button>
            <el-button v-if="row.status === 'IN_PROGRESS'" link size="small" type="primary" @click="changeStatus(row, 'REVIEW')">阶段复盘</el-button>
            <el-button v-if="row.status === 'REVIEW'" link size="small" type="warning" @click="changeStatus(row, 'PENDING_CLOSE')">申请结项</el-button>
            <el-button v-if="row.status === 'PENDING_CLOSE'" link size="small" type="success" @click="changeStatus(row, 'CLOSED')">结项</el-button>
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
      <el-form :model="formData" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="项目名称" required>
              <el-input v-model="formData.projectName" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="鹰系梯队" required>
              <el-select v-model="formData.projectType" placeholder="选择梯队类型" style="width: 100%">
                <el-option v-for="t in eagleTypes" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="培养开始日期" required>
              <el-date-picker v-model="formData.startDate" type="date" placeholder="选择开始日期" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="培养结束日期" required>
              <el-date-picker v-model="formData.endDate" type="date" placeholder="选择结束日期" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <!-- 规格书字段，待后端扩展：计划人数/项目负责人/导师组长 -->
          <el-col :span="8">
            <el-form-item label="计划人数">
              <el-input-number v-model="formData.planCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="实际纳入人数">
              <el-input-number v-model="formData.participantCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="预算(元)">
              <el-input-number v-model="formData.budget" :min="0" :step="10000" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="项目负责人">
              <el-input v-model="formData.manager" placeholder="请输入项目负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="导师组长">
              <el-input v-model="formData.mentorLead" placeholder="请输入导师组长" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展：721培养权重（合计必须100%） -->
        <el-divider content-position="left">721培养权重（%）</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="理论学习(10%)">
              <el-input-number v-model="formData.theoryWeight" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="岗位实践(70%)">
              <el-input-number v-model="formData.practiceWeight" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="导师帮带(20%)">
              <el-input-number v-model="formData.mentorWeight" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="权重合计">
          <el-tag :type="weightSum721 === 100 ? 'success' : 'danger'" size="large">{{ weightSum721 }}%</el-tag>
          <span style="margin-left: 8px; color: #909399; font-size: 12px">规格书硬性规则：三项权重合计不等于100%时拦截保存</span>
        </el-form-item>
        <!-- 规格书字段，待后端扩展：EL-P01立项方案 -->
        <el-form-item label="战略培养目标">
          <el-input type="textarea" v-model="formData.goal" :rows="2" placeholder="战略培养目标、人才补给方向、岗位储备用途" />
        </el-form-item>
        <el-form-item label="项目说明">
          <el-input type="textarea" v-model="formData.description" :rows="3" placeholder="请输入项目说明" />
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
import { startApproval, FLOW_DEF_CODES } from '@/api/flowApproval'

const eagleTypes = ['雏鹰工程', '飞鹰工程', '精鹰工程', '雄鹰工程']
// 培养状态机（规格书）：草稿立项/待审批/培养实施中/阶段复盘/待结项/已结项
const statusMap = {
  DRAFT: '草稿立项', PENDING_APPROVAL: '待审批', IN_PROGRESS: '培养实施中',
  REVIEW: '阶段复盘', PENDING_CLOSE: '待结项', CLOSED: '已结项'
}

const searchForm = reactive({ projectName: '', projectType: '', status: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('项目立项')
const loading = ref(false)

const defaultForm = {
  id: '', projectName: '', projectType: '', startDate: '', endDate: '',
  participantCount: 0, budget: 0, description: '', status: 'DRAFT',
  planCount: 0, manager: '', mentorLead: '',
  theoryWeight: 10, practiceWeight: 70, mentorWeight: 20, goal: ''
}
const formData = reactive({ ...defaultForm })
const projectList = ref([])

// 721权重合计校验（规格书核心固定算法）
const weightSum721 = computed(() =>
  Number(formData.theoryWeight || 0) + Number(formData.practiceWeight || 0) + Number(formData.mentorWeight || 0))

const filteredList = computed(() => {
  let list = [...projectList.value]
  if (searchForm.projectName) list = list.filter(p => p.projectName?.includes(searchForm.projectName))
  if (searchForm.projectType) list = list.filter(p => p.projectType === searchForm.projectType)
  if (searchForm.status) list = list.filter(p => p.status === searchForm.status)
  return list
})
const pagedList = computed(() => {
  pagination.total = filteredList.value.length
  const start = (pagination.currentPage - 1) * pagination.pageSize
  return filteredList.value.slice(start, start + pagination.pageSize)
})

function getEagleTypeColor(t) {
  const types = { '雏鹰工程': 'success', '飞鹰工程': 'primary', '精鹰工程': 'warning', '雄鹰工程': 'danger' }
  return types[t] || 'info'
}
function getStatusType(s) {
  const types = { DRAFT: 'info', PENDING_APPROVAL: 'warning', IN_PROGRESS: 'primary', REVIEW: 'warning', PENDING_CLOSE: 'danger', CLOSED: 'success' }
  return types[s] || 'info'
}

async function loadProjects() {
  loading.value = true
  try {
    const { data } = await request.get('/api/talent/dev-project/list')
    const list = data?.data || data || []
    projectList.value = list.map(r => ({
      id: r.id,
      projectName: r.projectName || '',
      projectType: r.projectType || '',
      year: r.startDate ? String(r.startDate).slice(0, 4) : '',
      startDate: r.startDate ? String(r.startDate).slice(0, 10) : '',
      endDate: r.endDate ? String(r.endDate).slice(0, 10) : '',
      duration: r.startDate && r.endDate ? `${String(r.startDate).slice(0, 7)} ~ ${String(r.endDate).slice(0, 7)}` : '',
      participantCount: r.participantCount || 0,
      budget: r.budget,
      description: r.description || '',
      status: r.status || 'DRAFT',
      planCount: r.planCount, manager: r.manager, mentorLead: r.mentorLead,
      theoryWeight: r.theoryWeight ?? 10, practiceWeight: r.practiceWeight ?? 70, mentorWeight: r.mentorWeight ?? 20,
      goal: r.goal || ''
    }))
  } catch (e) {
    ElMessage.error('项目列表加载失败：' + (e.message || ''))
  } finally {
    loading.value = false
  }
}

function doSearch() { pagination.currentPage = 1 }
function resetSearch() { searchForm.projectName = ''; searchForm.projectType = ''; searchForm.status = ''; pagination.currentPage = 1 }
function refreshData() { loadProjects(); ElMessage.success('数据已刷新') }

function openCreate() {
  dialogTitle.value = '项目立项'
  Object.assign(formData, { ...defaultForm })
  dialogVisible.value = true
}

function viewDetail(row) { ElMessage.info(`查看项目详情：${row.projectName}`) }

function editItem(row) {
  dialogTitle.value = '编辑项目'
  Object.assign(formData, { ...defaultForm, ...row })
  dialogVisible.value = true
}

function buildPayload(src, statusOverride) {
  return {
    projectName: src.projectName,
    projectType: src.projectType,
    startDate: src.startDate,
    endDate: src.endDate,
    participantCount: src.participantCount,
    budget: src.budget,
    description: src.description,
    status: statusOverride || src.status || 'DRAFT',
    // 规格书字段，待后端扩展（随提交携带）
    planCount: src.planCount, manager: src.manager, mentorLead: src.mentorLead,
    theoryWeight: src.theoryWeight, practiceWeight: src.practiceWeight, mentorWeight: src.mentorWeight,
    goal: src.goal
  }
}

// 状态机流转：提交审批/审批通过/阶段复盘/申请结项/结项
async function changeStatus(row, target) {
  const text = statusMap[target]
  if (target === 'CLOSED') {
    // 规格书：结项二次确认弹窗
    try {
      await ElMessageBox.confirm(`结项后项目「${row.projectName}」将进入归档流程，确认结项吗？`, '结项二次确认', { type: 'error', confirmButtonText: '确认结项', cancelButtonText: '取消' })
    } catch { return }
  } else {
    try { await ElMessageBox.confirm(`确定将项目「${row.projectName}」流转为「${text}」吗？`, '状态流转确认', { type: 'warning' }) } catch { return }
  }
  try {
    // 提交审批时优先走流程引擎
    if (target === 'PENDING_APPROVAL') {
      try {
        const instNo = await startApproval({
          defCode: FLOW_DEF_CODES.HRM_TALENT_DEV,
          bizCode: 'talent-dev',
          bizId: row.id,
          bizJson: row,
          title: `人才培养项目-${row.projectName}`,
          initiator: localStorage.getItem('username') || 'dev-user'
        })
        await request.put(`/api/talent/dev-project/${row.id}`, buildPayload(row, target))
        ElMessage.success('已发起流程审批，实例号：' + instNo)
        loadProjects()
        return
      } catch (e) {
        ElMessage.warning('流程引擎不可用，已本地提交')
      }
    }
    await request.put(`/api/talent/dev-project/${row.id}`, buildPayload(row, target))
    ElMessage.success(`已流转为「${text}」`)
    loadProjects()
  } catch (e) {
    ElMessage.error('流转失败：' + (e.response?.data?.message || e.message || ''))
  }
}

async function saveData() {
  if (!formData.projectName || !formData.projectType || !formData.startDate || !formData.endDate) {
    ElMessage.warning('请填写项目名称、鹰系梯队、培养周期'); return
  }
  // 规格书硬性拦截：721权重合计必须等于100%
  if (weightSum721.value !== 100) {
    ElMessage.error(`721培养权重合计必须等于100%（当前${weightSum721.value}%），禁止保存`); return
  }
  try {
    if (formData.id) {
      await request.put(`/api/talent/dev-project/${formData.id}`, buildPayload(formData))
      ElMessage.success('项目已更新')
    } else {
      await request.post('/api/talent/dev-project', buildPayload(formData, 'DRAFT'))
      ElMessage.success('项目立项成功')
    }
    dialogVisible.value = false
    loadProjects()
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => loadProjects())
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; gap: 8px; }
.search-form { margin-bottom: 0; }
</style>

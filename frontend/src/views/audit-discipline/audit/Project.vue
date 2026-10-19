<template>
  <div class="page-container">
    <div class="page-header">
      <h2>审计项目</h2>
      <el-button type="primary" @click="handleAdd">新增项目</el-button>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="项目名称">
        <el-input v-model="searchForm.name" placeholder="请输入项目名称" style="width:180px" />
      </el-form-item>
      <el-form-item label="关联计划">
        <el-select v-model="searchForm.planId" placeholder="选择计划" style="width:180px">
          <el-option v-for="plan in plans" :key="plan.id" :label="plan.name" :value="plan.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width:150px">
          <el-option label="准备中" value="preparing" />
          <el-option label="现场审计" value="field" />
          <el-option label="报告阶段" value="reporting" />
          <el-option label="已完成" value="completed" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="projectList" border stripe size="small" v-loading="loading">
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="projectNo" label="项目编号" width="140" />
      <el-table-column prop="name" label="项目名称" min-width="200">
        <template #default="{row}">
          <el-link type="primary" @click="viewDetail(row)">{{ row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="planName" label="关联计划" width="150" />
      <el-table-column prop="auditScope" label="审计范围" width="150" />
      <el-table-column prop="startDate" label="开始日期" width="120" />
      <el-table-column prop="endDate" label="结束日期" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="leadAuditor" label="主审" width="120" />
      <el-table-column prop="teamMembers" label="团队成员" width="150">
        <template #default="{row}">
          <el-tag v-for="member in row.teamMembers" :key="member" size="small" style="margin:2px">{{ member }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300">
        <template #default="{row}">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 'preparing'" size="small" type="warning" @click="handleStage(row, 'start-field', '进场实施')">进场</el-button>
          <el-button v-if="row.status === 'preparing'" size="small" type="info" @click="handleSubmitApproval(row)">提交审批</el-button>
          <el-button v-if="row.status === 'field'" size="small" type="warning" @click="handleStage(row, 'start-reporting', '转入报告')">报告阶段</el-button>
          <el-button v-if="row.status === 'reporting'" size="small" type="success" @click="handleStage(row, 'complete', '项目结项')">结项</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="pagination.currentPage"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pagination.pageSize"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top: 20px; text-align: right;"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-form-item label="项目编号" required>
          <el-input v-model="formData.projectNo" placeholder="请输入项目编号" />
        </el-form-item>
        <el-form-item label="项目名称" required>
          <el-input v-model="formData.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="关联计划">
          <el-select v-model="formData.planId" placeholder="选择关联计划">
            <el-option v-for="plan in plans" :key="plan.id" :label="plan.name" :value="plan.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="审计范围">
          <el-input v-model="formData.auditScope" placeholder="请输入审计范围" />
        </el-form-item>
        <el-form-item label="审计目标">
          <el-input v-model="formData.objective" type="textarea" placeholder="请输入审计目标" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="formData.startDate" type="date" placeholder="选择开始日期" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="formData.endDate" type="date" placeholder="选择结束日期" />
        </el-form-item>
        <el-form-item label="主审">
          <el-input v-model="formData.leadAuditor" placeholder="请输入主审姓名" />
        </el-form-item>
        <el-form-item label="团队成员">
          <el-input v-model="formData.teamMembersInput" placeholder="多个成员用逗号分隔" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status">
            <el-option label="准备中" value="preparing" />
            <el-option label="现场审计" value="field" />
            <el-option label="报告阶段" value="reporting" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="审计项目详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="项目编号">{{ detailData.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="项目名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="关联计划">{{ detailData.planName }}</el-descriptions-item>
        <el-descriptions-item label="审计范围">{{ detailData.auditScope }}</el-descriptions-item>
        <el-descriptions-item label="审计目标">{{ detailData.objective }}</el-descriptions-item>
        <el-descriptions-item label="主审">{{ detailData.leadAuditor }}</el-descriptions-item>
        <el-descriptions-item label="团队成员">
          <el-tag v-for="member in detailData.teamMembers" :key="member" size="small" style="margin:2px">{{ member }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ detailData.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ detailData.endDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)" size="small">{{ getStatusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const searchForm = reactive({
  name: '',
  planId: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增审计项目')
const formRef = ref(null)

const plans = ref([])

async function loadPlans() {
  try {
    const res = await axios.get('/api/audit/plan/list', { params: { pageNum: 1, pageSize: 200 } })
    const page = res.data && res.data.data ? res.data.data : {}
    plans.value = (page.records || []).map(p => ({ id: p.id, name: p.name }))
  } catch (e) {
    console.error('加载审计计划列表失败', e)
  }
}

const formData = reactive({
  id: null,
  projectNo: '',
  name: '',
  planId: null,
  planName: '',
  auditScope: '',
  objective: '',
  startDate: '',
  endDate: '',
  leadAuditor: '',
  teamMembers: [],
  teamMembersInput: '',
  status: 'preparing'
})

const detailData = reactive({})

const projectList = ref([])

function getStatusLabel(status) {
  const map = { preparing: '准备中', field: '现场审计', reporting: '报告阶段', completed: '已完成' }
  return map[status] || status
}

function getStatusType(status) {
  const map = { preparing: 'info', field: 'warning', reporting: 'primary', completed: 'success' }
  return map[status] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await axios.get('/api/audit/project/list', {
      params: {
        name: searchForm.name || undefined,
        planId: searchForm.planId || undefined,
        status: searchForm.status || undefined,
        pageNum: pagination.currentPage,
        pageSize: pagination.pageSize
      }
    })
    if (res.data.code === 200) {
      const page = res.data.data
      projectList.value = page.records || []
      pagination.total = page.total || 0
    } else {
      ElMessage.error(res.data.message || '加载审计项目列表失败')
    }
  } catch (e) {
    ElMessage.error('加载审计项目列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.currentPage = 1
  loadData()
}

function resetSearch() {
  searchForm.name = ''
  searchForm.planId = ''
  searchForm.status = ''
  pagination.currentPage = 1
  loadData()
}

function handleSizeChange(val) {
  pagination.pageSize = val
  loadData()
}

function handleCurrentChange(val) {
  pagination.currentPage = val
  loadData()
}

function handleAdd() {
  dialogTitle.value = '新增审计项目'
  Object.assign(formData, {
    id: null,
    projectNo: '',
    name: '',
    planId: null,
    planName: '',
    auditScope: '',
    objective: '',
    startDate: '',
    endDate: '',
    leadAuditor: '',
    teamMembers: [],
    teamMembersInput: '',
    status: 'preparing'
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑审计项目'
  Object.assign(formData, {
    ...row,
    teamMembersInput: Array.isArray(row.teamMembers) ? row.teamMembers.join(',') : ''
  })
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/audit/project/${row.id}`)
    if (res.data.code === 200) {
      Object.assign(detailData, res.data.data)
      detailVisible.value = true
    } else {
      ElMessage.error(res.data.message || '获取审计项目详情失败')
    }
  } catch (e) {
    ElMessage.error('获取审计项目详情失败')
    console.error(e)
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该审计项目吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/audit/project/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadData()
      } else {
        ElMessage.error(res.data.message || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败')
      console.error(e)
    }
  }).catch(() => {})
}

async function handleStage(row, action, label) {
  try {
    const res = await axios.put(`/api/audit/project/${row.id}/${action}`)
    if (res.data.code === 200) {
      ElMessage.success(label + '成功')
      loadData()
    } else {
      ElMessage.error(res.data.message || (label + '失败'))
    }
  } catch (e) {
    ElMessage.error(label + '失败')
    console.error(e)
  }
}

// 立项审批对接统一流程引擎 FlowEngineMgt(8206)，流程定义 AUDIT_PROJECT
async function handleSubmitApproval(row) {
  try {
    const res = await axios.post('/api/flow/instance/start', {
      defCode: 'AUDIT_PROJECT',
      bizCode: 'audit_project',
      bizId: row.projectNo || String(row.id),
      bizJson: JSON.stringify({ projectId: row.id, name: row.name }),
      title: '审计项目立项审批-' + row.name,
      initiator: localStorage.getItem('username') || 'audit-user'
    })
    if (res.data.code === 200) {
      ElMessage.success('已提交立项审批，流程实例#' + (res.data.data && res.data.data.id))
    } else {
      ElMessage.error(res.data.message || '提交审批失败')
    }
  } catch (e) {
    ElMessage.error('提交审批失败（流程引擎不可用？）')
    console.error(e)
  }
}

async function handleSubmit() {
  if (formData.teamMembersInput) {
    formData.teamMembers = formData.teamMembersInput.split(',').map(s => s.trim()).filter(s => s)
  } else {
    formData.teamMembers = []
  }
  try {
    let res
    if (formData.id) {
      res = await axios.put(`/api/audit/project/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/audit/project', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success(dialogTitle.value === '新增审计项目' ? '新增成功' : '修改成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

onMounted(() => {
  loadPlans()
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>
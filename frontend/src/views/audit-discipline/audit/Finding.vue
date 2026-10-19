<template>
  <div class="page-container">
    <div class="page-header">
      <h2>审计发现</h2>
      <el-button type="primary" @click="handleAdd">新增发现</el-button>
    </div>

    <el-row :gutter="12" style="margin-bottom: 20px;">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon red"><el-icon><Warning /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.highCount }}</div>
            <div class="stat-label">高风险</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon orange"><el-icon><InfoFilled /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.mediumCount }}</div>
            <div class="stat-label">中风险</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon blue"><el-icon><Info /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.lowCount }}</div>
            <div class="stat-label">低风险</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon orange"><el-icon><Clock /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.openCount }}</div>
            <div class="stat-label">未整改</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon red"><el-icon><AlarmClock /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.overdueCount }}</div>
            <div class="stat-label">超期未整改</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon green"><el-icon><CheckCircle /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.closedCount }}</div>
            <div class="stat-label">已闭环</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="问题标题">
        <el-input v-model="searchForm.title" placeholder="请输入问题标题" style="width:180px" />
      </el-form-item>
      <el-form-item label="严重程度">
        <el-select v-model="searchForm.level" placeholder="选择级别" style="width:120px">
          <el-option label="高" value="high" />
          <el-option label="中" value="medium" />
          <el-option label="低" value="low" />
        </el-select>
      </el-form-item>
      <el-form-item label="整改状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width:150px">
          <el-option label="待整改" value="pending" />
          <el-option label="整改中" value="correcting" />
          <el-option label="待复核" value="reviewing" />
          <el-option label="已闭环" value="closed" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="findingList" border stripe size="small" v-loading="loading">
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="projectName" label="关联项目" width="150" />
      <el-table-column prop="title" label="问题标题" min-width="200">
        <template #default="{row}">
          <el-link type="primary" @click="viewDetail(row)">{{ row.title }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="level" label="严重程度" width="100">
        <template #default="{row}">
          <el-tag :type="getLevelType(row.level)" size="small">{{ getLevelLabel(row.level) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="问题描述" min-width="250" />
      <el-table-column prop="recommendation" label="整改建议" min-width="200" />
      <el-table-column prop="deadline" label="整改期限" width="120" />
      <el-table-column prop="status" label="整改状态" width="100">
        <template #default="{row}">
          <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="responsible" label="责任人" width="100" />
      <el-table-column label="操作" width="320">
        <template #default="{row}">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 'pending'" size="small" type="warning" @click="handleDistribute(row)">下发整改</el-button>
          <el-button v-if="row.status === 'correcting'" size="small" type="warning" @click="handleSubmitRectify(row)">提交整改</el-button>
          <el-button v-if="row.status === 'reviewing'" size="small" type="success" @click="handleReview(row, true)">复核通过</el-button>
          <el-button v-if="row.status === 'reviewing'" size="small" type="warning" @click="handleReview(row, false)">退回</el-button>
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
        <el-form-item label="关联项目">
          <el-select v-model="formData.projectId" placeholder="选择关联项目">
            <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题标题" required>
          <el-input v-model="formData.title" placeholder="请输入问题标题" />
        </el-form-item>
        <el-form-item label="严重程度" required>
          <el-select v-model="formData.level">
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入问题描述" />
        </el-form-item>
        <el-form-item label="整改建议">
          <el-input v-model="formData.recommendation" type="textarea" :rows="3" placeholder="请输入整改建议" />
        </el-form-item>
        <el-form-item label="整改期限">
          <el-date-picker v-model="formData.deadline" type="date" placeholder="选择整改期限" />
        </el-form-item>
        <el-form-item label="责任人">
          <el-input v-model="formData.responsible" placeholder="请输入责任人" />
        </el-form-item>
        <el-form-item label="整改措施">
          <el-input v-model="formData.rectificationMeasure" type="textarea" :rows="2" placeholder="整改落实情况/措施（提交整改时填写）" />
        </el-form-item>
        <el-form-item label="整改状态">
          <el-select v-model="formData.status">
            <el-option label="待整改" value="pending" />
            <el-option label="整改中" value="correcting" />
            <el-option label="待复核" value="reviewing" />
            <el-option label="已闭环" value="closed" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="审计发现详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="关联项目">{{ detailData.projectName }}</el-descriptions-item>
        <el-descriptions-item label="问题标题">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="严重程度">
          <el-tag :type="getLevelType(detailData.level)" size="small">{{ getLevelLabel(detailData.level) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="整改状态">
          <el-tag :type="getStatusType(detailData.status)" size="small">{{ getStatusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">{{ detailData.description }}</el-descriptions-item>
        <el-descriptions-item label="整改建议" :span="2">{{ detailData.recommendation }}</el-descriptions-item>
        <el-descriptions-item label="整改期限">{{ detailData.deadline }}</el-descriptions-item>
        <el-descriptions-item label="责任人">{{ detailData.responsible }}</el-descriptions-item>
        <el-descriptions-item label="整改措施" :span="2">{{ detailData.rectificationMeasure || '-' }}</el-descriptions-item>
        <el-descriptions-item label="复核人">{{ detailData.reviewer || '-' }}</el-descriptions-item>
        <el-descriptions-item label="复核意见">{{ detailData.reviewComment || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发现时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="整改提交时间">{{ detailData.rectifyTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="关闭时间">{{ detailData.closeTime || '-' }}</el-descriptions-item>
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
  title: '',
  level: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增审计发现')
const formRef = ref(null)

const projects = ref([])

async function loadProjects() {
  try {
    const res = await axios.get('/api/audit/project/list', { params: { pageNum: 1, pageSize: 200 } })
    const page = res.data && res.data.data ? res.data.data : {}
    projects.value = (page.records || []).map(p => ({ id: p.id, name: p.name }))
  } catch (e) {
    console.error('加载审计项目列表失败', e)
  }
}

const formData = reactive({
  id: null,
  projectId: null,
  projectName: '',
  title: '',
  level: 'medium',
  description: '',
  recommendation: '',
  rectificationMeasure: '',
  deadline: '',
  responsible: '',
  status: 'pending'
})

const detailData = reactive({})

const findingList = ref([])

const stats = reactive({
  highCount: 0, mediumCount: 0, lowCount: 0,
  pendingCount: 0, correctingCount: 0, reviewingCount: 0,
  closedCount: 0, openCount: 0, overdueCount: 0
})

async function loadStatistics() {
  try {
    const res = await axios.get('/api/audit/finding/statistics')
    if (res.data.code === 200 && res.data.data) {
      Object.assign(stats, res.data.data)
    }
  } catch (e) { /* 统计非阻断 */ }
}

function getLevelLabel(level) {
  const map = { high: '高', medium: '中', low: '低' }
  return map[level] || level
}

function getLevelType(level) {
  const map = { high: 'danger', medium: 'warning', low: 'info' }
  return map[level] || 'info'
}

function getStatusLabel(status) {
  const map = { pending: '待整改', correcting: '整改中', reviewing: '待复核', closed: '已闭环', resolved: '已闭环' }
  return map[status] || status
}

function getStatusType(status) {
  const map = { pending: 'danger', correcting: 'warning', reviewing: 'primary', closed: 'success', resolved: 'success' }
  return map[status] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await axios.get('/api/audit/finding/list', {
      params: {
        title: searchForm.title || undefined,
        level: searchForm.level || undefined,
        status: searchForm.status || undefined,
        projectId: searchForm.projectId || undefined,
        pageNum: pagination.currentPage,
        pageSize: pagination.pageSize
      }
    })
    if (res.data.code === 200) {
      const page = res.data.data
      findingList.value = page.records || []
      pagination.total = page.total || 0
      loadStatistics()
    } else {
      ElMessage.error(res.data.message || '加载审计发现列表失败')
    }
  } catch (e) {
    ElMessage.error('加载审计发现列表失败')
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
  searchForm.title = ''
  searchForm.level = ''
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
  dialogTitle.value = '新增审计发现'
  Object.assign(formData, {
    id: null,
    projectId: null,
    projectName: '',
    title: '',
    level: 'medium',
    description: '',
    recommendation: '',
    rectificationMeasure: '',
    deadline: '',
    responsible: '',
    status: 'pending'
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑审计发现'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/audit/finding/${row.id}`)
    if (res.data.code === 200) {
      Object.assign(detailData, res.data.data)
      detailVisible.value = true
    } else {
      ElMessage.error(res.data.message || '获取审计发现详情失败')
    }
  } catch (e) {
    ElMessage.error('获取审计发现详情失败')
    console.error(e)
  }
}

async function doPut(name, url, body, okMsg) {
  try {
    const res = await axios.put(url, body || {})
    if (res.data.code === 200) {
      ElMessage.success(okMsg)
      loadData()
    } else {
      ElMessage.error(res.data.message || (name + '失败'))
    }
  } catch (e) {
    ElMessage.error(name + '失败')
    console.error(e)
  }
}

function handleDistribute(row) {
  ElMessageBox.prompt('请确认整改责任人（可留空，整改期限可在编辑中维护）', '下发整改', {
    confirmButtonText: '下发', cancelButtonText: '取消', inputValue: row.responsible || ''
  }).then(({ value }) => {
    doPut('下发整改', `/api/audit/finding/${row.id}/distribute`, { responsible: value }, '已下发整改')
  }).catch(() => {})
}

function handleSubmitRectify(row) {
  ElMessageBox.prompt('请填写整改措施/落实情况', '提交整改', {
    confirmButtonText: '提交', cancelButtonText: '取消',
    inputType: 'textarea', inputValue: row.rectificationMeasure || '',
    inputValidator: (v) => (v && v.trim() ? true : '整改措施不能为空')
  }).then(({ value }) => {
    doPut('提交整改', `/api/audit/finding/${row.id}/submit-rectification`,
      { rectificationMeasure: value }, '整改已提交，等待复核')
  }).catch(() => {})
}

function handleReview(row, passed) {
  if (passed) {
    ElMessageBox.prompt('请填写复核意见', '复核验证（通过）', {
      confirmButtonText: '通过并关闭', cancelButtonText: '取消', inputValue: '同意销号'
    }).then(({ value }) => {
      doPut('复核', `/api/audit/finding/${row.id}/review`,
        { passed: true, reviewer: localStorage.getItem('username') || '审计复核', reviewComment: value }, '复核通过，问题闭环')
    }).catch(() => {})
  } else {
    ElMessageBox.prompt('请填写退回原因', '复核退回', {
      confirmButtonText: '退回整改', cancelButtonText: '取消',
      inputValidator: (v) => (v && v.trim() ? true : '退回原因不能为空')
    }).then(({ value }) => {
      doPut('复核退回', `/api/audit/finding/${row.id}/review`,
        { passed: false, reviewer: localStorage.getItem('username') || '审计复核', reviewComment: value }, '已退回整改')
    }).catch(() => {})
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该审计发现吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/audit/finding/${row.id}`)
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

async function handleSubmit() {
  try {
    let res
    if (formData.id) {
      res = await axios.put(`/api/audit/finding/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/audit/finding', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success(dialogTitle.value === '新增审计发现' ? '新增成功' : '修改成功')
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
  loadProjects()
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

.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}

.stat-icon.red { background-color: #fff1f0; color: #f5222d; }
.stat-icon.orange { background-color: #fff7e6; color: #fa8c16; }
.stat-icon.blue { background-color: #e6f7ff; color: #1890ff; }
.stat-icon.green { background-color: #f6ffed; color: #52c41a; }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.search-form {
  margin-bottom: 20px;
}
</style>
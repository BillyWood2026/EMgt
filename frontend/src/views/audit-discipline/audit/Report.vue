<template>
  <div class="page-container">
    <div class="page-header">
      <h2>审计报告</h2>
      <el-button type="primary" @click="handleAdd">生成报告</el-button>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="报告名称">
        <el-input v-model="searchForm.name" placeholder="请输入报告名称" style="width:180px" />
      </el-form-item>
      <el-form-item label="关联项目">
        <el-select v-model="searchForm.projectId" placeholder="选择项目" style="width:180px">
          <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width:150px">
          <el-option label="草稿" value="draft" />
          <el-option label="待审核" value="reviewing" />
          <el-option label="已发布" value="published" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="reportList" border stripe size="small" v-loading="loading">
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="reportNo" label="报告编号" width="140" />
      <el-table-column prop="name" label="报告名称" min-width="200">
        <template #default="{row}">
          <el-link type="primary" @click="viewDetail(row)">{{ row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="projectName" label="关联项目" width="150" />
      <el-table-column prop="findingsCount" label="问题数量" width="100">
        <template #default="{row}">
          <el-tag :type="row.findingsCount > 5 ? 'danger' : row.findingsCount > 0 ? 'warning' : 'success'" size="small">{{ row.findingsCount }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="executiveSummary" label="摘要" min-width="250" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="author" label="编制人" width="100" />
      <el-table-column prop="createTime" label="生成时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 'draft'" size="small" type="warning" @click="handleSubmitReview(row)">提交审核</el-button>
          <el-button v-if="row.status === 'reviewing'" size="small" type="success" @click="handlePublish(row)">发布</el-button>
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
        <el-form-item label="报告编号" required>
          <el-input v-model="formData.reportNo" placeholder="请输入报告编号" />
        </el-form-item>
        <el-form-item label="报告名称" required>
          <el-input v-model="formData.name" placeholder="请输入报告名称" />
        </el-form-item>
        <el-form-item label="关联项目">
          <el-select v-model="formData.projectId" placeholder="选择关联项目">
            <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="审计摘要">
          <el-input v-model="formData.executiveSummary" type="textarea" :rows="3" placeholder="请输入审计摘要" />
        </el-form-item>
        <el-form-item label="审计结论">
          <el-input v-model="formData.conclusion" type="textarea" :rows="4" placeholder="请输入审计结论" />
        </el-form-item>
        <el-form-item label="编制人">
          <el-input v-model="formData.author" placeholder="请输入编制人" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status">
            <el-option label="草稿" value="draft" />
            <el-option label="待审核" value="reviewing" />
            <el-option label="已发布" value="published" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="审计报告详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="报告编号">{{ detailData.reportNo }}</el-descriptions-item>
        <el-descriptions-item label="报告名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="关联项目">{{ detailData.projectName }}</el-descriptions-item>
        <el-descriptions-item label="问题数量">
          <el-tag :type="detailData.findingsCount > 5 ? 'danger' : detailData.findingsCount > 0 ? 'warning' : 'success'" size="small">{{ detailData.findingsCount }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审计摘要" :span="2">{{ detailData.executiveSummary }}</el-descriptions-item>
        <el-descriptions-item label="审计结论" :span="2">{{ detailData.conclusion }}</el-descriptions-item>
        <el-descriptions-item label="编制人">{{ detailData.author }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)" size="small">{{ getStatusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="生成时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ detailData.reviewTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div style="margin-top: 20px;">
        <h4>发现问题列表</h4>
        <el-table :data="detailData.findings" border size="small" v-if="detailData.findings && detailData.findings.length">
          <el-table-column prop="id" label="编号" width="60" />
          <el-table-column prop="title" label="问题标题" width="200" />
          <el-table-column prop="level" label="严重程度" width="100">
            <template #default="{row}">
              <el-tag :type="row.level === 'high' ? 'danger' : row.level === 'medium' ? 'warning' : 'info'" size="small">
                {{ row.level === 'high' ? '高' : row.level === 'medium' ? '中' : '低' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="问题描述" min-width="300" />
        </el-table>
        <p v-else class="text-gray">暂无发现问题</p>
      </div>
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
  projectId: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('生成审计报告')
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
  reportNo: '',
  name: '',
  projectId: null,
  projectName: '',
  executiveSummary: '',
  conclusion: '',
  findingsCount: 0,
  author: '',
  status: 'draft'
})

const detailData = reactive({
  findings: []
})

const reportList = ref([])

function getStatusLabel(status) {
  const map = { draft: '草稿', reviewing: '待审核', published: '已发布' }
  return map[status] || status
}

function getStatusType(status) {
  const map = { draft: 'info', reviewing: 'warning', published: 'success' }
  return map[status] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await axios.get('/api/audit/report/list', {
      params: {
        name: searchForm.name || undefined,
        projectId: searchForm.projectId || undefined,
        status: searchForm.status || undefined,
        pageNum: pagination.currentPage,
        pageSize: pagination.pageSize
      }
    })
    if (res.data.code === 200) {
      const page = res.data.data
      reportList.value = page.records || []
      pagination.total = page.total || 0
    } else {
      ElMessage.error(res.data.message || '加载审计报告列表失败')
    }
  } catch (e) {
    ElMessage.error('加载审计报告列表失败')
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
  searchForm.projectId = ''
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
  dialogTitle.value = '生成审计报告'
  Object.assign(formData, {
    id: null,
    reportNo: '',
    name: '',
    projectId: null,
    projectName: '',
    executiveSummary: '',
    conclusion: '',
    findingsCount: 0,
    author: '',
    status: 'draft'
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑审计报告'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/audit/report/${row.id}`)
    if (res.data.code === 200) {
      Object.assign(detailData, res.data.data)
      detailVisible.value = true
    } else {
      ElMessage.error(res.data.message || '获取审计报告详情失败')
    }
  } catch (e) {
    ElMessage.error('获取审计报告详情失败')
    console.error(e)
  }
}

async function handleSubmitReview(row) {
  try {
    const res = await axios.put(`/api/audit/report/${row.id}/submit-review`)
    if (res.data.code === 200) {
      ElMessage.success('已提交审核')
      loadData()
    } else {
      ElMessage.error(res.data.message || '提交审核失败')
    }
  } catch (e) {
    ElMessage.error('提交审核失败')
    console.error(e)
  }
}

async function handlePublish(row) {
  try {
    const res = await axios.put(`/api/audit/report/${row.id}/publish`)
    if (res.data.code === 200) {
      ElMessage.success('报告已发布')
      loadData()
    } else {
      ElMessage.error(res.data.message || '发布失败')
    }
  } catch (e) {
    ElMessage.error('发布失败')
    console.error(e)
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该审计报告吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/audit/report/${row.id}`)
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
      res = await axios.put(`/api/audit/report/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/audit/report', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success(dialogTitle.value === '生成审计报告' ? '生成成功' : '修改成功')
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

.search-form {
  margin-bottom: 20px;
}

.text-gray {
  color: #999;
  text-align: center;
  padding: 20px;
}
</style>
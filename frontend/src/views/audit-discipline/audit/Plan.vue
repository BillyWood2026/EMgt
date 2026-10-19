<template>
  <div class="page-container">
    <div class="page-header">
      <h2>审计计划</h2>
      <el-button type="primary" @click="handleAdd">新增计划</el-button>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="计划名称">
        <el-input v-model="searchForm.name" placeholder="请输入计划名称" style="width:180px" />
      </el-form-item>
      <el-form-item label="审计类型">
        <el-select v-model="searchForm.type" placeholder="选择类型" style="width:150px">
          <el-option label="内部审计" value="internal" />
          <el-option label="外部审计" value="external" />
          <el-option label="专项审计" value="special" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width:150px">
          <el-option label="计划中" value="planning" />
          <el-option label="执行中" value="executing" />
          <el-option label="已完成" value="completed" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="planList" border stripe size="small" v-loading="loading">
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="planNo" label="计划编号" width="140" />
      <el-table-column prop="name" label="计划名称" min-width="200">
        <template #default="{row}">
          <el-link type="primary" @click="viewDetail(row)">{{ row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="审计类型" width="120">
        <template #default="{row}">
          <el-tag size="small">{{ getTypeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="scope" label="审计范围" width="150" />
      <el-table-column prop="startDate" label="开始日期" width="120" />
      <el-table-column prop="endDate" label="结束日期" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditor" label="审计负责人" width="120" />
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 'planning'" size="small" type="warning" @click="handleExecute(row)">执行</el-button>
          <el-button v-if="row.status === 'executing'" size="small" type="success" @click="handleComplete(row)">完成</el-button>
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
        <el-form-item label="计划编号" required>
          <el-input v-model="formData.planNo" placeholder="请输入计划编号" />
        </el-form-item>
        <el-form-item label="计划名称" required>
          <el-input v-model="formData.name" placeholder="请输入计划名称" />
        </el-form-item>
        <el-form-item label="审计类型" required>
          <el-select v-model="formData.type" placeholder="选择审计类型">
            <el-option label="内部审计" value="internal" />
            <el-option label="外部审计" value="external" />
            <el-option label="专项审计" value="special" />
          </el-select>
        </el-form-item>
        <el-form-item label="审计范围">
          <el-input v-model="formData.scope" placeholder="请输入审计范围" />
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
        <el-form-item label="审计负责人">
          <el-input v-model="formData.auditor" placeholder="请输入审计负责人" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status">
            <el-option label="计划中" value="planning" />
            <el-option label="执行中" value="executing" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="审计计划详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="计划编号">{{ detailData.planNo }}</el-descriptions-item>
        <el-descriptions-item label="计划名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="审计类型">{{ getTypeLabel(detailData.type) }}</el-descriptions-item>
        <el-descriptions-item label="审计范围">{{ detailData.scope }}</el-descriptions-item>
        <el-descriptions-item label="审计目标">{{ detailData.objective }}</el-descriptions-item>
        <el-descriptions-item label="审计负责人">{{ detailData.auditor }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ detailData.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ detailData.endDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)" size="small">{{ getStatusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
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
  type: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增审计计划')
const formRef = ref(null)

const formData = reactive({
  id: null,
  planNo: '',
  name: '',
  type: 'internal',
  scope: '',
  objective: '',
  startDate: '',
  endDate: '',
  auditor: '',
  status: 'planning'
})

const detailData = reactive({})

const planList = ref([])

function getTypeLabel(type) {
  const map = { internal: '内部审计', external: '外部审计', special: '专项审计' }
  return map[type] || type
}

function getStatusLabel(status) {
  const map = { planning: '计划中', executing: '执行中', completed: '已完成' }
  return map[status] || status
}

function getStatusType(status) {
  const map = { planning: 'info', executing: 'warning', completed: 'success' }
  return map[status] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await axios.get('/api/audit/plan/list', {
      params: {
        name: searchForm.name || undefined,
        type: searchForm.type || undefined,
        status: searchForm.status || undefined,
        pageNum: pagination.currentPage,
        pageSize: pagination.pageSize
      }
    })
    if (res.data.code === 200) {
      const page = res.data.data
      planList.value = page.records || []
      pagination.total = page.total || 0
    } else {
      ElMessage.error(res.data.message || '加载审计计划列表失败')
    }
  } catch (e) {
    ElMessage.error('加载审计计划列表失败')
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
  searchForm.type = ''
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
  dialogTitle.value = '新增审计计划'
  Object.assign(formData, {
    id: null,
    planNo: '',
    name: '',
    type: 'internal',
    scope: '',
    objective: '',
    startDate: '',
    endDate: '',
    auditor: '',
    status: 'planning'
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑审计计划'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/audit/plan/${row.id}`)
    if (res.data.code === 200) {
      Object.assign(detailData, res.data.data)
      detailVisible.value = true
    } else {
      ElMessage.error(res.data.message || '获取审计计划详情失败')
    }
  } catch (e) {
    ElMessage.error('获取审计计划详情失败')
    console.error(e)
  }
}

async function handleExecute(row) {
  try {
    const res = await axios.put(`/api/audit/plan/${row.id}/execute`)
    if (res.data.code === 200) {
      ElMessage.success('审计计划已开始执行')
      loadData()
    } else {
      ElMessage.error(res.data.message || '执行失败')
    }
  } catch (e) {
    ElMessage.error('执行失败')
    console.error(e)
  }
}

async function handleComplete(row) {
  try {
    const res = await axios.put(`/api/audit/plan/${row.id}/complete`)
    if (res.data.code === 200) {
      ElMessage.success('审计计划已完结')
      loadData()
    } else {
      ElMessage.error(res.data.message || '完结失败')
    }
  } catch (e) {
    ElMessage.error('完结失败')
    console.error(e)
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该审计计划吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/audit/plan/${row.id}`)
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
      res = await axios.put(`/api/audit/plan/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/audit/plan', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success(dialogTitle.value === '新增审计计划' ? '新增成功' : '修改成功')
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
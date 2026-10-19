<template>
  <div class="page-container">
    <div class="page-header">
      <h2>日志管理</h2>
      <div>
        <el-button type="danger" @click="handleClean">清空日志</el-button>
      </div>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="系统模块">
        <el-input v-model="searchForm.title" placeholder="请输入系统模块" style="width:180px" />
      </el-form-item>
      <el-form-item label="操作人">
        <el-input v-model="searchForm.operName" placeholder="请输入操作人" style="width:150px" />
      </el-form-item>
      <el-form-item label="操作类型">
        <el-select v-model="searchForm.businessType" placeholder="选择操作类型" style="width:150px">
          <el-option label="其它" value="OTHER" />
          <el-option label="新增" value="INSERT" />
          <el-option label="修改" value="UPDATE" />
          <el-option label="删除" value="DELETE" />
          <el-option label="授权" value="GRANT" />
          <el-option label="导出" value="EXPORT" />
          <el-option label="导入" value="IMPORT" />
          <el-option label="强退" value="FORCE" />
          <el-option label="生成代码" value="GENCODE" />
          <el-option label="清空数据" value="CLEAN" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width:120px">
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAIL" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间">
        <el-date-picker
          v-model="searchForm.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width:300px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="logList" border stripe size="small" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="系统模块" width="150" />
      <el-table-column prop="businessType" label="操作类型" width="100">
        <template #default="{row}">
          <el-tag :type="getBusinessTypeTag(row.businessType)" size="small">{{ getBusinessTypeLabel(row.businessType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operName" label="操作人" width="120" />
      <el-table-column prop="method" label="操作方法" min-width="200" />
      <el-table-column prop="requestMethod" label="请求方式" width="100" />
      <el-table-column prop="operIp" label="操作IP" width="150" />
      <el-table-column prop="operLocation" label="操作地点" width="150" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
            {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="costTime" label="耗时(ms)" width="100" />
      <el-table-column prop="operTime" label="操作时间" width="180" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{row}">
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
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

    <el-dialog v-model="detailVisible" title="日志详情" width="800px">
      <el-form :model="detailData" label-width="120px" :disabled="true">
        <el-row>
          <el-col :span="12">
            <el-form-item label="系统模块">
              <el-input v-model="detailData.title" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作类型">
              <el-tag :type="getBusinessTypeTag(detailData.businessType)" size="small">{{ getBusinessTypeLabel(detailData.businessType) }}</el-tag>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="操作人">
              <el-input v-model="detailData.operName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作IP">
              <el-input v-model="detailData.operIp" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="操作地点">
              <el-input v-model="detailData.operLocation" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="请求方式">
              <el-input v-model="detailData.requestMethod" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="操作方法">
          <el-input v-model="detailData.method" disabled />
        </el-form-item>
        <el-form-item label="请求URL">
          <el-input v-model="detailData.operUrl" disabled />
        </el-form-item>
        <el-form-item label="请求参数">
          <el-input v-model="detailData.operParam" type="textarea" :rows="4" disabled />
        </el-form-item>
        <el-form-item label="返回结果">
          <el-input v-model="detailData.jsonResult" type="textarea" :rows="4" disabled />
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="detailData.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
            {{ detailData.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </el-form-item>
        <el-form-item v-if="detailData.status === 'FAIL'" label="错误信息">
          <el-input v-model="detailData.errorMsg" type="textarea" :rows="3" disabled />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="操作时间">
              <el-input v-model="detailData.operTime" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="耗时">
              <el-input :value="detailData.costTime + ' ms'" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
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
  operName: '',
  businessType: '',
  status: '',
  dateRange: []
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const detailVisible = ref(false)
const detailData = reactive({})
const logList = ref([])

const businessTypeMap = {
  OTHER: { label: '其它', type: 'info' },
  INSERT: { label: '新增', type: 'primary' },
  UPDATE: { label: '修改', type: 'warning' },
  DELETE: { label: '删除', type: 'danger' },
  GRANT: { label: '授权', type: 'success' },
  EXPORT: { label: '导出', type: 'success' },
  IMPORT: { label: '导入', type: 'success' },
  FORCE: { label: '强退', type: 'danger' },
  GENCODE: { label: '生成代码', type: 'info' },
  CLEAN: { label: '清空数据', type: 'danger' }
}

function getBusinessTypeLabel(type) {
  return businessTypeMap[type]?.label || type
}

function getBusinessTypeTag(type) {
  return businessTypeMap[type]?.type || 'info'
}

async function loadLogList() {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      title: searchForm.title || undefined,
      operName: searchForm.operName || undefined,
      businessType: searchForm.businessType || undefined,
      status: searchForm.status || undefined
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
    }
    const res = await axios.get('/api/sys/log/list', { params })
    if (res.data.code === 200) {
      logList.value = res.data.data.list
      pagination.total = res.data.data.total
    }
  } catch (e) {
    ElMessage.error('加载日志列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.currentPage = 1
  loadLogList()
}

function resetSearch() {
  searchForm.title = ''
  searchForm.operName = ''
  searchForm.businessType = ''
  searchForm.status = ''
  searchForm.dateRange = []
  pagination.currentPage = 1
  loadLogList()
}

function handleSizeChange(val) {
  pagination.pageSize = val
  loadLogList()
}

function handleCurrentChange(val) {
  pagination.currentPage = val
  loadLogList()
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/sys/log/${row.id}`)
    if (res.data.code === 200) {
      Object.assign(detailData, res.data.data)
      detailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取日志详情失败')
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该日志吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/sys/log/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadLogList()
      } else {
        ElMessage.error(res.data.message || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

function handleClean() {
  ElMessageBox.confirm('确定要清空所有日志吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete('/api/sys/log/clean')
      if (res.data.code === 200) {
        ElMessage.success('清空成功')
        loadLogList()
      } else {
        ElMessage.error(res.data.message || '清空失败')
      }
    } catch (e) {
      ElMessage.error('清空失败')
    }
  })
}

onMounted(() => {
  loadLogList()
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

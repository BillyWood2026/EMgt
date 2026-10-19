<template>
  <div class="page-container">
    <div class="page-header">
      <h2>日常监督记录</h2>
      <el-button type="primary" @click="handleAdd">新增记录</el-button>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="记录编号">
        <el-input v-model="searchForm.recordNo" placeholder="请输入记录编号" style="width:180px" />
      </el-form-item>
      <el-form-item label="监督类型">
        <el-select v-model="searchForm.supervisionType" placeholder="选择类型" style="width:150px" clearable>
          <el-option label="日常检查" value="daily" />
          <el-option label="专项督查" value="special" />
          <el-option label="巡察" value="inspection" />
        </el-select>
      </el-form-item>
      <el-form-item label="风险等级">
        <el-select v-model="searchForm.riskLevel" placeholder="选择等级" style="width:130px" clearable>
          <el-option label="低" value="low" />
          <el-option label="中" value="medium" />
          <el-option label="高" value="high" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width:130px" clearable>
          <el-option label="待处置" value="pending" />
          <el-option label="处置中" value="rectifying" />
          <el-option label="已完成" value="done" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" border stripe size="small" v-loading="loading">
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="recordNo" label="记录编号" width="150" />
      <el-table-column prop="supervisionType" label="监督类型" width="110">
        <template #default="{row}">{{ getTypeLabel(row.supervisionType) }}</template>
      </el-table-column>
      <el-table-column prop="riskLevel" label="风险等级" width="100">
        <template #default="{row}"><el-tag :type="getLevelType(row.riskLevel)" size="small">{{ getLevelLabel(row.riskLevel) }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}"><el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="responsiblePerson" label="责任人" width="120" />
      <el-table-column prop="occurTime" label="发生时间" width="160" />
      <el-table-column prop="handleResult" label="处置结果" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="250">
        <template #default="{row}">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 'pending'" size="small" type="warning" @click="handleAdvance(row, 'handle')">启动处置</el-button>
          <el-button v-if="row.status === 'rectifying'" size="small" type="success" @click="handleAdvance(row, 'complete')">办结</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
      :current-page="pagination.currentPage" :page-sizes="[10,20,50]" :page-size="pagination.pageSize"
      :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" style="margin-top:20px;text-align:right" />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="记录编号"><el-input v-model="formData.recordNo" /></el-form-item>
        <el-form-item label="监督类型">
          <el-select v-model="formData.supervisionType">
            <el-option label="日常检查" value="daily" /><el-option label="专项督查" value="special" /><el-option label="巡察" value="inspection" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="formData.riskLevel">
            <el-option label="低" value="low" /><el-option label="中" value="medium" /><el-option label="高" value="high" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status">
            <el-option label="待处置" value="pending" />
            <el-option label="处置中" value="rectifying" />
            <el-option label="已完成" value="done" />
          </el-select>
        </el-form-item>
        <el-form-item label="责任人"><el-input v-model="formData.responsiblePerson" /></el-form-item>
        <el-form-item label="发生时间"><el-date-picker v-model="formData.occurTime" type="datetime" placeholder="选择发生时间" style="width:100%" /></el-form-item>
        <el-form-item label="处置结果"><el-input v-model="formData.handleResult" type="textarea" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="formData.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="监督记录详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="记录编号">{{ detailData.recordNo }}</el-descriptions-item>
        <el-descriptions-item label="监督类型">{{ getTypeLabel(detailData.supervisionType) }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">{{ getLevelLabel(detailData.riskLevel) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusLabel(detailData.status) }}</el-descriptions-item>
        <el-descriptions-item label="责任人">{{ detailData.responsiblePerson }}</el-descriptions-item>
        <el-descriptions-item label="发生时间">{{ detailData.occurTime }}</el-descriptions-item>
        <el-descriptions-item label="处置结果" :span="2">{{ detailData.handleResult }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detailData.description }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'

const API = '/api/discipline/supervision'
const loading = ref(false)
const searchForm = reactive({ recordNo: '', supervisionType: '', riskLevel: '', status: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增监督记录')
const list = ref([])
const formData = reactive({ id: null, recordNo: '', supervisionType: 'daily', riskLevel: 'low', status: 'pending', responsiblePerson: '', occurTime: '', handleResult: '', description: '' })
const detailData = reactive({})

const typeMap = { daily: '日常检查', special: '专项督查', inspection: '巡察' }
const levelMap = { low: '低', medium: '中', high: '高' }
const levelTypeMap = { low: 'success', medium: 'warning', high: 'danger' }
const statusMap = { pending: '待处置', rectifying: '处置中', done: '已完成' }
const statusTypeMap = { pending: 'info', rectifying: 'warning', done: 'success' }
const getTypeLabel = v => typeMap[v] || v
const getLevelLabel = v => levelMap[v] || v
const getLevelType = v => levelTypeMap[v] || 'info'
const getStatusLabel = v => statusMap[v] || v
const getStatusType = v => statusTypeMap[v] || 'info'

function handleAdvance(row, act) {
  const call = (body) => axios.put(API + '/' + row.id + '/' + act, body || {}).then(res => {
    if (res.data.code === 200) { ElMessage.success(act === 'complete' ? '处置已办结' : '已启动处置'); loadData() }
    else ElMessage.error(res.data.message || '操作失败')
  }).catch(() => ElMessage.error('操作失败'))
  if (act === 'complete') {
    ElMessageBox.prompt('请填写处置结果', '处置办结', {
      confirmButtonText: '办结', cancelButtonText: '取消', inputType: 'textarea',
      inputValue: row.handleResult || '',
      inputValidator: (v) => (v && v.trim() ? true : '处置结果不能为空')
    }).then(({ value }) => call({ handleResult: value })).catch(() => {})
  } else {
    call({})
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await axios.get(API + '/list', { params: { ...searchForm, pageNum: pagination.currentPage, pageSize: pagination.pageSize } })
    if (res.data.code === 200) { list.value = res.data.data.records || []; pagination.total = res.data.data.total || 0 }
    else ElMessage.error(res.data.message || '加载失败')
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}
const handleSearch = () => { pagination.currentPage = 1; loadData() }
const resetSearch = () => { Object.assign(searchForm, { recordNo: '', supervisionType: '', riskLevel: '', status: '' }); pagination.currentPage = 1; loadData() }
const handleSizeChange = v => { pagination.pageSize = v; loadData() }
const handleCurrentChange = v => { pagination.currentPage = v; loadData() }

function handleAdd() {
  dialogTitle.value = '新增监督记录'
  Object.assign(formData, { id: null, recordNo: '', supervisionType: 'daily', riskLevel: 'low', status: 'pending', responsiblePerson: '', occurTime: '', handleResult: '', description: '' })
  dialogVisible.value = true
}
function handleEdit(row) { dialogTitle.value = '编辑监督记录'; Object.assign(formData, row); dialogVisible.value = true }
async function viewDetail(row) {
  const res = await axios.get(API + '/' + row.id)
  if (res.data.code === 200) { Object.assign(detailData, res.data.data); detailVisible.value = true }
  else ElMessage.error('获取详情失败')
}
function handleDelete(row) {
  ElMessageBox.confirm('确定删除该记录？', '提示', { type: 'warning' }).then(async () => {
    const res = await axios.delete(API + '/' + row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() } else ElMessage.error('删除失败')
  }).catch(() => {})
}
async function handleSubmit() {
  const res = formData.id ? await axios.put(API + '/' + formData.id, formData) : await axios.post(API, formData)
  if (res.data.code === 200) { ElMessage.success('操作成功'); dialogVisible.value = false; loadData() }
  else ElMessage.error(res.data.message || '操作失败')
}
onMounted(loadData)
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.search-form { margin-bottom: 20px; }
</style>

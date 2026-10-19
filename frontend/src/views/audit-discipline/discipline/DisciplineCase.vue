<template>
  <div class="page-container">
    <div class="page-header">
      <h2>纪检案件</h2>
      <el-button type="primary" @click="handleAdd">新增案件</el-button>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="案件编号">
        <el-input v-model="searchForm.caseNo" placeholder="请输入案件编号" style="width:180px" />
      </el-form-item>
      <el-form-item label="案件类型">
        <el-select v-model="searchForm.caseType" placeholder="选择类型" style="width:150px" clearable>
          <el-option label="违纪" value="discipline" />
          <el-option label="违法" value="illegal" />
          <el-option label="作风" value="style" />
        </el-select>
      </el-form-item>
      <el-form-item label="等级">
        <el-select v-model="searchForm.caseLevel" placeholder="选择等级" style="width:130px" clearable>
          <el-option label="一般" value="general" />
          <el-option label="较重" value="serious" />
          <el-option label="严重" value="severe" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width:150px" clearable>
          <el-option label="线索登记" value="registered" />
          <el-option label="初步核实" value="verified" />
          <el-option label="立案" value="filed" />
          <el-option label="调查取证" value="investigating" />
          <el-option label="审理" value="reviewing" />
          <el-option label="处分决定" value="decided" />
          <el-option label="已结案" value="closed" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" border stripe size="small" v-loading="loading">
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="caseNo" label="案件编号" width="150" />
      <el-table-column prop="caseType" label="类型" width="100">
        <template #default="{row}">{{ getTypeLabel(row.caseType) }}</template>
      </el-table-column>
      <el-table-column prop="caseLevel" label="等级" width="100">
        <template #default="{row}">{{ getLevelLabel(row.caseLevel) }}</template>
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
          <el-button v-if="nextStage[row.status]" size="small" :type="row.status==='reviewing'?'danger':'success'"
            @click="handleAdvance(row)">{{ nextStage[row.status].label }}</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
      :current-page="pagination.currentPage" :page-sizes="[10,20,50]" :page-size="pagination.pageSize"
      :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" style="margin-top:20px;text-align:right" />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="案件编号"><el-input v-model="formData.caseNo" /></el-form-item>
        <el-form-item label="案件类型">
          <el-select v-model="formData.caseType">
            <el-option label="违纪" value="discipline" /><el-option label="违法" value="illegal" /><el-option label="作风" value="style" />
          </el-select>
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="formData.caseLevel">
            <el-option label="一般" value="general" /><el-option label="较重" value="serious" /><el-option label="严重" value="severe" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status">
            <el-option label="线索登记" value="registered" />
            <el-option label="初步核实" value="verified" />
            <el-option label="立案" value="filed" />
            <el-option label="调查取证" value="investigating" />
            <el-option label="审理" value="reviewing" />
            <el-option label="处分决定" value="decided" />
            <el-option label="已结案" value="closed" />
          </el-select>
        </el-form-item>
        <el-form-item label="责任人"><el-input v-model="formData.responsiblePerson" /></el-form-item>
        <el-form-item label="发生时间"><el-date-picker v-model="formData.occurTime" type="datetime" placeholder="选择发生时间" style="width:100%" /></el-form-item>
        <el-form-item label="处置结果"><el-input v-model="formData.handleResult" type="textarea" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="formData.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="案件详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="案件编号">{{ detailData.caseNo }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ getTypeLabel(detailData.caseType) }}</el-descriptions-item>
        <el-descriptions-item label="等级">{{ getLevelLabel(detailData.caseLevel) }}</el-descriptions-item>
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

const API = '/api/discipline/case'
const loading = ref(false)
const searchForm = reactive({ caseNo: '', caseType: '', caseLevel: '', status: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增纪检案件')
const list = ref([])
const formData = reactive({ id: null, caseNo: '', caseType: 'discipline', caseLevel: 'general', status: 'registered', responsiblePerson: '', occurTime: '', handleResult: '', description: '' })
const detailData = reactive({})

const typeMap = { discipline: '违纪', illegal: '违法', style: '作风' }
const levelMap = { general: '一般', serious: '较重', severe: '严重' }
const statusMap = { registered: '线索登记', verified: '初步核实', filed: '立案', investigating: '调查取证', reviewing: '审理', decided: '处分决定', closed: '已结案', pending: '线索登记', handling: '调查中' }
const statusTypeMap = { registered: 'info', verified: 'info', filed: 'warning', investigating: 'warning', reviewing: 'primary', decided: 'danger', closed: 'success' }
const nextStage = {
  registered: { act: 'verify', label: '初步核实' },
  verified: { act: 'file', label: '立案' },
  filed: { act: 'investigate', label: '调查取证' },
  investigating: { act: 'review', label: '审理' },
  reviewing: { act: 'decide', label: '处分决定' },
  decided: { act: 'close', label: '结案' }
}
const getTypeLabel = v => typeMap[v] || v
const getLevelLabel = v => levelMap[v] || v
const getStatusLabel = v => statusMap[v] || v
const getStatusType = v => statusTypeMap[v] || 'info'

async function advance(id, act, body) {
  return axios.put(API + '/' + id + '/' + act, body || {})
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
const resetSearch = () => { Object.assign(searchForm, { caseNo: '', caseType: '', caseLevel: '', status: '' }); pagination.currentPage = 1; loadData() }
const handleSizeChange = v => { pagination.pageSize = v; loadData() }
const handleCurrentChange = v => { pagination.currentPage = v; loadData() }

function handleAdd() {
  dialogTitle.value = '新增纪检案件'
  Object.assign(formData, { id: null, caseNo: '', caseType: 'discipline', caseLevel: 'general', status: 'registered', responsiblePerson: '', occurTime: '', handleResult: '', description: '' })
  dialogVisible.value = true
}
function handleEdit(row) { dialogTitle.value = '编辑纪检案件'; Object.assign(formData, row); dialogVisible.value = true }

function handleAdvance(row) {
  const step = nextStage[row.status]
  if (!step) return
  const doAdvance = (body) => {
    advance(row.id, step.act, body).then(res => {
      if (res.data.code === 200) { ElMessage.success(step.label + '完成'); loadData() }
      else ElMessage.error(res.data.message || (step.label + '失败'))
    }).catch(() => ElMessage.error(step.label + '失败'))
  }
  if (step.act === 'decide') {
    ElMessageBox.prompt('请填写处分/处置决定内容', '处分决定', {
      confirmButtonText: '作出决定', cancelButtonText: '取消', inputType: 'textarea',
      inputValue: row.handleResult || '',
      inputValidator: (v) => (v && v.trim() ? true : '处分决定内容不能为空')
    }).then(({ value }) => doAdvance({ handleResult: value })).catch(() => {})
  } else {
    doAdvance({})
  }
}
async function viewDetail(row) {
  const res = await axios.get(API + '/' + row.id)
  if (res.data.code === 200) { Object.assign(detailData, res.data.data); detailVisible.value = true }
  else ElMessage.error('获取详情失败')
}
function handleDelete(row) {
  ElMessageBox.confirm('确定删除该案件？', '提示', { type: 'warning' }).then(async () => {
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

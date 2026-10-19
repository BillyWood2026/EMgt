<template>
  <div class="page-container">
    <div class="page-header">
      <h2>廉政建设（教育）</h2>
      <el-button type="primary" @click="handleAdd">新增教育</el-button>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="教育编号">
        <el-input v-model="searchForm.eduNo" placeholder="请输入教育编号" style="width:180px" />
      </el-form-item>
      <el-form-item label="教育类型">
        <el-select v-model="searchForm.eduType" placeholder="选择类型" style="width:150px" clearable>
          <el-option label="专题学习" value="study" />
          <el-option label="警示教育" value="warning" />
          <el-option label="廉政培训" value="training" />
        </el-select>
      </el-form-item>
      <el-form-item label="等级">
        <el-select v-model="searchForm.level" placeholder="选择等级" style="width:130px" clearable>
          <el-option label="一般" value="general" />
          <el-option label="重点" value="key" />
          <el-option label="专项" value="special" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width:130px" clearable>
          <el-option label="计划中" value="planned" />
          <el-option label="进行中" value="ongoing" />
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
      <el-table-column prop="eduNo" label="教育编号" width="150" />
      <el-table-column prop="eduType" label="教育类型" width="110">
        <template #default="{row}">{{ getTypeLabel(row.eduType) }}</template>
      </el-table-column>
      <el-table-column prop="level" label="等级" width="100">
        <template #default="{row}">{{ getLevelLabel(row.level) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}"><el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="responsiblePerson" label="责任人" width="120" />
      <el-table-column prop="occurTime" label="开展时间" width="160" />
      <el-table-column prop="result" label="成效" min-width="160" show-overflow-tooltip />
      <el-table-column prop="participantCount" label="参与人数" width="90" />
      <el-table-column prop="commitmentCount" label="承诺人数" width="90" />
      <el-table-column label="操作" width="290">
        <template #default="{row}">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 'planned'" size="small" type="warning" @click="handleAdvance(row, 'start')">开展活动</el-button>
          <el-button v-if="row.status === 'ongoing'" size="small" type="info" @click="handleAdvance(row, 'signin')">签到</el-button>
          <el-button v-if="row.status === 'ongoing'" size="small" type="success" @click="handleAdvance(row, 'complete')">完成</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
      :current-page="pagination.currentPage" :page-sizes="[10,20,50]" :page-size="pagination.pageSize"
      :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" style="margin-top:20px;text-align:right" />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="教育编号"><el-input v-model="formData.eduNo" /></el-form-item>
        <el-form-item label="教育类型">
          <el-select v-model="formData.eduType">
            <el-option label="专题学习" value="study" /><el-option label="警示教育" value="warning" /><el-option label="廉政培训" value="training" />
          </el-select>
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="formData.level">
            <el-option label="一般" value="general" /><el-option label="重点" value="key" /><el-option label="专项" value="special" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status">
            <el-option label="计划中" value="planned" /><el-option label="进行中" value="ongoing" /><el-option label="已完成" value="done" />
          </el-select>
        </el-form-item>
        <el-form-item label="责任人"><el-input v-model="formData.responsiblePerson" /></el-form-item>
        <el-form-item label="开展时间"><el-date-picker v-model="formData.occurTime" type="datetime" placeholder="选择开展时间" style="width:100%" /></el-form-item>
        <el-form-item label="参与人数"><el-input-number v-model="formData.participantCount" :min="0" /></el-form-item>
        <el-form-item label="承诺人数"><el-input-number v-model="formData.commitmentCount" :min="0" /></el-form-item>
        <el-form-item label="成效"><el-input v-model="formData.result" type="textarea" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="formData.content" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="廉政教育详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="教育编号">{{ detailData.eduNo }}</el-descriptions-item>
        <el-descriptions-item label="教育类型">{{ getTypeLabel(detailData.eduType) }}</el-descriptions-item>
        <el-descriptions-item label="等级">{{ getLevelLabel(detailData.level) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusLabel(detailData.status) }}</el-descriptions-item>
        <el-descriptions-item label="责任人">{{ detailData.responsiblePerson }}</el-descriptions-item>
        <el-descriptions-item label="开展时间">{{ detailData.occurTime }}</el-descriptions-item>
        <el-descriptions-item label="参与/签到人数">{{ detailData.participantCount }}</el-descriptions-item>
        <el-descriptions-item label="廉政承诺人数">{{ detailData.commitmentCount }}</el-descriptions-item>
        <el-descriptions-item label="成效" :span="2">{{ detailData.result }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">{{ detailData.content }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'

const API = '/api/discipline/integrity'
const loading = ref(false)
const searchForm = reactive({ eduNo: '', eduType: '', level: '', status: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增廉政教育')
const list = ref([])
const formData = reactive({ id: null, eduNo: '', eduType: 'study', level: 'general', status: 'planned', responsiblePerson: '', occurTime: '', result: '', participantCount: 0, commitmentCount: 0, content: '' })
const detailData = reactive({})

const typeMap = { study: '专题学习', warning: '警示教育', training: '廉政培训' }
const levelMap = { general: '一般', key: '重点', special: '专项' }
const statusMap = { planned: '计划中', ongoing: '进行中', done: '已完成' }
const statusTypeMap = { planned: 'info', ongoing: 'warning', done: 'success' }
const getTypeLabel = v => typeMap[v] || v
const getLevelLabel = v => levelMap[v] || v
const getStatusLabel = v => statusMap[v] || v
const getStatusType = v => statusTypeMap[v] || 'info'

function handleAdvance(row, act) {
  const call = (body) => axios.put(API + '/' + row.id + '/' + act, body || {}).then(res => {
    if (res.data.code === 200) {
      ElMessage.success({ start: '活动已开展', signin: '签到已登记', complete: '活动已完成（宣传/承诺已登记）' }[act] || '操作成功')
      loadData()
    } else ElMessage.error(res.data.message || '操作失败')
  }).catch(() => ElMessage.error('操作失败'))
  if (act === 'signin') {
    ElMessageBox.prompt('请输入本次参与/签到人数', '签到登记', {
      confirmButtonText: '登记', cancelButtonText: '取消', inputValue: String(row.participantCount || 0),
      inputValidator: (v) => {
        const n = Number(v)
        return (Number.isInteger(n) && n >= 0) ? true : '请输入非负整数'
      }
    }).then(({ value }) => call({ participantCount: Number(value) })).catch(() => {})
  } else if (act === 'complete') {
    ElMessageBox.prompt('请填写活动成效（宣传教育/廉政承诺情况）', '完成活动', {
      confirmButtonText: '完成', cancelButtonText: '取消', inputType: 'textarea', inputValue: row.result || '',
      inputValidator: (v) => (v && v.trim() ? true : '成效不能为空')
    }).then(({ value }) => call({
      result: value,
      participantCount: row.participantCount || 0,
      commitmentCount: row.participantCount || 0
    })).catch(() => {})
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
const resetSearch = () => { Object.assign(searchForm, { eduNo: '', eduType: '', level: '', status: '' }); pagination.currentPage = 1; loadData() }
const handleSizeChange = v => { pagination.pageSize = v; loadData() }
const handleCurrentChange = v => { pagination.currentPage = v; loadData() }

function handleAdd() {
  dialogTitle.value = '新增廉政教育'
  Object.assign(formData, { id: null, eduNo: '', eduType: 'study', level: 'general', status: 'planned', responsiblePerson: '', occurTime: '', result: '', participantCount: 0, commitmentCount: 0, content: '' })
  dialogVisible.value = true
}
function handleEdit(row) { dialogTitle.value = '编辑廉政教育'; Object.assign(formData, row); dialogVisible.value = true }
async function viewDetail(row) {
  const res = await axios.get(API + '/' + row.id)
  if (res.data.code === 200) { Object.assign(detailData, res.data.data); detailVisible.value = true }
  else ElMessage.error('获取详情失败')
}
function handleDelete(row) {
  ElMessageBox.confirm('确定删除该教育记录？', '提示', { type: 'warning' }).then(async () => {
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

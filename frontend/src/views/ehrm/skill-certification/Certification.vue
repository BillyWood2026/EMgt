<template>
  <div class="page-container">
    <div class="page-header">
      <h2>技能认证</h2>
      <el-button type="primary" @click="handleAdd">新增认证</el-button>
    </div>

    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon blue"><el-icon><Certificate /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ totalCount }}</div>
            <div class="stat-label">认证总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon green"><el-icon><CheckCircle /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ passedCount }}</div>
            <div class="stat-label">已通过</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon orange"><el-icon><Clock /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ pendingCount }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon red"><el-icon><CircleClose /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ failedCount }}</div>
            <div class="stat-label">未通过</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="员工姓名">
        <el-input v-model="searchForm.employeeName" placeholder="请输入员工姓名" style="width:150px" />
      </el-form-item>
      <el-form-item label="技能名称">
        <el-input v-model="searchForm.skillName" placeholder="请输入技能名称" style="width:180px" />
      </el-form-item>
      <el-form-item label="认证状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width:120px">
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="passed" />
          <el-option label="未通过" value="failed" />
          <el-option label="有效期内" value="valid" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="certificationList" border stripe size="small" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="certNo" label="认证编号" width="140" />
      <el-table-column prop="employeeNo" label="员工编号" width="120" />
      <el-table-column prop="employeeName" label="员工姓名" width="100">
        <template #default="{row}">
          <el-link type="primary" @click="viewDetail(row)">{{ row.employeeName }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="skillName" label="技能名称" width="150" />
      <el-table-column prop="level" label="认证等级" width="100">
        <template #default="{row}">
          <el-tag :type="getLevelType(row.level)" size="small">{{ getLevelLabel(row.level) }}</el-tag>
        </template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展：证书类型（内部认证/社会证书） -->
      <el-table-column prop="certType" label="证书类型" width="100" align="center">
        <template #default="{row}">
          <el-tag v-if="row.certType" :type="row.certType === 'INTERNAL' ? 'primary' : 'warning'" size="small">
            {{ row.certType === 'INTERNAL' ? '内部认证' : '社会证书' }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展：发证机构 -->
      <el-table-column prop="issueOrg" label="发证机构" width="120" show-overflow-tooltip>
        <template #default="{row}">{{ row.issueOrg || '-' }}</template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展：绑定岗位（持证准入） -->
      <el-table-column prop="bindPosition" label="绑定岗位" width="120" show-overflow-tooltip>
        <template #default="{row}">{{ row.bindPosition || '-' }}</template>
      </el-table-column>
      <el-table-column prop="score" label="得分" width="80">
        <template #default="{row}">
          <span :style="{ color: row.score >= 80 ? '#67c23a' : row.score >= 60 ? '#faad14' : '#f56c6c' }">{{ row.score }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="examDate" label="考试日期" width="120" />
      <el-table-column prop="validFrom" label="生效日期" width="120" />
      <el-table-column prop="validTo" label="失效日期" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{row}">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 'pending'" size="small" type="success" @click="handlePass(row)">通过</el-button>
          <el-button v-if="row.status === 'pending'" size="small" type="danger" @click="handleFail(row)">不通过</el-button>
          <el-button v-if="row.status === 'passed' || row.status === 'valid'" size="small" type="danger" @click="handleRevoke(row)">作废</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      v-model:current-page="pagination.current"
      :page-sizes="[10, 20, 50, 100]"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top: 20px; text-align: right;"
      v-loading="loading"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-form-item label="认证编号" required>
          <el-input v-model="formData.certNo" placeholder="请输入认证编号" />
        </el-form-item>
        <el-form-item label="员工编号" required>
          <el-input v-model="formData.employeeNo" placeholder="请输入员工编号" />
        </el-form-item>
        <el-form-item label="员工姓名" required>
          <el-input v-model="formData.employeeName" placeholder="请输入员工姓名" />
        </el-form-item>
        <el-form-item label="技能名称" required>
          <el-select v-model="formData.skillName" placeholder="选择技能名称">
            <el-option label="CNC操作技能" value="CNC操作技能" />
            <el-option label="质量管理体系" value="质量管理体系" />
            <el-option label="项目管理" value="项目管理" />
            <el-option label="焊接技能" value="焊接技能" />
            <el-option label="PLC编程" value="PLC编程" />
          </el-select>
        </el-form-item>
        <el-form-item label="认证等级">
          <el-select v-model="formData.level">
            <el-option label="初级" value="level1" />
            <el-option label="中级" value="level2" />
            <el-option label="高级" value="level3" />
            <el-option label="专家级" value="level4" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试得分">
          <el-input-number v-model="formData.score" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="考试日期">
          <el-date-picker v-model="formData.examDate" type="date" placeholder="选择考试日期" />
        </el-form-item>
        <el-form-item label="生效日期">
          <el-date-picker v-model="formData.validFrom" type="date" placeholder="选择生效日期" />
        </el-form-item>
        <el-form-item label="失效日期">
          <el-date-picker v-model="formData.validTo" type="date" placeholder="选择失效日期" />
        </el-form-item>
        <el-form-item label="认证结果">
          <el-input type="textarea" v-model="formData.result" :rows="3" placeholder="请输入认证结果" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展：证书类型 / 发证机构 / 绑定岗位 -->
        <el-form-item label="证书类型">
          <el-select v-model="formData.certType" placeholder="选择证书类型">
            <el-option label="内部认证" value="INTERNAL" />
            <el-option label="社会证书" value="EXTERNAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="发证机构">
          <el-input v-model="formData.issueOrg" placeholder="请输入发证机构（如：集团技能认定中心）" />
        </el-form-item>
        <el-form-item label="绑定岗位">
          <el-input v-model="formData.bindPosition" placeholder="请输入绑定岗位（持证准入岗位）" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status">
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="passed" />
            <el-option label="未通过" value="failed" />
            <el-option label="有效期内" value="valid" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="技能认证详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="认证编号">{{ detailData.certNo }}</el-descriptions-item>
        <el-descriptions-item label="员工编号">{{ detailData.employeeNo }}</el-descriptions-item>
        <el-descriptions-item label="员工姓名">{{ detailData.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="技能名称">{{ detailData.skillName }}</el-descriptions-item>
        <el-descriptions-item label="认证等级">
          <el-tag :type="getLevelType(detailData.level)" size="small">{{ getLevelLabel(detailData.level) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="考试得分">
          <span :style="{ color: detailData.score >= 80 ? '#67c23a' : detailData.score >= 60 ? '#faad14' : '#f56c6c' }">{{ detailData.score }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="考试日期">{{ detailData.examDate }}</el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ detailData.validFrom }}</el-descriptions-item>
        <el-descriptions-item label="失效日期">{{ detailData.validTo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)" size="small">{{ getStatusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="认证结果" :span="2">{{ detailData.result || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/api/axiosConfig'

const searchForm = reactive({
  employeeName: '',
  skillName: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增技能认证')
const formRef = ref(null)

const formData = reactive({
  id: null,
  certNo: '',
  employeeNo: '',
  employeeName: '',
  skillName: '',
  level: 'level1',
  score: 0,
  examDate: '',
  validFrom: '',
  validTo: '',
  result: '',
  status: 'pending',
  // 规格书字段，待后端扩展
  certType: 'INTERNAL',
  issueOrg: '',
  bindPosition: ''
})

const detailData = reactive({})
const certificationList = ref([])
const loading = ref(false)

const totalCount = computed(() => pagination.total)
const passedCount = computed(() => certificationList.value.filter(c => c.status === 'passed' || c.status === 'valid').length)
const pendingCount = computed(() => certificationList.value.filter(c => c.status === 'pending').length)
const failedCount = computed(() => certificationList.value.filter(c => c.status === 'failed').length)

const loadList = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/skill-cert/annual-review/list', {
      params: {
        current: pagination.current,
        size: pagination.size,
        ...searchForm
      }
    })
    const data = res.data?.data || res.data
    certificationList.value = data.records || data.list || []
    pagination.total = data.total || 0
  } catch (error) {
    console.error('加载列表失败', error)
  } finally {
    loading.value = false
  }
}

function getLevelLabel(level) {
  const map = { level1: '初级', level2: '中级', level3: '高级', level4: '专家级' }
  return map[level] || level
}

function getLevelType(level) {
  const map = { level1: 'info', level2: 'primary', level3: 'warning', level4: 'danger' }
  return map[level] || 'info'
}

function getStatusLabel(status) {
  const map = { pending: '待审核', passed: '已通过', failed: '未通过', valid: '有效期内' }
  return map[status] || status
}

function getStatusType(status) {
  const map = { pending: 'warning', passed: 'success', failed: 'danger', valid: 'success' }
  return map[status] || 'info'
}

function handleSearch() {
  pagination.current = 1
  loadList()
}

function resetSearch() {
  searchForm.employeeName = ''
  searchForm.skillName = ''
  searchForm.status = ''
  pagination.current = 1
  loadList()
}

function handleSizeChange(val) {
  pagination.size = val
  loadList()
}

function handleCurrentChange(val) {
  pagination.current = val
  loadList()
}

function handleAdd() {
  dialogTitle.value = '新增技能认证'
  Object.assign(formData, {
    id: null,
    certNo: '',
    employeeNo: '',
    employeeName: '',
    skillName: '',
    level: 'level1',
    score: 0,
    examDate: '',
    validFrom: '',
    validTo: '',
    result: '',
    status: 'pending',
    certType: 'INTERNAL',
    issueOrg: '',
    bindPosition: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑技能认证'
  Object.assign(formData, row)
  dialogVisible.value = true
}

function viewDetail(row) {
  Object.assign(detailData, row)
  detailVisible.value = true
}

async function handlePass(row) {
  try {
    await ElMessageBox.confirm('确定要通过该认证吗？', '确认', { type: 'warning' })
    await axios.post(`/api/skill-cert/assessment/${row.id}/approve-level`)
    ElMessage.success('认证已通过')
    loadList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败', error)
    }
  }
}

async function handleFail(row) {
  try {
    await ElMessageBox.confirm('确定要拒绝该认证吗？', '确认', { type: 'warning' })
    await axios.post(`/api/skill-cert/assessment/${row.id}/reject-level`)
    ElMessage.success('认证已拒绝')
    loadList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败', error)
    }
  }
}

// 作废证书：红色二次确认，走年审/证书台账 revoke 端点
async function handleRevoke(row) {
  try {
    await ElMessageBox.confirm(
      `确定作废 ${row.employeeName} 的认证证书"${row.certNo || row.skillName}"吗？作废后证书立即失效，持证准入岗位将触发合规预警。`,
      '作废证书确认（高危操作）',
      { type: 'error', confirmButtonText: '确认作废' }
    )
    await axios.put(`/api/skill-cert/annual-review/${row.id}/revoke`)
    ElMessage.success('证书已作废')
    loadList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('作废失败', error)
      ElMessage.error('作废失败')
    }
  }
}

async function handleSubmit() {
  try {
    if (formData.id) {
      await axios.put(`/api/skill-cert/annual-review/${formData.id}`, formData)
    } else {
      await axios.post('/api/skill-cert/annual-review', formData)
    }
    dialogVisible.value = false
    ElMessage.success(dialogTitle.value === '新增技能认证' ? '新增成功' : '修改成功')
    loadList()
  } catch (error) {
    console.error('提交失败', error)
    ElMessage.error('提交失败')
  }
}

onMounted(() => {
  loadList()
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

.stat-icon.blue { background-color: #e6f7ff; color: #1890ff; }
.stat-icon.green { background-color: #f6ffed; color: #52c41a; }
.stat-icon.orange { background-color: #fff7e6; color: #fa8c16; }
.stat-icon.red { background-color: #fff1f0; color: #f5222d; }

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
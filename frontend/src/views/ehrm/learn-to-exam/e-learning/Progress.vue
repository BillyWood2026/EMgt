<template>
  <div class="page-card">
    <div class="page-header">
      <h2>学习进度</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>分配课程
        </el-button>
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px">
      查询员工在线学习进度，统计已学课程数、累计学时、完成率与最近学习时间，支持按部门、状态筛选与进度催办。
    </el-alert>

    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="员工姓名">
          <el-input v-model="searchForm.employee" placeholder="请输入员工姓名" style="width:180px" clearable />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.department" placeholder="选择部门" style="width:180px" clearable>
            <el-option v-for="d in departments" :key="d" :label="d" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item label="完成状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" style="width:150px" clearable>
            <el-option label="未开始" value="not_started" />
            <el-option label="学习中" value="learning" />
            <el-option label="已完成" value="completed" />
            <el-option label="已逾期" value="overdue" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="progressList" border stripe size="small">
        <el-table-column prop="id" label="记录ID" width="110" />
        <el-table-column prop="employee" label="员工姓名" width="110">
          <template #default="{row}">
            <el-link type="primary" @click="viewDetail(row)">{{ row.employee }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="120" />
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="assignType" label="指派类型" width="100" align="center">
          <template #default="{row}">
            <el-tag :type="row.assignType === '强制' ? 'danger' : 'success'" size="small">{{ row.assignType || '自主' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="courseCount" label="已学课程" width="100" align="center" />
        <el-table-column prop="totalHours" label="累计学时" width="100" align="center">
          <template #default="{row}">{{ row.totalHours }} h</template>
        </el-table-column>
        <el-table-column prop="credit" label="累计学分" width="100" align="center" />
        <el-table-column prop="progress" label="完成进度" width="200" align="center">
          <template #default="{row}">
            <el-progress :percentage="row.progress" :status="getProgressStatus(row.progress)" :stroke-width="12" />
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="homeworkStatus" label="作业状态" width="90" align="center">
          <template #default="{row}">{{ row.homeworkStatus || '—' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="examStatus" label="考试状态" width="90" align="center">
          <template #default="{row}">{{ row.examStatus || '—' }}</template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止时间" width="120" />
        <!-- 预警等级（前端算法）：截止前3天黄色提醒，超时未学红色督办 -->
        <el-table-column label="预警等级" width="100" align="center">
          <template #default="{row}">
            <el-tag :type="getWarnLevel(row).type" size="small">{{ getWarnLevel(row).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastStudyTime" label="最近学习时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" type="warning" @click="editItem(row)">编辑</el-button>
            <el-button link size="small" type="success" @click="remindStudy(row)">催办</el-button>
            <el-button link size="small" type="danger" @click="deleteItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top:16px; text-align:right"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form ref="formRef" :model="formData" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="员工姓名" required>
              <el-input v-model="formData.employeeName" placeholder="请输入员工姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门" required>
              <el-select v-model="formData.department" placeholder="选择部门" style="width:100%">
                <el-option v-for="d in departments" :key="d" :label="d" :value="d" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分配课程">
              <el-input v-model="formData.courseName" placeholder="请输入分配的课程" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标学时">
              <el-input-number v-model="formData.targetHours" :min="1" :max="500" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="截止日期">
              <el-date-picker v-model="formData.deadline" type="date" placeholder="选择截止日期" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否必修">
              <el-switch v-model="formData.required" active-value="mandatory" inactive-value="elective" active-text="必修" inactive-text="选修" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- EL-TA01 批量指派规则（规格书字段，待后端扩展） -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="指派类型">
              <el-select v-model="formData.assignType" placeholder="选择指派类型" style="width:100%">
                <el-option label="自主选课" value="自主" />
                <el-option label="强制指派" value="强制" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="逾期预警天数">
              <el-input-number v-model="formData.overdueWarnDays" :min="1" :max="30" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="学习备注">
          <el-input type="textarea" v-model="formData.remark" :rows="3" placeholder="请输入备注说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="saveData">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import request from '@/api/axiosConfig'

const departments = ['研发部', '生产部', '销售部', '财务部', '人力资源部', '行政部']

const searchForm = reactive({ employee: '', department: '', status: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('分配课程')
const formRef = ref(null)

const formData = reactive({
  id: '', employeeName: '', department: '', courseName: '',
  targetHours: 40, deadline: '', required: 'mandatory', remark: '',
  // 规格书字段，待后端扩展
  assignType: '自主', overdueWarnDays: 3
})

const progressList = ref([])

function getStatusLabel(value) { const labels = { not_started: '未开始', NOT_STARTED: '未开始', learning: '学习中', LEARNING: '学习中', completed: '已完成', COMPLETED: '已完成', overdue: '已逾期', OVERDUE: '已逾期' }; return labels[value] || value }
function getStatusType(value) { const types = { not_started: 'info', NOT_STARTED: 'info', learning: 'primary', LEARNING: 'primary', completed: 'success', COMPLETED: 'success', overdue: 'danger', OVERDUE: 'danger' }; return types[value] || 'info' }
function getProgressStatus(percentage) { if (percentage >= 100) return 'success'; if (percentage < 30) return 'exception'; return undefined }

// 预警等级算法（规格书）：已完成无预警；截止前3天黄色学习提醒；超时未完成红色督办（必修超时锁定学分与上岗资格）
function getWarnLevel(row) {
  const status = String(row.status || '')
  if (status === 'COMPLETED' || status === 'completed') return { label: '无', type: 'info' }
  if (status === 'OVERDUE' || status === 'overdue') return { label: '红色督办', type: 'danger' }
  if (row.deadline) {
    const days = Math.ceil((new Date(row.deadline).getTime() - Date.now()) / 86400000)
    if (days < 0) return { label: '红色督办', type: 'danger' }
    if (days <= 3) return { label: '黄色提醒', type: 'warning' }
  }
  return { label: '正常', type: 'success' }
}

async function loadProgress() {
  try {
    const { data } = await request.get('/api/learn/tasks/list', {
      params: { pageNum: pagination.currentPage, pageSize: pagination.pageSize }
    })
    const records = data?.records || data?.data || []
    progressList.value = records.map(r => ({
      id: r.id, employee: r.employeeName || r.employee, department: r.department,
      courseName: r.courseName, courseCount: r.courseCount || 0,
      totalHours: r.totalHours || 0, credit: r.credit || 0,
      progress: r.progress || 0, lastStudyTime: r.lastStudyTime || '',
      status: r.status || 'NOT_STARTED', deadline: r.deadline ? String(r.deadline).slice(0,10) : '',
      // 规格书字段，待后端扩展
      assignType: r.assignType, homeworkStatus: r.homeworkStatus, examStatus: r.examStatus
    }))
    pagination.total = data?.total || progressList.value.length
  } catch (e) {
    ElMessage.error('学习进度加载失败：' + (e.message || ''))
  }
}

function doSearch() { pagination.currentPage = 1; loadProgress() }
function resetSearch() { searchForm.employee = ''; searchForm.department = ''; searchForm.status = ''; loadProgress() }
function refreshData() { loadProgress(); ElMessage.success('数据已刷新') }

function openCreate() {
  dialogTitle.value = '分配课程'
  Object.assign(formData, { id: '', employeeName: '', department: '', courseName: '', targetHours: 40, deadline: '', required: 'mandatory', remark: '' })
  dialogVisible.value = true
}

function viewDetail(row) { ElMessage.info(`查看员工学习详情: ${row.employee}`) }

function editItem(row) {
  dialogTitle.value = '编辑学习记录'
  Object.assign(formData, {
    id: row.id, employeeName: row.employee, department: row.department, courseName: row.courseName,
    targetHours: row.targetHours || 40, deadline: row.deadline, required: row.required || 'mandatory', remark: row.remark || ''
  })
  dialogVisible.value = true
}

function remindStudy(row) { ElMessage.success(`已向 ${row.employee} 发送学习催办通知`) }

function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除 ${row.employee} 的学习记录吗？`, '删除确认', { type: 'warning' })
    .then(async () => { try { await request.delete(`/api/learn/tasks/${row.id}`); ElMessage.success('学习记录已删除'); loadProgress() } catch(e){ ElMessage.error('删除失败') } })
    .catch(() => {})
}

async function saveData() {
  if (!formData.employeeName) { ElMessage.warning('请填写员工姓名'); return }
  try {
    if (formData.id) {
      await request.put('/api/learn/tasks', formData)
    } else {
      await request.post('/api/learn/tasks', formData)
    }
    dialogVisible.value = false
    ElMessage.success(formData.id ? '学习记录已更新' : '课程已分配')
    loadProgress()
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => loadProgress())
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; gap: 8px; }
.search-form { margin-bottom: 0; }
</style>

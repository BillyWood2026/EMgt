<template>
  <div class="page-card">
    <div class="page-header">
      <h2>培养进度监控与督办</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>新建跟踪
        </el-button>
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="培养进度实时监控（721法则）"
      description="综合培养进度=理论学习进度×10%+岗位实践进度×70%+导师帮带进度×20%。滞后≤7天黄色预警，滞后>7天红色滞后督办并强制人工跟进；必达节点（开课/轮岗/帮带/实操/复盘/答辩/结业）超期未完成禁止结项。"
      style="margin-bottom: 16px"
    />

    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="学员姓名">
          <el-input v-model="searchForm.studentName" placeholder="请输入学员姓名" style="width: 180px" clearable />
        </el-form-item>
        <el-form-item label="所属项目">
          <el-select v-model="searchForm.projectId" placeholder="选择项目" style="width: 220px" clearable>
            <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="培养节点">
          <el-select v-model="searchForm.milestone" placeholder="选择节点" style="width: 150px" clearable>
            <el-option v-for="m in milestoneOptions" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="督办状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 140px" clearable>
            <el-option label="在训跟踪" value="ONGOING" />
            <el-option label="督办中" value="URGING" />
            <el-option label="已闭环" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="pagedList" border stripe size="small" v-loading="loading">
        <el-table-column prop="id" label="跟踪编号" width="90" />
        <el-table-column prop="employeeName" label="学员" width="90">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.employeeName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="梯队项目" min-width="170" show-overflow-tooltip />
        <el-table-column prop="milestone" label="培养节点" width="90">
          <template #default="{ row }">
            <el-tag size="small">{{ row.milestone || '-' }}</el-tag>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展：721三维度分项进度 -->
        <el-table-column label="学习/实践/帮带" width="150" align="center">
          <template #default="{ row }">
            <span v-if="row.theoryProgress != null">{{ row.theoryProgress }}% / {{ row.practiceProgress }}% / {{ row.mentorProgress }}%</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="综合培养进度" width="150">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :color="getProgressColor(row.progress)" :stroke-width="14" :text-inside="true" />
          </template>
        </el-table-column>
        <el-table-column prop="delayDays" label="滞后天数" width="90" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.delayDays > 7 ? '#f56c6c' : row.delayDays > 0 ? '#e6a23c' : '#67c23a' }">{{ row.delayDays > 0 ? row.delayDays + '天' : '未滞后' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="风险等级" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.riskLevel === '滞后' ? 'danger' : row.riskLevel === '预警' ? 'warning' : 'success'" size="small">{{ row.riskLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="督办状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'CLOSED' ? 'success' : row.status === 'URGING' ? 'danger' : 'primary'" size="small">{{ trackStatusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展：跟进人/闭环时间 -->
        <el-table-column prop="follower" label="跟进人" width="90">
          <template #default="{ row }">{{ row.follower || '-' }}</template>
        </el-table-column>
        <el-table-column prop="closeTime" label="闭环时间" width="110">
          <template #default="{ row }">{{ row.closeTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" @click="editItem(row)">编辑</el-button>
            <el-button v-if="row.status !== 'CLOSED' && row.riskLevel !== '正常'" link size="small" type="danger" @click="changeStatus(row, 'URGING')">发起督办</el-button>
            <el-button v-if="row.status === 'URGING'" link size="small" type="success" @click="changeStatus(row, 'CLOSED')">闭环</el-button>
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
        style="margin-top: 16px; text-align: right"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
      <el-form :model="formData" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学员姓名" required>
              <el-input v-model="formData.employeeName" placeholder="请输入学员姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属项目" required>
              <el-select v-model="formData.projectId" placeholder="选择项目" style="width: 100%">
                <el-option v-for="p in projectOptions" :key="p.id" :label="p.projectName" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="培养节点" required>
              <el-select v-model="formData.milestone" placeholder="选择必达节点" style="width: 100%">
                <el-option v-for="m in milestoneOptions" :key="m" :label="m" :value="m" />
              </el-select>
            </el-form-item>
          </el-col>
          <!-- 规格书字段，待后端扩展：跟进人 -->
          <el-col :span="12">
            <el-form-item label="跟进人">
              <el-input v-model="formData.follower" placeholder="督办跟进人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划完成日期">
              <el-date-picker v-model="formData.plannedDate" type="date" placeholder="选择计划日期" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际完成日期">
              <el-date-picker v-model="formData.actualDate" type="date" placeholder="未完成留空" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展：721三维度分项进度（综合进度自动加权） -->
        <el-divider content-position="left">721维度进度（综合进度=学习×10%+实践×70%+帮带×20%）</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="理论学习进度%">
              <el-input-number v-model="formData.theoryProgress" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="岗位实践进度%">
              <el-input-number v-model="formData.practiceProgress" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="导师帮带进度%">
              <el-input-number v-model="formData.mentorProgress" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="综合培养进度">
          <el-progress :percentage="compositeProgress" :color="getProgressColor(compositeProgress)" :stroke-width="16" :text-inside="true" />
        </el-form-item>
        <el-form-item label="跟踪备注">
          <el-input type="textarea" v-model="formData.comment" :rows="3" placeholder="滞后原因、整改要求、跟进评语、闭环验收记录" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveData">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import request from '@/api/axiosConfig'

// 过程监控必达节点（规格书固化）
const milestoneOptions = ['开课', '轮岗', '帮带', '实操', '复盘', '答辩', '结业']
const trackStatusMap = { ONGOING: '在训跟踪', URGING: '督办中', CLOSED: '已闭环' }

const searchForm = reactive({ studentName: '', projectId: '', milestone: '', status: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('新建跟踪')
const loading = ref(false)

const defaultForm = {
  id: '', employeeName: '', projectId: '', milestone: '', plannedDate: '', actualDate: '',
  theoryProgress: 0, practiceProgress: 0, mentorProgress: 0,
  follower: '', comment: '', status: 'ONGOING'
}
const formData = reactive({ ...defaultForm })
const trackingList = ref([])
const projectOptions = ref([])

// 721综合进度算法（规格书核心固定算法）
const compositeProgress = computed(() => Math.round(
  Number(formData.theoryProgress || 0) * 0.1 +
  Number(formData.practiceProgress || 0) * 0.7 +
  Number(formData.mentorProgress || 0) * 0.2))

const filteredList = computed(() => {
  let list = [...trackingList.value]
  if (searchForm.studentName) list = list.filter(t => t.employeeName?.includes(searchForm.studentName))
  if (searchForm.projectId) list = list.filter(t => t.projectId === searchForm.projectId)
  if (searchForm.milestone) list = list.filter(t => t.milestone === searchForm.milestone)
  if (searchForm.status) list = list.filter(t => t.status === searchForm.status)
  return list
})
const pagedList = computed(() => {
  pagination.total = filteredList.value.length
  const start = (pagination.currentPage - 1) * pagination.pageSize
  return filteredList.value.slice(start, start + pagination.pageSize)
})

function getProgressColor(p) {
  if (p >= 80) return '#67c23a'
  if (p >= 60) return '#409eff'
  if (p >= 40) return '#e6a23c'
  return '#f56c6c'
}

// 滞后天数与风险等级（规格书：≤7天预警、>7天滞后督办）
function calcDelay(row) {
  if (!row.plannedDate) return 0
  const end = row.actualDate ? new Date(row.actualDate) : new Date()
  const diff = Math.floor((end - new Date(row.plannedDate)) / 86400000)
  return Math.max(0, diff)
}
function riskOf(days) {
  if (days > 7) return '滞后'
  if (days > 0) return '预警'
  return '正常'
}

async function loadProjects() {
  try {
    const { data } = await request.get('/api/talent/dev-project/list')
    projectOptions.value = data?.data || data || []
  } catch (e) { /* 项目下拉加载失败不阻断 */ }
}

async function loadTracking() {
  loading.value = true
  try {
    const { data } = await request.get('/api/talent/tracking/list')
    const list = data?.data || data || []
    const projMap = {}
    projectOptions.value.forEach(p => { projMap[p.id] = p.projectName })
    trackingList.value = list.map(r => {
      const row = {
        id: r.id,
        projectId: r.projectId,
        projectName: projMap[r.projectId] || `项目#${r.projectId || '-'}`,
        employeeName: r.employeeName || '',
        milestone: r.milestone || '',
        plannedDate: r.plannedDate ? String(r.plannedDate).slice(0, 10) : '',
        actualDate: r.actualDate ? String(r.actualDate).slice(0, 10) : '',
        progress: Number(r.progress || 0),
        status: r.status || 'ONGOING',
        comment: r.comment || '',
        theoryProgress: r.theoryProgress, practiceProgress: r.practiceProgress, mentorProgress: r.mentorProgress,
        follower: r.follower || '', closeTime: r.closeTime || ''
      }
      row.delayDays = calcDelay(row)
      row.riskLevel = riskOf(row.delayDays)
      return row
    })
  } catch (e) {
    ElMessage.error('跟踪列表加载失败：' + (e.message || ''))
  } finally {
    loading.value = false
  }
}

async function loadAll() {
  await loadProjects()
  await loadTracking()
}

function doSearch() { pagination.currentPage = 1 }
function resetSearch() { searchForm.studentName = ''; searchForm.projectId = ''; searchForm.milestone = ''; searchForm.status = ''; pagination.currentPage = 1 }
function refreshData() { loadAll(); ElMessage.success('数据已刷新') }

function openCreate() {
  dialogTitle.value = '新建跟踪'
  Object.assign(formData, { ...defaultForm })
  dialogVisible.value = true
}

function viewDetail(row) { ElMessage.info(`查看学员跟踪详情：${row.employeeName}`) }

function editItem(row) {
  dialogTitle.value = '编辑跟踪'
  Object.assign(formData, {
    ...defaultForm, ...row,
    theoryProgress: row.theoryProgress ?? row.progress,
    practiceProgress: row.practiceProgress ?? row.progress,
    mentorProgress: row.mentorProgress ?? row.progress
  })
  dialogVisible.value = true
}

function buildPayload(src, statusOverride) {
  const progress = Math.round(Number(src.theoryProgress || 0) * 0.1 + Number(src.practiceProgress || 0) * 0.7 + Number(src.mentorProgress || 0) * 0.2)
  return {
    projectId: src.projectId,
    employeeId: src.employeeId,
    employeeName: src.employeeName,
    milestone: src.milestone,
    plannedDate: src.plannedDate || null,
    actualDate: src.actualDate || null,
    progress,
    status: statusOverride || src.status || 'ONGOING',
    comment: src.comment,
    // 规格书字段，待后端扩展（随提交携带）
    theoryProgress: src.theoryProgress, practiceProgress: src.practiceProgress, mentorProgress: src.mentorProgress,
    follower: src.follower,
    closeTime: statusOverride === 'CLOSED' ? new Date().toISOString().slice(0, 10) : src.closeTime
  }
}

// 督办流转：发起督办(→URGING) / 闭环(→CLOSED)
async function changeStatus(row, target) {
  const text = target === 'URGING' ? '发起督办' : '督办闭环'
  try {
    await ElMessageBox.confirm(`确定对学员「${row.employeeName}」执行${text}吗？`, `${text}确认`, { type: target === 'URGING' ? 'error' : 'warning' })
  } catch { return }
  try {
    await request.put(`/api/talent/tracking/${row.id}`, buildPayload(row, target))
    ElMessage.success(`${text}成功`)
    loadTracking()
  } catch (e) {
    ElMessage.error(`${text}失败：` + (e.response?.data?.message || e.message || ''))
  }
}

function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除跟踪记录「${row.employeeName}」吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      try {
        await request.delete(`/api/talent/tracking/${row.id}`)
        ElMessage.success('删除成功')
        loadTracking()
      } catch (e) { ElMessage.error('删除失败') }
    })
    .catch(() => {})
}

async function saveData() {
  if (!formData.employeeName || !formData.projectId || !formData.milestone) {
    ElMessage.warning('请填写学员姓名、所属项目、培养节点'); return
  }
  try {
    if (formData.id) {
      await request.put(`/api/talent/tracking/${formData.id}`, buildPayload(formData))
      ElMessage.success('跟踪记录已更新')
    } else {
      await request.post('/api/talent/tracking', buildPayload(formData))
      ElMessage.success('跟踪记录新增成功')
    }
    dialogVisible.value = false
    loadTracking()
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => loadAll())
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; gap: 8px; }
.search-form { margin-bottom: 0; }
</style>

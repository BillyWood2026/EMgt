<template>
  <div class="page-card">
    <div class="page-header">
      <h2>课程学习</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>新建课程
        </el-button>
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px">
      在线课程学习平台，支持专业技能、管理、合规、安全等多类课程在线学习，记录学时与学习进度，助力员工持续成长。
    </el-alert>

    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="课程名称">
          <el-input v-model="searchForm.name" placeholder="请输入课程名称" style="width:200px" clearable />
        </el-form-item>
        <el-form-item label="课程分类">
          <el-select v-model="searchForm.category" placeholder="选择分类" style="width:160px" clearable>
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="必修选修">
          <el-select v-model="searchForm.required" placeholder="选择性质" style="width:140px" clearable>
            <el-option label="必修" value="mandatory" />
            <el-option label="选修" value="elective" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" style="width:140px" clearable>
            <el-option label="已发布" value="published" />
            <el-option label="草稿" value="draft" />
            <el-option label="已下线" value="offline" />
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
      <el-table :data="courseList" border stripe size="small">
        <el-table-column prop="id" label="课程ID" width="110" />
        <el-table-column prop="name" label="课程名称" min-width="200">
          <template #default="{row}">
            <el-link type="primary" @click="viewDetail(row)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="课程分类" width="110">
          <template #default="{row}">
            <el-tag :type="getCategoryType(row.category)" size="small">{{ getCategoryLabel(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="学时" width="90" align="center">
          <template #default="{row}">{{ row.duration }} 学时</template>
        </el-table-column>
        <el-table-column prop="credit" label="学分" width="80" align="center" />
        <el-table-column prop="level" label="难度" width="100">
          <template #default="{row}">
            <el-tag :type="getLevelType(row.level)" size="small">{{ getLevelLabel(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="required" label="必修/选修" width="100" align="center">
          <template #default="{row}">
            <el-tag :type="row.required === 'mandatory' ? 'danger' : 'success'" size="small">
              {{ row.required === 'mandatory' ? '必修' : '选修' }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="learnMode" label="学习形式" width="100" align="center">
          <template #default="{row}">{{ row.learnMode || '自主' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="finishMode" label="结业方式" width="110">
          <template #default="{row}">{{ row.finishMode || '—' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="examStatus" label="考试状态" width="100" align="center">
          <template #default="{row}">
            <el-tag v-if="row.examStatus" size="small" type="primary">{{ row.examStatus }}</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="enrollCount" label="学习人数" width="100" align="center" />
        <el-table-column prop="version" label="版本号" width="80" align="center">
          <template #default="{row}">V{{ row.version || 1 }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" type="success" @click="startLearn(row)">学习</el-button>
            <el-button link size="small" @click="editItem(row)">编辑</el-button>
            <el-button link size="small" type="success" v-if="['DRAFT','draft','OFFLINE','offline'].includes(row.status)" @click="publishCourse(row)">发布</el-button>
            <el-button link size="small" type="warning" v-if="['PUBLISHED','published'].includes(row.status)" @click="offlineCourse(row)">下线</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="950px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基础信息" name="base">
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程名称" required>
              <el-input v-model="formData.courseName" placeholder="请输入课程名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程分类" required>
              <el-select v-model="formData.category" placeholder="选择分类" style="width:100%">
                <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学时" required>
              <el-input-number v-model="formData.duration" :min="1" :max="200" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学分" required>
              <el-input-number v-model="formData.credit" :min="0" :max="20" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="难度等级" required>
              <el-select v-model="formData.level" placeholder="选择难度" style="width:100%">
                <el-option label="入门" value="beginner" />
                <el-option label="中级" value="intermediate" />
                <el-option label="高级" value="advanced" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="必修课程">
              <el-switch v-model="formData.required" active-value="mandatory" inactive-value="elective" active-text="必修" inactive-text="选修" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="讲师">
              <el-input v-model="formData.teacher" placeholder="请输入讲师姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上线日期">
              <el-date-picker v-model="formData.onlineDate" type="date" placeholder="选择上线日期" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学习形式">
              <el-select v-model="formData.learnMode" placeholder="选择学习形式" style="width:100%">
                <el-option label="自主学习" value="自主" />
                <el-option label="指派学习" value="指派" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结业方式">
              <el-select v-model="formData.finishMode" placeholder="选择结业方式" style="width:100%">
                <el-option label="学习完成即结业" value="学习完成即结业" />
                <el-option label="作业合格结业" value="作业合格结业" />
                <el-option label="考试及格结业" value="考试及格结业" />
                <el-option label="综合条件结业" value="综合条件结业" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="课程简介">
          <el-input type="textarea" v-model="formData.description" :rows="3" placeholder="请输入课程简介" />
        </el-form-item>
      </el-form>
        </el-tab-pane>
        <!-- EL-CO02 学习过程管控（规格书字段，待后端扩展） -->
        <el-tab-pane label="学习管控" name="control">
          <el-form :model="formData" label-width="180px">
            <el-form-item label="禁止拖拽进度">
              <el-switch v-model="formData.noDrag" />
            </el-form-item>
            <el-form-item label="强制100%观看">
              <el-switch v-model="formData.forceFullWatch" />
            </el-form-item>
            <el-form-item label="允许倍速">
              <el-switch v-model="formData.allowSpeed" />
            </el-form-item>
            <el-form-item label="倍速最大限制">
              <el-select v-model="formData.maxSpeed" style="width:200px">
                <el-option label="1.0x" :value="1" /><el-option label="1.5x" :value="1.5" /><el-option label="2.0x" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="单次最短学习时长(分钟)">
              <el-input-number v-model="formData.minSessionMinutes" :min="1" :max="120" style="width:200px" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <!-- EL-CO03 结业考核规则（规格书字段，待后端扩展） -->
        <el-tab-pane label="结业考核规则" name="finish">
          <el-form :model="formData" label-width="180px">
            <el-form-item label="课件必阅"><el-switch v-model="formData.mustReadCourseware" /></el-form-item>
            <el-form-item label="作业必做"><el-switch v-model="formData.mustHomework" /></el-form-item>
            <el-form-item label="考试必考"><el-switch v-model="formData.mustExam" /></el-form-item>
            <el-form-item label="满意度必评"><el-switch v-model="formData.mustEvaluate" /></el-form-item>
            <el-form-item label="考试及格线">
              <el-input-number v-model="formData.examPassScore" :min="0" :max="100" style="width:200px" />
            </el-form-item>
            <el-form-item label="最大补考次数">
              <el-input-number v-model="formData.maxRetake" :min="0" :max="5" style="width:200px" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <!-- EL-CO04 学分发放规则（规格书字段，待后端扩展） -->
        <el-tab-pane label="学分规则" name="credit">
          <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px"
            title="学分算法（华为标准）：最终学分 = 标准学分 × 视频完成率系数 × 作业合格系数 × 考试得分系数 × 评估完成系数 × 时效系数（按时1.0/逾期0.5/超时未完成0）" />
          <el-form :model="formData" label-width="180px">
            <el-form-item label="观看完成系数">
              <el-input-number v-model="formData.watchCoef" :min="0" :max="1" :step="0.1" :precision="1" style="width:200px" />
            </el-form-item>
            <el-form-item label="作业系数">
              <el-input-number v-model="formData.homeworkCoef" :min="0" :max="1" :step="0.1" :precision="1" style="width:200px" />
            </el-form-item>
            <el-form-item label="考试得分系数">
              <el-input-number v-model="formData.examCoef" :min="0" :max="1" :step="0.1" :precision="1" style="width:200px" />
            </el-form-item>
            <el-form-item label="逾期扣分">
              <el-input-number v-model="formData.overdueDeduct" :min="0" :max="20" :step="0.5" style="width:200px" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
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

const categoryOptions = [
  { label: '专业技能', value: 'professional' },
  { label: '管理类', value: 'management' },
  { label: '合规类', value: 'compliance' },
  { label: '安全类', value: 'safety' },
  { label: '通用素养', value: 'general' }
]

const searchForm = reactive({ name: '', category: '', required: '', status: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('新建课程')
const activeTab = ref('base')
const formRef = ref(null)

const defaultExt = () => ({
  // 规格书字段，待后端扩展
  learnMode: '自主', finishMode: '', examStatus: '',
  noDrag: true, forceFullWatch: true, allowSpeed: false, maxSpeed: 1.5, minSessionMinutes: 10,
  mustReadCourseware: true, mustHomework: true, mustExam: true, mustEvaluate: true,
  examPassScore: 60, maxRetake: 2,
  watchCoef: 1.0, homeworkCoef: 1.0, examCoef: 1.0, overdueDeduct: 0
})

const formData = reactive({
  id: '', courseName: '', category: '', duration: 1, credit: 1,
  level: 'beginner', required: 'elective', teacher: '', onlineDate: '',
  description: '', status: 'DRAFT', enrollCount: 0,
  ...defaultExt()
})

const courseList = ref([])

function getCategoryLabel(value) { const item = categoryOptions.find(c => c.value === value); return item ? item.label : value }
function getCategoryType(value) { const types = { professional: '', management: 'warning', compliance: 'danger', safety: 'success', general: 'info' }; return types[value] || 'info' }
function getLevelLabel(value) { const labels = { beginner: '入门', intermediate: '中级', advanced: '高级' }; return labels[value] || value }
function getLevelType(value) { const types = { beginner: 'success', intermediate: 'warning', advanced: 'danger' }; return types[value] || 'info' }
function getStatusLabel(value) { const labels = { published: '已发布', PUBLISHED: '已发布', draft: '草稿', DRAFT: '草稿', offline: '已下线', OFFLINE: '已下线' }; return labels[value] || value }
function getStatusType(value) { const types = { published: 'success', PUBLISHED: 'success', draft: 'warning', DRAFT: 'warning', offline: 'info', OFFLINE: 'info' }; return types[value] || 'info' }

async function loadCourses() {
  try {
    const { data } = await request.get('/api/learn/courses/list', {
      params: { pageNum: pagination.currentPage, pageSize: pagination.pageSize }
    })
    const records = data?.records || data?.data || []
    courseList.value = records.map(r => ({
      id: r.id, name: r.courseName || r.name, category: r.category,
      duration: r.duration, credit: r.credit, level: r.level,
      required: r.required, teacher: r.teacher,
      onlineDate: r.onlineDate ? String(r.onlineDate).slice(0, 10) : '',
      enrollCount: r.enrollCount || 0, status: r.status || 'DRAFT', description: r.description,
      version: r.version || 1,
      // 规格书字段，待后端扩展
      learnMode: r.learnMode, finishMode: r.finishMode, examStatus: r.examStatus
    }))
    pagination.total = data?.total || courseList.value.length
  } catch (e) {
    ElMessage.error('课程列表加载失败：' + (e.message || ''))
  }
}

function doSearch() { pagination.currentPage = 1; loadCourses() }
function resetSearch() { searchForm.name = ''; searchForm.category = ''; searchForm.required = ''; searchForm.status = ''; loadCourses() }
function refreshData() { loadCourses(); ElMessage.success('数据已刷新') }

function openCreate() {
  dialogTitle.value = '新建课程'
  activeTab.value = 'base'
  Object.assign(formData, {
    id: '', courseName: '', category: '', duration: 1, credit: 1,
    level: 'beginner', required: 'elective', teacher: '', onlineDate: '',
    description: '', status: 'DRAFT', enrollCount: 0,
    ...defaultExt()
  })
  dialogVisible.value = true
}

function viewDetail(row) { ElMessage.info(`查看课程详情: ${row.name}`) }

function editItem(row) {
  dialogTitle.value = '编辑课程'
  activeTab.value = 'base'
  Object.assign(formData, defaultExt(), {
    id: row.id, courseName: row.name, category: row.category, duration: row.duration,
    credit: row.credit, level: row.level, required: row.required, teacher: row.teacher,
    onlineDate: row.onlineDate, description: row.description, status: row.status, enrollCount: row.enrollCount,
    learnMode: row.learnMode || '自主', finishMode: row.finishMode || '', examStatus: row.examStatus || ''
  })
  dialogVisible.value = true
}

function startLearn(row) { ElMessage.success(`开始学习课程: ${row.name}（前端禁止拖拽快进，超限倍速不计入有效时长）`) }

// 课程发布（必修课程需先配置结业/考试规则，否则拦截；走通用 PUT 端点置 PUBLISHED）
async function publishCourse(row) {
  if (row.required === 'mandatory' && !row.finishMode) {
    ElMessageBox.alert('必修课程未配置结业规则，禁止发布。请先在「结业考核规则」页签完成配置。', '发布拦截', { type: 'warning' })
    return
  }
  try {
    await request.put('/api/learn/courses', { id: row.id, courseName: row.name, status: 'PUBLISHED' })
    ElMessage.success('课程已发布（已生成版本快照，历史学习数据不失效）')
    loadCourses()
  } catch (e) {
    ElMessage.error('发布失败')
  }
}

// 课程下线（走通用 PUT 端点置 OFFLINE）
async function offlineCourse(row) {
  ElMessageBox.confirm(`课程下线后历史学习数据保留归档。确定要下线课程"${row.name}"吗？`, '下线确认', { type: 'warning' })
    .then(async () => {
      try {
        await request.put('/api/learn/courses', { id: row.id, courseName: row.name, status: 'OFFLINE' })
        ElMessage.success('课程已下线')
        loadCourses()
      } catch (e) { ElMessage.error('下线失败') }
    })
    .catch(() => {})
}

async function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除课程"${row.name}"吗？`, '删除确认', { type: 'warning' })
    .then(async () => { try { await request.delete(`/api/learn/courses/${row.id}`); ElMessage.success('课程已删除'); loadCourses() } catch(e){ ElMessage.error('删除失败') } })
    .catch(() => {})
}

async function saveData() {
  if (!formData.courseName) { ElMessage.warning('请填写课程名称'); return }
  try {
    if (formData.id) {
      await request.put('/api/learn/courses', formData)
    } else {
      await request.post('/api/learn/courses', formData)
    }
    dialogVisible.value = false
    ElMessage.success(formData.id ? '课程已更新' : '课程已创建')
    loadCourses()
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => loadCourses())
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; gap: 8px; }
.search-form { margin-bottom: 0; }
</style>

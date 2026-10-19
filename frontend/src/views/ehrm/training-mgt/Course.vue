<template>
  <div class="page-card">
    <div class="page-header">
      <h2>培训课程</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新建课程
        </el-button>
      </div>
    </div>

    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="课程名称">
          <el-input v-model="searchForm.name" placeholder="请输入课程名称" style="width:200px" />
        </el-form-item>
        <el-form-item label="课程类型">
          <el-select v-model="searchForm.type" placeholder="选择类型" style="width:150px">
            <el-option label="必修课程" value="mandatory" />
            <el-option label="选修课程" value="elective" />
            <el-option label="认证课程" value="certification" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="courseList" border stripe size="small" v-loading="loading">
        <el-table-column prop="courseNo" label="课程编号" width="140" />
        <el-table-column prop="name" label="课程名称" min-width="200">
          <template #default="{row}">
            <el-link type="primary" @click="viewDetail(row)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="课程类型" width="120">
          <template #default="{row}">
            <el-tag size="small">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="课程时长" width="90">
          <template #default="{row}">{{ row.duration }}课时</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="applicableSequence" label="适用序列" width="110">
          <template #default="{row}">{{ row.applicableSequence || '—' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="standardCredit" label="标准学分" width="90" align="center">
          <template #default="{row}">{{ row.standardCredit ?? '—' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="assessmentMethod" label="考核方式" width="100">
          <template #default="{row}">{{ row.assessmentMethod || '—' }}</template>
        </el-table-column>
        <el-table-column prop="level" label="难度等级" width="100">
          <template #default="{row}">
            <el-tag :type="getLevelType(row.level)" size="small">{{ getLevelLabel(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="课程状态" width="100">
          <template #default="{row}">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本号" width="80" align="center">
          <template #default="{row}">V{{ row.version || 1 }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">编辑</el-button>
            <el-button link size="small" type="success" v-if="row.status === '待发布'" @click="publishCourse(row)">发布</el-button>
            <el-button link size="small" type="warning" v-if="row.status === '已发布'" @click="offlineCourse(row)">下线</el-button>
            <el-button link size="small" type="danger" @click="deleteCourse(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程名称" required>
              <el-input v-model="formData.name" placeholder="请输入课程名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程类型" required>
              <el-select v-model="formData.type" placeholder="选择类型">
                <el-option label="必修课程" value="mandatory" />
                <el-option label="选修课程" value="elective" />
                <el-option label="认证课程" value="certification" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课时" required>
              <el-input-number v-model="formData.duration" :min="1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度等级" required>
              <el-select v-model="formData.level" placeholder="选择等级">
                <el-option label="初级" value="beginner" />
                <el-option label="中级" value="intermediate" />
                <el-option label="高级" value="advanced" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="适用序列">
              <el-input v-model="formData.applicableSequence" placeholder="如：技术序列/管理序列" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标准学分">
              <el-input-number v-model="formData.standardCredit" :min="0" :step="0.5" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="考核方式">
              <el-select v-model="formData.assessmentMethod" placeholder="选择考核方式" style="width:100%">
                <el-option label="在线考试" value="在线考试" />
                <el-option label="课后作业" value="课后作业" />
                <el-option label="实操考核" value="实操考核" />
                <el-option label="综合评定" value="综合评定" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="及格线">
              <el-input-number v-model="formData.passScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- EL-CU03 结业规则（规格书字段，待后端扩展）：考勤占比/作业占比/考试占比/补考次数 -->
        <el-form-item label="课程简介">
          <el-input type="textarea" v-model="formData.description" :rows="3" placeholder="请输入课程简介" />
        </el-form-item>
        <el-form-item label="课程大纲">
          <el-input type="textarea" v-model="formData.outline" :rows="4" placeholder="请输入课程大纲" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const searchForm = reactive({
  name: '',
  type: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('新建培训课程')

const formData = reactive({
  id: null,
  courseNo: '',
  name: '',
  type: '',
  duration: 1,
  level: 'beginner',
  description: '',
  outline: '',
  status: '待发布',
  // 规格书字段，待后端扩展
  applicableSequence: '',
  standardCredit: null,
  assessmentMethod: '',
  passScore: 60
})

const courseList = ref([])

function getTypeLabel(type) {
  const labels = { 'mandatory': '必修课程', 'elective': '选修课程', 'certification': '认证课程' }
  return labels[type] || type
}

function getLevelLabel(level) {
  const labels = { 'beginner': '初级', 'intermediate': '中级', 'advanced': '高级' }
  return labels[level] || level
}

function getLevelType(level) {
  const types = { 'beginner': 'success', 'intermediate': 'warning', 'advanced': 'danger' }
  return types[level] || 'info'
}

function getStatusType(status) {
  const types = { '待发布': 'warning', '已发布': 'success', '已下线': 'info' }
  return types[status] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const params = {}
    if (searchForm.type) params.courseType = searchForm.type
    const res = await axios.get('/api/training/course', { params })
    if (res.data.code === 200) {
      courseList.value = res.data.data || []
    }
  } catch (e) {
    ElMessage.error('加载培训课程失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadData()
}

function resetSearch() {
  searchForm.name = ''
  searchForm.type = ''
  loadData()
}

function openAddDialog() {
  dialogTitle.value = '新建培训课程'
  Object.assign(formData, {
    id: null,
    courseNo: 'KC-' + new Date().getFullYear() + '-' + String(courseList.value.length + 1).padStart(4, '0'),
    name: '',
    type: '',
    duration: 1,
    level: 'beginner',
    description: '',
    outline: '',
    status: '待发布',
    applicableSequence: '',
    standardCredit: null,
    assessmentMethod: '',
    passScore: 60
  })
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/training/course/${row.id}`)
    if (res.data.code === 200) {
      Object.assign(formData, res.data.data)
      dialogTitle.value = '编辑培训课程'
      dialogVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取培训课程详情失败')
    console.error(e)
  }
}

function publishCourse(row) {
  ElMessageBox.confirm(`确定要发布课程"${row.name}"吗？`, '发布课程', { type: 'warning' })
    .then(async () => {
      try {
        const res = await axios.post(`/api/training/course/${row.id}/publish`)
        if (res.data.code === 200) {
          ElMessage.success('课程已发布')
          loadData()
        } else {
          ElMessage.error(res.data.message || '发布失败')
        }
      } catch (e) {
        ElMessage.error('发布失败')
        console.error(e)
      }
    })
    .catch(() => {})
}

function offlineCourse(row) {
  ElMessageBox.confirm(`课程下架后历史数据保留归档，不影响已参训人员档案。确定要将课程"${row.name}"下线吗？`, '下线确认', { type: 'warning' })
    .then(async () => {
      try {
        const res = await axios.post(`/api/training/course/${row.id}/offline`)
        if (res.data.code === 200) {
          ElMessage.success('课程已下线')
          loadData()
        } else {
          ElMessage.error(res.data.message || '下线失败')
        }
      } catch (e) {
        ElMessage.error('下线失败')
        console.error(e)
      }
    })
    .catch(() => {})
}

// 删除课程（走后端 DELETE 端点）
function deleteCourse(row) {
  ElMessageBox.confirm(`确定要删除课程"${row.name}"吗？`, '删除确认', { type: 'warning', confirmButtonClass: 'el-button--danger' })
    .then(async () => {
      try {
        const res = await axios.delete(`/api/training/course/${row.id}`)
        if (res.data.code === 200) {
          ElMessage.success('课程已删除')
          loadData()
        } else {
          ElMessage.error(res.data.message || '删除失败')
        }
      } catch (e) {
        ElMessage.error('删除失败')
        console.error(e)
      }
    })
    .catch(() => {})
}

async function handleSubmit() {
  try {
    let res
    if (formData.id) {
      res = await axios.put('/api/training/course', formData)
    } else {
      res = await axios.post('/api/training/course', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success('培训课程已保存')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
    console.error(e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.search-form { margin-bottom: 0; }
</style>
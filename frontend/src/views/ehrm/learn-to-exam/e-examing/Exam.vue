<template>
  <div class="page-card">
    <div class="page-header">
      <h2>考试管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>新建考试
        </el-button>
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px">
      在线考试管理平台，支持入职考试、晋升考试、资质认证、年度考核等多种考试类型，统一管理考试时长、总分、及格分与考试时间。
    </el-alert>

    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="考试名称">
          <el-input v-model="searchForm.name" placeholder="请输入考试名称" style="width:200px" clearable />
        </el-form-item>
        <el-form-item label="考试类型">
          <el-select v-model="searchForm.type" placeholder="选择类型" style="width:160px" clearable>
            <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" style="width:140px" clearable>
            <el-option label="未开始" value="pending" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已结束" value="finished" />
            <el-option label="已归档" value="archived" />
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
      <el-table :data="examList" border stripe size="small">
        <el-table-column prop="id" label="考试ID" width="110" />
        <el-table-column prop="name" label="考试名称" min-width="200">
          <template #default="{row}">
            <el-link type="primary" @click="viewDetail(row)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="考试类型" width="110">
          <template #default="{row}">
            <el-tag :type="getTypeTagType(row.type)" size="small">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="考试时长" width="100" align="center">
          <template #default="{row}">{{ row.duration }} 分钟</template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="80" align="center" />
        <el-table-column prop="passScore" label="及格分" width="90" align="center" />
        <el-table-column prop="examTime" label="考试时间" width="160" />
        <el-table-column prop="examineeCount" label="参考人数" width="90" align="center" />
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="passedCount" label="及格人数" width="90" align="center">
          <template #default="{row}">{{ row.passedCount ?? '-' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="avgScore" label="平均分" width="90" align="center">
          <template #default="{row}">{{ row.avgScore ?? '-' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column label="通过率" width="140" align="center">
          <template #default="{row}">
            <el-progress v-if="row.passRate != null" :percentage="Number(row.passRate)" :stroke-width="10" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展：风控异常数（切屏/秒答/雷同等防作弊命中） -->
        <el-table-column prop="riskCount" label="风控异常" width="90" align="center">
          <template #default="{row}">
            <el-tag v-if="row.riskCount > 0" type="danger" size="small">{{ row.riskCount }}</el-tag>
            <span v-else>0</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button v-if="isPending(row.status)" link size="small" type="success" @click="beginExam(row)">开始考试</el-button>
            <el-button v-if="isOngoing(row.status)" link size="small" type="warning" @click="finishExam(row)">结束考试</el-button>
            <el-button link size="small" type="success" @click="startExam(row)">进入考试</el-button>
            <el-button link size="small" @click="editItem(row)">编辑</el-button>
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
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="考试名称" required>
              <el-input v-model="formData.examName" placeholder="请输入考试名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考试类型" required>
              <el-select v-model="formData.type" placeholder="选择类型" style="width:100%">
                <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="考试时长" required>
              <el-input-number v-model="formData.duration" :min="10" :max="300" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总分" required>
              <el-input-number v-model="formData.totalScore" :min="1" :max="200" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="及格分" required>
              <el-input-number v-model="formData.passScore" :min="1" :max="200" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考试时间" required>
              <el-date-picker v-model="formData.examTime" type="datetime" placeholder="选择考试时间" style="width:100%" value-format="YYYY-MM-DD HH:mm" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="考试形式">
              <el-select v-model="formData.mode" placeholder="选择考试形式" style="width:100%">
                <el-option label="在线闭卷" value="closed_online" />
                <el-option label="在线开卷" value="open_online" />
                <el-option label="线下笔试" value="offline" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否限时">
              <el-switch v-model="formData.timeLimited" active-text="限时" inactive-text="不限时" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="考试说明">
          <el-input type="textarea" v-model="formData.description" :rows="3" placeholder="请输入考试说明与注意事项" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展：EL-EX 防作弊风控配置 -->
        <el-divider content-position="left">防作弊风控配置（规格书字段，待后端扩展）</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="双随机抽题">
              <el-switch v-model="formData.randomPaper" active-text="启用" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="切屏上限">
              <el-input-number v-model="formData.maxSwitchCount" :min="0" :max="10" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="秒答阈值">
              <el-input-number v-model="formData.minAnswerSeconds" :min="1" :max="60" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="AI雷同检测">
              <el-switch v-model="formData.aiSimilarityCheck" active-text="启用" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="规律作答识别">
              <el-switch v-model="formData.patternCheck" active-text="启用" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="强制全屏">
              <el-switch v-model="formData.forceFullScreen" active-text="启用" />
            </el-form-item>
          </el-col>
        </el-row>
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

const typeOptions = [
  { label: '入职考试', value: 'onboarding' },
  { label: '晋升考试', value: 'promotion' },
  { label: '资质认证', value: 'qualification' },
  { label: '年度考核', value: 'annual' },
  { label: '专项测试', value: 'special' }
]

const searchForm = reactive({ name: '', type: '', status: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('新建考试')
const formRef = ref(null)

const formData = reactive({
  id: '', examName: '', type: '', duration: 60, totalScore: 100,
  passScore: 60, examTime: '', mode: 'closed_online', timeLimited: true,
  description: '', status: 'PENDING', examineeCount: 0,
  // 规格书字段，待后端扩展：EL-EX 防作弊风控配置
  randomPaper: true, maxSwitchCount: 3, minAnswerSeconds: 5,
  aiSimilarityCheck: true, patternCheck: true, forceFullScreen: true
})

function extDefaults() {
  return { randomPaper: true, maxSwitchCount: 3, minAnswerSeconds: 5, aiSimilarityCheck: true, patternCheck: true, forceFullScreen: true }
}

const examList = ref([])

function getTypeLabel(value) { const item = typeOptions.find(t => t.value === value); return item ? item.label : value }
function getTypeTagType(value) { const types = { onboarding: 'success', promotion: 'warning', qualification: 'danger', annual: 'primary', special: 'info' }; return types[value] || 'info' }
function getStatusLabel(value) { const labels = { pending: '未开始', PENDING: '未开始', ongoing: '进行中', ONGOING: '进行中', finished: '已结束', FINISHED: '已结束', archived: '已归档', ARCHIVED: '已归档' }; return labels[value] || value }
function getStatusType(value) { const types = { pending: 'info', PENDING: 'info', ongoing: 'success', ONGOING: 'success', finished: 'warning', FINISHED: 'warning', archived: 'info', ARCHIVED: 'info' }; return types[value] || 'info' }
function isPending(s) { return s === 'pending' || s === 'PENDING' }
function isOngoing(s) { return s === 'ongoing' || s === 'ONGOING' }

// 北森防作弊算法说明（双随机/秒答无效/切屏风控/AI雷同/规律作答）
const ANTI_CHEAT_RULES = [
  '① 双随机抽题：试题与选项顺序双重随机，千人千卷；',
  '② 秒答无效：单题作答时长低于阈值（如＜5秒）判定无效作答并记录；',
  '③ 切屏风控：考试中切屏/失焦超过次数上限将强制交卷并记入风控日志；',
  '④ AI雷同检测：客观题作答序列与主观题文本进行雷同度比对，超标判异常；',
  '⑤ 规律作答识别：整卷规律性作答（如全选同一项）自动标记风控异常。'
]

async function loadExams() {
  try {
    const { data } = await request.get('/api/learn/exams/list', {
      params: { pageNum: pagination.currentPage, pageSize: pagination.pageSize }
    })
    const records = data?.records || data?.data || []
    examList.value = records.map(r => ({
      id: r.id, name: r.examName || r.name, type: r.type, duration: r.duration,
      totalScore: r.totalScore, passScore: r.passScore, examTime: r.examTime || '',
      examineeCount: r.examineeCount || 0, status: r.status || 'PENDING',
      mode: r.mode, timeLimited: r.timeLimited, description: r.description,
      // 规格书字段，待后端扩展
      passedCount: r.passedCount, avgScore: r.avgScore, passRate: r.passRate, riskCount: r.riskCount || 0
    }))
    pagination.total = data?.total || examList.value.length
  } catch (e) {
    ElMessage.error('考试列表加载失败：' + (e.message || ''))
  }
}

function doSearch() { pagination.currentPage = 1; loadExams() }
function resetSearch() { searchForm.name = ''; searchForm.type = ''; searchForm.status = ''; loadExams() }
function refreshData() { loadExams(); ElMessage.success('数据已刷新') }

function openCreate() {
  dialogTitle.value = '新建考试'
  Object.assign(formData, {
    id: '', examName: '', type: '', duration: 60, totalScore: 100, passScore: 60,
    examTime: '', mode: 'closed_online', timeLimited: true, description: '', status: 'PENDING', examineeCount: 0,
    ...extDefaults()
  })
  dialogVisible.value = true
}

function viewDetail(row) { ElMessage.info(`查看考试详情: ${row.name}`) }

function editItem(row) {
  dialogTitle.value = '编辑考试'
  Object.assign(formData, {
    id: row.id, examName: row.name, type: row.type, duration: row.duration, totalScore: row.totalScore,
    passScore: row.passScore, examTime: row.examTime, mode: row.mode, timeLimited: row.timeLimited,
    description: row.description, status: row.status, examineeCount: row.examineeCount,
    // 规格书字段，待后端扩展：编辑时回填防作弊配置，无值则取默认
    ...extDefaults(),
    randomPaper: row.randomPaper ?? true, maxSwitchCount: row.maxSwitchCount ?? 3,
    minAnswerSeconds: row.minAnswerSeconds ?? 5, aiSimilarityCheck: row.aiSimilarityCheck ?? true,
    patternCheck: row.patternCheck ?? true, forceFullScreen: row.forceFullScreen ?? true
  })
  dialogVisible.value = true
}

function startExam(row) {
  if (!isOngoing(row.status)) { ElMessage.warning('当前考试不在进行中，无法进入'); return }
  // 进入考试前展示全屏防作弊风控规则（双随机/秒答无效/切屏风控/AI雷同/规律作答）
  ElMessageBox.alert(ANTI_CHEAT_RULES.join('\n'), '考试风控规则须知', {
    confirmButtonText: '我已知晓，进入考试',
    customStyle: { whiteSpace: 'pre-line' },
    type: 'warning'
  }).then(() => ElMessage.success(`已进入考试: ${row.name}`)).catch(() => {})
}

// 开始考试：PENDING -> ONGOING
function beginExam(row) {
  ElMessageBox.confirm(`确定开始考试"${row.name}"吗？开始后考生可进入作答。`, '开始考试', { type: 'warning' })
    .then(async () => {
      try {
        await request.put('/api/learn/exams', { id: row.id, status: 'ONGOING' })
        ElMessage.success('考试已开始')
        loadExams()
      } catch (e) { ElMessage.error('操作失败：' + (e.response?.data?.message || e.message || '')) }
    }).catch(() => {})
}

// 结束考试：ONGOING -> FINISHED
function finishExam(row) {
  ElMessageBox.confirm(`确定结束考试"${row.name}"吗？结束后将自动交卷并进入阅卷环节，未交卷考生按已答内容计分。`, '结束考试', { type: 'warning', confirmButtonText: '确认结束' })
    .then(async () => {
      try {
        await request.put('/api/learn/exams', { id: row.id, status: 'FINISHED' })
        ElMessage.success('考试已结束')
        loadExams()
      } catch (e) { ElMessage.error('操作失败：' + (e.response?.data?.message || e.message || '')) }
    }).catch(() => {})
}

function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除考试"${row.name}"吗？`, '删除确认', { type: 'warning' })
    .then(async () => { try { await request.delete(`/api/learn/exams/${row.id}`); ElMessage.success('考试已删除'); loadExams() } catch(e){ ElMessage.error('删除失败') } })
    .catch(() => {})
}

async function saveData() {
  if (!formData.examName) { ElMessage.warning('请填写考试名称'); return }
  if (formData.passScore > formData.totalScore) { ElMessage.warning('及格分不能大于总分'); return }
  try {
    if (formData.id) {
      await request.put('/api/learn/exams', formData)
    } else {
      await request.post('/api/learn/exams', formData)
    }
    dialogVisible.value = false
    ElMessage.success(formData.id ? '考试已更新' : '考试已创建')
    loadExams()
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => loadExams())
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; gap: 8px; }
.search-form { margin-bottom: 0; }
</style>

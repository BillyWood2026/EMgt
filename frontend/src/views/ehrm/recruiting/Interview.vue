<template>
  <div class="page-card">
    <div class="page-header">
      <h2>面试管理（STAR标准评审）</h2>
      <div class="header-actions">
        <el-select v-model="hrmAuth.role" size="small" style="width:170px" @change="setHrmRole">
          <el-option v-for="r in HRM_ROLES" :key="r.value" :label="r.label" :value="r.value" />
        </el-select>
        <el-button v-if="can('HR_ADMIN','HRBP')" type="primary" @click="openScheduleDialog">
          <el-icon><Plus /></el-icon>安排面试
        </el-button>
        <ImportExportBar
          v-if="can('HR_ADMIN','HRBP','HR_DIRECTOR')"
          :columns="exportCols" :rows="interviewList" file-name="面试评审台账" title="面试管理" />
      </div>
    </div>

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon"><el-icon><Clock /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ todayCount }}</div>
            <div class="stat-label">今日面试</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-icon"><el-icon><User /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ pendingCount }}</div>
            <div class="stat-label">待面试</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ passedCount }}</div>
            <div class="stat-label">通过</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon"><el-icon><Close /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ rejectedCount }}</div>
            <div class="stat-label">未通过</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-content">
            <div class="stat-value">{{ passRate }}%</div>
            <div class="stat-label">面试通过率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-content">
            <div class="stat-value">{{ secondRoundCount }}</div>
            <div class="stat-label">复试场次</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true" class="search-form">
        <el-form-item label="候选人">
          <el-input v-model="searchForm.candidateName" placeholder="候选人姓名" style="width:150px" />
        </el-form-item>
        <el-form-item label="面试轮次">
          <el-select v-model="searchForm.stage" placeholder="轮次" style="width:110px" clearable>
            <el-option label="初试" value="初试" />
            <el-option label="复试" value="复试" />
          </el-select>
        </el-form-item>
        <el-form-item label="面试结果">
          <el-select v-model="searchForm.result" placeholder="结果" style="width:110px" clearable>
            <el-option label="通过" value="通过" />
            <el-option label="备选" value="备选" />
            <el-option label="未通过" value="未通过" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="interviewList" border stripe size="small" v-loading="loading">
        <el-table-column prop="interviewNo" label="面试单号" width="140" />
        <el-table-column prop="candidateName" label="候选人" width="90" />
        <el-table-column prop="position" label="面试岗位" width="140" />
        <el-table-column prop="interviewer" label="评委" width="90" />
        <el-table-column prop="interviewDate" label="面试时间" width="150">
          <template #default="{row}">{{ row.interviewDate }} {{ row.interviewTime }}</template>
        </el-table-column>
        <el-table-column prop="stage" label="面试轮次" width="90" align="center">
          <template #default="{row}">
            <el-tag :type="row.stage === '初试' ? 'primary' : 'warning'" size="small">{{ row.stage }}</el-tag>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展：面试类型(线上/线下)、价值观/专业能力/综合素质/潜力四维得分、综合评分 -->
        <el-table-column prop="location" label="地点/链接" min-width="110" show-overflow-tooltip />
        <el-table-column prop="result" label="面试结论" width="90" align="center">
          <template #default="{row}">
            <el-tag v-if="row.result === '通过'" type="success" size="small">通过</el-tag>
            <el-tag v-else-if="row.result === '备选'" type="warning" size="small">备选</el-tag>
            <el-tag v-else-if="row.result === '未通过'" type="danger" size="small">未通过</el-tag>
            <el-tag v-else type="info" size="small">待面试</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="can('HR_ADMIN','HRBP') && !row.result" link size="small" type="success" @click="recordResult(row)">录入结果</el-button>
            <el-button v-if="can('HR_ADMIN','HRBP') && row.result === '备选'" link size="small" type="warning" @click="rebook(row)">二次预约</el-button>
            <el-button v-if="can('HR_ADMIN')" link size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 安排面试弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="formData" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="候选人" required>
              <el-select v-model="formData.candidateName" placeholder="选择候选人" filterable @change="onCandidateChange">
                <el-option v-for="c in candidates" :key="c.id" :label="c.name" :value="c.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="应聘岗位" required>
              <el-input v-model="formData.position" placeholder="选择候选人自动带入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="面试轮次" required>
              <el-radio-group v-model="formData.stage">
                <el-radio label="初试" />
                <el-radio label="复试" />
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评委" required>
              <el-input v-model="formData.interviewer" placeholder="部门负责人/直属上级/技术负责人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="面试日期" required>
              <el-date-picker v-model="formData.interviewDate" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面试时间" required>
              <el-time-picker v-model="formData.interviewTime" format="HH:mm" placeholder="选择时间" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="面试类型">
              <el-radio-group v-model="formData.interviewType">
                <el-radio label="线下" />
                <el-radio label="线上" />
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="地点/会议链接">
          <el-input v-model="formData.location" placeholder="线下地点或线上会议链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认安排</el-button>
      </template>
    </el-dialog>

    <!-- 录入面试结果弹窗（STAR四维评分卡） -->
    <el-dialog v-model="resultDialogVisible" title="录入面试结果（STAR评分卡）" width="620px">
      <el-form :model="resultForm" label-width="110px">
        <el-form-item label="候选人">
          <span>{{ resultForm.candidateName }}（{{ resultForm.stage }}）</span>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="价值观得分">
              <el-input-number v-model="resultForm.valuesScore" :min="0" :max="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="专业能力得分">
              <el-input-number v-model="resultForm.professionScore" :min="0" :max="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="综合素质得分">
              <el-input-number v-model="resultForm.qualityScore" :min="0" :max="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="发展潜力得分">
              <el-input-number v-model="resultForm.potentialScore" :min="0" :max="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="综合评分">
          <el-tag type="primary" size="large">{{ compositeScore }} 分</el-tag>
          <span class="score-hint">四维加权（价值观30% + 专业40% + 素质20% + 潜力10%），价值观&lt;60一票否决</span>
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="STAR场景记录">
          <el-input type="textarea" v-model="resultForm.starRecord" :rows="3"
            placeholder="情景(Situation)/任务(Task)/行动(Action)/结果(Result)，禁止空泛评价" />
        </el-form-item>
        <el-form-item label="面试结论" required>
          <el-radio-group v-model="resultForm.result">
            <el-radio label="通过" :disabled="resultForm.valuesScore < 60">通过</el-radio>
            <el-radio label="备选" />
            <el-radio label="未通过" />
          </el-radio-group>
          <div v-if="resultForm.valuesScore < 60" class="veto-hint">价值观得分低于60分，触发一票否决，不可选「通过」</div>
        </el-form-item>
        <el-form-item label="评审意见">
          <el-input type="textarea" v-model="resultForm.comment" :rows="3" placeholder="专业能力评价、综合素质评价、优缺点总结、风险点" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resultDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitResult">确认</el-button>
      </template>
    </el-dialog>

    <!-- 面试详情弹窗 -->
    <el-dialog v-model="detailVisible" title="面试详情" width="560px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="面试单号">{{ detailRow.interviewNo }}</el-descriptions-item>
        <el-descriptions-item label="候选人">{{ detailRow.candidateName }}</el-descriptions-item>
        <el-descriptions-item label="面试岗位">{{ detailRow.position }}</el-descriptions-item>
        <el-descriptions-item label="评委">{{ detailRow.interviewer }}</el-descriptions-item>
        <el-descriptions-item label="轮次">{{ detailRow.stage }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ detailRow.interviewDate }} {{ detailRow.interviewTime }}</el-descriptions-item>
        <el-descriptions-item label="地点/链接" :span="2">{{ detailRow.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="面试结论">{{ detailRow.result || '待面试' }}</el-descriptions-item>
        <el-descriptions-item label="评审意见" :span="2">{{ detailRow.comment || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Clock, User, CircleCheck, Close } from '@element-plus/icons-vue'
import ImportExportBar from '@/components/ImportExportBar.vue'
import { hrmAuth, setHrmRole, can, HRM_ROLES } from '../hrmAuth.js'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('安排面试')
const resultDialogVisible = ref(false)
const detailVisible = ref(false)
const detailRow = ref({})

const candidates = ref([])
const searchForm = reactive({ candidateName: '', stage: '', result: '' })

const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const formData = reactive({
  id: null, candidateName: '', position: '', stage: '初试', interviewer: '',
  interviewDate: '', interviewTime: '', location: '', result: '',
  interviewType: '线下' // 规格书字段，待后端扩展
})

const resultForm = reactive({
  id: null, candidateName: '', stage: '', result: '通过', comment: '',
  // 规格书字段，待后端扩展：STAR四维评分
  valuesScore: 80, professionScore: 80, qualityScore: 80, potentialScore: 80, starRecord: ''
})

const interviewList = ref([])

const todayCount = computed(() => {
  const today = new Date().toISOString().slice(0, 10)
  return interviewList.value.filter(i => i.interviewDate === today).length
})
const pendingCount = computed(() => interviewList.value.filter(i => !i.result).length)
const passedCount = computed(() => interviewList.value.filter(i => i.result === '通过').length)
const rejectedCount = computed(() => interviewList.value.filter(i => i.result === '未通过').length)
const secondRoundCount = computed(() => interviewList.value.filter(i => i.stage === '复试').length)
// 面试通过率 = 通过 / (通过+未通过+备选)
const passRate = computed(() => {
  const done = interviewList.value.filter(i => i.result).length
  return done ? Math.round(passedCount.value / done * 100) : 0
})
// STAR四维加权综合评分：价值观30% + 专业40% + 素质20% + 潜力10%
const compositeScore = computed(() => Math.round(
  resultForm.valuesScore * 0.3 + resultForm.professionScore * 0.4 + resultForm.qualityScore * 0.2 + resultForm.potentialScore * 0.1
))

const exportCols = [
  { key: 'interviewNo', title: '面试单号' }, { key: 'candidateName', title: '候选人' }, { key: 'position', title: '面试岗位' },
  { key: 'interviewer', title: '评委' }, { key: 'interviewDate', title: '面试日期' }, { key: 'interviewTime', title: '面试时间' },
  { key: 'stage', title: '面试轮次' }, { key: 'result', title: '面试结论' }, { key: 'comment', title: '评审意见' }, { key: 'location', title: '地点' }
]

function extractList(res) {
  const data = res.data
  if (data && typeof data === 'object' && 'code' in data) {
    const payload = data.data
    if (Array.isArray(payload)) return { list: payload, total: payload.length }
    if (payload && Array.isArray(payload.records)) return { list: payload.records, total: payload.total || 0 }
    if (payload && Array.isArray(payload.list)) return { list: payload.list, total: payload.total || 0 }
    return { list: [], total: 0 }
  }
  if (Array.isArray(data)) return { list: data, total: data.length }
  if (data && Array.isArray(data.records)) return { list: data.records, total: data.total || 0 }
  if (data && Array.isArray(data.list)) return { list: data.list, total: data.total || 0 }
  return { list: [], total: 0 }
}

async function loadData() {
  loading.value = true
  try {
    const res = await axios.get('/api/recruitment/interview/list', {
      params: {
        page: pagination.currentPage,
        size: pagination.pageSize,
        candidateName: searchForm.candidateName || undefined,
        stage: searchForm.stage || undefined,
        result: searchForm.result || undefined
      }
    })
    const { list, total } = extractList(res)
    interviewList.value = list
    pagination.total = total
  } catch (e) {
    ElMessage.error('加载面试列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 候选人下拉改为真实数据（候选人管理端点）
async function loadCandidates() {
  try {
    const res = await axios.get('/api/recruitment/rec02/list')
    candidates.value = extractList(res).list.filter(c => c.currentStage !== '已淘汰')
  } catch (e) { /* 候选人加载失败时允许手工输入 */ }
}

function onCandidateChange(name) {
  const c = candidates.value.find(x => x.name === name)
  if (c) formData.position = c.position
}

function resetSearch() {
  searchForm.candidateName = ''; searchForm.stage = ''; searchForm.result = ''
  loadData()
}

function handleSizeChange(size) { pagination.pageSize = size; loadData() }
function handleCurrentChange(page) { pagination.currentPage = page; loadData() }

function openScheduleDialog() {
  dialogTitle.value = '安排面试'
  Object.assign(formData, {
    id: null, candidateName: '', position: '', stage: '初试', interviewer: '',
    interviewDate: '', interviewTime: '', location: '', result: '', interviewType: '线下'
  })
  dialogVisible.value = true
}

function viewDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

function recordResult(row) {
  Object.assign(resultForm, {
    id: row.id, candidateName: row.candidateName, stage: row.stage, result: '通过', comment: row.comment || '',
    valuesScore: 80, professionScore: 80, qualityScore: 80, potentialScore: 80, starRecord: ''
  })
  resultDialogVisible.value = true
}

// 备选候选人二次预约：复制该行生成新面试
function rebook(row) {
  dialogTitle.value = '二次预约面试'
  Object.assign(formData, {
    id: null, candidateName: row.candidateName, position: row.position, stage: row.stage,
    interviewer: row.interviewer, interviewDate: '', interviewTime: '', location: row.location || '', result: '', interviewType: '线下'
  })
  dialogVisible.value = true
}

async function submitResult() {
  if (resultForm.valuesScore < 60 && resultForm.result === '通过') {
    ElMessage.error('价值观得分低于60分，一票否决，不可通过')
    return
  }
  try {
    // 综合评分与STAR记录并入评审意见提交（规格书字段，待后端扩展独立列）
    const payload = {
      id: resultForm.id,
      candidateName: resultForm.candidateName,
      result: resultForm.result,
      comment: `[综合评分${compositeScore.value}分|价值观${resultForm.valuesScore}|专业${resultForm.professionScore}|素质${resultForm.qualityScore}|潜力${resultForm.potentialScore}] `
        + (resultForm.starRecord ? `[STAR]${resultForm.starRecord} ` : '') + (resultForm.comment || '')
    }
    await axios.put(`/api/recruitment/interview/${resultForm.id}`, payload)
    ElMessage.success('面试结果已录入')
    resultDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除面试记录"${row.interviewNo}"吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      try {
        await axios.delete(`/api/recruitment/interview/${row.id}`)
        ElMessage.success('删除成功')
        loadData()
      } catch (e) {
        ElMessage.error('删除失败')
        console.error(e)
      }
    })
    .catch(() => {})
}

async function handleSubmit() {
  try {
    await axios.post('/api/recruitment/interview', formData)
    ElMessage.success('面试已安排')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

onMounted(() => {
  loadData()
  loadCandidates()
})
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; gap: 8px; align-items: center; }
.search-form { margin-bottom: 0; }

.stat-card { display: flex; align-items: center; gap: 16px; padding: 20px; }
.stat-icon { width: 56px; height: 56px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28px; background: #e3f2fd; color: #409eff; }
.stat-card.warning .stat-icon { background: #fef7e8; color: #e6a23c; }
.stat-card.success .stat-icon { background: #e8f5e9; color: #67c23a; }
.stat-value { font-size: 32px; font-weight: bold; color: #303133; }
.stat-label { color: #909399; font-size: 14px; margin-top: 4px; }
.score-hint { margin-left: 10px; color: #909399; font-size: 12px; }
.veto-hint { color: #f56c6c; font-size: 12px; }
</style>

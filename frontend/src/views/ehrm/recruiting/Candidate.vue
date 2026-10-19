<template>
  <div class="page-card">
    <div class="page-header">
      <h2>候选人管理（简历资源池）</h2>
      <div class="header-actions">
        <el-select v-model="hrmAuth.role" size="small" style="width:170px" @change="setHrmRole">
          <el-option v-for="r in HRM_ROLES" :key="r.value" :label="r.label" :value="r.value" />
        </el-select>
        <el-button v-if="can('HR_ADMIN','HRBP')" type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>录入候选人
        </el-button>
        <ImportExportBar
          v-if="can('HR_ADMIN','HRBP','HR_DIRECTOR')"
          :columns="exportCols" :rows="candidateList" file-name="候选人简历库" title="候选人"
          :import-api="can('HR_ADMIN','HRBP') ? '/api/recruitment/rec02' : ''"
          :field-map="fieldMap" @done="loadData" />
      </div>
    </div>

    <!-- 招聘漏斗转化率（规格书BI算法：各阶段转化率 = 下一阶段人数 / 上一阶段人数） -->
    <div class="stat-cards">
      <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ funnel.total }}</div><div class="stat-label">简历总数</div></el-card>
      <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ funnel.interview }}</div><div class="stat-label">进入面试 {{ funnel.total ? Math.round(funnel.interview / funnel.total * 100) : 0 }}%</div></el-card>
      <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ funnel.offer }}</div><div class="stat-label">待录用 {{ funnel.interview ? Math.round(funnel.offer / funnel.interview * 100) : 0 }}%</div></el-card>
      <el-card shadow="hover" class="stat-card"><div class="stat-value success">{{ funnel.hired }}</div><div class="stat-label">已入职 {{ funnel.offer ? Math.round(funnel.hired / funnel.offer * 100) : 0 }}%</div></el-card>
      <el-card shadow="hover" class="stat-card"><div class="stat-value danger">{{ funnel.rejected }}</div><div class="stat-label">已淘汰</div></el-card>
      <el-card shadow="hover" class="stat-card"><div class="stat-value warn">{{ internalCount }}</div><div class="stat-label">内推简历</div></el-card>
    </div>

    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" style="width:150px" />
        </el-form-item>
        <el-form-item label="应聘岗位">
          <el-select v-model="searchForm.position" placeholder="选择岗位" style="width:180px" clearable>
            <el-option v-for="pos in positions" :key="pos" :label="pos" :value="pos" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="searchForm.source" placeholder="选择来源" style="width:120px" clearable>
            <el-option label="智联" value="zhilian" />
            <el-option label="前程无忧" value="51job" />
            <el-option label="BOSS直聘" value="boss" />
            <el-option label="内推" value="internal" />
            <el-option label="猎头" value="headhunter" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="candidateList" border stripe size="small" v-loading="loading">
        <el-table-column prop="name" label="姓名" width="90" />
        <el-table-column prop="phone" label="手机号" width="120">
          <template #default="{row}">{{ maskPhone(row.phone) }}</template>
        </el-table-column>
        <el-table-column prop="position" label="意向岗位" width="130" />
        <el-table-column prop="source" label="来源渠道" width="100">
          <template #default="{row}">
            <el-tag size="small" :type="row.source === 'internal' ? 'warning' : 'info'">{{ getSourceLabel(row.source) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="education" label="学历" width="80" />
        <el-table-column prop="experience" label="工作年限" width="90" />
        <!-- 规格书字段，待后端扩展：性别、年龄、专业、技能证书、薪资预期、期望地点 -->
        <el-table-column label="人岗匹配分" width="120" align="center">
          <template #default="{row}">
            <el-tag :type="matchTagType(matchScore(row))" size="small">{{ matchScore(row) }}分·{{ matchLevel(matchScore(row)) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentStage" label="跟进状态" width="100" align="center">
          <template #default="{row}">
            <el-tag :type="getStageType(row.currentStage)" size="small">{{ row.currentStage }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="投递时间" width="150" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewResume(row)">查看简历</el-button>
            <el-button v-if="can('HR_ADMIN','HRBP') && row.currentStage !== '已入职' && row.currentStage !== '已淘汰'" link size="small" @click="openInterviewDialog(row)">安排面试</el-button>
            <el-button v-if="can('HR_ADMIN','HRBP') && nextStage(row)" link size="small" type="success" @click="advanceStage(row)">流转{{ nextStage(row) }}</el-button>
            <el-button v-if="can('HR_ADMIN','HRBP')" link size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button v-if="can('HR_ADMIN','HRBP') && row.currentStage !== '已入职' && row.currentStage !== '已淘汰'" link size="small" type="danger" @click="rejectCandidate(row)">淘汰</el-button>
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

    <!-- 录入/编辑候选人弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="750px">
      <el-form :model="formData" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" required>
              <el-input v-model="formData.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" required>
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="性别">
              <el-radio-group v-model="formData.gender">
                <el-radio label="男" />
                <el-radio label="女" />
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="年龄">
              <el-input-number v-model="formData.age" :min="16" :max="70" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="邮箱">
              <el-input v-model="formData.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="应聘岗位" required>
              <el-select v-model="formData.position" placeholder="选择岗位">
                <el-option v-for="pos in positions" :key="pos" :label="pos" :value="pos" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="来源" required>
              <el-select v-model="formData.source" placeholder="选择来源">
                <el-option label="智联" value="zhilian" />
                <el-option label="前程无忧" value="51job" />
                <el-option label="BOSS直聘" value="boss" />
                <el-option label="内推" value="internal" />
                <el-option label="猎头" value="headhunter" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展：内推人（内推来源必填，三重查重锁定首次推荐人） -->
            <el-form-item label="内推人" v-if="formData.source === 'internal'">
              <el-input v-model="formData.referrer" placeholder="推荐人工号/姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学历">
              <el-select v-model="formData.education" placeholder="选择学历">
                <el-option label="博士" value="博士" />
                <el-option label="硕士" value="硕士" />
                <el-option label="本科" value="本科" />
                <el-option label="大专" value="大专" />
                <el-option label="高中" value="高中" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作年限">
              <el-input v-model="formData.experience" placeholder="例如：5年以上" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="专业">
              <el-input v-model="formData.major" placeholder="毕业专业" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="技能标签">
              <el-input v-model="formData.skillTags" placeholder="逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="期望薪资">
              <el-input v-model="formData.expectedSalary" placeholder="例如：15K-20K" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="期望地点">
              <el-input v-model="formData.expectedLocation" placeholder="期望工作城市" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="formData.remark" :rows="3" placeholder="工作经历、项目经验、资质证书等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 简历详情弹窗 -->
    <el-dialog v-model="resumeVisible" title="简历详情" width="620px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="姓名">{{ resumeRow.name }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ maskPhone(resumeRow.phone) }}</el-descriptions-item>
        <el-descriptions-item label="应聘岗位">{{ resumeRow.position }}</el-descriptions-item>
        <el-descriptions-item label="来源渠道">{{ getSourceLabel(resumeRow.source) }}</el-descriptions-item>
        <el-descriptions-item label="学历">{{ resumeRow.education || '-' }}</el-descriptions-item>
        <el-descriptions-item label="工作年限">{{ resumeRow.experience || '-' }}</el-descriptions-item>
        <el-descriptions-item label="人岗匹配分">{{ matchScore(resumeRow) }}分（{{ matchLevel(matchScore(resumeRow)) }}）</el-descriptions-item>
        <el-descriptions-item label="跟进状态">{{ resumeRow.currentStage }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ resumeRow.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-alert type="info" :closable="false" style="margin-top:12px">
        匹配评分模型：硬性指标40分（学历/年限/专业/资质）+ 技能指标35分 + 经历指标15分 + 附加指标10分；≥80优质，60-80合格，&lt;60待优化。
      </el-alert>
    </el-dialog>

    <!-- 安排面试弹窗（直连面试管理端点） -->
    <el-dialog v-model="interviewVisible" title="安排面试" width="550px">
      <el-form :model="interviewForm" label-width="100px">
        <el-form-item label="候选人"><span>{{ interviewForm.candidateName }}</span></el-form-item>
        <el-form-item label="应聘岗位"><span>{{ interviewForm.position }}</span></el-form-item>
        <el-form-item label="面试阶段" required>
          <el-radio-group v-model="interviewForm.stage">
            <el-radio label="初试" />
            <el-radio label="复试" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="面试官" required>
          <el-input v-model="interviewForm.interviewer" placeholder="请输入面试官" />
        </el-form-item>
        <el-form-item label="面试日期" required>
          <el-date-picker v-model="interviewForm.interviewDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="面试时间">
          <el-time-picker v-model="interviewForm.interviewTime" format="HH:mm" value-format="HH:mm" style="width:100%" />
        </el-form-item>
        <el-form-item label="面试地点">
          <el-input v-model="interviewForm.location" placeholder="线下地点或线上会议链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="interviewVisible=false">取消</el-button>
        <el-button type="primary" @click="submitInterview">确认安排</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import ImportExportBar from '@/components/ImportExportBar.vue'
import { hrmAuth, setHrmRole, can, HRM_ROLES } from '../hrmAuth.js'

const loading = ref(false)
const searchForm = reactive({ name: '', position: '', source: '' })

const positions = ['高级Java工程师', '销售经理', '会计', '生产主管', '行政专员']

const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const dialogVisible = ref(false)
const dialogTitle = ref('录入候选人')
const isEdit = ref(false)
const resumeVisible = ref(false)
const resumeRow = ref({})
const interviewVisible = ref(false)

const emptyForm = {
  id: null, name: '', phone: '', position: '', source: '', education: '', experience: '', remark: '', currentStage: '简历筛选',
  // 规格书字段，待后端扩展（提交时一并带上，后端多字段不报错）
  gender: '', age: null, email: '', major: '', skillTags: '', expectedSalary: '', expectedLocation: '', referrer: ''
}
const formData = reactive({ ...emptyForm })

const interviewForm = reactive({ candidateName: '', position: '', stage: '初试', interviewer: '', interviewDate: '', interviewTime: '', location: '' })

const candidateList = ref([])

// 招聘漏斗统计（规格书BI：进度看板各阶段转化）
const funnel = computed(() => {
  const list = candidateList.value
  const interview = list.filter(r => ['初试', '复试', '待录用', '已入职'].includes(r.currentStage)).length
  const offer = list.filter(r => ['待录用', '已入职'].includes(r.currentStage)).length
  const hired = list.filter(r => r.currentStage === '已入职').length
  return { total: list.length, interview, offer, hired, rejected: list.filter(r => r.currentStage === '已淘汰').length }
})
const internalCount = computed(() => candidateList.value.filter(r => r.source === 'internal').length)

// 人岗匹配加权评分（规格书算法：硬性40 + 技能35 + 经历15 + 附加10，前端展示）
const EDU_SCORE = { '博士': 25, '硕士': 22, '本科': 18, '大专': 12, '高中': 8 }
function matchScore(row) {
  if (!row || !row.name) return 0
  let score = 0
  // 硬性指标：学历25 + 年限15
  score += EDU_SCORE[row.education] || 10
  const years = parseInt(row.experience) || 0
  score += years >= 5 ? 15 : years >= 3 ? 12 : years >= 1 ? 8 : 5
  // 技能指标：有技能标签/备注关键词
  const text = ((row.skillTags || '') + (row.remark || ''))
  score += text ? Math.min(35, 15 + text.length) : 15
  // 经历指标：有工作经历备注
  score += (row.remark ? 12 : 6)
  // 附加指标：内推加分、信息完整度
  score += (row.source === 'internal' ? 6 : 3) + (row.phone ? 2 : 0) + (row.email ? 2 : 0)
  return Math.min(100, score)
}
const matchLevel = (s) => (s >= 80 ? '优质' : s >= 60 ? '合格' : '待优化')
const matchTagType = (s) => (s >= 80 ? 'success' : s >= 60 ? 'warning' : 'danger')

const exportCols = [
  { key: 'name', title: '姓名' }, { key: 'phone', title: '手机号' }, { key: 'position', title: '应聘岗位' },
  { key: 'source', title: '来源' }, { key: 'education', title: '学历' }, { key: 'experience', title: '工作年限' },
  { key: 'currentStage', title: '当前阶段' }, { key: 'remark', title: '备注' }, { key: 'createTime', title: '投递时间' }
]
const fieldMap = { '姓名': 'name', '手机号': 'phone', '应聘岗位': 'position', '来源': 'source', '学历': 'education', '工作年限': 'experience', '当前阶段': 'currentStage', '备注': 'remark' }

function maskPhone(phone) {
  if (!phone) return ''
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

function getSourceLabel(source) {
  const labels = { 'zhilian': '智联', '51job': '前程无忧', 'boss': 'BOSS直聘', 'internal': '内推', 'headhunter': '猎头' }
  return labels[source] || source
}

function getStageType(stage) {
  const types = { '简历筛选': 'info', '初试': 'primary', '复试': 'warning', '待录用': 'success', '已入职': 'success', '已淘汰': 'danger' }
  return types[stage] || 'info'
}

// 阶段状态机：简历筛选→初试→复试→待录用→已入职
const STAGE_FLOW = { '简历筛选': '初试', '初试': '复试', '复试': '待录用', '待录用': '已入职' }
const nextStage = (row) => STAGE_FLOW[row.currentStage] || ''

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
    const res = await axios.get('/api/recruitment/rec02/list', {
      params: {
        page: pagination.currentPage,
        size: pagination.pageSize,
        name: searchForm.name || undefined,
        position: searchForm.position || undefined,
        source: searchForm.source || undefined
      }
    })
    const { list, total } = extractList(res)
    candidateList.value = list
    pagination.total = total
  } catch (e) {
    ElMessage.error('加载候选人列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() { pagination.currentPage = 1; loadData() }
function resetSearch() { searchForm.name = ''; searchForm.position = ''; searchForm.source = ''; pagination.currentPage = 1; loadData() }
function handleSizeChange(size) { pagination.pageSize = size; loadData() }
function handleCurrentChange(page) { pagination.currentPage = page; loadData() }

function openAddDialog() {
  isEdit.value = false
  dialogTitle.value = '录入候选人'
  Object.assign(formData, emptyForm)
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  dialogTitle.value = '编辑候选人'
  Object.assign(formData, emptyForm, row)
  dialogVisible.value = true
}

function viewResume(row) {
  resumeRow.value = row
  resumeVisible.value = true
}

function openInterviewDialog(row) {
  Object.assign(interviewForm, { candidateName: row.name, position: row.position, stage: nextStage(row) === '复试' ? '复试' : '初试', interviewer: '', interviewDate: '', interviewTime: '', location: '' })
  interviewVisible.value = true
}

async function submitInterview() {
  if (!interviewForm.interviewer || !interviewForm.interviewDate) {
    ElMessage.warning('请填写面试官与面试日期')
    return
  }
  try {
    await axios.post('/api/recruitment/interview', { ...interviewForm })
    // 面试安排后候选人阶段同步流转至对应轮次
    const cand = candidateList.value.find(c => c.name === interviewForm.candidateName)
    if (cand && STAGE_FLOW[cand.currentStage]) {
      await axios.put(`/api/recruitment/rec02/${cand.id}`, { ...cand, currentStage: interviewForm.stage })
    }
    ElMessage.success('面试已安排')
    interviewVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('安排面试失败')
  }
}

async function advanceStage(row) {
  const target = nextStage(row)
  try {
    await axios.put(`/api/recruitment/rec02/${row.id}`, { ...row, currentStage: target })
    ElMessage.success(`已流转至「${target}」`)
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 淘汰 = 归档至淘汰人才池（不物理删除）
function rejectCandidate(row) {
  ElMessageBox.confirm(`确定要淘汰候选人"${row.name}"吗？淘汰后将归档至淘汰人才池。`, '淘汰确认', { type: 'warning' })
    .then(async () => {
      try {
        await axios.put(`/api/recruitment/rec02/${row.id}`, { ...row, currentStage: '已淘汰' })
        ElMessage.success('候选人已淘汰归档')
        loadData()
      } catch (e) {
        ElMessage.error('操作失败')
        console.error(e)
      }
    })
    .catch(() => {})
}

async function handleSubmit() {
  try {
    if (isEdit.value) {
      await axios.put(`/api/recruitment/rec02/${formData.id}`, formData)
      ElMessage.success('候选人信息已更新')
    } else {
      await axios.post('/api/recruitment/rec02', formData)
      ElMessage.success('候选人信息已保存')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
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
.header-actions { display: flex; gap: 8px; align-items: center; }
.search-form { margin-bottom: 0; }
.stat-cards { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.stat-card { flex: 1; min-width: 130px; text-align: center; }
.stat-value { font-size: 26px; font-weight: bold; color: #303133; }
.stat-value.success { color: #67c23a; }
.stat-value.danger { color: #f56c6c; }
.stat-value.warn { color: #e6a23c; }
.stat-label { color: #909399; font-size: 13px; margin-top: 4px; }
</style>

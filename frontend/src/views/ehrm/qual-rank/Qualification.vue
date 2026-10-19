<template>
  <div class="page-card">
    <div class="page-header">
      <h2>等级标准</h2>
      <div class="header-actions">
        <ImportExportBar
          :columns="ioColumns"
          :rows="standardList"
          file-name="任职资格标准"
          title="任职资格标准"
          import-api="/api/standard"
          :field-map="ioFieldMap"
          @done="loadData"
        />
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新增标准
        </el-button>
        <el-button @click="loadData">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标准名称">
          <el-input v-model="searchForm.name" placeholder="请输入标准名称" style="width:180px" />
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="searchForm.level" placeholder="选择等级" style="width:140px" clearable>
            <el-option label="初级" value="初级" />
            <el-option label="中级" value="中级" />
            <el-option label="高级" value="高级" />
            <el-option label="专家" value="专家" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" style="width:120px" clearable>
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="standardList" border stripe size="small" v-loading="loading">
        <el-table-column prop="standardNo" label="标准编号" width="140" />
        <el-table-column prop="name" label="标准名称" width="180" />
        <el-table-column prop="level" label="等级" width="100">
          <template #default="{row}">
            <el-tag size="small">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="类别" width="100" />
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="sequence" label="岗位序列" width="90">
          <template #default="{row}">
            <el-tag size="small" type="info">{{ row.sequence || '—' }}</el-tag>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="grade" label="档位" width="70">
          <template #default="{row}">{{ row.grade ? row.grade + '档' : '—' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展：五维加权（基础20%+知识技能25%+行为履职25%+绩效贡献20%+综合素质10%） -->
        <el-table-column label="五维权重" width="120">
          <template #default="{row}">
            <el-progress :percentage="weightSumOf(row)" :stroke-width="12" :text-inside="true" :status="weightSumOf(row) === 100 ? 'success' : 'exception'" />
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="passScore" label="达标线" width="80">
          <template #default="{row}">{{ row.passScore ?? '—' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展：L4/L5名额管控 -->
        <el-table-column label="名额(用/配)" width="100">
          <template #default="{row}">{{ row.quotaUsed ?? 0 }}/{{ row.quotaTotal ?? '不限' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展：2年有效期制度 -->
        <el-table-column label="有效期" width="80">
          <template #default="{row}">{{ row.validYears ? row.validYears + '年' : '—' }}</template>
        </el-table-column>
        <el-table-column prop="requirements" label="资格要求" min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="row.status === '启用' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="140" />
        <el-table-column prop="updateTime" label="更新时间" width="140" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button v-if="row.status !== '启用'" link size="small" type="success" @click="toggleStatus(row)">发布</el-button>
            <el-button v-else link size="small" type="warning" @click="toggleStatus(row)">停用</el-button>
            <el-button link size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="820px">
      <el-form :model="formData" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="标准名称" required>
              <el-input v-model="formData.name" placeholder="请输入标准名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标准编号" required>
              <el-input v-model="formData.standardNo" placeholder="自动生成" readonly />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="等级" required>
              <el-select v-model="formData.level" placeholder="选择等级">
                <el-option label="初级" value="初级" />
                <el-option label="中级" value="中级" />
                <el-option label="高级" value="高级" />
                <el-option label="专家" value="专家" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类别">
              <el-input v-model="formData.category" placeholder="例如：技术类、管理类" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="formData.status" placeholder="选择状态">
                <el-option label="启用" value="启用" />
                <el-option label="停用" value="停用" />
              </el-select>
            </el-form-item>
          </el-col>
          <!-- 规格书字段，待后端扩展 -->
          <el-col :span="12">
            <el-form-item label="档位">
              <el-select v-model="formData.grade" placeholder="选择档位" clearable>
                <el-option label="a档" value="a" />
                <el-option label="b档" value="b" />
                <el-option label="c档" value="c" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位序列">
              <el-select v-model="formData.sequence" placeholder="选择岗位序列" clearable>
                <el-option label="管理序列(M)" value="M" />
                <el-option label="专业技术序列(P)" value="P" />
                <el-option label="专业职能序列(F)" value="F" />
                <el-option label="业务销售序列(S)" value="S" />
                <el-option label="操作运营序列(O)" value="O" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="子序列">
              <el-input v-model="formData.subSequence" placeholder="例如：后端开发、前端开发" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生效日期">
              <el-date-picker v-model="formData.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择生效日期" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="失效日期">
              <el-date-picker v-model="formData.expiryDate" type="date" value-format="YYYY-MM-DD" placeholder="选择失效日期" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展：五维加权评审算法（基础条件20%+知识技能25%+行为履职25%+绩效贡献20%+综合素质10%） -->
        <el-divider content-position="left">五维考核权重（合计须为100%）</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="基础条件%">
              <el-input-number v-model="formData.baseWeight" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="知识技能%">
              <el-input-number v-model="formData.knowledgeWeight" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="行为履职%">
              <el-input-number v-model="formData.behaviorWeight" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="绩效贡献%">
              <el-input-number v-model="formData.performanceWeight" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="综合素质%">
              <el-input-number v-model="formData.qualityWeight" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="权重合计">
              <el-tag :type="weightSum === 100 ? 'success' : 'danger'" size="large">{{ weightSum }}%</el-tag>
              <span v-if="weightSum !== 100" style="color:#f56c6c; margin-left:8px; font-size:12px">≠100%禁止保存</span>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展：达标阈值/关键项100%达标/普通项容错/名额管控/2年有效期 -->
        <el-divider content-position="left">达标阈值与名额管控</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="达标分数线">
              <el-input-number v-model="formData.passScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="普通项容错%">
              <el-input-number v-model="formData.normalTolerance" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="关键项严卡">
              <el-switch v-model="formData.keyItemStrict" active-text="100%达标" inactive-text="允许容错" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="年度名额">
              <el-input-number v-model="formData.quotaTotal" :min="0" :max="9999" style="width:100%" placeholder="0或空=不限" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="有效期(年)">
              <el-input-number v-model="formData.validYears" :min="1" :max="10" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="已用名额">
              <el-input-number v-model="formData.quotaUsed" :min="0" :max="9999" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="准入条件">
              <el-input v-model="formData.admissionCondition" placeholder="如：司龄≥2年且近2年绩效≥B" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="举证要求">
              <el-input v-model="formData.evidenceRequirement" placeholder="如：项目成果、证书、绩效证明" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="资格要求">
          <el-input type="textarea" v-model="formData.requirements" :rows="4" placeholder="请输入资格要求" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="formData.description" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import ImportExportBar from '@/components/ImportExportBar.vue'

const ioColumns = [
  { key: 'standardNo', title: '标准编号' },
  { key: 'name', title: '标准名称' },
  { key: 'level', title: '等级' },
  { key: 'grade', title: '档位' },
  { key: 'sequence', title: '岗位序列' },
  { key: 'category', title: '类别' },
  { key: 'baseWeight', title: '基础条件权重%' },
  { key: 'knowledgeWeight', title: '知识技能权重%' },
  { key: 'behaviorWeight', title: '行为履职权重%' },
  { key: 'performanceWeight', title: '绩效贡献权重%' },
  { key: 'qualityWeight', title: '综合素质权重%' },
  { key: 'passScore', title: '达标分数线' },
  { key: 'quotaTotal', title: '年度名额' },
  { key: 'quotaUsed', title: '已用名额' },
  { key: 'validYears', title: '有效期(年)' },
  { key: 'requirements', title: '资格要求' },
  { key: 'status', title: '状态' },
  { key: 'createTime', title: '创建时间' },
  { key: 'updateTime', title: '更新时间' }
]
const ioFieldMap = {
  '标准编号': 'standardNo',
  '标准名称': 'name',
  '等级': 'level',
  '类别': 'category',
  '资格要求': 'requirements',
  '描述': 'description',
  '状态': 'status',
  '档位': 'grade',
  '岗位序列': 'sequence',
  '子序列': 'subSequence',
  '生效日期': 'effectiveDate',
  '失效日期': 'expiryDate',
  '基础条件权重': 'baseWeight',
  '知识技能权重': 'knowledgeWeight',
  '行为履职权重': 'behaviorWeight',
  '绩效贡献权重': 'performanceWeight',
  '综合素质权重': 'qualityWeight',
  '达标分数线': 'passScore',
  '普通项容错': 'normalTolerance',
  '关键项严卡': 'keyItemStrict',
  '年度名额': 'quotaTotal',
  '已用名额': 'quotaUsed',
  '有效期': 'validYears',
  '准入条件': 'admissionCondition',
  '举证要求': 'evidenceRequirement'
}

const loading = ref(false)
const searchForm = reactive({
  name: '',
  level: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增标准')
const isEdit = ref(false)

const formData = reactive({
  id: null,
  standardNo: '',
  name: '',
  level: '',
  category: '',
  requirements: '',
  description: '',
  status: '启用',
  grade: '',
  sequence: '',
  subSequence: '',
  effectiveDate: '',
  expiryDate: '',
  // 规格书字段，待后端扩展：五维权重（默认20/25/25/20/10）
  baseWeight: 20,
  knowledgeWeight: 25,
  behaviorWeight: 25,
  performanceWeight: 20,
  qualityWeight: 10,
  passScore: 60,
  normalTolerance: 0,
  keyItemStrict: true,
  quotaTotal: 0,
  quotaUsed: 0,
  validYears: 2,
  admissionCondition: '',
  evidenceRequirement: ''
})

const standardList = ref([])

const num = (v) => (v === null || v === undefined || v === '' ? 0 : Number(v) || 0)

// 五维权重合计（规格书算法：合计必须=100%）
const weightSum = computed(() =>
  num(formData.baseWeight) + num(formData.knowledgeWeight) + num(formData.behaviorWeight) +
  num(formData.performanceWeight) + num(formData.qualityWeight)
)

function weightSumOf(row) {
  return num(row.baseWeight) + num(row.knowledgeWeight) + num(row.behaviorWeight) +
    num(row.performanceWeight) + num(row.qualityWeight)
}

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
    const res = await axios.get('/api/standard/list', {
      params: {
        page: pagination.currentPage,
        size: pagination.pageSize,
        name: searchForm.name || undefined,
        level: searchForm.level || undefined,
        status: searchForm.status || undefined
      }
    })
    const { list, total } = extractList(res)
    standardList.value = list
    pagination.total = total
  } catch (e) {
    ElMessage.error('加载等级标准列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.currentPage = 1
  loadData()
}

function resetSearch() {
  searchForm.name = ''
  searchForm.level = ''
  searchForm.status = ''
  pagination.currentPage = 1
  loadData()
}

function handleSizeChange(size) {
  pagination.pageSize = size
  loadData()
}

function handleCurrentChange(page) {
  pagination.currentPage = page
  loadData()
}

function resetForm() {
  Object.assign(formData, {
    id: null,
    standardNo: '',
    name: '',
    level: '',
    category: '',
    requirements: '',
    description: '',
    status: '启用',
    grade: '',
    sequence: '',
    subSequence: '',
    effectiveDate: '',
    expiryDate: '',
    baseWeight: 20,
    knowledgeWeight: 25,
    behaviorWeight: 25,
    performanceWeight: 20,
    qualityWeight: 10,
    passScore: 60,
    normalTolerance: 0,
    keyItemStrict: true,
    quotaTotal: 0,
    quotaUsed: 0,
    validYears: 2,
    admissionCondition: '',
    evidenceRequirement: ''
  })
}

function openAddDialog() {
  isEdit.value = false
  dialogTitle.value = '新增标准'
  resetForm()
  formData.standardNo = 'STD-' + new Date().getFullYear() + '-' + String(Date.now()).slice(-3)
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  dialogTitle.value = '编辑标准'
  Object.assign(formData, {
    id: row.id,
    standardNo: row.standardNo || '',
    name: row.name || '',
    level: row.level || '',
    category: row.category || '',
    requirements: row.requirements || '',
    description: row.description || '',
    status: row.status || '启用',
    grade: row.grade || '',
    sequence: row.sequence || '',
    subSequence: row.subSequence || '',
    effectiveDate: row.effectiveDate || '',
    expiryDate: row.expiryDate || '',
    baseWeight: row.baseWeight ?? 20,
    knowledgeWeight: row.knowledgeWeight ?? 25,
    behaviorWeight: row.behaviorWeight ?? 25,
    performanceWeight: row.performanceWeight ?? 20,
    qualityWeight: row.qualityWeight ?? 10,
    passScore: row.passScore ?? 60,
    normalTolerance: row.normalTolerance ?? 0,
    keyItemStrict: row.keyItemStrict ?? true,
    quotaTotal: row.quotaTotal ?? 0,
    quotaUsed: row.quotaUsed ?? 0,
    validYears: row.validYears ?? 2,
    admissionCondition: row.admissionCondition || '',
    evidenceRequirement: row.evidenceRequirement || ''
  })
  dialogVisible.value = true
}

function toggleStatus(row) {
  const target = row.status === '启用' ? '停用' : '启用'
  ElMessageBox.confirm(`确定要${target}等级标准"${row.name}"吗？`, `${target}确认`, { type: 'warning' })
    .then(async () => {
      try {
        await axios.put(`/api/standard/${row.id}`, { ...row, status: target })
        ElMessage.success(`已${target}`)
        loadData()
      } catch (e) {
        ElMessage.error(`${target}失败`)
        console.error(e)
      }
    })
    .catch(() => {})
}

function viewDetail(row) {
  ElMessage.info(`查看等级标准: ${row.name}`)
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除等级标准"${row.name}"吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      try {
        await axios.delete(`/api/standard/${row.id}`)
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
  // 规格书拦截逻辑：五维权重合计≠100%禁止保存；未配置达标分数线禁止启用
  if (weightSum.value !== 100) {
    ElMessage.error(`五维权重合计为${weightSum.value}%，不等于100%，禁止保存`)
    return
  }
  if (!formData.passScore && formData.status === '启用') {
    ElMessage.error('未配置达标分数线，禁止启用标准')
    return
  }
  try {
    if (isEdit.value) {
      await axios.put(`/api/standard/${formData.id}`, formData)
      ElMessage.success('等级标准已更新')
    } else {
      await axios.post('/api/standard', formData)
      ElMessage.success('等级标准已创建')
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
.header-actions { display: flex; align-items: center; gap: 8px; }
.search-form { margin-bottom: 0; }
</style>

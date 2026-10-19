<template>
  <div class="page-card">
    <div class="page-header">
      <h2>岗位价值评估与岗位定级</h2>
      <div class="header-actions">
        <ImportExportBar
          :columns="ioColumns"
          :rows="positionList"
          file-name="岗位价值评估"
          title="岗位价值评估台账"
        />
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新增评估
        </el-button>
      </div>
    </div>

    <!-- 四套评估模型一键切换（规格书第十五章） -->
    <el-card shadow="never" style="margin-bottom:16px">
      <div class="model-bar">
        <span class="model-label">评估模型：</span>
        <el-radio-group v-model="currentModel" @change="onModelChange">
          <el-radio-button v-for="m in modelList" :key="m.key" :value="m.key">{{ m.name }}</el-radio-button>
        </el-radio-group>
      </div>
      <el-table :data="modelDims" border size="small" style="margin-top:12px">
        <el-table-column prop="name" label="评估维度" min-width="160" />
        <el-table-column prop="weight" label="权重" width="100">
          <template #default="{row}">{{ (row.weight * 100).toFixed(0) }}%</template>
        </el-table-column>
        <el-table-column prop="desc" label="评分标准（0-100分制）" min-width="320" show-overflow-tooltip />
      </el-table>
      <div class="model-formula">{{ modelFormula }}</div>
    </el-card>

    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="岗位名称">
          <el-input v-model="searchForm.positionName" placeholder="请输入岗位名称" style="width:180px" clearable />
        </el-form-item>
        <el-form-item label="价值等级">
          <el-select v-model="searchForm.valueLevel" placeholder="选择等级" style="width:150px" clearable>
            <el-option label="一级" value="一级" />
            <el-option label="二级" value="二级" />
            <el-option label="三级" value="三级" />
            <el-option label="四级" value="四级" />
            <el-option label="五级" value="五级" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="positionList" border stripe size="small" v-loading="loading">
        <el-table-column prop="id" label="评估单号" width="90" />
        <el-table-column prop="positionCode" label="岗位编码" width="120" />
        <el-table-column prop="positionName" label="岗位名称" width="150" />
        <el-table-column prop="department" label="所属组织" width="120" />
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="jobSequence" label="岗位序列" width="110">
          <template #default="{row}">{{ row.jobSequence || '-' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="evalModel" label="选用评估模型" width="130">
          <template #default="{row}">
            <el-tag v-if="row.evalModel" size="small" type="primary">{{ getModelName(row.evalModel) }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="knowledgeScore" label="维度一" width="80">
          <template #default="{row}">{{ row.knowledgeScore ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="responsibilityScore" label="维度二" width="80">
          <template #default="{row}">{{ row.responsibilityScore ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="effortScore" label="维度三" width="80">
          <template #default="{row}">{{ row.effortScore ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="environmentScore" label="维度四" width="80">
          <template #default="{row}">{{ row.environmentScore ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="totalScore" label="加权总分" width="90">
          <template #default="{row}">
            <el-tag v-if="row.totalScore != null" size="small">{{ row.totalScore }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="系统匹配岗级" width="150">
          <template #default="{row}">{{ matchGrade(row) }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="calibrateLevel" label="人工校准岗级" width="110">
          <template #default="{row}">{{ row.calibrateLevel || '-' }}</template>
        </el-table-column>
        <el-table-column prop="valueLevel" label="价值等级" width="90">
          <template #default="{row}">
            <el-tag v-if="row.valueLevel" :type="getLevelType(row.valueLevel)" size="small">{{ row.valueLevel }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="evalVersion" label="评估版本" width="90">
          <template #default="{row}">{{ row.evalVersion || 'V1' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="评估状态" width="90">
          <template #default="{row}">
            <el-tag v-if="row.status" :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="assessTime" label="生效时间" width="150" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="editRow(row)">打分</el-button>
            <el-button link size="small" type="success" v-if="row.status !== '已完成'" @click="confirmEffective(row)">生效</el-button>
            <el-button link size="small" type="danger" @click="deleteRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; text-align: right"
      />
    </el-card>

    <!-- 在线打分弹窗（规格书950px） -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="950px">
      <el-form :model="formData" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="岗位编码" required>
              <el-input v-model="formData.positionCode" placeholder="请输入岗位编码" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="岗位名称" required>
              <el-input v-model="formData.positionName" placeholder="请输入岗位名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="所属组织">
              <el-input v-model="formData.department" placeholder="请输入所属组织" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="岗位序列">
              <el-select v-model="formData.jobSequence" placeholder="选择序列" style="width:100%">
                <el-option label="管理序列" value="管理序列" />
                <el-option label="技术研发序列" value="技术研发序列" />
                <el-option label="职能支撑序列" value="职能支撑序列" />
                <el-option label="业务营销序列" value="业务营销序列" />
                <el-option label="生产操作序列" value="生产操作序列" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="评估模型">
              <el-select v-model="formData.evalModel" placeholder="选择模型" style="width:100%">
                <el-option v-for="m in modelList" :key="m.key" :label="m.name" :value="m.key" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="评估版本">
              <el-input v-model="formData.evalVersion" placeholder="例如：V1" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item :label="dimLabel(0)" required>
              <el-input-number v-model="formData.knowledgeScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item :label="dimLabel(1)" required>
              <el-input-number v-model="formData.responsibilityScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item :label="dimLabel(2)" required>
              <el-input-number v-model="formData.effortScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item :label="dimLabel(3)" required>
              <el-input-number v-model="formData.environmentScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="加权总分">
              <el-input :model-value="weightedTotal" readonly>
                <template #append>分</template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="系统匹配岗级">
              <el-input :model-value="autoGrade" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="人工校准岗级">
              <el-input v-model="formData.calibrateLevel" placeholder="可选，校准需留痕" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 岗级大幅校准预警（跨度超2级高危预警，规格书第十五章） -->
        <el-alert v-if="calibrateWarning" type="error" :closable="false" show-icon
          title="岗级大幅校准预警" :description="calibrateWarning" style="margin-bottom:12px" />
        <el-form-item label="打分依据">
          <el-input type="textarea" v-model="formData.scoreBasis" :rows="3" placeholder="请输入打分依据与佐证说明（规格书字段，待后端扩展）" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import ImportExportBar from '@/components/ImportExportBar.vue'

// 四大评估模型（规格书第十五章，权重与薪级匹配规则）
const modelList = [
  {
    key: 'huawei', name: '华为量化模型',
    dims: [
      { name: '岗位责任', weight: 0.25, desc: '经营、管理、业务、合规责任大小，覆盖执行/部门/公司经营/战略决策四级' },
      { name: '工作复杂度', weight: 0.20, desc: '流程难度、问题复杂度、突发场景、非标工作占比、跨部门协同难度' },
      { name: '专业技能', weight: 0.20, desc: '专业深度、从业年限、资质门槛、技术壁垒、学习难度' },
      { name: '管理跨度', weight: 0.15, desc: '管辖人数、团队层级、项目管控范围、资源调度权限' }
    ],
    formula: '总分=责任×25%+复杂度×20%+技能×20%+管理跨度×15%+负荷风险×10%+稀缺度×10%（前四维在线打分）',
    grades: [[90, 'L10-L12 集团高层/核心决策岗'], [80, 'L07-L09 中层管理/高级专家岗'], [70, 'L04-L06 资深骨干/技术专家岗'], [60, 'L02-L03 成熟执行骨干岗'], [0, 'L01 基础操作/辅助岗']]
  },
  {
    key: 'mercer', name: 'Mercer IPE3.0',
    dims: [
      { name: '岗位影响与贡献', weight: 0.30, desc: '对营收、利润、战略落地、业务结果的直接影响层级' },
      { name: '沟通协作层级', weight: 0.20, desc: '内外部沟通难度、跨组织协同、商务谈判、公共对接复杂度' },
      { name: '创新与变革能力', weight: 0.20, desc: '工作标准化程度、流程优化、问题创新解决、模式变革要求' },
      { name: '专业知识与经验', weight: 0.20, desc: '专业壁垒、从业经验、资质要求、技术迭代难度' }
    ],
    formula: '总分=影响贡献×30%+沟通协作×20%+创新变革×20%+知识经验×20%+风险管控×10%（前四维在线打分）',
    grades: [[90, 'L10-L12 高层管理岗'], [80, 'L07-L09 中层管理/核心专家岗'], [70, 'L04-L06 资深骨干/技术专家岗'], [60, 'L02-L03 成熟执行岗'], [0, 'L01 基础操作岗']]
  },
  {
    key: 'hay', name: 'HAY海氏模型',
    dims: [
      { name: '知识技能水平', weight: 0.40, desc: '专业知识深度、管理技能广度、人际协同能力，基础/熟练/专业/资深/专家五级' },
      { name: '解决问题能力', weight: 0.30, desc: '思维难度、问题复杂度、自主决策权限、突发问题处置与创新解决能力' },
      { name: '岗位承担责任', weight: 0.30, desc: '行动自由度、岗位决策权、经营结果责任、团队管理责任、失误后果影响' },
      { name: '综合补充维度', weight: 0.00, desc: '海氏三维以外的补充观察项（不计入加权）' }
    ],
    formula: '总分=知识技能×40%+解决问题×30%+岗位责任×30%',
    grades: [[85, '高层决策岗/核心经营岗'], [75, '中层管理/高级技术专家'], [65, '主管骨干/资深技术岗'], [55, '常规职能/成熟业务岗'], [0, '基础执行/辅助操作岗']]
  },
  {
    key: 'aon', name: '翰威特模型',
    dims: [
      { name: '岗位战略价值', weight: 0.20, desc: '对战略目标、业务增长、核心产出的支撑价值' },
      { name: '专业胜任要求', weight: 0.20, desc: '学历、从业年限、资质门槛、专业壁垒、岗位准入难度' },
      { name: '工作复杂度', weight: 0.15, desc: '流程复杂度、跨部门协同频次、工作迭代、突发工作占比' },
      { name: '管理与人效责任', weight: 0.15, desc: '团队管理规模、项目管控权限、目标与结果责任' }
    ],
    formula: '总分=战略价值×20%+专业胜任×20%+复杂度×15%+管理责任×15%+创新价值×15%+稀缺替代×15%（前四维在线打分）',
    grades: [[90, '特级核心岗/集团核心管理层'], [80, '一级骨干岗/高级管理或专家岗'], [70, '二级成熟骨干岗/中层业务岗'], [60, '三级常规执行岗/职能支撑岗'], [0, '基础辅助岗/入门执行岗']]
  }
]

const currentModel = ref('huawei')
const modelDims = computed(() => modelList.find(m => m.key === currentModel.value).dims)
const modelFormula = computed(() => '计算公式：' + modelList.find(m => m.key === currentModel.value).formula)

function getModelName(key) {
  const hit = modelList.find(m => m.key === key)
  return hit ? hit.name : key
}

const loading = ref(false)
const positionList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增岗位评估')

const searchForm = reactive({ positionName: '', valueLevel: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const formData = reactive({
  id: null,
  positionCode: '',
  positionName: '',
  department: '',
  jobSequence: '',
  evalModel: 'huawei',
  evalVersion: 'V1',
  knowledgeScore: null,
  responsibilityScore: null,
  effortScore: null,
  environmentScore: null,
  calibrateLevel: '',
  scoreBasis: ''
})

const ioColumns = [
  { key: 'id', title: '评估单号' },
  { key: 'positionCode', title: '岗位编码' },
  { key: 'positionName', title: '岗位名称' },
  { key: 'department', title: '所属组织' },
  { key: 'jobSequence', title: '岗位序列' },
  { key: 'evalModel', title: '选用评估模型' },
  { key: 'totalScore', title: '加权总分' },
  { key: 'calibrateLevel', title: '人工校准岗级' },
  { key: 'valueLevel', title: '价值等级' },
  { key: 'evalVersion', title: '评估版本' },
  { key: 'status', title: '评估状态' },
  { key: 'assessTime', title: '生效时间' }
]

function dimLabel(idx) {
  const dims = modelList.find(m => m.key === formData.evalModel)?.dims || modelDims.value
  return dims[idx] ? dims[idx].name : `维度${idx + 1}`
}

// 按当前模型权重前端实时加权（0-100分制）
const weightedTotal = computed(() => {
  const dims = modelList.find(m => m.key === formData.evalModel)?.dims || []
  const scores = [formData.knowledgeScore, formData.responsibilityScore, formData.effortScore, formData.environmentScore]
  let total = 0, weightSum = 0
  for (let i = 0; i < 4; i++) {
    const s = Number(scores[i])
    const w = dims[i] ? dims[i].weight : 0
    if (!isNaN(s) && scores[i] != null) {
      total += s * w
      weightSum += w
    }
  }
  if (weightSum <= 0) return 0
  // 归一化到0-100
  return Math.round(total / weightSum)
})

function gradeText(modelKey, score) {
  const m = modelList.find(x => x.key === modelKey) || modelList[0]
  for (const [min, label] of m.grades) {
    if (score >= min) return label
  }
  return m.grades[m.grades.length - 1][1]
}

const autoGrade = computed(() => gradeText(formData.evalModel, weightedTotal.value))

function matchGrade(row) {
  if (row.totalScore == null) return '-'
  return gradeText(row.evalModel || 'huawei', Number(row.totalScore))
}

// 价值等级（一级~五级）按总分区间自动匹配
function valueLevelOf(score) {
  if (score >= 90) return '一级'
  if (score >= 80) return '二级'
  if (score >= 70) return '三级'
  if (score >= 60) return '四级'
  return '五级'
}

// 校准跨度超2级高危预警
const calibrateWarning = computed(() => {
  if (!formData.calibrateLevel) return ''
  const sysLevel = valueLevelOf(weightedTotal.value)
  const levelNum = { '一级': 1, '二级': 2, '三级': 3, '四级': 4, '五级': 5 }
  const cal = String(formData.calibrateLevel).replace(/[级\s]/g, '')
  const calMap = { '一': 1, '二': 2, '三': 3, '四': 4, '五': 5, '1': 1, '2': 2, '3': 3, '4': 4, '5': 5 }
  const calNum = levelNum[formData.calibrateLevel] ?? calMap[cal]
  if (calNum == null) return ''
  if (Math.abs(calNum - levelNum[sysLevel]) > 2) {
    return `人工校准岗级与系统匹配岗级（${sysLevel}）跨度超2级，需总监级复核后方可生效`
  }
  return ''
})

async function loadData() {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (searchForm.positionName) params.positionName = searchForm.positionName
    if (searchForm.valueLevel) params.valueLevel = searchForm.valueLevel
    const res = await axios.get('/api/salary/job-value/list', { params })
    const data = res.data
    let list = []
    let total = 0
    if (data && data.code === 200) {
      const payload = data.data
      if (Array.isArray(payload)) {
        list = payload
        total = payload.length
      } else if (payload && Array.isArray(payload.records)) {
        list = payload.records
        total = payload.total != null ? payload.total : payload.records.length
      }
    } else if (data && Array.isArray(data.records)) {
      list = data.records
      total = data.total != null ? data.total : data.records.length
    } else if (Array.isArray(data)) {
      list = data
      total = data.length
    }
    positionList.value = list
    pagination.total = total
  } catch (e) {
    ElMessage.error('加载岗位价值评估失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 模型切换未保存拦截
async function onModelChange(val) {
  if (dialogVisible.value) {
    try {
      await ElMessageBox.confirm('切换模型将清空当前未保存打分数据并加载新模型维度与权重，是否继续？', '模型切换确认', { type: 'warning' })
      formData.evalModel = val
      formData.knowledgeScore = null
      formData.responsibilityScore = null
      formData.effortScore = null
      formData.environmentScore = null
    } catch (e) {}
  }
}

function getLevelType(level) {
  const types = { '一级': 'danger', '二级': 'warning', '三级': 'primary', '四级': 'success', '五级': 'info' }
  return types[level] || 'info'
}

function getStatusType(status) {
  const types = { '待评估': 'warning', '评估中': 'primary', '已完成': 'success' }
  return types[status] || 'info'
}

function handleSearch() {
  pagination.page = 1
  loadData()
}

function resetSearch() {
  searchForm.positionName = ''
  searchForm.valueLevel = ''
  pagination.page = 1
  loadData()
}

function handleSizeChange(val) {
  pagination.size = val
  pagination.page = 1
  loadData()
}

function handleCurrentChange(val) {
  pagination.page = val
  loadData()
}

function openAddDialog() {
  dialogTitle.value = '新增岗位评估'
  Object.assign(formData, {
    id: null, positionCode: '', positionName: '', department: '', jobSequence: '',
    evalModel: currentModel.value, evalVersion: 'V1',
    knowledgeScore: null, responsibilityScore: null, effortScore: null, environmentScore: null,
    calibrateLevel: '', scoreBasis: ''
  })
  dialogVisible.value = true
}

function editRow(row) {
  dialogTitle.value = '在线打分'
  Object.assign(formData, {
    id: row.id, positionCode: row.positionCode, positionName: row.positionName,
    department: row.department, jobSequence: row.jobSequence || '',
    evalModel: row.evalModel || currentModel.value, evalVersion: row.evalVersion || 'V1',
    knowledgeScore: row.knowledgeScore ?? null, responsibilityScore: row.responsibilityScore ?? null,
    effortScore: row.effortScore ?? null, environmentScore: row.environmentScore ?? null,
    calibrateLevel: row.calibrateLevel || '', scoreBasis: row.scoreBasis || ''
  })
  dialogVisible.value = true
}

// 评估版本生效红色二次确认
async function confirmEffective(row) {
  try {
    await ElMessageBox.confirm(
      `评估版本生效后将同步更新对应模型下薪酬矩阵匹配关系，且不可直接修改。确认「${row.positionName}」评估结果生效？`,
      '评估生效确认',
      { type: 'error', confirmButtonText: '确认生效', cancelButtonText: '取消' }
    )
    const now = new Date()
    const ts = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
    const res = await axios.put(`/api/salary/job-value/${row.id}`, { ...row, status: '已完成', assessTime: ts })
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success('评估结果已生效')
      loadData()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
      console.error(e)
    }
  }
}

function deleteRow(row) {
  ElMessageBox.confirm(`确定要删除岗位"${row.positionName}"的评估记录吗？评估数据将归档留存。`, '删除确认', { type: 'warning' })
    .then(async () => {
      try {
        const res = await axios.delete(`/api/salary/job-value/${row.id}`)
        if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
          ElMessage.success('评估记录已删除')
          loadData()
        } else {
          ElMessage.error(res.data.message || res.data.msg || '删除失败')
        }
      } catch (e) {
        ElMessage.error('删除失败')
        console.error(e)
      }
    })
    .catch(() => {})
}

async function handleSubmit() {
  if (!formData.positionCode || !formData.positionName) {
    ElMessage.warning('请填写岗位编码与岗位名称')
    return
  }
  // 维度缺分拦截：任一维度未打分禁止提交
  const scores = [formData.knowledgeScore, formData.responsibilityScore, formData.effortScore, formData.environmentScore]
  if (scores.some(s => s == null)) {
    ElMessage.error('当前模型任一维度未打分，禁止生成评估结果与薪级匹配')
    return
  }
  // 分值越界拦截：单维度0-100
  if (scores.some(s => s < 0 || s > 100)) {
    ElMessage.error('单维度分值超出0-100分区间，请修正后保存')
    return
  }
  const payload = {
    ...formData,
    totalScore: weightedTotal.value,
    valueLevel: valueLevelOf(weightedTotal.value),
    status: '评估中'
  }
  try {
    let res
    if (formData.id) {
      res = await axios.put(`/api/salary/job-value/${formData.id}`, payload)
    } else {
      res = await axios.post('/api/salary/job-value', payload)
    }
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success(formData.id ? '评估打分已保存' : '评估新增成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '操作失败')
    }
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
.model-bar { display: flex; align-items: center; gap: 12px; }
.model-label { font-weight: 600; }
.model-formula { margin-top: 10px; color: #409eff; font-size: 13px; }
</style>

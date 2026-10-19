<template>
  <div class="page-card">
    <div class="page-header">
      <h2>职级薪酬矩阵与薪酬包配置</h2>
      <div class="header-actions">
        <ImportExportBar
          :columns="ioColumns"
          :rows="salaryLevels"
          file-name="职级薪酬矩阵"
          title="职级薪酬矩阵列表"
        />
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新增职级
        </el-button>
      </div>
    </div>

    <!-- 核心算法说明（规格书第十四章 页面2） -->
    <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px"
      title="核心算法：月度经营薪酬包=当月营收×核定E/R值×效益浮动系数；战略薪酬包=战略预算额度×任务完成进度系数；薪酬带宽=(上限-下限)÷中位值×100%；员工薪酬总额严格落在对应职级薪酬带宽内，超带宽自动规则拦截" />

    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover" class="salary-card">
          <div class="salary-icon">1</div>
          <div class="salary-info">
            <h4>基本工资</h4>
            <p>占比 50%</p>
            <p class="salary-desc">岗位固定薪资，根据职级带宽确定</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="salary-card">
          <div class="salary-icon">2</div>
          <div class="salary-info">
            <h4>绩效工资</h4>
            <p>占比 30%</p>
            <p class="salary-desc">根据绩效考核结果计算</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="salary-card">
          <div class="salary-icon">3</div>
          <div class="salary-info">
            <h4>奖金</h4>
            <p>占比 15%</p>
            <p class="salary-desc">季度/年度绩效奖金</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="salary-card">
          <div class="salary-icon">4</div>
          <div class="salary-info">
            <h4>津贴补贴</h4>
            <p>占比 5%</p>
            <p class="salary-desc">通讯、餐补、交通等补贴</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top:16px">
      <h3 class="card-title">职级薪酬带宽矩阵</h3>
      <el-table :data="salaryLevels" border stripe size="small" v-loading="loading">
        <el-table-column prop="level" label="职级" width="90" />
        <el-table-column prop="levelName" label="等级名称" width="110" />
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="jobSequence" label="岗位序列" width="110">
          <template #default="{row}">{{ row.jobSequence || '-' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="bandMin" label="带宽下限" width="110">
          <template #default="{row}">{{ fmtNum(row.bandMin) }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="bandMid" label="带宽中位" width="110">
          <template #default="{row}">{{ fmtNum(row.bandMid) }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="bandMax" label="带宽上限" width="110">
          <template #default="{row}">{{ fmtNum(row.bandMax) }}</template>
        </el-table-column>
        <el-table-column label="薪酬带宽" width="110">
          <template #default="{row}">
            <el-tag v-if="bandWidth(row) != null" size="small" type="warning">{{ bandWidth(row) }}%</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="basicSalary" label="基本工资(元)" width="120">
          <template #default="{row}">{{ fmtNum(row.basicSalary) }}</template>
        </el-table-column>
        <el-table-column prop="performanceSalary" label="绩效工资(元)" width="120">
          <template #default="{row}">{{ fmtNum(row.performanceSalary) }}</template>
        </el-table-column>
        <el-table-column prop="totalSalary" label="年度标准薪酬(元)" width="140">
          <template #default="{row}">{{ fmtNum(row.totalSalary) }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="packageType" label="薪酬包类型" width="120">
          <template #default="{row}">
            <el-tag v-if="row.packageType" size="small" :type="row.packageType === '战略薪酬包' ? 'danger' : 'primary'">{{ row.packageType }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="applyOrg" label="适用组织" width="130">
          <template #default="{row}">{{ row.applyOrg || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="生效状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status === '生效' ? 'success' : 'info'" size="small">{{ row.status || '生效' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="editLevel(row)">编辑</el-button>
            <el-button link size="small" type="danger" @click="deleteLevel(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 规则配置弹窗（规格书950px） -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="950px">
      <el-form :model="formData" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="职级" required>
              <el-input v-model="formData.level" placeholder="例如：L1" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="等级名称" required>
              <el-input v-model="formData.levelName" placeholder="例如：初级" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="岗位序列">
              <el-select v-model="formData.jobSequence" placeholder="选择序列">
                <el-option label="管理序列" value="管理序列" />
                <el-option label="技术研发序列" value="技术研发序列" />
                <el-option label="职能支撑序列" value="职能支撑序列" />
                <el-option label="业务营销序列" value="业务营销序列" />
                <el-option label="生产操作序列" value="生产操作序列" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="薪酬带宽下限">
              <el-input-number v-model="formData.bandMin" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="薪酬带宽中位">
              <el-input-number v-model="formData.bandMid" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="薪酬带宽上限">
              <el-input-number v-model="formData.bandMax" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="基本工资" required>
              <el-input-number v-model="formData.basicSalary" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="绩效工资" required>
              <el-input-number v-model="formData.performanceSalary" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="年度标准薪酬">
              <el-input-number v-model="formData.totalSalary" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="薪酬包类型">
              <el-select v-model="formData.packageType" placeholder="选择薪酬包">
                <el-option label="经营薪酬包" value="经营薪酬包" />
                <el-option label="战略薪酬包" value="战略薪酬包" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="E/R值阈值(%)">
              <el-input-number v-model="formData.erThreshold" :min="0" :max="100" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="适用组织">
              <el-input v-model="formData.applyOrg" placeholder="例如：集团总部" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="适用岗位说明">
          <el-input type="textarea" v-model="formData.description" :rows="3" placeholder="请输入适用岗位说明" />
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
import ImportExportBar from '@/components/ImportExportBar.vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增职级薪酬')

const formData = reactive({
  id: null,
  level: '',
  levelName: '',
  jobSequence: '',
  bandMin: 0,
  bandMid: 0,
  bandMax: 0,
  basicSalary: 0,
  performanceSalary: 0,
  totalSalary: 0,
  packageType: '',
  erThreshold: null,
  applyOrg: '',
  description: ''
})

const salaryLevels = ref([])

const ioColumns = [
  { key: 'level', title: '职级' },
  { key: 'levelName', title: '等级名称' },
  { key: 'jobSequence', title: '岗位序列' },
  { key: 'bandMin', title: '带宽下限' },
  { key: 'bandMid', title: '带宽中位' },
  { key: 'bandMax', title: '带宽上限' },
  { key: 'basicSalary', title: '基本工资' },
  { key: 'performanceSalary', title: '绩效工资' },
  { key: 'totalSalary', title: '年度标准薪酬' },
  { key: 'packageType', title: '薪酬包类型' },
  { key: 'applyOrg', title: '适用组织' },
  { key: 'status', title: '生效状态' }
]

function fmtNum(v) {
  return v != null ? Number(v).toLocaleString() : '-'
}

// 薪酬带宽算法：(上限-下限)÷中位×100%
function bandWidth(row) {
  const min = Number(row.bandMin), max = Number(row.bandMax), mid = Number(row.bandMid)
  if (!mid || mid <= 0 || max == null || min == null || isNaN(max) || isNaN(min)) return null
  return (((max - min) / mid) * 100).toFixed(1)
}

async function loadData() {
  loading.value = true
  try {
    const params = { page: 1, size: 100 }
    const res = await axios.get('/api/salary/matrix-package/list', { params })
    const data = res.data
    if (data && data.code === 200) {
      const payload = data.data
      salaryLevels.value = Array.isArray(payload) ? payload : (payload && Array.isArray(payload.records) ? payload.records : [])
    } else if (data && Array.isArray(data.records)) {
      salaryLevels.value = data.records
    } else if (Array.isArray(data)) {
      salaryLevels.value = data
    } else {
      salaryLevels.value = []
    }
  } catch (e) {
    ElMessage.error('加载职级薪酬矩阵失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  dialogTitle.value = '新增职级薪酬'
  Object.assign(formData, {
    id: null, level: '', levelName: '', jobSequence: '',
    bandMin: 0, bandMid: 0, bandMax: 0,
    basicSalary: 0, performanceSalary: 0, totalSalary: 0,
    packageType: '', erThreshold: null, applyOrg: '', description: ''
  })
  dialogVisible.value = true
}

function editLevel(row) {
  dialogTitle.value = '编辑职级薪酬'
  Object.assign(formData, {
    id: row.id, level: row.level, levelName: row.levelName,
    jobSequence: row.jobSequence || '',
    bandMin: row.bandMin ?? 0, bandMid: row.bandMid ?? 0, bandMax: row.bandMax ?? 0,
    basicSalary: row.basicSalary, performanceSalary: row.performanceSalary, totalSalary: row.totalSalary,
    packageType: row.packageType || '', erThreshold: row.erThreshold ?? null,
    applyOrg: row.applyOrg || '', description: row.description || ''
  })
  dialogVisible.value = true
}

function deleteLevel(row) {
  ElMessageBox.confirm(`确定要删除职级"${row.levelName}"吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      try {
        const res = await axios.delete(`/api/salary/matrix-package/${row.id}`)
        if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
          ElMessage.success('职级薪酬已删除')
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
  if (!formData.level || !formData.levelName) {
    ElMessage.warning('请填写职级与等级名称')
    return
  }
  // 带宽校验：上限必须大于下限
  if (formData.bandMax && formData.bandMin && formData.bandMax < formData.bandMin) {
    ElMessage.error('薪酬带宽上限不能低于下限')
    return
  }
  // 合计缺省时自动汇总
  if (!formData.totalSalary) {
    formData.totalSalary = (Number(formData.basicSalary) || 0) + (Number(formData.performanceSalary) || 0)
  }
  try {
    let res
    if (formData.id) {
      res = await axios.put(`/api/salary/matrix-package/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/salary/matrix-package', formData)
    }
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success(dialogTitle.value === '新增职级薪酬' ? '职级薪酬新增成功' : '职级薪酬已更新')
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
.card-title { margin: 0 0 16px; font-size: 15px; font-weight: 600; }

.salary-card { display: flex; align-items: center; gap: 16px; padding: 20px; }
.salary-icon { width: 48px; height: 48px; border-radius: 50%; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; display: flex; align-items: center; justify-content: center; font-size: 24px; font-weight: bold; }
.salary-info h4 { margin: 0 0 8px; font-size: 16px; }
.salary-info p { margin: 4px 0; color: #666; }
.salary-desc { font-size: 13px; color: #999; }
</style>

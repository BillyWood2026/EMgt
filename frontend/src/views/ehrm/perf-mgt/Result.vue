<template>
  <div class="page-card">
    <div class="page-header">
      <h2>评估结果</h2>
      <div class="header-actions">
        <el-button @click="exportResult">
          <el-icon><Download /></el-icon>导出结果
        </el-button>
      </div>
    </div>

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6">
        <el-form-item label="评估周期">
          <el-select v-model="queryPeriod" placeholder="全部周期" clearable style="width:100%">
            <el-option label="2026年Q2" value="2026Q2" />
            <el-option label="2026年Q1" value="2026Q1" />
            <el-option label="2025年年度" value="2025Y" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="6">
        <el-form-item label="部门">
          <el-select v-model="queryDept" placeholder="全部部门" clearable style="width:100%">
            <el-option v-for="d in deptOptions" :key="d" :label="d" :value="d" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="6">
        <el-form-item label="评级">
          <el-select v-model="queryGrade" placeholder="全部评级" clearable style="width:100%">
            <el-option v-for="g in ['S','A','B','C','D']" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon"><el-icon><User /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ totalCount }}</div>
            <div class="stat-label">总人数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-icon"><el-icon><TrendCharts /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ avgScore }}</div>
            <div class="stat-label">平均分</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-icon"><el-icon><Medal /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ excellentCount }}</div>
            <div class="stat-label">S/A级人数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" :class="{danger: !distributionValid}">
          <div class="stat-icon"><el-icon><TrendCharts /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ distributionValid ? '合规' : '超限' }}</div>
            <div class="stat-label">强制分布校验</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="12">
        <el-card shadow="hover">
          <h3 class="card-title">评级分布（强制分布 S5%/A15%/B60%/C15%/D5%，容差±3%）</h3>
          <el-row :gutter="8">
            <el-col :span="4" v-for="item in gradeDistribution" :key="item.grade" style="margin-bottom:8px">
              <div class="grade-item">
                <div class="grade-label">
                  {{ item.grade }}
                  <el-tooltip :content="`强制分布要求 ${item.required}% ±3%`" placement="top">
                    <el-icon v-if="!item.valid" style="color:#f56c6c"><WarningFilled /></el-icon>
                  </el-tooltip>
                </div>
                <div class="grade-bar">
                  <div class="grade-fill" :style="{width: item.percentage + '%', background: item.color}"></div>
                </div>
                <div class="grade-count">{{ item.count }}人 ({{ item.percentage }}%)</div>
                <div class="grade-required" :style="{color: item.valid ? '#67c23a' : '#f56c6c'}">要求{{ item.required }}%</div>
              </div>
            </el-col>
          </el-row>
          <el-alert v-if="!distributionValid" type="error" :closable="false" style="margin-top:10px"
            title="评级分布超出强制分布容差（±3%），请校准评估结果后再归档" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <h3 class="card-title">部门平均分</h3>
          <el-table :data="deptAvgScore" border size="small">
            <el-table-column prop="department" label="部门" width="120" />
            <el-table-column prop="avgScore" label="平均分" width="140">
              <template #default="{row}">
                <el-progress :percentage="Math.min(100, row.avgScore)" :color="getScoreColor(row.avgScore)" :stroke-width="8" />
              </template>
            </el-table-column>
            <el-table-column prop="count" label="人数" width="80" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <ImportExportBar :columns="ioColumns" :rows="filteredList" file-name="评估结果" title="评估结果导出" style="margin-bottom:12px" @done="loadData" />

    <el-card shadow="never">
      <h3 class="card-title">评估结果明细</h3>
      <el-table :data="filteredList" border stripe size="small" v-loading="loading">
        <el-table-column prop="employeeName" label="员工姓名" width="100" />
        <el-table-column prop="department" label="部门" width="110" />
        <el-table-column prop="position" label="岗位" width="110" />
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="period" label="评估周期" width="100">
          <template #default="{row}">{{ row.period || '—' }}</template>
        </el-table-column>
        <el-table-column prop="score" label="评估得分" width="100">
          <template #default="{row}">
            <el-tag :type="getScoreTagType(row.score)" size="small">{{ row.score }}分</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="评级" width="80">
          <template #default="{row}">
            <el-tag :type="getGradeType(row.grade)" size="small">{{ row.grade }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rank" label="排名" width="90">
          <template #default="{row}"><span v-if="row.rank != null">第{{ row.rank }}名</span><span v-else>—</span></template>
        </el-table-column>
        <el-table-column prop="reviewer" label="评审人" width="100" />
        <el-table-column prop="comment" label="评语" min-width="180" show-overflow-tooltip />
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="appealStatus" label="申诉状态" width="95">
          <template #default="{row}">
            <el-tag v-if="row.appealStatus" :type="row.appealStatus === '已受理' ? 'warning' : 'info'" size="small">{{ row.appealStatus }}</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="评估结果详情" width="680px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="员工姓名">{{ detailRow.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ detailRow.department }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ detailRow.position }}</el-descriptions-item>
        <el-descriptions-item label="评估周期">{{ detailRow.period || '—' }}</el-descriptions-item>
        <el-descriptions-item label="评估得分">{{ detailRow.score }} 分</el-descriptions-item>
        <el-descriptions-item label="评级">
          <el-tag :type="getGradeType(detailRow.grade)" size="small">{{ detailRow.grade }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="排名">{{ detailRow.rank != null ? `第${detailRow.rank}名` : '—' }}</el-descriptions-item>
        <el-descriptions-item label="评审人">{{ detailRow.reviewer }}</el-descriptions-item>
        <el-descriptions-item label="评语" :span="2">{{ detailRow.comment }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import { Download, User, TrendCharts, Medal, WarningFilled } from '@element-plus/icons-vue'
import ImportExportBar from '@/components/ImportExportBar.vue'

const loading = ref(false)
const resultList = ref([])
const queryPeriod = ref('')
const queryDept = ref('')
const queryGrade = ref('')
const detailVisible = ref(false)
const detailRow = ref({})

const ioColumns = [
  { key: 'employeeName', title: '员工姓名' },
  { key: 'department', title: '部门' },
  { key: 'position', title: '岗位' },
  { key: 'period', title: '评估周期' },
  { key: 'score', title: '评估得分' },
  { key: 'grade', title: '评级' },
  { key: 'rank', title: '排名' },
  { key: 'reviewer', title: '评审人' },
  { key: 'comment', title: '评语' }
]

const filteredList = computed(() => resultList.value.filter(r => {
  if (queryPeriod.value && r.period !== queryPeriod.value) return false
  if (queryDept.value && r.department !== queryDept.value) return false
  if (queryGrade.value && r.grade !== queryGrade.value) return false
  return true
}))

const deptOptions = computed(() => [...new Set(resultList.value.map(r => r.department).filter(Boolean))])

async function loadData() {
  loading.value = true
  try {
    const params = { page: 1, size: 100 }
    const res = await axios.get('/api/perf/archive/list', { params })
    const data = res.data
    if (data && data.code === 200) {
      const payload = data.data
      if (Array.isArray(payload)) resultList.value = payload
      else if (payload && Array.isArray(payload.records)) resultList.value = payload.records
      else resultList.value = []
    } else if (data && Array.isArray(data.records)) resultList.value = data.records
    else if (Array.isArray(data)) resultList.value = data
    else resultList.value = []
  } catch (e) {
    ElMessage.error('加载评估结果失败')
  } finally {
    loading.value = false
  }
}

const totalCount = computed(() => filteredList.value.length)
const avgScore = computed(() => {
  if (filteredList.value.length === 0) return 0
  const total = filteredList.value.reduce((sum, item) => sum + (Number(item.score) || 0), 0)
  return Math.round(total / filteredList.value.length)
})
const excellentCount = computed(() => filteredList.value.filter(r => r.grade === 'S' || r.grade === 'A').length)

// 强制分布: S5% A15% B60% C15% D5%，容差±3%
const FORCED = { S: 5, A: 15, B: 60, C: 15, D: 5 }
const gradeDistribution = computed(() => {
  const colors = { S: '#b88230', A: '#67c23a', B: '#409eff', C: '#e6a23c', D: '#f56c6c' }
  const total = filteredList.value.length
  return ['S', 'A', 'B', 'C', 'D'].map(g => {
    const count = filteredList.value.filter(r => r.grade === g).length
    const percentage = total > 0 ? Math.round((count / total) * 100) : 0
    const required = FORCED[g]
    const valid = total === 0 || Math.abs(percentage - required) <= 3
    return { grade: g, count, percentage, required, valid, color: colors[g] }
  })
})
const distributionValid = computed(() => gradeDistribution.value.every(g => g.valid))

const deptAvgScore = computed(() => {
  const map = {}
  filteredList.value.forEach(item => {
    const dept = item.department || '未知'
    if (!map[dept]) map[dept] = { department: dept, totalScore: 0, count: 0 }
    map[dept].totalScore += Number(item.score) || 0
    map[dept].count += 1
  })
  return Object.values(map).map(d => ({
    department: d.department,
    avgScore: d.count > 0 ? Math.round(d.totalScore / d.count) : 0,
    count: d.count
  }))
})

function getScoreColor(score) {
  if (score >= 90) return '#67c23a'
  if (score >= 80) return '#409eff'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}
function getScoreTagType(score) {
  if (score >= 90) return 'success'
  if (score >= 80) return 'primary'
  if (score >= 60) return 'warning'
  return 'danger'
}
function getGradeType(grade) {
  const types = { S: 'danger', A: 'success', B: 'primary', C: 'warning', D: 'danger' }
  return types[grade] || 'info'
}

function exportResult() {
  ElMessage.success('请使用导入导出工具条导出评估结果')
}
function viewDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

onMounted(() => { loadData() })
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.card-title { margin: 0 0 16px; font-size: 15px; font-weight: 600; }

.stat-card { display: flex; align-items: center; gap: 16px; padding: 20px; }
.stat-icon { width: 56px; height: 56px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28px; background: #e3f2fd; color: #409eff; }
.stat-card.success .stat-icon { background: #e8f5e9; color: #67c23a; }
.stat-card.warning .stat-icon { background: #fef7e8; color: #e6a23c; }
.stat-card.danger .stat-icon { background: #fef0f0; color: #f56c6c; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { color: #909399; font-size: 14px; margin-top: 4px; }

.grade-item { text-align: center; }
.grade-label { font-weight: bold; margin-bottom: 8px; }
.grade-bar { height: 24px; background: #f0f0f0; border-radius: 12px; overflow: hidden; }
.grade-fill { height: 100%; transition: width 0.3s; }
.grade-count { font-size: 12px; color: #666; margin-top: 4px; }
.grade-required { font-size: 12px; margin-top: 2px; }
</style>

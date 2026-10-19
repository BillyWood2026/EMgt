<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">明细钻取报表：筛选 → 导出（需二次确认，留痕审计）→ 点击行查看下钻抽屉</el-tag>
    </RoleBar>

    <div class="bar mt">
      <el-select v-model="query.type" size="small" style="width:210px" @change="load">
        <el-option v-for="t in TYPES" :key="t.value" :label="t.label" :value="t.value" />
      </el-select>
      <el-select v-model="query.org" size="small" style="width:180px" clearable placeholder="全集团" @change="load">
        <el-option v-for="o in ORGS" :key="o" :label="o" :value="o" />
      </el-select>
      <el-input v-model="query.keyword" size="small" placeholder="本地过滤关键字" clearable style="width:180px" />
      <el-button size="small" @click="load">查询</el-button>
      <el-button size="small" type="primary" @click="doExport">导出CSV</el-button>
      <el-tag size="small" effect="plain">{{ rows.length }} 行</el-tag>
    </div>

    <el-table :data="filtered" size="small" border v-loading="loading" class="mt" max-height="560" @row-click="openRow">
      <el-table-column type="index" width="44" />
      <el-table-column v-for="(h, i) in colHeaders" :key="h" :prop="'col' + (i + 1)" :label="h" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">
          <el-progress v-if="progressCol === i" :percentage="Number(row['col' + (i + 1)]) || 0" :stroke-width="8" />
          <template v-else>{{ row['col' + (i + 1)] }}</template>
        </template>
      </el-table-column>
    </el-table>

    <!-- 指标明细下钻抽屉 -->
    <el-drawer v-model="drawer" :title="'明细下钻 · ' + typeLabel" size="420px">
      <el-descriptions :column="1" border size="small" v-if="current">
        <el-descriptions-item v-for="(h, i) in colHeaders" :key="h" :label="h">{{ current['col' + (i + 1)] }}</el-descriptions-item>
      </el-descriptions>
      <el-tag size="small" type="warning" effect="plain" style="margin-top:12px">
        敏感明细仅管理层可导出，导出操作已留痕审计
      </el-tag>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'

const route = useRoute()
const ORGS = ['集团总部', '华东子公司']

const TYPES = [
  { value: 'study', label: '学习完成明细' },
  { value: 'exam', label: '考试结果明细' },
  { value: 'survey', label: '调研回收明细' },
  { value: 'case', label: '案例投稿明细' },
  { value: 'activity', label: '活动参与明细' },
  { value: 'news', label: '新闻产出明细' },
  { value: 'asset', label: '资产浏览明细' },
  { value: 'score', label: '文化积分台账' },
  { value: 'duty', label: '干部履职明细' },
  { value: 'alert_study_overdue', label: '预警-学习逾期部门' },
  { value: 'alert_survey_low', label: '预警-回收率过低' },
  { value: 'alert_low_score', label: '预警-低分人员' },
  { value: 'alert_rectify_overdue', label: '预警-整改逾期' },
  { value: 'alert_news_pending', label: '预警-新闻待审超时' }
]

const HEADERS = {
  study: ['任务', '成员', '部门', '进度%', '状态', '完成时间'],
  exam: ['考试', '成员', '部门', '状态', '分数', '已考次数'],
  survey: ['问卷', '填写人', '部门', '提交时间'],
  case: ['案例', '投稿人', '组织', '状态', '积分', '发布时间'],
  activity: ['活动ID', '成员', '部门', '报名状态', '签到状态', '报名时间'],
  news: ['标题', '组织', '状态', '阅读量', '发布时间'],
  asset: ['资产', '分类', '浏览量', '下载量', '状态'],
  score: ['成员', '部门', '综合分', '加分次数', '扣分次数', '核算时间'],
  duty: ['周期', '干部', '部门', '状态'],
  alert_study_overdue: ['部门', '任务', '未完成人数'],
  alert_survey_low: ['问卷', '目标数', '已回收', '回收率%'],
  alert_low_score: ['成员', '部门', '综合分'],
  alert_rectify_overdue: ['整改项', '部门', '截止日', '状态'],
  alert_news_pending: ['标题', '组织', '提交时间', '滞留小时']
}

const loading = ref(false)
const rows = ref([])
const query = reactive({ type: route.query.type || 'study', org: route.query.org || '', keyword: '' })
const drawer = ref(false)
const current = ref(null)

const colHeaders = computed(() => HEADERS[query.type] || [])
const typeLabel = computed(() => TYPES.find(t => t.value === query.type)?.label || query.type)
const progressCol = computed(() => (query.type === 'study' ? 3 : -1))

const filtered = computed(() => {
  const k = query.keyword.trim()
  if (!k) return rows.value
  return rows.value.filter(r => Object.values(r).some(v => String(v ?? '').includes(k)))
})

async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/dashboard/drill', { params: { type: query.type, org: query.org || undefined } })
    rows.value = r.data.data.rows || []
  } catch (e) {
    rows.value = []
    ElMessage.error(e.response?.data?.message || '加载明细失败')
  } finally {
    loading.value = false
  }
}

function openRow(row) {
  current.value = row
  drawer.value = true
}

async function doExport() {
  try {
    await ElMessageBox.confirm(
      '导出为敏感操作，将写入审计日志（操作人/角色/行数/文件名），确认导出？',
      '导出二次确认', { type: 'warning', confirmButtonText: '确认导出', cancelButtonText: '取消' }
    )
  } catch { return }
  try {
    const r = await axios.get('/api/culture/dashboard/drill', {
      params: { type: query.type, org: query.org || undefined, export: true, confirm: true }
    })
    const d = r.data.data
    const blob = new Blob([d.csv], { type: 'text/csv;charset=utf-8' })
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = d.fileName
    a.click()
    URL.revokeObjectURL(a.href)
    ElMessage.success(`已导出 ${d.rows} 行 → ${d.fileName}（已留痕审计）`)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '导出失败')
  }
}

onMounted(load)
</script>

<style scoped>
.bar { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.mt { margin-top: 12px; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">分组织文化看板：按组织筛选钻取本组织全部指标 · 子公司专员仅限本组织</el-tag>
    </RoleBar>

    <div class="bar mt">
      <el-select v-model="org" size="small" style="width:220px" @change="load">
        <el-option label="全集团" value="" />
        <el-option v-for="o in ORGS" :key="o" :label="o" :value="o" />
      </el-select>
      <el-button size="small" @click="load">刷新</el-button>
      <el-tag v-if="org" size="small" type="primary" effect="plain">当前：{{ org }}</el-tag>
    </div>

    <el-alert v-if="denied" type="error" :closable="false" class="mt"
              title="无权查看该组织数据（E-DB-02）" description="子公司文化专员仅可查看本组织看板" />

    <template v-if="data && !denied">
      <el-row :gutter="10" class="mt">
        <el-col :span="3" v-for="c in topCards" :key="c.label">
          <el-card shadow="hover" class="kpi" @click="goDrill(c.drill)">
            <div class="t">{{ c.label }}</div>
            <div class="n" :style="{ color: c.color }">{{ c.value }}<span class="u">{{ c.unit }}</span></div>
            <div class="s">{{ c.sub }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="10" class="mt">
        <el-col :span="14">
          <el-card shadow="never">
            <template #header><b>各部门文化综合得分</b></template>
            <div ref="deptRef" class="chart" />
          </el-card>
        </el-col>
        <el-col :span="10">
          <el-card shadow="never">
            <template #header><b>文化积分分布</b></template>
            <div ref="distRef" class="chart" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 五类预警 -->
      <div class="sec-title">本组织智能预警</div>
      <el-row :gutter="10">
        <el-col :span="4" v-for="a in alertCards" :key="a.label">
          <el-card shadow="hover" class="kpi" :class="{ danger: a.count > 0 }" @click="goDrill(a.drill)">
            <div class="t">{{ a.label }}</div>
            <div class="n" :style="{ color: a.count > 0 ? '#e64242' : '#999' }">{{ a.count }}<span class="u">项</span></div>
            <div class="s">{{ a.count > 0 ? '点击查看明细' : '暂无风险' }}</div>
          </el-card>
        </el-col>
      </el-row>

      <div class="sec-title">干部履职完成率（按部门）</div>
      <el-table :data="data.duty?.byDept || []" size="small" border>
        <el-table-column prop="dept" label="部门" min-width="180" />
        <el-table-column prop="submitted" label="已提交" width="90" align="center" />
        <el-table-column prop="total" label="应提交" width="90" align="center" />
        <el-table-column label="完成率" width="140" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.total === 0 || row.submitted >= row.total ? 'success' : 'warning'">
              {{ row.total === 0 ? '0%' : Math.round(row.submitted * 100 / row.total) + '%' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import * as echarts from 'echarts'

const router = useRouter()
const ORGS = ['集团总部', '华东子公司']
const org = ref('')
const data = ref(null)
const denied = ref(false)
const charts = []

const topCards = computed(() => {
  const k = data.value?.kpi || {}
  return [
    { label: '学习完成率', value: k.study?.rate ?? '-', unit: '%', color: '#2f6fdd', drill: 'study', sub: `${k.study?.done ?? 0}/${k.study?.total ?? 0}` },
    { label: '考试通过率', value: k.exam?.rate ?? '-', unit: '%', color: '#2f6fdd', drill: 'exam', sub: `${k.exam?.passed ?? 0}/${k.exam?.graded ?? 0}` },
    { label: '问卷参与率', value: k.survey?.rate ?? '-', unit: '%', color: '#2f6fdd', drill: 'survey', sub: `${k.survey?.responses ?? 0}/${k.survey?.target ?? 0}` },
    { label: '氛围指数', value: data.value?.climateIndex ?? '-', unit: '', color: '#67c23a', drill: 'survey', sub: '维度分均值' },
    { label: '案例投稿', value: k.caseTotal ?? 0, unit: '篇', color: '#e6a23c', drill: 'case', sub: '全员投稿' },
    { label: '活动参与', value: k.activityJoin ?? 0, unit: '人次', color: '#e6a23c', drill: 'activity', sub: '报名合计' },
    { label: '新闻浏览', value: k.news?.reads ?? 0, unit: '次', color: '#e6a23c', drill: 'news', sub: `产出${k.news?.published ?? 0}篇` },
    { label: '资产浏览', value: k.asset?.views ?? 0, unit: '次', color: '#e6a23c', drill: 'asset', sub: `下载${k.asset?.downloads ?? 0}` },
    { label: '部门达标率', value: data.value?.deptPassRate?.rate ?? '-', unit: '%', color: '#67c23a', drill: 'score', sub: `达标线${data.value?.deptPassRate?.passLine ?? 80}分` },
    { label: '履职完成率', value: data.value?.duty?.rate ?? '-', unit: '%', color: '#67c23a', drill: 'duty', sub: `${data.value?.duty?.submitted ?? 0}/${data.value?.duty?.total ?? 0}` }
  ]
})

const alertCards = computed(() => {
  const a = data.value?.alerts || {}
  return [
    { label: '学习逾期预警', count: (a.studyOverdue || []).length, drill: 'alert_study_overdue' },
    { label: '回收率过低预警', count: (a.surveyLow || []).length, drill: 'alert_survey_low' },
    { label: '低分人员预警', count: (a.lowScore || []).length, drill: 'alert_low_score' },
    { label: '整改逾期预警', count: (a.rectifyOverdue || []).length, drill: 'alert_rectify_overdue' },
    { label: '新闻待审超时', count: (a.newsPending || []).length, drill: 'alert_news_pending' }
  ]
})

function goDrill(type) {
  router.push({ path: '/culture-mgt/dashboard/drill', query: { type, org: org.value || undefined } })
}

function renderCharts() {
  const d = data.value || {}
  charts.push(echarts.init(document.querySelectorAll('.chart')[0]), echarts.init(document.querySelectorAll('.chart')[1]))
  const ds = (d.deptScores || []).map(x => ({ name: x.dept, value: x.avgScore }))
  charts[0].setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, bottom: 60, top: 20 },
    xAxis: { type: 'category', data: ds.map(x => x.name), axisLabel: { interval: 0, rotate: 20, fontSize: 10 } },
    yAxis: { type: 'value', max: 100 },
    series: [{ type: 'bar', data: ds.map(x => x.value), itemStyle: { color: '#2f6fdd' }, label: { show: true, position: 'top' } }]
  })
  charts[1].setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['35%', '65%'], data: (d.scoreDist || []).map(x => ({ name: x.bucket, value: x.num })), label: { formatter: '{b}: {c}' } }]
  })
}

async function load() {
  denied.value = false
  try {
    const r = await axios.get('/api/culture/dashboard/org', { params: org.value ? { org: org.value } : {} })
    data.value = r.data.data
    charts.forEach(c => c.dispose())
    charts.length = 0
    await nextTick()
    renderCharts()
  } catch (e) {
    const code = e.response?.data?.data?.errorCode || ''
    if (code === 'E-DB-02' || e.response?.status === 403 || (e.response?.data?.msg || '').includes('E-DB-02')) {
      denied.value = true
      data.value = null
    } else if (code === 'E-DB-04') {
      ElMessage.warning('请选择组织（子公司专员仅可查看本组织）')
    } else {
      ElMessage.error(e.response?.data?.message || '加载失败')
    }
  }
}

function onResize() { charts.forEach(c => c.resize()) }

onMounted(() => {
  load()
  window.addEventListener('resize', onResize)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  charts.forEach(c => c.dispose())
})
</script>

<style scoped>
.sec-title { font-weight: 700; margin: 12px 0 6px; font-size: 13px; color: #333; }
.kpi { cursor: pointer; }
.kpi:hover { transform: translateY(-2px); }
.kpi.danger { border-color: #f3b2b2; }
.kpi .t { font-size: 12px; color: #666; }
.kpi .n { font-size: 22px; font-weight: 700; margin: 4px 0; }
.kpi .u { font-size: 12px; font-weight: 400; margin-left: 3px; color: #999; }
.kpi .s { font-size: 11px; color: #999; }
.chart { height: 280px; }
.bar { display: flex; gap: 8px; align-items: center; }
.mt { margin-top: 12px; }
</style>

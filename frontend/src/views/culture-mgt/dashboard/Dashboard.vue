<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">集团文化总看板（高管大屏）：跨模块聚合 · 点击指标卡/预警卡可下钻至明细报表 · 🔒指标仅管理层可见</el-tag>
    </RoleBar>

    <!-- 覆盖度 -->
    <div class="sec-title">覆盖度</div>
    <el-row :gutter="10">
      <el-col :span="6" v-for="c in coverCards" :key="c.label">
        <el-card shadow="hover" class="kpi" @click="goDrill(c.drill)">
          <div class="t">{{ c.label }} <el-tag size="small" effect="plain" type="warning">🔒</el-tag></div>
          <div class="n" :style="{ color: c.color }">{{ c.value }}<span class="u">{{ c.unit }}</span></div>
          <div class="s">{{ c.sub }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 活跃度 -->
    <div class="sec-title">活跃度</div>
    <el-row :gutter="10">
      <el-col :span="3" v-for="c in activeCards" :key="c.label">
        <el-card shadow="hover" class="kpi" @click="goDrill(c.drill)">
          <div class="t">{{ c.label }} <el-tag size="small" effect="plain" type="warning">🔒</el-tag></div>
          <div class="n" :style="{ color: c.color }">{{ c.value }}<span class="u">{{ c.unit }}</span></div>
          <div class="s">{{ c.sub }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 文化健康 + 干部履职 -->
    <div class="sec-title">文化健康与干部履职</div>
    <el-row :gutter="10">
      <el-col :span="6" v-for="c in healthCards" :key="c.label">
        <el-card shadow="hover" class="kpi" @click="goDrill(c.drill)">
          <div class="t">{{ c.label }} <el-tag size="small" effect="plain" type="warning">🔒</el-tag></div>
          <div class="n" :style="{ color: c.color }">{{ c.value }}<span class="u">{{ c.unit }}</span></div>
          <div class="s">{{ c.sub }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 五类智能预警卡 -->
    <div class="sec-title">智能预警</div>
    <el-row :gutter="10">
      <el-col :span="4" v-for="a in alertCards" :key="a.label">
        <el-card shadow="hover" class="kpi" :class="{ danger: a.count > 0 }" @click="goDrill(a.drill)">
          <div class="t">{{ a.label }}</div>
          <div class="n" :style="{ color: a.count > 0 ? '#e64242' : '#999' }">{{ a.count }}<span class="u">项</span></div>
          <div class="s">{{ a.count > 0 ? '点击查看明细' : '暂无风险' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 -->
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
const data = ref(null)
const charts = []

const coverCards = computed(() => {
  const k = data.value?.kpi || {}
  return [
    { label: '文化学习完成率', value: k.study?.rate ?? '-', unit: '%', sub: `已完成 ${k.study?.done ?? 0} / ${k.study?.total ?? 0} 人次`, color: '#2f6fdd', drill: 'study' },
    { label: '考试通过率', value: k.exam?.rate ?? '-', unit: '%', sub: `合格 ${k.exam?.passed ?? 0} / ${k.exam?.graded ?? 0} 人次`, color: '#2f6fdd', drill: 'exam' },
    { label: '调研问卷参与率', value: k.survey?.rate ?? '-', unit: '%', sub: `回收 ${k.survey?.responses ?? 0} / 目标 ${k.survey?.target ?? 0}`, color: '#2f6fdd', drill: 'survey' },
    { label: '氛围指数', value: data.value?.climateIndex ?? '-', unit: '', sub: '全部问卷维度分均值（0-100）', color: '#67c23a', drill: 'survey' }
  ]
})

const activeCards = computed(() => {
  const k = data.value?.kpi || {}
  return [
    { label: '案例投稿总数', value: k.caseTotal ?? 0, unit: '篇', sub: '全员文化案例', color: '#e6a23c', drill: 'case' },
    { label: '活动参与人次', value: k.activityJoin ?? 0, unit: '人次', sub: '文化活动报名合计', color: '#e6a23c', drill: 'activity' },
    { label: '新闻产出', value: k.news?.published ?? 0, unit: '篇', sub: `总浏览 ${k.news?.reads ?? 0} 次`, color: '#e6a23c', drill: 'news' },
    { label: '资产浏览量', value: k.asset?.views ?? 0, unit: '次', sub: `下载 ${k.asset?.downloads ?? 0} 次`, color: '#e6a23c', drill: 'asset' },
    { label: '积分人数', value: k.score?.total ?? 0, unit: '人', sub: `低分 ${k.score?.low ?? 0} 人`, color: '#e6a23c', drill: 'score' }
  ]
})

const healthCards = computed(() => {
  const d = data.value || {}
  return [
    { label: '部门综合得分', value: topDept?.value ?? '-', unit: '分', sub: topDeptName.value, color: '#67c23a', drill: 'score' },
    { label: '部门文化达标率', value: d.deptPassRate?.rate ?? '-', unit: '%', sub: `达标 ${d.deptPassRate?.pass ?? 0} / ${d.deptPassRate?.total ?? 0} 部门（达标线 ${d.deptPassRate?.passLine ?? 80} 分）`, color: '#67c23a', drill: 'score' },
    { label: '干部履职完成率', value: d.duty?.rate ?? '-', unit: '%', sub: `已提交 ${d.duty?.submitted ?? 0} / ${d.duty?.total ?? 0}`, color: '#67c23a', drill: 'duty' },
    { label: '积分分布', value: (d.scoreDist || []).length, unit: '档', sub: '优秀/良好/合格/低分', color: '#67c23a', drill: 'score' }
  ]
})

const topDept = computed(() => data.value?.deptScores?.[0]?.avgScore)
const topDeptName = computed(() => data.value?.deptScores?.[0]?.dept || '暂无数据')

const alertCards = computed(() => {
  const a = data.value?.alerts || {}
  return [
    { label: '学习逾期部门预警', count: (a.studyOverdue || []).length, drill: 'alert_study_overdue' },
    { label: '调研回收率过低预警', count: (a.surveyLow || []).length, drill: 'alert_survey_low' },
    { label: '文化低分人员预警', count: (a.lowScore || []).length, drill: 'alert_low_score' },
    { label: '整改逾期预警', count: (a.rectifyOverdue || []).length, drill: 'alert_rectify_overdue' },
    { label: '新闻待审超时预警', count: (a.newsPending || []).length, drill: 'alert_news_pending' }
  ]
})

function goDrill(type) {
  router.push({ path: '/culture-mgt/dashboard/drill', query: { type } })
}

function renderCharts() {
  const d = data.value || {}
  const deptC = echarts.init(document.querySelectorAll('.chart')[0])
  const distC = echarts.init(document.querySelectorAll('.chart')[1])
  charts.push(deptC, distC)
  const ds = (d.deptScores || []).map(x => ({ name: x.dept, value: x.avgScore }))
  deptC.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, bottom: 60, top: 20 },
    xAxis: { type: 'category', data: ds.map(x => x.name), axisLabel: { interval: 0, rotate: 20, fontSize: 10 } },
    yAxis: { type: 'value', max: 100 },
    series: [{ type: 'bar', data: ds.map(x => x.value), itemStyle: { color: '#2f6fdd' }, label: { show: true, position: 'top' } }]
  })
  distC.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['35%', '65%'], data: (d.scoreDist || []).map(x => ({ name: x.bucket, value: x.num })), label: { formatter: '{b}: {c}' } }]
  })
}

async function load() {
  try {
    const r = await axios.get('/api/culture/dashboard/overview')
    data.value = r.data.data
    await nextTick()
    renderCharts()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '加载看板失败')
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
.sec-title:first-of-type { margin-top: 4px; }
.kpi { cursor: pointer; }
.kpi:hover { transform: translateY(-2px); }
.kpi.danger { border-color: #f3b2b2; }
.kpi .t { font-size: 12px; color: #666; }
.kpi .n { font-size: 26px; font-weight: 700; margin: 4px 0; }
.kpi .u { font-size: 12px; font-weight: 400; margin-left: 3px; color: #999; }
.kpi .s { font-size: 11px; color: #999; }
.chart { height: 300px; }
.mt { margin-top: 12px; }
</style>

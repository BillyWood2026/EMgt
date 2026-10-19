<template>
  <div class="dashboard">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">大屏观态势 · 点击指标卡可下钻至明细 · 数据与管理后台/移动端同源实时一致</el-tag>
      <el-switch v-model="autoRotate" active-text="自动轮播" style="margin-left:8px" />
    </RoleBar>

    <!-- 核心指标卡 -->
    <el-row :gutter="12">
      <el-col :span="3" v-for="c in cards" :key="c.label">
        <el-card shadow="hover" class="kpi" @click="drill(c.drill)">
          <div class="t">{{ c.label }}</div>
          <div class="n" :style="{ color: c.color }">{{ c.value }}<span class="u">{{ c.unit }}</span></div>
          <div class="s">{{ c.sub }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top:12px">
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><b>近12个月活动趋势</b></template>
          <div ref="trendRef" class="chart" />
        </el-card>
      </el-col>
      <el-col :span="7">
        <el-card shadow="never">
          <template #header><b>组织分布（按类型）</b></template>
          <div ref="orgRef" class="chart" />
        </el-card>
      </el-col>
      <el-col :span="7">
        <el-card shadow="never">
          <template #header><b>考核结果分布</b></template>
          <div ref="gradeRef" class="chart" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top:12px">
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><b>支部完成率 TOP10</b></template>
          <el-table :data="rank.branchRank" size="small" :show-header="false">
            <el-table-column type="index" width="40" />
            <el-table-column prop="orgName" show-overflow-tooltip />
            <el-table-column label="" width="90" align="right">
              <template #default="{ row }"><el-tag size="small" :type="row.qualifyRate>=100?'success':'warning'">{{ row.qualifyRate }}%</el-tag></template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!rank.branchRank?.length" description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><b>党员积分 TOP10</b></template>
          <el-table :data="rank.memberScoreRank" size="small" :show-header="false">
            <el-table-column type="index" width="40" />
            <el-table-column prop="memberName" width="90" />
            <el-table-column prop="orgName" show-overflow-tooltip />
            <el-table-column label="" width="70" align="right">
              <template #default="{ row }"><b style="color:#2f6fdd">{{ row.totalScore }}</b></template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!rank.memberScoreRank?.length" description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><b>考核排名 TOP10</b></template>
          <el-table :data="rank.assessRank" size="small" :show-header="false">
            <el-table-column label="#" width="40">
              <template #default="{ row }">{{ row.rankNo || '-' }}</template>
            </el-table-column>
            <el-table-column prop="orgName" show-overflow-tooltip />
            <el-table-column prop="totalScore" width="70" align="right" />
            <el-table-column label="" width="70">
              <template #default="{ row }"><el-tag v-if="row.grade" size="small" :type="{ '优秀':'success','良好':'primary','合格':'warning','不合格':'danger' }[row.grade]">{{ row.grade }}</el-tag></template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!rank.assessRank?.length" description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from '@/api/axiosConfig'
import * as echarts from 'echarts'
import RoleBar from '../PartyRoleBar.vue'

const router = useRouter()
const api = '/api/party/stats/dashboard'
const ov = ref({})
const rank = ref({})
const autoRotate = ref(false)
const trendRef = ref(null)
const orgRef = ref(null)
const gradeRef = ref(null)
let charts = []
let rotateTimer = null

const cards = computed(() => [
  { label: '党组织数', value: ov.value.orgCount ?? '-', unit: '个', color: '#2f6fdd', sub: '正常状态组织', drill: '/party-mgt/org/organization' },
  { label: '党员总数', value: ov.value.memberCount ?? '-', unit: '人', color: '#2f6fdd', sub: `正式 ${ov.value.fullMemberCount ?? 0} · 预备 ${ov.value.probationCount ?? 0}`, drill: '/party-mgt/member/info' },
  { label: '三会一课完成率', value: ov.value.meetingRate ?? '-', unit: '%', color: ov.value.meetingRate >= 100 ? '#389e0d' : '#d48806', sub: '年度平均', drill: '/party-mgt/activity/assessment' },
  { label: '党费收缴率', value: ov.value.duesRate ?? '-', unit: '%', color: ov.value.duesRate >= 100 ? '#389e0d' : '#d48806', sub: '实缴/应缴', drill: '/party-mgt/membership/dues-publication' },
  { label: '发展党员', value: `${ov.value.devFull ?? 0}/${ov.value.devTotal ?? 0}`, unit: '', color: '#722ed1', sub: '已转正/培养中', drill: '/party-mgt/membership/development' },
  { label: '舆情态势', value: ov.value.opinionNegative ?? 0, unit: '条负面', color: ov.value.opinionNegative > 0 ? '#cf1322' : '#389e0d', sub: `总量 ${ov.value.opinionTotal ?? 0} · 未闭环预警 ${ov.value.opinionAlertOpen ?? 0}`, drill: '/party-mgt/opinion/analysis' },
  { label: '考核均分', value: ov.value.assessAvg ?? '-', unit: '分', color: '#2f6fdd', sub: `优秀 ${ov.value.assessExcellent ?? 0} · 不合格 ${ov.value.assessFail ?? 0}`, drill: '/party-mgt/assessment/result' },
  { label: '数据下钻', value: '明细', unit: '', color: '#5a6a7e', sub: '逐级下钻至单据', drill: '/party-mgt/stats/drill' }
])

function drill(path) { if (path) router.push(path) }

async function load() {
  const [o, t, r, d, g] = await Promise.all([
    axios.get(`${api}/overview`),
    axios.get(`${api}/trend`),
    axios.get(`${api}/rank`),
    axios.get(`${api}/org-distribution`),
    axios.get('/api/party/stats/report/assessment')
  ])
  ov.value = o.data.data || {}
  rank.value = r.data.data || {}
  await nextTick()
  renderTrend(t.data.data || [])
  renderOrg(d.data.data || [])
  renderGrade((g.data.data && g.data.data.rows) || [])
}

function makeChart(el, option) {
  if (!el) return
  const c = echarts.init(el)
  c.setOption(option)
  charts.push(c)
}
function renderTrend(rows) {
  makeChart(trendRef.value, {
    grid: { left: 40, right: 16, top: 30, bottom: 24 },
    tooltip: { trigger: 'axis' },
    legend: { data: ['活动场次', '签到人次'], top: 0 },
    xAxis: { type: 'category', data: rows.map(r => r.ym) },
    yAxis: { type: 'value' },
    series: [
      { name: '活动场次', type: 'bar', data: rows.map(r => r.activityCount), itemStyle: { color: '#2f6fdd' } },
      { name: '签到人次', type: 'line', smooth: true, data: rows.map(r => r.signCount), itemStyle: { color: '#389e0d' } }
    ]
  })
}
function renderOrg(rows) {
  makeChart(orgRef.value, {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['40%', '65%'], label: { formatter: '{b}\n{c}个' },
      data: rows.map(r => ({ name: r.orgType, value: r.orgCount })) }]
  })
}
function renderGrade(rows) {
  const order = ['优秀', '良好', '合格', '不合格']
  const cnt = {}
  rows.forEach(r => { if (r.grade) cnt[r.grade] = (cnt[r.grade] || 0) + 1 })
  makeChart(gradeRef.value, {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['40%', '65%'],
      data: order.map(k => ({ name: k, value: cnt[k] || 0, itemStyle: { color: { '优秀': '#389e0d', '良好': '#2f6fdd', '合格': '#d48806', '不合格': '#cf1322' }[k] } })) }]
  })
}

watch(autoRotate, v => {
  if (rotateTimer) { clearInterval(rotateTimer); rotateTimer = null }
  if (v) rotateTimer = setInterval(load, 30000)
})
onMounted(load)
onBeforeUnmount(() => { charts.forEach(c => c.dispose()); charts = []; if (rotateTimer) clearInterval(rotateTimer) })
</script>

<style scoped>
.dashboard { padding: 12px; }
.kpi { text-align: center; cursor: pointer; }
.kpi .t { color: #5a6a7e; font-size: 13px; }
.kpi .n { font-size: 26px; font-weight: 600; margin: 6px 0; }
.kpi .u { font-size: 12px; margin-left: 2px; color: #8a97a8; }
.kpi .s { color: #8a97a8; font-size: 12px; }
.chart { height: 260px; }
</style>

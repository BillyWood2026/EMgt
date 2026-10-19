<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">文化健康度（算法61）：氛围综合指数=∑维度得分×权重；优秀≥85 / 良好70-84 / 一般60-69 / 薄弱&lt;60；维度低于70分标记短板</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <span>调研问卷/周期：</span>
        <el-select v-model="qid" size="small" style="width:300px" filterable @change="loadOverview">
          <el-option v-for="q in qOptions" :key="q.id" :label="q.title" :value="q.id" />
        </el-select>
        <el-button size="small" @click="loadOverview">刷新分析</el-button>
        <span style="flex:1"></span>
        <el-tag size="small" type="info" effect="plain">高风险：薄弱｜中风险：一般｜低风险：良好及以上</el-tag>
      </div>
    </el-card>

    <el-row :gutter="14" v-loading="loading">
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><b>集团整体氛围指数</b></template>
          <div class="overall">
            <div class="big-index" :class="idxClass(overview.overall?.index)">{{ overview.overall?.index ?? '-' }}</div>
            <div class="overall-tags">
              <el-tag :type="gradeType(overview.overall?.grade)" size="large">{{ overview.overall?.grade || '-' }}</el-tag>
              <el-tag :type="riskType(overview.overall?.riskLevel)" size="large" effect="dark">{{ overview.overall?.riskLevel || '-' }}</el-tag>
            </div>
            <div class="overall-sub">{{ overview.questionnaireTitle || '' }}</div>
            <div class="overall-sub">参与 {{ overview.overall?.headcount || 0 }} 人 · 参与率 {{ overview.overall?.participationRate || 0 }}%</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="never">
          <template #header><b>多周期氛围指数趋势</b></template>
          <div ref="trendChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="mt">
      <template #header><b>部门健康度排名（指数升序，薄弱部门优先整改）</b></template>
      <el-table :data="overview.depts || []" size="small" border>
        <el-table-column type="index" label="排名" width="60" align="center" />
        <el-table-column prop="dept" label="参与部门" width="160" show-overflow-tooltip />
        <el-table-column prop="headcount" label="参与人数" width="80" align="center" />
        <el-table-column label="参与率" width="170">
          <template #default="{row}">
            <el-progress :percentage="Number(row.participationRate || 0)" :stroke-width="10" />
          </template>
        </el-table-column>
        <el-table-column label="综合指数" width="150">
          <template #default="{row}">
            <el-progress :percentage="Number(row.index || 0)" :stroke-width="12" :status="row.index >= 85 ? 'success' : row.index < 60 ? 'exception' : ''" />
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="等级" width="80" align="center">
          <template #default="{row}"><el-tag size="small" :type="gradeType(row.grade)">{{ row.grade }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="90" align="center">
          <template #default="{row}"><el-tag size="small" :type="riskType(row.riskLevel)" effect="dark">{{ row.riskLevel }}</el-tag></template>
        </el-table-column>
        <el-table-column label="短板标签" min-width="200">
          <template #default="{row}">
            <el-tag v-for="t in (row.shortBoardTags || [])" :key="t" size="small" type="danger" effect="plain" class="score-tag">{{ t }}</el-tag>
            <span v-if="!(row.shortBoardTags || []).length" class="ok-text">无明显短板</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{row}">
            <el-button size="small" type="primary" link @click="openDept(row.dept)">部门诊断</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 部门氛围诊断抽屉 ELCUL010 -->
    <el-drawer v-model="deptVis" :title="`部门氛围诊断 - ${detail.dept || ''}`" size="860px"
               @opened="onDrawerOpened" @closed="drawerReady = false">
      <template v-if="detail.dept">
        <el-descriptions :column="4" border size="small" class="mb">
          <el-descriptions-item label="综合指数"><b :class="idxClass(detail.index)">{{ detail.index }}</b></el-descriptions-item>
          <el-descriptions-item label="等级"><el-tag size="small" :type="gradeType(detail.grade)">{{ detail.grade }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="风险"><el-tag size="small" :type="riskType(detail.riskLevel)" effect="dark">{{ detail.riskLevel }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="样本数">{{ detail.headcount }}</el-descriptions-item>
        </el-descriptions>

        <el-tabs v-model="tab" @tab-change="onTabChange">
          <el-tab-pane label="维度得分" name="dim">
            <el-row :gutter="12">
              <el-col :span="13">
                <el-table :data="detail.dimensions || []" size="small" border>
                  <el-table-column prop="dimension" label="维度" min-width="110" />
                  <el-table-column prop="weight" label="权重" width="70" align="center">
                    <template #default="{row}">{{ row.weight }}%</template>
                  </el-table-column>
                  <el-table-column prop="score" label="本期得分" width="90" align="center">
                    <template #default="{row}"><b :class="row.shortBoard ? 'rate-bad' : 'rate-good'">{{ row.score }}</b></template>
                  </el-table-column>
                  <el-table-column label="环比" width="80" align="center">
                    <template #default="{row}">
                      <span v-if="row.delta === null || row.delta === undefined">-</span>
                      <span v-else :class="row.delta >= 0 ? 'rate-good' : 'rate-bad'">{{ row.delta >= 0 ? '+' : '' }}{{ row.delta }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="短板" width="70" align="center">
                    <template #default="{row}">
                      <el-tag v-if="row.shortBoard" size="small" type="danger">短板</el-tag>
                      <span v-else class="ok-text">达标</span>
                    </template>
                  </el-table-column>
                </el-table>
              </el-col>
              <el-col :span="11">
                <el-card shadow="never">
                  <template #header><b>维度雷达</b></template>
                  <div ref="radarChart" class="chart-sm"></div>
                </el-card>
              </el-col>
            </el-row>
          </el-tab-pane>

          <el-tab-pane label="词云预览" name="cloud">
            <el-card shadow="never">
              <template #header><b>开放题高频词（来自文本题与作答内容，Top 40）</b></template>
              <div class="word-cloud">
                <span v-for="w in (detail.wordCloud || [])" :key="w.word" class="word"
                      :style="wordStyle(w.count)">{{ w.word }}</span>
                <el-empty v-if="!(detail.wordCloud || []).length" description="暂无文本题作答内容" :image-size="70" />
              </div>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="题项分布" name="option">
            <el-card v-for="(s, i) in (detail.optionStats || [])" :key="i" shadow="never" class="mb">
              <template #header>{{ s.qid }}. {{ s.stem }}（{{ s.type }}）</template>
              <div v-for="(v, k) in s.counts" :key="k" class="dist-line">
                <span class="dist-dept">{{ k }}</span>
                <el-progress :percentage="optionPct(v, s.counts)" :stroke-width="12" style="flex:1" />
                <span class="dist-num">{{ v }} 票</span>
              </div>
            </el-card>
            <el-empty v-if="!(detail.optionStats || []).length" description="本问卷无单选/多选题" :image-size="70" />
          </el-tab-pane>

          <el-tab-pane label="历史趋势" name="trend">
            <el-card shadow="never">
              <template #header><b>该部门多周期氛围指数</b></template>
              <div ref="deptTrendChart" class="chart"></div>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="关联整改台账" name="rectify">
            <div class="mb">
              <el-button v-if="can('C02','C03','C99')" size="small" type="primary" @click="openCreateRectify">从短板创建整改任务</el-button>
            </div>
            <el-table :data="detail.rectifies || []" size="small" border>
              <el-table-column prop="title" label="整改任务" min-width="200" show-overflow-tooltip />
              <el-table-column prop="sourceTag" label="短板" width="160" show-overflow-tooltip />
              <el-table-column prop="owner" label="责任人" width="100" />
              <el-table-column prop="deadline" label="截止日期" width="110" />
              <el-table-column prop="status" label="状态" width="130">
                <template #default="{row}"><el-tag size="small" :type="rectifyStatusType(row.status)">{{ row.status }}</el-tag></template>
              </el-table-column>
              <el-table-column prop="closeTime" label="闭环时间" width="150">
                <template #default="{row}">{{ row.closeTime ? fmt(row.closeTime) : '-' }}</template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!(detail.rectifies || []).length" description="该部门暂无整改任务" :image-size="70" />
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-drawer>

    <!-- 创建整改任务 -->
    <el-dialog v-model="rectVis" title="创建氛围短板整改任务" width="560px">
      <el-form label-width="96px" size="small">
        <el-form-item label="责任部门">
          <el-input v-model="rect.dept" disabled />
        </el-form-item>
        <el-form-item label="任务名称" required>
          <el-input v-model="rect.title" maxlength="100" show-word-limit placeholder="如 客户服务部行为层短板专项整改" />
        </el-form-item>
        <el-form-item label="短板标签">
          <el-input v-model="rect.sourceTag" placeholder="如 行为层践行" />
        </el-form-item>
        <el-form-item label="责任人" required>
          <el-input v-model="rect.owner" placeholder="责任人账号（如 chenqi）" />
        </el-form-item>
        <el-form-item label="整改截止">
          <el-date-picker v-model="rect.deadline" type="date" value-format="YYYY-MM-DD" placeholder="选择截止日期" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="rectVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="rectSaving" @click="doCreateRectify">创建并下发</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const loading = ref(false)
const qOptions = ref([])
const qid = ref(null)
const overview = ref({})

const deptVis = ref(false)
const detail = ref({})
const tab = ref('dim')
const drawerReady = ref(false)
const trendChart = ref(null)
const radarChart = ref(null)
const deptTrendChart = ref(null)
let trendIns = null
let radarIns = null
let deptTrendIns = null

const rectVis = ref(false)
const rectSaving = ref(false)
const rect = reactive({ dept: '', title: '', sourceTag: '', owner: '', deadline: '' })

function fmt(s) { return s ? String(s).replace('T', ' ').slice(0, 16) : '-' }
function gradeType(g) { return { 优秀: 'success', 良好: '', 一般: 'warning', 薄弱: 'danger' }[g] || 'info' }
function riskType(r) { return { 高风险: 'danger', 中风险: 'warning', 低风险: 'success' }[r] || 'info' }
function idxClass(v) {
  const n = Number(v || 0)
  return n >= 85 ? 'rate-good' : n < 60 ? 'rate-bad' : n < 70 ? 'rate-mid' : 'rate-ok'
}
function rectifyStatusType(s) {
  return { 待方案提交: 'info', 方案待审核: 'warning', 整改实施中: '', 待验收: 'warning', 验收闭环: 'success', 验收驳回重整改: 'danger' }[s] || 'info'
}
function optionPct(v, counts) {
  const total = Object.values(counts || {}).reduce((a, b) => a + Number(b), 0)
  return total ? Math.round(Number(v) * 10000 / total) / 100 : 0
}
function wordStyle(count) {
  const max = Math.max(1, ...(detail.value.wordCloud || []).map(w => w.count))
  const ratio = count / max
  const size = 13 + Math.round(ratio * 20)
  const colors = ['#2f6f57', '#3a8a6b', '#c47f2d', '#b35c44', '#4a6fa5', '#7a5ea0']
  return { fontSize: size + 'px', color: colors[count % colors.length] }
}

async function loadQOptions() {
  const r = await axios.get('/api/culture/survey-questionnaire/list')
  qOptions.value = (r.data.data || []).filter(x => x.templateFlag !== 1)
}

async function loadOverview() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/survey-health/overview', { params: { questionnaireId: qid.value || undefined } })
    overview.value = r.data.data || {}
    if (overview.value.questionnaireId) qid.value = overview.value.questionnaireId
    await nextTick()
    drawTrend()
  } finally { loading.value = false }
}

function drawTrend() {
  if (!trendChart.value) return
  trendIns = trendIns || echarts.init(trendChart.value)
  const t = overview.value.trend || []
  trendIns.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 44, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: t.map(x => x.period) },
    yAxis: { type: 'value', min: 0, max: 100, splitLine: { lineStyle: { type: 'dashed' } } },
    series: [{
      name: '氛围指数', type: 'line', smooth: true, data: t.map(x => x.index),
      label: { show: true, fontSize: 11 },
      lineStyle: { width: 3, color: '#2f6f57' }, itemStyle: { color: '#2f6f57' },
      markLine: { silent: true, data: [{ yAxis: 85, lineStyle: { color: '#22a06b', type: 'dashed' }, label: { formatter: '优秀85' } }, { yAxis: 60, lineStyle: { color: '#f56c6c', type: 'dashed' }, label: { formatter: '薄弱60' } }] },
      areaStyle: { color: 'rgba(47,111,87,0.12)' }
    }]
  })
}

async function openDept(dept) {
  detail.value = {}
  tab.value = 'dim'
  drawerReady.value = false
  deptVis.value = true
  const r = await axios.get('/api/culture/survey-health/org', { params: { dept, questionnaireId: qid.value || undefined } })
  detail.value = r.data.data || {}
  // 抽屉动画可能已结束（数据返回更慢时），直接补绘
  await nextTick()
  if (drawerReady.value) drawRadar()
}

function onDrawerOpened() {
  drawerReady.value = true
  nextTick(() => drawRadar())
}

function onTabChange(name) {
  if (name === 'trend') {
    nextTick(() => {
      if (deptTrendIns) deptTrendIns.resize()
      drawDeptTrend()
    })
  }
}

function drawRadar() {
  if (!radarChart.value) return
  radarIns = radarIns || echarts.init(radarChart.value)
  radarIns.resize()
  const dims = detail.value.dimensions || []
  radarIns.setOption({
    tooltip: {},
    radar: {
      indicator: dims.map(d => ({ name: d.dimension, max: 100 })),
      radius: '62%', splitLine: { lineStyle: { color: '#d8e5dd' } }
    },
    series: [{
      type: 'radar',
      data: [{ value: dims.map(d => d.score), name: '本期得分', areaStyle: { color: 'rgba(47,111,87,0.25)' }, lineStyle: { color: '#2f6f57' } }]
    }]
  }, true)
}

function drawDeptTrend() {
  if (!deptTrendChart.value) return
  deptTrendIns = deptTrendIns || echarts.init(deptTrendChart.value)
  deptTrendIns.resize()
  const t = detail.value.trend || []
  deptTrendIns.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 44, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: t.map(x => x.period) },
    yAxis: { type: 'value', min: 0, max: 100, splitLine: { lineStyle: { type: 'dashed' } } },
    series: [{ type: 'bar', data: t.map(x => x.index), barWidth: 28, itemStyle: { color: '#4a9d7c' }, label: { show: true, position: 'top' } }]
  }, true)
}

function openCreateRectify() {
  Object.assign(rect, {
    dept: detail.value.dept,
    title: `${detail.value.dept}氛围短板专项整改`,
    sourceTag: (detail.value.shortBoardTags || []).join(','),
    owner: '', deadline: ''
  })
  rectVis.value = true
}

async function doCreateRectify() {
  if (!rect.title.trim() || !rect.owner.trim()) return ElMessage.warning('任务名称与责任人必填')
  rectSaving.value = true
  try {
    await axios.post('/api/culture/survey-rectify', {
      questionnaireId: detail.value.questionnaireId,
      title: rect.title.trim(), sourceTag: rect.sourceTag,
      dept: rect.dept, owner: rect.owner.trim(), deadline: rect.deadline || null
    })
    ElMessage.success('整改任务已创建并下发')
    rectVis.value = false
    const r = await axios.get('/api/culture/survey-health/org', { params: { dept: detail.value.dept, questionnaireId: qid.value || undefined } })
    detail.value = r.data.data || {}
  } finally { rectSaving.value = false }
}

window.addEventListener('resize', () => {
  trendIns && trendIns.resize()
  radarIns && radarIns.resize()
  deptTrendIns && deptTrendIns.resize()
})

onMounted(async () => {
  await loadQOptions()
  await loadOverview()
})
</script>

<style scoped>
.mb { margin-bottom: 12px; }
.mt { margin-top: 14px; }
.bar { display: flex; gap: 10px; align-items: center; }
.overall { text-align: center; padding: 8px 0; }
.big-index { font-size: 52px; font-weight: 700; line-height: 1.1; }
.overall-tags { margin: 10px 0; display: flex; gap: 10px; justify-content: center; }
.overall-sub { color: #777; font-size: 12.5px; margin-top: 4px; }
.chart { height: 230px; }
.chart-sm { height: 250px; }
.rate-good { color: #22a06b; }
.rate-mid { color: #e6a23c; }
.rate-bad { color: #f56c6c; }
.rate-ok { color: #409eff; }
.ok-text { color: #a0a5aa; font-size: 12px; }
.score-tag { margin: 2px 4px 2px 0; }
.word-cloud { padding: 10px; line-height: 2.4; }
.word { display: inline-block; margin: 0 12px; font-weight: 600; }
.dist-line { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.dist-dept { width: 170px; font-size: 13px; color: #444; }
.dist-num { width: 56px; text-align: right; font-size: 12.5px; color: #666; }
</style>

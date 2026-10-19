<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">调研任务管理：发布问卷并监控回收进度；参与率=已提交/目标人数（跨模块看板指标）；已发布问卷可手动结束</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="filter.status" placeholder="状态" clearable size="small" style="width:120px" @change="load">
          <el-option label="草稿" value="草稿" />
          <el-option label="已发布" value="已发布" />
          <el-option label="已结束" value="已结束" />
        </el-select>
        <el-select v-model="filter.periodType" placeholder="周期" clearable size="small" style="width:120px" @change="load">
          <el-option label="季度" value="季度" />
          <el-option label="年度" value="年度" />
          <el-option label="专项" value="专项" />
        </el-select>
        <span style="flex:1"></span>
        <el-button size="small" @click="load">刷新</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" class="mt">
        <el-table-column prop="title" label="问卷名称" min-width="230" show-overflow-tooltip />
        <el-table-column prop="periodType" label="周期" width="70" align="center" />
        <el-table-column label="形式" width="70" align="center">
          <template #default="{row}">{{ row.anonymousFlag === 1 ? '匿名' : '实名' }}</template>
        </el-table-column>
        <el-table-column prop="targetOrg" label="参与组织" width="150" show-overflow-tooltip />
        <el-table-column label="有效期" width="280">
          <template #default="{row}">{{ fmt(row.startTime) }} ~ {{ fmt(row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="targetCount" label="目标人数" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{row}">
            <el-tag size="small" :type="row.status === '已发布' ? 'success' : row.status === '已结束' ? 'info' : 'warning'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisher" label="发布人" width="100" />
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('C02','C03','C99') && row.status === '草稿'" size="small" type="primary" link @click="openPublish(row)">发布</el-button>
            <el-button v-if="can('C02','C03','C99') && row.status === '已发布'" size="small" type="warning" link @click="doFinish(row)">结束</el-button>
            <el-button size="small" link @click="openProgress(row)">回收进度</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 发布弹窗 -->
    <el-dialog v-model="pubVis" title="发布调研问卷" width="560px">
      <el-alert type="warning" :closable="false" class="mb">发布后问卷内容将锁定不可修改，请确认题目与有效期无误。</el-alert>
      <el-form label-width="100px" size="small">
        <el-form-item label="问卷名称">
          <el-input :model-value="pubRow.title" disabled />
        </el-form-item>
        <el-form-item label="参与组织" required>
          <el-input v-model="pubRow.targetOrg" placeholder="如 集团总部及各分公司" />
        </el-form-item>
        <el-form-item label="目标参与人数" required>
          <el-input-number v-model="pubRow.targetCount" :min="1" :max="99999" controls-position="right" />
          <span class="tip">用于计算参与率</span>
        </el-form-item>
        <el-form-item label="有效期" required>
          <el-date-picker v-model="pubRange" type="datetimerange" start-placeholder="开始时间" end-placeholder="结束时间"
                          value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm"
                          :default-time="defaultTime" range-separator="至" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="pubVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="publishing" @click="doPublish">确认发布</el-button>
      </template>
    </el-dialog>

    <!-- 回收进度抽屉 -->
    <el-drawer v-model="progVis" :title="`回收进度 - ${progRow.title || ''}`" size="720px">
      <template v-if="progRow.id">
        <el-descriptions :column="3" border size="small" class="mb">
          <el-descriptions-item label="目标人数">{{ progress.targetCount ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="已提交">{{ progress.submitted ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="参与率">
            <span :class="rateClass(progress.participationRate)"><b>{{ progress.participationRate ?? 0 }}%</b></span>
          </el-descriptions-item>
        </el-descriptions>
        <el-progress :percentage="Number(progress.participationRate || 0)" :stroke-width="14" class="mb"
                     :status="(progress.participationRate || 0) >= 100 ? 'success' : ''" />
        <el-card shadow="never" class="mb">
          <template #header><b>部门回收分布</b></template>
          <div v-for="(v, k) in (progress.deptDistribution || {})" :key="k" class="dist-line">
            <span class="dist-dept">{{ k }}</span>
            <el-progress :percentage="distPct(v, progress.targetCount)" :stroke-width="12" style="flex:1" />
            <span class="dist-num">{{ v }} 份</span>
          </div>
          <el-empty v-if="!Object.keys(progress.deptDistribution || {}).length" description="暂无答卷" :image-size="60" />
        </el-card>
        <el-card shadow="never">
          <template #header><b>答卷明细（{{ respRows.length }}）</b></template>
          <el-table :data="respRows" size="small" border max-height="320">
            <el-table-column prop="respondent" label="作答人" width="120" show-overflow-tooltip />
            <el-table-column prop="respondentDept" label="部门" width="150" show-overflow-tooltip />
            <el-table-column label="维度得分" min-width="220">
              <template #default="{row}">
                <el-tag v-for="(v, k) in parseObj(row.dimensionScores)" :key="k" size="small" effect="plain" class="score-tag"
                        :type="v < 60 ? 'danger' : v < 70 ? 'warning' : 'success'">{{ k }} {{ v }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="150">
              <template #default="{row}">{{ fmt(row.submitTime) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const rows = ref([])
const loading = ref(false)
const publishing = ref(false)
const filter = reactive({ status: '', periodType: '' })

const pubVis = ref(false)
const pubRow = ref({})
const pubRange = ref([])
const defaultTime = [new Date(2026, 9, 1, 9, 0, 0), new Date(2026, 12, 31, 18, 0, 0)]

const progVis = ref(false)
const progRow = ref({})
const progress = ref({})
const respRows = ref([])

function fmt(s) { return s ? String(s).replace('T', ' ').slice(0, 16) : '-' }
function parseObj(s) { try { return JSON.parse(s || '{}') } catch (e) { return {} } }
function distPct(v, total) { return total ? Math.min(100, Math.round(v * 10000 / total) / 100) : 0 }
function rateClass(v) {
  const n = Number(v || 0)
  return n >= 80 ? 'rate-good' : n >= 60 ? 'rate-mid' : 'rate-bad'
}

async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/survey-questionnaire/list', {
      params: { status: filter.status || undefined, periodType: filter.periodType || undefined }
    })
    rows.value = (r.data.data || []).filter(x => x.templateFlag !== 1)
  } finally { loading.value = false }
}

function openPublish(row) {
  pubRow.value = { id: row.id, title: row.title, targetOrg: row.targetOrg || '', targetCount: row.targetCount || null }
  pubRange.value = []
  pubVis.value = true
}

async function doPublish() {
  if (!pubRow.value.targetOrg || !pubRow.value.targetOrg.trim()) return ElMessage.warning('请填写参与组织')
  if (!pubRow.value.targetCount) return ElMessage.warning('请填写目标参与人数')
  if (!pubRange.value || pubRange.value.length !== 2) return ElMessage.warning('请选择有效期起止时间')
  publishing.value = true
  try {
    await axios.post(`/api/culture/survey-questionnaire/${pubRow.value.id}/publish`, {
      targetOrg: pubRow.value.targetOrg.trim(),
      targetCount: pubRow.value.targetCount,
      startTime: pubRange.value[0],
      endTime: pubRange.value[1]
    })
    ElMessage.success('问卷已发布')
    pubVis.value = false
    load()
  } finally { publishing.value = false }
}

async function doFinish(row) {
  await ElMessageBox.confirm(`确认提前结束《${row.title}》？结束后员工将无法继续作答。`, '结束确认', { type: 'warning' })
  await axios.post(`/api/culture/survey-questionnaire/${row.id}/finish`)
  ElMessage.success('问卷已结束')
  load()
}

async function openProgress(row) {
  progRow.value = row
  progVis.value = true
  progress.value = {}
  respRows.value = []
  const [p, rs] = await Promise.all([
    axios.get(`/api/culture/survey-questionnaire/${row.id}/progress`),
    axios.get(`/api/culture/survey-questionnaire/${row.id}/responses`)
  ])
  progress.value = p.data.data || {}
  respRows.value = rs.data.data || []
}

onMounted(load)
</script>

<style scoped>
.bar { display: flex; gap: 8px; align-items: center; }
.mt { margin-top: 10px; }
.mb { margin-bottom: 14px; }
.tip { color: #909399; font-size: 12px; margin-left: 10px; }
.rate-good { color: #22a06b; }
.rate-mid { color: #e6a23c; }
.rate-bad { color: #f56c6c; }
.dist-line { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.dist-dept { width: 150px; font-size: 13px; color: #444; }
.dist-num { width: 60px; text-align: right; font-size: 12.5px; color: #666; }
.score-tag { margin: 2px 4px 2px 0; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">四本台账自动归集 · 年度应召开 党员大会4/支委会12/党小组会12/党课4 · 补录显著标记 · 连续两季度不达标升级扣分 · 书记每年至少讲1次党课</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-select v-model="ov.orgId" placeholder="全部支部" clearable size="small" style="width:140px">
          <el-option v-for="o in orgs" :key="o.id" :label="o.name" :value="o.id" />
        </el-select>
        <el-input-number v-model="ov.year" :min="2020" :max="2030" size="small" controls-position="right" style="width:110px" />
        <el-input-number v-model="ov.quarter" :min="1" :max="4" size="small" controls-position="right" placeholder="季度(空=年度)" style="width:150px" />
        <el-button size="small" type="primary" @click="loadOverview">台账总览</el-button>
        <el-button size="small" type="warning" @click="checkComplete">完整率校验</el-button>
        <el-button v-if="can('R02','R03','R04')" size="small" type="danger" @click="checkConsecutive">连续两季度检查</el-button>
        <el-button size="small" @click="checkLecturer">党课授课刚性</el-button>
        <el-button v-if="can('R02','R07','R08')" size="small" type="success" @click="openBackfill()">台账补录</el-button>
        <el-button size="small" @click="exportCsv">导出CSV</el-button>
      </div>

      <el-row :gutter="12" v-if="overview.rows">
        <el-col :span="6" v-for="r in overview.rows" :key="r.meetingType">
          <el-card shadow="hover" class="rate-card">
            <div class="rate-title">{{ r.meetingType }}</div>
            <div class="rate-num" :class="rateClass(r.completeRate)">{{ r.completeRate }}%</div>
            <div class="rate-sub">应开 {{ r.shouldCount }} · 已归 {{ r.actualCount }} · 补录 {{ r.backfillCount }}</div>
          </el-card>
        </el-col>
      </el-row>
      <el-alert v-if="overview.overallRate!=null" :title="`周期总完整率 ${overview.overallRate}%`" :type="overview.overallRate<100?'warning':'success'" :closable="false" style="margin-top:10px" />
      <el-alert v-if="overview.dataNote" :title="overview.dataNote" type="info" :closable="false" style="margin-top:8px" />

      <el-table v-if="overview.lecturerCheck" :data="overview.lecturerCheck" size="small" border style="margin-top:10px">
        <el-table-column prop="lecturerRole" label="党课授课刚性（每年≥1次）" min-width="200" />
        <el-table-column prop="required" label="要求" width="80" />
        <el-table-column prop="actual" label="实际" width="80" />
        <el-table-column label="结果" width="100">
          <template #default="{ row }"><el-tag size="small" :type="row.passed?'success':'danger'">{{ row.passed ? '达标' : '未达标' }}</el-tag></template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never">
      <el-tabs v-model="meetingType" @tab-change="loadList">
        <el-tab-pane v-for="t in ledgerTypes" :key="t" :label="t" :name="t" />
      </el-tabs>
      <el-table :data="rows" size="small" border v-loading="loading">
        <el-table-column prop="orgName" label="党支部" width="120" show-overflow-tooltip />
        <el-table-column prop="title" label="会议主题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="holdDate" label="召开日期" width="100" />
        <el-table-column prop="host" label="主持人" width="90" />
        <el-table-column label="应/实到" width="90">
          <template #default="{ row }">{{ row.shouldCount ?? '—' }}/{{ row.actualCount ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="签到" width="70"><template #default="{ row }">{{ row.signCount ?? '—' }}</template></el-table-column>
        <el-table-column label="补录" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.isBackfill===1" size="small" type="warning" effect="dark">补录</el-tag>
            <span v-else class="muted">正常归集</span>
          </template>
        </el-table-column>
        <el-table-column prop="backfillReason" label="补录原因" min-width="140" show-overflow-tooltip />
        <el-table-column prop="lecturerRole" label="主讲身份" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }"><el-tag size="small" type="success">{{ row.status }}</el-tag></template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="bfVisible" title="台账补录（追记，原因必填）" width="560px">
      <el-alert title="补录记录将在台账中显著标记；未说明原因阻断保存（E-AC-10）" type="warning" :closable="false" style="margin-bottom:10px" />
      <el-form :model="bf" label-width="100px">
        <el-form-item label="台账类型" required>
          <el-select v-model="bf.meetingType" style="width:100%">
            <el-option v-for="t in ledgerTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="党支部" required>
          <el-select v-model="bf.orgId" style="width:100%" @change="bf.orgName=(orgs.find(o=>o.id===bf.orgId)||{}).name">
            <el-option v-for="o in orgs" :key="o.id" :label="o.name" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="会议主题" required><el-input v-model="bf.title" /></el-form-item>
        <el-form-item label="召开日期" required><el-date-picker v-model="bf.holdDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="地点/主持"><el-input v-model="bf.location" placeholder="地点；主持人" /></el-form-item>
        <el-form-item label="应到/实到">
          <el-input-number v-model="bf.shouldCount" :min="0" controls-position="right" style="width:120px" />
          <el-input-number v-model="bf.actualCount" :min="0" controls-position="right" style="width:120px;margin-left:8px" />
        </el-form-item>
        <el-form-item label="主要内容"><el-input v-model="bf.content" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="补录原因" required><el-input v-model="bf.backfillReason" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bfVisible=false">取消</el-button>
        <el-button type="primary" @click="submitBackfill">提交补录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/activity/ledger'
const orgs = [{ id: 1, name: '第一党支部' }, { id: 2, name: '第二党支部' }]
const ledgerTypes = ['支部党员大会', '支部委员会', '党小组会', '党课']

const loading = ref(false)
const rows = ref([])
const meetingType = ref('支部党员大会')
const ov = reactive({ orgId: null, year: new Date().getFullYear(), quarter: null })
const overview = ref({})
const bfVisible = ref(false)
const bf = ref({})

function rateClass(rate) { return rate >= 100 ? 'ok' : (rate >= 80 ? 'warn' : 'bad') }

async function loadList() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { meetingType: meetingType.value, orgId: ov.orgId, year: ov.year, quarter: ov.quarter } })
    if (res.data.code === 200) rows.value = res.data.data
  } finally { loading.value = false }
}
async function loadOverview() {
  try {
    const res = await axios.get(`${api}/overview`, { params: { orgId: ov.orgId, year: ov.year, quarter: ov.quarter } })
    if (res.data.code === 200) overview.value = res.data.data
    loadList()
  } catch {}
}
async function checkComplete() {
  try {
    await axios.post(`${api}/check-complete`, null, { params: { orgId: ov.orgId, year: ov.year, quarter: ov.quarter || 1 } })
    ElMessage.success('台账完整率100%')
  } catch { loadOverview() }
}
async function checkConsecutive() {
  try {
    await axios.post(`${api}/check-consecutive`, null, { params: { orgId: ov.orgId, year: ov.year, quarter: ov.quarter || 1 } })
    ElMessage.success('未出现连续两季度不达标')
  } catch { loadOverview() }
}
async function checkLecturer() {
  try {
    await axios.post(`${api}/check-lecturer`, null, { params: { orgId: ov.orgId, year: ov.year } })
    ElMessage.success('党课授课刚性达标')
  } catch { loadOverview() }
}
function openBackfill() {
  bf.value = { orgId: ov.orgId || 1, meetingType: meetingType.value, holdDate: new Date().toISOString().slice(0, 10) }
  bfVisible.value = true
}
async function submitBackfill() {
  try {
    bf.value.orgName = (orgs.find(o => o.id === bf.value.orgId) || {}).name
    await axios.post(`${api}/backfill`, bf.value)
    ElMessage.success('补录成功，台账已显著标记')
    bfVisible.value = false
    loadOverview()
  } catch {}
}
function exportCsv() {
  const head = ['党支部', '类型', '主题', '召开日期', '主持人', '应到', '实到', '补录', '补录原因', '状态']
  const lines = rows.value.map(r => [r.orgName, r.meetingType, r.title, r.holdDate, r.host, r.shouldCount, r.actualCount, r.isBackfill===1?'补录':'', r.backfillReason||'', r.status].map(v => `"${v??''}"`).join(','))
  const blob = new Blob(['﻿' + head.join(',') + '\n' + lines.join('\n')], { type: 'text/csv;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `三会一课台账_${meetingType.value}_${ov.year}.csv`
  a.click()
}
onMounted(loadOverview)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.mb { margin-bottom: 12px; }
.muted { color: #c0c4cc; }
.rate-card { text-align: center; margin-bottom: 6px; }
.rate-title { font-size: 13px; color: #606266; }
.rate-num { font-size: 26px; font-weight: 700; margin: 4px 0; }
.rate-num.ok { color: #67c23a; }
.rate-num.warn { color: #e6a23c; }
.rate-num.bad { color: #f56c6c; }
.rate-sub { font-size: 12px; color: #909399; }
</style>

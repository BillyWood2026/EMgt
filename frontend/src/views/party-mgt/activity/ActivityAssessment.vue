<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">完成率=已归档/应召开 · 党员参与率阈值80% · 未达标自动生成扣分建议联动第10章 · 口径不全标注"数据待补齐"不造假</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-select v-model="f.orgId" placeholder="全部支部" clearable size="small" style="width:140px">
          <el-option v-for="o in orgs" :key="o.id" :label="o.name" :value="o.id" />
        </el-select>
        <el-input-number v-model="f.year" :min="2020" :max="2030" size="small" controls-position="right" style="width:110px" />
        <el-input-number v-model="f.quarter" :min="1" :max="4" size="small" controls-position="right" placeholder="季度(空=年度)" style="width:150px" />
        <el-button size="small" type="primary" @click="loadDashboard">刷新看板</el-button>
        <el-button v-if="can('R02','R04')" size="small" type="success" @click="openGenerate">生成考核</el-button>
      </div>

      <el-row :gutter="12" v-if="dash.qualifyRate!=null">
        <el-col :span="6">
          <el-card shadow="hover" class="kpi"><div class="t">三会一课完成率</div>
            <div class="n" :class="dash.qualifyRate>=100?'ok':'bad'">{{ dash.qualifyRate }}%</div>
            <div class="s">应开 {{ dash.shouldCount }} · 已归档 {{ dash.actualCount }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="kpi"><div class="t">党员参与率</div>
            <div class="n" :class="participationClass">{{ dash.participationRate == null ? '—' : dash.participationRate + '%' }}</div>
            <div class="s">应到 {{ dash.expectSum }} · 签到 {{ dash.signSum }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="kpi"><div class="t">已归档活动</div>
            <div class="n neutral">{{ dash.recordCount }}</div>
            <div class="s">周期内记录数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="kpi"><div class="t">达标判定</div>
            <div class="n" :class="dash.unqualified.length===0?'ok':'bad'">{{ dash.unqualified.length===0 ? '达标' : '未达标' }}</div>
            <div class="s">{{ dash.unqualified.length }} 项问题</div>
          </el-card>
        </el-col>
      </el-row>

      <el-alert v-for="(u,i) in dash.unqualified" :key="i" :title="u" type="error" :closable="false" style="margin-top:8px" />
      <el-alert v-if="dash.dataNote" :title="dash.dataNote" type="warning" :closable="false" style="margin-top:8px" />
    </el-card>

    <el-card shadow="never">
      <el-tabs v-model="tab" @tab-change="load">
        <el-tab-pane label="考核记录" name="all" />
        <el-tab-pane label="未达标清单" name="unq" />
      </el-tabs>
      <el-table :data="rows" size="small" border v-loading="loading">
        <el-table-column prop="orgName" label="党支部" width="130" show-overflow-tooltip />
        <el-table-column label="周期" width="110">
          <template #default="{ row }">{{ row.assessYear }}{{ row.periodType==='季度' ? ' Q'+row.periodQuarter : ' 年度' }}</template>
        </el-table-column>
        <el-table-column prop="qualifyRate" label="完成率%" width="90" />
        <el-table-column prop="participationRate" label="参与率%" width="90" />
        <el-table-column prop="activityTimes" label="活动数" width="80" />
        <el-table-column label="等次/得分" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="row.unqualifiedFlag===1?'danger':'success'">{{ row.grade }} {{ row.score }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deductSuggestion" label="扣分建议" min-width="220" show-overflow-tooltip />
        <el-table-column prop="dataNote" label="数据标注" min-width="160" show-overflow-tooltip />
        <el-table-column prop="comment" label="评语" min-width="160" show-overflow-tooltip />
        <el-table-column prop="assessDate" label="考核日期" width="110" />
      </el-table>
    </el-card>

    <el-dialog v-model="genVisible" title="生成活动考核" width="520px">
      <el-alert title="统计口径不全（无已归档记录/缺签到数据）将被标注数据待补齐，不生成虚假数据（E-AC-14）" type="info" :closable="false" style="margin-bottom:10px" />
      <el-form :model="gen" label-width="110px">
        <el-form-item label="党支部" required>
          <el-select v-model="gen.orgId" style="width:100%" @change="gen.orgName=(orgs.find(o=>o.id===gen.orgId)||{}).name">
            <el-option v-for="o in orgs" :key="o.id" :label="o.name" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年度" required><el-input-number v-model="gen.year" :min="2020" :max="2030" controls-position="right" /></el-form-item>
        <el-form-item label="季度(空=年度)"><el-input-number v-model="gen.quarter" :min="1" :max="4" controls-position="right" /></el-form-item>
        <el-form-item label="党员总数"><el-input-number v-model="gen.memberTotal" :min="0" controls-position="right" /></el-form-item>
        <el-form-item label="口径不全时强制生成"><el-switch v-model="gen.forceNoData" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="genVisible=false">取消</el-button>
        <el-button type="primary" @click="doGenerate">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/activity/assessment'
const orgs = [{ id: 1, name: '第一党支部' }, { id: 2, name: '第二党支部' }]

const loading = ref(false)
const rows = ref([])
const tab = ref('all')
const f = reactive({ orgId: null, year: new Date().getFullYear(), quarter: null })
const dash = ref({})
const genVisible = ref(false)
const gen = ref({})

const participationClass = computed(() => {
  const v = dash.value.participationRate
  if (v == null) return 'neutral'
  return v >= 80 ? 'ok' : 'bad'
})

async function loadDashboard() {
  try {
    const res = await axios.get(`${api}/dashboard`, { params: { ...f } })
    if (res.data.code === 200) dash.value = res.data.data
  } catch {}
}
async function load() {
  loading.value = true
  try {
    const url = tab.value === 'unq' ? `${api}/unqualified` : `${api}/list`
    const res = await axios.get(url, { params: tab.value === 'unq' ? { year: f.year } : { ...f } })
    if (res.data.code === 200) rows.value = res.data.data
  } finally { loading.value = false }
}
function openGenerate() {
  gen.value = { orgId: f.orgId || 1, year: f.year, quarter: f.quarter, memberTotal: 20, forceNoData: false }
  genVisible.value = true
}
async function doGenerate() {
  try {
    gen.value.orgName = (orgs.find(o => o.id === gen.value.orgId) || {}).name
    await axios.post(`${api}/generate`, null, { params: { ...gen.value } })
    ElMessage.success('考核已生成（未达标项已生成扣分建议）')
    genVisible.value = false
    loadDashboard()
    load()
  } catch {}
}
onMounted(() => { loadDashboard(); load() })
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.mb { margin-bottom: 12px; }
.kpi { text-align: center; }
.t { font-size: 13px; color: #606266; }
.n { font-size: 28px; font-weight: 700; margin: 6px 0; }
.n.ok { color: #67c23a; }
.n.bad { color: #f56c6c; }
.n.neutral { color: #409eff; }
.s { font-size: 12px; color: #909399; }
</style>

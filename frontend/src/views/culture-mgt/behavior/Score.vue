<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">个人文化积分台账：综合得分=客户导向×25%+奋斗担当×30%+艰苦奋斗×25%+自我批判×20%；等级S≥95/A85-94/B70-84/C60-69/D&lt;60</el-tag>
    </RoleBar>

    <el-row :gutter="14" class="mt">
      <el-col :span="6" v-for="card in kpiCards" :key="card.title">
        <el-card shadow="hover" :body-style="{padding:'14px'}">
          <div class="kpi-title">{{ card.title }}</div>
          <div class="kpi-value" :style="{color: card.color}">{{ card.value }}</div>
          <div class="kpi-sub">{{ card.sub }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="mt">
      <div class="bar">
        <el-input v-model="filter.userName" placeholder="查询用户" clearable size="small" style="width:140px" @change="loadData" />
        <el-select v-model="filter.dimension" placeholder="维度" clearable size="small" style="width:120px" @change="loadTxn">
          <el-option label="全部" value="" />
          <el-option label="客户导向" value="客户导向" />
          <el-option label="奋斗担当" value="奋斗担当" />
          <el-option label="艰苦奋斗" value="艰苦奋斗" />
          <el-option label="自我批判" value="自我批判" />
        </el-select>
        <el-input v-model="filter.periodTag" placeholder="周期标签" clearable size="small" style="width:130px" @change="loadTxn" />
        <span style="flex:1"></span>
        <el-button size="small" @click="loadData">刷新</el-button>
      </div>
      <el-tabs v-model="subTab" class="mt">
        <el-tab-pane label="积分流水" name="txn">
          <el-table :data="txnRows" size="small" border v-loading="loading">
            <el-table-column prop="userName" label="用户" width="100" />
            <el-table-column prop="periodTag" label="周期" width="100" />
            <el-table-column prop="dimension" label="维度" width="90" align="center" />
            <el-table-column prop="direction" label="方向" width="70" align="center">
              <template #default="{row}">
                <el-tag size="small" :type="row.direction === '加' ? 'success' : 'danger'">{{ row.direction }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="分值" width="80" align="center" />
            <el-table-column prop="sourceType" label="来源" width="90" align="center" />
            <el-table-column prop="sourceNo" label="来源编号" width="150" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" width="160" />
            <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="部门排名" name="rank">
          <el-table :data="rankRows" size="small" border v-loading="loading">
            <el-table-column type="index" label="排名" width="70" align="center" />
            <el-table-column prop="userName" label="用户" width="100" />
            <el-table-column prop="totalScore" label="综合得分" width="90" align="center" />
            <el-table-column prop="levelTag" label="等级" width="60" align="center">
              <template #default="{row}">
                <el-tag size="small" :type="levelType(row.levelTag)">{{ row.levelTag }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="dimCustomer" label="客户导向" width="80" align="center" />
            <el-table-column prop="dimStrive" label="奋斗担当" width="80" align="center" />
            <el-table-column prop="dimHardship" label="艰苦奋斗" width="80" align="center" />
            <el-table-column prop="dimReflect" label="自我批判" width="80" align="center" />
            <el-table-column prop="posCount" label="正向" width="70" align="center" />
            <el-table-column prop="negCount" label="反向" width="70" align="center" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="部门达标率" name="compliance">
          <el-form label-width="120px" size="small" class="mt">
            <el-form-item label="部门">
              <el-input v-model="complianceQuery.dept" placeholder="部门名称" clearable @change="loadCompliance" />
            </el-form-item>
            <el-form-item label="周期">
              <el-input v-model="complianceQuery.periodTag" placeholder="如 2026Q3" clearable @change="loadCompliance" />
            </el-form-item>
          </el-form>
          <el-result v-if="complianceData" :icon="complianceData.complianceRate >= 80 ? 'success' : 'warning'" :title="complianceData.complianceRate + '%'" :sub-title="'达标率：' + complianceData.passCount + '/' + complianceData.totalCount + ' 人'" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'

const loading = ref(false)
const filter = ref({ userName: '', dimension: '', periodTag: '' })
const subTab = ref('txn')

const myAccount = ref(null)
const txnRows = ref([])
const rankRows = ref([])
const complianceData = ref(null)
const complianceQuery = ref({ dept: '', periodTag: '' })

const kpiCards = computed(() => {
  const a = myAccount.value
  if (!a) return [
    { title: '综合得分', value: '-', sub: '-', color: '#333' },
    { title: '奋斗担当', value: '-', sub: '维度累计', color: '#333' },
    { title: '正向记录', value: '-', sub: '次', color: '#333' },
    { title: '等级', value: '-', sub: 'S/A/B/C/D', color: '#333' }
  ]
  return [
    { title: '综合得分', value: a.totalScore?.toFixed(1) || '-', sub: '基线60+动态', color: a.totalScore >= 70 ? '#67C23A' : '#E6A23C' },
    { title: '奋斗担当', value: a.dimStrive?.toFixed(1) || '-', sub: '维度累计', color: '#409EFF' },
    { title: '正向记录', value: a.posCount || 0, sub: '次', color: '#67C23A' },
    { title: '等级', value: a.levelTag || computeLevel(a.totalScore), sub: 'S/A/B/C/D', color: levelColor(a.levelTag || computeLevel(a.totalScore)) }
  ]
})

function computeLevel(t) { if (t >= 95) return 'S'; if (t >= 85) return 'A'; if (t >= 70) return 'B'; if (t >= 60) return 'C'; return 'D' }
function levelColor(l) { if (l === 'S' || l === 'A') return '#67C23A'; if (l === 'B') return '#409EFF'; if (l === 'C') return '#E6A23C'; return '#F56C6C' }

async function loadData() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/behavior/score/my')
    myAccount.value = r.data.data || null
  } catch { myAccount.value = null }
  await loadTxn()
  await loadRank()
  loading.value = false
}
async function loadTxn() {
  try { const r = await axios.get('/api/culture/behavior/score/txn', { params: filter.value }); txnRows.value = r.data.data || [] } catch { txnRows.value = [] }
}
async function loadRank() {
  try { const r = await axios.get('/api/culture/behavior/score/rank', { params: { scope: 'all' } }); rankRows.value = r.data.data || [] } catch { rankRows.value = [] }
}
async function loadCompliance() {
  try { const r = await axios.get('/api/culture/behavior/score/compliance', { params: complianceQuery.value }); complianceData.value = r.data.data || null } catch { complianceData.value = null }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.bar { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.mt { margin-top: 12px; }
.kpi-title { font-size: 13px; color: #666; margin-bottom: 6px; }
.kpi-value { font-size: 28px; font-weight: 700; line-height: 1; }
.kpi-sub { font-size: 12px; color: #999; margin-top: 6px; }
</style>

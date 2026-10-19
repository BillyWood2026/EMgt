<template>
  <div class="page-container">
    <div class="page-header">
      <h2>能源管理概览</h2>
      <el-button type="primary" @click="loadAll">刷新</el-button>
    </div>

    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon blue"><el-icon><Lightning /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ totalStandardCoal }}</div>
            <div class="stat-label">总能耗（标煤·吨）</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon orange"><el-icon><DataLine /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ unitProductConsumption }}</div>
            <div class="stat-label">单位产品能耗</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon red"><el-icon><Warning /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ overBaselineCount }}</div>
            <div class="stat-label">超基线预警数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon green"><el-icon><TrendCharts /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.savingTotal }}</div>
            <div class="stat-label">节能项目数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon green"><el-icon><Coin /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">¥{{ totalSavingBenefit }}</div>
            <div class="stat-label">节能效益(元)</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon blue"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ efficiencyComplianceRate }}%</div>
            <div class="stat-label">能效达标率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>最新能耗记录</span></template>
          <el-table :data="consumptions" border size="small" v-loading="loading" max-height="380">
            <el-table-column prop="statPeriod" label="周期" width="100" />
            <el-table-column prop="energyType" label="能源类型" width="90" />
            <el-table-column label="消耗量" width="130">
              <template #default="{row}">{{ row.consumption }} {{ row.unit }}</template>
            </el-table-column>
            <el-table-column prop="standardCoal" label="折标煤(吨)" width="110" />
            <el-table-column label="超基线" width="80">
              <template #default="{row}">
                <el-tag v-if="isOverBaseline(row)" type="danger" size="small">超标</el-tag>
                <el-tag v-else type="success" size="small">达标</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>节能项目进展</span></template>
          <el-table :data="savings" border size="small" v-loading="loading" max-height="380">
            <el-table-column prop="projectName" label="项目名称" min-width="140" />
            <el-table-column prop="annualSaving" label="年节约(元)" width="110" />
            <el-table-column prop="responsible" label="责任人" width="90" />
            <el-table-column label="状态" width="100">
              <template #default="{row}">
                <el-tag :type="savingStatusTag(row.status)" size="small">
                  {{ savingStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { energyApi } from '@/api/eehs'

const loading = ref(false)
const consumptions = ref([])
const savings = ref([])
const analyses = ref([])
const stats = reactive({ savingTotal: 0 })

// 总能耗（标煤）= 所有能耗记录折标煤之和
const totalStandardCoal = computed(() =>
  consumptions.value.reduce((s, x) => s + (Number(x.standardCoal) || 0), 0).toFixed(2)
)

// 单位产品能耗 = 总能耗(标煤) / 总产量
const unitProductConsumption = computed(() => {
  const totalCoal = consumptions.value.reduce((s, x) => s + (Number(x.standardCoal) || 0), 0)
  // 规格书字段：产量 output，待后端扩展
  const totalOutput = consumptions.value.reduce((s, x) => s + (Number(x.output) || 0), 0)
  if (totalOutput <= 0) return '—'
  return (totalCoal / totalOutput).toFixed(4)
})

// 超基线预警数：消耗量 > 基线的记录数
const overBaselineCount = computed(() =>
  consumptions.value.filter(x => isOverBaseline(x)).length
)
function isOverBaseline(row) {
  // 规格书字段：基线 baseline，待后端扩展
  const baseline = Number(row.baseline)
  if (!baseline) return false
  return Number(row.consumption) > baseline
}

// 节能效益 = 所有节能项目年节约金额之和
const totalSavingBenefit = computed(() =>
  savings.value.reduce((s, x) => s + (Number(x.annualSaving) || 0), 0).toFixed(2)
)

// 能效达标率 = 消耗量 ≤ 目标值的分析数 / 分析总数 × 100%
const efficiencyComplianceRate = computed(() => {
  if (analyses.value.length === 0) return '0.00'
  const ok = analyses.value.filter(x => {
    const tv = Number(x.targetValue)
    return tv > 0 && Number(x.consumption) <= tv
  }).length
  return ((ok / analyses.value.length) * 100).toFixed(1)
})

function savingStatusText(s) {
  return {
    planning: '立项', implementing: '实施', acceptance: '验收',
    benefit_evaluation: '效益评估', archived: '归档'
  }[s] || s
}
function savingStatusTag(s) {
  return {
    planning: 'info', implementing: 'warning', acceptance: 'primary',
    benefit_evaluation: 'warning', archived: 'success'
  }[s] || 'info'
}

async function loadAll() {
  loading.value = true
  try {
    const [c, s, a] = await Promise.all([
      energyApi.consumptions.list(),
      energyApi.savings.list(),
      energyApi.analyses.list()
    ])
    const cData = c.data.code === 200 ? (c.data.data || []) : []
    const sData = s.data.code === 200 ? (s.data.data || []) : []
    const aData = a.data.code === 200 ? (a.data.data || []) : []
    consumptions.value = cData
    savings.value = sData.slice(0, 10)
    analyses.value = aData
    stats.savingTotal = sData.length
  } finally { loading.value = false }
}

onMounted(loadAll)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; padding: 16px; }
.stat-icon { width: 48px; height: 48px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24px; margin-right: 16px; }
.stat-icon.blue { background-color: #e6f7ff; color: #1890ff; }
.stat-icon.green { background-color: #f6ffed; color: #52c41a; }
.stat-icon.orange { background-color: #fff7e6; color: #fa8c16; }
.stat-icon.red { background-color: #fff1f0; color: #f5222d; }
.stat-info { flex: 1; }
.stat-value { font-size: 24px; font-weight: bold; color: #333; }
.stat-label { font-size: 14px; color: #999; }
</style>

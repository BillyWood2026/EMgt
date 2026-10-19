<template>
  <div class="page-container">
    <div class="page-header">
      <h2>安全管理概览</h2>
      <el-button type="primary" @click="loadAll">刷新</el-button>
    </div>

    <!-- 统计卡：高风险点位、未闭环隐患、特种作业待审批、逾期整改、事故统计、培训完成率 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon red"><el-icon><Warning /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.highRiskPoints }}</div>
            <div class="stat-label">高风险点位</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon orange"><el-icon><CircleClose /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.unclosedHazards }}</div>
            <div class="stat-label">未闭环隐患</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon blue"><el-icon><Document /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.specialOpPending }}</div>
            <div class="stat-label">特种作业待审批</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon red"><el-icon><AlarmClock /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.overdueRectify }}</div>
            <div class="stat-label">逾期整改</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon orange"><el-icon><WarningFilled /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.incidentTotal }}</div>
            <div class="stat-label">事故统计</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon green"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.trainingCompletionRate }}%</div>
            <div class="stat-label">培训完成率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>最新安全检查（未闭环置顶）</span></template>
          <el-table :data="inspections" border size="small" v-loading="loading" max-height="380">
            <el-table-column prop="inspectionName" label="检查名称" min-width="140" />
            <el-table-column prop="location" label="地点" width="100" />
            <el-table-column prop="inspectDate" label="检查日期" width="110" />
            <el-table-column label="风险等级" width="100">
              <template #default="{row}">
                <el-tag :type="riskTagType(row.riskLevel)" size="small" v-if="row.riskLevel">{{ row.riskLevel }}</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{row}">
                <el-tag :type="row.status === 'closed' ? 'success' : 'warning'" size="small">
                  {{ row.status === 'closed' ? '已闭环' : '未闭环' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>事故台账</span></template>
          <el-table :data="incidents" border size="small" v-loading="loading" max-height="380">
            <el-table-column prop="incidentNo" label="事故编号" width="110" />
            <el-table-column prop="incidentName" label="事故名称" min-width="130" />
            <el-table-column prop="level" label="等级" width="100" />
            <el-table-column prop="occurTime" label="发生时间" width="150" />
            <el-table-column label="状态" width="90">
              <template #default="{row}">
                <el-tag :type="row.status === 'closed' ? 'success' : 'warning'" size="small">
                  {{ row.status === 'closed' ? '已结案' : '处理中' }}
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
import { ref, reactive, onMounted } from 'vue'
import { safetyApi } from '@/api/eehs'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const inspections = ref([])
const incidents = ref([])
const stats = reactive({
  highRiskPoints: 0,
  unclosedHazards: 0,
  specialOpPending: 0,
  overdueRectify: 0,
  incidentTotal: 0,
  trainingCompletionRate: 0
})

function riskTagType(level) {
  if (level === '红') return 'danger'
  if (level === '橙') return 'warning'
  if (level === '黄') return 'info'
  if (level === '蓝') return 'success'
  return 'info'
}

async function loadAll() {
  loading.value = true
  try {
    const [i, c, t] = await Promise.all([
      safetyApi.inspections.list(),
      safetyApi.incidents.list(),
      safetyApi.trainings.list()
    ])
    const iData = i.data.code === 200 ? (i.data.data || []) : []
    const cData = c.data.code === 200 ? (c.data.data || []) : []
    const tData = t.data.code === 200 ? (t.data.data || []) : []
    inspections.value = [...iData].sort((a, b) => (a.status === 'closed' ? 1 : -1) - (b.status === 'closed' ? 1 : -1)).slice(0, 10)
    incidents.value = cData.slice(0, 10)
    stats.incidentTotal = cData.length
    stats.unclosedHazards = iData.filter(x => x.status !== 'closed').length
    stats.highRiskPoints = iData.filter(x => x.riskLevel === '红' || x.riskLevel === '橙').length
    stats.overdueRectify = iData.filter(x => x.status !== 'closed' && x.rectifyDeadline && new Date(x.rectifyDeadline) < new Date()).length
    // 特种作业待审批为规格书衍生指标（待后端特种作业模块扩展）
    stats.specialOpPending = 0
    stats.trainingCompletionRate = tData.length ? Math.round((tData.filter(x => x.status === 'done').length / tData.length) * 100) : 0
  } catch (e) {
    ElMessage.error('加载失败')
  } finally { loading.value = false }
}

onMounted(loadAll)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; padding: 12px; }
.stat-icon { width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; margin-right: 12px; }
.stat-icon.blue { background-color: #e6f7ff; color: #1890ff; }
.stat-icon.green { background-color: #f6ffed; color: #52c41a; }
.stat-icon.orange { background-color: #fff7e6; color: #fa8c16; }
.stat-icon.red { background-color: #fff1f0; color: #f5222d; }
.stat-info { flex: 1; }
.stat-value { font-size: 22px; font-weight: bold; color: #333; }
.stat-label { font-size: 13px; color: #999; }
</style>

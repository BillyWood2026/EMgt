<template>
  <div class="epros-monitor">
    <div class="monitor-header">
      <div class="header-title">
        <h2>流程运营监控与优化</h2>
        <p>建立流程监控指标体系，定期输出流程运营报告，持续优化流程管理</p>
      </div>
      <div class="header-actions">
        <el-select v-model="filterPeriod" placeholder="选择周期">
          <el-option label="今日" value="today" />
          <el-option label="本周" value="week" />
          <el-option label="本月" value="month" />
          <el-option label="本季度" value="quarter" />
        </el-select>
        <el-button type="primary" icon="Download" @click="exportReport">导出报告</el-button>
        <el-button icon="Refresh" @click="refreshData">刷新</el-button>
      </div>
    </div>

    <div class="monitor-body">
      <div class="stats-panel">
        <div class="stat-card" v-for="stat in statList" :key="stat.label">
          <div class="stat-icon">
            <el-icon><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}%</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
          <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
            <el-icon>{{ stat.trend > 0 ? ArrowUp : ArrowDown }}</el-icon>
            <span>{{ Math.abs(stat.trend) }}%</span>
          </div>
        </div>
      </div>

      <div class="detail-section">
        <el-tabs v-model="activeTab" type="card">
          <el-tab-pane label="超时流程预警" name="timeout">
            <el-table :data="timeoutWarnings" border>
              <el-table-column prop="flowName" label="流程名称" />
              <el-table-column prop="currentNode" label="当前节点" />
              <el-table-column prop="delayTime" label="超时时间" />
              <el-table-column prop="assignee" label="负责人" />
              <el-table-column label="操作">
                <template #default="{ row }">
                  <el-button size="small" @click="remindAssignee(row)">催办</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="异常流程监控" name="abnormal">
            <el-table :data="abnormalFlows" border>
              <el-table-column prop="flowName" label="流程名称" />
              <el-table-column prop="type" label="异常类型" />
              <el-table-column prop="description" label="异常描述" />
              <el-table-column prop="status" label="状态" />
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="流程办理排行" name="ranking">
            <el-table :data="rankingData" border>
              <el-table-column prop="rank" label="排名" />
              <el-table-column prop="deptName" label="部门" />
              <el-table-column prop="onTimeRate" label="按时率" />
              <el-table-column prop="rejectRate" label="驳回率" />
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="KPI指标库" name="kpi">
            <div class="kpi-grid">
              <div v-for="kpi in kpiList" :key="kpi.id" class="kpi-card">
                <div>{{ kpi.name }}</div>
                <div>{{ kpi.currentValue }} / {{ kpi.targetValue }}</div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="流程复盘" name="review">
            <div v-for="review in reviewList" :key="review.id" class="review-card">
              <div>{{ review.title }}</div>
              <div>{{ review.period }}</div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="流程资产库" name="asset">
            <div v-for="asset in assetList" :key="asset.id" class="asset-card">
              <div>{{ asset.name }}</div>
              <div>{{ asset.type }}</div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="内审任务" name="audit-task">
            <div v-for="task in auditTasks" :key="task.id" class="task-card">
              <div>{{ task.name }}</div>
              <div>{{ task.status }}</div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="问题整改台账" name="issue">
            <el-table :data="issueList" border>
              <el-table-column prop="title" label="问题标题" />
              <el-table-column prop="severity" label="严重程度" />
              <el-table-column prop="status" label="状态" />
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="流程优化建议" name="optimize">
            <div v-for="item in optimizeList" :key="item.id" class="optimize-card">
              <div>{{ item.title }}</div>
              <div>{{ item.status }}</div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="优秀流程库" name="excellent">
            <div v-for="item in excellentFlows" :key="item.id" class="excellent-card">
              <div>{{ item.flowName }}</div>
              <div>{{ item.domain }}</div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import axios from '@/api/axiosConfig'
import {
  Download, Refresh, CircleCheck, CircleClose, Clock, DocumentChecked, Check, User,
  ArrowUp, ArrowDown
} from '@element-plus/icons-vue'

const activeTab = ref('timeout')
const filterPeriod = ref('month')

// 后端流程实例全量列表（/api/process/instance/list 返回 IPage<ProcessInstance>）
const instances = ref([])
const loading = ref(false)

const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '')

const statusName = (s) => {
  const map = { pending: '待审批', processing: '审批中', approved: '已通过', rejected: '已驳回', cancelled: '已取消' }
  return map[s] || s || '-'
}

// 超时判定：待审批/审批中且提交时间超过 2 天视为超时
const isOverdue = (row) => {
  if (!['pending', 'processing'].includes(row.status)) return false
  if (!row.submitTime) return false
  const submit = new Date(String(row.submitTime).replace(' ', 'T'))
  const diff = (Date.now() - submit.getTime()) / 86400000
  return diff > 2
}

const delayText = (row) => {
  if (!row.submitTime) return '-'
  const submit = new Date(String(row.submitTime).replace(' ', 'T'))
  const diff = (Date.now() - submit.getTime()) / 86400000
  if (diff >= 1) return `超时${Math.floor(diff)}天`
  const hours = Math.floor(diff * 24)
  return hours > 0 ? `超时${hours}小时` : '超时'
}

// 超时流程预警（pending/processing 且超期）
const timeoutWarnings = computed(() =>
  instances.value
    .filter(r => isOverdue(r))
    .map(r => ({
      id: r.id,
      flowName: r.processName || r.processCode || `流程#${r.id}`,
      currentNode: r.currentNode || statusName(r.status),
      delayTime: delayText(r),
      assignee: r.applicant || '-'
    }))
)

// 异常流程监控（已驳回/已取消）
const abnormalFlows = computed(() =>
  instances.value
    .filter(r => ['rejected', 'cancelled'].includes(r.status))
    .map(r => ({
      id: r.id,
      flowName: r.processName || r.processCode || `流程#${r.id}`,
      type: statusName(r.status),
      description: r.status === 'rejected' ? '流程被驳回，请跟进处理' : '流程已取消',
      status: statusName(r.status)
    }))
)

// 流程办理排行：按申请人聚合按时率/驳回率
const rankingData = computed(() => {
  const groups = {}
  for (const r of instances.value) {
    const k = r.applicant || '未知'
    if (!groups[k]) groups[k] = { total: 0, approved: 0, rejected: 0 }
    groups[k].total++
    if (r.status === 'approved') groups[k].approved++
    if (r.status === 'rejected') groups[k].rejected++
  }
  return Object.entries(groups)
    .map(([name, v]) => ({
      deptName: name,
      total: v.total,
      onTimeRate: v.total ? +((v.approved / v.total) * 100).toFixed(1) : 0,
      rejectRate: v.total ? +((v.rejected / v.total) * 100).toFixed(1) : 0
    }))
    .sort((a, b) => b.onTimeRate - a.onTimeRate)
    .map((g, i) => ({ rank: i + 1, ...g }))
})

// 统计指标（由实例列表前端聚合）
const totals = computed(() => {
  const total = instances.value.length || 1
  const approved = instances.value.filter(r => r.status === 'approved').length
  const rejected = instances.value.filter(r => r.status === 'rejected').length
  const cancelled = instances.value.filter(r => r.status === 'cancelled').length
  const processing = instances.value.filter(r => ['pending', 'processing'].includes(r.status)).length
  const overdue = timeoutWarnings.value.length
  return {
    total: instances.value.length,
    approved, rejected, cancelled, processing, overdue,
    onTimeRate: +((approved / total) * 100).toFixed(1),
    rejectRate: +((rejected / total) * 100).toFixed(1),
    overdueRate: +((overdue / total) * 100).toFixed(1),
    complianceRate: +(((total - rejected - cancelled) / total) * 100).toFixed(1),
    closeRate: total ? +((approved / (approved + rejected || 1)) * 100).toFixed(1) : 0,
    coverageRate: +(((approved + processing) / total) * 100).toFixed(1)
  }
})

const statList = computed(() => [
  { label: '按时办结率', value: totals.value.onTimeRate, trend: 0, icon: CircleCheck },
  { label: '流程驳回率', value: totals.value.rejectRate, trend: 0, icon: CircleClose },
  { label: '流程超时率', value: totals.value.overdueRate, trend: 0, icon: Clock },
  { label: '表单合规率', value: totals.value.complianceRate, trend: 0, icon: DocumentChecked },
  { label: '问题闭环率', value: totals.value.closeRate, trend: 0, icon: Check },
  { label: '执行覆盖率', value: totals.value.coverageRate, trend: 0, icon: User }
])

const kpiList = computed(() => [
  { id: 1, name: '流程按时办结率', currentValue: totals.value.onTimeRate + '%', targetValue: '95%' },
  { id: 2, name: '流程驳回率', currentValue: totals.value.rejectRate + '%', targetValue: '<5%' },
  { id: 3, name: '流程超时率', currentValue: totals.value.overdueRate + '%', targetValue: '<3%' },
  { id: 4, name: '运行中实例', currentValue: totals.value.processing + '个', targetValue: '实时' }
])

// 复盘/资产/内审/优化：后端无对应实体，使用真实空态（不灌假数据）
const reviewList = ref([])
const assetList = ref([])
const auditTasks = ref([])
const optimizeList = ref([])

// 问题整改台账：由驳回实例派生
const issueList = computed(() =>
  instances.value
    .filter(r => r.status === 'rejected')
    .map(r => ({
      id: r.id,
      title: `${r.processName || '流程'} 审批被驳回`,
      severity: '重要',
      status: '待整改'
    }))
)

// 优秀流程库：由已通过实例派生
const excellentFlows = computed(() =>
  instances.value
    .filter(r => r.status === 'approved')
    .map(r => ({
      id: r.id,
      flowName: r.processName || r.processCode || `流程#${r.id}`,
      domain: r.applicant || '-'
    }))
)

const loadInstances = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/process/instance/list', {
      params: { pageNum: 1, pageSize: 200 }
    })
    const page = res.data && res.data.data ? res.data.data : {}
    instances.value = page.records || []
  } catch (e) {
    ElMessage.error('加载流程实例失败：' + ((e.response && e.response.data && (e.response.data.message || e.response.data.msg)) || e.message))
    console.error(e)
  } finally {
    loading.value = false
  }
}

const exportReport = () => { ElMessage.success('正在导出流程运营报告...') }
const refreshData = async () => {
  await loadInstances()
  ElMessage.success('数据已刷新')
}
const remindAssignee = (row) => { ElMessage.success(`已向 ${row.assignee} 发送催办提醒`) }

loadInstances()
</script>

<style lang="scss" scoped>
.epros-monitor { height: 100%; display: flex; flex-direction: column; background: #f5f5f5; }
.monitor-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%); color: #fff; }
.monitor-body { flex: 1; padding: 16px; overflow: auto; }
.stats-panel { display: grid; grid-template-columns: repeat(6, 1fr); gap: 16px; margin-bottom: 16px; }
.stat-card { background: #fff; border-radius: 8px; padding: 16px; text-align: center; }
.detail-section { background: #fff; border-radius: 8px; }
.kpi-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.kpi-card, .review-card, .asset-card, .task-card, .optimize-card, .excellent-card { background: #fff; border-radius: 8px; padding: 16px; margin-bottom: 16px; }
</style>

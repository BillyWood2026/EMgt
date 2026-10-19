<template>
  <div class="page-container">
    <div class="page-header">
      <h2>性能监控</h2>
      <el-button @click="handleRefresh">刷新</el-button>
    </div>

    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon blue"><el-icon><TrendCharts /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ avgResponseTime }}ms</div>
            <div class="stat-label">平均响应时间</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon green"><el-icon><Odometer /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ qps }}</div>
            <div class="stat-label">QPS</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon orange"><el-icon><Warning /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ errorRate }}%</div>
            <div class="stat-label">错误率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon purple"><el-icon><Timer /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ uptime }}</div>
            <div class="stat-label">系统可用性</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>CPU使用率趋势</span>
          </template>
          <div class="chart-container">
            <div class="chart-bars">
              <div v-for="(item, index) in cpuTrend" :key="index" class="chart-bar-wrapper">
                <div class="chart-bar" :style="{ height: item.value + '%', backgroundColor: getBarColor(item.value) }"></div>
                <span class="chart-label">{{ item.label }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>内存使用率趋势</span>
          </template>
          <div class="chart-container">
            <div class="chart-bars">
              <div v-for="(item, index) in memoryTrend" :key="index" class="chart-bar-wrapper">
                <div class="chart-bar" :style="{ height: item.value + '%', backgroundColor: getBarColor(item.value) }"></div>
                <span class="chart-label">{{ item.label }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 20px;">
      <template #header>
        <span>接口性能排行</span>
      </template>
      <el-table :data="apiList" border stripe size="small">
        <el-table-column prop="rank" label="排名" width="60">
          <template #default="{row}">
            <el-tag v-if="row.rank <= 3" :type="row.rank === 1 ? 'danger' : row.rank === 2 ? 'warning' : 'success'" size="small">{{ row.rank }}</el-tag>
            <span v-else>{{ row.rank }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="api" label="接口名称" min-width="200" />
        <el-table-column prop="method" label="方法" width="80">
          <template #default="{row}">
            <el-tag :type="row.method === 'POST' ? 'danger' : 'success'" size="small">{{ row.method }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" width="250" />
        <el-table-column prop="avgTime" label="平均响应时间" width="120">
          <template #default="{row}">
            <span :style="{ color: row.avgTime > 500 ? '#f56c6c' : '#67c23a' }">{{ row.avgTime }}ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="maxTime" label="最大响应时间" width="120">
          <template #default="{row}">
            <span>{{ row.maxTime }}ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="qps" label="QPS" width="80" />
        <el-table-column prop="errorRate" label="错误率" width="100">
          <template #default="{row}">
            <span :style="{ color: row.errorRate > 1 ? '#f56c6c' : '#666' }">{{ row.errorRate }}%</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { TrendCharts, Odometer, Warning, Timer } from '@element-plus/icons-vue'

const avgResponseTime = ref(125)
const qps = ref(2340)
const errorRate = ref(0.3)
const uptime = ref('99.97%')

const cpuTrend = ref([
  { label: '08:00', value: 45 },
  { label: '10:00', value: 62 },
  { label: '12:00', value: 55 },
  { label: '14:00', value: 78 },
  { label: '16:00', value: 72 },
  { label: '18:00', value: 65 },
  { label: '20:00', value: 58 },
  { label: '22:00', value: 42 }
])

const memoryTrend = ref([
  { label: '08:00', value: 58 },
  { label: '10:00', value: 65 },
  { label: '12:00', value: 62 },
  { label: '14:00', value: 75 },
  { label: '16:00', value: 72 },
  { label: '18:00', value: 68 },
  { label: '20:00', value: 65 },
  { label: '22:00', value: 55 }
])

const apiList = ref([
  { rank: 1, api: '员工列表查询', method: 'GET', path: '/api/employee/list', avgTime: 850, maxTime: 1200, qps: 450, errorRate: 0.5 },
  { rank: 2, api: '薪资计算', method: 'POST', path: '/api/salary/calculate', avgTime: 620, maxTime: 980, qps: 120, errorRate: 0.2 },
  { rank: 3, api: '绩效评估', method: 'POST', path: '/api/performance/evaluate', avgTime: 450, maxTime: 720, qps: 80, errorRate: 0.1 },
  { rank: 4, api: '登录认证', method: 'POST', path: '/api/auth/login', avgTime: 120, maxTime: 350, qps: 200, errorRate: 0.8 },
  { rank: 5, api: '部门树查询', method: 'GET', path: '/api/department/tree', avgTime: 85, maxTime: 200, qps: 320, errorRate: 0 },
  { rank: 6, api: '培训计划列表', method: 'GET', path: '/api/training/list', avgTime: 150, maxTime: 420, qps: 180, errorRate: 0.3 },
  { rank: 7, api: '招聘需求列表', method: 'GET', path: '/api/recruitment/list', avgTime: 95, maxTime: 280, qps: 150, errorRate: 0 },
  { rank: 8, api: '战略指标查询', method: 'GET', path: '/api/strategy/indicator', avgTime: 280, maxTime: 550, qps: 60, errorRate: 0.2 }
])

function getBarColor(val) {
  if (val >= 90) return '#f56c6c'
  if (val >= 70) return '#e6a23c'
  return '#67c23a'
}

function handleRefresh() {
  el.message.success('数据已刷新')
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}

.stat-icon.blue { background-color: #e6f7ff; color: #1890ff; }
.stat-icon.green { background-color: #f6ffed; color: #52c41a; }
.stat-icon.orange { background-color: #fff7e6; color: #fa8c16; }
.stat-icon.purple { background-color: #f9f0ff; color: #722ed1; }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.chart-container {
  height: 200px;
}

.chart-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 100%;
  padding: 10px 0;
}

.chart-bar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 10%;
}

.chart-bar {
  width: 24px;
  border-radius: 4px 4px 0 0;
  min-height: 4px;
  transition: height 0.3s ease;
}

.chart-label {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}
</style>
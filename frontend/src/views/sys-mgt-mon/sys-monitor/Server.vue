<template>
  <div class="page-container">
    <div class="page-header">
      <h2>服务器监控</h2>
      <el-button @click="handleRefresh">刷新</el-button>
    </div>

    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon blue"><el-icon><Monitor /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ serverCount }}</div>
            <div class="stat-label">服务器总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon green"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ runningCount }}</div>
            <div class="stat-label">运行中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon orange"><el-icon><Warning /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ warningCount }}</div>
            <div class="stat-label">告警</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon red"><el-icon><CircleClose /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ offlineCount }}</div>
            <div class="stat-label">离线</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-table :data="serverList" border stripe size="small">
      <el-table-column prop="name" label="服务器名称" width="150" />
      <el-table-column prop="ip" label="IP地址" width="150" />
      <el-table-column prop="type" label="服务器类型" width="120">
        <template #default="{row}">
          <el-tag size="small">{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="cpu" label="CPU使用率" width="120">
        <template #default="{row}">
          <div class="progress-mini">
            <el-progress :percentage="row.cpu" :color="getProgressColor(row.cpu)" :show-text="false" />
            <span class="progress-text">{{ row.cpu }}%</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="memory" label="内存使用率" width="120">
        <template #default="{row}">
          <div class="progress-mini">
            <el-progress :percentage="row.memory" :color="getProgressColor(row.memory)" :show-text="false" />
            <span class="progress-text">{{ row.memory }}%</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="disk" label="磁盘使用率" width="120">
        <template #default="{row}">
          <div class="progress-mini">
            <el-progress :percentage="row.disk" :color="getProgressColor(row.disk)" :show-text="false" />
            <span class="progress-text">{{ row.disk }}%</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastUpdate" label="最后更新" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{row}">
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="服务器详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="服务器名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detailData.ip }}</el-descriptions-item>
        <el-descriptions-item label="服务器类型">{{ detailData.type }}</el-descriptions-item>
        <el-descriptions-item label="操作系统">{{ detailData.os }}</el-descriptions-item>
        <el-descriptions-item label="CPU核心数">{{ detailData.cpuCores }}</el-descriptions-item>
        <el-descriptions-item label="内存大小">{{ detailData.memorySize }}</el-descriptions-item>
        <el-descriptions-item label="CPU使用率" :span="2">
          <el-progress :percentage="detailData.cpu" :color="getProgressColor(detailData.cpu)" />
        </el-descriptions-item>
        <el-descriptions-item label="内存使用率" :span="2">
          <el-progress :percentage="detailData.memory" :color="getProgressColor(detailData.memory)" />
        </el-descriptions-item>
        <el-descriptions-item label="磁盘使用率" :span="2">
          <el-progress :percentage="detailData.disk" :color="getProgressColor(detailData.disk)" />
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)" size="small">{{ getStatusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="最后更新">{{ detailData.lastUpdate }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const serverCount = ref(8)
const runningCount = ref(6)
const warningCount = ref(1)
const offlineCount = ref(1)

const detailVisible = ref(false)
const detailData = reactive({})

const serverList = ref([
  { id: 1, name: '生产服务器1', ip: '192.168.1.10', type: '生产环境', cpu: 65, memory: 72, disk: 58, status: 'running', os: 'CentOS 7.9', cpuCores: 16, memorySize: '32GB', lastUpdate: '2024-01-15 14:30:00' },
  { id: 2, name: '生产服务器2', ip: '192.168.1.11', type: '生产环境', cpu: 78, memory: 85, disk: 62, status: 'warning', os: 'CentOS 7.9', cpuCores: 16, memorySize: '32GB', lastUpdate: '2024-01-15 14:30:00' },
  { id: 3, name: '测试服务器1', ip: '192.168.1.20', type: '测试环境', cpu: 35, memory: 42, disk: 45, status: 'running', os: 'Ubuntu 20.04', cpuCores: 8, memorySize: '16GB', lastUpdate: '2024-01-15 14:30:00' },
  { id: 4, name: '测试服务器2', ip: '192.168.1.21', type: '测试环境', cpu: 42, memory: 55, disk: 38, status: 'running', os: 'Ubuntu 20.04', cpuCores: 8, memorySize: '16GB', lastUpdate: '2024-01-15 14:30:00' },
  { id: 5, name: '开发服务器1', ip: '192.168.1.30', type: '开发环境', cpu: 28, memory: 35, disk: 52, status: 'running', os: 'CentOS 8', cpuCores: 8, memorySize: '16GB', lastUpdate: '2024-01-15 14:30:00' },
  { id: 6, name: '开发服务器2', ip: '192.168.1.31', type: '开发环境', cpu: 55, memory: 62, disk: 48, status: 'running', os: 'CentOS 8', cpuCores: 8, memorySize: '16GB', lastUpdate: '2024-01-15 14:30:00' },
  { id: 7, name: '数据库服务器', ip: '192.168.1.40', type: '数据库', cpu: 68, memory: 78, disk: 85, status: 'running', os: 'CentOS 7.9', cpuCores: 32, memorySize: '64GB', lastUpdate: '2024-01-15 14:30:00' },
  { id: 8, name: '备用服务器', ip: '192.168.1.50', type: '备用', cpu: 0, memory: 5, disk: 20, status: 'offline', os: 'CentOS 7.9', cpuCores: 16, memorySize: '32GB', lastUpdate: '2024-01-15 10:00:00' }
])

function getStatusLabel(status) {
  const map = { running: '运行中', warning: '告警', offline: '离线' }
  return map[status] || status
}

function getStatusType(status) {
  const map = { running: 'success', warning: 'warning', offline: 'danger' }
  return map[status] || 'info'
}

function getProgressColor(val) {
  if (val >= 90) return '#f56c6c'
  if (val >= 70) return '#e6a23c'
  return '#67c23a'
}

function viewDetail(row) {
  Object.assign(detailData, row)
  detailVisible.value = true
}

function handleRefresh() {
  import('element-plus').then(({ ElMessage }) => {
    ElMessage.success('数据已刷新')
  })
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
.stat-icon.red { background-color: #fff1f0; color: #f5222d; }

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

.progress-mini {
  display: flex;
  align-items: center;
}

.progress-mini .el-progress {
  flex: 1;
  margin-right: 8px;
}

.progress-text {
  font-size: 12px;
  color: #666;
  width: 40px;
  text-align: right;
}
</style>
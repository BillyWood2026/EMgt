<template>
  <div class="page-container">
    <div class="page-header">
      <h2>数据库监控</h2>
      <el-button @click="handleRefresh">刷新</el-button>
    </div>

    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon blue"><el-icon><Coin /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ dbCount }}</div>
            <div class="stat-label">数据库实例</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon green"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ connectionCount }}</div>
            <div class="stat-label">活跃连接</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon orange"><el-icon><Clock /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ slowQueryCount }}</div>
            <div class="stat-label">慢查询数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon purple"><el-icon><DataBoard /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ totalSize }}</div>
            <div class="stat-label">总存储</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="数据库列表" name="list">
        <el-table :data="dbList" border stripe size="small">
          <el-table-column prop="name" label="数据库名称" width="150" />
          <el-table-column prop="type" label="数据库类型" width="120">
            <template #default="{row}">
              <el-tag size="small">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="host" label="主机地址" width="150" />
          <el-table-column prop="port" label="端口" width="80" />
          <el-table-column prop="connections" label="连接数" width="100">
            <template #default="{row}">
              <span>{{ row.connections }}/{{ row.maxConnections }}</span>
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
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{row}">
              <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{row}">
              <el-button size="small" @click="viewDetail(row)">详情</el-button>
              <el-button size="small" type="warning" @click="handleOptimize(row)">优化</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="慢查询" name="slowquery">
        <el-table :data="slowQueryList" border stripe size="small">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="dbName" label="数据库" width="120" />
          <el-table-column prop="duration" label="执行时间" width="100">
            <template #default="{row}">
              <el-tag :type="row.duration > 10 ? 'danger' : 'warning'" size="small">{{ row.duration }}s</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="rows" label="扫描行数" width="100" />
          <el-table-column prop="sql" label="SQL语句" min-width="400" />
          <el-table-column prop="createTime" label="时间" width="180" />
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="detailVisible" title="数据库详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="数据库名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="数据库类型">{{ detailData.type }}</el-descriptions-item>
        <el-descriptions-item label="主机地址">{{ detailData.host }}</el-descriptions-item>
        <el-descriptions-item label="端口">{{ detailData.port }}</el-descriptions-item>
        <el-descriptions-item label="连接数">{{ detailData.connections }}/{{ detailData.maxConnections }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ detailData.version }}</el-descriptions-item>
        <el-descriptions-item label="CPU使用率" :span="2">
          <el-progress :percentage="detailData.cpu" :color="getProgressColor(detailData.cpu)" />
        </el-descriptions-item>
        <el-descriptions-item label="内存使用率" :span="2">
          <el-progress :percentage="detailData.memory" :color="getProgressColor(detailData.memory)" />
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

const dbCount = ref(4)
const connectionCount = ref(128)
const slowQueryCount = ref(5)
const totalSize = ref('850GB')

const activeTab = ref('list')
const detailVisible = ref(false)
const detailData = reactive({})

const dbList = ref([
  { id: 1, name: 'primary_db', type: 'MySQL', host: '192.168.1.40', port: 3306, connections: 45, maxConnections: 200, cpu: 65, memory: 72, status: 'running', version: '8.0.33', lastUpdate: '2024-01-15 14:30:00' },
  { id: 2, name: 'secondary_db', type: 'MySQL', host: '192.168.1.41', port: 3306, connections: 32, maxConnections: 200, cpu: 45, memory: 55, status: 'running', version: '8.0.33', lastUpdate: '2024-01-15 14:30:00' },
  { id: 3, name: 'redis_cache', type: 'Redis', host: '192.168.1.42', port: 6379, connections: 51, maxConnections: 1000, cpu: 28, memory: 68, status: 'running', version: '7.0.12', lastUpdate: '2024-01-15 14:30:00' },
  { id: 4, name: 'search_db', type: 'Elasticsearch', host: '192.168.1.43', port: 9200, connections: 0, maxConnections: 500, cpu: 35, memory: 75, status: 'running', version: '8.11.0', lastUpdate: '2024-01-15 14:30:00' }
])

const slowQueryList = ref([
  { id: 1, dbName: 'primary_db', duration: 15.2, rows: 150000, sql: 'SELECT * FROM employee WHERE department_id = 1 ORDER BY hire_date DESC', createTime: '2024-01-15 14:25:00' },
  { id: 2, dbName: 'primary_db', duration: 8.5, rows: 80000, sql: 'SELECT COUNT(*) FROM salary_record WHERE month = \'2024-01\'', createTime: '2024-01-15 14:20:00' },
  { id: 3, dbName: 'primary_db', duration: 5.2, rows: 50000, sql: 'SELECT * FROM performance WHERE score < 60', createTime: '2024-01-15 14:15:00' },
  { id: 4, dbName: 'secondary_db', duration: 12.8, rows: 120000, sql: 'SELECT * FROM training_record WHERE employee_id IN (SELECT id FROM employee WHERE department=\'技术部\')', createTime: '2024-01-15 14:10:00' },
  { id: 5, dbName: 'primary_db', duration: 3.5, rows: 30000, sql: 'SELECT * FROM recruitment WHERE status = \'pending\'', createTime: '2024-01-15 14:05:00' }
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

function handleOptimize(row) {
  import('element-plus').then(({ ElMessage }) => {
    ElMessage.info(`优化数据库: ${row.name}`)
  })
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
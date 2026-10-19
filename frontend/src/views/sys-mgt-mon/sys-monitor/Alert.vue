<template>
  <div class="page-container">
    <div class="page-header">
      <h2>告警管理</h2>
      <div>
        <el-button type="primary" @click="handleAckAll">全部确认</el-button>
        <el-button @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon blue"><el-icon><Bell /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ totalCount }}</div>
            <div class="stat-label">告警总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon red"><el-icon><Warning /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ criticalCount }}</div>
            <div class="stat-label">严重告警</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon orange"><el-icon><InfoFilled /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ warningCount }}</div>
            <div class="stat-label">警告告警</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon green"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ ackCount }}</div>
            <div class="stat-label">已确认</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="告警级别">
        <el-select v-model="searchForm.level" placeholder="选择级别" style="width:120px">
          <el-option label="严重" value="critical" />
          <el-option label="警告" value="warning" />
          <el-option label="信息" value="info" />
        </el-select>
      </el-form-item>
      <el-form-item label="告警来源">
        <el-select v-model="searchForm.source" placeholder="选择来源" style="width:150px">
          <el-option label="服务器" value="server" />
          <el-option label="数据库" value="database" />
          <el-option label="应用" value="application" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width:120px">
          <el-option label="未确认" value="unack" />
          <el-option label="已确认" value="ack" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="alertList" border stripe size="small">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="level" label="级别" width="100">
        <template #default="{row}">
          <el-tag :type="getLevelType(row.level)" size="small">{{ getLevelLabel(row.level) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="告警标题" min-width="200" />
      <el-table-column prop="source" label="告警来源" width="120">
        <template #default="{row}">
          <el-tag size="small">{{ getSourceLabel(row.source) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sourceName" label="来源名称" width="150" />
      <el-table-column prop="message" label="告警信息" min-width="250" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status === 'ack' ? 'success' : 'warning'" size="small">
            {{ row.status === 'ack' ? '已确认' : '未确认' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发生时间" width="180" />
      <el-table-column label="操作" width="150">
        <template #default="{row}">
          <el-button v-if="row.status === 'unack'" size="small" type="primary" @click="handleAck(row)">确认</el-button>
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="pagination.currentPage"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pagination.pageSize"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top: 20px; text-align: right;"
    />

    <el-dialog v-model="detailVisible" title="告警详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="告警ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="告警级别">
          <el-tag :type="getLevelType(detailData.level)" size="small">{{ getLevelLabel(detailData.level) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警标题">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="告警来源">
          <el-tag size="small">{{ getSourceLabel(detailData.source) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="来源名称">{{ detailData.sourceName }}</el-descriptions-item>
        <el-descriptions-item label="告警状态">
          <el-tag :type="detailData.status === 'ack' ? 'success' : 'warning'" size="small">
            {{ detailData.status === 'ack' ? '已确认' : '未确认' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警信息" :span="2">{{ detailData.message }}</el-descriptions-item>
        <el-descriptions-item label="详细内容" :span="2">
          <pre style="white-space: pre-wrap; word-break: break-all; max-height: 200px; overflow-y: auto;">{{ detailData.details }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="发生时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="确认时间">{{ detailData.ackTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="detailData.status === 'unack'" type="primary" @click="handleAckDetail">确认告警</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const totalCount = ref(15)
const criticalCount = ref(3)
const warningCount = ref(8)
const ackCount = ref(4)

const searchForm = reactive({
  level: '',
  source: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 50
})

const detailVisible = ref(false)
const detailData = reactive({})

const alertList = ref([
  { id: 1, level: 'critical', title: '服务器CPU使用率过高', source: 'server', sourceName: '生产服务器2', message: 'CPU使用率超过90%', status: 'unack', createTime: '2024-01-15 14:25:00', details: 'CPU使用率: 95%\n内存使用率: 85%\n时间: 2024-01-15 14:25:00\n建议: 检查进程占用，考虑扩容' },
  { id: 2, level: 'critical', title: '数据库连接池耗尽', source: 'database', sourceName: 'primary_db', message: '数据库连接数达到上限', status: 'unack', createTime: '2024-01-15 14:20:00', details: '当前连接数: 200/200\n最大连接数: 200\n时间: 2024-01-15 14:20:00\n建议: 增加连接池大小或优化查询' },
  { id: 3, level: 'warning', title: '应用响应时间过长', source: 'application', sourceName: 'HR系统', message: '接口平均响应时间超过500ms', status: 'unack', createTime: '2024-01-15 14:15:00', details: '平均响应时间: 620ms\n最大响应时间: 1200ms\n接口: /api/employee/list\n时间: 2024-01-15 14:15:00' },
  { id: 4, level: 'warning', title: '服务器磁盘使用率过高', source: 'server', sourceName: '数据库服务器', message: '磁盘使用率超过85%', status: 'ack', createTime: '2024-01-15 13:30:00', ackTime: '2024-01-15 13:35:00', details: '磁盘使用率: 88%\n磁盘大小: 500GB\n已用: 440GB\n时间: 2024-01-15 13:30:00' },
  { id: 5, level: 'info', title: '服务器离线告警', source: 'server', sourceName: '备用服务器', message: '备用服务器离线', status: 'ack', createTime: '2024-01-15 10:00:00', ackTime: '2024-01-15 10:05:00', details: '服务器IP: 192.168.1.50\n离线时间: 2024-01-15 10:00:00\n状态: 预期内维护' },
  { id: 6, level: 'warning', title: '慢查询告警', source: 'database', sourceName: 'primary_db', message: '检测到慢查询', status: 'unack', createTime: '2024-01-15 14:10:00', details: '查询耗时: 15.2s\n扫描行数: 150000\nSQL: SELECT * FROM employee WHERE department_id = 1 ORDER BY hire_date DESC\n时间: 2024-01-15 14:10:00' },
  { id: 7, level: 'critical', title: '内存使用率过高', source: 'server', sourceName: '生产服务器2', message: '内存使用率超过90%', status: 'unack', createTime: '2024-01-15 14:28:00', details: '内存使用率: 92%\n内存大小: 32GB\n已用: 29.4GB\n时间: 2024-01-15 14:28:00\n建议: 检查内存泄漏或增加内存' },
  { id: 8, level: 'warning', title: 'API错误率上升', source: 'application', sourceName: 'HR系统', message: 'API错误率超过1%', status: 'ack', createTime: '2024-01-15 12:00:00', ackTime: '2024-01-15 12:10:00', details: '错误率: 1.2%\n接口: /api/auth/login\n时间: 2024-01-15 12:00:00\n已恢复' }
])

function getLevelLabel(level) {
  const map = { critical: '严重', warning: '警告', info: '信息' }
  return map[level] || level
}

function getLevelType(level) {
  const map = { critical: 'danger', warning: 'warning', info: 'info' }
  return map[level] || 'info'
}

function getSourceLabel(source) {
  const map = { server: '服务器', database: '数据库', application: '应用' }
  return map[source] || source
}

function handleSearch() {
  pagination.currentPage = 1
}

function resetSearch() {
  searchForm.level = ''
  searchForm.source = ''
  searchForm.status = ''
  pagination.currentPage = 1
}

function handleSizeChange(val) {
  pagination.pageSize = val
}

function handleCurrentChange(val) {
  pagination.currentPage = val
}

function viewDetail(row) {
  Object.assign(detailData, row)
  detailVisible.value = true
}

function handleAck(row) {
  row.status = 'ack'
  row.ackTime = '2024-01-15 ' + new Date().toLocaleTimeString('zh-CN', { hour12: false })
  el.message.success('告警已确认')
}

function handleAckDetail() {
  detailData.status = 'ack'
  detailData.ackTime = '2024-01-15 ' + new Date().toLocaleTimeString('zh-CN', { hour12: false })
  detailVisible.value = false
  el.message.success('告警已确认')
}

function handleAckAll() {
  el.confirm('确定要确认所有告警吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    alertList.value.forEach(item => {
      if (item.status === 'unack') {
        item.status = 'ack'
        item.ackTime = '2024-01-15 ' + new Date().toLocaleTimeString('zh-CN', { hour12: false })
      }
    })
    el.message.success('所有告警已确认')
  })
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
.stat-icon.red { background-color: #fff1f0; color: #f5222d; }
.stat-icon.orange { background-color: #fff7e6; color: #fa8c16; }
.stat-icon.green { background-color: #f6ffed; color: #52c41a; }

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

.search-form {
  margin-bottom: 20px;
}
</style>
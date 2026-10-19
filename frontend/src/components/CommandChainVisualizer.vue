<template>
  <div class="chain-visualizer">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="zoom-controls">
        <button @click="zoomOut" class="btn btn-default">−</button>
        <span>{{ zoomLevel }}%</span>
        <button @click="zoomIn" class="btn btn-default">+</button>
        <button @click="resetZoom" class="btn btn-default">1:1</button>
      </div>
      <div class="action-controls">
        <button @click="handleScreenshot" class="btn btn-secondary">📷 截图</button>
        <button @click="handleExportPdf" class="btn btn-secondary">📄 导出PDF</button>
        <button @click="toggleFullscreen" class="btn btn-default">{{ isFullscreen ? '退出全屏' : '全屏' }}</button>
        <button @click="handleLocateAbnormal" class="btn btn-warning">🔍 定位异常</button>
      </div>
    </div>
    
    <!-- 可视化区域 -->
    <div class="visualization-area" :style="{ transform: `scale(${zoomLevel / 100})` }">
      <!-- 主链 -->
      <div class="main-chain">
        <div v-for="(node, index) in chainNodes" :key="node.id" class="chain-node-wrapper">
          <div 
            class="chain-node" 
            :class="[getNodeClass(node)]"
            @mouseenter="showNodeTooltip(node, $event)"
            @mouseleave="hideTooltip"
          >
            <div class="node-header">
              <div class="node-order">{{ index + 1 }}</div>
              <div class="node-status" :class="getNodeStatusClass(node)"></div>
            </div>
            <div class="node-content">
              <div class="node-icon">{{ getNodeIcon(node.nodeType) }}</div>
              <div class="node-title">{{ node.title }}</div>
              <div class="node-person">{{ node.person }}</div>
              <div class="node-permissions">
                <span v-for="perm in node.permissions" :key="perm" class="perm-tag">{{ perm }}</span>
              </div>
            </div>
            <div class="node-timer" v-if="node.dueTime">
              ⏱️ {{ node.dueTime }}
            </div>
          </div>
          
          <!-- 箭头 -->
          <div v-if="index < chainNodes.length - 1" class="node-connector">
            <div class="connector-line" :class="{ 'cross-department': chainNodes[index + 1]?.crossDepartment }"></div>
            <div class="connector-arrow">▼</div>
          </div>
        </div>
      </div>
      
      <!-- 分支节点 -->
      <div v-if="branchNodes.length > 0" class="branch-nodes">
        <div v-for="branch in branchNodes" :key="branch.id" class="branch-group">
          <div class="branch-label">{{ branch.branchName }}</div>
          <div class="branch-chain">
            <div v-for="(node, index) in branch.nodes" :key="node.id" class="branch-node">
              <div 
                class="chain-node small" 
                :class="[getNodeClass(node)]"
                @mouseenter="showNodeTooltip(node, $event)"
                @mouseleave="hideTooltip"
              >
                <div class="node-title">{{ node.title }}</div>
                <div class="node-person">{{ node.person }}</div>
              </div>
              <div v-if="index < branch.nodes.length - 1" class="branch-connector"></div>
            </div>
          </div>
          <div class="branch-connection" :style="{ top: branch.attachTo * 100 + '%' }"></div>
        </div>
      </div>
    </div>
    
    <!-- 图例 -->
    <div class="legend">
      <div class="legend-item">
        <span class="legend-color normal"></span>
        <span>正常</span>
      </div>
      <div class="legend-item">
        <span class="legend-color timeout"></span>
        <span>超时</span>
      </div>
      <div class="legend-item">
        <span class="legend-color pending"></span>
        <span>待处理</span>
      </div>
      <div class="legend-item">
        <span class="legend-color rejected"></span>
        <span>已驳回</span>
      </div>
      <div class="legend-item">
        <span class="legend-color disabled"></span>
        <span>已停用</span>
      </div>
    </div>
    
    <!-- 节点提示框 -->
    <div v-if="tooltipVisible" class="node-tooltip" :style="tooltipStyle">
      <h4>{{ tooltipData.title }}</h4>
      <div class="tooltip-row">
        <span class="label">岗位：</span>
        <span>{{ tooltipData.position }}</span>
      </div>
      <div class="tooltip-row">
        <span class="label">人员：</span>
        <span>{{ tooltipData.person }}</span>
      </div>
      <div class="tooltip-row">
        <span class="label">权限：</span>
        <span>{{ tooltipData.permissions?.join('、') }}</span>
      </div>
      <div class="tooltip-row">
        <span class="label">序号：</span>
        <span>{{ tooltipData.order }}</span>
      </div>
      <div class="tooltip-row">
        <span class="label">到期时间：</span>
        <span>{{ tooltipData.dueTime }}</span>
      </div>
      <div class="tooltip-row">
        <span class="label">状态：</span>
        <span :class="['status-badge', tooltipData.status?.toLowerCase()]">{{ formatStatus(tooltipData.status) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

// 缩放控制
const zoomLevel = ref(100)
const isFullscreen = ref(false)

// 节点提示框
const tooltipVisible = ref(false)
const tooltipData = reactive({})
const tooltipStyle = reactive({})

// 模拟数据
const chainNodes = ref([])
const branchNodes = ref([])

// 根据状态获取节点样式类
const getNodeClass = (node) => {
  return {
    'timeout': node.status === 'TIMEOUT',
    'pending': node.status === 'PENDING',
    'rejected': node.status === 'REJECTED',
    'disabled': node.status === 'DISABLED',
    'cross-department': node.crossDepartment
  }
}

const getNodeStatusClass = (node) => {
  return node.status?.toLowerCase() || 'normal'
}

// 状态码转中文显示
const formatStatus = (status) => {
  const map = {
    NORMAL: '正常',
    COMPLETED: '已完成',
    PENDING: '待处理',
    TIMEOUT: '超时',
    REJECTED: '已驳回',
    DISABLED: '已停用'
  }
  return map[status?.toUpperCase()] || status
}

const getNodeIcon = (type) => {
  const icons = {
    'MANAGER': '👔',
    'DEPARTMENT_HEAD': '🏢',
    'DIRECTOR': '🎯',
    'STAFF': '👤',
    'SYSTEM': '🤖'
  }
  return icons[type] || '👤'
}

const showNodeTooltip = (node, event) => {
  tooltipData.title = node.title
  tooltipData.position = node.position || '暂无'
  tooltipData.person = node.person
  tooltipData.permissions = node.permissions
  tooltipData.order = node.order
  tooltipData.dueTime = node.dueTime
  tooltipData.status = node.status
  
  tooltipStyle.left = (event.clientX + 15) + 'px'
  tooltipStyle.top = (event.clientY + 15) + 'px'
  tooltipVisible.value = true
}

const hideTooltip = () => {
  tooltipVisible.value = false
}

// 缩放功能
const zoomIn = () => {
  if (zoomLevel.value < 200) {
    zoomLevel.value += 10
  }
}

const zoomOut = () => {
  if (zoomLevel.value > 50) {
    zoomLevel.value -= 10
  }
}

const resetZoom = () => {
  zoomLevel.value = 100
}

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

const handleScreenshot = () => {
  alert('正在截图...')
}

const handleExportPdf = () => {
  alert('正在导出PDF...')
}

const handleLocateAbnormal = () => {
  alert('正在定位异常节点...')
}

// 加载模拟数据
const loadData = () => {
  chainNodes.value = [
    { id: 1, title: '部门经理', person: '张明', position: '经理', nodeType: 'DEPARTMENT_HEAD', permissions: ['审批', '审核'], order: 1, status: 'COMPLETED', dueTime: '2小时前' },
    { id: 2, title: '人力资源专员', person: '李娟', position: 'HR专员', nodeType: 'STAFF', permissions: ['审核', '核对'], order: 2, status: 'PENDING', dueTime: '1天2小时' },
    { id: 3, title: '事业部总监', person: '王强', position: '总监', nodeType: 'DIRECTOR', permissions: ['审批', '上报'], order: 3, status: 'NORMAL', dueTime: '2天' },
    { id: 4, title: '运营副总裁', person: '刘芳', position: '副总裁', nodeType: 'MANAGER', permissions: ['最终审批'], order: 4, status: 'NORMAL', dueTime: '3天', crossDepartment: true },
    { id: 5, title: '首席执行官', person: '赵磊', position: '首席执行官', nodeType: 'MANAGER', permissions: ['最终核准'], order: 5, status: 'DISABLED', dueTime: '-' }
  ]
  
  branchNodes.value = [
    { 
      id: 'b1', 
      branchName: '财务审核', 
      attachTo: 0.3,
      nodes: [
        { id: 'b1-1', title: '财务经理', person: '周涛', status: 'COMPLETED' },
        { id: 'b1-2', title: '会计', person: '吴敏', status: 'NORMAL' }
      ]
    },
    { 
      id: 'b2', 
      branchName: '法务审核', 
      attachTo: 0.7,
      nodes: [
        { id: 'b2-1', title: '法务顾问', person: '郑浩', status: 'PENDING' }
      ]
    }
  ]
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.chain-visualizer {
  position: relative;
  height: 100%;
  background-color: #fafafa;
  border-radius: 8px;
  overflow: hidden;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
}

.zoom-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-controls {
  display: flex;
  gap: 10px;
}

.visualization-area {
  position: relative;
  padding: 40px;
  height: calc(100% - 50px);
  overflow: auto;
  transform-origin: top left;
}

.main-chain {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0;
}

.chain-node-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.chain-node {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 15px 20px;
  min-width: 200px;
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}

.chain-node:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.6);
}

.chain-node.small {
  min-width: 150px;
  padding: 10px 15px;
}

.chain-node.timeout {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  animation: pulse 2s infinite;
}

.chain-node.pending {
  background: linear-gradient(135deg, #feca57 0%, #ff9f43 100%);
}

.chain-node.rejected {
  background: linear-gradient(135deg, #ff4757 0%, #c0392b 100%);
}

.chain-node.disabled {
  background: linear-gradient(135deg, #a0aec0 0%, #718096 100%);
  opacity: 0.6;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4); }
  50% { box-shadow: 0 4px 25px rgba(255, 107, 107, 0.8); }
}

.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.node-order {
  background-color: rgba(255, 255, 255, 0.3);
  padding: 3px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.node-status {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #67C23A;
}

.node-status.timeout { background-color: #F56C6C; }
.node-status.pending { background-color: #E6A23C; }
.node-status.rejected { background-color: #F56C6C; }
.node-status.disabled { background-color: #909399; }

.node-content {
  text-align: center;
}

.node-icon {
  font-size: 28px;
  margin-bottom: 5px;
}

.node-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 3px;
}

.node-person {
  font-size: 12px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.node-permissions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 4px;
}

.perm-tag {
  background-color: rgba(255, 255, 255, 0.2);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
}

.node-timer {
  font-size: 11px;
  opacity: 0.8;
  margin-top: 8px;
  text-align: center;
}

.node-connector {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 50px;
}

.connector-line {
  width: 3px;
  flex: 1;
  background-color: #ddd;
  position: relative;
}

.connector-line.cross-department {
  background-color: #E6A23C;
  background: repeating-linear-gradient(
    90deg,
    #E6A23C,
    #E6A23C 5px,
    #fff 5px,
    #fff 10px
  );
}

.connector-arrow {
  color: #667eea;
  font-size: 16px;
}

.chain-node.timeout + .node-connector .connector-arrow { color: #ff6b6b; }
.chain-node.pending + .node-connector .connector-arrow { color: #feca57; }

.branch-nodes {
  position: absolute;
  top: 40px;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.branch-group {
  position: absolute;
  left: 20px;
  width: 180px;
  pointer-events: auto;
}

.branch-label {
  font-size: 12px;
  color: #666;
  font-weight: bold;
  margin-bottom: 10px;
  padding-left: 5px;
}

.branch-chain {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.branch-node .chain-node {
  min-width: 160px;
  padding: 10px;
}

.branch-connector {
  width: 100%;
  height: 8px;
  border-left: 2px dashed #ddd;
  margin-left: 45%;
}

.branch-connection {
  position: absolute;
  right: -20px;
  width: 20px;
  height: 2px;
  background-color: #ddd;
}

.legend {
  position: absolute;
  bottom: 15px;
  right: 15px;
  display: flex;
  gap: 15px;
  background-color: rgba(255, 255, 255, 0.9);
  padding: 10px 15px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 4px;
}

.legend-color.normal { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.legend-color.timeout { background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%); }
.legend-color.pending { background: linear-gradient(135deg, #feca57 0%, #ff9f43 100%); }
.legend-color.rejected { background: linear-gradient(135deg, #ff4757 0%, #c0392b 100%); }
.legend-color.disabled { background: linear-gradient(135deg, #a0aec0 0%, #718096 100%); }

.node-tooltip {
  position: fixed;
  background-color: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  z-index: 1000;
  min-width: 200px;
  max-width: 300px;
}

.node-tooltip h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.tooltip-row {
  display: flex;
  gap: 10px;
  margin-bottom: 6px;
  font-size: 13px;
}

.tooltip-row .label {
  color: #999;
  font-weight: bold;
}

.status-badge {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}

.status-badge.completed { background-color: #E8F5E9; color: #27AE60; }
.status-badge.pending { background-color: #FFFBE6; color: #E6A23C; }
.status-badge.normal { background-color: #E3F2FD; color: #1976D2; }
.status-badge.timeout { background-color: #FEF0F0; color: #F56C6C; }
.status-badge.disabled { background-color: #F5F5F5; color: #9E9E9E; }

.btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-primary { background-color: #409EFF; color: white; }
.btn-secondary { background-color: #67C23A; color: white; }
.btn-default { background-color: #f5f5f5; color: #666; }
.btn-warning { background-color: #E6A23C; color: white; }
</style>
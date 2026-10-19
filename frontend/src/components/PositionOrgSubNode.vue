<template>
  <div class="position-sub-node-wrapper">
    <div 
      class="org-node" 
      :class="['level-' + node.orgLevel]"
    >
      <div class="org-header">
        <span class="org-icon">{{ getOrgIcon(node.orgLevel) }}</span>
        <span class="org-name">{{ node.orgName }}</span>
      </div>
      <div class="org-stats">
        <span>{{ node.positionCount || 0 }} 个岗位</span>
      </div>
    </div>
    
    <!-- 岗位列表 -->
    <div v-if="node.positions && node.positions.length > 0" class="positions-container">
      <div class="positions-row">
        <div 
          v-for="pos in node.positions" 
          :key="pos.positionCode"
          class="position-card"
          :class="{ 'open': pos.status === 'OPEN', 'filled': pos.status === 'FILLED' }"
          @click="$emit('position-click', pos)"
          @mouseenter="$emit('position-hover', pos)"
        >
          <div class="pos-header">
            <span class="pos-name">{{ pos.positionName }}</span>
            <span :class="['pos-status', pos.status.toLowerCase()]">{{ formatPosStatus(pos.status) }}</span>
          </div>
          <div class="pos-info">
            <span class="pos-code">{{ pos.positionCode }}</span>
            <span :class="['pos-level', pos.positionLevel.toLowerCase()]">{{ pos.positionLevel }}</span>
          </div>
          <div class="pos-staffing">
            <span>{{ pos.actualCount || 0 }}/{{ pos.staffing || 1 }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 子节点 -->
    <div v-if="node.children && node.children.length > 0" class="children-container">
      <div class="connector-line"></div>
      <div class="children-row">
        <template v-for="child in node.children" :key="child.id">
          <div class="child-wrapper">
            <div class="vertical-connector"></div>
            <PositionOrgSubNode 
              :node="child" 
              :level="child.orgLevel"
              @position-click="$emit('position-click', $event)"
              @position-hover="$emit('position-hover', $event)"
            />
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  node: {
    type: Object,
    required: true
  },
  level: {
    type: Number,
    default: 1
  }
})

defineEmits(['position-click', 'position-hover'])

const getOrgIcon = (level) => {
  const icons = { 1: '🏢', 2: '🏛️', 3: '📦', 4: '📋', 5: '👥' }
  return icons[level] || '📁'
}

// 岗位状态码转中文显示
const formatPosStatus = (status) => {
  const map = { OPEN: '空缺', FILLED: '已配岗' }
  return map[status?.toUpperCase()] || status
}
</script>

<style scoped>
.position-sub-node-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.org-node {
  background: white;
  border-radius: 8px;
  padding: 10px 20px;
  min-width: 160px;
  text-align: center;
  border: 2px solid transparent;
  margin-bottom: 12px;
}

.org-node.level-1 { border-color: #F56C6C; }
.org-node.level-2 { border-color: #E6A23C; }
.org-node.level-3 { border-color: #409EFF; }
.org-node.level-4 { border-color: #67C23A; }
.org-node.level-5 { border-color: #909399; }

.org-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 4px;
}

.org-icon {
  font-size: 16px;
}

.org-name {
  font-weight: bold;
  font-size: 13px;
}

.org-stats {
  font-size: 11px;
  color: #666;
}

.positions-container {
  margin-bottom: 12px;
}

.positions-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.position-card {
  background: white;
  border-radius: 6px;
  padding: 10px;
  min-width: 140px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid #eee;
}

.position-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.08);
}

.position-card.open {
  border-color: #E6A23C;
  background-color: #FFFBE6;
}

.position-card.filled {
  border-color: #67C23A;
}

.pos-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.pos-name {
  font-weight: bold;
  font-size: 12px;
}

.pos-status {
  font-size: 9px;
  padding: 1px 4px;
  border-radius: 3px;
}

.pos-status.open { background: #FFF3CD; color: #856404; }
.pos-status.filled { background: #D4EDDA; color: #155724; }

.pos-info {
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: #666;
  margin-bottom: 4px;
}

.pos-level {
  padding: 1px 3px;
  border-radius: 2px;
  font-size: 9px;
}

.pos-level.p1 { background: #E3F2FD; color: #1976D2; }
.pos-level.p2 { background: #E8F5E9; color: #388E3C; }
.pos-level.p3 { background: #FFF3E0; color: #F57C00; }
.pos-level.p4 { background: #FCE4EC; color: #C2185B; }
.pos-level.p5 { background: #EDE7F6; color: #7B1FA2; }

.pos-staffing {
  font-size: 11px;
  font-weight: bold;
  color: #409EFF;
}

.children-container {
  margin-top: 12px;
}

.connector-line {
  height: 2px;
  background-color: #ddd;
  margin: 0 auto;
}

.children-row {
  display: flex;
  gap: 20px;
}

.child-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.vertical-connector {
  width: 2px;
  height: 12px;
  background-color: #ddd;
}
</style>
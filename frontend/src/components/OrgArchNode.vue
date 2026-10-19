<template>
  <div class="org-arch-node-wrapper">
    <div 
      class="org-arch-node" 
      :class="['level-' + node.orgLevel, { 'highlighted': highlighted, 'disabled': node.orgStatus === 'DISABLED' }]"
      @click="$emit('node-click', node)"
      @mouseenter="$emit('node-hover', node)"
      @mousedown="$emit('node-drag', node, $event)"
    >
      <div class="node-header">
        <span class="node-icon">{{ getLevelIcon(node.orgLevel) }}</span>
        <span class="node-name">{{ node.orgName }}</span>
        <span v-if="node.orgStatus === 'DISABLED'" class="status-badge">已停用</span>
      </div>
      <div class="node-info">
        <span class="org-code">{{ node.orgCode }}</span>
        <span class="manager">{{ node.managerName || '-' }}</span>
      </div>
      <div class="node-stats">
        <span class="stat-item">编制: {{ node.staffing || 0 }}</span>
        <span class="stat-item">在岗: {{ node.actual || 0 }}</span>
      </div>
    </div>
    
    <!-- 子节点 -->
    <div v-if="node.children && node.children.length > 0" class="node-children">
      <div class="connector-line"></div>
      <div class="children-grid">
        <div v-for="child in node.children" :key="child.id" class="child-item">
          <div class="vertical-line"></div>
          <OrgArchNode 
            :node="child" 
            :highlighted="false"
            @node-click="$emit('node-click', $event)"
            @node-hover="$emit('node-hover', $event)"
            @node-drag="$emit('node-drag', $event)"
          />
        </div>
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
  highlighted: {
    type: Boolean,
    default: false
  }
})

defineEmits(['node-click', 'node-hover', 'node-drag'])

const getLevelIcon = (level) => {
  const icons = { 1: '🏢', 2: '🏛️', 3: '📦', 4: '📋', 5: '👥' }
  return icons[level] || '📁'
}
</script>

<style scoped>
.org-arch-node-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.org-arch-node {
  background: white;
  border-radius: 8px;
  padding: 10px 16px;
  min-width: 160px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.org-arch-node.level-1 { border-color: #F56C6C; }
.org-arch-node.level-2 { border-color: #E6A23C; }
.org-arch-node.level-3 { border-color: #409EFF; }
.org-arch-node.level-4 { border-color: #67C23A; }
.org-arch-node.level-5 { border-color: #909399; }

.org-arch-node:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.org-arch-node.highlighted {
  animation: pulse 1s infinite;
}

.org-arch-node.disabled {
  opacity: 0.6;
  background-color: #f5f5f5;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.4); }
  50% { box-shadow: 0 0 0 10px rgba(64, 158, 255, 0); }
}

.node-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.node-icon {
  font-size: 16px;
}

.node-name {
  font-weight: bold;
  font-size: 13px;
  flex: 1;
}

.status-badge {
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 3px;
  background-color: #f5f5f5;
  color: #999;
}

.node-info {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #666;
  margin-bottom: 4px;
}

.node-stats {
  display: flex;
  gap: 10px;
  font-size: 10px;
  color: #999;
}

.stat-item {
  background-color: #f5f7fa;
  padding: 1px 6px;
  border-radius: 3px;
}

.node-children {
  margin-top: 15px;
}

.connector-line {
  height: 2px;
  background-color: #ddd;
  margin: 0 auto;
}

.children-grid {
  display: flex;
  gap: 20px;
  margin-top: 15px;
}

.child-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.vertical-line {
  width: 2px;
  height: 15px;
  background-color: #ddd;
}
</style>
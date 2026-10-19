<template>
  <div class="personnel-org-node-wrapper">
    <!-- 组织节点 -->
    <div class="org-box" :class="['level-' + node.orgLevel]">
      <div class="org-header">
        <span class="org-icon">{{ getOrgIcon(node.orgLevel) }}</span>
        <span class="org-name">{{ node.orgName }}</span>
      </div>
    </div>
    
    <!-- 岗位列表 -->
    <div v-if="node.positions && node.positions.length > 0" class="positions-box">
      <div class="positions-list">
        <div 
          v-for="pos in node.positions" 
          :key="pos.positionCode"
          class="position-item"
        >
          <div class="pos-label">
            <span class="pos-name">{{ pos.positionName }}</span>
            <span :class="['pos-level', pos.positionLevel.toLowerCase()]">{{ pos.positionLevel }}</span>
          </div>
          
          <!-- 员工 -->
          <div class="employees-row">
            <div 
              v-for="(emp, idx) in pos.employees" 
              :key="emp.employeeId"
              class="employee-card"
              :class="['status-' + emp.status.toLowerCase()]"
              @click="$emit('employee-click', emp, pos)"
            >
              <div class="emp-avatar">
                <span>{{ emp.name.charAt(0) }}</span>
              </div>
              <div class="emp-info">
                <span class="emp-name">{{ emp.name }}</span>
                <span class="emp-id">{{ emp.employeeId }}</span>
              </div>
              <span :class="['emp-status', emp.status.toLowerCase()]">{{ formatEmpStatus(emp.status) }}</span>
            </div>
            
            <!-- 空缺岗位 -->
            <div 
              v-if="pos.employees.length === 0 && showEmpty"
              class="empty-position"
              @click="$emit('empty-click', pos)"
            >
              <div class="empty-icon">📝</div>
              <span class="empty-text">空缺</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 子节点 -->
    <div v-if="node.children && node.children.length > 0" class="children-box">
      <div class="connector"></div>
      <div class="children-list">
        <PersonnelOrgNode 
          v-for="child in node.children" 
          :key="child.id"
          :node="child"
          :show-empty="showEmpty"
          @employee-click="$emit('employee-click', $event)"
          @empty-click="$emit('empty-click', $event)"
        />
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
  showEmpty: {
    type: Boolean,
    default: true
  }
})

defineEmits(['employee-click', 'empty-click'])

const getOrgIcon = (level) => {
  const icons = { 1: '🏢', 2: '🏛️', 3: '📦', 4: '📋', 5: '👥' }
  return icons[level] || '📁'
}

// 员工状态码转中文显示
const formatEmpStatus = (status) => {
  const map = { ACTIVE: '在职', PROBATION: '试用中' }
  return map[status?.toUpperCase()] || status
}
</script>

<style scoped>
.personnel-org-node-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.org-box {
  background: white;
  border-radius: 8px;
  padding: 12px 24px;
  min-width: 200px;
  text-align: center;
  border: 2px solid transparent;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.org-box.level-1 { border-color: #F56C6C; }
.org-box.level-2 { border-color: #E6A23C; }
.org-box.level-3 { border-color: #409EFF; }
.org-box.level-4 { border-color: #67C23A; }
.org-box.level-5 { border-color: #909399; }

.org-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.org-icon {
  font-size: 20px;
}

.org-name {
  font-weight: bold;
  font-size: 14px;
}

.positions-box {
  margin-bottom: 15px;
}

.positions-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.position-item {
  background: #fafafa;
  border-radius: 8px;
  padding: 12px;
}

.pos-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.pos-name {
  font-weight: bold;
  font-size: 13px;
}

.pos-level {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
}

.pos-level.p1 { background: #E3F2FD; color: #1976D2; }
.pos-level.p2 { background: #E8F5E9; color: #388E3C; }
.pos-level.p3 { background: #FFF3E0; color: #F57C00; }
.pos-level.p4 { background: #FCE4EC; color: #C2185B; }
.pos-level.p5 { background: #EDE7F6; color: #7B1FA2; }

.employees-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.employee-card {
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.employee-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.employee-card.status-active { border-color: #67C23A; }
.employee-card.status-probation { border-color: #E6A23C; }

.emp-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.emp-info {
  display: flex;
  flex-direction: column;
}

.emp-name {
  font-weight: bold;
  font-size: 13px;
}

.emp-id {
  font-size: 11px;
  color: #666;
}

.emp-status {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
}

.emp-status.active { background: #D4EDDA; color: #155724; }
.emp-status.probation { background: #FFF3CD; color: #856404; }

.empty-position {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #FFF5F5;
  border-radius: 8px;
  padding: 15px 20px;
  border: 2px dashed #F56C6C;
  cursor: pointer;
  transition: all 0.3s ease;
}

.empty-position:hover {
  background: #FFEBEB;
}

.empty-icon {
  font-size: 24px;
  margin-bottom: 5px;
}

.empty-text {
  font-size: 12px;
  color: #F56C6C;
}

.children-box {
  margin-top: 15px;
}

.connector {
  height: 2px;
  background-color: #ddd;
  margin: 0 auto;
  width: 100%;
  max-width: 500px;
}

.children-list {
  display: flex;
  gap: 25px;
  margin-top: 15px;
}
</style>
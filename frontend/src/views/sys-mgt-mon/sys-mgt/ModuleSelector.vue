<template>
  <div class="module-selector">
    <h2>模块选装配置</h2>
    <p class="desc">勾选需要启用的模块，点击"应用配置"后菜单和路由会动态更新</p>
    
    <div class="module-tree">
      <div v-for="parent in parentModules" :key="parent.key" class="parent-group">
        <div class="parent-header">
          <el-checkbox 
            :model-value="isParentAllChecked(parent.key)" 
            :indeterminate="isParentIndeterminate(parent.key)"
            @change="toggleParent(parent.key, $event)"
          >
            <strong>{{ parent.title }}</strong>
          </el-checkbox>
        </div>
        <div class="sub-list">
          <div v-for="sub in getSubModules(parent.key)" :key="sub.subKey" class="sub-item">
            <el-checkbox 
              :model-value="enabledKeys.includes(sub.subKey)" 
              @change="toggleSub(sub.subKey, $event)"
            >
              {{ sub.subTitle }}
            </el-checkbox>
          </div>
        </div>
      </div>
    </div>

    <div class="actions">
      <el-button type="primary" @click="applyConfig">应用配置</el-button>
      <el-button @click="resetDefaults">恢复默认</el-button>
      <el-button @click="selectAll">全选</el-button>
      <el-button @click="clearAll">全不选</el-button>
    </div>

    <div class="stats">
      <el-tag>已启用 {{ enabledKeys.length }} 个子模块</el-tag>
      <el-tag type="success">{{ parentModules.length }} 个一级模块</el-tag>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { moduleMetaMap, defaultEnabledSubModuleKeys } from '../../../config/moduleMeta'
import { getEnabledModuleKeys, refreshModuleCache, toggleModule } from '../../../api/moduleConfig'
import { reloadDynamicRoutes } from '../../../router'

const enabledKeys = ref([])

const parentModules = computed(() => {
  return Object.values(moduleMetaMap)
    .map(p => ({ key: p.parentKey, title: p.parentTitle }))
    .sort((a, b) => {
      const aOrder = moduleMetaMap[a.key]?.displayOrder || 0
      const bOrder = moduleMetaMap[b.key]?.displayOrder || 0
      return aOrder - bOrder
    })
})

function getSubModules(parentKey) {
  const parent = moduleMetaMap[parentKey]
  if (!parent) return []
  return Object.values(parent.subModules).sort((a, b) => {
    return (a.displayOrder || 0) - (b.displayOrder || 0)
  })
}

function isParentAllChecked(parentKey) {
  const subs = getSubModules(parentKey)
  if (subs.length === 0) return false
  return subs.every(s => enabledKeys.value.includes(s.subKey))
}

function isParentIndeterminate(parentKey) {
  const subs = getSubModules(parentKey)
  const checked = subs.filter(s => enabledKeys.value.includes(s.subKey)).length
  return checked > 0 && checked < subs.length
}

function toggleParent(parentKey, checked) {
  const subs = getSubModules(parentKey)
  const subKeys = subs.map(s => s.subKey)
  
  if (checked) {
    subKeys.forEach(k => {
      if (!enabledKeys.value.includes(k)) {
        enabledKeys.value.push(k)
      }
    })
  } else {
    enabledKeys.value = enabledKeys.value.filter(k => !subKeys.includes(k))
  }
}

function toggleSub(subKey, checked) {
  if (checked) {
    enabledKeys.value.push(subKey)
  } else {
    enabledKeys.value = enabledKeys.value.filter(k => k !== subKey)
  }
}

async function applyConfig() {
  try {
    const currentKeys = await getEnabledModuleKeys()
    const toEnable = enabledKeys.value.filter(k => !currentKeys.includes(k))
    const toDisable = currentKeys.filter(k => !enabledKeys.value.includes(k))

    for (const key of [...toEnable, ...toDisable]) {
      await toggleModule(key)
    }

    await refreshModuleCache()
    await reloadDynamicRoutes()
    
    ElMessage.success(`配置已应用，启用 ${enabledKeys.value.length} 个模块，页面将刷新`)
    setTimeout(() => {
      window.location.reload()
    }, 800)
  } catch (e) {
    console.error(e)
    ElMessage.error('应用配置失败: ' + e.message)
  }
}

function resetDefaults() {
  enabledKeys.value = [...defaultEnabledSubModuleKeys]
  ElMessage.info('已恢复默认配置，记得点击"应用配置"')
}

function selectAll() {
  enabledKeys.value = Object.values(moduleMetaMap).flatMap(p => 
    Object.keys(p.subModules)
  )
}

function clearAll() {
  enabledKeys.value = []
}

onMounted(async () => {
  enabledKeys.value = await getEnabledModuleKeys()
})
</script>

<style scoped>
.module-selector {
  padding: 24px;
  max-width: 900px;
  margin: 0 auto;
}

.module-selector h2 {
  margin-top: 0;
  color: #303133;
}

.desc {
  color: #909399;
  margin-bottom: 24px;
}

.module-tree {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.parent-group {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.parent-group:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.parent-header {
  margin-bottom: 12px;
  font-size: 15px;
}

.sub-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px;
  padding-left: 24px;
}

.sub-item {
  font-size: 14px;
  color: #606266;
}

.actions {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.stats {
  display: flex;
  gap: 12px;
}
</style>

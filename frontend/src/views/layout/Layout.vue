<template>
  <el-container class="layout">
    <!-- 顶部一级模块标签 -->
    <div class="top-tabs">
      <div class="logo-small">
        <el-icon><Trophy /></el-icon>
        <span>eOpt企业运营平台</span>
      </div>
      <div class="tabs-container">
        <div
          v-for="group in menuGroups"
          :key="group.key"
          class="top-tab"
          :class="{ active: activeModule === group.key }"
          @click="switchModule(group.key)"
        >
          <el-icon><component :is="group.icon" /></el-icon>
          <span>{{ group.title }}</span>
        </div>
      </div>
      <div class="user-area">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="28" style="background:#1890ff">{{ username?.charAt(0) || 'U' }}</el-avatar>
            <span class="username">{{ username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主体区域 -->
    <div class="main-container">
      <!-- 紧凑左侧子菜单 -->
      <div class="sub-sidebar" :style="{ width: showSubMenu ? '220px' : '0' }">
        <div class="sub-menu-header">
          <span>{{ currentGroup?.title }}</span>
          <button class="collapse-btn" @click="showSubMenu = !showSubMenu">
            <el-icon><ArrowDown :class="{ rotated: !showSubMenu }" /></el-icon>
          </button>
        </div>
        <!-- 支持 subGroups 的模块：嵌套分组菜单 -->
        <el-menu
          v-if="currentGroup?.subGroups && currentGroup.subGroups.length"
          :default-active="$route.path"
          class="sub-menu"
          background-color="#fafafa"
          text-color="#333"
          active-text-color="#1890ff"
          mode="vertical"
          router
          unique-opened
        >
          <template v-for="sg in currentGroup.subGroups" :key="sg.groupKey">
            <!-- 三级嵌套菜单 -->
            <template v-if="sg.subSubGroups && sg.subSubGroups.length">
              <el-sub-menu :index="sg.groupKey">
                <template #title>
                  <el-icon><component :is="sg.subSubGroups?.[0]?.items?.[0]?.icon || 'File'" /></el-icon>
                  <span>{{ sg.groupTitle }}</span>
                </template>
                <el-sub-menu
                  v-for="ss in sg.subSubGroups"
                  :key="ss.subKey"
                  :index="ss.subKey"
                >
                  <template #title>
                    <el-icon><component :is="ss.items?.[0]?.icon || 'File'" /></el-icon>
                    <span>{{ ss.subTitle }}</span>
                  </template>
                  <el-menu-item
                    v-for="item in ss.items"
                    :key="item.path"
                    :index="item.path"
                  >
                    <el-icon><component :is="item.icon || 'File'" /></el-icon>
                    <span>{{ item.title }}</span>
                  </el-menu-item>
                </el-sub-menu>
              </el-sub-menu>
            </template>
            <!-- 二级菜单 -->
            <template v-else>
              <el-sub-menu :key="sg.groupKey" :index="sg.groupKey">
                <template #title>
                  <el-icon><component :is="sg.items?.[0]?.icon || 'File'" /></el-icon>
                  <span>{{ sg.groupTitle }}</span>
                </template>
                <el-menu-item
                  v-for="item in sg.items"
                  :key="item.path"
                  :index="item.path"
                >
                  <el-icon><component :is="item.icon || 'File'" /></el-icon>
                  <span>{{ item.title }}</span>
                </el-menu-item>
              </el-sub-menu>
            </template>
          </template>
        </el-menu>
        <!-- 平级菜单 -->
        <el-menu
          v-else
          :default-active="$route.path"
          class="sub-menu"
          background-color="#fafafa"
          text-color="#333"
          active-text-color="#1890ff"
          mode="vertical"
          router
        >
          <el-menu-item
            v-for="item in currentGroup?.items"
            :key="item.path"
            :index="item.path"
          >
            <el-icon><component :is="item.icon || 'File'" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 主内容区 -->
      <div class="content-area">
        <!-- 层级面包屑 -->
        <div class="breadcrumb-bar">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="(p, idx) in breadcrumb" :key="idx">{{ p.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <router-view v-slot="{ Component, route }">
          <component :is="Component" :key="route.fullPath" />
        </router-view>
      </div>
    </div>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Trophy, ArrowDown } from '@element-plus/icons-vue'
import { getEnabledModuleKeys, buildMenuGroups, refreshModuleCache } from '../../api/moduleConfig'

const router = useRouter()
const route = useRoute()
const activeModule = ref('')
const showSubMenu = ref(true)
const username = ref(localStorage.getItem('username') || '用户')
const menuGroups = ref([])
const menuLoading = ref(false)

async function loadMenus() {
  menuLoading.value = true
  try {
    const enabledKeys = await getEnabledModuleKeys()
    menuGroups.value = buildMenuGroups(enabledKeys)
  } catch (e) {
    console.error('Failed to load menus:', e)
  } finally {
    menuLoading.value = false
  }
}

onMounted(async () => {
  await loadMenus()
  const path = route.path
  if (path !== '/' && path !== '') {
    updateActiveModule(path)
  }
})

function navigateToFirst() {
  if (menuGroups.value.length === 0) return
  const firstGroup = menuGroups.value[0]
  if (firstGroup.subGroups?.length) {
    const firstSub = firstGroup.subGroups[0]
    if (firstSub.subSubGroups?.length) {
      const firstSS = firstSub.subSubGroups[0]
      if (firstSS?.items?.length) router.push(firstSS.items[0].path)
    } else if (firstSub.items?.length) {
      router.push(firstSub.items[0].path)
    }
  } else if (firstGroup.items?.length) {
    router.push(firstGroup.items[0].path)
  }
}

watch(() => route.path, (newPath) => {
  updateActiveModule(newPath)
})

function switchModule(key) {
  activeModule.value = key
  const group = menuGroups.value.find(g => g.key === key)
  if (!group) return
  
  if (group.subGroups?.length) {
    const firstSub = group.subGroups[0]
    if (firstSub) {
      if (firstSub.subSubGroups?.length) {
        const firstSS = firstSub.subSubGroups[0]
        if (firstSS?.items?.length) router.push(firstSS.items[0].path)
      } else if (firstSub.items?.length) {
        router.push(firstSub.items[0].path)
      }
    }
  } else if (group?.items?.length) {
    router.push(group.items[0].path)
  }
}

function handleCommand(cmd) {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
      .then(() => {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('role')
        router.push('/login')
      })
      .catch(() => {})
  } else if (cmd === 'profile') {
    const userPath = '/sys-mgt-mon/sys-mgt/user'
    const hasSysMgt = menuGroups.value.some(g => 
      g.subGroups?.some(sg => sg.items?.some(it => it.path === userPath))
    )
    if (hasSysMgt) {
      router.push(userPath)
    } else {
      ElMessage.warning('系统管理模块未启用')
    }
  }
}

function updateActiveModule(path) {
  const sortedGroups = [...menuGroups.value].sort((a, b) => b.key.length - a.key.length)
  let matched = sortedGroups.find(g => path.startsWith(g.key))
  if (!matched) {
    matched = sortedGroups.find(g => {
      if (g.subGroups) {
        return g.subGroups.some(sg => path.startsWith(sg.groupKey))
      }
      return false
    })
  }
  if (matched) {
    activeModule.value = matched.key
  } else if (menuGroups.value[0]?.subGroups?.[0]?.items?.length) {
    router.push(menuGroups.value[0].subGroups[0].items[0].path)
  }
}

const currentGroup = computed(() => menuGroups.value.find(g => g.key === activeModule.value))

const breadcrumb = computed(() => {
  const parts = []
  const g = currentGroup.value
  if (!g) return []
  parts.push({ title: g.title })
  
  if (g.subGroups && g.subGroups.length) {
    let sg = g.subGroups.find(sg => route.path.startsWith(sg.groupKey))
    if (!sg) {
      sg = g.subGroups.find(sg => {
        if (sg.subSubGroups && sg.subSubGroups.length) {
          return sg.subSubGroups.some(ss => ss.items?.some(it => it.path === route.path))
        }
        return sg.items?.some(it => it.path === route.path)
      })
    }
    if (sg) {
      parts.push({ title: sg.groupTitle })
      if (sg.subSubGroups && sg.subSubGroups.length) {
        let ss = sg.subSubGroups.find(ss => route.path.startsWith(ss.subKey))
        if (!ss) ss = sg.subSubGroups.find(ss => ss.items?.some(it => it.path === route.path))
        if (ss) {
          if (ss.subTitle && ss.subTitle !== sg.groupTitle) parts.push({ title: ss.subTitle })
          const item = ss.items?.find(it => it.path === route.path)
          if (item) parts.push({ title: item.title })
        }
      } else {
        const item = sg.items?.find(it => it.path === route.path)
        if (item) parts.push({ title: item.title })
      }
    }
  } else {
    const item = g.items?.find(it => it.path === route.path)
    if (item) parts.push({ title: item.title })
  }
  return parts
})

</script>

<style lang="scss" scoped>
.layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}

.top-tabs {
  height: 48px;
  background: #001529;
  display: flex;
  align-items: center;
  flex-shrink: 0;
  padding: 0 8px;
  gap: 4px;
}

.logo-small {
  height: 100%;
  display: flex;
  align-items: center;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  gap: 6px;
  padding: 0 12px;
  white-space: nowrap;
  border-right: 1px solid rgba(255,255,255,0.1);
  margin-right: 8px;
}

.tabs-container {
  display: flex;
  align-items: center;
  overflow-x: auto;
  flex: 1;
  gap: 2px;
  scrollbar-width: thin;
  scrollbar-color: rgba(255,255,255,0.3) transparent;

  &::-webkit-scrollbar {
    height: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255,255,255,0.3);
    border-radius: 2px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: rgba(255,255,255,0.5);
  }
}

.top-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 12px;
  height: 48px;
  color: rgba(255,255,255,0.7);
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;

  &:hover {
    color: #fff;
    background: rgba(255,255,255,0.1);
  }

  &.active {
    color: #fff;
    background: #1890ff;
  }

  .el-icon {
    font-size: 14px;
  }
}

.user-area {
  display: flex;
  align-items: center;
  padding: 0 12px;
  border-left: 1px solid rgba(255,255,255,0.1);
  margin-left: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #fff;

  .username {
    font-size: 13px;
  }
}

.main-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.sub-sidebar {
  width: 220px;
  background: #fafafa;
  border-right: 1px solid #e8e8e8;
  overflow-y: auto;
  transition: width 0.3s;
  flex-shrink: 0;
}

.sub-menu-header {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  font-size: 13px;
  font-weight: 600;
  color: #333;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.collapse-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  color: #666;
  transition: all 0.2s;

  &:hover {
    color: #1890ff;
  }
}

.sub-menu {
  border-right: none;

  :deep(.el-menu-item) {
    height: 38px;
    line-height: 38px;
    font-size: 13px;
    padding-left: 12px !important;
    overflow: hidden;
    text-overflow: ellipsis;

    .el-icon {
      font-size: 14px;
      margin-right: 8px;
    }

    &.is-active {
      background: #e6f7ff;
    }
  }

  :deep(.el-sub-menu__title) {
    height: 40px;
    line-height: 40px;
    font-size: 13px;
    font-weight: 600;
    color: #333;
    padding-left: 12px !important;

    .el-icon {
      font-size: 14px;
      margin-right: 8px;
    }
  }

  :deep(.el-menu-item.is-active) {
    background: #e6f7ff;
  }
}

.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.breadcrumb-bar {
  background: #fff;
  padding: 10px 16px;
  margin-bottom: 12px;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  font-size: 14px;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
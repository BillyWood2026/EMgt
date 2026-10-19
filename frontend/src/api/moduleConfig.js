import axios from './axiosConfig'
import { moduleMetaMap, defaultEnabledSubModuleKeys } from '../config/moduleMeta'

const STORAGE_KEY = 'emgt_module_config'
const ENABLED_KEYS_STORAGE = 'emgt_enabled_modules'

function buildDefaultConfigs() {
  const configs = []
  Object.values(moduleMetaMap).forEach(parent => {
    Object.values(parent.subModules).forEach(sub => {
      configs.push({
        parentModuleKey: parent.parentKey,
        parentModuleName: parent.parentTitle,
        subModuleKey: sub.subKey,
        subModuleName: sub.subTitle,
        enabled: defaultEnabledSubModuleKeys.includes(sub.subKey),
        displayOrder: sub.displayOrder || 0,
        icon: parent.parentIcon
      })
    })
  })
  return configs
}

function getLocalConfigs() {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      try {
        return JSON.parse(stored)
      } catch (e) {
        console.warn('Failed to parse local module config:', e)
      }
    }
  } catch (e) {
    console.warn('Failed to parse local module config:', e)
  }
  const defaults = buildDefaultConfigs()
  localStorage.setItem(STORAGE_KEY, JSON.stringify(defaults))
  return defaults
}

function saveLocalConfigs(configs) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(configs))
}

function getLocalEnabledKeys() {
  try {
    const stored = localStorage.getItem(ENABLED_KEYS_STORAGE)
    if (stored) {
      try {
        const keys = JSON.parse(stored)
        if (Array.isArray(keys)) {
          // 过滤已不存在的子模块key（板块重组后旧key失效），并补齐新增模块
          const valid = defaultEnabledSubModuleKeys
          const stale = keys.some(k => !valid.includes(k))
          const missing = valid.some(k => !keys.includes(k))
          if (stale || missing) {
            localStorage.removeItem(ENABLED_KEYS_STORAGE)
            localStorage.removeItem(STORAGE_KEY)
            return [...valid]
          }
          return keys
        }
      } catch (e) {
        console.warn('Failed to parse enabled modules:', e)
      }
    }
  } catch (e) {
    console.warn('Failed to parse enabled modules:', e)
  }
  return [...defaultEnabledSubModuleKeys]
}

function saveLocalEnabledKeys(keys) {
  localStorage.setItem(ENABLED_KEYS_STORAGE, JSON.stringify(keys))
}

let cachedEnabledKeys = null

export async function getEnabledModuleKeys() {
  if (cachedEnabledKeys) {
    return cachedEnabledKeys
  }
  
  const localKeys = getLocalEnabledKeys()
  
  if (localKeys && localKeys.length > 0 && !localStorage.getItem('emgt_dev_mode')) {
    cachedEnabledKeys = localKeys
    return cachedEnabledKeys
  }
  
  try {
    const res = await axios.get('/api/module-config/enabled')
    if (res.data && Array.isArray(res.data)) {
      cachedEnabledKeys = res.data.map(m => m.subModuleKey)
      saveLocalEnabledKeys(cachedEnabledKeys)
      return cachedEnabledKeys
    }
  } catch (e) {
    console.warn('Module config API unavailable, using local settings:', e.message)
  }
  
  cachedEnabledKeys = [...defaultEnabledSubModuleKeys]
  localStorage.setItem('emgt_dev_mode', 'true')
  localStorage.setItem('emgt_enabled_modules', JSON.stringify(cachedEnabledKeys))
  
  return cachedEnabledKeys
}

export function buildMenuGroups(enabledKeys) {
  const menuGroups = []
  const parentMap = {}

  Object.values(moduleMetaMap).forEach(parent => {
    const group = {
      key: parent.parentKey,
      title: parent.parentTitle,
      icon: parent.parentIcon,
      subGroups: []
    }

    Object.values(parent.subModules).forEach(sub => {
      if (!enabledKeys.includes(sub.subKey)) {
        return
      }

      if (sub.items && sub.items.length) {
        group.subGroups.push({
          groupKey: sub.subKey,
          groupTitle: sub.subTitle,
          items: sub.items.map(item => ({
            path: item.path,
            title: item.title,
            icon: item.icon
          }))
        })
      } else if (sub.subSubGroups && sub.subSubGroups.length) {
        group.subGroups.push({
          groupKey: sub.subKey,
          groupTitle: sub.subTitle,
          subSubGroups: sub.subSubGroups.map(ssg => ({
            subKey: ssg.subKey,
            subTitle: ssg.subTitle,
            items: ssg.items.map(item => ({
              path: item.path,
              title: item.title,
              icon: item.icon
            }))
          }))
        })
      }
    })

    group.subGroups.sort((a, b) => {
      const aOrder = moduleMetaMap[parent.parentKey]?.subModules[a.groupKey]?.displayOrder || 0
      const bOrder = moduleMetaMap[parent.parentKey]?.subModules[b.groupKey]?.displayOrder || 0
      return aOrder - bOrder
    })

    if (group.subGroups.length > 0) {
      parentMap[parent.parentKey] = {
        ...group,
        displayOrder: parent.displayOrder || 0
      }
    }
  })

  return Object.values(parentMap).sort((a, b) => a.displayOrder - b.displayOrder)
}

export function getEnabledRoutes(enabledKeys) {
  const routes = []
  Object.values(moduleMetaMap).forEach(parent => {
    Object.values(parent.subModules).forEach(sub => {
      if (!enabledKeys.includes(sub.subKey)) {
        return
      }
      if (sub.items) {
        sub.items.forEach(item => {
          if (item.component) {
            routes.push({
              path: item.path,
              name: item.path.replace(/\//g, '-').substring(1),
              component: item.component,
              meta: { title: item.title }
            })
          }
        })
      }
      if (sub.subSubGroups) {
        sub.subSubGroups.forEach(ssg => {
          ssg.items.forEach(item => {
            if (item.component) {
              routes.push({
                path: item.path,
                name: item.path.replace(/\//g, '-').substring(1),
                component: item.component,
                meta: { title: item.title }
              })
            }
          })
        })
      }
    })
  })
  return routes
}

export async function getAllModuleConfigs() {
  try {
    const res = await axios.get('/api/module-config')
    if (res.data && Array.isArray(res.data)) {
      saveLocalConfigs(res.data)
      return res.data
    }
  } catch (e) {
    console.warn('Module config API unavailable, using local:', e.message)
  }
  return getLocalConfigs()
}

export async function toggleModule(subModuleKey) {
  try {
    const res = await axios.patch(`/api/module-config/sub/${encodeURIComponent(subModuleKey)}/toggle`)
    if (res.data) {
      cachedEnabledKeys = null
      return res.data
    }
  } catch (e) {
    console.warn('Toggle API unavailable, using local:', e.message)
  }
  const enabledKeys = getLocalEnabledKeys()
  const idx = enabledKeys.indexOf(subModuleKey)
  if (idx > -1) {
    enabledKeys.splice(idx, 1)
  } else {
    enabledKeys.push(subModuleKey)
  }
  saveLocalEnabledKeys(enabledKeys)
  cachedEnabledKeys = enabledKeys
  return { subModuleKey, enabled: idx === -1 }
}

export async function refreshModuleCache() {
  cachedEnabledKeys = null
  return getEnabledModuleKeys()
}

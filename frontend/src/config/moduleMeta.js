const configModules = import.meta.glob('../views/*/module.config.json', { eager: true, import: 'default' })

function buildModuleMetaMap() {
  const metaMap = {}
  const subKeys = []

  for (const path in configModules) {
    const config = configModules[path]
    if (!config || !config.key || !config.subModules) continue

    const parentKey = config.key
    const parent = {
      parentKey: parentKey,
      parentTitle: config.title || '',
      parentIcon: config.icon || '',
      displayOrder: config.displayOrder || 999,
      subModules: {}
    }

    for (const sub of config.subModules) {
      if (!sub.key) continue
      parent.subModules[sub.key] = {
        subKey: sub.key,
        subTitle: sub.title || '',
        displayOrder: sub.displayOrder || 999,
        items: sub.items || [],
        subSubGroups: Array.isArray(sub.subSubGroups)
          ? sub.subSubGroups.map(ssg => ({
              subKey: ssg.subKey,
              subTitle: ssg.subTitle || '',
              items: ssg.items || []
            }))
          : null
      }
      subKeys.push(sub.key)
    }

    metaMap[parentKey] = parent
  }

  const sortedMetaMap = {}
  Object.keys(metaMap)
    .sort((a, b) => (metaMap[a].displayOrder || 999) - (metaMap[b].displayOrder || 999))
    .forEach(key => {
      sortedMetaMap[key] = metaMap[key]
    })

  return { metaMap: sortedMetaMap, allSubKeys: subKeys }
}

const { metaMap: moduleMetaMap, allSubKeys: defaultEnabledSubModuleKeys } = buildModuleMetaMap()

function buildMenuGroups(enabledSubModuleKeys) {
  const groups = []

  for (const parentKey in moduleMetaMap) {
    const parent = moduleMetaMap[parentKey]
    const enabledSubs = Object.values(parent.subModules).filter(s =>
      enabledSubModuleKeys.includes(s.subKey)
    )

    if (enabledSubs.length === 0) continue

    const sortedSubs = [...enabledSubs].sort((a, b) =>
      (a.displayOrder || 999) - (b.displayOrder || 999)
    )

    const subGroups = sortedSubs.map(sub => {
      const firstItem = sub.items?.[0]
      return {
        groupKey: sub.subKey,
        groupTitle: sub.subTitle,
        icon: firstItem?.icon || 'Document',
        items: sub.items || []
      }
    })

    const firstSub = subGroups[0]
    groups.push({
      key: parentKey,
      title: parent.parentTitle,
      icon: parent.parentIcon,
      displayOrder: parent.displayOrder,
      firstPath: firstSub?.items?.[0]?.path || '/',
      subGroups
    })
  }

  return groups
}

function getEnabledRoutes(enabledSubModuleKeys) {
  const routes = []

  for (const parentKey in moduleMetaMap) {
    const parent = moduleMetaMap[parentKey]
    for (const subKey in parent.subModules) {
      if (!enabledSubModuleKeys.includes(subKey)) continue
      const sub = parent.subModules[subKey]
      for (const item of sub.items) {
        routes.push({
          path: item.path,
          name: item.path.split('/').filter(Boolean).join('-'),
          component: item.component,
          meta: { title: item.title, icon: item.icon }
        })
      }
    }
  }

  return routes
}

export {
  moduleMetaMap,
  defaultEnabledSubModuleKeys,
  buildMenuGroups,
  getEnabledRoutes
}

export default moduleMetaMap

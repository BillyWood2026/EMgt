import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import Register from '../components/Register.vue'
import Layout from '../views/layout/Layout.vue'
import Home from '../views/Home.vue'
import { getEnabledModuleKeys, getEnabledRoutes } from '../api/moduleConfig'

const modules = import.meta.glob('../views/**/*.vue', { eager: true })

function normalizePath(path) {
  return path.replace(/\\/g, '/')
}

function getComponent(componentPath) {
  const pathWithoutExt = componentPath.replace(/\.vue$/, '')
  
  const possiblePaths = [
    `../views/${componentPath}`,
    `../views/${pathWithoutExt}.vue`,
    componentPath,
    `${pathWithoutExt}.vue`
  ]
  
  const normalizedModuleKeys = {}
  Object.keys(modules).forEach(key => {
    normalizedModuleKeys[normalizePath(key)] = modules[key]
  })
  
  for (const path of possiblePaths) {
    const normalizedPath = normalizePath(path)
    if (normalizedModuleKeys[normalizedPath]) {
      return () => Promise.resolve(normalizedModuleKeys[normalizedPath].default || normalizedModuleKeys[normalizedPath])
    }
  }
  
  const matchedKey = Object.keys(normalizedModuleKeys).find(key => 
    key.endsWith(`/${componentPath}`) || 
    key.endsWith(`/${pathWithoutExt}.vue`)
  )
  
  if (matchedKey) {
    return () => Promise.resolve(normalizedModuleKeys[matchedKey].default || normalizedModuleKeys[matchedKey])
  }
  
  const componentName = componentPath.split('/').pop().replace(/\.vue$/, '')
  const byNameKey = Object.keys(normalizedModuleKeys).find(key => 
    key.includes(componentName) && 
    key.endsWith('.vue')
  )
  
  if (byNameKey) {
    return () => Promise.resolve(normalizedModuleKeys[byNameKey].default || normalizedModuleKeys[byNameKey])
  }
  
  console.warn(`Component not found for path: ${componentPath}`)
  return null
}

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/',
    name: 'Layout',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Home',
        component: Home
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

let dynamicRoutesLoaded = false

async function loadDynamicRoutes() {
  if (dynamicRoutesLoaded) return

  try {
    const enabledKeys = await getEnabledModuleKeys()
    console.log('Enabled module keys:', enabledKeys)
    
    const enabledRoutes = getEnabledRoutes(enabledKeys)
    console.log('Enabled routes count:', enabledRoutes.length)

    enabledRoutes.forEach(routeConfig => {
      const comp = getComponent(routeConfig.component)
      if (comp) {
        router.addRoute('Layout', {
          path: routeConfig.path,
          name: routeConfig.name,
          component: comp,
          meta: routeConfig.meta || {}
        })
        console.log('Added route:', routeConfig.path)
      } else {
        console.warn('Component not found for route:', routeConfig.path, routeConfig.component)
      }
    })

    dynamicRoutesLoaded = true
    console.log('Dynamic routes loaded, total:', router.getRoutes().length)
  } catch (e) {
    console.error('Failed to load dynamic routes:', e)
  }
}

router.beforeEach(async (to, from, next) => {
  let token = localStorage.getItem('token')
  let username = localStorage.getItem('username')

  // 开发模式自动登录（可通过环境变量控制）
  if (import.meta.env.DEV && !token && !username) {
    token = 'dev-token-12345'
    username = 'admin'
    localStorage.setItem('token', token)
    localStorage.setItem('username', username)
    localStorage.setItem('role', 'ADMIN')
  }

  if (to.path === '/login' || to.path === '/register') {
    if (token && username) {
      next('/')
      return
    }
    next()
    return
  }

  if (!token || !username) {
    next('/login')
    return
  }

  if (to.path === '/' || to.path === '') {
    if (!dynamicRoutesLoaded) {
      await loadDynamicRoutes()
    }
    next()
    return
  }

  if (!dynamicRoutesLoaded) {
    await loadDynamicRoutes()
    next({ ...to, replace: true })
    return
  }

  next()
})

export function reloadDynamicRoutes() {
  dynamicRoutesLoaded = false
  const layoutRoute = router.options.routes.find(r => r.name === 'Layout')
  if (layoutRoute) {
    layoutRoute.children = []
  }
  return loadDynamicRoutes()
}

export default router
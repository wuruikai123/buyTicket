import { createRouter as createVueRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/login' },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/home',
    name: 'home',
    meta: { requiresAuth: true },
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/order',
    name: 'order',
    meta: { requiresAuth: true },
    component: () => import('@/views/OrderVerify.vue')
  },
  {
    path: '/scan-result',
    name: 'scanResult',
    meta: { requiresAuth: true },
    component: () => import('@/views/ScanResult.vue')
  },
  {
    path: '/scan-verify',
    name: 'scanVerify',
    meta: { requiresAuth: true },
    component: () => import('@/views/ScanVerify.vue')
  },
  {
    path: '/records',
    name: 'records',
    meta: { requiresAuth: true },
    component: () => import('@/views/Records.vue')
  }
]

const TOKEN_KEY = 'seller_token'

export function createRouter() {
  const router = createVueRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
  })

  router.beforeEach((to, _from, next) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (to.meta.requiresAuth && !token) {
      next({ name: 'login' })
    } else if (to.name === 'login' && token) {
      next({ name: 'home' })
    } else {
      next()
    }
  })

  return router
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}


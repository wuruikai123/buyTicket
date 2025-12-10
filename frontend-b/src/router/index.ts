import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据概览', icon: 'Odometer' }
      },
      {
        path: 'user/list',
        name: 'UserList',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户列表', icon: 'User' }
      },
      {
        path: 'user/statistics',
        name: 'UserStatistics',
        component: () => import('@/views/user/UserStatistics.vue'),
        meta: { title: '用户统计', icon: 'User' }
      },
      {
        path: 'exhibition/list',
        name: 'ExhibitionList',
        component: () => import('@/views/exhibition/ExhibitionList.vue'),
        meta: { title: '展览列表', icon: 'Picture' }
      },
      {
        path: 'exhibition/create',
        name: 'ExhibitionCreate',
        component: () => import('@/views/exhibition/ExhibitionForm.vue'),
        meta: { title: '创建展览', icon: 'Picture' }
      },
      {
        path: 'exhibition/edit/:id',
        name: 'ExhibitionEdit',
        component: () => import('@/views/exhibition/ExhibitionForm.vue'),
        meta: { title: '编辑展览', icon: 'Picture' }
      },
      {
        path: 'exhibition/carousel',
        name: 'CarouselManagement',
        component: () => import('@/views/exhibition/CarouselManagement.vue'),
        meta: { title: '轮播图管理', icon: 'Picture' }
      },
      {
        path: 'ticket/inventory',
        name: 'TicketInventory',
        component: () => import('@/views/ticket/InventoryList.vue'),
        meta: { title: '库存管理', icon: 'Ticket' }
      },
      {
        path: 'ticket/warning',
        name: 'TicketWarning',
        component: () => import('@/views/ticket/WarningList.vue'),
        meta: { title: '库存预警', icon: 'Ticket' }
      },
      {
        path: 'product/list',
        name: 'ProductList',
        component: () => import('@/views/product/ProductList.vue'),
        meta: { title: '商品列表', icon: 'Goods' }
      },
      {
        path: 'product/create',
        name: 'ProductCreate',
        component: () => import('@/views/product/ProductForm.vue'),
        meta: { title: '创建商品', icon: 'Goods' }
      },
      {
        path: 'product/edit/:id',
        name: 'ProductEdit',
        component: () => import('@/views/product/ProductForm.vue'),
        meta: { title: '编辑商品', icon: 'Goods' }
      },
      {
        path: 'statistics/sales',
        name: 'SalesReport',
        component: () => import('@/views/statistics/SalesReport.vue'),
        meta: { title: '销售报表', icon: 'DataAnalysis' }
      },
      {
        path: 'statistics/user-analysis',
        name: 'UserAnalysis',
        component: () => import('@/views/statistics/UserAnalysis.vue'),
        meta: { title: '用户分析', icon: 'DataAnalysis' }
      },
      {
        path: 'system/basic',
        name: 'SystemBasic',
        component: () => import('@/views/system/BasicSettings.vue'),
        meta: { title: '基础设置', icon: 'Setting' }
      },
      {
        path: 'system/content',
        name: 'SystemContent',
        component: () => import('@/views/system/ContentManagement.vue'),
        meta: { title: '内容管理', icon: 'Setting' }
      },
      {
        path: 'system/log',
        name: 'SystemLog',
        component: () => import('@/views/system/OperationLog.vue'),
        meta: { title: '操作日志', icon: 'Setting' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.path === '/login') {
    if (authStore.token) {
      next('/')
    } else {
      next()
    }
  } else {
    if (authStore.token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router


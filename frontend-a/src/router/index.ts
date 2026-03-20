import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/service-terms',
      name: 'ServiceTerms',
      component: () => import('@/views/ServiceTerms.vue')
    },
    {
      path: '/',
      component: BasicLayout,
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('@/views/Home.vue'),
        },
        {
          path: 'exhibitions',
          name: 'Exhibitions',
          component: () => import('@/views/Exhibitions.vue'),
        },
        {
          path: 'mall',
          name: 'Mall',
          component: () => import('@/views/Mall.vue'),
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/Profile.vue'),
        },
        {
          path: 'ticket/:id',
          name: 'TicketPurchase',
          component: () => import('@/views/TicketPurchase.vue'),
        },
        {
          path: 'date-choose/:id',
          name: 'DateChoose',
          component: () => import('@/views/buyTicket/datechoose.vue'),
        },
        {
          path: 'ticket-info/:id',
          name: 'TicketInfo',
          component: () => import('@/views/buyTicket/ticketinfo.vue'),
        },
        {
          path: 'cart',
          name: 'Cart',
          component: () => import('@/views/Cart.vue'),
        },
        {
          path: 'mall-checkout',
          name: 'MallCheckout',
          component: () => import('@/views/MallCheckout.vue'),
        },
        {
          path: 'address-book',
          name: 'AddressBook',
          component: () => import('@/views/AddressBook.vue'),
        },
        {
          path: 'edit-profile',
          name: 'EditProfile',
          component: () => import('@/views/EditProfile.vue'),
        },
        {
          path: 'order/:id',
          name: 'OrderDetail',
          component: () => import('@/views/OrderDetail.vue'),
        },
        {
          path: 'payment/:orderId',
          name: 'Payment',
          component: () => import('@/views/Payment.vue'),
        },
        {
          path: 'huifu-payment/:orderId',
          name: 'HuifuPayment',
          component: () => import('@/views/HuifuPayment.vue'),
          meta: { title: '支付订单' }
        },
        {
          path: 'order-success',
          name: 'OrderSuccess',
          component: () => import('@/views/OrderSuccess.vue'),
        },
        {
          path: 'wechat-pay-callback',
          name: 'WechatPayCallback',
          component: () => import('@/views/WechatPayCallback.vue'),
        },
        {
          path: 'about',
          name: 'About',
          component: () => import('@/views/About.vue'),
        },
      ],
    },
    // 管理后台路由
    {
      path: '/admin',
      component: AdminLayout,
      redirect: '/admin/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
          meta: { title: '数据概览' }
        },
        // 展览管理
        {
          path: 'exhibition',
          name: 'AdminExhibitionList',
          component: () => import('@/views/admin/ExhibitionList.vue'),
          meta: { title: '展览列表' }
        },
        {
          path: 'exhibition/create',
          name: 'AdminExhibitionCreate',
          component: () => import('@/views/admin/ExhibitionForm.vue'),
          meta: { title: '创建展览' }
        },
        {
          path: 'exhibition/edit/:id',
          name: 'AdminExhibitionEdit',
          component: () => import('@/views/admin/ExhibitionForm.vue'),
          meta: { title: '编辑展览' }
        },
        // 库存管理
        {
          path: 'inventory',
          name: 'AdminInventoryList',
          component: () => import('@/views/admin/InventoryList.vue'),
          meta: { title: '库存列表' }
        },
        {
          path: 'inventory/batch',
          name: 'AdminInventoryBatch',
          component: () => import('@/views/admin/InventoryBatch.vue'),
          meta: { title: '批量创建库存' }
        },
        // 商品管理
        {
          path: 'product',
          name: 'AdminProductList',
          component: () => import('@/views/admin/ProductList.vue'),
          meta: { title: '商品列表' }
        },
        {
          path: 'product/create',
          name: 'AdminProductCreate',
          component: () => import('@/views/admin/ProductForm.vue'),
          meta: { title: '创建商品' }
        },
        {
          path: 'product/edit/:id',
          name: 'AdminProductEdit',
          component: () => import('@/views/admin/ProductForm.vue'),
          meta: { title: '编辑商品' }
        },
      ]
    },
],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  // 不需要登录就能访问的页面（公开页面）
  const publicPages = [
    'Login',
    'ServiceTerms',
    'Home',
    'Exhibitions',
    'Mall',
    'About'
  ]

  // 需要登录才能访问的页面
  const requiresAuth = !publicPages.includes(to.name as string)

  if (requiresAuth && !token) {
    // 需要登录但未登录，跳转到登录页
    next({ name: 'Login' })
  } else {
    next()
  }
})

export default router

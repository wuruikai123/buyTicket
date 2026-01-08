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
          path: 'order-success',
          name: 'OrderSuccess',
          component: () => import('@/views/OrderSuccess.vue'),
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
  console.log('Router Guard Debug:')
  console.log('  To:', to.name, to.path)
  console.log('  From:', from.name, from.path)
  console.log('  Token:', token)

  if (to.name !== 'Login' && !token) {
    console.log('  Action: Redirect to Login')
    next({ name: 'Login' })
  } else {
    console.log('  Action: Allow')
    next()
  }
})

export default router

import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'

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
  ],
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

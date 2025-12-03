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

export default router

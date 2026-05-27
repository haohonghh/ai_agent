import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/agent'
    },
    {
      path: '/web',
      component: () => import('../views/web/WebChatWidget.vue')
    },
    {
      path: '/agent',
      component: () => import('../views/agent/Workbench.vue')
    },
    {
      path: '/admin',
      component: () => import('../views/admin/AdminDashboard.vue')
    }
  ]
})

export default router
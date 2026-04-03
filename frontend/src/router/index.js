import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn } from '../session/storage'
import LoginView from '../views/LoginView.vue'
import AppShellView from '../views/AppShellView.vue'
import OrdersListView from "../views/OrdersListView.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { guestOnly: true },
    },
    {
      path: '/app',
      name: 'app',
      component: AppShellView,
      meta: { requiresAuth: true },
    },
    {
      path: '/orders',
      name: 'ordersList',
      component: OrdersListView,
      meta: { requiresAuth: true },
    },
    { path: '/', redirect: '/app' },
    { path: '/:pathMatch(.*)*', redirect: '/app' },
  ],
})

router.beforeEach((to) => {
  const loggedIn = isLoggedIn()
  if (to.meta.requiresAuth && !loggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && loggedIn) {
    return { name: 'app' }
  }
  return true
})

export default router

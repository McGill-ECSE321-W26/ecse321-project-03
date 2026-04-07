import { createRouter, createWebHashHistory } from 'vue-router'
import { isLoggedIn } from '../session/storage'
import LoginView from '../views/LoginView.vue'
import AppShellView from '../views/AppShellView.vue'
import SpecificItemView from '../views/SpecificItemView.vue'
import OrdersListView from "../views/OrdersListView.vue";

const router = createRouter({
  history: createWebHashHistory(),
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
    { path: '/app/account', redirect: '/app' },
    { path: '/', redirect: '/app' },
    { path: '/:pathMatch(.*)*', redirect: '/app' },
    { path: '/product/:id', name: 'product', component: SpecificItemView },
  ],
})

/**
 * redirect to login page if user is not logged in and the route requires authentication
 * redirect to app shell if user is logged in and the route is guest only
 * redirect to the target route after login (if redirect query parameter is queried)
 */
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

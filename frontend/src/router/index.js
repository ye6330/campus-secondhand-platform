import { createRouter, createWebHistory } from 'vue-router'

// 路由配置
const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    // requiresAuth: true 表示需要登录才能访问
    path: '/home',
    name: 'Home',
    meta: { requiresAuth: true },
    component: () => import('../views/Home.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：每次跳转页面时检查登录状态
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  // 如果目标页面需要登录，但没有 token，跳回登录页
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  // 如果已经登录了，再访问登录页或注册页，自动跳到首页
  if ((to.path === '/login' || to.path === '/register') && token) {
    next('/home')
    return
  }

  next()
})

export default router

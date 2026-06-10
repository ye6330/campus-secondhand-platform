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
  },
  {
    path: '/products',
    name: 'ProductList',
    meta: { requiresAuth: true },
    component: () => import('../views/ProductList.vue')
  },
  {
    path: '/products/publish',
    name: 'ProductPublish',
    meta: { requiresAuth: true },
    component: () => import('../views/ProductPublish.vue')
  },
  {
    path: '/products/:id',
    name: 'ProductDetail',
    meta: { requiresAuth: true },
    component: () => import('../views/ProductDetail.vue')
  },
  {
    path: '/products/:id/edit',
    name: 'ProductEdit',
    meta: { requiresAuth: true },
    component: () => import('../views/ProductEdit.vue')
  },
  {
    path: '/my/products',
    name: 'MyProducts',
    meta: { requiresAuth: true },
    component: () => import('../views/MyProducts.vue')
  },
  {
    path: '/my/favorites',
    name: 'MyFavorites',
    meta: { requiresAuth: true },
    component: () => import('../views/MyFavorites.vue')
  },
  {
    path: '/my/notifications',
    name: 'MyNotifications',
    meta: { requiresAuth: true },
    component: () => import('../views/MyNotifications.vue')
  },
  {
    path: '/profile',
    name: 'Profile',
    meta: { requiresAuth: true },
    component: () => import('../views/Profile.vue')
  },
  {
    path: '/admin/home',
    name: 'AdminHome',
    meta: { requiresAuth: true },
    component: () => import('../views/AdminHome.vue')
  },
  {
    path: '/admin/review',
    name: 'AdminReview',
    meta: { requiresAuth: true },
    component: () => import('../views/AdminReview.vue')
  },
  {
    path: '/admin/reports',
    name: 'AdminReports',
    meta: { requiresAuth: true },
    component: () => import('../views/AdminReports.vue')
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

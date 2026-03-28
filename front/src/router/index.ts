import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/user',
    name: 'User',
    component: () => import('@/views/User.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/user/:id',
    name: 'UserInfo',
    component: () => import('@/views/UserInfo.vue'),
    meta: { title: '用户信息' }
  },
  {
    path: '/shop',
    name: 'Shop',
    component: () => import('@/views/Shop.vue'),
    meta: { title: '商铺列表' }
  },
  {
    path: '/shop/:id',
    name: 'ShopDetail',
    component: () => import('@/views/ShopDetail.vue'),
    meta: { title: '商铺详情' }
  },
  {
    path: '/blog',
    name: 'Blog',
    component: () => import('@/views/Blog.vue'),
    meta: { title: '探店笔记' }
  },
  {
    path: '/blog/:id',
    name: 'BlogDetail',
    component: () => import('@/views/BlogDetail.vue'),
    meta: { title: '笔记详情' }
  },
  {
    path: '/blog/add',
    name: 'BlogAdd',
    component: () => import('@/views/BlogAdd.vue'),
    meta: { title: '发布笔记', requiresAuth: true }
  },
  {
    path: '/voucher',
    name: 'Voucher',
    component: () => import('@/views/Voucher.vue'),
    meta: { title: '优惠券' }
  },
  {
    path: '/seckill',
    name: 'Seckill',
    component: () => import('@/views/Seckill.vue'),
    meta: { title: '秒杀' }
  },
  {
    path: '/follow',
    name: 'Follow',
    component: () => import('@/views/Follow.vue'),
    meta: { title: '关注', requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string) || '凌水市集'
  
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      next('/login')
      return
    }
  }
  
  next()
})

export default router

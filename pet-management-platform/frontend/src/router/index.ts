import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '工作台', icon: 'HomeFilled' }
      },
      {
        path: 'pets',
        name: 'Pets',
        component: () => import('@/views/pets/PetList.vue'),
        meta: { 
          title: '宠物管理', 
          icon: 'PieChart',
          roles: ['ROLE_USER', 'ROLE_ADMIN']  // 只有用户和管理员
        }
      },
      {
        path: 'health-records',
        name: 'HealthRecords',
        component: () => import('@/views/health/HealthRecordList.vue'),
        meta: { 
          title: '健康记录', 
          icon: 'DocumentChecked',
          roles: ['ROLE_USER', 'ROLE_ADMIN']  // 只有用户和管理员
        }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/orders/OrderList.vue'),
        meta: { title: '我的订单', icon: 'Document' }
      },
      // ✅ 新增：客户管理（商家专用）
      {
        path: 'customers',
        name: 'Customers',
        component: () => import('@/views/customers/CustomerList.vue'),
        meta: { 
          title: '客户管理', 
          icon: 'User',
          roles: ['ROLE_MERCHANT_HOSPITAL', 'ROLE_MERCHANT_HOUSE', 'ROLE_MERCHANT_GOODS', 'ROLE_ADMIN']
        }
      },
      // ✅ 新增：客户详情（不在菜单显示）
      {
        path: 'customers/:id',
        name: 'CustomerDetail',
        component: () => import('@/views/customers/CustomerDetail.vue'),
        meta: { 
          title: '客户详情',
          hidden: true,  // 不在菜单显示
          roles: ['ROLE_MERCHANT_HOSPITAL', 'ROLE_MERCHANT_HOUSE', 'ROLE_MERCHANT_GOODS', 'ROLE_ADMIN']
        }
      },
      // 商家端菜单
      {
        path: 'services',
        name: 'Services',
        component: () => import('@/views/services/ServiceList.vue'),
        meta: { title: '服务管理', icon: 'ShoppingCart', roles: ['ROLE_MERCHANT_HOSPITAL', 'ROLE_MERCHANT_HOUSE', 'ROLE_MERCHANT_GOODS'] }
      },
      // 平台管理端菜单
      {
        path: 'nfc-tags',
        name: 'NfcTags',
        component: () => import('@/views/nfc/NfcTagList.vue'),
        meta: { title: 'NFC吊牌管理', icon: 'Postcard', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'base-data',
        name: 'BaseData',
        component: () => import('@/views/base-data/BaseDataList.vue'),
        meta: { title: '基础数据管理', icon: 'Setting', roles: ['ROLE_ADMIN', 'ROLE_DATA_MANAGER'] }
      },
      {
        path: 'organization',
        name: 'Organization',
        component: () => import('@/views/organization/Organization.vue'),
        meta: { title: '组织管理', icon: 'Setting', roles: ['ROLE_ADMIN'] },
        children: [
          {
            path: 'roles',
            name: 'Roles',
            component: () => import('@/views/organization/RolePermission.vue'),
            meta: { title: '角色权限', icon: 'UserFilled', roles: ['ROLE_ADMIN'] }
          },
          {
            path: 'users',
            name: 'UserAccounts',
            component: () => import('@/views/organization/EmployeeAccount.vue'),
            meta: { title: '用户账号', icon: 'Users', roles: ['ROLE_ADMIN'] }
          }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  console.log('🛡️ 路由守卫 | 目标:', to.path, '| 认证:', authStore.isAuthenticated)
  
  // 1. 检查是否需要认证
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    console.log('⛔ 未登录，跳转登录页')
    next('/login')
    return
  }
  
  // 2. 已登录用户访问登录页，跳转到首页
  if (to.path === '/login' && authStore.isAuthenticated) {
    console.log('✅ 已登录，跳转首页')
    next('/')
    return
  }
  
  // 3. 检查角色权限
  if (to.meta.roles && Array.isArray(to.meta.roles)) {
    if (!authStore.user || !authStore.user.roles) {
      console.warn('⚠️ 用户信息异常，跳转Dashboard')
      if (to.path !== '/dashboard') {
        next('/dashboard')
        return
      }
    } else {
      const userRoles = authStore.user.roles
      const requiredRoles = to.meta.roles as string[]
      const hasPermission = requiredRoles.some(role => userRoles.includes(role))
      
      console.log('🔑 权限检查 | 需要:', requiredRoles, '| 拥有:', userRoles, '| 通过:', hasPermission)
      
      if (!hasPermission) {
        console.warn('⛔ 无权限，跳转Dashboard')
        if (to.path !== '/dashboard') {
          next('/dashboard')
          return
        }
      }
    }
  }
  
  console.log('✅ 路由放行')
  next()
})

export default router
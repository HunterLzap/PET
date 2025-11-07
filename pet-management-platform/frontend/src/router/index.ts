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
        meta: { title: '宠物管理', icon: 'PieChart' }
      },
      {
        path: 'health-records',
        name: 'HealthRecords',
        component: () => import('@/views/health/HealthRecordList.vue'),
        meta: { title: '健康记录', icon: 'DocumentChecked' }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/orders/OrderList.vue'),
        meta: { title: '我的订单', icon: 'Document' }
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

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.path === '/login' && authStore.isAuthenticated) {
    next('/')
  } else {
    if (to.meta.roles && Array.isArray(to.meta.roles)) {
      const userRoles = authStore.user?.roles || []
      const hasPermission = to.meta.roles.some(role => userRoles.includes(role as string))
      if (!hasPermission) {
        next('/dashboard')
        return
      }
    }
    next()
  }
})

export default router
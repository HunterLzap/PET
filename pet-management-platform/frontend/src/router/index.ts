import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'), // 修正：移除service层级
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
        component: () => import('@/views/Dashboard.vue'), // 修正：移除service层级
        meta: { title: '工作台', icon: 'HomeFilled' }
      },
      {
        path: 'pets',
        name: 'Pets',
        component: () => import('@/views/pets/PetList.vue'), // 路径正确
        meta: { title: '宠物管理', icon: 'PieChart' }
      },
      {
        path: 'nfc-tags',
        name: 'NfcTags',
        component: () => import('@/views/nfc/NfcTagList.vue'), // 路径正确
        meta: { title: 'NFC吊牌管理', icon: 'Postcard', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'services',
        name: 'Services',
        component: () => import('@/views/services/ServiceList.vue'), // 路径正确
        meta: { title: '服务管理', icon: 'ShoppingCart', roles: ['ROLE_BUSINESS', 'ROLE_ADMIN'] }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/orders/OrderList.vue'), // 修正：移除service层级
        meta: { title: '订单管理', icon: 'Document' }
      },
      {
        path: 'base-data',
        name: 'BaseData',
        component: () => import('@/views/base-data/BaseDataList.vue'), // 修正：移除service层级
        meta: { title: '基础数据管理', icon: 'Setting', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'health-records',
        name: 'HealthRecords',
        component: () => import('@/views/health/HealthRecordList.vue'), // 路径正确
        meta: { title: '健康记录管理', icon: 'DocumentChecked' }
      },
      {
        path: 'organization',
        name: 'Organization',
        component: () => import('@/views/organization/Organization.vue'), // 路径正确
        meta: { title: '组织管理', icon: 'Setting', roles: ['ROLE_ADMIN'] },
        children: [
          {
            path: 'roles',
            name: 'Roles',
            component: () => import('@/views/organization/RolePermission.vue'), // 路径正确
            meta: { title: '角色权限', icon: 'UserFilled', roles: ['ROLE_ADMIN'] }
          },
          {
            path: 'users',
            name: 'UserAccounts',
            component: () => import('@/views/organization/EmployeeAccount.vue'), // 路径正确
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

// 路由守卫逻辑保持不变
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
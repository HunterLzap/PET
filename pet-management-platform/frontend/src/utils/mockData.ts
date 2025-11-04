import type { Role, User } from '@/types'

// 模拟角色数据
export const mockRoles: Role[] = [
  {
    id: 1,
    name: 'ROLE_ADMIN',
    description: '系统管理员，拥有所有权限',
    permissions: {
      dashboard: {
        canView: true,
        canCreate: true,
        canUpdate: true,
        canDelete: true
      },
      pets: {
        canView: true,
        canCreate: true,
        canUpdate: true,
        canDelete: true
      },
      'nfc-tags': {
        canView: true,
        canCreate: true,
        canUpdate: true,
        canDelete: true
      },
      services: {
        canView: true,
        canCreate: true,
        canUpdate: true,
        canDelete: true
      },
      orders: {
        canView: true,
        canCreate: true,
        canUpdate: true,
        canDelete: true
      },
      'base-data': {
        canView: true,
        canCreate: true,
        canUpdate: true,
        canDelete: true
      },
      'health-records': {
        canView: true,
        canCreate: true,
        canUpdate: true,
        canDelete: true
      },
      organization: {
        canView: true,
        canCreate: true,
        canUpdate: true,
        canDelete: true
      },
      roles: {
        canView: true,
        canCreate: true,
        canUpdate: true,
        canDelete: true
      },
      users: {
        canView: true,
        canCreate: true,
        canUpdate: true,
        canDelete: true
      }
    }
  },
  {
    id: 2,
    name: 'ROLE_BUSINESS',
    description: '商家角色，管理服务和订单',
    permissions: {
      dashboard: {
        canView: true,
        canCreate: false,
        canUpdate: false,
        canDelete: false
      },
      services: {
        canView: true,
        canCreate: true,
        canUpdate: true,
        canDelete: true
      },
      orders: {
        canView: true,
        canCreate: false,
        canUpdate: true,
        canDelete: false
      },
      'health-records': {
        canView: true,
        canCreate: true,
        canUpdate: true,
        canDelete: true
      }
    }
  },
  {
    id: 3,
    name: 'ROLE_PET_OWNER',
    description: '宠物主人角色，查看自己的宠物信息',
    permissions: {
      dashboard: {
        canView: true,
        canCreate: false,
        canUpdate: false,
        canDelete: false
      },
      pets: {
        canView: true,
        canCreate: false,
        canUpdate: false,
        canDelete: false
      },
      orders: {
        canView: true,
        canCreate: false,
        canUpdate: false,
        canDelete: false
      },
      'health-records': {
        canView: true,
        canCreate: false,
        canUpdate: false,
        canDelete: false
      }
    }
  }
]

// 模拟用户数据
export const mockUsers: User[] = [
  {
    id: 1,
    username: 'admin',
    email: 'admin@example.com',
    roles: ['ROLE_ADMIN']
  },
  {
    id: 2,
    username: 'business',
    email: 'business@example.com',
    roles: ['ROLE_BUSINESS']
  },
  {
    id: 3,
    username: 'user',
    email: 'user@example.com',
    roles: ['ROLE_PET_OWNER']
  },
  {
    id: 4,
    username: 'zhang_san',
    email: 'zhang_san@example.com',
    roles: ['ROLE_PET_OWNER']
  },
  {
    id: 5,
    username: 'li_si',
    email: 'li_si@example.com',
    roles: ['ROLE_BUSINESS']
  },
  {
    id: 6,
    username: 'wang_wu',
    email: 'wang_wu@example.com',
    roles: ['ROLE_PET_OWNER']
  }
]
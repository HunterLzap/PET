import request from '@/utils/request'

// ==================== 角色相关 ====================

export interface Role {
  id?: number
  name: string
  description?: string
}

// 获取所有角色
export function getRoles() {
  return request({
    url: '/api/admin/roles',
    method: 'get'
  }) as Promise<Role[]>
}

// 获取单个角色
export function getRoleById(id: number) {
  return request({
    url: `/api/admin/roles/${id}`,
    method: 'get'
  }) as Promise<Role>
}

// 创建角色
export function createRole(data: Role) {
  return request({
    url: '/api/admin/roles',
    method: 'post',
    data
  }) as Promise<Role>
}

// 更新角色
export function updateRole(id: number, data: Role) {
  return request({
    url: `/api/admin/roles/${id}`,
    method: 'put',
    data
  }) as Promise<Role>
}

// 删除角色
export function deleteRole(id: number) {
  return request({
    url: `/api/admin/roles/${id}`,
    method: 'delete'
  })
}

// ==================== 用户相关 ====================

export interface User {
  id?: number
  username: string
  email: string
  password?: string
  userType?: string
  status?: number
  roles?: string[]
  phone?: string
  realName?: string
}

// 获取所有用户
export function getUsers() {
  return request({
    url: '/api/admin/users',
    method: 'get'
  }) as Promise<User[]>
}

// 获取单个用户
export function getUserById(id: number) {
  return request({
    url: `/api/admin/users/${id}`,
    method: 'get'
  }) as Promise<User>
}

// 创建用户
export function createUser(data: User) {
  return request({
    url: '/api/admin/users',
    method: 'post',
    data
  }) as Promise<User>
}

// 更新用户
export function updateUser(id: number, data: User) {
  return request({
    url: `/api/admin/users/${id}`,
    method: 'put',
    data
  }) as Promise<User>
}

// 删除用户
export function deleteUser(id: number) {
  return request({
    url: `/api/admin/users/${id}`,
    method: 'delete'
  })
}

// 分配角色给用户
export function assignRoles(userId: number, roleIds: number[]) {
  return request({
    url: `/api/admin/users/${userId}/roles`,
    method: 'post',
    data: { roleIds }
  })
}

// 启用/禁用用户
export function toggleUserStatus(userId: number, status: number) {
  return request({
    url: `/api/admin/users/${userId}/status`,
    method: 'put',
    data: { status }
  })
}
import request from '@/utils/request'
import type { Role, User } from '@/types'

// 角色相关API
export function getRoles() {
  return request({
    url: '/roles',
    method: 'get'
  }) as Promise<Role[]>
}

export function createRole(data: Omit<Role, 'id'>) {
  return request({
    url: '/roles',
    method: 'post',
    data
  }) as Promise<Role>
}

export function updateRole(id: number, data: Omit<Role, 'id'>) {
  return request({
    url: `/roles/${id}`,
    method: 'put',
    data
  }) as Promise<Role>
}

export function deleteRole(id: number) {
  return request({
    url: `/roles/${id}`,
    method: 'delete'
  })
}

// 员工账号相关API
export function getUsers() {
  return request({
    url: '/users',
    method: 'get'
  }) as Promise<User[]>
}

export function createUser(data: Omit<User, 'id'> & { password: string }) {
  return request({
    url: '/users',
    method: 'post',
    data
  }) as Promise<User>
}

export function updateUser(id: number, data: Partial<User>) {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  }) as Promise<User>
}

export function deleteUser(id: number) {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  })
}

export function updateUserPassword(id: number, data: { password: string }) {
  return request({
    url: `/users/${id}/password`,
    method: 'put',
    data
  })
}

// 搜索用户
export function searchUsers(keyword: string) {
  return request({
    url: '/users/search',
    method: 'get',
    params: { keyword }
  }) as Promise<User[]>
}
import request from '@/utils/request'
import type { Role, User } from '@/types'

// Role APIs
export function getRoles(): Promise<Role[]> {
  return request({ url: '/api/admin/roles', method: 'get' })
}

// User APIs
export function getUsers(): Promise<User[]> {
  return request({ url: '/api/admin/users', method: 'get' })
}
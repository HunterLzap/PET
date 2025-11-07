import request from '@/utils/sort'

export function getAllUsers() {
  return request({
    url: '/api/admin/users',
    method: 'get'
  })
}
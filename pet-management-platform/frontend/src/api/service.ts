import request from '@/utils/request'
import type { Service } from '@/types'

export function getServices() {
  return request({
    url: '/api/services',  // ✅ 加上 /api
    method: 'get'
  }) as Promise<Service[]>
}

export function getServiceById(id: number) {
  return request({
    url: `/api/services/${id}`,  // ✅ 加上 /api
    method: 'get'
  }) as Promise<Service>
}

export function createService(data: Service) {
  return request({
    url: '/api/services',  // ✅ 加上 /api
    method: 'post',
    data
  }) as Promise<Service>
}

export function updateService(id: number, data: Service) {
  return request({
    url: `/api/services/${id}`,  // ✅ 加上 /api
    method: 'put',
    data
  }) as Promise<Service>
}

export function deleteService(id: number) {
  return request({
    url: `/api/services/${id}`,  // ✅ 加上 /api
    method: 'delete'
  })
}
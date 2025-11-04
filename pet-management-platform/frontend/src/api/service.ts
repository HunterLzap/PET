import request from '@/utils/request'
import type { Service } from '@/types'

export function getServices() {
  return request({
    url: '/services',
    method: 'get'
  }) as Promise<Service[]>
}

export function getServiceById(id: number) {
  return request({
    url: `/services/${id}`,
    method: 'get'
  }) as Promise<Service>
}

export function createService(data: Service) {
  return request({
    url: '/services',
    method: 'post',
    data
  }) as Promise<Service>
}

export function updateService(id: number, data: Service) {
  return request({
    url: `/services/${id}`,
    method: 'put',
    data
  }) as Promise<Service>
}

export function deleteService(id: number) {
  return request({
    url: `/services/${id}`,
    method: 'delete'
  })
}


import request from '@/utils/request'
import type { BaseData } from '@/types'

export function getAllBaseData() {
  return request({
    url: '/base-data',
    method: 'get'
  }) as Promise<BaseData[]>
}

export function getBaseDataByType(type: string) {
  return request({
    url: `/base-data/type/${type}`,
    method: 'get'
  }) as Promise<BaseData[]>
}

export function getBaseDataById(id: number) {
  return request({
    url: `/base-data/${id}`,
    method: 'get'
  }) as Promise<BaseData>
}

export function createBaseData(data: BaseData) {
  return request({
    url: '/base-data',
    method: 'post',
    data
  }) as Promise<BaseData>
}

export function updateBaseData(id: number, data: BaseData) {
  return request({
    url: `/base-data/${id}`,
    method: 'put',
    data
  }) as Promise<BaseData>
}

export function deleteBaseData(id: number) {
  return request({
    url: `/base-data/${id}`,
    method: 'delete'
  })
}


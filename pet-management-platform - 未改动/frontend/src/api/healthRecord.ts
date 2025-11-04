import request from '@/utils/request'
import type { HealthRecord } from '@/types'

export function getHealthRecordsByPetId(petId: number) {
  return request({
    url: `/health-records/pet/${petId}`,
    method: 'get'
  }) as Promise<HealthRecord[]>
}

export function getHealthRecordById(id: number) {
  return request({
    url: `/health-records/${id}`,
    method: 'get'
  }) as Promise<HealthRecord>
}

export function createHealthRecord(data: HealthRecord) {
  return request({
    url: '/health-records',
    method: 'post',
    data
  }) as Promise<HealthRecord>
}

export function updateHealthRecord(id: number, data: HealthRecord) {
  return request({
    url: `/health-records/${id}`,
    method: 'put',
    data
  }) as Promise<HealthRecord>
}

export function deleteHealthRecord(id: number) {
  return request({
    url: `/health-records/${id}`,
    method: 'delete'
  })
}


import request from '@/utils/request'
import type { Pet } from '@/types'

export function getPets() {
  return request({
    url: '/pets',
    method: 'get'
  }) as Promise<Pet[]>
}

export function getPetById(id: number) {
  return request({
    url: `/pets/${id}`,
    method: 'get'
  }) as Promise<Pet>
}

export function createPet(data: Pet) {
  return request({
    url: '/pets',
    method: 'post',
    data
  }) as Promise<Pet>
}

export function updatePet(id: number, data: Pet) {
  return request({
    url: `/pets/${id}`,
    method: 'put',
    data
  }) as Promise<Pet>
}

export function deletePet(id: number) {
  return request({
    url: `/pets/${id}`,
    method: 'delete'
  })
}


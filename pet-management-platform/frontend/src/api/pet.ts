import request from '@/utils/request'
import type { Pet, AddPetRequest } from '@/types'

// 获取我的宠物列表
export function getMyPets(): Promise<Pet[]> {
  return request({
    url: '/api/user/pets',
    method: 'get'
  })
}

// 获取宠物详情
export function getPetDetail(id: number): Promise<Pet> {
  return request({
    url: `/api/user/pets/${id}`,
    method: 'get'
  })
}

// 添加宠物
export function addPet(data: AddPetRequest): Promise<Pet> {
  return request({
    url: '/api/user/pets',
    method: 'post',
    data
  })
}

// 更新宠物
export function updatePet(id: number, data: Partial<AddPetRequest>): Promise<Pet> {
  return request({
    url: `/api/user/pets/${id}`,
    method: 'put',
    data
  })
}

// 删除宠物
export function deletePet(id: number): Promise<void> {
  return request({
    url: `/api/user/pets/${id}`,
    method: 'delete'
  })
}

// 兼容旧接口名（避免其他地方报错）
export const getPets = getMyPets
export const createPet = addPet
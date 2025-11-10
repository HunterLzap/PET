import request from '@/utils/request'

// 字典值类型
export interface DictValue {
  id: number
  dictCode: string
  valueCode: string
  valueName: string
  valueOrder: number
  extraData?: string
  colorTag?: string
  icon?: string
  status: number
}

// 获取字典类型列表
export function getDictTypes() {
  return request({
    url: '/api/base-dict/types',
    method: 'get'
  })
}

// 获取字典值列表（根据字典类型代码）
export function getDictValuesByType(dictCode: string) {
  return request({
    url: `/api/base-dict/values/${dictCode}`,
    method: 'get'
  }) as Promise<DictValue[]>
}

// ✅ 根据种类获取品种列表（用于宠物品种联动）
export function getDictValues(dictCode: string, speciesCode: string) {
  return request({
    url: `/api/base-dict/values/${dictCode}/by-species/${speciesCode}`,
    method: 'get'
  }) as Promise<DictValue[]>
}

// 创建字典值
export function createDictValue(data: any) {
  return request({
    url: '/api/base-dict/values',
    method: 'post',
    data
  })
}

// 更新字典值
export function updateDictValue(id: number, data: any) {
  return request({
    url: `/api/base-dict/values/${id}`,
    method: 'put',
    data
  })
}

// 删除字典值
export function deleteDictValue(id: number) {
  return request({
    url: `/api/base-dict/values/${id}`,
    method: 'delete'
  })
}
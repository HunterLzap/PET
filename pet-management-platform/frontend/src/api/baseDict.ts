import request from '@/utils/request'

// 字典值接口
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

// 获取字典值列表
export function getDictValues(dictCode: string) {
  return request({
    url: `/api/base-dict/values/${dictCode}`,
    method: 'get'
  })
}

// 根据父级查询子级（级联）
export function getDictValuesBySpecies(dictCode: string, speciesCode: string) {
  return request({
    url: `/api/base-dict/values/${dictCode}/by-species/${speciesCode}`,
    method: 'get'
  })
}

// 获取所有常用基础数据
export function getAllCommonDict() {
  return request({
    url: '/api/base-dict/all-common',
    method: 'get'
  })
}
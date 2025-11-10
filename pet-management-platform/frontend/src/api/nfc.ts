import request from '@/utils/request'

// NFC标签接口类型定义
export interface NfcTag {
  id?: number
  tagUid: string
  petId?: number
  protocolType: string
  status: string
  activatedAt?: string
  createdAt?: string
  updatedAt?: string
}

// 获取NFC标签列表
export function getNfcTags() {
  return request({
    url: '/api/nfc-tags',
    method: 'get'
  }) as Promise<NfcTag[]>
}

// 获取单个NFC标签
export function getNfcTagById(id: number) {
  return request({
    url: `/api/nfc-tags/${id}`,
    method: 'get'
  }) as Promise<NfcTag>
}

// 创建NFC标签
export function createNfcTag(data: { tagUid: string; protocolType?: string }) {
  return request({
    url: '/api/nfc-tags',
    method: 'post',
    data
  }) as Promise<NfcTag>
}

// ✅ 绑定NFC标签到宠物（匹配后端路径参数）
export function bindNfcTag(tagId: number, petId: number) {
  return request({
    url: `/api/nfc-tags/bind/${tagId}/${petId}`,  // ✅ 路径参数
    method: 'post'
  })
}

// ✅ 解绑NFC标签（匹配后端）
export function unbindNfcTag(tagId: number) {
  return request({
    url: `/api/nfc-tags/unbind/${tagId}`,
    method: 'post'
  })
}

// 更新NFC标签
export function updateNfcTag(id: number, data: Partial<NfcTag>) {
  return request({
    url: `/api/nfc-tags/${id}`,
    method: 'put',
    data
  }) as Promise<NfcTag>
}

// 删除NFC标签
export function deleteNfcTag(id: number) {
  return request({
    url: `/api/nfc-tags/${id}`,
    method: 'delete'
  })
}

// 通过tagUid扫描获取宠物信息
export function scanNfcTag(tagUid: string) {
  return request({
    url: `/api/nfc-tags/scan/${tagUid}`,
    method: 'get'
  })
}
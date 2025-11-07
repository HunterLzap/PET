import request from '@/utils/request'

// 获取NFC列表（示例）
export function getNfcTags() {
  return request({
    url: '/api/nfc-tags',
    method: 'get'
  })
}
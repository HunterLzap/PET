import request from '@/utils/request'
import type { NfcTag } from '@/types'

export function getNfcTags() {
  return request({
    url: '/nfc-tags',
    method: 'get'
  }) as Promise<NfcTag[]>
}

export function getNfcTagById(id: number) {
  return request({
    url: `/nfc-tags/${id}`,
    method: 'get'
  }) as Promise<NfcTag>
}

export function createNfcTag(data: NfcTag) {
  return request({
    url: '/nfc-tags',
    method: 'post',
    data
  }) as Promise<NfcTag>
}

export function bindNfcTag(tagId: number, petId: number) {
  return request({
    url: `/nfc-tags/bind/${tagId}/${petId}`,
    method: 'post'
  })
}

export function unbindNfcTag(tagId: number) {
  return request({
    url: `/nfc-tags/unbind/${tagId}`,
    method: 'post'
  })
}

export function scanNfcTag(tagUid: string) {
  return request({
    url: `/nfc-tags/scan/${tagUid}`,
    method: 'get'
  })
}


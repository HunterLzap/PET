import request from '@/utils/request'
import type { Order } from '@/types'

export function getOrders() {
  return request({
    url: '/orders',
    method: 'get'
  }) as Promise<Order[]>
}

export function getOrderById(id: number) {
  return request({
    url: `/orders/${id}`,
    method: 'get'
  }) as Promise<Order>
}

export function createOrder(data: Order) {
  return request({
    url: '/orders',
    method: 'post',
    data
  }) as Promise<Order>
}

export function updateOrder(id: number, data: Order) {
  return request({
    url: `/orders/${id}`,
    method: 'put',
    data
  }) as Promise<Order>
}

export function deleteOrder(id: number) {
  return request({
    url: `/orders/${id}`,
    method: 'delete'
  })
}

export function scanNfcForOrder(tagUid: string) {
  return request({
    url: `/nfc-tags/scan/${tagUid}`,
    method: 'get'
  })
}


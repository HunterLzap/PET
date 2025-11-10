import request from '@/utils/request'

// 订单接口类型定义
export interface Order {
  id?: number
  userId: number
  businessId: number
  serviceId: number
  petId?: number
  totalAmount: number
  status: string
  paymentStatus: string
  appointmentTime?: string
  remarks?: string
  createdAt?: string
  updatedAt?: string
}

// 获取订单列表
export function getOrders() {
  return request({
    url: '/api/orders',
    method: 'get'
  }) as Promise<Order[]>
}

// 获取单个订单
export function getOrderById(id: number) {
  return request({
    url: `/api/orders/${id}`,
    method: 'get'
  }) as Promise<Order>
}

// 创建订单
export function createOrder(data: Order) {
  return request({
    url: '/api/orders',
    method: 'post',
    data
  }) as Promise<Order>
}

// 更新订单
export function updateOrder(id: number, data: Order) {
  return request({
    url: `/api/orders/${id}`,
    method: 'put',
    data
  }) as Promise<Order>
}

// 删除订单
export function deleteOrder(id: number) {
  return request({
    url: `/api/orders/${id}`,
    method: 'delete'
  })
}

// NFC扫码获取宠物信息（用于创建订单）
export function scanNfcForOrder(tagUid: string) {
  return request({
    url: `/api/nfc-tags/scan/${tagUid}`,
    method: 'get'
  })
}
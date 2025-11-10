import request from '@/utils/request'

// 客户接口类型定义
export interface Customer {
  id?: number
  merchantId?: number
  name: string
  phone: string
  userId?: number
  gender?: string
  address?: string
  notes?: string
  totalOrders?: number
  totalAmount?: number
  lastVisitAt?: string
  status?: number
  createdAt?: string
  updatedAt?: string
}

// 获取客户列表
export function getCustomers() {
  return request({
    url: '/api/customers',
    method: 'get'
  }) as Promise<Customer[]>
}

// 搜索客户
export function searchCustomers(keyword: string) {
  return request({
    url: '/api/customers/search',
    method: 'get',
    params: { keyword }
  }) as Promise<Customer[]>
}

// 获取客户详情
export function getCustomerById(id: number) {
  return request({
    url: `/api/customers/${id}`,
    method: 'get'
  }) as Promise<Customer>
}

// 创建客户
export function createCustomer(data: Customer) {
  return request({
    url: '/api/customers',
    method: 'post',
    data
  }) as Promise<Customer>
}

// 更新客户
export function updateCustomer(id: number, data: Customer) {
  return request({
    url: `/api/customers/${id}`,
    method: 'put',
    data
  }) as Promise<Customer>
}

// 删除客户
export function deleteCustomer(id: number) {
  return request({
    url: `/api/customers/${id}`,
    method: 'delete'
  })
}

// 获取客户的宠物列表
export function getCustomerPets(customerId: number) {
  return request({
    url: `/api/user/pets/customer/${customerId}`,
    method: 'get'
  })
}
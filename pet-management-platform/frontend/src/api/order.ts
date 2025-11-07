import request from '@/utils/request'

// 获取订单列表（只是一个示例，避免报错）
export function getOrders() {
  return request({
    url: '/api/user/orders', // 假设有这个接口
    method: 'get'
  })
}
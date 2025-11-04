import request from '@/utils/request'
import type { LoginRequest, LoginResponse } from '@/types'

export function login(data: LoginRequest) {
  return request({
    url: '/auth/signin',
    method: 'post',
    data
  }) as Promise<LoginResponse>
}

export function register(data: any) {
  return request({
    url: '/auth/signup',
    method: 'post',
    data
  })
}


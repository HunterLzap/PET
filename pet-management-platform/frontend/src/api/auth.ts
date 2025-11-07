import request from '@/utils/request'

// 登录请求参数
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应
export interface LoginResponse {
  id: number
  username: string
  email: string
  roles: string[]
  accessToken: string
  tokenType: string
}

// 登录
export function login(data: LoginRequest) {
  return request({
    url: '/api/auth/signin',
    method: 'post',
    data
  })
}

// 注册请求参数
export interface SignupRequest {
  username: string
  email: string
  password: string
  phone?: string
  realName?: string
}

// 注册
export function signup(data: SignupRequest) {
  return request({
    url: '/api/auth/signup',
    method: 'post',
    data
  })
}
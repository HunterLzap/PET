import axios from 'axios'
import type { AxiosInstance, AxiosError, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8081',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    console.log('📤 请求拦截 -', config.url, '| Token:', !!token)
    
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    console.error('❌ 请求错误:', error)
    return Promise.reject(error)
  }
)

let isUnauthorizedMessageShown = false

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    console.log('✅ 响应成功 -', response.config.url, '| 状态:', response.status)
    return response.data
  },
  (error: AxiosError) => {
    const status = error.response?.status
    console.error('❌ 响应错误 -', error.config?.url, '| 状态:', status)
    
    if (status === 401) {
      if (!isUnauthorizedMessageShown) {
        isUnauthorizedMessageShown = true
        ElMessage.error('登录已过期，请重新登录')
        
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        
        router.push('/login').then(() => {
          setTimeout(() => {
            isUnauthorizedMessageShown = false
          }, 1000)
        })
      }
    } 
    else if (status === 403) {
      ElMessage.warning('您没有权限访问该功能')
    } 
    else {
      const errorMsg = (error.response?.data as any)?.message || '请求失败，请稍后重试'
      ElMessage.error(errorMsg)
    }
    
    return Promise.reject(error)
  }
)

export default service
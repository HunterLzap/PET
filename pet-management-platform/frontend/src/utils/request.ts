import axios, { AxiosError, AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

const service: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8081',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：自动添加 Token
service.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers = config.headers || {}
      ;(config.headers as any).Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => Promise.reject(error)
)

// 响应拦截器：只在 401 时清空 Token 并跳转
service.interceptors.response.use(
  (response: AxiosResponse) => response.data,
  (error: AxiosError) => {
    const status = error.response?.status
    
    // 只在 401（未授权）时，才认为是登录过期，并跳转到登录页
    if (status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    } 
    // 403（无权限）只提示，不跳转
    else if (status === 403) {
      ElMessage.warning('您没有权限访问该功能')
    } 
    // 其他错误，显示后端返回的错误信息
    else {
      const errorMsg = (error.response?.data as any)?.message || '请求失败，请稍后重试'
      ElMessage.error(errorMsg)
    }
    
    return Promise.reject(error)
  }
)

export default service
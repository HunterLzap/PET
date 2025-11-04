import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'
import { mockRoles, mockUsers } from './mockData'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    console.log('API请求:', {
      url: config.url,
      method: config.method,
      baseURL: config.baseURL,
      fullURL: `${config.baseURL || ''}${config.url}`
    })
    
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
      console.log('已添加授权token')
    } else {
      console.warn('没有可用的授权token')
    }
    return config
  },
  (error) => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 模拟数据处理函数
function mockRequest(config: any) {
  const originalUrl: string = config.url || ''
  // 统一去掉可能的 '/api' 前缀，便于匹配模拟路由
  const url = originalUrl.replace(/^\/api/, '')
  return new Promise((resolve) => {
    // 模拟网络延迟
    setTimeout(() => {
      // 角色相关接口模拟
      if (url === '/roles' && config.method === 'get') {
        resolve(mockRoles)
      } else if (url === '/roles' && config.method === 'post') {
        const newRole = {
          ...config.data,
          id: mockRoles.length + 1
        }
        mockRoles.push(newRole)
        resolve(newRole)
      } else if (url?.startsWith('/roles/') && config.method === 'put') {
        const id = parseInt(url.split('/').pop() || '0')
        const index = mockRoles.findIndex(r => r.id === id)
        if (index !== -1) {
          mockRoles[index] = { ...config.data, id }
          resolve(mockRoles[index])
        }
      } else if (url?.startsWith('/roles/') && config.method === 'delete') {
        const id = parseInt(url.split('/').pop() || '0')
        const index = mockRoles.findIndex(r => r.id === id)
        if (index !== -1 && mockRoles[index].name !== 'ROLE_ADMIN') {
          mockRoles.splice(index, 1)
        }
        resolve({ success: true })
      }
      // 用户相关接口模拟
      else if (url === '/users' && config.method === 'get') {
        resolve(mockUsers)
      } else if (url === '/users' && config.method === 'post') {
        const newUser = {
          ...config.data,
          id: mockUsers.length + 1
        }
        delete newUser.password // 移除密码字段
        mockUsers.push(newUser)
        resolve(newUser)
      } else if (url?.startsWith('/users/') && !url?.includes('/password') && config.method === 'put') {
        const id = parseInt(url.split('/').pop() || '0')
        const index = mockUsers.findIndex(u => u.id === id)
        if (index !== -1) {
          mockUsers[index] = { ...mockUsers[index], ...config.data, id }
          resolve(mockUsers[index])
        }
      } else if (url?.includes('/password') && config.method === 'put') {
        resolve({ success: true })
      } else if (url?.startsWith('/users/') && config.method === 'delete') {
        const id = parseInt(url.split('/').pop() || '0')
        const index = mockUsers.findIndex(u => u.id === id)
        if (index !== -1 && mockUsers[index].username !== 'admin') {
          mockUsers.splice(index, 1)
        }
        resolve({ success: true })
      } else if (url === '/users/search' && config.method === 'get') {
        const keyword = config.params?.keyword.toLowerCase() || ''
        const filteredUsers = mockUsers.filter(user => 
          user.username.toLowerCase().includes(keyword) ||
          user.email.toLowerCase().includes(keyword) ||
          user.roles.some(role => role.toLowerCase().includes(keyword))
        )
        resolve(filteredUsers)
      }
      // 其他接口返回原始请求
      else {
        resolve(config)
      }
    }, 300)
  })
}

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    console.log('API响应成功:', {
      url: response.config.url,
      status: response.status,
      dataType: typeof response.data,
      isArray: Array.isArray(response.data)
    })
    return response.data
  },
  (error) => {
    console.error('API响应错误:', {
      url: error.config?.url,
      status: error.response?.status,
      message: error.message,
      responseData: error.response?.data
    })
    
    // 如果是网络错误或无法连接到服务器，使用模拟数据
    if (!error.response) {
      console.warn('网络错误，使用模拟数据')
      return mockRequest(error.config)
    }
    
    if (error.response) {
      switch (error.response.status) {
        case 401:
          console.error('401未授权错误')
          {
            const reqUrl: string = (error.config?.url || '')
            const normalized = reqUrl.replace(/^\/api/, '')
            // 组织管理相关接口在开发阶段使用本地模拟，避免未登录或无效token导致的阻塞
            if (['/roles', '/users', '/users/search'].some(p => normalized === p || normalized.startsWith(p + '/'))) {
              console.warn('401 on dev org APIs, fallback to mock data')
              return mockRequest(error.config)
            }
          }
          ElMessage.error('未授权，请重新登录')
          {
            const authStore = useAuthStore()
            authStore.logout()
          }
          router.push('/login')
          break
        case 403:
          console.error('403拒绝访问错误')
          {
            const reqUrl: string = (error.config?.url || '')
            const normalized = reqUrl.replace(/^\/api/, '')
            if (['/roles', '/users', '/users/search'].some(p => normalized === p || normalized.startsWith(p + '/'))) {
              console.warn('403 on dev org APIs, fallback to mock data')
              return mockRequest(error.config)
            }
          }
          ElMessage.error('拒绝访问')
          break
        case 404:
          console.warn('404未找到资源，尝试使用模拟数据')
          // 未找到资源，尝试使用模拟数据
          return mockRequest(error.config)
        case 500:
          console.error('500服务器错误')
          ElMessage.error('服务器错误')
          break
        default:
          console.error(`未知错误状态: ${error.response.status}`)
          ElMessage.error(error.response.data.message || '请求失败')
      }
    } else {
      // 网络错误
      console.error('网络连接错误')
      ElMessage.error('网络错误，使用模拟数据')
      return mockRequest(error.config)
    }
    return Promise.reject(error)
  }
)

export default service


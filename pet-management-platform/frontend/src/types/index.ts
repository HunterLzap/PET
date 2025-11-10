// 登录请求
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

// 客户信息
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

// 字典值
export interface DictValue {
  id: number
  dictCode: string
  valueCode: string
  valueName: string
  valueOrder: number
  extraData?: string
  colorTag?: string
  icon?: string
  status: number
}

// 宠物信息
export interface Pet {
  id?: number
  name: string
  speciesCode: string
  speciesName?: string
  breedCode: string
  breedName?: string
  genderCode: string
  genderName?: string
  birthDate?: string
  ageInMonths?: number
  weight?: number
  color?: string
  description?: string
  avatarUrl?: string
  createdAt?: string
  updatedAt?: string
}

// 添加宠物请求
export interface AddPetRequest {
  customerId?: number  // 商家为客户添加宠物时使用
  name: string
  speciesCode: string
  breedCode: string
  genderCode: string
  birthDate?: string
  weight?: number
  color?: string
  description?: string
  avatarUrl?: string
}

// 服务信息
export interface Service {
  id?: number
  businessId?: number
  name: string
  type: string
  price: number
  description?: string
  durationMinutes?: number
  createdAt?: string
  updatedAt?: string
}

// 订单信息
export interface Order {
  id?: number
  userId: number
  businessId: number
  customerId?: number  // 商家为客户下单时使用
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

// 健康记录
export interface HealthRecord {
  id?: number
  petId: number
  recordDate: string
  recordType: string
  description: string
  notes?: string
  attachmentUrl?: string
  createdAt?: string
  updatedAt?: string
}
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
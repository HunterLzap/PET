export interface User {
  id: number
  username: string
  email: string
  roles: string[]
}

export interface Role {
  id: number
  name: string
  description: string
  permissions: {
    [menuKey: string]: {
      canView: boolean
      canCreate: boolean
      canUpdate: boolean
      canDelete: boolean
    }
  }
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  tokenType: string
  id: number
  username: string
  email: string
  roles: string[]
}

export interface Pet {
  id?: number
  ownerId: number
  name: string
  species: string
  breedId?: number
  gender: 'MALE' | 'FEMALE' | 'UNKNOWN'
  birthday?: string
  weight?: number
  color?: string
  description?: string
  avatarUrl?: string
  createdAt?: string
  updatedAt?: string
}

export interface NfcTag {
  id?: number
  tagUid: string
  petId?: number
  protocolType: string
  status: 'ACTIVE' | 'INACTIVE' | 'LOST' | 'BOUND' | 'UNBOUND'
  activatedAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface Service {
  id?: number
  businessId: number
  name: string
  type: string
  price: number
  description?: string
  durationMinutes: number
  createdAt?: string
  updatedAt?: string
}

export interface Order {
  id?: number
  userId: number
  businessId: number
  serviceId: number
  petId?: number
  totalAmount: number
  status: 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED'
  paymentStatus: 'PENDING' | 'PAID' | 'REFUNDED'
  appointmentTime?: string
  remarks?: string
  createdAt?: string
  updatedAt?: string
}

export interface BaseData {
  id?: number
  type: string
  value: string
  description?: string
  createdAt?: string
  updatedAt?: string
}

export interface HealthRecord {
  id?: number
  petId: number
  recordDate: string
  recordType: string
  description?: string
  notes?: string
  attachmentUrl?: string
  createdAt?: string
  updatedAt?: string
}


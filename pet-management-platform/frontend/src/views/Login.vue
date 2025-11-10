<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span class="title">宠物综合管理平台</span>
        </div>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
        <el-form-item style="text-align: center; margin-bottom: 0;">
          <span style="color: #909399; font-size: 14px;">还没有账号？</span>
          <el-link type="primary" @click="router.push('/register')" style="margin-left: 5px;">
            立即注册
          </el-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import type { LoginRequest } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive<LoginRequest>({
  username: '',
  password: ''
})

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        console.log('🔐 开始登录...')
        const response: any = await login(loginForm)
        
        console.log('📦 登录响应:', response)
        console.log('🎫 Token:', response.accessToken)
        console.log('👤 用户ID:', response.id)
        console.log('📧 邮箱:', response.email)
        console.log('🏷️ 角色:', response.roles)
        
        if (!response.accessToken) {
          console.error('❌ 响应中没有accessToken')
          ElMessage.error('登录失败：未获取到token')
          return
        }
        
        const roles = Array.isArray(response.roles) ? response.roles : []
        
        authStore.setToken(response.accessToken)
        authStore.setUser({
          id: response.id,
          username: response.username,
          email: response.email,
          roles: roles
        })
        
        console.log('💾 Token已保存:', authStore.token.substring(0, 20) + '...')
        console.log('💾 用户已保存:', authStore.user)
        
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error: any) {
        console.error('❌ 登录失败:', error)
        console.error('错误响应:', error.response)
        
        const errorMsg = error.response?.data?.message || '登录失败，请检查用户名和密码'
        ElMessage.error(errorMsg)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 450px;
}

.card-header {
  text-align: center;
}

.title {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}
</style>
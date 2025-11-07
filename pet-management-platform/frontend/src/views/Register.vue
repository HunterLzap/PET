<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <span class="title">用户注册</span>
        </div>
      </template>
      
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="100px">
        <!-- 注册类型选择 -->
        <el-form-item label="注册类型" prop="userType">
          <el-radio-group v-model="registerForm.userType" @change="handleUserTypeChange" class="type-radio-group">
            <el-radio-button label="USER">
              <div class="radio-content">
                <div class="radio-title">
                  <el-icon><User /></el-icon>
                  <span>普通用户</span>
                </div>
                <div class="radio-description">宠物主人，管理宠物信息</div>
              </div>
            </el-radio-button>
            <el-radio-button label="MERCHANT">
              <div class="radio-content">
                <div class="radio-title">
                  <el-icon><Shop /></el-icon>
                  <span>商家</span>
                </div>
                <div class="radio-description">提供宠物服务（医院/宠物馆/用品商家）</div>
              </div>
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 商家类型选择（仅商家显示）-->
        <el-form-item 
          v-if="registerForm.userType === 'MERCHANT'" 
          label="商家类型" 
          prop="merchantType"
        >
          <el-select v-model="registerForm.merchantType" placeholder="请选择商家类型" style="width: 100%;">
            <el-option label="🏥 宠物医院 - 提供医疗、体检、疫苗等服务" value="merchant_hospital" />
            <el-option label="🏠 宠物馆 - 提供寄养、美容、训练等服务" value="merchant_house" />
            <el-option label="🛒 用品商家 - 销售宠物用品、食品等" value="merchant_goods" />
          </el-select>
        </el-form-item>

        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>

        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="registerForm.username" 
            placeholder="请输入用户名（3-20位字母数字）" 
            autocomplete="off"
          />
        </el-form-item>
        
        <el-form-item label="真实姓名" prop="realName">
          <el-input 
            v-model="registerForm.realName" 
            :placeholder="registerForm.userType === 'MERCHANT' ? '请输入负责人姓名（必填）' : '请输入真实姓名（选填）'"
            autocomplete="off"
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" autocomplete="off" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="registerForm.phone" 
            :placeholder="registerForm.userType === 'MERCHANT' ? '请输入手机号（必填）' : '请输入手机号（选填）'"
            autocomplete="off"
          />
        </el-form-item>

        <!-- 商家信息（仅商家显示）-->
        <template v-if="registerForm.userType === 'MERCHANT'">
          <el-divider content-position="left">商家信息</el-divider>

          <el-form-item label="商家名称" prop="merchantName">
            <el-input 
              v-model="registerForm.merchantName" 
              placeholder="请输入商家名称（如：爱宠宠物医院）" 
              autocomplete="off"
            />
          </el-form-item>

          <el-form-item label="营业执照号" prop="businessLicense">
            <el-input 
              v-model="registerForm.businessLicense" 
              placeholder="请输入营业执照号" 
              autocomplete="off"
            />
          </el-form-item>

          <el-form-item label="商家地址" prop="address">
            <el-input 
              v-model="registerForm.address" 
              placeholder="请输入详细地址" 
              autocomplete="off"
            />
          </el-form-item>

          <el-alert
            title="提示：商家账号注册后需要平台审核，审核通过后方可使用"
            type="info"
            :closable="false"
            style="margin-bottom: 20px;"
          />
        </template>

        <!-- 密码设置 -->
        <el-divider content-position="left">密码设置</el-divider>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="请输入密码（至少6位）" 
            show-password 
            autocomplete="new-password"
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入密码" 
            show-password 
            autocomplete="new-password"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleRegister" 
            :loading="loading" 
            style="width: 100%"
          >
            {{ registerForm.userType === 'MERCHANT' ? '提交注册申请' : '立即注册' }}
          </el-button>
        </el-form-item>
        
        <el-form-item style="text-align: center; margin-bottom: 0;">
          <span style="color: #909399; font-size: 14px;">已有账号？</span>
          <el-link type="primary" @click="router.push('/login')" style="margin-left: 5px;">
            立即登录
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
import { User, Shop } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { signup } from '@/api/auth'

const router = useRouter()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)

const registerForm = reactive({
  userType: 'USER',
  merchantType: '',
  username: '',
  realName: '',
  email: '',
  phone: '',
  merchantName: '',
  businessLicense: '',
  address: '',
  password: '',
  confirmPassword: ''
})

// 用户类型改变时，清空相关字段
const handleUserTypeChange = (value: string) => {
  if (value === 'USER') {
    registerForm.merchantType = ''
    registerForm.merchantName = ''
    registerForm.businessLicense = ''
    registerForm.address = ''
  }
  // 重新验证表单
  registerFormRef.value?.clearValidate()
}

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule: any, value: any, callback: any) => {
  if (registerForm.userType === 'MERCHANT' && !value) {
    callback(new Error('商家手机号不能为空'))
  } else if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const validateRealName = (rule: any, value: any, callback: any) => {
  if (registerForm.userType === 'MERCHANT' && !value) {
    callback(new Error('商家负责人姓名不能为空'))
  } else {
    callback()
  }
}

const rules = reactive<FormRules>({
  userType: [
    { required: true, message: '请选择注册类型', trigger: 'change' }
  ],
  merchantType: [
    { 
      required: true, 
      message: '请选择商家类型', 
      trigger: 'change',
      validator: (rule: any, value: any, callback: any) => {
        if (registerForm.userType === 'MERCHANT' && !value) {
          callback(new Error('请选择商家类型'))
        } else {
          callback()
        }
      }
    }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  realName: [
    { validator: validateRealName, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  merchantName: [
    { 
      validator: (rule: any, value: any, callback: any) => {
        if (registerForm.userType === 'MERCHANT' && !value) {
          callback(new Error('请输入商家名称'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  businessLicense: [
    { 
      validator: (rule: any, value: any, callback: any) => {
        if (registerForm.userType === 'MERCHANT' && !value) {
          callback(new Error('请输入营业执照号'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  address: [
    { 
      validator: (rule: any, value: any, callback: any) => {
        if (registerForm.userType === 'MERCHANT' && !value) {
          callback(new Error('请输入商家地址'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 构建注册数据
        const signupData: any = {
          username: registerForm.username,
          email: registerForm.email,
          password: registerForm.password,
          phone: registerForm.phone || undefined,
          realName: registerForm.realName || undefined,
          userType: registerForm.userType,

          // 核心修改：添加商家信息
          merchantName: registerForm.merchantName,
          businessLicense: registerForm.businessLicense,
          address: registerForm.address
        }

        // 根据用户类型设置角色
        if (registerForm.userType === 'MERCHANT') {
          signupData.role = [registerForm.merchantType]
        }
        
        await signup(signupData)
        
        if (registerForm.userType === 'MERCHANT') {
          ElMessage.success('注册申请已提交！请等待平台审核')
        } else {
          ElMessage.success('注册成功！请登录')
        }
        
        setTimeout(() => {
          router.push('/login')
        }, 2000)
        
      } catch (error: any) {
        console.error('Register error:', error)
        const errorMsg = error.response?.data?.message || '注册失败，请重试'
        ElMessage.error(errorMsg)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
  width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.card-header {
  text-align: center;
}

.title {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.type-radio-group {
  width: 100%;
}

.radio-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 10px;
  text-align: left;
}

.radio-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.radio-description {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  line-height: 1.4;
}

:deep(.el-radio-button__inner) {
  width: 100%;
  height: auto;
  padding: 0;
  border-radius: 4px !important;
  border-left: 1px solid var(--el-border-color);
  box-shadow: none !important;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner .radio-title) {
  color: var(--el-color-white);
}
:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner .radio-description) {
  color: rgba(255, 255, 255, 0.75);
}
</style>
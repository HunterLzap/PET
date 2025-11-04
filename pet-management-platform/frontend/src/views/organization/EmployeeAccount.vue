<template>
  <div class="user-account-container">
    <el-card class="user-card">
      <template #header>
        <div class="card-header">
          <span>用户账号列表</span>
          <el-button type="primary" @click="handleCreateUser">新增用户</el-button>
        </div>
      </template>
      
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入用户名、邮箱或角色进行搜索"
          style="width: 300px"
          :prefix-icon="Search"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
      
      <el-table :data="users" style="width: 100%">
        <el-table-column prop="id" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="角色" width="180">
          <template #default="{ row }">
            <span>{{ row.roles.join(', ') }}</span>
          </template>
        </el-table-column>
  <el-table-column label="操作" width="160" fixed="right">
    <template #default="{ row }">
      <div class="action-buttons">
        <el-button size="small" @click="handleEditUser(row)">编辑</el-button>
        <el-button size="small" type="danger" @click="handleDeleteUser(row.id)" :disabled="row.username === 'admin'">删除</el-button>
      </div>
    </template>
  </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑用户弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleCloseDialog"
    >
      <el-form ref="userFormRef" :model="userForm" label-width="100px" :rules="formRules">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" :disabled="editingUser?.username === 'admin'" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" type="email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item v-if="!editingUser" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="角色" prop="roles">
          <el-select v-model="userForm.roles" multiple placeholder="请选择角色" :disabled="editingUser?.username === 'admin'">
            <el-option v-for="role in roles" :key="role.id" :label="`${role.name} (${role.description})`" :value="role.name" />
          </el-select>
        </el-form-item>

        <el-divider v-if="editingUser" />
        <template v-if="editingUser">
          <el-form-item label="重置密码">
            <el-input v-model="resetPassword" type="password" placeholder="输入新密码（至少6位）" show-password />
          </el-form-item>
          <div style="text-align:right;margin-top:-12px;">
            <el-button :disabled="!resetPassword || resetPassword.length<6" @click="submitResetPassword">立即重置</el-button>
          </div>
        </template>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { User, Role } from '@/types'
import { getUsers, createUser, updateUser, deleteUser, searchUsers, updateUserPassword, getRoles } from '@/api/organization'
import { useAuthStore } from '@/stores/auth'

const users = ref<User[]>([])
const roles = ref<Role[]>([]) // 添加角色列表引用
const searchKeyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增员工')
const editingUser = ref<User | null>(null)
const userFormRef = ref<FormInstance>()

const resetPassword = ref<string>('')

// 用户表单数据
const userForm = ref<Partial<User> & { password?: string }>({
  username: '',
  email: '',
  roles: [],
  password: ''
})

// 表单验证规则
const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  roles: [
    { required: true, message: '请选择角色', trigger: 'change' },
    { type: 'array', min: 1, message: '至少选择一个角色', trigger: 'change' }
  ]
}

// 获取用户列表和角色列表
const fetchData = async () => {
  try {
    console.log('EmployeeAccount: 开始获取数据...')
    console.log('当前组件路径:', window.location.pathname)
    console.log('是否有权限:', useAuthStore().isAuthenticated)
    console.log('用户角色:', useAuthStore().user?.roles)
    
    // 并行获取用户和角色数据
    const [usersData, rolesData] = await Promise.all([
      getUsers().catch(error => {
        console.error('EmployeeAccount: 获取用户列表失败:', error)
        ElMessage.error('获取用户列表失败，使用模拟数据')
        // 返回模拟数据以确保页面能正常显示
        return [
          { id: 1, username: 'admin', email: 'admin@example.com', roles: ['ROLE_ADMIN'] },
          { id: 2, username: 'business', email: 'business@example.com', roles: ['ROLE_BUSINESS'] }
        ]
      }),
      getRoles().catch(error => {
        console.error('EmployeeAccount: 获取角色列表失败:', error)
        ElMessage.error('获取角色列表失败，使用模拟数据')
        // 返回模拟数据以确保页面能正常显示
        return [
          { id: 1, name: 'ROLE_ADMIN', description: '系统管理员', permissions: {} },
          { id: 2, name: 'ROLE_BUSINESS', description: '商家角色', permissions: {} },
          { id: 3, name: 'ROLE_PET_OWNER', description: '宠物主人', permissions: {} }
        ]
      })
    ])
    
    console.log('EmployeeAccount: 获取用户数据结果:', { usersData, rolesData })
    
    // 验证数据
    if (!usersData || !Array.isArray(usersData)) {
      console.error('EmployeeAccount: 用户数据无效，使用模拟数据')
      users.value = [
        { id: 1, username: 'admin', email: 'admin@example.com', roles: ['ROLE_ADMIN'] },
        { id: 2, username: 'business', email: 'business@example.com', roles: ['ROLE_BUSINESS'] }
      ]
    } else {
      users.value = usersData
    }
    
    if (!rolesData || !Array.isArray(rolesData)) {
      console.error('EmployeeAccount: 角色数据无效，使用模拟数据')
      roles.value = [
        { id: 1, name: 'ROLE_ADMIN', description: '系统管理员', permissions: {} },
        { id: 2, name: 'ROLE_BUSINESS', description: '商家角色', permissions: {} },
        { id: 3, name: 'ROLE_PET_OWNER', description: '宠物主人', permissions: {} }
      ]
    } else {
      roles.value = rolesData
    }
  } catch (error) {
    console.error('EmployeeAccount: 获取数据失败:', error)
    ElMessage.error('获取数据失败，请检查控制台')
    
    // 使用默认模拟数据
    users.value = [
      { id: 1, username: 'admin', email: 'admin@example.com', roles: ['ROLE_ADMIN'] },
      { id: 2, username: 'business', email: 'business@example.com', roles: ['ROLE_BUSINESS'] }
    ]
    roles.value = [
      { id: 1, name: 'ROLE_ADMIN', description: '系统管理员', permissions: {} },
      { id: 2, name: 'ROLE_BUSINESS', description: '商家角色', permissions: {} },
      { id: 3, name: 'ROLE_PET_OWNER', description: '宠物主人', permissions: {} }
    ]
  }
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    console.log('开始获取用户列表...')
    const data = await getUsers()
    console.log('获取用户列表成功:', data)
    users.value = data
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  }
}

// 处理搜索
const handleSearch = async () => {
  try {
    if (searchKeyword.value.trim()) {
      const data = await searchUsers(searchKeyword.value.trim())
      users.value = data
    } else {
      await fetchData()
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，使用现有数据')
  }
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  fetchData()
}

// 处理新增用户
const handleCreateUser = () => {
  dialogTitle.value = '新增用户'
  editingUser.value = null
  userForm.value = {
    username: '',
    email: '',
    roles: [],
    password: ''
  }
  dialogVisible.value = true
}

// 处理编辑用户
const handleEditUser = (user: User) => {
  dialogTitle.value = '编辑用户'
  editingUser.value = user
  userForm.value = {
    ...user
  }
  dialogVisible.value = true
}

// 处理重置密码
const handleResetPassword = async (id: number) => {
  try {
    await ElMessageBox.prompt('请输入新密码', '重置密码', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'password'
    }).then(async ({ value }) => {
      if (value && value.length >= 6) {
        await updateUserPassword(id, { password: value })
        ElMessage.success('密码重置成功')
      } else {
        ElMessage.warning('密码长度不能少于 6 个字符')
      }
    })
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('密码重置失败')
    }
  }
}

const submitResetPassword = async () => {
  if (!editingUser.value) return
  if (!resetPassword.value || resetPassword.value.length < 6) {
    ElMessage.warning('请输入至少6位的新密码')
    return
  }
  try {
    await updateUserPassword(editingUser.value.id, { password: resetPassword.value })
    ElMessage.success('密码已重置')
    resetPassword.value = ''
  } catch (e) {
    ElMessage.error('重置密码失败')
  }
}

// 处理删除用户
const handleDeleteUser = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteUser(id)
    ElMessage.success('用户删除成功')
    await fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('用户删除失败')
    }
  }
}

// 处理提交表单
const handleSubmit = async () => {
  try {
    await userFormRef.value?.validate()
    
    if (editingUser.value) {
      // 编辑用户时不更新密码
      const { password, ...updateData } = userForm.value
      await updateUser(editingUser.value.id, updateData as Partial<User>)
      ElMessage.success('用户更新成功')
    } else {
      // 新增用户时需要密码
      await createUser(userForm.value as Omit<User, 'id'> & { password: string })
      ElMessage.success('用户创建成功')
    }
    
    dialogVisible.value = false
    await fetchUsers()
  } catch (error) {
    ElMessage.error('表单验证失败')
  }
}

// 处理关闭弹窗
const handleCloseDialog = () => {
  userFormRef.value?.resetFields()
}

// 组件挂载时获取数据
onMounted(() => {
  console.log('EmployeeAccount组件挂载')
  fetchData()
})
</script>

<style scoped>
.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-account-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
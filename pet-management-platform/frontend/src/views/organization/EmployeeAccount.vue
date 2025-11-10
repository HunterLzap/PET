<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户账号管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名或邮箱"
          clearable
          style="width: 300px; margin-right: 10px;"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="filterUserType" placeholder="用户类型" clearable style="width: 150px; margin-right: 10px;">
          <el-option label="全部" value="" />
          <el-option label="管理员" value="ADMIN" />
          <el-option label="商家" value="BUSINESS" />
          <el-option label="普通用户" value="USER" />
        </el-select>
        <el-button type="primary" @click="fetchData">搜索</el-button>
      </div>

      <el-table :data="paginatedData" v-loading="loading" stripe border style="margin-top: 20px;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="userType" label="用户类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.userType === 'ADMIN'" type="danger">管理员</el-tag>
            <el-tag v-else-if="row.userType === 'BUSINESS'" type="warning">商家</el-tag>
            <el-tag v-else type="success">普通用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="250">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" style="margin-right: 5px;" size="small">
              {{ getRoleDescription(role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">正常</el-tag>
            <el-tag v-else-if="row.status === 2" type="warning">待审核</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleAssignRoles(row)">分配角色</el-button>
            <el-button 
              size="small" 
              :type="row.status === 1 ? 'danger' : 'success'" 
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredData.length"
        />
      </div>
    </el-card>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="form.userType" placeholder="请选择用户类型">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="商家" value="BUSINESS" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="2">待审核</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px">
      <el-checkbox-group v-model="selectedRoles">
        <el-checkbox v-for="role in allRoles" :key="role.name" :label="role.name">
          {{ getRoleDescription(role.name) }}
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitRoles" :loading="roleLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getUsers, createUser, updateUser, assignRoles, toggleUserStatus, getRoles } from '@/api/organization'
import type { User, Role } from '@/api/organization'

const loading = ref(false)
const tableData = ref<User[]>([])
const allRoles = ref<Role[]>([])
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const roleLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const filterUserType = ref('')
const selectedRoles = ref<string[]>([])
const currentUserId = ref<number>(0)

const form = reactive<User>({
  username: '',
  email: '',
  password: '',
  realName: '',
  phone: '',
  userType: 'USER',
  status: 1
})

const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }]
})

// 过滤数据
const filteredData = computed(() => {
  return tableData.value.filter(user => {
    const matchKeyword = !searchKeyword.value ||
      user.username.includes(searchKeyword.value) ||
      user.email.includes(searchKeyword.value)
    const matchType = !filterUserType.value || user.userType === filterUserType.value
    return matchKeyword && matchType
  })
})

// 分页数据
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredData.value.slice(start, end)
})

// 获取角色描述
const getRoleDescription = (roleName: string) => {
  switch (roleName) {
    case 'ROLE_USER': return '普通用户'
    case 'ROLE_MERCHANT_HOSPITAL': return '宠物医院'
    case 'ROLE_MERCHANT_HOUSE': return '宠物馆'
    case 'ROLE_MERCHANT_GOODS': return '用品商家'
    case 'ROLE_ADMIN': return '超级管理员'
    case 'ROLE_DATA_MANAGER': return '数据管理员'
    case 'ROLE_OPERATOR': return '运营管理员'
    default: return roleName
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    tableData.value = await getUsers()
  } catch (error) {
    console.error('Fetch users error:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const fetchRoles = async () => {
  try {
    allRoles.value = await getRoles()
  } catch (error) {
    console.error('Fetch roles error:', error)
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: User) => {
  dialogTitle.value = '编辑用户'
  Object.assign(form, row)
  delete form.password // 编辑时不显示密码
  dialogVisible.value = true
}

const handleAssignRoles = async (row: User) => {
  currentUserId.value = row.id!
  selectedRoles.value = row.roles || []
  await fetchRoles()
  roleDialogVisible.value = true
}

const handleToggleStatus = (row: User) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  
  ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await toggleUserStatus(row.id!, newStatus)
      ElMessage.success(`${action}成功`)
      fetchData()
    } catch (error) {
      console.error('Toggle user status error:', error)
      ElMessage.error(`${action}失败`)
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (form.id) {
          await updateUser(form.id, form)
          ElMessage.success('更新成功')
        } else {
          await createUser(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error('Submit user error:', error)
        ElMessage.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleSubmitRoles = async () => {
  roleLoading.value = true
  try {
    // 将角色名转换为角色ID
    const roleIds = allRoles.value
      .filter(r => selectedRoles.value.includes(r.name))
      .map(r => r.id!)
    
    await assignRoles(currentUserId.value, roleIds)
    ElMessage.success('分配角色成功')
    roleDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('Assign roles error:', error)
    ElMessage.error('分配角色失败')
  } finally {
    roleLoading.value = false
  }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  resetForm()
}

const resetForm = () => {
  Object.assign(form, {
    id: undefined,
    username: '',
    email: '',
    password: '',
    realName: '',
    phone: '',
    userType: 'USER',
    status: 1
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
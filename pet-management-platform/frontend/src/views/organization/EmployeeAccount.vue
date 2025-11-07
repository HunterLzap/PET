<template>
  <div class="user-account-container">
    <el-card class="user-card">
      <template #header>
        <div class="card-header">
          <span>用户账号列表</span>
        </div>
      </template>
      
      <el-table :data="users" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="userType" label="用户类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.userType === 'ADMIN'" type="danger">管理员</el-tag>
            <el-tag v-else-if="row.userType === 'MERCHANT'" type="warning">商家</el-tag>
            <el-tag v-else type="success">普通用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="220">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" style="margin-right: 5px;">
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
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getUsers } from '@/api/organization' // 确保从这里导入
import type { User } from '@/types'

const users = ref<User[]>([])
const loading = ref(true)

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
    default: return '未知角色'
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    users.value = await getUsers()
  } catch (err) {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.user-account-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
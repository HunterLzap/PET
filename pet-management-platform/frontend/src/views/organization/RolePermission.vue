<template>
  <div class="role-permission-container">
    <el-card class="role-card">
      <template #header>
        <div class="card-header">
          <span>角色列表</span>
        </div>
      </template>
      
      <el-table :data="roles" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名" />
        <el-table-column prop="description" label="描述">
          <template #default="{ row }">
            {{ getRoleDescription(row.name) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getRoles } from '@/api/organization' // 确保从这里导入
import type { Role } from '@/types'

const roles = ref<Role[]>([])
const loading = ref(true)

// 获取角色描述
const getRoleDescription = (roleName: string) => {
  switch (roleName) {
    case 'ROLE_USER': return '普通用户（宠物主人）'
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
    roles.value = await getRoles()
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
.role-permission-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
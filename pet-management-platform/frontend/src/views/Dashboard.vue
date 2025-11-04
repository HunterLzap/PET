<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409eff"><PieChart /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pets }}</div>
              <div class="stat-label">宠物总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#67c23a"><Document /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.orders }}</div>
              <div class="stat-label">订单总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#e6a23c"><Postcard /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.nfcTags }}</div>
              <div class="stat-label">NFC吊牌总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#f56c6c"><ShoppingCart /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.services }}</div>
              <div class="stat-label">服务总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>欢迎使用宠物综合管理平台</span>
          </template>
          <div class="welcome-content">
            <p>您好，{{ authStore.user?.username }}！</p>
            <p>当前角色：{{ userRoles }}</p>
            <p>本平台提供宠物档案管理、NFC吊牌绑定、服务预约、订单管理等功能。</p>
            <p>请从左侧菜单选择相应功能进行操作。</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const stats = ref({
  pets: 0,
  orders: 0,
  nfcTags: 0,
  services: 0
})

const userRoles = computed(() => {
  const roles = authStore.user?.roles || []
  const roleMap: Record<string, string> = {
    'ROLE_ADMIN': '管理员',
    'ROLE_BUSINESS': '商家',
    'ROLE_PET_OWNER': '宠物主人'
  }
  return roles.map(role => roleMap[role] || role).join(', ')
})

onMounted(() => {
  // TODO: 从API获取统计数据
  stats.value = {
    pets: 0,
    orders: 0,
    nfcTags: 0,
    services: 0
  }
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  font-size: 48px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.welcome-content {
  line-height: 2;
  font-size: 16px;
}

.welcome-content p {
  margin: 10px 0;
}
</style>


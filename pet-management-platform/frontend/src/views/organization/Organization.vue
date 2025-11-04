<template>
  <div class="organization-container">
    <!-- 子路由内容将在这里渲染 -->
    <router-view />
    
    <!-- 当没有选择子路由时显示欢迎页面 -->
    <el-card v-if="!$route.path.includes('organization/roles') && !$route.path.includes('organization/users')" class="welcome-card">
      <div class="welcome-content">
        <h2>欢迎使用组织管理</h2>
        <p>请从左侧菜单选择需要的功能</p>
        <div class="feature-cards">
          <el-card class="feature-card" @click="$router.push('/organization/roles')">
            <div class="feature-icon">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="feature-info">
              <h3>角色权限</h3>
              <p>管理系统中的角色和权限设置</p>
            </div>
          </el-card>
          
          <el-card class="feature-card" @click="$router.push('/organization/users')">
            <div class="feature-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="feature-info">
              <h3>用户账号</h3>
              <p>管理系统中的用户账号信息</p>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { UserFilled, User } from '@element-plus/icons-vue'
</script>

<style scoped>
.organization-container {
  padding: 20px;
  min-height: calc(100vh - 100px);
}

.welcome-card {
  background-color: #f8f9fa;
  border: none;
  border-radius: 12px;
  overflow: hidden;
}

.welcome-content {
  text-align: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.welcome-content h2 {
  margin-bottom: 16px;
  color: #2c3e50;
  font-size: 28px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.welcome-content p {
  margin-bottom: 50px;
  color: #7f8c8d;
  font-size: 16px;
  max-width: 500px;
  margin-left: auto;
  margin-right: auto;
}

.feature-cards {
  display: flex;
  justify-content: center;
  gap: 30px;
  flex-wrap: wrap;
  max-width: 900px;
  margin: 0 auto;
}

.feature-card {
  width: 300px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e4e7ed;
  background-color: white;
  border-radius: 10px;
  padding: 30px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
}

.feature-card:hover {
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.15);
  transform: translateY(-4px);
  border-color: #409eff;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #409eff, #67c23a);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.feature-card:hover::before {
  transform: scaleX(1);
}

.feature-icon {
  font-size: 52px;
  color: #409eff;
  margin-bottom: 20px;
  transition: transform 0.3s ease;
}

.feature-card:hover .feature-icon {
  transform: scale(1.1);
}

.feature-info h3 {
  margin-bottom: 10px;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
  transition: color 0.3s ease;
}

.feature-card:hover .feature-info h3 {
  color: #409eff;
}

.feature-info p {
  color: #606266;
  margin-bottom: 0;
  font-size: 14px;
  line-height: 1.6;
}
</style>
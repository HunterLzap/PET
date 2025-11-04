<template>
  <div class="role-permission-container">
    <el-card class="role-card">
      <template #header>
        <div class="card-header">
          <span>角色列表</span>
          <el-button type="primary" @click="handleCreateRole">新增角色</el-button>
        </div>
      </template>
      
      <el-table :data="roles" style="width: 100%">
        <el-table-column prop="id" label="角色ID" width="80" />
        <el-table-column prop="name" label="角色名称" />
        <el-table-column prop="description" label="角色描述" />
  <el-table-column label="操作" width="160" fixed="right">
    <template #default="{ row }">
      <div class="action-buttons">
        <el-button size="small" @click="handleEditRole(row)">编辑</el-button>
        <el-button size="small" type="danger" @click="handleDeleteRole(row.id)" :disabled="row.name === 'ROLE_ADMIN'">删除</el-button>
      </div>
    </template>
  </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑角色弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="handleCloseDialog"
    >
      <el-form ref="roleFormRef" :model="roleForm" label-width="100px" :rules="formRules">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" :disabled="editingRole?.name === 'ROLE_ADMIN'" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" placeholder="请输入角色描述" />
        </el-form-item>
        
        <el-form-item label="菜单权限">
          <el-tree
            v-model="checkedMenus"
            :data="menuTree"
            show-checkbox
            node-key="key"
            check-strictly
            default-expand-all
          />
        </el-form-item>
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
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { Role } from '@/types'
import { getRoles, createRole, updateRole, deleteRole } from '@/api/organization'
import { useAuthStore } from '@/stores/auth'

const roles = ref<Role[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const editingRole = ref<Role | null>(null)
const roleFormRef = ref<FormInstance>()

// 菜单树数据
const menuTree = ref([
  {
    key: 'dashboard',
    label: '工作台',
    children: []
  },
  {
    key: 'pets',
    label: '宠物管理',
    children: []
  },
  {
    key: 'nfc-tags',
    label: 'NFC吊牌管理',
    children: []
  },
  {
    key: 'services',
    label: '服务管理',
    children: []
  },
  {
    key: 'orders',
    label: '订单管理',
    children: []
  },
  {
    key: 'base-data',
    label: '基础数据管理',
    children: []
  },
  {
    key: 'health-records',
    label: '健康记录管理',
    children: []
  },
  {
    key: 'organization',
    label: '组织管理',
    children: [
      {
        key: 'roles',
        label: '角色权限',
        children: []
      },
      {            key: 'users',            label: '用户账号',            children: []          }
    ]
  }
])

// 角色表单数据
const roleForm = ref<Partial<Role>>({
  name: '',
  description: '',
  permissions: {}
})

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 3, max: 20, message: '角色名称长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 100, message: '角色描述不能超过 100 个字符', trigger: 'blur' }
  ]
}

// 已选菜单
const checkedMenus = computed({
  get() {
    const checked: string[] = []
    if (roleForm.value.permissions) {
      Object.keys(roleForm.value.permissions).forEach(key => {
        if (roleForm.value.permissions![key].canView) {
          checked.push(key)
        }
      })
    }
    return checked
  },
  set(keys: string[]) {
    const permissions: Role['permissions'] = {}
    
    // 重置所有权限
    menuTree.value.forEach(menu => {
      permissions[menu.key] = {
        canView: false,
        canCreate: false,
        canUpdate: false,
        canDelete: false
      }
      
      if (menu.children) {
        menu.children.forEach(child => {
          permissions[child.key] = {
            canView: false,
            canCreate: false,
            canUpdate: false,
            canDelete: false
          }
        })
      }
    })
    
    // 设置选中的菜单权限
    keys.forEach(key => {
      if (!permissions[key]) {
        permissions[key] = {
          canView: true,
          canCreate: true,
          canUpdate: true,
          canDelete: true
        }
      } else {
        permissions[key].canView = true
        permissions[key].canCreate = true
        permissions[key].canUpdate = true
        permissions[key].canDelete = true
      }
    })
    
    roleForm.value.permissions = permissions
  }
})

// 获取角色列表
const fetchRoles = async () => {
  try {
    console.log('RolePermission: 开始获取角色列表...')
    console.log('当前组件路径:', window.location.pathname)
    console.log('是否有权限:', useAuthStore().isAuthenticated)
    console.log('用户角色:', useAuthStore().user?.roles)
    
    const response = await getRoles()
    console.log('RolePermission: 获取角色列表响应:', response)
    
    // 详细检查返回数据
    if (!response) {
      console.error('RolePermission: 返回数据为空')
      throw new Error('返回数据为空')
    }
    
    if (!Array.isArray(response)) {
      console.error('RolePermission: 返回数据不是数组，实际类型:', typeof response)
      throw new Error('返回数据格式错误，不是数组')
    }
    
    console.log('RolePermission: 角色列表长度:', response.length)
    roles.value = response.length > 0 ? response : []
    
    if (roles.value.length === 0) {
      console.warn('RolePermission: 角色列表为空，使用模拟数据')
      // 设置模拟数据
      roles.value = [
        {
          id: 1,
          name: 'ROLE_ADMIN',
          description: '系统管理员，拥有所有权限',
          permissions: {
            dashboard: { canView: true, canCreate: true, canUpdate: true, canDelete: true },
            pets: { canView: true, canCreate: true, canUpdate: true, canDelete: true },
            'nfc-tags': { canView: true, canCreate: true, canUpdate: true, canDelete: true },
            services: { canView: true, canCreate: true, canUpdate: true, canDelete: true },
            orders: { canView: true, canCreate: true, canUpdate: true, canDelete: true },
            'base-data': { canView: true, canCreate: true, canUpdate: true, canDelete: true },
            'health-records': { canView: true, canCreate: true, canUpdate: true, canDelete: true },
            organization: { canView: true, canCreate: true, canUpdate: true, canDelete: true },
            roles: { canView: true, canCreate: true, canUpdate: true, canDelete: true },
            users: { canView: true, canCreate: true, canUpdate: true, canDelete: true }
          }
        }
      ]
    }
  } catch (error) {
    console.error('RolePermission: 获取角色列表失败，详细错误:', error)
    ElMessage.error('获取角色列表失败，请检查控制台')
    
    // 无论如何都设置模拟数据以确保页面能正常显示
    roles.value = [
      {
        id: 1,
        name: 'ROLE_ADMIN',
        description: '系统管理员，拥有所有权限',
        permissions: {
          dashboard: { canView: true, canCreate: true, canUpdate: true, canDelete: true },
          pets: { canView: true, canCreate: true, canUpdate: true, canDelete: true },
          'nfc-tags': { canView: true, canCreate: true, canUpdate: true, canDelete: true },
          services: { canView: true, canCreate: true, canUpdate: true, canDelete: true },
          orders: { canView: true, canCreate: true, canUpdate: true, canDelete: true },
          'base-data': { canView: true, canCreate: true, canUpdate: true, canDelete: true },
          'health-records': { canView: true, canCreate: true, canUpdate: true, canDelete: true },
          organization: { canView: true, canCreate: true, canUpdate: true, canDelete: true },
          roles: { canView: true, canCreate: true, canUpdate: true, canDelete: true },
          users: { canView: true, canCreate: true, canUpdate: true, canDelete: true }
        }
      }
    ]
  }
}

// 处理新增角色
const handleCreateRole = () => {
  dialogTitle.value = '新增角色'
  editingRole.value = null
  roleForm.value = {
    name: '',
    description: '',
    permissions: {}
  }
  dialogVisible.value = true
}

// 处理编辑角色
const handleEditRole = (role: Role) => {
  dialogTitle.value = '编辑角色'
  editingRole.value = role
  roleForm.value = {
    ...role
  }
  dialogVisible.value = true
}

// 处理删除角色
const handleDeleteRole = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteRole(id)
    ElMessage.success('角色删除成功')
    await fetchRoles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('角色删除失败')
    }
  }
}

// 处理提交表单
const handleSubmit = async () => {
  try {
    await roleFormRef.value?.validate()
    
    if (editingRole.value) {
      await updateRole(editingRole.value.id, roleForm.value as Omit<Role, 'id'>)
      ElMessage.success('角色更新成功')
    } else {
      await createRole(roleForm.value as Omit<Role, 'id'>)
      ElMessage.success('角色创建成功')
    }
    
    dialogVisible.value = false
    await fetchRoles()
  } catch (error) {
    ElMessage.error('表单验证失败')
  }
}

// 处理关闭弹窗
const handleCloseDialog = () => {
  roleFormRef.value?.resetFields()
}

// 组件挂载时获取角色列表
onMounted(() => {
  console.log('RolePermission组件挂载')
  fetchRoles()
})
</script>

<style scoped>
.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}
.role-permission-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
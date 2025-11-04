<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>基础数据管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增基础数据
          </el-button>
        </div>
      </template>

      <!-- 筛选区域 -->
      <div class="filter-container">
        <el-form :inline="true">
          <el-form-item label="数据类型">
            <el-select v-model="filterType" placeholder="全部类型" clearable @change="handleFilter">
              <el-option label="宠物品种" value="PET_BREED" />
              <el-option label="服务类型" value="SERVICE_TYPE" />
              <el-option label="健康记录类型" value="HEALTH_RECORD_TYPE" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="handleResetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="type" label="数据类型" width="200">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'PET_BREED'" type="primary">宠物品种</el-tag>
            <el-tag v-else-if="row.type === 'SERVICE_TYPE'" type="success">服务类型</el-tag>
            <el-tag v-else-if="row.type === 'HEALTH_RECORD_TYPE'" type="warning">健康记录类型</el-tag>
            <el-tag v-else type="info">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="value" label="数据值" width="200" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="数据类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择数据类型">
            <el-option label="宠物品种" value="PET_BREED" />
            <el-option label="服务类型" value="SERVICE_TYPE" />
            <el-option label="健康记录类型" value="HEALTH_RECORD_TYPE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据值" prop="value">
          <el-input v-model="form.value" placeholder="请输入数据值" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getAllBaseData, getBaseDataByType, createBaseData, updateBaseData, deleteBaseData } from '@/api/baseData'
import type { BaseData } from '@/types'

const loading = ref(false)
const tableData = ref<BaseData[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const filterType = ref('')

const form = reactive<BaseData>({
  type: '',
  value: '',
  description: ''
})

const rules = reactive<FormRules>({
  type: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
  value: [{ required: true, message: '请输入数据值', trigger: 'blur' }]
})

const fetchData = async () => {
  loading.value = true
  try {
    tableData.value = await getAllBaseData()
  } catch (error) {
    console.error('Fetch base data error:', error)
  } finally {
    loading.value = false
  }
}

const handleFilter = async () => {
  if (!filterType.value) {
    fetchData()
    return
  }
  loading.value = true
  try {
    tableData.value = await getBaseDataByType(filterType.value)
  } catch (error) {
    console.error('Filter base data error:', error)
  } finally {
    loading.value = false
  }
}

const handleResetFilter = () => {
  filterType.value = ''
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增基础数据'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: BaseData) => {
  dialogTitle.value = '编辑基础数据'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row: BaseData) => {
  ElMessageBox.confirm('确定要删除该基础数据吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteBaseData(row.id!)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('Delete base data error:', error)
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
          await updateBaseData(form.id, form)
          ElMessage.success('更新成功')
        } else {
          await createBaseData(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error('Submit base data error:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  resetForm()
}

const resetForm = () => {
  Object.assign(form, {
    id: undefined,
    type: '',
    value: '',
    description: ''
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

.filter-container {
  margin-bottom: 20px;
}
</style>


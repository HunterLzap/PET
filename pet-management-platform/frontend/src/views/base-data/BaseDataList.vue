<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>基础数据管理（只读）</span>
        </div>
      </template>

      <el-alert
        title="提示：此页面用于查看基础数据。如需修改，请联系平台超级管理员。"
        type="info"
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <el-select 
        v-model="selectedDict" 
        placeholder="请选择要查看的字典类型" 
        @change="fetchData"
        style="margin-bottom: 20px; width: 300px;"
      >
        <el-option
          v-for="dict in dictTypes"
          :key="dict.value"
          :label="dict.label"
          :value="dict.value"
        />
      </el-select>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="valueCode" label="编码" width="200" />
        <el-table-column prop="valueName" label="名称" width="200" />
        <el-table-column prop="extraData" label="附加属性" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDictValues } from '@/api/baseDict'

const loading = ref(false)
const tableData = ref([])
const selectedDict = ref('pet_species')

// 可供查看的字典类型列表
const dictTypes = ref([
  { label: '宠物种类', value: 'pet_species' },
  { label: '宠物品种', value: 'pet_breed' },
  { label: '宠物性别', value: 'pet_gender' },
  { label: '宠物体型', value: 'pet_size' },
  { label: '医疗服务', value: 'medical_service' },
  { label: '寄养服务', value: 'foster_service' },
  { label: '美容服务', value: 'beauty_service' },
  { label: '训练服务', value: 'training_service' },
  { label: '订单状态', value: 'order_status' },
  { label: '支付状态', value: 'payment_status' },
  { label: '商家类型', value: 'merchant_type' },
  { label: '疫苗类型', value: 'vaccine_type' },
  { label: '单位类型', value: 'unit_type' },
])

const fetchData = async () => {
  if (!selectedDict.value) return
  loading.value = true
  try {
    tableData.value = await getDictValues(selectedDict.value)
  } catch (error) {
    ElMessage.error('获取基础数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData() // 默认加载宠物种类
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
</style>
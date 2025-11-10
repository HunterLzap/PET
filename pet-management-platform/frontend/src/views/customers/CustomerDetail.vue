<template>
  <div class="page-container">
    <el-card class="customer-info-card">
      <template #header>
        <div class="card-header">
          <span>客户信息</span>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </template>
      
      <el-descriptions :column="2" border v-loading="customerLoading">
        <el-descriptions-item label="客户姓名">{{ customer.name }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ customer.phone }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ customer.gender || '-' }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ customer.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="历史订单数">{{ customer.totalOrders || 0 }}</el-descriptions-item>
        <el-descriptions-item label="消费总额">¥{{ (customer.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="最后到店时间" :span="2">
          {{ customer.lastVisitAt ? formatDateTime(customer.lastVisitAt) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ customer.notes || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="pet-list-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>客户的宠物列表</span>
          <el-button type="primary" @click="handleAddPet">
            <el-icon><Plus /></el-icon>
            为客户添加宠物
          </el-button>
        </div>
      </template>

      <el-table :data="pets" v-loading="petsLoading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="宠物名称" width="150" />
        <el-table-column prop="speciesName" label="种类" width="100" />
        <el-table-column prop="breedName" label="品种" width="150" />
        <el-table-column prop="genderName" label="性别" width="80" />
        <el-table-column prop="ageInMonths" label="年龄" width="100">
          <template #default="{ row }">
            <span v-if="row.ageInMonths">{{ Math.floor(row.ageInMonths / 12) }}岁{{ row.ageInMonths % 12 }}个月</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="体重(kg)" width="100" />
        <el-table-column prop="color" label="毛色" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewPet(row)">查看</el-button>
            <el-button size="small" type="danger" @click="handleDeletePet(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加宠物对话框 -->
    <el-dialog
      v-model="petDialogVisible"
      title="为客户添加宠物"
      width="700px"
      @close="handlePetDialogClose"
    >
      <el-form :model="petForm" :rules="petRules" ref="petFormRef" label-width="100px">
        <el-form-item label="宠物名称" prop="name">
          <el-input v-model="petForm.name" placeholder="请输入宠物名称" />
        </el-form-item>
        <el-form-item label="宠物种类" prop="speciesCode">
          <el-select v-model="petForm.speciesCode" placeholder="请选择种类" @change="handleSpeciesChange">
            <el-option label="犬" value="dog" />
            <el-option label="猫" value="cat" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="宠物品种" prop="breedCode">
          <el-select 
            v-model="petForm.breedCode" 
            placeholder="请选择品种" 
            filterable
            :disabled="!petForm.speciesCode"
          >
            <el-option
              v-for="breed in breeds"
              :key="breed.valueCode"
              :label="breed.valueName"
              :value="breed.valueCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="性别" prop="genderCode">
          <el-select v-model="petForm.genderCode" placeholder="请选择性别">
            <el-option label="公" value="male" />
            <el-option label="母" value="female" />
            <el-option label="未知" value="unknown" />
          </el-select>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthDate">
          <el-date-picker
            v-model="petForm.birthDate"
            type="date"
            placeholder="请选择出生日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="体重(kg)" prop="weight">
          <el-input-number v-model="petForm.weight" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="毛色" prop="color">
          <el-input v-model="petForm.color" placeholder="请输入毛色" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="petForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入宠物描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="petDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePetSubmit" :loading="petSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getCustomerById, getCustomerPets } from '@/api/customer'
import { addPet, deletePet } from '@/api/pet'
import { getDictValues } from '@/api/baseDict'
import type { Customer } from '@/api/customer'

const route = useRoute()
const router = useRouter()
const customerId = Number(route.params.id)

const customerLoading = ref(false)
const petsLoading = ref(false)
const customer = ref<Customer>({} as Customer)
const pets = ref<any[]>([])
const petDialogVisible = ref(false)
const petFormRef = ref<FormInstance>()
const petSubmitLoading = ref(false)
const breeds = ref<any[]>([])

const petForm = reactive({
  customerId: customerId,
  name: '',
  speciesCode: '',
  breedCode: '',
  genderCode: '',
  birthDate: '',
  weight: 0,
  color: '',
  description: ''
})

const petRules = reactive<FormRules>({
  name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  speciesCode: [{ required: true, message: '请选择宠物种类', trigger: 'change' }],
  breedCode: [{ required: true, message: '请选择宠物品种', trigger: 'change' }],
  genderCode: [{ required: true, message: '请选择性别', trigger: 'change' }]
})

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const fetchCustomer = async () => {
  customerLoading.value = true
  try {
    customer.value = await getCustomerById(customerId)
  } catch (error) {
    console.error('Fetch customer error:', error)
    ElMessage.error('获取客户信息失败')
  } finally {
    customerLoading.value = false
  }
}

const fetchPets = async () => {
  petsLoading.value = true
  try {
    pets.value = await getCustomerPets(customerId)
  } catch (error) {
    console.error('Fetch pets error:', error)
    ElMessage.error('获取宠物列表失败')
  } finally {
    petsLoading.value = false
  }
}

const handleSpeciesChange = async () => {
  petForm.breedCode = ''
  if (!petForm.speciesCode) return
  
  try {
    breeds.value = await getDictValues('pet_breed', petForm.speciesCode)
  } catch (error) {
    console.error('Fetch breeds error:', error)
  }
}

const handleAddPet = () => {
  resetPetForm()
  petDialogVisible.value = true
}

const handleViewPet = (row: any) => {
  ElMessage.info('查看宠物详情功能待开发')
}

const handleDeletePet = (row: any) => {
  ElMessageBox.confirm(`确定要删除宠物"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePet(row.id)
      ElMessage.success('删除成功')
      fetchPets()
    } catch (error) {
      console.error('Delete pet error:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handlePetSubmit = async () => {
  if (!petFormRef.value) return
  
  await petFormRef.value.validate(async (valid) => {
    if (valid) {
      petSubmitLoading.value = true
      try {
        await addPet(petForm)
        ElMessage.success('添加宠物成功')
        petDialogVisible.value = false
        fetchPets()
      } catch (error: any) {
        console.error('Add pet error:', error)
        const errorMsg = error.response?.data?.message || '添加失败'
        ElMessage.error(errorMsg)
      } finally {
        petSubmitLoading.value = false
      }
    }
  })
}

const handlePetDialogClose = () => {
  petFormRef.value?.resetFields()
  resetPetForm()
}

const resetPetForm = () => {
  Object.assign(petForm, {
    customerId: customerId,
    name: '',
    speciesCode: '',
    breedCode: '',
    genderCode: '',
    birthDate: '',
    weight: 0,
    color: '',
    description: ''
  })
  breeds.value = []
}

onMounted(() => {
  fetchCustomer()
  fetchPets()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pet-list-card {
  margin-top: 20px;
}
</style>
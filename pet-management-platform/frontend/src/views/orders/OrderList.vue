<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <!-- ✅ 管理员不显示新增按钮 -->
          <div class="header-actions" v-if="!isAdmin">
            <el-button @click="handleScanNfc">
              <el-icon><Postcard /></el-icon>
              NFC扫码
            </el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增订单
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="订单ID" width="100" />
        <el-table-column prop="serviceId" label="服务ID" width="100" />
        <el-table-column prop="petId" label="宠物ID" width="100" />
        <el-table-column prop="totalAmount" label="总金额(元)" width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PENDING'" type="warning">待确认</el-tag>
            <el-tag v-else-if="row.status === 'CONFIRMED'" type="primary">已确认</el-tag>
            <el-tag v-else-if="row.status === 'COMPLETED'" type="success">已完成</el-tag>
            <el-tag v-else-if="row.status === 'CANCELLED'" type="danger">已取消</el-tag>
            <el-tag v-else>{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.paymentStatus === 'PENDING'" type="warning">待支付</el-tag>
            <el-tag v-else-if="row.paymentStatus === 'PAID'" type="success">已支付</el-tag>
            <el-tag v-else-if="row.paymentStatus === 'REFUNDED'" type="info">已退款</el-tag>
            <el-tag v-else>{{ row.paymentStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="appointmentTime" label="预约时间" width="180" />
        <el-table-column prop="remarks" label="备注" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right" v-if="!isAdmin">
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
        <!-- ✅ 商家需要先选择客户 -->
        <el-form-item label="选择客户" prop="customerId" v-if="isMerchant">
          <el-select 
            v-model="form.customerId" 
            placeholder="请选择客户" 
            filterable
            @change="handleCustomerChange"
          >
            <el-option
              v-for="customer in customers"
              :key="customer.id"
              :label="`${customer.name} (${customer.phone})`"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择服务" prop="serviceId">
          <el-select v-model="form.serviceId" placeholder="请选择服务" filterable>
            <el-option
              v-for="service in services"
              :key="service.id"
              :label="`${service.name} (¥${service.price})`"
              :value="service.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择宠物" prop="petId">
          <el-select 
            v-model="form.petId" 
            placeholder="请选择宠物" 
            filterable
            :disabled="isMerchant && !form.customerId"
          >
            <el-option
              v-for="pet in availablePets"
              :key="pet.id"
              :label="`${pet.name} (${pet.speciesName})`"
              :value="pet.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="总金额(元)" prop="totalAmount">
          <el-input-number v-model="form.totalAmount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="订单状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择订单状态">
            <el-option label="待确认" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态" prop="paymentStatus">
          <el-select v-model="form.paymentStatus" placeholder="请选择支付状态">
            <el-option label="待支付" value="PENDING" />
            <el-option label="已支付" value="PAID" />
            <el-option label="已退款" value="REFUNDED" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约时间" prop="appointmentTime">
          <el-date-picker
            v-model="form.appointmentTime"
            type="datetime"
            placeholder="请选择预约时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="form.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- NFC扫码对话框 -->
    <el-dialog v-model="nfcDialogVisible" title="NFC扫码识别宠物" width="500px">
      <el-form label-width="120px">
        <el-form-item label="Tag UID">
          <el-input v-model="nfcTagUid" placeholder="请输入或扫描NFC Tag UID">
            <template #append>
              <el-button @click="handleNfcScan" :loading="nfcScanLoading">扫描</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="识别结果" v-if="scannedPet">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="宠物ID">{{ scannedPet.id }}</el-descriptions-item>
            <el-descriptions-item label="宠物名称">{{ scannedPet.name }}</el-descriptions-item>
            <el-descriptions-item label="物种">{{ scannedPet.species }}</el-descriptions-item>
          </el-descriptions>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nfcDialogVisible = false">关闭</el-button>
        <el-button 
          type="primary" 
          @click="handleUseScannedPet" 
          :disabled="!scannedPet"
        >
          使用该宠物创建订单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getOrders, createOrder, updateOrder, deleteOrder, scanNfcForOrder } from '@/api/order'
import { getServices } from '@/api/service'
import { getPets } from '@/api/pet'
import { getCustomers, getCustomerPets } from '@/api/customer'
import type { Order, Service, Pet, Customer } from '@/types'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const tableData = ref<Order[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const nfcDialogVisible = ref(false)
const nfcScanLoading = ref(false)
const nfcTagUid = ref('')
const scannedPet = ref<Pet | null>(null)
const services = ref<Service[]>([])
const pets = ref<Pet[]>([])
const customers = ref<Customer[]>([])
const customerPets = ref<Pet[]>([])

const form = reactive<Order>({
  userId: authStore.user?.id || 0,
  businessId: authStore.user?.id || 0,
  customerId: undefined,
  serviceId: 0,
  petId: undefined,
  totalAmount: 0,
  status: 'PENDING',
  paymentStatus: 'PENDING',
  appointmentTime: '',
  remarks: ''
})

const rules = reactive<FormRules>({
  serviceId: [{ required: true, message: '请选择服务', trigger: 'change' }],
  totalAmount: [{ required: true, message: '请输入总金额', trigger: 'blur' }],
  status: [{ required: true, message: '请选择订单状态', trigger: 'change' }],
  paymentStatus: [{ required: true, message: '请选择支付状态', trigger: 'change' }]
})

// ✅ 判断是否是管理员
const isAdmin = computed(() => {
  return authStore.user?.roles?.includes('ROLE_ADMIN') || false
})

// ✅ 判断是否是商家
const isMerchant = computed(() => {
  const roles = authStore.user?.roles || []
  return roles.some(role => 
    role === 'ROLE_MERCHANT_HOSPITAL' ||
    role === 'ROLE_MERCHANT_HOUSE' ||
    role === 'ROLE_MERCHANT_GOODS'
  )
})

// ✅ 可选的宠物列表（根据选择的客户动态变化）
const availablePets = computed(() => {
  if (isMerchant.value && form.customerId) {
    return customerPets.value
  }
  return pets.value
})

const fetchData = async () => {
  loading.value = true
  try {
    tableData.value = await getOrders()
  } catch (error) {
    console.error('Fetch orders error:', error)
  } finally {
    loading.value = false
  }
}

const fetchServices = async () => {
  try {
    services.value = await getServices()
  } catch (error) {
    console.error('Fetch services error:', error)
  }
}

const fetchPets = async () => {
  try {
    pets.value = await getPets()
  } catch (error) {
    console.error('Fetch pets error:', error)
  }
}

const fetchCustomers = async () => {
  try {
    customers.value = await getCustomers()
  } catch (error) {
    console.error('Fetch customers error:', error)
  }
}

// ✅ 客户选择变化时，加载该客户的宠物
const handleCustomerChange = async () => {
  form.petId = undefined
  customerPets.value = []
  
  if (!form.customerId) return
  
  try {
    customerPets.value = await getCustomerPets(form.customerId)
  } catch (error) {
    console.error('Fetch customer pets error:', error)
  }
}

const handleAdd = async () => {
  dialogTitle.value = '新增订单'
  resetForm()
  await fetchServices()
  
  if (isMerchant.value) {
    // 商家：加载客户列表
    await fetchCustomers()
  } else {
    // 用户：加载自己的宠物
    await fetchPets()
  }
  
  dialogVisible.value = true
}

const handleEdit = async (row: Order) => {
  dialogTitle.value = '编辑订单'
  Object.assign(form, row)
  await fetchServices()
  await fetchPets()
  dialogVisible.value = true
}

const handleDelete = (row: Order) => {
  ElMessageBox.confirm('确定要删除该订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteOrder(row.id!)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('Delete order error:', error)
    }
  }).catch(() => {})
}

const handleScanNfc = () => {
  nfcTagUid.value = ''
  scannedPet.value = null
  nfcDialogVisible.value = true
}

const handleNfcScan = async () => {
  if (!nfcTagUid.value) {
    ElMessage.warning('请输入Tag UID')
    return
  }
  nfcScanLoading.value = true
  try {
    const result = await scanNfcForOrder(nfcTagUid.value)
    scannedPet.value = result as any
    ElMessage.success('扫描成功')
  } catch (error) {
    console.error('Scan NFC error:', error)
    scannedPet.value = null
  } finally {
    nfcScanLoading.value = false
  }
}

const handleUseScannedPet = async () => {
  if (!scannedPet.value) return
  nfcDialogVisible.value = false
  await handleAdd()
  form.petId = scannedPet.value.id
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (form.id) {
          await updateOrder(form.id, form)
          ElMessage.success('更新成功')
        } else {
          await createOrder(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error('Submit order error:', error)
        ElMessage.error('操作失败')
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
    userId: authStore.user?.id || 0,
    businessId: authStore.user?.id || 0,
    customerId: undefined,
    serviceId: 0,
    petId: undefined,
    totalAmount: 0,
    status: 'PENDING',
    paymentStatus: 'PENDING',
    appointmentTime: '',
    remarks: ''
  })
  customerPets.value = []
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

.header-actions {
  display: flex;
  gap: 10px;
}
</style>
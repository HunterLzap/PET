<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>健康记录管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增健康记录
          </el-button>
        </div>
      </template>

      <!-- 筛选区域 -->
      <div class="filter-container">
        <el-form :inline="true">
          <el-form-item label="选择宠物">
            <el-select v-model="filterPetId" placeholder="全部宠物" clearable @change="handleFilter" filterable>
              <el-option
                v-for="pet in pets"
                :key="pet.id"
                :label="`${pet.name} (${pet.species})`"
                :value="pet.id"
              />
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
        <el-table-column prop="petId" label="宠物ID" width="100" />
        <el-table-column prop="recordDate" label="记录日期" width="120" />
        <el-table-column prop="recordType" label="记录类型" width="150">
          <template #default="{ row }">
            <el-tag v-if="row.recordType === '疫苗接种'" type="success">疫苗接种</el-tag>
            <el-tag v-else-if="row.recordType === '体检'" type="primary">体检</el-tag>
            <el-tag v-else-if="row.recordType === '治疗'" type="warning">治疗</el-tag>
            <el-tag v-else-if="row.recordType === '手术'" type="danger">手术</el-tag>
            <el-tag v-else type="info">{{ row.recordType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="notes" label="备注" show-overflow-tooltip />
        <el-table-column prop="attachmentUrl" label="附件" width="100">
          <template #default="{ row }">
            <el-link v-if="row.attachmentUrl" :href="row.attachmentUrl" target="_blank" type="primary">
              查看
            </el-link>
            <span v-else>-</span>
          </template>
        </el-table-column>
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
        <el-form-item label="选择宠物" prop="petId">
          <el-select v-model="form.petId" placeholder="请选择宠物" filterable>
            <el-option
              v-for="pet in pets"
              :key="pet.id"
              :label="`${pet.name} (${pet.species})`"
              :value="pet.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="记录日期" prop="recordDate">
          <el-date-picker
            v-model="form.recordDate"
            type="date"
            placeholder="请选择记录日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="记录类型" prop="recordType">
          <el-select v-model="form.recordType" placeholder="请选择记录类型">
            <el-option label="疫苗接种" value="疫苗接种" />
            <el-option label="体检" value="体检" />
            <el-option label="治疗" value="治疗" />
            <el-option label="手术" value="手术" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="备注" prop="notes">
          <el-input
            v-model="form.notes"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
          />
        </el-form-item>
        <el-form-item label="附件URL" prop="attachmentUrl">
          <el-input v-model="form.attachmentUrl" placeholder="请输入附件URL" />
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
import { getHealthRecordsByPetId, createHealthRecord, updateHealthRecord, deleteHealthRecord } from '@/api/healthRecord'
import { getPets } from '@/api/pet'
import type { HealthRecord, Pet } from '@/types'

const loading = ref(false)
const tableData = ref<HealthRecord[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const filterPetId = ref<number | undefined>(undefined)
const pets = ref<Pet[]>([])

const form = reactive<HealthRecord>({
  petId: 0,
  recordDate: '',
  recordType: '',
  description: '',
  notes: '',
  attachmentUrl: ''
})

const rules = reactive<FormRules>({
  petId: [{ required: true, message: '请选择宠物', trigger: 'change' }],
  recordDate: [{ required: true, message: '请选择记录日期', trigger: 'change' }],
  recordType: [{ required: true, message: '请选择记录类型', trigger: 'change' }]
})

const fetchPets = async () => {
  try {
    pets.value = await getPets()
  } catch (error) {
    console.error('Fetch pets error:', error)
  }
}

const fetchData = async () => {
  // 默认获取第一个宠物的健康记录
  if (pets.value.length > 0 && !filterPetId.value) {
    filterPetId.value = pets.value[0].id
  }
  
  if (!filterPetId.value) {
    ElMessage.warning('请先选择宠物')
    return
  }

  loading.value = true
  try {
    tableData.value = await getHealthRecordsByPetId(filterPetId.value)
  } catch (error) {
    console.error('Fetch health records error:', error)
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  if (!filterPetId.value) {
    ElMessage.warning('请选择宠物')
    return
  }
  fetchData()
}

const handleResetFilter = () => {
  filterPetId.value = undefined
  tableData.value = []
}

const handleAdd = () => {
  dialogTitle.value = '新增健康记录'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: HealthRecord) => {
  dialogTitle.value = '编辑健康记录'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row: HealthRecord) => {
  ElMessageBox.confirm('确定要删除该健康记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteHealthRecord(row.id!)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('Delete health record error:', error)
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
          await updateHealthRecord(form.id, form)
          ElMessage.success('更新成功')
        } else {
          await createHealthRecord(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error('Submit health record error:', error)
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
    petId: filterPetId.value || 0,
    recordDate: '',
    recordType: '',
    description: '',
    notes: '',
    attachmentUrl: ''
  })
}

onMounted(async () => {
  await fetchPets()
  if (pets.value.length > 0) {
    fetchData()
  }
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


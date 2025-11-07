<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>宠物管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增宠物
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="宠物名称" width="120" />
        <el-table-column prop="speciesName" label="种类" width="100" />
        <el-table-column prop="breedName" label="品种" width="120" />
        <el-table-column prop="genderName" label="性别" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.genderCode === 'male'" type="primary">{{ row.genderName }}</el-tag>
            <el-tag v-else-if="row.genderCode === 'female'" type="danger">{{ row.genderName }}</el-tag>
            <el-tag v-else type="info">{{ row.genderName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="birthDate" label="生日" width="120" />
        <el-table-column label="年龄" width="100">
          <template #default="{ row }">
            {{ row.ageInMonths ? Math.floor(row.ageInMonths / 12) + '岁' + (row.ageInMonths % 12) + '个月' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="体重(kg)" width="100" />
        <el-table-column prop="color" label="颜色" width="100" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
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
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="宠物名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入宠物名称" />
        </el-form-item>
        
        <el-form-item label="宠物种类" prop="speciesCode">
          <el-select v-model="form.speciesCode" placeholder="请选择宠物种类" @change="handleSpeciesChange">
            <el-option
              v-for="item in speciesList"
              :key="item.valueCode"
              :label="item.valueName"
              :value="item.valueCode"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="宠物品种" prop="breedCode">
          <el-select v-model="form.breedCode" placeholder="请先选择宠物种类" :disabled="!form.speciesCode">
            <el-option
              v-for="item in breedList"
              :key="item.valueCode"
              :label="item.valueName"
              :value="item.valueCode"
            >
              <span>{{ item.valueName }}</span>
              <span style="color: #8492a6; font-size: 12px; margin-left: 10px;">
                {{ getBreedOrigin(item) }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="性别" prop="genderCode">
          <el-radio-group v-model="form.genderCode">
            <el-radio v-for="item in genderList" :key="item.valueCode" :label="item.valueCode">
              {{ item.valueName }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="生日" prop="birthDate">
          <el-date-picker
            v-model="form.birthDate"
            type="date"
            placeholder="请选择生日"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item label="体重(kg)" prop="weight">
          <el-input-number v-model="form.weight" :min="0" :precision="2" />
        </el-form-item>
        
        <el-form-item label="颜色" prop="color">
          <el-input v-model="form.color" placeholder="请输入颜色" />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
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
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getMyPets, addPet, updatePet, deletePet } from '@/api/pet'
import { getDictValues, getDictValuesBySpecies } from '@/api/baseDict'
import type { Pet, AddPetRequest, DictValue } from '@/types'

const loading = ref(false)
const tableData = ref<Pet[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const speciesList = ref<DictValue[]>([])
const breedList = ref<DictValue[]>([])
const genderList = ref<DictValue[]>([])

const form = reactive<AddPetRequest>({
  name: '',
  speciesCode: '',
  breedCode: '',
  genderCode: '',
  birthDate: '',
  weight: 0,
  color: '',
  description: ''
})

const editingPetId = ref<number | undefined>()

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  speciesCode: [{ required: true, message: '请选择宠物种类', trigger: 'change' }],
  breedCode: [{ required: true, message: '请选择宠物品种', trigger: 'change' }],
  genderCode: [{ required: true, message: '请选择性别', trigger: 'change' }]
})

const getBreedOrigin = (item: DictValue) => {
  if (!item.extraData) return ''
  try {
    const data = JSON.parse(item.extraData)
    return data.origin || ''
  } catch {
    return ''
  }
}

const loadBaseData = async () => {
  try {
    speciesList.value = await getDictValues('pet_species') as DictValue[]
    genderList.value = await getDictValues('pet_gender') as DictValue[]
  } catch (error) {
    ElMessage.error('加载基础数据失败')
  }
}

const handleSpeciesChange = async (speciesCode: string) => {
  form.breedCode = ''
  breedList.value = []
  if (!speciesCode) return
  try {
    breedList.value = await getDictValuesBySpecies('pet_breed', speciesCode) as DictValue[]
  } catch (error) {
    ElMessage.error('加载品种数据失败')
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    tableData.value = await getMyPets()
  } catch (error) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增宠物'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row: Pet) => {
  dialogTitle.value = '编辑宠物'
  editingPetId.value = row.id
  if (row.speciesCode) {
    form.speciesCode = row.speciesCode
    await handleSpeciesChange(row.speciesCode)
  }
  Object.assign(form, {
    name: row.name,
    breedCode: row.breedCode,
    genderCode: row.genderCode,
    birthDate: row.birthDate || '',
    weight: row.weight || 0,
    color: row.color || '',
    description: row.description || ''
  })
  dialogVisible.value = true
}

const handleDelete = (row: Pet) => {
  ElMessageBox.confirm('确定要删除该宠物吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePet(row.id!)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      // 错误已由拦截器处理
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (editingPetId.value) {
          await updatePet(editingPetId.value, form)
          ElMessage.success('更新成功')
        } else {
          await addPet(form)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        // 错误已由拦截器处理
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
  editingPetId.value = undefined
  Object.assign(form, {
    name: '',
    speciesCode: '',
    breedCode: '',
    genderCode: '',
    birthDate: '',
    weight: 0,
    color: '',
    description: ''
  })
  breedList.value = []
}

onMounted(() => {
  loadBaseData()
  fetchData()
})
</script>

<style scoped>
.page-container { height: 100%; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
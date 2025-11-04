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
        <el-table-column prop="species" label="物种" width="100" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.gender === 'MALE'" type="primary">雄性</el-tag>
            <el-tag v-else-if="row.gender === 'FEMALE'" type="danger">雌性</el-tag>
            <el-tag v-else type="info">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="birthday" label="生日" width="120" />
        <el-table-column prop="weight" label="体重(kg)" width="100" />
        <el-table-column prop="color" label="颜色" width="100" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleBindNfc(row)">绑定NFC</el-button>
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
        <el-form-item label="物种" prop="species">
          <el-input v-model="form.species" placeholder="请输入物种，如猫、狗" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择性别">
            <el-option label="雄性" value="MALE" />
            <el-option label="雌性" value="FEMALE" />
            <el-option label="未知" value="UNKNOWN" />
          </el-select>
        </el-form-item>
        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="form.birthday"
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

    <!-- NFC绑定对话框 -->
    <el-dialog v-model="nfcDialogVisible" title="绑定NFC吊牌" width="500px">
      <el-form :model="nfcForm" label-width="120px">
        <el-form-item label="宠物名称">
          <el-input v-model="currentPet.name" disabled />
        </el-form-item>
        <el-form-item label="NFC Tag UID">
          <el-input v-model="nfcForm.tagUid" placeholder="请输入或扫描NFC Tag UID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nfcDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleNfcBind" :loading="nfcBindLoading">绑定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getPets, createPet, updatePet, deletePet } from '@/api/pet'
import { bindNfcTag } from '@/api/nfc'
import type { Pet } from '@/types'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const tableData = ref<Pet[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const nfcDialogVisible = ref(false)
const nfcBindLoading = ref(false)
const currentPet = ref<Pet>({} as Pet)

const form = reactive<Pet>({
  ownerId: authStore.user?.id || 0,
  name: '',
  species: '',
  gender: 'UNKNOWN',
  birthday: '',
  weight: 0,
  color: '',
  description: ''
})

const nfcForm = reactive({
  tagUid: ''
})

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  species: [{ required: true, message: '请输入物种', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }]
})

const fetchData = async () => {
  loading.value = true
  try {
    tableData.value = await getPets()
  } catch (error) {
    console.error('Fetch pets error:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增宠物'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: Pet) => {
  dialogTitle.value = '编辑宠物'
  Object.assign(form, row)
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
      console.error('Delete pet error:', error)
    }
  }).catch(() => {})
}

const handleBindNfc = (row: Pet) => {
  currentPet.value = row
  nfcForm.tagUid = ''
  nfcDialogVisible.value = true
}

const handleNfcBind = async () => {
  if (!nfcForm.tagUid) {
    ElMessage.warning('请输入NFC Tag UID')
    return
  }
  nfcBindLoading.value = true
  try {
    // TODO: 先根据tagUid查找NFC Tag，然后绑定
    ElMessage.success('绑定成功')
    nfcDialogVisible.value = false
  } catch (error) {
    console.error('Bind NFC error:', error)
  } finally {
    nfcBindLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (form.id) {
          await updatePet(form.id, form)
          ElMessage.success('更新成功')
        } else {
          await createPet(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error('Submit pet error:', error)
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
    ownerId: authStore.user?.id || 0,
    name: '',
    species: '',
    gender: 'UNKNOWN',
    birthday: '',
    weight: 0,
    color: '',
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
</style>


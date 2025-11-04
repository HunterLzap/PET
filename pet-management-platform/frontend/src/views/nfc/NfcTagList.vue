<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>NFC吊牌管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增NFC吊牌
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="tagUid" label="Tag UID" width="200" />
        <el-table-column prop="protocolType" label="协议类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.protocolType === 'ISO14443A'" type="success">ISO14443A</el-tag>
            <el-tag v-else-if="row.protocolType === 'ISO14443B'" type="warning">ISO14443B</el-tag>
            <el-tag v-else-if="row.protocolType === 'ISO15693'" type="info">ISO15693</el-tag>
            <el-tag v-else>{{ row.protocolType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'ACTIVE'" type="success">激活</el-tag>
            <el-tag v-else-if="row.status === 'INACTIVE'" type="info">未激活</el-tag>
            <el-tag v-else-if="row.status === 'LOST'" type="danger">丢失</el-tag>
            <el-tag v-else-if="row.status === 'BOUND'" type="primary">已绑定</el-tag>
            <el-tag v-else-if="row.status === 'UNBOUND'" type="warning">未绑定</el-tag>
            <el-tag v-else>{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="petId" label="绑定宠物ID" width="120" />
        <el-table-column prop="activatedAt" label="激活时间" width="180" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              size="small" 
              type="primary" 
              @click="handleBind(row)"
              :disabled="row.petId"
            >
              绑定宠物
            </el-button>
            <el-button 
              size="small" 
              type="warning" 
              @click="handleUnbind(row)"
              :disabled="!row.petId"
            >
              解绑
            </el-button>
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
        <el-form-item label="Tag UID" prop="tagUid">
          <el-input v-model="form.tagUid" placeholder="请输入Tag UID" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="协议类型" prop="protocolType">
          <el-select v-model="form.protocolType" placeholder="请选择协议类型">
            <el-option label="ISO14443A" value="ISO14443A" />
            <el-option label="ISO14443B" value="ISO14443B" />
            <el-option label="ISO15693" value="ISO15693" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="激活" value="ACTIVE" />
            <el-option label="未激活" value="INACTIVE" />
            <el-option label="丢失" value="LOST" />
            <el-option label="已绑定" value="BOUND" />
            <el-option label="未绑定" value="UNBOUND" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 绑定宠物对话框 -->
    <el-dialog v-model="bindDialogVisible" title="绑定宠物" width="600px">
      <el-form :model="bindForm" label-width="120px">
        <el-form-item label="Tag UID">
          <el-input v-model="currentTag.tagUid" disabled />
        </el-form-item>
        <el-form-item label="选择宠物">
          <el-select v-model="bindForm.petId" placeholder="请选择宠物" filterable>
            <el-option
              v-for="pet in pets"
              :key="pet.id"
              :label="`${pet.name} (${pet.species})`"
              :value="pet.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBindSubmit" :loading="bindLoading">绑定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getNfcTags, createNfcTag, bindNfcTag, unbindNfcTag } from '@/api/nfc'
import { getPets } from '@/api/pet'
import type { NfcTag, Pet } from '@/types'

const loading = ref(false)
const tableData = ref<NfcTag[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const bindDialogVisible = ref(false)
const bindLoading = ref(false)
const currentTag = ref<NfcTag>({} as NfcTag)
const pets = ref<Pet[]>([])

const form = reactive<NfcTag>({
  tagUid: '',
  protocolType: 'ISO14443A',
  status: 'INACTIVE'
})

const bindForm = reactive({
  petId: undefined as number | undefined
})

const rules = reactive<FormRules>({
  tagUid: [{ required: true, message: '请输入Tag UID', trigger: 'blur' }],
  protocolType: [{ required: true, message: '请选择协议类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
})

const fetchData = async () => {
  loading.value = true
  try {
    tableData.value = await getNfcTags()
  } catch (error) {
    console.error('Fetch NFC tags error:', error)
  } finally {
    loading.value = false
  }
}

const fetchPets = async () => {
  try {
    pets.value = await getPets()
  } catch (error) {
    console.error('Fetch pets error:', error)
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增NFC吊牌'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: NfcTag) => {
  dialogTitle.value = '编辑NFC吊牌'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row: NfcTag) => {
  ElMessageBox.confirm('确定要删除该NFC吊牌吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // TODO: 调用删除API
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('Delete NFC tag error:', error)
    }
  }).catch(() => {})
}

const handleBind = async (row: NfcTag) => {
  currentTag.value = row
  bindForm.petId = undefined
  await fetchPets()
  bindDialogVisible.value = true
}

const handleUnbind = (row: NfcTag) => {
  ElMessageBox.confirm('确定要解绑该NFC吊牌吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await unbindNfcTag(row.id!)
      ElMessage.success('解绑成功')
      fetchData()
    } catch (error) {
      console.error('Unbind NFC tag error:', error)
    }
  }).catch(() => {})
}

const handleBindSubmit = async () => {
  if (!bindForm.petId) {
    ElMessage.warning('请选择宠物')
    return
  }
  bindLoading.value = true
  try {
    await bindNfcTag(currentTag.value.id!, bindForm.petId)
    ElMessage.success('绑定成功')
    bindDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('Bind NFC tag error:', error)
  } finally {
    bindLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (form.id) {
          // TODO: 调用更新API
          ElMessage.success('更新成功')
        } else {
          await createNfcTag(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error('Submit NFC tag error:', error)
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
    tagUid: '',
    protocolType: 'ISO14443A',
    status: 'INACTIVE',
    petId: undefined
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


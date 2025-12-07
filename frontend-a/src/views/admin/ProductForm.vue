<template>
  <div class="product-form">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑商品' : '创建商品' }}</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        v-loading="loading"
        style="max-width: 700px"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" maxlength="100" />
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          />
        </el-form-item>

        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" />
          <span style="margin-left: 8px">元</span>
        </el-form-item>

        <el-form-item label="库存数量" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :max="99999" />
          <span style="margin-left: 8px">件</span>
        </el-form-item>

        <el-form-item label="商品图片" prop="coverImage">
          <ImageUpload v-model="form.coverImage" :limit="1" tip="建议尺寸 400x400，支持 jpg/png 格式" />
        </el-form-item>

        <el-form-item label="上架状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="上架"
            inactive-text="下架"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '保存修改' : '创建商品' }}
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import { adminProductApi, type AdminProduct } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)

const isEdit = computed(() => !!route.params.id)
const productId = computed(() => Number(route.params.id))

const form = reactive({
  name: '',
  description: '',
  price: 0,
  stock: 0,
  coverImage: '',
  status: 1
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }],
  coverImage: [{ required: true, message: '请上传商品图片', trigger: 'change' }]
}

// 加载商品详情
const loadDetail = async () => {
  if (!isEdit.value) return
  loading.value = true
  try {
    const data = await adminProductApi.getDetail(productId.value)
    form.name = data.name
    form.description = data.description
    form.price = data.price
    form.stock = data.stock
    form.coverImage = data.coverImage
    form.status = data.status
  } catch (e) {
    console.error(e)
    ElMessage.error('加载商品信息失败')
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const submitData: AdminProduct = {
    name: form.name,
    description: form.description,
    price: form.price,
    stock: form.stock,
    coverImage: form.coverImage,
    status: form.status
  }

  if (isEdit.value) {
    submitData.id = productId.value
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await adminProductApi.update(submitData)
      ElMessage.success('修改成功')
    } else {
      await adminProductApi.create(submitData)
      ElMessage.success('创建成功')
    }
    router.push('/admin/product')
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

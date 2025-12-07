<template>
  <div class="exhibition-form">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑展览' : '创建展览' }}</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        v-loading="loading"
        style="max-width: 800px"
      >
        <el-form-item label="展览名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入展览名称" maxlength="100" />
        </el-form-item>

        <el-form-item label="短描述" prop="shortDesc">
          <el-input v-model="form.shortDesc" placeholder="请输入短描述（列表页展示）" maxlength="200" />
        </el-form-item>

        <el-form-item label="详细介绍" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请输入详细介绍"
          />
        </el-form-item>

        <el-form-item label="展览时间" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="票价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" />
          <span style="margin-left: 8px">元</span>
        </el-form-item>

        <el-form-item label="封面图片" prop="coverImage">
          <ImageUpload v-model="form.coverImage" :limit="1" tip="建议尺寸 750x400，支持 jpg/png 格式" />
        </el-form-item>

        <el-form-item label="详情图片">
          <ImageUpload
            v-model="form.detailImages"
            :limit="9"
            :multiple="true"
            tip="最多上传9张，建议尺寸 750x自适应"
          />
        </el-form-item>

        <el-form-item label="标签">
          <el-select v-model="form.tags" multiple placeholder="选择标签" style="width: 100%">
            <el-option label="美团" value="美团" />
            <el-option label="抖音" value="抖音" />
            <el-option label="小红书" value="小红书" />
            <el-option label="热门" value="热门" />
            <el-option label="推荐" value="推荐" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">待开始</el-radio>
            <el-radio :value="1">进行中</el-radio>
            <el-radio :value="2">已结束</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '保存修改' : '创建展览' }}
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
import { adminExhibitionApi, type AdminExhibition } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)

const isEdit = computed(() => !!route.params.id)
const exhibitionId = computed(() => Number(route.params.id))

const form = reactive({
  name: '',
  shortDesc: '',
  description: '',
  dateRange: [] as string[],
  price: 0,
  coverImage: '',
  detailImages: [] as string[],
  tags: [] as string[],
  status: 0
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入展览名称', trigger: 'blur' }],
  shortDesc: [{ required: true, message: '请输入短描述', trigger: 'blur' }],
  description: [{ required: true, message: '请输入详细介绍', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择展览时间', trigger: 'change' }],
  price: [{ required: true, message: '请输入票价', trigger: 'blur' }],
  coverImage: [{ required: true, message: '请上传封面图片', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 加载展览详情
const loadDetail = async () => {
  if (!isEdit.value) return
  loading.value = true
  try {
    const data = await adminExhibitionApi.getDetail(exhibitionId.value)
    form.name = data.name
    form.shortDesc = data.shortDesc
    form.description = data.description
    form.dateRange = [data.startDate, data.endDate]
    form.price = data.price
    form.coverImage = data.coverImage
    form.detailImages = data.detailImages || []
    form.tags = data.tags || []
    form.status = data.status
  } catch (e) {
    console.error(e)
    ElMessage.error('加载展览信息失败')
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const submitData: AdminExhibition = {
    name: form.name,
    shortDesc: form.shortDesc,
    description: form.description,
    startDate: form.dateRange[0],
    endDate: form.dateRange[1],
    price: form.price,
    coverImage: form.coverImage,
    detailImages: form.detailImages,
    tags: form.tags,
    status: form.status
  }

  if (isEdit.value) {
    submitData.id = exhibitionId.value
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await adminExhibitionApi.update(submitData)
      ElMessage.success('修改成功')
    } else {
      await adminExhibitionApi.create(submitData)
      ElMessage.success('创建成功')
    }
    router.push('/admin/exhibition')
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

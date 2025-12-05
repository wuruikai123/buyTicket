<template>
  <div class="exhibition-form">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑展览' : '创建展览' }}</span>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="展览名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入展览名称" />
        </el-form-item>
        
        <el-form-item label="短描述" prop="shortDesc">
          <el-input
            v-model="form.shortDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入简短描述"
          />
        </el-form-item>
        
        <el-form-item label="详细介绍" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请输入详细介绍"
          />
        </el-form-item>
        
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            placeholder="选择结束日期"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="基础票价" prop="price">
          <el-input-number
            v-model="form.price"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="封面图片" prop="coverImage">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeUpload"
          >
            <img v-if="form.coverImage" :src="form.coverImage" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="详情图片">
          <el-upload
            action="#"
            list-type="picture-card"
            :file-list="detailImages"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="标签">
          <el-select
            v-model="form.tags"
            multiple
            placeholder="请选择标签"
            style="width: 100%"
          >
            <el-option label="美团" value="美团" />
            <el-option label="抖音" value="抖音" />
            <el-option label="热门" value="热门" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">待开始</el-radio>
            <el-radio :label="1">进行中</el-radio>
            <el-radio :label="2">已结束</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            保存
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
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { exhibitionApi } from '@/api/exhibition'

const route = useRoute()
const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)
const detailImages = ref<any[]>([])

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  name: '',
  shortDesc: '',
  description: '',
  startDate: '',
  endDate: '',
  price: 0,
  coverImage: '',
  tags: [] as string[],
  status: 0
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入展览名称', trigger: 'blur' }],
  shortDesc: [{ required: true, message: '请输入短描述', trigger: 'blur' }],
  description: [{ required: true, message: '请输入详细介绍', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  price: [{ required: true, message: '请输入基础票价', trigger: 'blur' }],
  coverImage: [{ required: true, message: '请上传封面图片', trigger: 'change' }]
}

const beforeUpload = (file: File) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    form.coverImage = e.target?.result as string
  }
  reader.readAsDataURL(file)
  return false
}

const handlePreview = () => {
  // 预览逻辑
}

const handleRemove = () => {
  // 移除逻辑
}

const loadData = async () => {
  if (!isEdit.value) return
  
  try {
    const data = await exhibitionApi.getDetail(Number(route.params.id))
    
    // 处理 tags: 字符串转数组
    let tagsArray: string[] = []
    if (data.tags) {
        tagsArray = typeof data.tags === 'string' ? data.tags.split(',') : data.tags
    }

    Object.assign(form, {
      ...data,
      startDate: data.startDate,
      endDate: data.endDate,
      tags: tagsArray
    })
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 转换 tags 数组为字符串，以匹配后端实体类类型
        const submitData = {
          ...form,
          tags: Array.isArray(form.tags) ? form.tags.join(',') : form.tags
        }

        if (isEdit.value) {
          await exhibitionApi.update({ id: Number(route.params.id), ...submitData })
        } else {
          await exhibitionApi.create(submitData)
        }
        ElMessage.success('保存成功')
        router.push('/exhibition/list')
      } catch (error) {
        console.error(error)
        ElMessage.error('保存失败')
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.exhibition-form {
  .avatar-uploader {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: all 0.3s;

      &:hover {
        border-color: #409eff;
      }
    }

    .avatar {
      width: 178px;
      height: 178px;
      display: block;
    }

    .avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 178px;
      height: 178px;
      text-align: center;
      line-height: 178px;
    }
  }
}
</style>


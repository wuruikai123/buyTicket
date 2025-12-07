<template>
  <div class="image-upload">
    <el-upload
      :class="{ 'hide-upload': hideUpload }"
      :action="uploadUrl"
      :headers="headers"
      :list-type="listType"
      :file-list="fileList"
      :limit="limit"
      :multiple="multiple"
      :accept="accept"
      :before-upload="handleBeforeUpload"
      :on-success="handleSuccess"
      :on-remove="handleRemove"
      :on-preview="handlePreview"
      :on-exceed="handleExceed"
    >
      <template v-if="listType === 'picture-card'">
        <el-icon><Plus /></el-icon>
      </template>
      <template v-else>
        <el-button type="primary">
          <el-icon><Upload /></el-icon>
          {{ buttonText }}
        </el-button>
      </template>
      <template #tip>
        <div class="el-upload__tip">{{ tip }}</div>
      </template>
    </el-upload>

    <!-- 图片预览 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="600px">
      <img :src="previewUrl" style="width: 100%" alt="preview" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Plus, Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { UploadFile, UploadRawFile } from 'element-plus'

interface Props {
  modelValue: string | string[]
  limit?: number
  multiple?: boolean
  listType?: 'text' | 'picture' | 'picture-card'
  accept?: string
  maxSize?: number // MB
  buttonText?: string
  tip?: string
}

const props = withDefaults(defineProps<Props>(), {
  limit: 1,
  multiple: false,
  listType: 'picture-card',
  accept: 'image/jpeg,image/png,image/gif,image/webp',
  maxSize: 5,
  buttonText: '上传图片',
  tip: '支持 jpg/png/gif/webp 格式，单个文件不超过 5MB'
})

const emit = defineEmits<{
  'update:modelValue': [value: string | string[]]
}>()

const uploadUrl = '/api/v1/admin/upload/image'
const headers = computed(() => ({
  Authorization: localStorage.getItem('token') || ''
}))

const fileList = ref<UploadFile[]>([])
const previewVisible = ref(false)
const previewUrl = ref('')

// 是否隐藏上传按钮
const hideUpload = computed(() => {
  if (props.multiple) {
    return fileList.value.length >= props.limit
  }
  return fileList.value.length >= 1
})

// 初始化文件列表
watch(
  () => props.modelValue,
  (val) => {
    if (!val) {
      fileList.value = []
      return
    }
    const urls = Array.isArray(val) ? val : [val]
    fileList.value = urls.filter(Boolean).map((url, index) => ({
      name: `image-${index}`,
      url,
      uid: Date.now() + index
    }))
  },
  { immediate: true }
)

// 上传前校验
const handleBeforeUpload = (file: UploadRawFile) => {
  const isValidType = props.accept.split(',').some(type => file.type === type.trim())
  if (!isValidType) {
    ElMessage.error('图片格式不支持')
    return false
  }
  const isValidSize = file.size / 1024 / 1024 < props.maxSize
  if (!isValidSize) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB`)
    return false
  }
  return true
}

// 上传成功
const handleSuccess = (response: any, file: UploadFile) => {
  if (response.code === 200) {
    file.url = response.data.url
    updateValue()
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
    // 移除失败的文件
    const index = fileList.value.findIndex(f => f.uid === file.uid)
    if (index > -1) fileList.value.splice(index, 1)
  }
}

// 移除文件
const handleRemove = () => {
  updateValue()
}

// 预览
const handlePreview = (file: UploadFile) => {
  previewUrl.value = file.url || ''
  previewVisible.value = true
}

// 超出限制
const handleExceed = () => {
  ElMessage.warning(`最多只能上传 ${props.limit} 张图片`)
}

// 更新值
const updateValue = () => {
  const urls = fileList.value.map(f => f.url).filter(Boolean) as string[]
  if (props.multiple) {
    emit('update:modelValue', urls)
  } else {
    emit('update:modelValue', urls[0] || '')
  }
}
</script>

<style scoped>
.image-upload :deep(.hide-upload .el-upload--picture-card) {
  display: none;
}
.el-upload__tip {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}
</style>

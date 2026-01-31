<template>
  <div class="about-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>关于展厅配置</span>
        </div>
      </template>

      <el-form :model="form" label-width="120px" style="max-width: 800px">
        <el-form-item label="标题" required>
          <el-input 
            v-model="form.title" 
            placeholder="请输入标题（不超过10个字）" 
            maxlength="10" 
            show-word-limit 
          />
        </el-form-item>

        <el-form-item label="正文内容" required>
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="8" 
            placeholder="请输入正文内容" 
          />
        </el-form-item>

        <el-form-item label="背景图片">
          <el-radio-group v-model="uploadMode.background" style="margin-bottom: 10px">
            <el-radio label="upload">上传图片</el-radio>
            <el-radio label="url">输入URL</el-radio>
          </el-radio-group>
          
          <!-- 上传模式 -->
          <el-upload
            v-if="uploadMode.background === 'upload'"
            class="image-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="(file) => handleImageUpload(file, 'background')"
          >
            <el-image 
              v-if="form.backgroundImage" 
              :src="form.backgroundImage" 
              fit="cover" 
              class="uploaded-image"
            />
            <div v-else class="upload-placeholder">
              <el-icon class="upload-icon"><Plus /></el-icon>
              <div class="upload-text">点击或拖拽上传背景图</div>
              <div class="upload-hint">建议尺寸：16:9</div>
            </div>
          </el-upload>
          
          <!-- URL模式 -->
          <el-input
            v-else
            v-model="form.backgroundImage"
            placeholder="请输入图片URL地址"
            clearable
          >
            <template #append>
              <el-button @click="previewImage(form.backgroundImage)">预览</el-button>
            </template>
          </el-input>
          
          <el-button 
            v-if="form.backgroundImage && uploadMode.background === 'upload'" 
            type="danger" 
            size="small" 
            @click="form.backgroundImage = ''"
            style="margin-top: 10px"
          >
            删除图片
          </el-button>
        </el-form-item>

        <el-form-item label="介绍图1">
          <el-radio-group v-model="uploadMode.intro1" style="margin-bottom: 10px">
            <el-radio label="upload">上传图片</el-radio>
            <el-radio label="url">输入URL</el-radio>
          </el-radio-group>
          
          <!-- 上传模式 -->
          <el-upload
            v-if="uploadMode.intro1 === 'upload'"
            class="image-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="(file) => handleImageUpload(file, 'intro1')"
          >
            <el-image 
              v-if="form.introImage1" 
              :src="form.introImage1" 
              fit="cover" 
              class="uploaded-image"
            />
            <div v-else class="upload-placeholder">
              <el-icon class="upload-icon"><Plus /></el-icon>
              <div class="upload-text">点击或拖拽上传介绍图1</div>
            </div>
          </el-upload>
          
          <!-- URL模式 -->
          <el-input
            v-else
            v-model="form.introImage1"
            placeholder="请输入图片URL地址"
            clearable
          >
            <template #append>
              <el-button @click="previewImage(form.introImage1)">预览</el-button>
            </template>
          </el-input>
          
          <el-button 
            v-if="form.introImage1 && uploadMode.intro1 === 'upload'" 
            type="danger" 
            size="small" 
            @click="form.introImage1 = ''"
            style="margin-top: 10px"
          >
            删除图片
          </el-button>
        </el-form-item>

        <el-form-item label="介绍图2">
          <el-radio-group v-model="uploadMode.intro2" style="margin-bottom: 10px">
            <el-radio label="upload">上传图片</el-radio>
            <el-radio label="url">输入URL</el-radio>
          </el-radio-group>
          
          <!-- 上传模式 -->
          <el-upload
            v-if="uploadMode.intro2 === 'upload'"
            class="image-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="(file) => handleImageUpload(file, 'intro2')"
          >
            <el-image 
              v-if="form.introImage2" 
              :src="form.introImage2" 
              fit="cover" 
              class="uploaded-image"
            />
            <div v-else class="upload-placeholder">
              <el-icon class="upload-icon"><Plus /></el-icon>
              <div class="upload-text">点击或拖拽上传介绍图2</div>
            </div>
          </el-upload>
          
          <!-- URL模式 -->
          <el-input
            v-else
            v-model="form.introImage2"
            placeholder="请输入图片URL地址"
            clearable
          >
            <template #append>
              <el-button @click="previewImage(form.introImage2)">预览</el-button>
            </template>
          </el-input>
          
          <el-button 
            v-if="form.introImage2 && uploadMode.intro2 === 'upload'" 
            type="danger" 
            size="small" 
            @click="form.introImage2 = ''"
            style="margin-top: 10px"
          >
            删除图片
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">保存配置</el-button>
          <el-button @click="handlePreview">预览效果</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface AboutConfig {
  id?: number
  title: string
  content: string
  backgroundImage: string
  introImage1: string
  introImage2: string
}

const loading = ref(false)
const form = ref<AboutConfig>({
  title: '关于展厅',
  content: '',
  backgroundImage: '',
  introImage1: '',
  introImage2: ''
})

// 上传模式：upload=上传图片, url=输入URL
const uploadMode = ref({
  background: 'url' as 'upload' | 'url',
  intro1: 'url' as 'upload' | 'url',
  intro2: 'url' as 'upload' | 'url'
})

const loadConfig = async () => {
  try {
    const data: any = await request.get('/admin/about/config')
    if (data) {
      form.value = data
    }
  } catch (error: any) {
    console.error('加载配置失败', error)
  }
}

// 处理图片上传（Base64方式）
const handleImageUpload = (file: File, type: 'background' | 'intro1' | 'intro2'): boolean => {
  const reader = new FileReader()
  reader.onload = (e) => {
    const base64 = e.target?.result as string
    if (type === 'background') {
      form.value.backgroundImage = base64
    } else if (type === 'intro1') {
      form.value.introImage1 = base64
    } else if (type === 'intro2') {
      form.value.introImage2 = base64
    }
    ElMessage.success('图片加载成功')
  }
  reader.onerror = () => {
    ElMessage.error('图片加载失败')
  }
  reader.readAsDataURL(file)
  return false // 阻止自动上传
}

const previewImage = (url: string) => {
  if (!url) {
    ElMessage.warning('请先输入图片URL')
    return
  }
  // 在新窗口打开图片
  window.open(url, '_blank')
}

const handleSubmit = async () => {
  if (!form.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!form.value.content) {
    ElMessage.warning('请输入正文内容')
    return
  }
  if (form.value.title.length > 10) {
    ElMessage.warning('标题不能超过10个字')
    return
  }

  loading.value = true
  try {
    await request.post('/admin/about/config', form.value)
    ElMessage.success('保存成功')
    loadConfig()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    loading.value = false
  }
}

const handlePreview = () => {
  // 在新窗口打开A端的关于展厅页面
  window.open('/about', '_blank')
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.about-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.image-uploader {
  width: 100%;
}

.image-uploader :deep(.el-upload) {
  width: 100%;
  max-width: 400px;
}

.image-uploader :deep(.el-upload-dragger) {
  width: 100%;
  height: 225px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.image-uploader :deep(.el-upload-dragger:hover) {
  border-color: #409eff;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  display: block;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.upload-icon {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
}
</style>

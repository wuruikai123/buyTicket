<template>
  <div class="basic-settings">
    <el-card>
      <template #header>
        <span>基础设置</span>
      </template>

      <el-form :model="form" label-width="150px" style="max-width: 600px">
        <el-form-item label="系统名称">
          <el-input v-model="form.systemName" placeholder="请输入系统名称" />
        </el-form-item>
        
        <el-form-item label="Logo">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeUpload"
          >
            <img v-if="form.logo" :src="form.logo" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="客服电话">
          <el-input v-model="form.servicePhone" placeholder="请输入客服电话" />
        </el-form-item>
        
        <el-form-item label="客服邮箱">
          <el-input v-model="form.serviceEmail" placeholder="请输入客服邮箱" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const form = reactive({
  systemName: '展览购票系统',
  logo: '',
  servicePhone: '400-123-4567',
  serviceEmail: 'service@example.com'
})

const beforeUpload = (file: File) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    form.logo = e.target?.result as string
  }
  reader.readAsDataURL(file)
  return false
}

const handleSave = () => {
  ElMessage.success('保存成功')
}

const handleReset = () => {
  form.systemName = '展览购票系统'
  form.logo = ''
  form.servicePhone = '400-123-4567'
  form.serviceEmail = 'service@example.com'
  ElMessage.info('已重置')
}
</script>

<style scoped lang="scss">
.basic-settings {
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
      width: 100px;
      height: 100px;
      display: block;
    }

    .avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 100px;
      height: 100px;
      text-align: center;
      line-height: 100px;
    }
  }
}
</style>


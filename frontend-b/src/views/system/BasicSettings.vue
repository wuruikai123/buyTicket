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
        
        <el-form-item label="每日开始时间">
          <el-time-picker
            v-model="form.dailyStartTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择每日开始时间"
            :clearable="false"
          />
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            买家只能购买此时间之后的票
          </div>
        </el-form-item>
        
        <el-form-item label="每日结束时间">
          <el-time-picker
            v-model="form.dailyEndTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择每日结束时间"
            :clearable="false"
          />
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            买家只能购买此时间之前的票
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="loading">保存</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const form = reactive({
  systemName: '展览购票系统',
  logo: '',
  servicePhone: '400-123-4567',
  serviceEmail: 'service@example.com',
  dailyStartTime: '09:00',  // 默认早上9点开始
  dailyEndTime: '17:00'     // 默认下午5点结束
})

const loading = ref(false)

// 加载配置
const loadConfig = async () => {
  try {
    const data: any = await request.get('/admin/system-config/all')
    if (data) {
      form.systemName = data.system_name || form.systemName
      form.servicePhone = data.service_phone || form.servicePhone
      form.serviceEmail = data.service_email || form.serviceEmail
      form.dailyStartTime = data.daily_start_time || form.dailyStartTime
      form.dailyEndTime = data.daily_end_time || form.dailyEndTime
    }
  } catch (error) {
    console.error('加载配置失败:', error)
  }
}

const beforeUpload = (file: File) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    form.logo = e.target?.result as string
  }
  reader.readAsDataURL(file)
  return false
}

const handleSave = async () => {
  loading.value = true
  try {
    const configMap = {
      system_name: form.systemName,
      service_phone: form.servicePhone,
      service_email: form.serviceEmail,
      daily_start_time: form.dailyStartTime,
      daily_end_time: form.dailyEndTime
    }
    await request.post('/admin/system-config/batch', configMap)
    ElMessage.success('保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  loadConfig()
  ElMessage.info('已重置')
}

onMounted(() => {
  loadConfig()
})
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


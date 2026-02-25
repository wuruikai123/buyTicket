<template>
  <div class="profile-settings">
    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <span>个人设置</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="settings-form"
      >
        <el-form-item label="当前账号">
          <el-input v-model="currentUsername" disabled />
        </el-form-item>

        <el-divider />

        <el-form-item label="新账号" prop="username">
          <el-input
            v-model="form.username"
            placeholder="留空则不修改账号"
            clearable
          />
        </el-form-item>

        <el-form-item label="新密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="留空则不修改密码"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            保存修改
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const authStore = useAuthStore()
const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)
const currentUsername = ref('')

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

// 验证确认密码
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value && value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { min: 3, max: 20, message: '账号长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 加载当前用户信息
const loadUserInfo = async () => {
  try {
    const res = await request.get('/admin/auth/info')
    if (res && res.username) {
      currentUsername.value = res.username
    }
  } catch (error: any) {
    console.error('加载用户信息失败:', error)
  }
}

// 提交修改
const handleSubmit = async () => {
  if (!formRef.value) return

  // 检查是否有修改
  if (!form.username && !form.password) {
    ElMessage.warning('请至少修改账号或密码')
    return
  }

  // 如果修改了密码，必须填写确认密码
  if (form.password && !form.confirmPassword) {
    ElMessage.warning('请输入确认密码')
    return
  }

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      await ElMessageBox.confirm(
        '修改账号或密码后需要重新登录，确定要继续吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )

      loading.value = true

      const updateData: any = {}
      if (form.username) {
        updateData.username = form.username
      }
      if (form.password) {
        updateData.password = form.password
      }

      await request.put('/admin/auth/profile', updateData)

      ElMessage.success('修改成功，请重新登录')

      // 延迟跳转到登录页
      setTimeout(() => {
        authStore.logout()
        router.push('/login')
      }, 1500)
    } catch (error: any) {
      if (error !== 'cancel') {
        loading.value = false
        ElMessage.error(error.message || '修改失败')
      }
    }
  })
}

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields()
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped lang="scss">
.profile-settings {
  max-width: 800px;
  margin: 0 auto;
}

.settings-card {
  .card-header {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }
}

.settings-form {
  max-width: 500px;
  margin: 20px 0;

  :deep(.el-form-item__label) {
    font-weight: 500;
  }

  :deep(.el-input) {
    width: 100%;
  }

  :deep(.el-button) {
    min-width: 100px;
  }
}

:deep(.el-divider) {
  margin: 24px 0;
}
</style>

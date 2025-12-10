<template>
  <div class="login-container">
    <div class="login-wrapper">
      <!-- 左侧欢迎区域 -->
      <div class="welcome-section">
        <h1 class="welcome-text">欢迎登陆XXX管理平台</h1>
      </div>

      <!-- 右侧登录表单区域 -->
      <div class="login-section">
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          label-position="top"
        >
          <el-form-item label="账号" prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入账号"
              size="large"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="default"
              size="large"
              :loading="loading"
              @click="handleLogin"
              class="login-button"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
        <div class="login-tip">
          <p>测试账号：admin / 密码：123456</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: '123456',
  remember: false
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await authStore.login(loginForm.username, loginForm.password)
        ElMessage.success('登录成功')
        router.push('/dashboard')
      } catch (error: any) {
        ElMessage.error(error.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  width: 100%;
  height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-wrapper {
  width: 100%;
  max-width: 1200px;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 40px;
}

.welcome-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-right: 60px;

  .welcome-text {
    font-size: 60px;
    font-weight: 500;
    color: #303133;
    margin: 0;
    line-height: 1.5;
  }
}

.login-section {
  flex: 1;
  max-width: 400px;
  padding: 40px;
  border-radius: 8px;
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }

  :deep(.el-form-item__label) {
    font-size: 16px;
    color: #303133;
    font-weight: 500;
    padding: 0;
    margin-bottom: 10px;
    line-height: 1.5;
    height: auto;
  }

  :deep(.el-input__wrapper) {
    padding: 0;
    height: 48px;
    border-radius: 4px;
    box-shadow: none !important;
    border: 1px solid #dcdfe6;
    background-color: #fff;

    &:hover {
      box-shadow: none !important;
      border-color: #c0c4cc;
    }

    &.is-focus {
      box-shadow: none !important;
      border-color: #409eff;
    }
  }

  :deep(.el-input__inner) {
    height: 48px;
    line-height: 48px;
    font-size: 16px;
    padding: 0 16px;
    border: none;
    background-color: transparent;

    &:focus {
      outline: none;
    }
  }

  .login-button {
    width: 100%;
    height: 48px;
    background-color: #e4e7ed;
    border-color: #dcdfe6;
    color: #303133;
    font-size: 16px;
    border-radius: 4px;
    margin-top: 8px;

    &:hover {
      background-color: #d3d4d6;
      border-color: #c0c4cc;
      color: #303133;
    }

    &:active {
      background-color: #c0c4cc;
      border-color: #a8abb2;
    }
  }
}

.login-tip {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;

  p {
    font-size: 12px;
    color: #909399;
    margin: 0;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
    padding: 20px;
  }

  .welcome-section {
    padding-right: 0;
    padding-bottom: 40px;
    flex: none;

    .welcome-text {
      font-size: 28px;
      text-align: center;
    }
  }

  .login-section {
    width: 100%;
    max-width: 100%;
  }
}
</style>


<template>
  <div class="login-container">
    <div class="login-wrapper">
      <!-- 左侧欢迎区域 -->
      <div class="welcome-section">
        <h1 class="welcome-text">欢迎登录元星河AI艺术馆售票系统管理平台</h1>
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
  username: '',
  password: '',
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  // 添加装饰性背景元素
  &::before {
    content: '';
    position: absolute;
    width: 500px;
    height: 500px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
    top: -200px;
    left: -200px;
  }

  &::after {
    content: '';
    position: absolute;
    width: 400px;
    height: 400px;
    background: rgba(255, 255, 255, 0.08);
    border-radius: 50%;
    bottom: -150px;
    right: -150px;
  }
}

.login-wrapper {
  width: 100%;
  max-width: 1200px;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 40px;
  position: relative;
  z-index: 1;
}

.welcome-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-right: 60px;

  .welcome-text {
    font-size: 48px;
    font-weight: 600;
    color: #ffffff;
    margin: 0;
    line-height: 1.4;
    text-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
    letter-spacing: 1px;
  }
}

.login-section {
  flex: 1;
  max-width: 420px;
  padding: 48px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 28px;
  }

  :deep(.el-form-item__label) {
    font-size: 15px;
    color: #4a5568 !important;
    font-weight: 600;
    padding: 0;
    margin-bottom: 12px;
    line-height: 1.5;
    height: auto;
  }

  :deep(.el-input__wrapper) {
    padding: 0;
    height: 52px;
    border-radius: 8px;
    box-shadow: none !important;
    border: 2px solid #e2e8f0;
    background-color: #ffffff;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: none !important;
      border-color: #cbd5e0;
    }

    &.is-focus {
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1) !important;
      border-color: #667eea;
    }
  }

  :deep(.el-input__inner) {
    height: 52px;
    line-height: 52px;
    font-size: 15px;
    padding: 0 18px;
    border: none;
    background-color: transparent;
    color: #2d3748;

    &::placeholder {
      color: #a0aec0;
    }

    &:focus {
      outline: none;
    }
  }

  .login-button {
    width: 100%;
    height: 56px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    color: #ffffff !important;
    font-size: 18px;
    font-weight: 700;
    letter-spacing: 4px;
    border-radius: 12px;
    margin-top: 20px;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.35);
    position: relative;
    overflow: hidden;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    // 按钮文字样式
    :deep(span) {
      position: relative;
      z-index: 1;
      display: inline-block;
      text-transform: uppercase;
      color: #ffffff !important;
    }

    // 添加光泽效果
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(
        90deg,
        transparent,
        rgba(255, 255, 255, 0.3),
        transparent
      );
      transition: left 0.5s;
    }

    &:hover {
      transform: translateY(-3px) scale(1.02);
      box-shadow: 0 12px 32px rgba(102, 126, 234, 0.5);
      background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
      letter-spacing: 5px;
      color: #ffffff !important;

      &::before {
        left: 100%;
      }
    }

    &:active {
      transform: translateY(-1px) scale(0.98);
      box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
    }

    // loading状态样式
    &.is-loading {
      pointer-events: none;
      opacity: 0.8;
    }
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
    padding: 32px 24px;
  }
}
</style>


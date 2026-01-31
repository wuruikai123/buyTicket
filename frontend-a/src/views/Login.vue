<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2 class="title">{{ isLogin ? '欢迎登录' : '注册账号' }}</h2>
        <p class="subtitle">{{ isLogin ? '登录以访问您的购票信息' : '创建新账号开启艺术之旅' }}</p>
      </div>
      
      <form @submit.prevent="handleSubmit" class="login-form">
        <div class="form-group">
          <label class="form-label">用户名</label>
          <input v-model="form.username" type="text" class="form-input" placeholder="请输入用户名" required />
        </div>
        
        <div class="form-group">
          <label class="form-label">密码</label>
          <input v-model="form.password" type="password" class="form-input" placeholder="请输入密码" required />
        </div>

        <div class="form-group" v-if="!isLogin">
          <label class="form-label">手机号</label>
          <input v-model="form.phone" type="tel" class="form-input" placeholder="请输入手机号" required />
        </div>
        
        <button type="submit" class="submit-btn">{{ isLogin ? '登 录' : '注 册' }}</button>
      </form>
      
      <div class="login-footer">
        <span class="footer-text">{{ isLogin ? '还没有账号？' : '已有账号？' }}</span>
        <span class="toggle-btn" @click="toggleMode">{{ isLogin ? '立即注册' : '立即登录' }}</span>
      </div>

      <!-- 为了测试方便 -->
      <div class="debug-info" v-if="isLogin">
        <p>测试账号: zhangsan / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const isLogin = ref(true)

const form = reactive({
  username: '',
  password: '',
  phone: ''
})

const toggleMode = () => {
  isLogin.value = !isLogin.value
  // 清空表单
  form.username = ''
  form.password = ''
  form.phone = ''
}

const handleSubmit = async () => {
  try {
    if (isLogin.value) {
        // 登录
        const res: any = await request.post('/user/login', {
            username: form.username,
            password: form.password
        })
        if (res && res.token) {
            console.log('Login Success, Token:', res.token)
            localStorage.setItem('token', res.token)
            // 保存用户信息
            if (res.user) {
                localStorage.setItem('userInfo', JSON.stringify(res.user))
            }
            // 简单的提示，实际可以用 ElMessage
            alert('登录成功')
            router.push('/')
        } else {
             console.error('Login Failed: No token in response', res)
        }
    } else {
        // 注册
        await request.post('/user/register', {
            username: form.username,
            password: form.password,
            phone: form.phone
        })
        alert('注册成功，请登录')
        isLogin.value = true
    }
  } catch (error: any) {
    alert(error.message || '操作失败')
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 360px;
  background: white;
  border-radius: 12px;
  padding: 32px 24px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.05);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0 0 8px;
}

.subtitle {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  color: #333;
  background-color: #f9f9f9;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #409eff;
  background-color: white;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background-color: #333;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s ease;
  margin-top: 12px;
}

.submit-btn:hover {
  background-color: #000;
}

.login-footer {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
}

.footer-text {
  color: #999;
}

.toggle-btn {
  color: #409eff;
  cursor: pointer;
  margin-left: 6px;
  font-weight: 500;
}

.toggle-btn:hover {
  text-decoration: underline;
}

.debug-info {
    margin-top: 20px;
    padding: 10px;
    background-color: #f0f0f0;
    border-radius: 4px;
    font-size: 12px;
    color: #666;
    text-align: center;
}
</style>

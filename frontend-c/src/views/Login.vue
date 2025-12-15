<template>
  <div class="page">
    <div class="title">核销端</div>

    <form class="form" @submit.prevent="handleLogin">
      <label class="label" for="account">账号</label>
      <input
        id="account"
        v-model="form.account"
        class="input"
        type="text"
        required
        autocomplete="username"
        :disabled="loading"
      />

      <label class="label" for="password">密码</label>
      <input
        id="password"
        v-model="form.password"
        class="input"
        type="password"
        required
        autocomplete="current-password"
        :disabled="loading"
      />

      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

      <button class="primary" type="submit" :disabled="loading">
        {{ loading ? '登录中...' : '登录' }}
      </button>
      
      <div class="tip">默认账号：seller / 密码：123456</div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { setToken } from '@/router'
import request from '@/utils/request'

const router = useRouter()
const form = reactive({
  account: 'seller',
  password: '123456'
})
const loading = ref(false)
const errorMsg = ref('')

async function handleLogin() {
  if (!form.account || !form.password) {
    errorMsg.value = '请输入账号和密码'
    return
  }
  
  loading.value = true
  errorMsg.value = ''
  
  try {
    // 调用后端登录接口
    const res = await request.post('/auth/login', {
      username: form.account,
      password: form.password
    })
    
    // 保存token
    if (res.token) {
      setToken(res.token)
      router.replace({ name: 'home' })
    } else {
      errorMsg.value = '登录失败：未返回token'
    }
  } catch (error: any) {
    console.error('登录失败:', error)
    errorMsg.value = error.message || '登录失败，请检查账号密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f4f4f4;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 24px;
}

.title {
  margin-top: 60px;
  font-size: 48px;
  font-weight: 500;
  color: #202020;
  letter-spacing: 2px;
}

.form {
  width: 100%;
  max-width: 520px;
  margin-top: 80px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.label {
  font-size: 20px;
  color: #505050;
}

.input {
  width: 100%;
  height: 56px;
  border: none;
  border-radius: 10px;
  padding: 0 14px;
  font-size: 20px;
  background: #ffffff;
  box-shadow: inset 0 0 0 1px #ededed;
}

.input:focus {
  outline: none;
  box-shadow: inset 0 0 0 2px #d0d0d0;
}

.primary {
  margin-top: 36px;
  width: 100%;
  height: 60px;
  border: none;
  border-radius: 12px;
  background: #d7d7d7;
  color: #2d2d2d;
  font-size: 22px;
  letter-spacing: 2px;
}

.primary:active {
  background: #cfcfcf;
}

.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-msg {
  padding: 12px;
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 8px;
  color: #c33;
  font-size: 16px;
  text-align: center;
}

.tip {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #909399;
}
</style>


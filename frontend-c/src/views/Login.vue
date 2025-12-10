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
      />

      <label class="label" for="password">密码</label>
      <input
        id="password"
        v-model="form.password"
        class="input"
        type="password"
        required
        autocomplete="current-password"
      />

      <button class="primary" type="submit">登录</button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { setToken } from '@/router'

const router = useRouter()
const form = reactive({
  account: '',
  password: ''
})

function handleLogin() {
  // 简易模拟登录，实际应调用后端接口
  setToken('mock-token')
  router.replace({ name: 'home' })
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
</style>


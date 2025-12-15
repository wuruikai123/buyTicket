<template>
  <div class="page">
    <header class="header">
      <div class="time-block">
        <div class="date">{{ currentDate }}</div>
        <div class="time">{{ currentTime }}</div>
      </div>
      <div class="count-block">
        <div class="count-text">今日已成功核销{{ todayCount }}单</div>
        <button class="outline" @click="goRecords">核销记录</button>
      </div>
    </header>

    <main class="main">
      <button class="card" @click="goScan">扫码核销</button>
      <button class="card" @click="goOrder">单号核销</button>
    </main>

    <footer class="footer">
      <button class="primary" @click="logout">退出登录</button>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { clearToken } from '@/router'
import { getTodayVerifiedCount } from '@/utils/orders'

const router = useRouter()
const todayCount = ref(0)
const currentDate = ref('')
const currentTime = ref('')
let timer: number | undefined

// 加载今日核销数量
async function loadTodayCount() {
  try {
    todayCount.value = await getTodayVerifiedCount()
  } catch (error) {
    console.error('获取今日核销数量失败:', error)
  }
}

function formatDateTime() {
  const now = new Date()
  const y = now.getFullYear()
  const m = now.getMonth() + 1
  const d = now.getDate()
  const hours = now.getHours().toString().padStart(2, '0')
  const minutes = now.getMinutes().toString().padStart(2, '0')
  currentDate.value = `${y}年${m}月${d}日`
  currentTime.value = `${hours}:${minutes}`
}

function goRecords() {
  router.push({ name: 'records' })
}

function goScan() {
  router.push({ name: 'scanVerify' })
}

function goOrder() {
  router.push({ name: 'order' })
}

function logout() {
  clearToken()
  router.replace({ name: 'login' })
}

onMounted(() => {
  formatDateTime()
  loadTodayCount()
  timer = window.setInterval(formatDateTime, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f4f4f4;
  display: flex;
  flex-direction: column;
  padding: 32px 20px 40px;
  gap: 28px;
}

.header {
  display: flex;
  gap: 22px;
  align-items: center;
}

.time-block,
.count-block {
  flex: 1;
  background: #f4f4f4;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.date {
  font-size: 22px;
  color: #2c2c2c;
}

.time {
  font-size: 52px;
  font-weight: 500;
  letter-spacing: 2px;
}

.count-text {
  font-size: 18px;
  color: #2c2c2c;
}

.outline {
  width: 168px;
  height: 52px;
  border: none;
  border-radius: 10px;
  background: #f6f6f6;
  box-shadow: inset 0 0 0 1px #e5e5e5;
  font-size: 18px;
}

.main {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 10px;
}

.card {
  width: 100%;
  height: 140px;
  border: none;
  border-radius: 18px;
  background: #f9f9f9;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.06);
  font-size: 34px;
  color: #202020;
}

.card:active {
  background: #f1f1f1;
}

.footer {
  margin-top: auto;
}

.primary {
  width: 100%;
  height: 64px;
  border: none;
  border-radius: 14px;
  background: #d7d7d7;
  color: #2d2d2d;
  font-size: 22px;
  letter-spacing: 2px;
}
</style>


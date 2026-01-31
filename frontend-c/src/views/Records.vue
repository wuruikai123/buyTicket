<template>
  <div class="page">
    <header class="topbar">
      <button class="back" aria-label="返回" @click="goBack">‹</button>
      <div class="spacer" />
    </header>

    <div class="filters">
      <select v-model.number="year">
        <option v-for="y in years" :key="y" :value="y">{{ y }}年</option>
      </select>
      <select v-model.number="month">
        <option v-for="m in months" :key="m" :value="m">{{ m }}月</option>
      </select>
      <select v-model.number="day">
        <option v-for="d in days" :key="d" :value="d">{{ d }}日</option>
      </select>
    </div>

    <div class="divider" />

    <div class="list">
      <div v-if="records.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <div class="empty-text">该日期暂无核销记录</div>
      </div>
      <div v-for="item in records" :key="item.id" class="card">
        <div class="line bold">{{ item.exhibition }}</div>
        <div class="line">有效时间：{{ item.validTime }}</div>
        <div class="line">购买账号：{{ item.buyer }}</div>
        <div class="line">核销时间：{{ item.verifyTime || '待核销' }}</div>
        <div class="line">订单号码：{{ item.orderNo }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listRecordsByDate } from '@/utils/orders'

const router = useRouter()
const now = new Date()
const years = [now.getFullYear() - 1, now.getFullYear(), now.getFullYear() + 1]
const months = Array.from({ length: 12 }, (_v, i) => i + 1)
const days = Array.from({ length: 31 }, (_v, i) => i + 1)

const year = ref(now.getFullYear())
const month = ref(now.getMonth() + 1)
const day = ref(now.getDate())
const records = ref<any[]>([])
const loading = ref(false)

// 加载记录
const loadRecords = async () => {
  loading.value = true
  try {
    const data = await listRecordsByDate(year.value, month.value, day.value)
    records.value = data
  } catch (error) {
    console.error('加载记录失败:', error)
    records.value = []
  } finally {
    loading.value = false
  }
}

// 监听日期变化
watch([year, month, day], () => {
  loadRecords()
})

// 初始加载
onMounted(() => {
  loadRecords()
})

function goBack() {
  router.back()
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f4f4f4;
  display: flex;
  flex-direction: column;
  padding: 18px 16px 28px;
  gap: 18px;
}

.topbar {
  display: flex;
  align-items: center;
  height: 40px;
}

.back {
  border: none;
  background: transparent;
  font-size: 32px;
  color: #8a8a8a;
  line-height: 1;
}

.spacer {
  flex: 1;
}

.filters {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 4px;
}

select {
  height: 50px;
  border-radius: 10px;
  border: none;
  padding: 0 12px;
  font-size: 18px;
  background: #ffffff;
  box-shadow: inset 0 0 0 1px #e5e5e5;
}

.divider {
  width: 100%;
  height: 2px;
  background: #e8e8e8;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  background: #fafafa;
  border-radius: 12px;
  padding: 16px 14px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.line {
  font-size: 18px;
  color: #202020;
}

.bold {
  font-weight: 600;
  letter-spacing: 1px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 16px;
}

.empty-icon {
  font-size: 64px;
  opacity: 0.3;
}

.empty-text {
  font-size: 16px;
  color: #999;
}
</style>




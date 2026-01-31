<template>
  <div class="page" @click="goHome">
    <header class="topbar">
      <button class="back" aria-label="返回" @click.stop="goBack">‹</button>
    </header>

    <main class="content">
      <div class="touch-area">
        <span>轻触继续</span>
      </div>

      <div class="info">
        <div class="title" :class="{ fail: isFail }">{{ isFail ? '核销失败' : '核销成功' }}</div>
        <template v-if="!isFail">
          <div class="text exhibition">{{ exhibition }}</div>
          <div class="text time">{{ time }}</div>
          <div class="text buyer">购买账号{{ buyer }}</div>
        </template>
        <template v-else>
          <div class="text fail-text">{{ message }}</div>
        </template>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const status = computed(() => (route.query.status as string) || 'success')
const exhibition = computed(() => (route.query.exhibition as string) || 'XXXXXXXXXXXXXXXXXXXXXX展')
const time = computed(() => (route.query.time as string) || '14:30-15:30')
const buyer = computed(() => (route.query.buyer as string) || '13777581964')
const message = computed(() => (route.query.message as string) || '不在时间段内')
const isFail = computed(() => status.value === 'fail')

function goBack() {
  router.back()
}

function goHome() {
  router.replace({ name: 'home' })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #2f2f2f;
  color: #f5f5f5;
  display: flex;
  flex-direction: column;
  padding: 18px 16px 32px;
}

.topbar {
  height: 44px;
  display: flex;
  align-items: center;
}

.back {
  border: none;
  background: transparent;
  font-size: 32px;
  color: #bdbdbd;
  line-height: 1;
}

.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 36px;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.touch-area {
  width: 86%;
  max-width: 620px;
  aspect-ratio: 1 / 1;
  background: #9a9a9a;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1f1f1f;
  font-size: 26px;
  user-select: none;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 14px;
  align-items: center;
}

.title {
  font-size: 32px;
  font-weight: 700;
}

.title.fail {
  color: #f1f1f1;
}

.text {
  font-size: 22px;
  color: #f5f5f5;
}

.exhibition {
  font-size: 24px;
  color: #ffffff;
  font-weight: 600;
}

.buyer {
  font-size: 22px;
}

.fail-text {
  font-size: 22px;
  color: #f5f5f5;
}
</style>


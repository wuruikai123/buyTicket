<template>
  <div class="page">
    <header class="topbar">
      <button class="back" aria-label="返回" @click="goBack">‹</button>
      <div class="title">输入订单号</div>
      <div class="placeholder" />
    </header>

    <main class="content">
      <input
        v-model.trim="orderNo"
        class="input"
        type="text"
        placeholder="请输入订单号"
        inputmode="numeric"
        autocomplete="one-time-code"
      />

      <div class="result">
        <div class="result-title">查询结果</div>
        <template v-if="status === 'found' && order">
          <div class="text">{{ order.exhibition }}</div>
          <div class="text">有效时间：{{ order.validTime }}</div>
          <div class="text">购买账号：{{ order.buyer }}</div>
          <div class="text" v-if="order.verifyTime">核销时间：{{ order.verifyTime }}</div>
          <div class="text">订单号码：{{ order.orderNo }}</div>
        </template>
        <template v-else-if="status === 'notfound'">
          <div class="text">未查询到</div>
        </template>
        <template v-else>
          <div class="text muted">请输入订单号后点击核销</div>
        </template>
      </div>
    </main>

    <footer class="footer">
      <button class="primary" :disabled="!orderNo" @click="handleVerify">核销</button>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { queryOrder, type OrderRecord } from '@/utils/orders'

const router = useRouter()
const orderNo = ref('')
const order = ref<OrderRecord | null>(null)
const status = ref<'idle' | 'found' | 'notfound'>('idle')

function goBack() {
  router.back()
}

function handleVerify() {
  const result = queryOrder(orderNo.value)
  if (result) {
    order.value = result
    status.value = 'found'
  } else {
    order.value = null
    status.value = 'notfound'
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f4f4f4;
  display: flex;
  flex-direction: column;
  padding: 20px 18px 32px;
  gap: 24px;
}

.topbar {
  display: grid;
  grid-template-columns: 40px 1fr 40px;
  align-items: center;
  height: 44px;
}

.back {
  border: none;
  background: transparent;
  font-size: 34px;
  color: #8a8a8a;
  line-height: 1;
}

.title {
  text-align: center;
  font-size: 28px;
  color: #202020;
}

.placeholder {
  width: 40px;
  height: 1px;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.input {
  width: 100%;
  height: 58px;
  border: none;
  border-radius: 10px;
  padding: 0 14px;
  font-size: 22px;
  background: #ffffff;
  box-shadow: inset 0 0 0 1px #ededed;
}

.input:focus {
  outline: none;
  box-shadow: inset 0 0 0 2px #d0d0d0;
}

.result {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
  margin-top: 10px;
}

.result-title {
  font-size: 22px;
  color: #202020;
}

.text {
  font-size: 20px;
  color: #202020;
}

.muted {
  color: #9c9c9c;
}

.footer {
  margin-top: auto;
}

.primary {
  width: 100%;
  height: 62px;
  border: none;
  border-radius: 12px;
  background: #d7d7d7;
  color: #2d2d2d;
  font-size: 22px;
  letter-spacing: 2px;
}

.primary:disabled {
  opacity: 0.6;
}
</style>


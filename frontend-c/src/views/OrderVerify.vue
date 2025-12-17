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
          <div class="text success">✓ 核销成功</div>
          <div class="text">{{ order.exhibition }}</div>
          <div class="text">有效时间：{{ order.validTime }}</div>
          <div class="text">购买账号：{{ order.buyer }}</div>
          <div class="text" v-if="order.verifyTime">核销时间：{{ order.verifyTime }}</div>
          <div class="text">订单号码：{{ order.orderNo }}</div>
        </template>
        <template v-else-if="status === 'verified' && order">
          <div class="text warning">⚠ 该订单已核销</div>
          <div class="text">{{ order.exhibition }}</div>
          <div class="text">有效时间：{{ order.validTime }}</div>
          <div class="text">购买账号：{{ order.buyer }}</div>
          <div class="text" v-if="order.verifyTime">核销时间：{{ order.verifyTime }}</div>
          <div class="text">订单号码：{{ order.orderNo }}</div>
        </template>
        <template v-else-if="status === 'notfound'">
          <div class="text error">✗ 未查询到订单</div>
        </template>
        <template v-else-if="status === 'error'">
          <div class="text error">✗ {{ errorMsg }}</div>
        </template>
        <template v-else>
          <div class="text muted">请输入订单号后点击核销</div>
        </template>
      </div>
    </main>

    <footer class="footer">
      <button class="primary" :disabled="!orderNo || loading" @click="handleVerify">
        {{ loading ? '核销中...' : '核销' }}
      </button>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { verifyOrder, type OrderRecord } from '@/utils/orders'

const router = useRouter()
const orderNo = ref('')
const order = ref<OrderRecord | null>(null)
const status = ref<'idle' | 'found' | 'notfound' | 'verified' | 'error'>('idle')
const loading = ref(false)
const errorMsg = ref('')

function goBack() {
  router.back()
}

async function handleVerify() {
  if (!orderNo.value || loading.value) return
  
  loading.value = true
  errorMsg.value = ''
  order.value = null
  status.value = 'idle'
  
  try {
    // 调用核销接口
    // 后端逻辑：查询订单 → 验证状态(必须是待使用) → 更新为已使用
    await verifyOrder(orderNo.value)
    
    // 核销成功
    order.value = {
      id: 0,
      orderNo: orderNo.value,
      exhibition: '门票订单',
      validTime: new Date().toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      }),
      buyer: '用户',
      status: 2
    }
    
    status.value = 'found'
    
    // 清空输入框，准备下一次核销
    setTimeout(() => {
      orderNo.value = ''
    }, 2000)
    
  } catch (error: any) {
    console.error('核销失败:', error)
    
    // 根据错误信息判断具体原因
    const errMsg = error.message || error.toString()
    
    if (errMsg.includes('订单不存在')) {
      status.value = 'notfound'
      errorMsg.value = '订单不存在，请检查订单号'
    } else if (errMsg.includes('已核销过了') || errMsg.includes('已核销')) {
      // 订单状态为2，已经核销过
      status.value = 'verified'
      order.value = {
        id: 0,
        orderNo: orderNo.value,
        exhibition: '门票订单',
        validTime: '已核销',
        buyer: '用户',
        status: 2
      }
      errorMsg.value = '该订单已核销过了'
    } else if (errMsg.includes('未支付')) {
      status.value = 'error'
      errorMsg.value = '订单未支付，无法核销'
    } else if (errMsg.includes('已取消')) {
      status.value = 'error'
      errorMsg.value = '订单已取消，无法核销'
    } else if (errMsg.includes('请输入订单号')) {
      status.value = 'error'
      errorMsg.value = '请输入订单号'
    } else {
      status.value = 'error'
      errorMsg.value = errMsg || '核销失败，请重试'
    }
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

.success {
  color: #52c41a;
  font-weight: 600;
}

.warning {
  color: #faad14;
  font-weight: 600;
}

.error {
  color: #f5222d;
  font-weight: 600;
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




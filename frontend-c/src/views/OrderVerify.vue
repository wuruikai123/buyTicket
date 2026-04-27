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

        <template v-if="status === 'ticket-select'">
          <div class="text exhibition-name">订单号：{{ selectedOrderNo }}</div>
          <div class="text">剩余待核销：{{ pendingItems.length }} 张</div>

          <div class="ticket-list" v-if="pendingItems.length">
            <button
              v-for="item in pendingItems"
              :key="item.id"
              class="ticket-item"
              :disabled="loading"
              @click="handleVerifySingle(item.id)"
            >
              <div class="ticket-row">子票ID：{{ item.id }}</div>
              <div class="ticket-row">姓名：{{ item.buyerName || '未填写' }}</div>
              <div class="ticket-row">
                场次：{{ item.ticketDate || '-' }} {{ item.timeSlot || '' }}
              </div>
              <div class="ticket-action">点击核销这 1 张</div>
            </button>
          </div>

          <div class="text muted" v-else>该订单暂无可核销门票</div>
        </template>

        <template v-else-if="status === 'found' && order">
          <div class="text success">✓ 核销成功</div>
          <div class="text exhibition-name">订单号：{{ order.orderNo }}</div>
          <div class="text">本次核销：1 张</div>
          <div class="text" v-if="order.verifyTime">最终核销时间：{{ order.verifyTime }}</div>
          <div class="text" v-if="remainingCount > 0">剩余待核销：{{ remainingCount }} 张</div>
          <div class="text muted" v-else>该订单已全部核销，状态将显示为已使用</div>
        </template>

        <template v-else-if="status === 'verified' && order">
          <div class="text warning">⚠ 该订单已全部核销</div>
          <div class="text exhibition-name">订单号：{{ order.orderNo }}</div>
          <div class="text" v-if="order.verifyTime">核销时间：{{ order.verifyTime }}</div>
        </template>

        <template v-else-if="status === 'notfound'">
          <div class="text error">✗ 未查询到订单</div>
        </template>

        <template v-else-if="status === 'error'">
          <div class="text error">✗ {{ errorMsg }}</div>
        </template>

        <template v-else>
          <div class="text muted">请输入订单号后点击查询</div>
        </template>
      </div>
    </main>

    <footer class="footer">
      <button class="primary" :disabled="!orderNo || loading" @click="handleQueryOrder">
        {{ loading ? '处理中...' : '查询待核销门票' }}
      </button>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  queryVerifyCode,
  verifyTicketItem,
  type OrderRecord,
  type PendingTicketItem
} from '@/utils/orders'

const router = useRouter()
const orderNo = ref('')
const selectedOrderNo = ref('')
const order = ref<OrderRecord | null>(null)
const pendingItems = ref<PendingTicketItem[]>([])
const remainingCount = ref(0)
const loading = ref(false)
const errorMsg = ref('')

const status = ref<'idle' | 'ticket-select' | 'found' | 'notfound' | 'verified' | 'error'>('idle')

function goBack() {
  router.back()
}

async function handleQueryOrder() {
  if (!orderNo.value || loading.value) return

  loading.value = true
  errorMsg.value = ''
  order.value = null
  status.value = 'idle'
  pendingItems.value = []
  remainingCount.value = 0
  selectedOrderNo.value = orderNo.value.trim()

  try {
    const data = await queryVerifyCode(selectedOrderNo.value)

    if (data.type === 'special') {
      errorMsg.value = '请使用扫码核销特殊票券'
      status.value = 'error'
      return
    }

    if ((data.waitingCount || 0) <= 0) {
      status.value = 'verified'
      order.value = {
        id: 0,
        orderNo: selectedOrderNo.value,
        exhibition: '门票订单',
        validTime: '已核销',
        buyer: data.contactName || '用户',
        verifyTime: data.verifyTime,
        status: data.status || 2
      }
      return
    }

    pendingItems.value = data.pendingItems || []
    status.value = 'ticket-select'
  } catch (error: any) {
    const errMsg = error.message || error.toString()

    if (errMsg.includes('订单不存在')) {
      status.value = 'notfound'
      errorMsg.value = '订单不存在，请检查订单号'
    } else {
      status.value = 'error'
      errorMsg.value = errMsg || '查询失败，请重试'
    }
  } finally {
    loading.value = false
  }
}

async function handleVerifySingle(ticketItemId: number) {
  if (!selectedOrderNo.value || loading.value) return

  loading.value = true
  errorMsg.value = ''

  try {
    await verifyTicketItem(selectedOrderNo.value, ticketItemId)

    const latest = await queryVerifyCode(selectedOrderNo.value)
    const waitingCount = latest.waitingCount || 0

    order.value = {
      id: 0,
      orderNo: selectedOrderNo.value,
      exhibition: '门票订单',
      validTime: new Date().toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      }),
      buyer: latest.contactName || '用户',
      verifyTime: latest.verifyTime,
      status: latest.status || (waitingCount > 0 ? 1 : 2)
    }

    remainingCount.value = waitingCount

    if (waitingCount > 0) {
      pendingItems.value = latest.pendingItems || []
      status.value = 'found'
    } else {
      pendingItems.value = []
      status.value = 'found'
      setTimeout(() => {
        orderNo.value = ''
        selectedOrderNo.value = ''
      }, 2000)
    }
  } catch (error: any) {
    const errMsg = error.message || error.toString()
    status.value = 'error'
    errorMsg.value = errMsg || '核销失败，请重试'
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

.exhibition-name {
  color: #000000;
  font-weight: 600;
}

.ticket-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 6px;
}

.ticket-item {
  width: 100%;
  border: 1px solid #e6e6e6;
  border-radius: 10px;
  background: #fff;
  text-align: left;
  padding: 12px;
}

.ticket-item:disabled {
  opacity: 0.65;
}

.ticket-row {
  font-size: 16px;
  color: #333;
  margin-bottom: 4px;
}

.ticket-action {
  margin-top: 8px;
  font-size: 14px;
  color: #1677ff;
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

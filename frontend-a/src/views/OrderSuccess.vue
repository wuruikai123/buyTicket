<template>
  <div class="order-success-page">
    <div class="success-container">
      <!-- 成功图标 -->
      <div class="success-icon">
        <svg viewBox="0 0 24 24" width="80" height="80">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" fill="#52c41a"/>
        </svg>
      </div>

      <!-- 状态信息 -->
      <div class="status-info">
        <h1 v-if="paymentStatus === 'success'" class="status-title success">支付成功</h1>
        <h1 v-else-if="paymentStatus === 'pending'" class="status-title pending">支付处理中</h1>
        <h1 v-else class="status-title failed">支付失败</h1>

        <p class="status-message">
          <span v-if="paymentStatus === 'success'">感谢您的支付，订单已成功创建</span>
          <span v-else-if="paymentStatus === 'pending'">您的支付正在处理中，请稍候...</span>
          <span v-else>支付未成功，请重试</span>
        </p>
      </div>

      <!-- 订单信息 -->
      <div class="order-details" v-if="orderInfo">
        <div class="detail-row">
          <span class="label">订单号：</span>
          <span class="value">{{ orderInfo.orderNo }}</span>
        </div>
        <div class="detail-row">
          <span class="label">订单金额：</span>
          <span class="value price">¥{{ orderInfo.totalAmount }}</span>
        </div>
        <div class="detail-row">
          <span class="label">支付时间：</span>
          <span class="value">{{ orderInfo.payTime || '处理中...' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">订单状态：</span>
          <span class="value" :class="statusClass">{{ statusText }}</span>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>正在查询订单状态...</p>
      </div>

      <!-- 错误信息 -->
      <div v-if="error" class="error-message">
        <p>{{ error }}</p>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <button class="btn btn-primary" @click="goToProfile">查看订单</button>
        <button class="btn btn-secondary" @click="goToHome">返回首页</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const orderNo = ref<string>('')
const orderType = ref<'ticket' | 'mall'>('ticket')
const orderInfo = ref<any>(null)
const paymentStatus = ref<'success' | 'pending' | 'failed'>('pending')
const loading = ref(true)
const error = ref<string>('')

const statusText = computed(() => {
  const statusMap: Record<number, string> = {
    0: '待支付',
    1: '已支付',
    2: '已发货',
    3: '已完成',
    4: '已取消',
    5: '已退款'
  }
  return statusMap[orderInfo.value?.status] || '未知状态'
})

const statusClass = computed(() => {
  if (orderInfo.value?.status === 1) return 'success'
  if (orderInfo.value?.status === 0) return 'pending'
  return 'failed'
})

// 查询订单信息
const queryOrderInfo = async () => {
  try {
    loading.value = true
    error.value = ''

    // 从URL参数获取订单号
    orderNo.value = (route.query.out_trade_no as string) || ''
    orderType.value = (route.query.type as 'ticket' | 'mall') || 'ticket'

    if (!orderNo.value) {
      error.value = '订单号不存在'
      paymentStatus.value = 'failed'
      return
    }

    // 根据订单类型查询订单信息
    let response
    if (orderType.value === 'ticket') {
      response = await axios.get(`/api/v1/order/ticket/${orderNo.value}`)
    } else {
      response = await axios.get(`/api/v1/order/mall/${orderNo.value}`)
    }

    if (response.data.code !== 200) {
      error.value = response.data.msg || '查询订单失败'
      paymentStatus.value = 'failed'
      return
    }

    const order = response.data.data
    if (!order) {
      error.value = '订单不存在'
      paymentStatus.value = 'failed'
      return
    }

    orderInfo.value = order

    // 根据订单状态判断支付状态
    if (order.status === 1) {
      // 1: 已支付
      paymentStatus.value = 'success'
    } else if (order.status === 0) {
      // 0: 待支付
      paymentStatus.value = 'pending'
    } else {
      paymentStatus.value = 'failed'
    }
  } catch (err: any) {
    error.value = err.message || '查询订单失败'
    paymentStatus.value = 'failed'
  } finally {
    loading.value = false
  }
}

// 返回个人中心
const goToProfile = () => {
  router.push('/profile')
}

// 返回首页
const goToHome = () => {
  router.push('/')
}

onMounted(() => {
  queryOrderInfo()
})
</script>

<style scoped>
.order-success-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.success-container {
  background: white;
  border-radius: 12px;
  padding: 40px;
  max-width: 500px;
  width: 100%;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.success-icon {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

.status-title {
  font-size: 28px;
  font-weight: bold;
  margin: 0 0 12px 0;

  &.success {
    color: #52c41a;
  }

  &.pending {
    color: #faad14;
  }

  &.failed {
    color: #f5222d;
  }
}

.status-message {
  font-size: 16px;
  color: #666;
  margin-bottom: 32px;
}

.order-details {
  background: #f5f5f5;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 32px;
  text-align: left;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #e0e0e0;

  &:last-child {
    border-bottom: none;
  }
}

.label {
  color: #999;
  font-size: 14px;
}

.value {
  color: #333;
  font-size: 14px;
  font-weight: 500;

  &.price {
    color: #ff4d4f;
    font-size: 16px;
    font-weight: bold;
  }

  &.success {
    color: #52c41a;
  }

  &.pending {
    color: #faad14;
  }

  &.failed {
    color: #f5222d;
  }
}

.loading {
  margin: 32px 0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.error-message {
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 24px;
  color: #ff4d4f;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.btn {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;

  &.btn-primary {
    background: #667eea;
    color: white;

    &:hover {
      background: #5568d3;
    }
  }

  &.btn-secondary {
    background: #f5f5f5;
    color: #333;

    &:hover {
      background: #e0e0e0;
    }
  }
}

@media (max-width: 768px) {
  .success-container {
    padding: 24px;
  }

  .status-title {
    font-size: 24px;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>

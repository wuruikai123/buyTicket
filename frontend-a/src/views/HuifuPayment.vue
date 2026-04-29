<template>
  <div class="huifu-payment-page">
    <div class="payment-container">
      <div class="payment-header">
        <h2>选择支付方式</h2>
        <p class="subtitle">安全快速的支付体验</p>
      </div>

      <div class="order-info" v-if="orderInfo">
        <div class="info-row">
          <span class="label">订单号：</span>
          <span class="value">{{ orderInfo.orderNo }}</span>
        </div>
        <div class="info-row">
          <span class="label">订单金额：</span>
          <span class="value price">¥{{ orderInfo.totalAmount }}</span>
        </div>
        <div class="info-row">
          <span class="label">订单类型：</span>
          <span class="value">{{ orderType === 'ticket' ? '门票订单' : '商城订单' }}</span>
        </div>
      </div>

      <div class="payment-methods">
        <h3>选择支付方式</h3>
        <div class="method-list">
          <!-- 微信支付（已迁移到微信原生JSAPI，不再走汇付） -->
          <div 
            class="method-item disabled" 
            :class="{ active: selectedMethod === 'WECHAT' }"
            @click="handleWechatDisabled"
          >
            <div class="method-icon wechat-icon">
              <svg viewBox="0 0 1024 1024" width="40" height="40">
                <path d="M512 0C229.376 0 0 229.376 0 512s229.376 512 512 512 512-229.376 512-512S794.624 0 512 0z m0 921.6c-225.792 0-409.6-183.808-409.6-409.6s183.808-409.6 409.6-409.6 409.6 183.808 409.6 409.6-183.808 409.6-409.6 409.6z" fill="#09B726"/>
                <path d="M512 204.8c-169.472 0-307.2 137.728-307.2 307.2s137.728 307.2 307.2 307.2 307.2-137.728 307.2-307.2-137.728-307.2-307.2-307.2z m0 563.2c-141.312 0-256-114.688-256-256s114.688-256 256-256 256 114.688 256 256-114.688 256-256 256z" fill="#09B726"/>
              </svg>
            </div>
            <div class="method-info">
              <div class="method-name">微信支付</div>
              <div class="method-desc">扫码支付，安全快速</div>
            </div>
            <div class="method-check">
              <span v-if="selectedMethod === 'WECHAT'" class="check-icon">✓</span>
            </div>
          </div>

          <!-- 支付宝支付 -->
          <div 
            class="method-item" 
            :class="{ active: selectedMethod === 'ALIPAY' }"
            @click="selectedMethod = 'ALIPAY'"
          >
            <div class="method-icon alipay-icon">
              <svg viewBox="0 0 1024 1024" width="40" height="40">
                <path d="M1024 701.9v162.5c0 88.4-71.6 160-160 160H160c-88.4 0-160-71.6-160-160V159.6C0 71.2 71.6 0 160 0h704c88.4 0 160 71.2 160 159.6v542.3z" fill="#00A0E9"/>
                <path d="M928 701.9V159.6c0-35.3-28.7-64-64-64H160c-35.3 0-64 28.7-64 64v704.8c0 35.3 28.7 64 64 64h704c35.3 0 64-28.7 64-64v-162.5z" fill="#FFFFFF"/>
              </svg>
            </div>
            <div class="method-info">
              <div class="method-name">支付宝支付</div>
              <div class="method-desc">账户支付，便捷可靠</div>
            </div>
            <div class="method-check">
              <span v-if="selectedMethod === 'ALIPAY'" class="check-icon">✓</span>
            </div>
          </div>
        </div>
      </div>

      <div class="payment-actions" v-if="!qrCodeUrl">
        <button class="btn-cancel" @click="handleCancel">取消订单</button>
        <button 
          class="btn-pay" 
          :disabled="!selectedMethod || paying"
          @click="handlePay"
        >
          {{ paying ? '跳转中...' : '立即支付' }}
        </button>
      </div>

      <!-- 扫码支付展示区 -->
      <div class="qr-section" v-if="qrCodeUrl">
        <div class="qr-tip">请使用{{ qrCodePayType === 'WECHAT' ? '微信' : '支付宝' }}扫描下方二维码完成支付</div>
        <div class="qr-wrapper">
          <qrcode-vue :value="qrCodeUrl" :size="220" level="H" />
        </div>
        <div class="qr-actions">
          <button class="btn-paid" :disabled="checking" @click="handleCheckPaid">
            {{ checking ? '查询中...' : '我已完成支付' }}
          </button>
          <button class="btn-reselect" @click="qrCodeUrl = ''; qrCodePayType = ''; stopPolling()">重新选择</button>
        </div>
      </div>

      <div class="payment-tips">
        <p>• 请在30分钟内完成支付，超时订单将自动取消</p>
        <p>• 支付成功后，订单状态将自动更新</p>
        <p>• 如有问题，请联系客服</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { huifuPayApi } from '@/api/huifuPay'
import { ticketApi } from '@/api/ticket'
import { mallApi } from '@/api/mall'
import QrcodeVue from 'qrcode.vue'

const route = useRoute()
const router = useRouter()

const orderId = ref<number>(0)
const orderType = ref<'ticket' | 'mall'>('ticket')
const orderInfo = ref<any>(null)
const selectedMethod = ref<'ALIPAY' | ''>('')
const paying = ref(false)
const qrCodeUrl = ref('')
const qrCodePayType = ref<'ALIPAY' | ''>('')
const checking = ref(false)
const pollTimer = ref<number | undefined>(undefined)

// 加载订单信息
const loadOrderInfo = async () => {
  try {
    orderId.value = parseInt(route.params.orderId as string)
    orderType.value = (route.query.type as 'ticket' | 'mall') || 'ticket'

    if (orderType.value === 'ticket') {
      orderInfo.value = await ticketApi.getOrderDetail(orderId.value)
    } else {
      orderInfo.value = await mallApi.getOrderDetail(orderId.value)
    }

    // 检查订单状态
    if (!orderInfo.value || orderInfo.value.status !== 0) {
      ElMessage.warning('当前订单已支付或状态已变更，请在“我的”页面查看')
      router.push('/profile')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载订单信息失败')
    router.push('/profile')
  }
}

// 轮询查单 - 直接查订单状态，不依赖汇付宝查单接口
const startPolling = (orderNo: string) => {
  stopPolling()
  let attempts = 0
  let failCount = 0
  const maxAttempts = 30
  pollTimer.value = setInterval(async () => {
    attempts++
    if (attempts > maxAttempts) {
      stopPolling()
      // 超时后直接跳转到profile，让用户自己确认
      window.location.href = '/profile'
      return
    }
    try {
      console.log(`[Poll] 第${attempts}次查订单状态: orderId=${orderId.value}, orderNo=${orderNo}`)
      // 优先用orderNo查询，更可靠
      const order = await ticketApi.getOrderByOrderNo(orderNo)
      console.log('[Poll] 订单状态:', order?.status)
      // status != 0 表示已支付
      if (order && order.status !== 0) {
        stopPolling()
        ElMessage.success('支付成功！门票已生成，请在"我的"页面查看')
        setTimeout(() => { window.location.href = '/profile' }, 1500)
      }
      failCount = 0
    } catch (e) {
      failCount++
      console.warn('[Poll] 查订单状态失败:', e)
      // 连续失败5次直接跳profile
      if (failCount >= 5) {
        stopPolling()
        window.location.href = '/profile'
      }
    }
  }, 3000) as unknown as number
}

const stopPolling = () => {
  if (pollTimer.value) {
    clearInterval(pollTimer.value)
    pollTimer.value = undefined
  }
}

// 手动点击"我已完成支付"
const handleCheckPaid = async () => {
  checking.value = true
  try {
    const orderNo = orderInfo.value?.orderNo
    if (!orderNo) throw new Error('订单号不存在')
    console.log('[Check] 手动查询订单状态: orderNo=', orderNo)
    const order = await ticketApi.getOrderByOrderNo(orderNo)
    console.log('[Check] 订单状态:', order?.status)
    if (order && order.status !== 0) {
      stopPolling()
      ElMessage.success('支付成功！门票已生成，请在"我的"页面查看')
      setTimeout(() => { window.location.href = '/profile' }, 1500)
    } else {
      ElMessage.warning('暂未检测到支付成功，请稍后再试')
    }
  } catch (e: any) {
    console.error('[Check] 查询失败:', e)
    ElMessage.error(e.message || '查询失败')
  } finally {
    checking.value = false
  }
}

// 处理支付
const handleWechatDisabled = () => {
  ElMessage.warning('微信支付已迁移为原生微信JSAPI，请走微信授权拉起支付流程，不再使用汇付页面通道')
}

const handlePay = async () => {
  if (!selectedMethod.value || paying.value) return

  try {
    paying.value = true

    if (!orderInfo.value || !orderInfo.value.orderNo) {
      throw new Error('订单号不存在')
    }

    const response = await huifuPayApi.createPayment(
      orderInfo.value.orderNo,
      selectedMethod.value
    )

    if (!response || !response.pay_url) {
      throw new Error('获取支付URL失败')
    }

    const payUrl = response.pay_url

    // 微信原生支付、支付宝扫码支付 => 展示二维码
    if (
      (selectedMethod.value === 'WECHAT' || selectedMethod.value === 'ALIPAY') &&
      (!payUrl.startsWith('http') || payUrl.startsWith('weixin://') || payUrl.startsWith('https://qr.alipay.com'))
    ) {
      qrCodeUrl.value = payUrl
      qrCodePayType.value = selectedMethod.value
      startPolling(orderInfo.value.orderNo)
    } else {
      stopPolling()
      qrCodePayType.value = ''
      window.location.href = payUrl
    }
  } catch (error: any) {
    ElMessage.error(error.message || '发起支付失败')
  } finally {
    paying.value = false
  }
}

// 取消订单
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    if (orderType.value === 'ticket') {
      await ticketApi.cancelOrder(orderId.value)
    } else {
      await mallApi.cancelOrder(orderId.value)
    }

    ElMessage.success('订单已取消')
    router.push('/profile')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消订单失败')
    }
  }
}

onMounted(() => {
  loadOrderInfo()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.huifu-payment-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.payment-container {
  max-width: 600px;
  width: 100%;
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.payment-header {
  text-align: center;
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.payment-header h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  color: #333;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: #999;
  font-size: 14px;
}

.order-info {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 28px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  align-items: center;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.info-row .value {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.info-row .value.price {
  color: #ff4d4f;
  font-size: 20px;
  font-weight: bold;
}

.payment-methods {
  margin-bottom: 28px;
}

.payment-methods h3 {
  font-size: 16px;
  color: #333;
  margin: 0 0 16px 0;
  font-weight: 600;
}

.method-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.method-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
}

.method-item:hover {
  border-color: #667eea;
  background: #f5f7ff;
  transform: translateY(-2px);
}

.method-item.active {
  border-color: #667eea;
  background: #f5f7ff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.method-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
}

.method-info {
  flex: 1;
}

.method-name {
  font-size: 16px;
  color: #333;
  font-weight: 600;
  margin-bottom: 4px;
}

.method-desc {
  font-size: 12px;
  color: #999;
}

.method-check {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.check-icon {
  color: #667eea;
  font-size: 20px;
  font-weight: bold;
}

.payment-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.btn-cancel,
.btn-pay {
  flex: 1;
  height: 48px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-pay {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
}

.btn-pay:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-pay:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
  opacity: 0.6;
}

.payment-tips {
  background: #fffbe6;
  border: 1px solid #ffe58f;
  border-radius: 8px;
  padding: 12px 16px;
}

.payment-tips p {
  margin: 0;
  padding: 4px 0;
  font-size: 12px;
  color: #666;
  line-height: 1.6;
}

.qr-section {
  text-align: center;
  padding: 8px 0 16px;
}

.qr-tip {
  font-size: 15px;
  color: #333;
  margin-bottom: 16px;
  font-weight: 500;
}

.qr-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
}

.qr-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn-paid {
  padding: 12px 28px;
  background: #1677ff;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-paid:hover:not(:disabled) {
  background: #0958d9;
}

.btn-paid:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-reselect {
  padding: 12px 20px;
  background: #f0f0f0;
  color: #666;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-reselect:hover {
  background: #e0e0e0;
}

@media (max-width: 768px) {
  .payment-container {
    padding: 20px;
  }

  .payment-header h2 {
    font-size: 24px;
  }

  .method-item {
    padding: 12px;
  }

  .method-icon {
    width: 48px;
    height: 48px;
    margin-right: 12px;
  }

  .method-name {
    font-size: 15px;
  }

  .btn-cancel,
  .btn-pay {
    height: 44px;
    font-size: 15px;
  }
}
</style>

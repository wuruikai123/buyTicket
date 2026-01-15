<template>
  <div class="payment-page">
    <div class="payment-container">
      <div class="payment-header">
        <h2>订单支付</h2>
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
          <div 
            class="method-item" 
            :class="{ active: selectedMethod === 'alipay' }"
            @click="selectedMethod = 'alipay'"
          >
            <div class="method-icon alipay-icon">
              <svg viewBox="0 0 1024 1024" width="32" height="32">
                <path d="M1024 701.9v162.5c0 88.4-71.6 160-160 160H160c-88.4 0-160-71.6-160-160V159.6C0 71.2 71.6 0 160 0h704c88.4 0 160 71.2 160 159.6v542.3z" fill="#00A0E9"/>
                <path d="M928 701.9V159.6c0-35.3-28.7-64-64-64H160c-35.3 0-64 28.7-64 64v704.8c0 35.3 28.7 64 64 64h704c35.3 0 64-28.7 64-64v-162.5z" fill="#FFFFFF"/>
                <path d="M772.5 548.6c-33.8-16.4-77.8-38.4-121.8-60.4-18.4 29.2-40.4 55.2-65.2 77.2-49.6 44-108.8 66.4-176.8 66.4-52.8 0-96.8-14.4-131.2-43.2-34.4-28.8-51.6-66.4-51.6-112.8 0-44 14.4-80.8 43.2-110.4 28.8-29.6 66.4-44.4 112.8-44.4 40 0 74.4 11.2 103.2 33.6 28.8 22.4 43.2 52 43.2 88.8 0 28.8-8.8 52-26.4 69.6-17.6 17.6-40.8 26.4-69.6 26.4-22.4 0-40.8-6.4-55.2-19.2-14.4-12.8-21.6-29.6-21.6-50.4 0-16.8 5.6-30.4 16.8-40.8 11.2-10.4 25.6-15.6 43.2-15.6 11.2 0 20.8 2.4 28.8 7.2v-33.6c-8.8-3.2-18.4-4.8-28.8-4.8-25.6 0-46.4 8-62.4 24-16 16-24 36.8-24 62.4 0 28 9.6 51.2 28.8 69.6 19.2 18.4 44 27.6 74.4 27.6 33.6 0 61.6-11.2 84-33.6 22.4-22.4 33.6-50.4 33.6-84 0-48-20-88-60-120-40-32-88.8-48-146.4-48-60.8 0-111.2 19.2-151.2 57.6-40 38.4-60 86.4-60 144 0 60.8 22.4 110.4 67.2 148.8 44.8 38.4 102.4 57.6 172.8 57.6 76.8 0 144-24.8 201.6-74.4 28.8-24.8 53.6-54.4 74.4-88.8 44 22.4 88 44.8 132 67.2-8.8 24-20 46.4-33.6 67.2H928V701.9c-48-24-96.8-48-155.5-73.3z" fill="#00A0E9"/>
              </svg>
            </div>
            <div class="method-info">
              <div class="method-name">支付宝支付</div>
              <div class="method-desc">推荐使用支付宝扫码支付</div>
            </div>
            <div class="method-check">
              <span v-if="selectedMethod === 'alipay'" class="check-icon">✓</span>
            </div>
          </div>

          <div 
            class="method-item disabled" 
            title="暂不支持"
          >
            <div class="method-icon wechat-icon">
              <svg viewBox="0 0 1024 1024" width="32" height="32">
                <path d="M1024 640c0 176-160 320-352 320-32 0-64-8-96-16l-160 96 32-128c-96-64-160-160-160-272 0-176 160-320 352-320s384 144 384 320z" fill="#09BB07"/>
                <path d="M736 576c-16 0-32-16-32-32s16-32 32-32 32 16 32 32-16 32-32 32z m128 0c-16 0-32-16-32-32s16-32 32-32 32 16 32 32-16 32-32 32z" fill="#FFFFFF"/>
                <path d="M416 0C192 0 0 160 0 352c0 112 64 208 160 272l-32 128 160-96c32 8 64 16 96 16 16 0 32 0 48-8-16-32-16-64-16-96 0-176 160-320 352-320 16 0 32 0 48 8C784 96 608 0 416 0z" fill="#09BB07"/>
                <path d="M288 224c-24 0-40-16-40-40s16-40 40-40 40 16 40 40-16 40-40 40z m256 0c-24 0-40-16-40-40s16-40 40-40 40 16 40 40-16 40-40 40z" fill="#FFFFFF"/>
              </svg>
            </div>
            <div class="method-info">
              <div class="method-name">微信支付</div>
              <div class="method-desc">暂不支持</div>
            </div>
          </div>
        </div>
      </div>

      <div class="payment-actions">
        <button class="btn-cancel" @click="handleCancel">取消订单</button>
        <button 
          class="btn-pay" 
          :disabled="!selectedMethod || paying"
          @click="handlePay"
        >
          {{ paying ? '跳转中...' : '立即支付' }}
        </button>
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
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { paymentApi } from '@/api/payment'
import { ticketApi } from '@/api/ticket'
import { mallApi } from '@/api/mall'

const route = useRoute()
const router = useRouter()

const orderId = ref<number>(0)
const orderType = ref<'ticket' | 'mall'>('ticket')
const orderInfo = ref<any>(null)
const selectedMethod = ref<string>('alipay')
const paying = ref(false)

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
      ElMessage.warning('订单状态异常，无法支付')
      router.push('/profile')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载订单信息失败')
    router.push('/profile')
  }
}

// 处理支付
const handlePay = async () => {
  if (!selectedMethod.value || paying.value) return

  try {
    paying.value = true

    if (selectedMethod.value === 'alipay') {
      // 检查是否有订单号
      if (!orderInfo.value || !orderInfo.value.orderNo) {
        throw new Error('订单号不存在')
      }

      // 调用后端支付接口获取支付表单（PC网页端）
      const response = await paymentApi.createAlipayPc({ orderNo: orderInfo.value.orderNo })
      
      if (response && typeof response === 'string') {
        // 后端返回支付表单HTML，直接提交
        const form = document.createElement('div')
        form.innerHTML = response
        document.body.appendChild(form)
        
        // 自动提交表单
        const submitForm = form.querySelector('form') as HTMLFormElement
        if (submitForm) {
          submitForm.submit()
        } else {
          throw new Error('支付表单生成失败')
        }
      } else {
        throw new Error('获取支付表单失败')
      }
    }
  } catch (error: any) {
    paying.value = false
    ElMessage.error(error.message || '发起支付失败')
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
</script>

<style scoped>
.payment-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
}

.payment-container {
  max-width: 600px;
  margin: 0 auto;
  background: white;
  border-radius: 8px;
  padding: 24px;
}

.payment-header {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e0e0e0;
}

.payment-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.order-info {
  background: #f9f9f9;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 24px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  color: #666;
  font-size: 14px;
}

.info-row .value {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.info-row .value.price {
  color: #ff4d4f;
  font-size: 18px;
  font-weight: bold;
}

.payment-methods {
  margin-bottom: 24px;
}

.payment-methods h3 {
  font-size: 16px;
  color: #333;
  margin: 0 0 16px 0;
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
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.method-item:hover:not(.disabled) {
  border-color: #1890ff;
  background: #f0f8ff;
}

.method-item.active {
  border-color: #1890ff;
  background: #f0f8ff;
}

.method-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.method-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.method-info {
  flex: 1;
}

.method-name {
  font-size: 16px;
  color: #333;
  font-weight: 500;
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
}

.check-icon {
  color: #1890ff;
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
  height: 44px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-pay {
  background: #1890ff;
  color: white;
  font-weight: 500;
}

.btn-pay:hover:not(:disabled) {
  background: #40a9ff;
}

.btn-pay:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.payment-tips {
  background: #fffbe6;
  border: 1px solid #ffe58f;
  border-radius: 6px;
  padding: 12px 16px;
}

.payment-tips p {
  margin: 0;
  padding: 4px 0;
  font-size: 12px;
  color: #666;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .payment-page {
    padding: 12px;
  }

  .payment-container {
    padding: 16px;
  }

  .payment-header h2 {
    font-size: 20px;
  }

  .method-item {
    padding: 12px;
  }

  .method-icon {
    width: 40px;
    height: 40px;
    margin-right: 12px;
  }

  .method-name {
    font-size: 15px;
  }
}
</style>

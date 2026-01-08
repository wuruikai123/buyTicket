<template>
  <div class="checkout-page">
    <!-- 顶部导航栏 -->
    <div class="header">
      <button class="back-button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <h1 class="page-title">确认订单</h1>
      <div class="header-placeholder"></div>
    </div>

    <!-- 联系人信息 -->
    <div class="section contact-section">
      <h3 class="section-title">联系人信息</h3>
      <div class="form-row">
        <div class="form-item">
          <label>联系人姓名</label>
          <input 
            v-model="formData.contactName" 
            type="text" 
            placeholder="请输入姓名"
            :class="{ error: errors.contactName }"
          />
          <span v-if="errors.contactName" class="error-msg">{{ errors.contactName }}</span>
        </div>
        <div class="form-item">
          <label>联系电话</label>
          <input 
            v-model="formData.contactPhone" 
            type="tel" 
            placeholder="请输入电话"
            :class="{ error: errors.contactPhone }"
          />
          <span v-if="errors.contactPhone" class="error-msg">{{ errors.contactPhone }}</span>
        </div>
      </div>
    </div>

    <!-- 收货地址 -->
    <div class="section address-section">
      <h3 class="section-title">收货地址</h3>
      <div class="form-item">
        <textarea 
          v-model="formData.address" 
          placeholder="请输入详细收货地址"
          rows="2"
          :class="{ error: errors.address }"
        ></textarea>
        <span v-if="errors.address" class="error-msg">{{ errors.address }}</span>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="section items-section">
      <h3 class="section-title">商品清单</h3>
      <div class="order-items">
        <div v-for="item in checkoutItems" :key="item.id" class="order-item">
          <div class="item-image" :style="{ backgroundImage: item.coverImage ? `url(${item.coverImage})` : '' }">
            <span v-if="!item.coverImage" class="no-image">暂无图片</span>
          </div>
          <div class="item-info">
            <h4 class="item-name">{{ item.productName }}</h4>
            <p class="item-price">¥{{ item.price }} × {{ item.quantity }}</p>
          </div>
          <div class="item-subtotal">¥{{ item.price * item.quantity }}</div>
        </div>
      </div>
    </div>

    <!-- 订单汇总 -->
    <div class="section summary-section">
      <div class="summary-row">
        <span>商品数量</span>
        <span>{{ totalQuantity }} 件</span>
      </div>
      <div class="summary-row total">
        <span>订单总额</span>
        <span class="total-amount">¥{{ totalAmount }}</span>
      </div>
    </div>

    <!-- 底部支付栏 -->
    <div class="bottom-bar">
      <div class="total-display">
        <span>合计:</span>
        <em>¥{{ totalAmount }}</em>
      </div>
      <button 
        class="pay-btn" 
        :disabled="!isFormValid || isSubmitting"
        @click="handleSubmit"
      >
        {{ isSubmitting ? '提交中...' : '提交订单' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { mallApi } from '@/api/mall'
import type { CartItemVO } from '@/utils/cartUtils'

const router = useRouter()
const checkoutItems = ref<CartItemVO[]>([])
const isSubmitting = ref(false)

const formData = reactive({
  contactName: '',
  contactPhone: '',
  address: ''
})

const errors = reactive({
  contactName: '',
  contactPhone: '',
  address: ''
})

// 计算属性
const totalQuantity = computed(() => 
  checkoutItems.value.reduce((sum, item) => sum + item.quantity, 0)
)

const totalAmount = computed(() => 
  checkoutItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
)

const isFormValid = computed(() => {
  return formData.contactName.trim() !== '' && 
         formData.contactPhone.trim() !== '' && 
         formData.address.trim() !== ''
})

// 验证表单
const validateForm = (): boolean => {
  let valid = true
  errors.contactName = ''
  errors.contactPhone = ''
  errors.address = ''

  if (!formData.contactName.trim()) {
    errors.contactName = '请输入联系人姓名'
    valid = false
  }
  if (!formData.contactPhone.trim()) {
    errors.contactPhone = '请输入联系电话'
    valid = false
  }
  if (!formData.address.trim()) {
    errors.address = '请输入收货地址'
    valid = false
  }
  return valid
}

// 提交订单
const handleSubmit = async () => {
  if (!validateForm()) return
  
  isSubmitting.value = true
  try {
    // 创建订单
    const orderData = {
      contactName: formData.contactName,
      contactPhone: formData.contactPhone,
      address: formData.address,
      totalAmount: totalAmount.value,
      items: checkoutItems.value.map(item => ({
        productId: item.productId,
        quantity: item.quantity,
        unitPrice: item.price
      }))
    }
    
    const res = await mallApi.createOrder(orderData)
    
    if (res && res.orderId) {
      // 清除购物车中已购买的商品
      const itemIds = checkoutItems.value.map(item => item.id).join(',')
      await mallApi.removeCartItem(itemIds)
      
      // 清除结算缓存
      sessionStorage.removeItem('checkoutItems')
      
      // 跳转到支付页面
      router.push({
        path: `/payment/${res.orderId}`,
        query: { type: 'mall' }
      })
    }
  } catch (e: any) {
    console.error('提交订单失败', e)
    alert(e.message || '提交订单失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 加载结算商品
onMounted(() => {
  const stored = sessionStorage.getItem('checkoutItems')
  if (stored) {
    checkoutItems.value = JSON.parse(stored)
  } else {
    // 没有商品，返回购物车
    router.replace('/cart')
  }
})
</script>


<style scoped>
.checkout-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 80px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background-color: white;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid #e0e0e0;
}

.back-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: none;
  border: none;
  cursor: pointer;
  color: #333;
  border-radius: 50%;
}

.back-button:hover {
  background-color: #f0f0f0;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.header-placeholder {
  width: 40px;
}

.section {
  background-color: white;
  margin: 12px 16px;
  padding: 16px;
  border-radius: 12px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin: 0 0 16px;
}

.form-row {
  display: flex;
  gap: 12px;
}

.form-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  font-size: 14px;
  color: #666;
}

.form-item input,
.form-item textarea {
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  color: #333;
  outline: none;
  transition: border-color 0.3s;
}

.form-item input:focus,
.form-item textarea:focus {
  border-color: #409eff;
}

.form-item input.error,
.form-item textarea.error {
  border-color: #f56c6c;
}

.error-msg {
  font-size: 12px;
  color: #f56c6c;
}

.form-item textarea {
  resize: none;
}

/* 商品列表 */
.order-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.item-image {
  width: 60px;
  height: 60px;
  background-color: #eee;
  background-size: cover;
  background-position: center;
  border-radius: 6px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-image {
  font-size: 10px;
  color: #999;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-price {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.item-subtotal {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

/* 订单汇总 */
.summary-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #666;
}

.summary-row.total {
  padding-top: 12px;
  border-top: 1px solid #eee;
  font-size: 16px;
  color: #333;
}

.total-amount {
  font-weight: bold;
  color: #f56c6c;
}

/* 底部支付栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: white;
  border-top: 1px solid #e0e0e0;
  z-index: 100;
}

.total-display {
  font-size: 14px;
  color: #333;
}

.total-display em {
  font-style: normal;
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
  margin-left: 4px;
}

.pay-btn {
  padding: 12px 32px;
  background-color: #f56c6c;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
}

.pay-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.pay-btn:not(:disabled):hover {
  background-color: #f78989;
}

/* 响应式 */
@media (max-width: 480px) {
  .form-row {
    flex-direction: column;
  }
}
</style>

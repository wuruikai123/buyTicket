<template>
  <div class="order-detail">
    <!-- 顶部导航 -->
    <div class="header">
      <button class="back-button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <h1 class="page-title">订单详情</h1>
      <div class="header-placeholder"></div>
    </div>

    <div class="detail-content" v-loading="loading">
      <!-- 订单状态 -->
      <div class="status-section">
        <div class="status-icon">
          <el-icon v-if="order.status === 0"><Clock /></el-icon>
          <el-icon v-else-if="order.status === 1"><Check /></el-icon>
          <el-icon v-else-if="order.status === 2"><CircleCheck /></el-icon>
          <el-icon v-else><Close /></el-icon>
        </div>
        <div class="status-info">
          <span class="status-text">{{ statusText }}</span>
          <span class="status-desc">{{ statusDesc }}</span>
        </div>
      </div>

      <!-- 联系人/收货信息 -->
      <div class="info-section">
        <h3 class="section-title">{{ orderType === 'ticket' ? '联系人信息' : '收货信息' }}</h3>
        <div class="info-row">
          <span class="label">{{ orderType === 'ticket' ? '联系人' : '收货人' }}</span>
          <span class="value">{{ order.contactName || order.receiverName }}</span>
        </div>
        <div class="info-row">
          <span class="label">联系电话</span>
          <span class="value">{{ order.contactPhone || order.receiverPhone }}</span>
        </div>
        <div class="info-row" v-if="orderType === 'mall'">
          <span class="label">收货地址</span>
          <span class="value">{{ order.receiverAddress }}</span>
        </div>
      </div>

      <!-- 商品/门票信息 -->
      <div class="items-section">
        <h3 class="section-title">{{ orderType === 'ticket' ? '门票信息' : '商品信息' }}</h3>
        <div v-for="(item, index) in order.items" :key="index" class="order-item">
          <div class="item-image" :style="{ backgroundImage: item.coverImage ? `url(${item.coverImage})` : '' }">
            <span v-if="!item.coverImage" class="no-image">暂无图片</span>
          </div>
          <div class="item-info">
            <h4 class="item-name">{{ item.name || item.exhibitionName || item.productName }}</h4>
            <p class="item-spec" v-if="orderType === 'ticket'">
              {{ item.ticketDate }} {{ item.timeSlot }}
            </p>
            <p class="item-price">¥{{ item.price }} × {{ item.quantity }}</p>
          </div>
          <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
        </div>
      </div>

      <!-- 订单信息 -->
      <div class="order-info-section">
        <h3 class="section-title">订单信息</h3>
        <div class="info-row">
          <span class="label">订单编号</span>
          <span class="value">{{ order.id }}</span>
        </div>
        <div class="info-row">
          <span class="label">下单时间</span>
          <span class="value">{{ order.createTime }}</span>
        </div>
        <div class="info-row" v-if="order.payTime">
          <span class="label">支付时间</span>
          <span class="value">{{ order.payTime }}</span>
        </div>
      </div>

      <!-- 金额信息 -->
      <div class="amount-section">
        <div class="amount-row total">
          <span>订单总额</span>
          <span class="total-amount">¥{{ order.totalAmount?.toFixed(2) }}</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-section" v-if="order.status === 0">
        <el-button type="primary" size="large" @click="handlePay">立即支付</el-button>
        <el-button size="large" @click="handleCancel">取消订单</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Clock, Check, CircleCheck, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ticketApi } from '@/api/ticket'
import { mallApi } from '@/api/mall'

interface OrderItem {
  name?: string
  exhibitionName?: string
  productName?: string
  ticketDate?: string
  timeSlot?: string
  price: number
  quantity: number
  coverImage?: string
}

interface OrderDetail {
  id: number
  status: number
  totalAmount: number
  contactName?: string
  contactPhone?: string
  receiverName?: string
  receiverPhone?: string
  receiverAddress?: string
  createTime: string
  payTime?: string
  items: OrderItem[]
}

const router = useRouter()
const route = useRoute()
const loading = ref(false)

const orderId = computed(() => Number(route.params.id))
const orderType = computed(() => route.query.type as string || 'ticket')

const order = ref<OrderDetail>({
  id: 0,
  status: 0,
  totalAmount: 0,
  createTime: '',
  items: []
})

const statusText = computed(() => {
  const map: Record<number, string> = {
    0: '待支付',
    1: orderType.value === 'ticket' ? '待使用' : '待发货',
    2: orderType.value === 'ticket' ? '已使用' : '已发货',
    3: '已完成',
    4: '已取消'
  }
  return map[order.value.status] || '未知'
})

const statusDesc = computed(() => {
  const map: Record<number, string> = {
    0: '请在30分钟内完成支付',
    1: orderType.value === 'ticket' ? '请按时到场使用' : '商家正在备货',
    2: orderType.value === 'ticket' ? '感谢您的光临' : '商品已发出，请注意查收',
    3: '订单已完成',
    4: '订单已取消'
  }
  return map[order.value.status] || ''
})

const loadOrderDetail = async () => {
  loading.value = true
  try {
    let res: any
    if (orderType.value === 'ticket') {
      res = await ticketApi.getOrderDetail(orderId.value)
    } else {
      res = await mallApi.getOrderDetail(orderId.value)
    }
    if (res) {
      order.value = res
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载订单详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const handlePay = async () => {
  try {
    await mallApi.pay({
      orderId: order.value.id,
      type: orderType.value,
      password: '123456'
    })
    ElMessage.success('支付成功')
    loadOrderDetail()
  } catch (e: any) {
    ElMessage.error(e.message || '支付失败')
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定取消该订单吗？', '提示', { type: 'warning' })
    if (orderType.value === 'ticket') {
      await ticketApi.cancelOrder(orderId.value)
    } else {
      await mallApi.cancelOrder(orderId.value)
    }
    ElMessage.success('订单已取消')
    loadOrderDetail()
  } catch (e) {
    // 取消操作
  }
}

onMounted(loadOrderDetail)
</script>

<style scoped>
.order-detail {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 100px;
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

.page-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.header-placeholder {
  width: 40px;
}

.detail-content {
  padding: 16px;
}

.status-section {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border-radius: 12px;
  margin-bottom: 12px;
  color: white;
}

.status-icon {
  width: 48px;
  height: 48px;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.status-text {
  font-size: 18px;
  font-weight: bold;
}

.status-desc {
  font-size: 14px;
  opacity: 0.9;
}

.info-section,
.items-section,
.order-info-section,
.amount-section {
  background-color: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin: 0 0 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}

.info-row .label {
  color: #999;
}

.info-row .value {
  color: #333;
  text-align: right;
  flex: 1;
  margin-left: 16px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 8px;
}

.order-item:last-child {
  margin-bottom: 0;
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

.item-spec {
  font-size: 12px;
  color: #999;
  margin: 0 0 4px;
}

.item-price {
  font-size: 12px;
  color: #666;
  margin: 0;
}

.item-subtotal {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #666;
}

.amount-row.total {
  font-size: 16px;
  color: #333;
}

.total-amount {
  font-weight: bold;
  color: #f56c6c;
  font-size: 20px;
}

.action-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 12px;
  padding: 16px;
  background-color: white;
  border-top: 1px solid #e0e0e0;
}

.action-section .el-button {
  flex: 1;
}
</style>

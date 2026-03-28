<template>
  <div class="order-detail">
    <div class="header">
      <button class="back-button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <h1 class="page-title">订单详情</h1>
      <div class="header-placeholder"></div>
    </div>

    <div class="detail-content" v-loading="loading">
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

      <div class="info-section">
        <h3 class="section-title">联系人信息</h3>
        <div class="info-row">
          <span class="label">联系人</span>
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

      <div class="items-section" v-if="orderType === 'ticket'">
        <h3 class="section-title">门票明细（按子票）</h3>
        <div v-for="item in order.items" :key="item.id" class="ticket-item-card">
          <div class="ticket-item-header">
            <div class="ticket-main">
              <span class="ticket-name">{{ item.exhibitionName || '展览门票' }}</span>
              <span class="ticket-time">{{ item.ticketDate }} {{ item.timeSlot }}</span>
            </div>
            <span class="ticket-status" :class="statusClass(item.ticketStatus)">{{ ticketStatusText(item.ticketStatus) }}</span>
          </div>

          <div class="ticket-buyer">
            <div>购票人：{{ item.buyerName || '-' }}</div>
            <div>证件号：{{ item.buyerIdCard || '-' }}</div>
          </div>

          <div class="ticket-select" v-if="item.ticketStatus === 1 || item.ticketStatus === 5">
            <el-checkbox
              v-if="item.ticketStatus === 1"
              :model-value="selectedRefundIds.includes(item.id)"
              @change="(val:boolean) => toggleSelect(item.id, val, 'refund')"
            >勾选申请退款</el-checkbox>
            <el-checkbox
              v-if="item.ticketStatus === 5"
              :model-value="selectedCancelRefundIds.includes(item.id)"
              @change="(val:boolean) => toggleSelect(item.id, val, 'cancel')"
            >勾选取消退款</el-checkbox>
          </div>
        </div>
      </div>

      <div class="items-section" v-else>
        <h3 class="section-title">商品信息</h3>
        <div v-for="(item, index) in order.items" :key="index" class="order-item">
          <div class="item-info">
            <h4 class="item-name">{{ item.name || item.productName }}</h4>
            <p class="item-price">¥{{ item.price }} × {{ item.quantity }}</p>
          </div>
          <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
        </div>
      </div>

      <div class="order-info-section">
        <h3 class="section-title">订单信息</h3>

        <div class="qrcode-section" v-if="orderType === 'ticket' && order.status === 1 && order.orderNo">
          <div class="qrcode-wrapper">
            <qrcode-vue :value="qrcodeData" :size="200" level="H" />
          </div>
          <p class="qrcode-tip">请向工作人员出示此二维码进行核销（每次核销1张子票）</p>
        </div>

        <div class="info-row order-no-row" v-if="order.orderNo">
          <span class="label">订单号</span>
          <div class="order-no-wrapper">
            <span class="value order-no">{{ order.orderNo }}</span>
            <button class="copy-btn" @click="copyOrderNo">
              <el-icon><CopyDocument /></el-icon>
              <span>复制</span>
            </button>
          </div>
        </div>
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

      <div class="amount-section">
        <div class="amount-row total">
          <span>订单总额</span>
          <span class="total-amount">¥{{ order.totalAmount?.toFixed(2) }}</span>
        </div>

        <div class="refund-button-container" v-if="orderType === 'ticket'">
          <button class="refund-button" @click="handleRequestRefund" :disabled="!selectedRefundIds.length">申请已勾选子票退款</button>
          <button class="cancel-refund-button" @click="handleCancelRefund" :disabled="!selectedCancelRefundIds.length">取消已勾选子票退款</button>
        </div>
      </div>

      <div class="action-section" v-if="order.status === 0">
        <el-button type="primary" size="large" @click="handlePay">支付宝支付</el-button>
        <el-button type="success" size="large" @click="handleHuifuPay">汇付宝支付</el-button>
        <el-button size="large" @click="handleCancel">取消订单</el-button>
        <el-button size="large" @click="loadOrderDetail">刷新状态</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Clock, Check, CircleCheck, Close, CopyDocument } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ticketApi } from '@/api/ticket'
import { mallApi } from '@/api/mall'
import QrcodeVue from 'qrcode.vue'

interface OrderItem {
  id: number
  name?: string
  exhibitionName?: string
  productName?: string
  ticketDate?: string
  timeSlot?: string
  price: number
  quantity: number
  coverImage?: string
  buyerName?: string
  buyerIdCard?: string
  ticketStatus?: number
}

interface OrderDetail {
  id: number
  orderNo?: string
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

const orderId = computed(() => {
  const id = Number(route.params.id)
  return Number.isFinite(id) ? id : 0
})
const orderType = computed(() => route.query.type as string || 'ticket')

const order = ref<OrderDetail>({
  id: 0,
  status: 0,
  totalAmount: 0,
  createTime: '',
  items: []
})

const selectedRefundIds = ref<number[]>([])
const selectedCancelRefundIds = ref<number[]>([])

const qrcodeData = computed(() => {
  if (!order.value.orderNo) return ''
  return JSON.stringify({ orderNo: order.value.orderNo, orderId: order.value.id, type: 'ticket', timestamp: Date.now() })
})

const statusText = computed(() => {
  const map: Record<number, string> = { 0: '待支付', 1: '待使用', 2: '已使用', 3: '已完成', 4: '已取消', 5: '退款中', 6: '已退款' }
  return map[order.value.status] || '未知'
})

const statusDesc = computed(() => {
  const map: Record<number, string> = {
    0: '请在30分钟内完成支付',
    1: '门票待使用，请按时到场',
    2: '感谢您的光临',
    3: '订单已完成',
    4: '订单已取消',
    5: '存在退款中子票，等待管理员处理',
    6: '订单子票均已退款'
  }
  return map[order.value.status] || ''
})

const ticketStatusText = (status?: number) => {
  const map: Record<number, string> = { 1: '待使用', 2: '已使用', 5: '退款中', 6: '已退款' }
  return map[status || 1] || '待使用'
}

const statusClass = (status?: number) => {
  if (status === 2) return 'used'
  if (status === 5) return 'refunding'
  if (status === 6) return 'refunded'
  return 'waiting'
}

const resetSelections = () => {
  selectedRefundIds.value = []
  selectedCancelRefundIds.value = []
}

const loadOrderDetail = async () => {
  loading.value = true
  try {
    if (!orderId.value) {
      ElMessage.error('订单ID无效')
      return
    }
    let res: any
    if (orderType.value === 'ticket') {
      res = await ticketApi.getOrderDetail(orderId.value)
    } else {
      res = await mallApi.getOrderDetail(orderId.value)
    }
    if (res) {
      order.value = res
      resetSelections()
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载订单详情失败')
  } finally {
    loading.value = false
  }
}

const toggleSelect = (id: number, checked: boolean, type: 'refund' | 'cancel') => {
  const list = type === 'refund' ? selectedRefundIds.value : selectedCancelRefundIds.value
  if (checked) {
    if (!list.includes(id)) list.push(id)
  } else {
    const idx = list.indexOf(id)
    if (idx >= 0) list.splice(idx, 1)
  }
}

const goBack = () => router.back()

const handlePay = () => {
  router.push({ name: 'Payment', params: { orderId: order.value.id }, query: { type: orderType.value } })
}

const handleHuifuPay = () => {
  router.push({ name: 'HuifuPayment', params: { orderId: order.value.id }, query: { type: orderType.value } })
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
  } catch (_e) {
  }
}

const copyOrderNo = async () => {
  try {
    await navigator.clipboard.writeText(order.value.orderNo || '')
    ElMessage.success('订单号已复制，可用于核销')
  } catch (_e) {
    ElMessage.error('复制失败，请手动复制')
  }
}

const handleRequestRefund = async () => {
  if (!selectedRefundIds.value.length) {
    ElMessage.warning('请先勾选要退款的子票')
    return
  }
  try {
    await ElMessageBox.confirm(`确认申请退款所选 ${selectedRefundIds.value.length} 张子票吗？`, '申请退款', { type: 'warning' })
    await ticketApi.requestRefund(orderId.value, selectedRefundIds.value)
    ElMessage.success('退款申请已提交')
    await loadOrderDetail()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '申请退款失败')
  }
}

const handleCancelRefund = async () => {
  if (!selectedCancelRefundIds.value.length) {
    ElMessage.warning('请先勾选要取消退款的子票')
    return
  }
  try {
    await ElMessageBox.confirm(`确认取消所选 ${selectedCancelRefundIds.value.length} 张子票退款吗？`, '取消退款', { type: 'warning' })
    await ticketApi.cancelRefund(orderId.value, selectedCancelRefundIds.value)
    ElMessage.success('已取消退款申请')
    await loadOrderDetail()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '取消退款失败')
  }
}

onMounted(() => {
  loadOrderDetail()
})
</script>

<style scoped>
.order-detail { min-height: 100vh; background-color: #f5f5f5; padding-bottom: 100px; }
.header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; background-color: white; position: sticky; top: 0; z-index: 100; border-bottom: 1px solid #e0e0e0; }
.back-button { display: flex; align-items: center; justify-content: center; width: 40px; height: 40px; background: none; border: none; cursor: pointer; color: #333; border-radius: 50%; }
.page-title { font-size: 18px; font-weight: bold; color: #333; margin: 0; }
.header-placeholder { width: 40px; }
.detail-content { padding: 16px; }
.status-section { display: flex; align-items: center; gap: 16px; padding: 24px; background: linear-gradient(135deg, #409eff, #66b1ff); border-radius: 12px; margin-bottom: 12px; color: white; }
.status-icon { width: 48px; height: 48px; background-color: rgba(255, 255, 255, 0.2); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24px; }
.status-info { display: flex; flex-direction: column; gap: 4px; }
.status-text { font-size: 18px; font-weight: bold; }
.status-desc { font-size: 14px; opacity: 0.9; }
.info-section,.items-section,.order-info-section,.amount-section { background-color: white; border-radius: 12px; padding: 16px; margin-bottom: 12px; }
.section-title { font-size: 16px; font-weight: bold; color: #333; margin: 0 0 16px; }
.info-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; }
.info-row .label { color: #999; }
.info-row .value { color: #333; text-align: right; flex: 1; margin-left: 16px; }
.order-no-row { background-color: #ecf5ff; padding: 12px; border-radius: 8px; margin-bottom: 8px; }
.order-no-wrapper { display: flex; align-items: center; gap: 8px; flex: 1; justify-content: flex-end; }
.order-no { font-weight: 600; color: #409eff; font-family: 'Courier New', monospace; letter-spacing: 0.5px; font-size: 15px; }
.copy-btn { display: flex; align-items: center; gap: 4px; padding: 6px 12px; background-color: #409eff; border: none; border-radius: 4px; color: white; cursor: pointer; font-size: 13px; white-space: nowrap; }

.ticket-item-card { border: 1px solid #eee; border-radius: 10px; padding: 12px; margin-bottom: 10px; }
.ticket-item-header { display: flex; justify-content: space-between; gap: 12px; }
.ticket-main { display: flex; flex-direction: column; gap: 4px; }
.ticket-name { font-size: 14px; font-weight: 600; color: #333; }
.ticket-time { font-size: 12px; color: #666; }
.ticket-buyer { margin-top: 8px; font-size: 13px; color: #444; line-height: 1.8; }
.ticket-status { font-size: 12px; padding: 2px 8px; border-radius: 12px; height: fit-content; }
.ticket-status.waiting { color: #409eff; background: #ecf5ff; }
.ticket-status.used { color: #67c23a; background: #f0f9eb; }
.ticket-status.refunding { color: #e6a23c; background: #fdf6ec; }
.ticket-status.refunded { color: #909399; background: #f4f4f5; }
.ticket-select { margin-top: 10px; }

.qrcode-section { display: flex; flex-direction: column; align-items: center; padding: 24px 16px; background: linear-gradient(135deg, #f5f7fa, #e8eef5); border-radius: 12px; margin-bottom: 16px; }
.qrcode-wrapper { padding: 16px; background-color: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
.qrcode-tip { margin: 12px 0 0; font-size: 13px; color: #666; text-align: center; }

.amount-row { display: flex; justify-content: space-between; font-size: 14px; color: #666; }
.amount-row.total { font-size: 16px; color: #333; }
.total-amount { font-weight: bold; color: #f56c6c; font-size: 20px; }
.refund-button-container { display: flex; gap: 12px; justify-content: center; margin-top: 20px; padding-top: 20px; border-top: 1px solid #f0f0f0; }
.refund-button,.cancel-refund-button { width: 45%; padding: 12px; border-radius: 8px; font-size: 14px; cursor: pointer; }
.refund-button { background-color: #333; color: white; border: none; }
.cancel-refund-button { background-color: white; color: #e53327; border: 2px solid #e53327; }
.refund-button:disabled,.cancel-refund-button:disabled { opacity: 0.4; cursor: not-allowed; }

.action-section { position: fixed; bottom: 0; left: 0; right: 0; display: flex; gap: 12px; padding: 16px; background-color: white; border-top: 1px solid #e0e0e0; }
.action-section .el-button { flex: 1; }
</style>

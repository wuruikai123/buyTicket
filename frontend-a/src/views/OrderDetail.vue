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
          <el-icon v-else-if="order.status === 5"><Clock /></el-icon>
          <el-icon v-else-if="order.status === 6"><CircleCheck /></el-icon>
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
          <span class="value">{{ order.contactName }}</span>
        </div>
        <div class="info-row" v-if="order.contactPhone">
          <span class="label">联系电话</span>
          <span class="value">{{ order.contactPhone }}</span>
        </div>
      </div>

      <div class="items-section" v-if="orderType === 'ticket'">
        <h3 class="section-title">门票明细（子票列表）</h3>
        <div v-for="item in ticketItems" :key="item.id" class="ticket-item-card">
          <div class="ticket-item-header">
            <div class="ticket-main">
              <span class="ticket-name">{{ item.exhibitionName || '展览门票' }}</span>
              <span class="ticket-time">{{ formatTicketTime(item) }}</span>
            </div>
            <span class="ticket-status" :class="statusClass(item.ticketStatus)">{{ ticketStatusText(item.ticketStatus) }}</span>
          </div>

          <div class="ticket-buyer">
            <div>购票人：{{ item.buyerName || '-' }}</div>
            <div>证件号：{{ item.buyerIdCard || '-' }}</div>
            <div v-if="item.buyerPhone">手机号：{{ item.buyerPhone }}</div>
          </div>

          <div class="ticket-actions">
            <el-button
              v-if="canRequestRefund(item)"
              size="small"
              type="danger"
              plain
              @click="handleRequestRefundByItem(item)"
            >
              单独退款
            </el-button>
            <el-button
              v-if="canCancelRefund(item)"
              size="small"
              @click="handleCancelRefundByItem(item)"
            >
              取消退款
            </el-button>
          </div>
        </div>
      </div>

      <div class="items-section" v-else>
        <h3 class="section-title">商品信息</h3>
        <div v-for="(item, index) in order.items" :key="index" class="order-item">
          <div class="item-info">
            <h4 class="item-name">{{ item.name || item.productName }}</h4>
            <p class="item-price">¥{{ Number(item.price || 0).toFixed(2) }} × {{ item.quantity || 0 }}</p>
          </div>
          <div class="item-subtotal">¥{{ (Number(item.price || 0) * Number(item.quantity || 0)).toFixed(2) }}</div>
        </div>
      </div>

      <div class="order-info-section">
        <h3 class="section-title">订单信息</h3>

        <div class="qrcode-section" v-if="orderType === 'ticket' && canShowTicketQrcodes">
          <h4 class="qrcode-title">门票二维码（每张票独立核销）</h4>
          <div class="ticket-qrcode-list">
            <div class="ticket-qrcode-item" v-for="item in activeTicketItems" :key="item.id">
              <div class="ticket-qrcode-meta">
                <div class="ticket-qrcode-name">{{ item.buyerName || '未填写姓名' }}</div>
                <div class="ticket-qrcode-time">{{ formatTicketTime(item) }}</div>
              </div>
              <div class="qrcode-wrapper">
                <qrcode-vue :value="buildTicketQrcode(item)" :size="160" level="H" />
              </div>
              <p class="qrcode-tip">出示此二维码可核销该子票</p>
            </div>
          </div>
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
          <button class="refund-button" @click="handleRequestRefund">申请整单退款</button>
        </div>
      </div>

      <div class="action-section" v-if="order.status === 0">
        <el-button type="primary" size="large" @click="handlePay">立即支付</el-button>
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
  orderId?: number
  exhibitionId?: number
  exhibitionName?: string
  ticketDate?: string
  timeSlot?: string
  quantity?: number
  price?: number
  buyerName?: string
  buyerIdCard?: string
  buyerPhone?: string
  ticketStatus?: number
  refundRequestTime?: string
  refundTime?: string
}

interface OrderDetail {
  id: number
  orderNo?: string
  userId?: number
  status: number
  totalAmount: number
  contactName?: string
  contactPhone?: string
  createTime: string
  payTime?: string
  verifyTime?: string
  refundRequestTime?: string
  refundTime?: string
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

const activeTicketItems = computed(() => {
  return (order.value.items || []).filter(item => {
    const status = item.ticketStatus || 1
    return status === 1 || status === 5
  })
})

const canShowTicketQrcodes = computed(() => {
  return orderType.value === 'ticket' && !!order.value.orderNo && activeTicketItems.value.length > 0
})

const ticketSummary = computed(() => {
  const items = order.value.items || []
  return {
    total: items.length,
    waiting: items.filter(i => (i.ticketStatus || 1) === 1).length,
    used: items.filter(i => i.ticketStatus === 2).length,
    refunding: items.filter(i => i.ticketStatus === 5).length,
    refunded: items.filter(i => i.ticketStatus === 6).length
  }
})

const statusText = computed(() => {
  const s = ticketSummary.value
  if (orderType.value === 'ticket') {
    if (s.refunding > 0) return `退款中（${s.refunding}/${s.total}）`
    if (s.refunded > 0 && s.waiting === 0 && s.used === 0) return `部分/全部已退款（${s.refunded}/${s.total}）`
    if (s.refunded > 0) return `部分已退款（${s.refunded}/${s.total}）`
    if (order.value.status === 1) return '待使用'
    if (order.value.status === 2) return '已使用'
    if (order.value.status === 0) return '待支付'
  }
  const map: Record<number, string> = { 0: '待支付', 1: '待使用', 2: '已使用', 3: '已完成', 4: '已取消', 5: '退款中', 6: '已退款' }
  return map[order.value.status] || '未知'
})

const statusDesc = computed(() => {
  const s = ticketSummary.value
  if (orderType.value === 'ticket') {
    if (s.refunding > 0) return '部分子票退款处理中，完成后可继续处理其他子票'
    if (s.refunded > 0 && s.waiting > 0) return '部分子票已退款，剩余子票仍可继续退款'
    if (s.refunded > 0 && s.waiting === 0 && s.used === 0) return '当前订单子票已全部退款'
    if (order.value.status === 1) return '门票待使用，请按时到场'
    if (order.value.status === 2) return '感谢您的光临'
    if (order.value.status === 0) return '请在30分钟内完成支付'
  }
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

const ticketItems = computed(() => order.value.items || [])

const formatTicketTime = (item: OrderItem) => {
  const date = item.ticketDate || '-'
  const slot = item.timeSlot || ''
  return slot ? `${date} ${slot}` : date
}

const ticketStatusText = (status?: number) => {
  const map: Record<number, string> = { 1: '待使用', 2: '已使用', 3: '已取消', 5: '退款中', 6: '已退款' }
  return map[status || 1] || '待使用'
}

const statusClass = (status?: number) => {
  if (status === 2) return 'used'
  if (status === 3) return 'cancelled'
  if (status === 5) return 'refunding'
  if (status === 6) return 'refunded'
  return 'waiting'
}

const resetSelections = () => {
  selectedRefundIds.value = []
  selectedCancelRefundIds.value = []
}

const buildTicketQrcode = (item: OrderItem) => {
  if (!order.value.orderNo || !item.id) return ''
  return `TT|${order.value.orderNo}|${item.id}`
}

const canRequestRefund = (item: OrderItem) => {
  return (item.ticketStatus || 1) === 1
}

const canCancelRefund = (item: OrderItem) => {
  return item.ticketStatus === 5
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

// 子票勾选退款逻辑已取消，保留相关状态变量以兼容历史数据
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
  try {
    await ElMessageBox.confirm('确认申请整单退款吗？', '申请退款', { type: 'warning' })
    await ticketApi.requestRefund(orderId.value)
    ElMessage.success('退款申请已提交')
    await loadOrderDetail()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '申请退款失败')
  }
}

const handleRequestRefundByItem = async (item: OrderItem) => {
  if (!item.id) return
  try {
    await ElMessageBox.confirm('确认只退款这1张票吗？', '单票退款', { type: 'warning' })
    await ticketApi.requestRefund(orderId.value, [item.id])
    ElMessage.success('该子票退款申请已提交')
    await loadOrderDetail()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '单票退款失败')
  }
}

const handleCancelRefundByItem = async (item: OrderItem) => {
  if (!item.id) return
  try {
    await ElMessageBox.confirm('确认取消这1张票的退款申请吗？', '取消退款', { type: 'warning' })
    await ticketApi.cancelRefund(orderId.value, [item.id])
    ElMessage.success('已取消该子票退款申请')
    await loadOrderDetail()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '取消退款失败')
  }
}

const handleCancelRefund = async () => {
  ElMessage.info('请在对应子票上点击“取消退款”')
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
.ticket-status.cancelled { color: #909399; background: #f4f4f5; }
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

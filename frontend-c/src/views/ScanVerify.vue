<template>
  <div class="page">
    <header class="topbar">
      <button class="back" aria-label="返回" @click="goBack">‹</button>
      <div class="title">扫码核销</div>
      <div class="placeholder" />
    </header>

    <main class="content">
      <div v-if="!isScanning" class="scan-placeholder">
        <button class="scan-btn" @click="startScan">
          📷 开始扫码
        </button>
        <p class="scan-tip">点击按钮打开摄像头扫描二维码</p>
      </div>
      
      <div v-else class="scanner-container">
        <qrcode-stream @decode="onDecode" @init="onInit" />
        <button @click="stopScan" class="stop-scan-btn">停止扫描</button>
      </div>

      <div class="result" v-if="status !== 'idle'">
        <div class="result-title">核销结果</div>
        <template v-if="status === 'success' && order">
          <div class="text success">✓ 核销成功</div>
          <div class="text">{{ order.exhibition }}</div>
          <div class="text">有效时间：{{ order.validTime }}</div>
          <div class="text">购买账号：{{ order.buyer }}</div>
          <div class="text">订单号码：{{ order.orderNo }}</div>
        </template>
        <template v-else-if="status === 'verified' && order">
          <div class="text warning">⚠ 该订单已核销</div>
          <div class="text">{{ order.exhibition }}</div>
          <div class="text">核销时间：{{ order.verifyTime }}</div>
        </template>
        <template v-else-if="status === 'notfound'">
          <div class="text error">✗ 未查询到订单</div>
        </template>
        <template v-else-if="status === 'error'">
          <div class="text error">✗ {{ errorMsg }}</div>
        </template>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { QrcodeStream } from 'qrcode-reader-vue3'
import { verifyOrder, type OrderRecord } from '@/utils/orders'

const router = useRouter()
const isScanning = ref(false)
const order = ref<OrderRecord | null>(null)
const status = ref<'idle' | 'success' | 'notfound' | 'verified' | 'error'>('idle')
const errorMsg = ref('')

function goBack() {
  router.back()
}

const startScan = () => {
  isScanning.value = true
  status.value = 'idle'
}

const stopScan = () => {
  isScanning.value = false
}

const onDecode = async (result: string) => {
  try {
    stopScan()
    
    // 解析二维码数据，提取订单号
    let orderNo = ''
    try {
      // 尝试解析JSON格式的二维码（A端生成的二维码）
      const data = JSON.parse(result)
      orderNo = data.orderNo || data.order_no || ''
    } catch {
      // 如果不是JSON，可能是纯订单号字符串
      orderNo = result.trim()
    }
    
    // 验证订单号格式（以T开头）
    if (!orderNo || !orderNo.startsWith('T')) {
      status.value = 'error'
      errorMsg.value = '无效的二维码格式'
      return
    }
    
    // 调用核销接口
    // 后端逻辑：查询订单 → 验证状态 → 更新为已使用
    await verifyOrder(orderNo)
    
    // 核销成功，显示结果
    order.value = {
      id: 0,
      orderNo: orderNo,
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
    
    status.value = 'success'
    
    // 3秒后自动重置，准备下一次扫码
    setTimeout(() => {
      status.value = 'idle'
      order.value = null
      startScan()
    }, 3000)
    
  } catch (error: any) {
    console.error('扫码核销失败:', error)
    
    // 根据错误信息判断具体原因
    const errMsg = error.message || error.toString()
    
    // 提取订单号用于显示
    let scannedOrderNo = ''
    try {
      const data = JSON.parse(result)
      scannedOrderNo = data.orderNo || data.order_no || ''
    } catch {
      scannedOrderNo = result.trim()
    }
    
    if (errMsg.includes('订单不存在')) {
      status.value = 'notfound'
      errorMsg.value = '订单不存在'
    } else if (errMsg.includes('已核销过了') || errMsg.includes('已核销')) {
      // 订单状态为2，已经核销过
      status.value = 'verified'
      order.value = {
        id: 0,
        orderNo: scannedOrderNo,
        exhibition: '门票订单',
        validTime: '已核销',
        buyer: '用户',
        verifyTime: '该订单已核销过了',
        status: 2
      }
      errorMsg.value = '该订单已核销过了'
    } else if (errMsg.includes('未支付')) {
      status.value = 'error'
      errorMsg.value = '订单未支付，无法核销'
    } else if (errMsg.includes('已取消')) {
      status.value = 'error'
      errorMsg.value = '订单已取消，无法核销'
    } else {
      status.value = 'error'
      errorMsg.value = errMsg || '核销失败'
    }
    
    // 2秒后自动重置，准备下一次扫码
    setTimeout(() => {
      status.value = 'idle'
      order.value = null
      startScan()
    }, 2000)
  }
}

const onInit = async (promise: Promise<any>) => {
  try {
    await promise
  } catch (error: any) {
    if (error.name === 'NotAllowedError') {
      errorMsg.value = '需要摄像头权限才能扫码'
    } else if (error.name === 'NotFoundError') {
      errorMsg.value = '未检测到摄像头'
    } else {
      errorMsg.value = '摄像头初始化失败'
    }
    status.value = 'error'
    stopScan()
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

.scan-placeholder {
  text-align: center;
  padding: 60px 20px;
}

.scan-btn {
  padding: 20px 40px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 24px;
  cursor: pointer;
  margin-bottom: 20px;
}

.scan-tip {
  color: #999;
  font-size: 18px;
}

.scanner-container {
  width: 100%;
  position: relative;
}

.scanner-container :deep(video) {
  width: 100%;
  border-radius: 12px;
}

.stop-scan-btn {
  margin-top: 16px;
  width: 100%;
  padding: 16px;
  background-color: #f56c6c;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 20px;
  cursor: pointer;
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
</style>

<template>
  <div class="wechat-callback">
    <div class="callback-box">
      <div class="spinner" v-if="loading"></div>
      <p class="status-text">{{ statusText }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { paymentApi } from '@/api/payment'

const router = useRouter()
const loading = ref(true)
const statusText = ref('正在获取支付信息...')

onMounted(async () => {
  const params = new URLSearchParams(window.location.search)
  const code = params.get('code')
  const orderNo = params.get('orderNo') || params.get('state')

  if (!code || !orderNo) {
    statusText.value = '参数错误，正在返回...'
    loading.value = false
    setTimeout(() => router.push('/profile'), 1500)
    return
  }

  try {
    // 用 code 换取 openid
    statusText.value = '正在获取授权信息...'
    const { openid, sub_appid } = await paymentApi.getWechatOpenId(code)

    // 用 openid 发起支付
    statusText.value = '正在创建支付订单...'
    const response = await paymentApi.createWechatPay({
      orderNo,
      subOpenId: openid
    })

    const payUrl = response?.pay_url
    if (!payUrl) throw new Error('获取支付链接失败')

    statusText.value = '正在跳转支付...'
    window.location.href = payUrl
  } catch (error: any) {
    loading.value = false
    statusText.value = '支付创建失败，正在返回...'
    ElMessage.error(error.message || '微信支付失败')
    setTimeout(() => router.push('/profile'), 2000)
  }
})
</script>

<style scoped>
.wechat-callback {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f5f5f5;
}
.callback-box {
  text-align: center;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}
.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e0e0e0;
  border-top-color: #09B81F;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 16px;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.status-text {
  color: #555;
  font-size: 15px;
  margin: 0;
}
</style>

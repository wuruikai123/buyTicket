<template>
  <div class="order-success-container">
    <div class="success-card">
      <!-- 成功图标 -->
      <div class="success-icon">
        <svg viewBox="0 0 52 52" class="checkmark">
          <circle class="checkmark-circle" cx="26" cy="26" r="25" fill="none"/>
          <path class="checkmark-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8"/>
        </svg>
      </div>

      <!-- 成功信息 -->
      <h1 class="success-title">支付成功！</h1>
      <p class="success-subtitle">感谢您的购买</p>

      <!-- 订单信息 -->
      <div class="order-info" v-if="orderInfo.orderNo">
        <div class="info-row">
          <span class="label">订单号：</span>
          <span class="value">{{ orderInfo.orderNo }}</span>
        </div>
        <div class="info-row" v-if="orderInfo.tradeNo">
          <span class="label">支付宝交易号：</span>
          <span class="value">{{ orderInfo.tradeNo }}</span>
        </div>
        <div class="info-row" v-if="orderInfo.totalAmount">
          <span class="label">支付金额：</span>
          <span class="value amount">¥{{ orderInfo.totalAmount }}</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <button class="btn btn-primary" @click="goToOrderDetail">查看订单详情</button>
        <button class="btn btn-secondary" @click="goToHome">返回首页</button>
      </div>

      <!-- 提示信息 -->
      <div class="tips">
        <p>您可以在"个人中心 - 我的订单"中查看订单详情</p>
        <p>门票将在展览当天使用订单号进行核销</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ticketApi } from '@/api/ticket';
import { mallApi } from '@/api/mall';

const route = useRoute();
const router = useRouter();

const orderInfo = ref({
  orderNo: '',
  tradeNo: '',
  totalAmount: '',
  orderId: 0,
  orderType: 'ticket'
});

const loading = ref(false);

onMounted(async () => {
  // 检查Token是否存在
  const token = localStorage.getItem('token');
  if (!token) {
    ElMessage.warning('登录已过期，请重新登录查看订单');
    // 保存当前URL参数，登录后可以返回
    const queryParams = route.query;
    if (queryParams.out_trade_no) {
      sessionStorage.setItem('pendingOrderNo', queryParams.out_trade_no as string);
    }
    // 延迟跳转到登录页
    setTimeout(() => {
      router.push({
        name: 'Login',
        query: { redirect: '/order-success', ...queryParams }
      });
    }, 2000);
    return;
  }

  // 从URL参数中获取支付宝返回的信息
  const queryParams = route.query;
  
  orderInfo.value = {
    orderNo: (queryParams.out_trade_no as string) || (queryParams.orderNo as string) || '',
    tradeNo: (queryParams.trade_no as string) || (queryParams.tradeNo as string) || '',
    totalAmount: (queryParams.total_amount as string) || (queryParams.totalAmount as string) || '',
    orderId: 0,
    orderType: 'ticket'
  };

  // 如果没有订单号，检查sessionStorage
  if (!orderInfo.value.orderNo) {
    const pendingOrderNo = sessionStorage.getItem('pendingOrderNo');
    if (pendingOrderNo) {
      orderInfo.value.orderNo = pendingOrderNo;
      sessionStorage.removeItem('pendingOrderNo');
    }
  }

  // 如果没有订单号，可能是直接访问，显示提示
  if (!orderInfo.value.orderNo) {
    return;
  }

  // 自动跳转到订单详情页面
  await autoRedirectToOrderDetail();
});

// 根据订单号查询订单详情并跳转
const autoRedirectToOrderDetail = async () => {
  loading.value = true;
  try {
    // 先尝试查询门票订单
    try {
      const ticketOrder = await ticketApi.getOrderByOrderNo(orderInfo.value.orderNo);
      
      if (ticketOrder && ticketOrder.id) {
        orderInfo.value.orderId = ticketOrder.id;
        orderInfo.value.orderType = 'ticket';
        
        // 延迟1.5秒后跳转，让用户看到成功页面
        setTimeout(() => {
          router.push({
            name: 'OrderDetail',
            params: { id: String(ticketOrder.id) },
            query: { type: 'ticket' }
          });
        }, 1500);
        return;
      }
    } catch (e) {
      // 门票订单查询失败，尝试查询商城订单
    }

    // 尝试查询商城订单
    try {
      const mallOrder = await mallApi.getOrderByOrderNo(orderInfo.value.orderNo);
      
      if (mallOrder && mallOrder.id) {
        orderInfo.value.orderId = mallOrder.id;
        orderInfo.value.orderType = 'mall';
        
        // 延迟1.5秒后跳转，让用户看到成功页面
        setTimeout(() => {
          router.push({
            name: 'OrderDetail',
            params: { id: String(mallOrder.id) },
            query: { type: 'mall' }
          });
        }, 1500);
        return;
      }
    } catch (e) {
      // 商城订单查询失败
    }

    // 如果都没找到，显示错误信息
    ElMessage.warning('无法找到对应的订单，请稍后手动查看');
  } catch (error) {
    ElMessage.warning('订单查询失败，请稍后手动查看');
  } finally {
    loading.value = false;
  }
};

const goToOrderDetail = () => {
  if (orderInfo.value.orderId) {
    router.push({
      name: 'OrderDetail',
      params: { id: String(orderInfo.value.orderId) },
      query: { type: orderInfo.value.orderType }
    });
  } else if (orderInfo.value.orderNo) {
    // 如果没有订单ID，尝试通过订单号查询
    autoRedirectToOrderDetail();
  } else {
    router.push('/profile');
  }
};

const goToHome = () => {
  router.push('/');
};
</script>

<style scoped>
.order-success-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.success-card {
  background: white;
  border-radius: 20px;
  padding: 60px 40px;
  max-width: 500px;
  width: 100%;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

/* 成功图标动画 */
.success-icon {
  margin: 0 auto 30px;
  width: 80px;
  height: 80px;
}

.checkmark {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: block;
  stroke-width: 2;
  stroke: #4CAF50;
  stroke-miterlimit: 10;
  box-shadow: inset 0px 0px 0px #4CAF50;
  animation: fill 0.4s ease-in-out 0.4s forwards, scale 0.3s ease-in-out 0.9s both;
}

.checkmark-circle {
  stroke-dasharray: 166;
  stroke-dashoffset: 166;
  stroke-width: 2;
  stroke-miterlimit: 10;
  stroke: #4CAF50;
  fill: none;
  animation: stroke 0.6s cubic-bezier(0.65, 0, 0.45, 1) forwards;
}

.checkmark-check {
  transform-origin: 50% 50%;
  stroke-dasharray: 48;
  stroke-dashoffset: 48;
  animation: stroke 0.3s cubic-bezier(0.65, 0, 0.45, 1) 0.8s forwards;
}

@keyframes stroke {
  100% {
    stroke-dashoffset: 0;
  }
}

@keyframes scale {
  0%, 100% {
    transform: none;
  }
  50% {
    transform: scale3d(1.1, 1.1, 1);
  }
}

@keyframes fill {
  100% {
    box-shadow: inset 0px 0px 0px 30px #4CAF50;
  }
}

/* 文字样式 */
.success-title {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.success-subtitle {
  font-size: 16px;
  color: #666;
  margin-bottom: 40px;
}

/* 订单信息 */
.order-info {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 30px;
  text-align: left;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e9ecef;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  color: #666;
  font-size: 14px;
}

.info-row .value {
  color: #333;
  font-size: 14px;
  font-weight: 500;
  word-break: break-all;
}

.info-row .amount {
  color: #4CAF50;
  font-size: 18px;
  font-weight: bold;
}

/* 按钮 */
.action-buttons {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

.btn {
  flex: 1;
  padding: 14px 24px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-secondary:hover {
  background: #f8f9fa;
}

/* 提示信息 */
.tips {
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
}

.tips p {
  color: #999;
  font-size: 13px;
  line-height: 1.8;
  margin: 5px 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .success-card {
    padding: 40px 24px;
  }

  .success-title {
    font-size: 24px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
}
</style>

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

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const orderInfo = ref({
  orderNo: '',
  tradeNo: '',
  totalAmount: ''
});

onMounted(() => {
  // 从URL参数中获取支付宝返回的信息
  orderInfo.value = {
    orderNo: route.query.out_trade_no || '',
    tradeNo: route.query.trade_no || '',
    totalAmount: route.query.total_amount || ''
  };

  // 如果没有订单信息，可能是直接访问，跳转到首页
  if (!orderInfo.value.orderNo) {
    console.warn('没有订单信息，可能是直接访问');
  }
});

const goToOrderDetail = () => {
  if (orderInfo.value.orderNo) {
    router.push(`/order/${orderInfo.value.orderNo}`);
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

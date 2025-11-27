<template>
    <div class="ticket-info">
        <!-- 顶部导航栏 -->
        <div class="header">
            <button class="back-button" @click="goBack">
                <el-icon><ArrowLeft /></el-icon>
            </button>
            <h1 class="page-title">{{ exhibition.name }}</h1>
            <div class="header-placeholder"></div>
        </div>

        <!-- 联系人信息 -->
        <div class="contact-section">
            <div class="contact-item">
                <label class="contact-label">购票联系人姓名</label>
                <input 
                    v-model="contactName" 
                    type="text" 
                    class="contact-input" 
                    placeholder="请输入姓名"
                />
            </div>
            <div class="contact-item">
                <label class="contact-label">联系电话</label>
                <input 
                    v-model="contactPhone" 
                    type="tel" 
                    class="contact-input" 
                    placeholder="请输入电话"
                />
            </div>
        </div>

        <!-- 票务选择区域 -->
        <div class="tickets-section">
            <div 
                v-for="(ticket, index) in selectedTickets" 
                :key="index"
                class="ticket-item"
            >
                <div class="ticket-date">{{ ticket.dateTime }}</div>
                <div class="ticket-quantity">
                    <button 
                        class="quantity-btn minus" 
                        @click="decreaseQuantity(index)"
                        :disabled="ticket.quantity <= 1"
                    >
                        <el-icon><Minus /></el-icon>
                    </button>
                    <span class="quantity-number">{{ ticket.quantity }}</span>
                    <button 
                        class="quantity-btn plus" 
                        @click="increaseQuantity(index)"
                    >
                        <el-icon><Plus /></el-icon>
                    </button>
                </div>
                <div class="ticket-price">¥{{ ticket.totalPrice }}</div>
            </div>

            <!-- 添加更多票务 -->
            <div class="add-ticket" @click="addMoreTickets">
                <el-icon class="add-icon"><Plus /></el-icon>
            </div>
        </div>

        <!-- 服务协议 -->
        <div class="agreement-section">
            <h3 class="agreement-title">服务协议</h3>
            <div class="agreement-content">
                <p v-for="(line, index) in agreementLines" :key="index">{{ line }}</p>
            </div>
        </div>

        <!-- 底部支付栏 -->
        <div class="payment-bar">
            <div class="total-info">
                <span class="total-label">合计:</span>
                <span class="total-amount">¥{{ totalAmount }}元</span>
            </div>
            <button class="pay-button" @click="handlePayment">支付</button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ArrowLeft, Plus, Minus } from '@element-plus/icons-vue';

interface Ticket {
    dateTime: string;
    quantity: number;
    unitPrice: number;
    totalPrice: number;
}

const router = useRouter();
const route = useRoute();

// 展览信息
const exhibition = ref({
    id: 0,
    name: 'XXXXXXXXXX展'
});

// 联系人信息
const contactName = ref('');
const contactPhone = ref('');

// 选中的票务列表
const selectedTickets = ref<Ticket[]>([
    {
        dateTime: '2025年10月11日 12:00-14:00',
        quantity: 2,
        unitPrice: 150,
        totalPrice: 300
    },
    {
        dateTime: '2025年10月12日 08:00-10:00',
        quantity: 1,
        unitPrice: 150,
        totalPrice: 150
    }
]);

// 服务协议内容（假数据）
const agreementLines = ref([
    'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
    'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
    'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
    'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
    'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
    'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
    'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
    'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
    'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
    'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX'
]);

// 计算总金额
const totalAmount = computed(() => {
    return selectedTickets.value.reduce((sum, ticket) => sum + ticket.totalPrice, 0);
});

// 减少数量
const decreaseQuantity = (index: number) => {
    const ticket = selectedTickets.value[index];
    if (ticket && ticket.quantity > 1) {
        ticket.quantity--;
        ticket.totalPrice = ticket.quantity * ticket.unitPrice;
    }
};

// 增加数量
const increaseQuantity = (index: number) => {
    const ticket = selectedTickets.value[index];
    if (ticket) {
        ticket.quantity++;
        ticket.totalPrice = ticket.quantity * ticket.unitPrice;
    }
};

// 添加更多票务
const addMoreTickets = () => {
    // TODO: 可以跳转回日期选择页面或打开选择器
    router.push(`/date-choose/${exhibition.value.id}`);
};

// 返回上一页
const goBack = () => {
    router.back();
};

// 处理支付
const handlePayment = () => {
    if (!contactName.value || !contactPhone.value) {
        alert('请填写联系人信息');
        return;
    }
    
    // TODO: 跳转到支付页面
    console.log('支付信息:', {
        contactName: contactName.value,
        contactPhone: contactPhone.value,
        tickets: selectedTickets.value,
        totalAmount: totalAmount.value
    });
    
    // router.push(`/payment/${exhibition.value.id}`);
};

// 从路由参数获取数据
onMounted(() => {
    const exhibitionId = route.params.id as string;
    if (exhibitionId) {
        exhibition.value.id = parseInt(exhibitionId);
        loadExhibitionData(parseInt(exhibitionId));
    }
    
    // TODO: 从路由参数或状态管理获取选中的日期和时间段
    // 这里使用假数据
});

// 加载展览数据
const loadExhibitionData = (id: number) => {
    const mockData: Record<number, { name: string }> = {
        1: { name: 'XXXXXXXXXX展' },
        2: { name: '印象派大师作品展' },
        3: { name: '现代雕塑艺术展' }
    };
    
    if (mockData[id]) {
        exhibition.value.name = mockData[id].name;
    }
};
</script>

<style scoped>
.ticket-info {
    min-height: 100vh;
    background-color: #f5f5f5;
}

/* 顶部导航栏 */
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
    padding: 0;
    border-radius: 50%;
    transition: background-color 0.3s ease;
}

.back-button:hover {
    background-color: #f0f0f0;
}

.back-button .el-icon {
    font-size: 24px;
}

.page-title {
    font-size: 18px;
    font-weight: bold;
    color: #333;
    margin: 0;
    flex: 1;
    text-align: center;
}

.header-placeholder {
    width: 40px;
}

/* 联系人信息 */
.contact-section {
    display: flex;
    gap: 12px;
    padding: 20px;
    background-color: white;
    margin-bottom: 12px;
}

.contact-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.contact-label {
    font-size: 14px;
    color: #666;
}

.contact-input {
    padding: 12px;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
    font-size: 14px;
    color: #333;
    background-color: white;
    outline: none;
    transition: border-color 0.3s ease;
}

.contact-input:focus {
    border-color: #409eff;
}

.contact-input::placeholder {
    color: #999;
}

/* 票务选择区域 */
.tickets-section {
    background-color: white;
    padding: 20px;
    margin-bottom: 12px;
}

.ticket-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
}

.ticket-item:last-of-type {
    border-bottom: none;
}

.ticket-date {
    flex: 1;
    font-size: 14px;
    color: #333;
    min-width: 0;
}

.ticket-quantity {
    display: flex;
    align-items: center;
    gap: 12px;
}

.quantity-btn {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    border: 1px solid #e0e0e0;
    background-color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s ease;
    padding: 0;
}

.quantity-btn:hover:not(:disabled) {
    background-color: #f0f0f0;
    border-color: #d0d0d0;
}

.quantity-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.quantity-btn .el-icon {
    font-size: 16px;
    color: #666;
}

.quantity-number {
    font-size: 16px;
    color: #333;
    min-width: 24px;
    text-align: center;
}

.ticket-price {
    font-size: 16px;
    color: #333;
    font-weight: 500;
    min-width: 60px;
    text-align: right;
}

/* 添加更多票务 */
.add-ticket {
    margin-top: 16px;
    padding: 40px;
    border: 2px dashed #d0d0d0;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s ease;
    background-color: #fafafa;
}

.add-ticket:hover {
    border-color: #409eff;
    background-color: #f0f7ff;
}

.add-icon {
    font-size: 32px;
    color: #999;
}

.add-ticket:hover .add-icon {
    color: #409eff;
}

/* 服务协议 */
.agreement-section {
    background-color: white;
    padding: 20px;
    margin-bottom: 12px;
}

.agreement-title {
    font-size: 18px;
    font-weight: bold;
    color: #333;
    margin: 0 0 16px 0;
    text-align: center;
}

.agreement-content {
    font-size: 14px;
    color: #666;
    line-height: 1.8;
}

.agreement-content p {
    margin: 0 0 12px 0;
}

/* 底部支付栏 */
.payment-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    background-color: white;
    border-top: 1px solid #e0e0e0;
    z-index: 1000;
    box-sizing: border-box;
}

.total-info {
    display: flex;
    align-items: baseline;
    gap: 8px;
}

.total-label {
    font-size: 16px;
    color: #333;
}

.total-amount {
    font-size: 20px;
    font-weight: bold;
    color: #f56c6c;
}

.pay-button {
    padding: 12px 32px;
    background-color: #e8e8e8;
    color: #333;
    border: none;
    border-radius: 6px;
    font-size: 16px;
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.3s ease;
    white-space: nowrap;
}

.pay-button:hover {
    background-color: #d8d8d8;
}

.pay-button:active {
    background-color: #c8c8c8;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .contact-section {
        flex-direction: column;
        padding: 16px;
    }

    .tickets-section {
        padding: 16px;
    }

    .ticket-item {
        flex-wrap: wrap;
        gap: 12px;
    }

    .ticket-date {
        width: 100%;
    }

    .agreement-section {
        padding: 16px;
    }

    .payment-bar {
        padding: 12px 16px;
    }

    .total-amount {
        font-size: 18px;
    }

    .pay-button {
        padding: 10px 24px;
        font-size: 15px;
    }
}
</style>


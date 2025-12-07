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
import { ticketApi } from '@/api/ticket';
import { exhibitionApi } from '@/api/exhibition';

interface Ticket {
    date: string;
    timeSlot: string;
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
    name: '',
    price: 0
});

// 联系人信息
const contactName = ref('');
const contactPhone = ref('');

// 选中的票务列表
const selectedTickets = ref<Ticket[]>([]);

// 服务协议内容
const agreementLines = ref([
    '1. 购票须知：所有门票一经售出，非不可抗力因素不退不换。',
    '2. 入场要求：请携带本人身份证原件，凭电子票二维码扫码入场。',
    '3. 禁止携带：严禁携带易燃易爆物品、管制刀具等危险品入场。',
    '4. 知识产权：展馆内部分区域禁止拍照，请留意现场标识。',
    '5. 儿童说明：1.2米以下儿童免票，需由成人陪同。'
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



// 返回上一页
const goBack = () => {
    router.back();
};

// 处理支付
const handlePayment = async () => {
    if (!contactName.value || !contactPhone.value) {
        alert('请填写联系人信息');
        return;
    }

    if (selectedTickets.value.length === 0) {
        alert('请选择票务');
        return;
    }
    
    try {
        // 1. 先检查每个时间段的票数是否足够
        for (const ticket of selectedTickets.value) {
            const availability = await ticketApi.getAvailability({
                exhibitionId: exhibition.value.id,
                date: ticket.date,
                timeSlot: ticket.timeSlot
            });
            
            if (!availability || availability.remainingCount < ticket.quantity) {
                alert(`${ticket.dateTime} 票数不足！剩余 ${availability?.remainingCount || 0} 张，您选择了 ${ticket.quantity} 张`);
                return;
            }
        }

        // 2. 创建订单
        const items = selectedTickets.value.map(t => ({
            date: t.date,
            timeSlot: t.timeSlot,
            quantity: t.quantity,
            unitPrice: t.unitPrice
        }));

        const createRes = await ticketApi.createOrder({
            exhibitionId: exhibition.value.id,
            contactName: contactName.value,
            contactPhone: contactPhone.value,
            totalAmount: totalAmount.value,
            items: items
        });

        if (createRes && createRes.orderId) {
            // 3. 模拟支付（写死支付成功）
            await ticketApi.pay({
                orderId: createRes.orderId,
                type: 'ticket',
                password: '123456' // 默认支付密码
            });
            
            alert('支付成功！');
            router.push('/profile');
        }
    } catch (e: any) {
        console.error(e);
        alert(e.message || '支付失败，请重试');
    }
};

// 加载展览详情
const loadExhibition = async (id: number) => {
    try {
        const data = await exhibitionApi.getDetail(id);
        if (data) {
            exhibition.value.id = data.id;
            exhibition.value.name = data.name;
            exhibition.value.price = data.price || 0;
            
            // 初始化第一张票 (如果有参数)
            const date = route.query.date as string;
            const timeSlot = route.query.timeSlot as string;
            
            if (date && timeSlot) {
                const price = data.price || 0;
                selectedTickets.value = [{
                    date,
                    timeSlot,
                    dateTime: `${date} ${timeSlot}`,
                    quantity: 1,
                    unitPrice: price,
                    totalPrice: price
                }];
            }
        }
    } catch (e) {
        console.error(e);
    }
};

onMounted(() => {
    const exhibitionId = route.params.id as string;
    if (exhibitionId) {
        const id = parseInt(exhibitionId);
        loadExhibition(id);
    }
});
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


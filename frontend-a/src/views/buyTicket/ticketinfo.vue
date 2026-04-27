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
                <label class="contact-label">联系人姓名</label>
                <input 
                    v-model="contactName" 
                    type="text" 
                    class="contact-input" 
                    placeholder="请输入联系人姓名"
                />
            </div>
            <div class="contact-item">
                <label class="contact-label">联系人手机号</label>
                <input 
                    v-model="contactPhone" 
                    type="tel" 
                    class="contact-input" 
                    placeholder="请输入联系人手机号"
                    maxlength="11"
                />
            </div>
        </div>

        <!-- 购票者信息 -->
        <div class="buyers-section">
            <div class="section-header">
                <h3 class="section-title">购票者信息</h3>
                <span class="section-badge">每张票填写一份，最多6张</span>
            </div>

            <div class="buyers-summary">
                <div class="summary-item">
                    <span class="summary-label">已选票数</span>
                    <span class="summary-value">{{ totalTicketCount }} 张</span>
                </div>
                <div class="summary-divider"></div>
                <div class="summary-item">
                    <span class="summary-label">待填写</span>
                    <span class="summary-value">{{ buyers.length }} 份</span>
                </div>
            </div>

            <div v-for="(buyer, index) in buyers" :key="index" class="buyer-card">
                <div class="buyer-card-header">
                    <div class="buyer-card-title">购票者 {{ index + 1 }}</div>
                    <div class="buyer-card-subtitle">第 {{ index + 1 }} 张票对应信息</div>
                </div>
                <div class="buyer-form-grid">
                    <div class="buyer-field">
                        <label class="buyer-label">姓名</label>
                        <input v-model="buyer.name" type="text" class="buyer-input" placeholder="请输入购票人姓名" />
                    </div>
                    <div class="buyer-field">
                        <label class="buyer-label">身份证号</label>
                        <input v-model="buyer.idCard" type="text" class="buyer-input" placeholder="请输入身份证号" maxlength="18" />
                    </div>
                </div>
            </div>
        </div>

        <!-- 票务选择区域 -->
        <div class="tickets-section">
            <div class="section-header compact">
                <h3 class="section-title">票务明细</h3>
                <span class="section-badge">最多 {{ maxTickets }} 张</span>
            </div>
            <div 
                v-for="(ticket, index) in selectedTickets" 
                :key="index"
                class="ticket-item"
            >
                <div class="ticket-date">{{ ticket.dateTime }}</div>
                <div class="ticket-quantity-fixed">
                    <span class="quantity-label">数量：</span>
                    <div class="quantity-stepper">
                        <button class="stepper-btn" type="button" @click="decreaseTicket(ticket)">-</button>
                        <span class="quantity-number-fixed">{{ ticket.quantity }}张</span>
                        <button class="stepper-btn" type="button" @click="increaseTicket(ticket)">+</button>
                    </div>
                </div>
                <div class="ticket-price">¥{{ ticket.totalPrice }}</div>
            </div>
            <div class="ticket-actions">
                <button class="ticket-action-btn add" type="button" @click="addTicketSlot">添加一张票</button>
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
import { ArrowLeft } from '@element-plus/icons-vue';
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

interface BuyerInfo {
    name: string;
    idCard: string;
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
const buyers = ref<BuyerInfo[]>([]);
const maxTickets = 6;

// 服务协议内容
const agreementLines = ref([
    '1. 购票须知：所有门票一经售出，非不可抗力因素不退不换。',
    '2. 入场要求：请携带本人身份证原件，凭电子票二维码扫码入场。',
    '3. 禁止携带：严禁携带易燃易爆物品、管制刀具等危险品入场。',
    '4. 知识产权：展馆内部分区域禁止拍照，请留意现场标识。'
]);

// 计算总金额
const totalAmount = computed(() => {
    return selectedTickets.value.reduce((sum, ticket) => sum + ticket.totalPrice, 0);
});

const totalTicketCount = computed(() => selectedTickets.value.reduce((sum, ticket) => sum + ticket.quantity, 0));

const syncBuyerRows = () => {
    const count = totalTicketCount.value;
    const next = Array.from({ length: count }, (_, index) => buyers.value[index] || { name: '', idCard: '' });
    buyers.value = next;
};

const addTicketSlot = () => {
    if (totalTicketCount.value >= maxTickets) {
        alert(`单笔订单最多支持${maxTickets}张票`);
        return;
    }
    if (selectedTickets.value.length === 0) {
        alert('请先选择日期和场次');
        return;
    }
    const firstTicket = selectedTickets.value[0];
    selectedTickets.value[0] = {
        ...firstTicket,
        quantity: firstTicket.quantity + 1,
        totalPrice: (firstTicket.quantity + 1) * firstTicket.unitPrice
    };
    syncBuyerRows();
};

const increaseTicket = (ticket: Ticket) => {
    if (totalTicketCount.value >= maxTickets) {
        alert(`单笔订单最多支持${maxTickets}张票`);
        return;
    }
    ticket.quantity += 1;
    ticket.totalPrice = ticket.quantity * ticket.unitPrice;
    syncBuyerRows();
};

const decreaseTicket = (ticket: Ticket) => {
    if (ticket.quantity <= 1) {
        if (selectedTickets.value.length <= 1) {
            alert('至少保留1张票');
            return;
        }
        const index = selectedTickets.value.indexOf(ticket);
        if (index >= 0) {
            selectedTickets.value.splice(index, 1);
            syncBuyerRows();
        }
        return;
    }
    ticket.quantity -= 1;
    ticket.totalPrice = ticket.quantity * ticket.unitPrice;
    syncBuyerRows();
};

// 注意：每张票单独填写购票者信息

// 返回上一页
const goBack = () => {
    router.back();
};

// 验证身份证号码
const validateIdCard = (idCard: string): boolean => {
    // 去除空格
    idCard = idCard.trim();
    
    // 15位或18位身份证号码正则
    const idCardPattern = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/;
    
    // 基本格式验证
    if (!idCardPattern.test(idCard)) {
        return false;
    }
    
    // 18位身份证校验码验证
    if (idCard.length === 18) {
        const factors = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
        const checkCodes = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'];
        
        let sum = 0;
        for (let i = 0; i < 17; i++) {
            sum += parseInt(idCard[i]) * factors[i];
        }
        
        const checkCode = checkCodes[sum % 11];
        const lastChar = idCard[17].toUpperCase();
        
        if (lastChar !== checkCode) {
            return false;
        }
    }
    
    return true;
};

// 处理支付
const handlePayment = async () => {
    if (!contactName.value || !contactPhone.value) {
        alert('请填写联系人信息');
        return;
    }
    
    if (contactName.value.trim().length < 2) {
        alert('请输入正确的姓名');
        return;
    }

    if (!/^1\d{10}$/.test(contactPhone.value.trim())) {
        alert('请输入正确的联系人手机号');
        return;
    }

    if (selectedTickets.value.length === 0) {
        alert('请选择票务');
        return;
    }

    if (totalTicketCount.value > maxTickets) {
        alert('单笔订单最多支持6张票');
        return;
    }

    if (buyers.value.length !== totalTicketCount.value) {
        alert('请为每张票填写一份购票者信息');
        return;
    }

    for (const buyer of buyers.value) {
        if (!buyer.name.trim() || !validateIdCard(buyer.idCard)) {
            alert('请完整填写所有购票者姓名和身份证号');
            return;
        }
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
            items: items,
            buyers: buyers.value
        });

        if (createRes && createRes.orderId) {
            // 3. 跳转到支付页面
            router.push({
                path: `/payment/${createRes.orderId}`,
                query: { type: 'ticket' }
            });
        }
    } catch (e: any) {
        console.error(e);
        alert(e.message || '创建订单失败，请重试');
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
                syncBuyerRows();
            }
            if (selectedTickets.value.length > 0) {
                syncBuyerRows();
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
    padding-bottom: 140px; /* 为底部支付栏和导航栏预留空间 */
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

.section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 14px;
}

.section-header.compact {
    margin-bottom: 12px;
}

.section-title {
    font-size: 18px;
    font-weight: 700;
    color: #1f2937;
    margin: 0;
}

.section-badge {
    font-size: 12px;
    color: #213d7c;
    background: #eef3ff;
    padding: 6px 10px;
    border-radius: 999px;
    white-space: nowrap;
}

.buyers-summary {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 16px;
    background: linear-gradient(135deg, #f7f9fc 0%, #eef3ff 100%);
    border: 1px solid #e5ebf5;
    border-radius: 12px;
    margin-bottom: 16px;
}

.summary-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.summary-label {
    font-size: 13px;
    color: #6b7280;
}

.summary-value {
    font-size: 14px;
    font-weight: 700;
    color: #111827;
}

.summary-divider {
    width: 1px;
    height: 24px;
    background: #d8e1ee;
}

.buyer-card {
    padding: 16px;
    border: 1px solid #e7edf6;
    border-radius: 14px;
    background: #fafcff;
    margin-bottom: 12px;
    box-shadow: 0 2px 10px rgba(33, 61, 124, 0.04);
}

.buyer-card-header {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 14px;
}

.buyer-card-title {
    font-size: 16px;
    font-weight: 700;
    color: #213d7c;
}

.buyer-card-subtitle {
    font-size: 12px;
    color: #6b7280;
}

.buyer-form-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
}

.buyer-field {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.buyer-label {
    font-size: 13px;
    font-weight: 600;
    color: #4b5563;
}

.buyer-input {
    height: 44px;
    padding: 0 14px;
    border: 1px solid #dce5f2;
    border-radius: 10px;
    font-size: 14px;
    color: #111827;
    background-color: white;
    outline: none;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.buyer-input::placeholder {
    color: #9ca3af;
}

.buyer-input:focus {
    border-color: #213d7c;
    box-shadow: 0 0 0 3px rgba(33, 61, 124, 0.12);
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

.ticket-quantity-fixed {
    display: flex;
    align-items: center;
    gap: 8px;
}

.quantity-stepper {
    display: inline-flex;
    align-items: center;
    gap: 8px;
}

.stepper-btn {
    width: 28px;
    height: 28px;
    border: 1px solid #dcdfe6;
    background: #fff;
    color: #213d7c;
    border-radius: 50%;
    cursor: pointer;
    font-size: 18px;
    line-height: 1;
}

.stepper-btn:hover {
    background: #ecf5ff;
    border-color: #213d7c;
}

.ticket-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 12px;
}

.ticket-action-btn {
    padding: 8px 14px;
    border-radius: 8px;
    border: 1px solid #213d7c;
    background: #213d7c;
    color: #fff;
    cursor: pointer;
}

.quantity-label {
    font-size: 14px;
    color: #666;
}

.quantity-number-fixed {
    font-size: 16px;
    color: #333;
    font-weight: 500;
    padding: 6px 12px;
    background-color: #f5f5f5;
    border-radius: 4px;
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
    bottom: 60px; /* 抬高到导航栏上方 */
    left: 0;
    right: 0;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    background-color: white;
    border-top: 1px solid #e0e0e0;
    z-index: 999;
    box-sizing: border-box;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
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
    background-color: #213d7c;
    color: #ffffff;
    border: none;
    border-radius: 6px;
    font-size: 16px;
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.3s ease;
    white-space: nowrap;
}

.pay-button:hover {
    background-color: #1a2f63;
}

.pay-button:active {
    background-color: #152749;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .contact-section {
        flex-direction: column;
        padding: 16px;
    }

    .tickets-section,
    .buyers-section,
    .agreement-section {
        padding: 16px;
    }

    .section-header {
        align-items: flex-start;
        flex-direction: column;
    }

    .buyers-summary {
        flex-direction: column;
        align-items: stretch;
    }

    .summary-divider {
        width: 100%;
        height: 1px;
    }

    .buyer-form-grid {
        grid-template-columns: 1fr;
    }

    .buyer-card-header {
        flex-direction: column;
        align-items: flex-start;
    }

    .ticket-item {
        flex-wrap: wrap;
        gap: 12px;
    }

    .ticket-date {
        width: 100%;
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


<template>
    <div class="ticket-info">
        <div class="header">
            <button class="back-button" @click="goBack">
                <el-icon><ArrowLeft /></el-icon>
            </button>
            <h1 class="page-title">{{ exhibition.name }}</h1>
            <div class="header-placeholder"></div>
        </div>

        <div class="tickets-section" v-if="selectedTicket">
            <div class="ticket-item">
                <div class="ticket-date">{{ selectedTicket.dateTime }}</div>
                <div class="ticket-quantity">
                    <span class="quantity-label">数量：</span>
                    <button class="quantity-btn" @click="changeQuantity(-1)">-</button>
                    <span class="quantity-number">{{ selectedTicket.quantity }}张</span>
                    <button class="quantity-btn" @click="changeQuantity(1)">+</button>
                </div>
                <div class="ticket-price">¥{{ selectedTicket.totalPrice.toFixed(2) }}</div>
            </div>
        </div>

        <div class="contact-section">
            <div class="contact-item">
                <label class="contact-label">联系人姓名</label>
                <input v-model="contactName" type="text" class="contact-input" placeholder="请输入联系人姓名" />
            </div>
            <div class="contact-item">
                <label class="contact-label">联系人手机号</label>
                <input v-model="contactPhone" type="text" class="contact-input" placeholder="请输入联系人手机号" maxlength="11" />
            </div>
        </div>

        <div class="buyers-section" v-if="buyers.length">
            <h3 class="buyers-title">购票人信息（{{ buyers.length }}人）</h3>
            <div class="buyer-card" v-for="(buyer, index) in buyers" :key="index">
                <div class="buyer-index">第{{ index + 1 }}张票</div>
                <div class="buyer-field">
                    <label>姓名</label>
                    <input v-model="buyer.name" type="text" placeholder="请输入真实姓名" />
                </div>
                <div class="buyer-field">
                    <label>身份证号</label>
                    <input v-model="buyer.idCard" type="text" maxlength="18" placeholder="请输入身份证号" />
                </div>
            </div>
        </div>

        <div class="agreement-section">
            <h3 class="agreement-title">服务协议</h3>
            <div class="agreement-content">
                <p v-for="(line, index) in agreementLines" :key="index">{{ line }}</p>
            </div>
        </div>

        <div class="payment-bar">
            <div class="total-info">
                <span class="total-label">合计:</span>
                <span class="total-amount">¥{{ totalAmount.toFixed(2) }}元</span>
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

const exhibition = ref({
    id: 0,
    name: '',
    price: 0
});

const contactName = ref('');
const contactPhone = ref('');

const selectedTicket = ref<Ticket | null>(null);
const buyers = ref<BuyerInfo[]>([]);

const agreementLines = ref([
    '1. 购票须知：所有门票一经售出，非不可抗力因素不退不换。',
    '2. 入场要求：请携带本人身份证原件，凭电子票二维码扫码入场。',
    '3. 禁止携带：严禁携带易燃易爆物品、管制刀具等危险品入场。',
    '4. 知识产权：展馆内部分区域禁止拍照，请留意现场标识。'
]);

const totalAmount = computed(() => selectedTicket.value?.totalPrice || 0);

const goBack = () => router.back();

const validateIdCard = (idCard: string): boolean => {
    idCard = idCard.trim();
    const idCardPattern = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/;
    if (!idCardPattern.test(idCard)) return false;
    if (idCard.length === 18) {
        const factors = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
        const checkCodes = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'];
        let sum = 0;
        for (let i = 0; i < 17; i++) {
            sum += parseInt(idCard[i]) * factors[i];
        }
        return idCard[17].toUpperCase() === checkCodes[sum % 11];
    }
    return true;
};

const ensureBuyers = (count: number) => {
    const next = buyers.value.slice(0, count);
    while (next.length < count) {
        next.push({ name: '', idCard: '' });
    }
    buyers.value = next;
};

const changeQuantity = async (delta: number) => {
    if (!selectedTicket.value) return;
    const nextQty = selectedTicket.value.quantity + delta;
    if (nextQty < 1 || nextQty > 6) return;

    if (delta > 0) {
        const availability = await ticketApi.getAvailability({
            exhibitionId: exhibition.value.id,
            date: selectedTicket.value.date,
            timeSlot: selectedTicket.value.timeSlot
        });
        if (!availability || availability.remainingCount < nextQty) {
            alert(`库存不足，当前仅剩 ${availability?.remainingCount || 0} 张`);
            return;
        }
    }

    selectedTicket.value.quantity = nextQty;
    selectedTicket.value.totalPrice = Number((selectedTicket.value.unitPrice * nextQty).toFixed(2));
    ensureBuyers(nextQty);
};

const handlePayment = async () => {
    if (!selectedTicket.value) {
        alert('请选择票务');
        return;
    }
    if (!contactName.value.trim() || !contactPhone.value.trim()) {
        alert('请填写联系人信息');
        return;
    }
    if (!/^1\d{10}$/.test(contactPhone.value.trim())) {
        alert('请输入正确的联系人手机号');
        return;
    }

    for (let i = 0; i < buyers.value.length; i++) {
        const b = buyers.value[i];
        if (!b.name.trim() || !b.idCard.trim()) {
            alert(`请完善第${i + 1}位购票人信息`);
            return;
        }
        if (!validateIdCard(b.idCard)) {
            alert(`第${i + 1}位购票人身份证号不正确`);
            return;
        }
    }

    try {
        const availability = await ticketApi.getAvailability({
            exhibitionId: exhibition.value.id,
            date: selectedTicket.value.date,
            timeSlot: selectedTicket.value.timeSlot
        });
        if (!availability || availability.remainingCount < selectedTicket.value.quantity) {
            alert(`票数不足！剩余 ${availability?.remainingCount || 0} 张`);
            return;
        }

        const createRes = await ticketApi.createOrder({
            exhibitionId: exhibition.value.id,
            contactName: contactName.value.trim(),
            contactPhone: contactPhone.value.trim(),
            totalAmount: totalAmount.value,
            items: [{
                date: selectedTicket.value.date,
                timeSlot: selectedTicket.value.timeSlot,
                quantity: selectedTicket.value.quantity,
                unitPrice: selectedTicket.value.unitPrice
            }],
            buyers: buyers.value.map(item => ({
                name: item.name.trim(),
                idCard: item.idCard.trim().toUpperCase()
            }))
        });

        if (createRes && createRes.orderId) {
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

const loadExhibition = async (id: number) => {
    try {
        const data = await exhibitionApi.getDetail(id);
        if (!data) return;
        exhibition.value = {
            id: data.id,
            name: data.name,
            price: data.price || 0
        };

        const date = route.query.date as string;
        const timeSlot = route.query.timeSlot as string;
        if (date && timeSlot) {
            selectedTicket.value = {
                date,
                timeSlot,
                dateTime: `${date} ${timeSlot}`,
                quantity: 1,
                unitPrice: data.price || 0,
                totalPrice: data.price || 0
            };
            ensureBuyers(1);
        }
    } catch (e) {
        console.error(e);
    }
};

onMounted(() => {
    const exhibitionId = route.params.id as string;
    if (exhibitionId) {
        loadExhibition(parseInt(exhibitionId));
    }
});
</script>

<style scoped>
.ticket-info { min-height: 100vh; background: #f5f5f5; padding-bottom: 140px; }
.header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; background: #fff; position: sticky; top: 0; z-index: 100; border-bottom: 1px solid #e0e0e0; }
.back-button { display: flex; align-items: center; justify-content: center; width: 40px; height: 40px; background: none; border: none; cursor: pointer; color: #333; border-radius: 50%; }
.page-title { font-size: 18px; font-weight: bold; color: #333; margin: 0; flex: 1; text-align: center; }
.header-placeholder { width: 40px; }

.tickets-section, .contact-section, .buyers-section, .agreement-section { background: #fff; padding: 20px; margin-bottom: 12px; }
.ticket-item { display: flex; align-items: center; gap: 16px; }
.ticket-date { flex: 1; font-size: 14px; color: #333; }
.ticket-quantity { display: flex; align-items: center; gap: 8px; }
.quantity-btn { width: 28px; height: 28px; border: 1px solid #ddd; background: #fff; border-radius: 4px; cursor: pointer; }
.quantity-number { min-width: 52px; text-align: center; font-weight: 600; }
.ticket-price { font-weight: 600; min-width: 80px; text-align: right; }

.contact-section { display: flex; gap: 12px; }
.contact-item { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.contact-label { font-size: 14px; color: #666; }
.contact-input, .buyer-field input { padding: 12px; border: 1px solid #e0e0e0; border-radius: 4px; font-size: 14px; }

.buyers-title { margin: 0 0 12px 0; }
.buyer-card { border: 1px solid #eee; border-radius: 8px; padding: 12px; margin-bottom: 10px; }
.buyer-index { font-weight: 600; margin-bottom: 10px; }
.buyer-field { display: flex; flex-direction: column; gap: 6px; margin-bottom: 10px; }

.agreement-title { font-size: 18px; font-weight: bold; margin: 0 0 16px; text-align: center; }
.agreement-content { font-size: 14px; color: #666; line-height: 1.8; }
.agreement-content p { margin: 0 0 12px; }

.payment-bar { position: fixed; bottom: 60px; left: 0; right: 0; display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; background: #fff; border-top: 1px solid #e0e0e0; z-index: 999; box-shadow: 0 -2px 8px rgba(0,0,0,.1); }
.total-amount { font-size: 20px; font-weight: bold; color: #f56c6c; }
.pay-button { padding: 12px 32px; background: #213d7c; color: #fff; border: none; border-radius: 6px; font-size: 16px; cursor: pointer; }

@media (max-width: 768px) {
  .contact-section { flex-direction: column; padding: 16px; }
  .tickets-section, .buyers-section, .agreement-section { padding: 16px; }
  .payment-bar { padding: 12px 16px; }
}
</style>

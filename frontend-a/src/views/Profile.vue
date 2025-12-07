<template>
    <div class="profile">
        <!-- 用户信息头部 -->
        <div class="profile-header">
            <div class="user-info">
                <div class="avatar" :style="{ backgroundImage: isLoggedIn && userInfo.avatar ? `url(${userInfo.avatar})` : '', backgroundSize: 'cover' }">
                    <span v-if="!isLoggedIn" class="avatar-placeholder">?</span>
                </div>
                <div class="user-details">
                    <h2 class="username">{{ isLoggedIn ? userInfo.username : '未登录' }}</h2>
                    <p class="uid">UID: {{ isLoggedIn ? userInfo.uid : '---' }}</p>
                </div>
            </div>
            <div class="action-buttons">
                <!-- 未登录状态：显示登录和注册按钮 -->
                <template v-if="!isLoggedIn">
                    <button class="action-btn login-action-btn" @click="goToLogin">
                        <span>登录</span>
                    </button>
                    <button class="action-btn register-action-btn" @click="goToRegister">
                        <span>注册</span>
                    </button>
                </template>
                <!-- 已登录状态：显示地址簿和修改信息按钮 -->
                <template v-else>
                    <button class="action-btn" @click="goToAddressBook">
                        <el-icon><Location /></el-icon>
                        <span>地址簿</span>
                    </button>
                    <button class="action-btn" @click="goToEditProfile">
                        <el-icon><EditPen /></el-icon>
                        <span>修改信息</span>
                    </button>
                </template>
            </div>
        </div>

        <!-- 分隔线 -->
        <div class="divider"></div>

        <!-- 标签页 -->
        <div class="tabs">
            <div 
                class="tab-item" 
                :class="{ active: isTicketsActive, disabled: !isLoggedIn }"
                @click="isLoggedIn && switchTab('tickets')"
            >
                我的门票订单
            </div>
            <div 
                class="tab-item" 
                :class="{ active: isMallActive, disabled: !isLoggedIn }"
                @click="isLoggedIn && switchTab('mall')"
            >
                我的商城订单
            </div>
        </div>

        <!-- 排序和筛选 -->
        <div class="filter-bar">
            <div class="sort-options">
                <span class="sort-label">排序</span>
                <span class="sort-separator">|</span>
                <span class="sort-value">开展时间最近优先</span>
            </div>
            <div class="filter-toggle">
                <span>仅展示未核销的</span>
            </div>
        </div>

        <!-- 订单列表 -->
        <div class="order-list">
            <!-- 未登录时显示提示 -->
            <div v-if="!isLoggedIn" class="empty-state">
                <p class="empty-text">请先登录以查看订单信息</p>
            </div>
            <!-- 已登录时显示订单 -->
            <template v-else>
                <div 
                    v-for="order in currentOrders" 
                    :key="order.id" 
                    class="order-card"
                    @click="goToOrderDetail(order)"
                >
                    <div class="order-image" :style="{ backgroundImage: order.coverImage ? `url(${order.coverImage})` : '' }"></div>
                    <div class="order-content">
                        <h3 class="order-title">{{ order.title }}</h3>
                        <p class="order-time">时间: {{ order.time }}</p>
                        <p class="order-price">¥{{ order.price }}元</p>
                    </div>
                    <div class="ticket-edge"></div>
                    <div class="order-action">
                        <button 
                            class="use-button" 
                            :class="{ 'use-button-active': order.status === 1 }"
                            @click.stop="handleOrderAction(order)"
                        >{{ order.statusText }}</button>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeMount, onActivated } from 'vue';
import { useRouter } from 'vue-router';
import { Location, EditPen } from '@element-plus/icons-vue';
import { userApi, type User } from '@/api/user';
import { ticketApi } from '@/api/ticket';
import { mallApi } from '@/api/mall';

// 定义订单数据类型
interface Order {
    id: number;
    title: string;
    time: string;
    price: number;
    status: number;
    statusText: string;
    coverImage?: string;
}

const router = useRouter();

// 检查登录状态
const isLoggedIn = ref(false);

// 检查是否有 token
const checkLoginStatus = () => {
    const token = localStorage.getItem('token');
    isLoggedIn.value = !!token;
};

// 用户信息
const userInfo = reactive<User>({
    id: 0,
    username: '张三',
    uid: '---',
    avatar: '',
    balance: 0
});

// 当前激活的标签页
const activeTab = ref<'tickets' | 'mall'>('tickets');

// 切换标签页
const switchTab = (tab: 'tickets' | 'mall') => {
    activeTab.value = tab;
};

// 计算属性用于类型检查
const isTicketsActive = computed(() => activeTab.value === 'tickets');
const isMallActive = computed(() => activeTab.value === 'mall');

// 订单列表
const ticketOrders = ref<Order[]>([]);
const mallOrders = ref<Order[]>([]);

// 当前显示的列表
const currentOrders = computed(() => isTicketsActive.value ? ticketOrders.value : mallOrders.value);

// 跳转到登录页面
const goToLogin = () => {
    router.push('/login');
};

// 跳转到注册页面（登录页面可以切换模式）
const goToRegister = () => {
    router.push('/login');
};

// 跳转到地址簿
const goToAddressBook = () => {
    router.push('/address-book');
};

// 跳转到修改信息
const goToEditProfile = () => {
    router.push('/edit-profile');
};

// 跳转到订单详情
const goToOrderDetail = (order: Order) => {
    const type = isTicketsActive.value ? 'ticket' : 'mall';
    router.push(`/order/${order.id}?type=${type}`);
};

// 处理订单操作（核销）
const handleOrderAction = async (order: Order) => {
    // 只有待使用状态(status=1)的门票订单可以核销
    if (isTicketsActive.value && order.status === 1) {
        if (confirm('确认核销此门票？核销后将无法撤销。')) {
            try {
                await ticketApi.verifyOrder(order.id);
                alert('核销成功！');
                // 更新订单状态
                order.status = 2;
                order.statusText = '已使用';
            } catch (e: any) {
                alert(e.message || '核销失败');
            }
        }
    }
};

// 加载用户数据和订单
const loadUserData = async () => {
    if (!isLoggedIn.value) {
        return;
    }

    try {
        // 用户信息
        const user = await userApi.getUserInfo();
        if (user) {
            Object.assign(userInfo, user);
        }

        // 门票订单
        const tOrders = await ticketApi.getOrderList();
        if (tOrders) {
            ticketOrders.value = tOrders.map((o: any) => ({
                id: o.id,
                title: o.exhibitionName || o.contactName ? `${o.contactName}的门票订单` : '门票订单',
                time: o.createTime,
                price: o.totalAmount,
                status: o.status,
                statusText: getStatusText(o.status),
                coverImage: o.coverImage
            }));
        }

        // 商城订单
        const mOrders = await mallApi.getOrderList();
        if (mOrders) {
            mallOrders.value = mOrders.map((o: any) => ({
                id: o.id,
                title: o.title || '商城购物订单',
                time: o.createTime,
                price: o.totalAmount,
                status: o.status,
                statusText: getMallStatusText(o.status),
                coverImage: o.coverImage
            }));
        }
    } catch (e) {
        console.error('获取数据失败', e);
    }
};

// 在组件挂载前检查登录状态
onBeforeMount(() => {
    checkLoginStatus();
});

// 获取用户信息和订单
onMounted(async () => {
    await loadUserData();
});

// 当组件被激活时（从其他页面返回时）重新检查登录状态
onActivated(() => {
    const wasLoggedIn = isLoggedIn.value;
    checkLoginStatus();
    // 如果之前未登录，现在已登录，则加载数据
    if (!wasLoggedIn && isLoggedIn.value) {
        loadUserData();
    }
});

const getStatusText = (status: number) => {
    const map: Record<number, string> = {
        0: '待支付',
        1: '待使用',
        2: '已使用',
        3: '已取消'
    };
    return map[status] || '未知';
};

const getMallStatusText = (status: number) => {
    const map: Record<number, string> = {
        0: '待支付',
        1: '待发货',
        2: '已发货',
        3: '已完成',
        4: '已取消'
    };
    return map[status] || '未知';
};
</script>

<style scoped>
.profile {
    min-height: 100vh;
    background-color: #f5f5f5;
    padding-bottom: 20px;
}

/* 用户信息头部 */
.profile-header {
    background-color: white;
    padding: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 16px;
}

.avatar {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background-color: #d0d0d0;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
}

.avatar-placeholder {
    font-size: 24px;
    color: #999;
    font-weight: bold;
}

.user-details {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.username {
    font-size: 20px;
    font-weight: bold;
    color: #333;
    margin: 0;
}

.uid {
    font-size: 14px;
    color: #666;
    margin: 0;
}

.action-buttons {
    display: flex;
    flex-direction: column;
    gap: 8px;
    flex-shrink: 0;
}

.action-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    background-color: #f5f5f5;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
    color: #666;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.action-btn:hover {
    background-color: #e8e8e8;
}

.action-btn .el-icon {
    font-size: 16px;
}

/* 未登录时的按钮样式 */
.login-action-btn {
    background-color: #409eff !important;
    color: white !important;
    border-color: #409eff !important;
}

.login-action-btn:hover {
    background-color: #66b1ff !important;
    border-color: #66b1ff !important;
}

.register-action-btn {
    background-color: #f5f5f5 !important;
    color: #333 !important;
}

/* 标签页 */
.tabs {
    display: flex;
    justify-content: center;
    background-color: white;
    border-bottom: 1px solid #e0e0e0;
    padding: 0 20px;
}

.tab-item {
    padding: 16px 20px;
    font-size: 16px;
    color: #999;
    cursor: pointer;
    position: relative;
    transition: color 0.3s ease;
}

.tab-item.active {
    color: #333;
    font-weight: bold;
}

.tab-item.active::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2px;
    background-color: #409eff;
}

.tab-item.disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

/* 排序和筛选 */
.filter-bar {
    background-color: white;
    padding: 12px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 14px;
    color: #666;
    border-bottom: 1px solid #f0f0f0;
}

.sort-options {
    display: flex;
    align-items: center;
    gap: 8px;
}

.sort-label {
    color: #333;
}

.sort-separator {
    color: #ccc;
}

.sort-value {
    color: #666;
}

.filter-toggle {
    color: #666;
}

/* 订单列表 */
.order-list {
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 16px;
}

/* 空状态提示 */
.empty-state {
    background-color: white;
    border-radius: 12px;
    padding: 60px 20px;
    text-align: center;
    margin-top: 20px;
}

.empty-text {
    font-size: 16px;
    color: #999;
    margin: 0;
}

.order-card {
    background-color: white;
    border-radius: 12px;
    display: flex;
    position: relative;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    border: 1px solid #e8e8e8;
    margin-bottom: 16px;
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;
}

.order-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.order-image {
    width: 140px;
    height: 140px;
    background-color: #d0d0d0;
    background-size: cover;
    background-position: center;
    flex-shrink: 0;
    border-radius: 12px 0 0 12px;
    margin: 8px 0 8px 8px;
}

.order-content {
    flex: 1;
    padding: 20px 16px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-width: 0;
}

.order-title {
    font-size: 18px;
    font-weight: bold;
    color: #333;
    margin: 0 0 12px 0;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
}

.order-time {
    font-size: 14px;
    color: #666;
    margin: 0 0 12px 0;
    line-height: 1.5;
}

.order-price {
    font-size: 22px;
    font-weight: bold;
    color: #333;
    margin: 0;
}

.order-action {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 16px 12px;
    flex-shrink: 0;
    width: 90px;
    min-width: 90px;
    position: relative;
    box-sizing: border-box;
}

.use-button {
    padding: 10px 16px;
    background-color: #f5f5f5;
    color: #666;
    border: 1px solid #e0e0e0;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.3s ease;
    width: 80%;
    height: 80px;
    max-width: 100%;
    box-sizing: border-box;
}

.use-button:hover {
    background-color: #e8e8e8;
    border-color: #d0d0d0;
}

/* 待使用状态 - 蓝色按钮 */
.use-button-active {
    background-color: #409eff !important;
    color: white !important;
    border-color: #409eff !important;
}

.use-button-active:hover {
    background-color: #66b1ff !important;
    border-color: #66b1ff !important;
}

/* 票根边缘效果 - 虚线分隔 */
.ticket-edge {
    position: absolute;
    right: 90px;
    top: 8px;
    bottom: 8px;
    width: 0;
    border-left: 1px dashed #ccc;
    z-index: 2;
    pointer-events: none;
}

/* 票根边缘的半圆形缺口效果 */
.ticket-edge::before,
.ticket-edge::after {
    content: '';
    position: absolute;
    left: -8px;
    width: 16px;
    height: 16px;
    background-color: #f5f5f5;
    border-radius: 50%;
    box-shadow: 0 0 0 1px #e0e0e0;
}

.ticket-edge::before {
    top: -8px;
}

.ticket-edge::after {
    bottom: -8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .profile-header {
        padding: 16px;
    }

    .user-info {
        flex: 1;
        min-width: 0;
    }

    .avatar {
        width: 50px;
        height: 50px;
    }

    .username {
        font-size: 18px;
    }

    .uid {
        font-size: 12px;
    }

    .action-buttons {
        flex-shrink: 0;
        margin-left: 12px;
    }

    .action-btn {
        padding: 6px 12px;
        font-size: 13px;
        white-space: nowrap;
    }

    .order-card {
        flex-direction: row;
        padding: 8px;
        overflow: hidden;
    }

    .order-image {
        width: 90px;
        height: 90px;
        border-radius: 8px 0 0 8px;
        margin: 0;
        flex-shrink: 0;
    }

    .order-content {
        padding: 12px 8px;
        min-width: 0;
        flex: 1;
    }

    .order-title {
        font-size: 15px;
        margin: 0 0 6px 0;
        -webkit-line-clamp: 2;
        line-clamp: 2;
    }

    .order-time {
        font-size: 12px;
        margin: 0 0 6px 0;
    }

    .order-price {
        font-size: 18px;
    }

    .order-action {
        width: 75px;
        min-width: 75px;
        padding: 12px 6px;
    }

    .use-button {
        padding: 8px 10px;
        font-size: 12px;
    }

    .ticket-edge {
        right: 75px;
        top: 8px;
        bottom: 8px;
    }
}

/* iPhone 14 Pro 优化 (390px) */
@media (max-width: 390px) {
    .order-image {
        width: 80px;
        height: 80px;
    }

    .order-content {
        padding: 10px 6px;
    }

    .order-title {
        font-size: 14px;
        margin: 0 0 4px 0;
    }

    .order-time {
        font-size: 11px;
        margin: 0 0 4px 0;
    }

    .order-price {
        font-size: 16px;
    }

    .order-action {
        width: 70px;
        min-width: 70px;
        padding: 10px 4px;
    }

    .use-button {
        padding: 6px 8px;
        font-size: 11px;
    }

    .ticket-edge {
        right: 70px;
    }
}
</style>


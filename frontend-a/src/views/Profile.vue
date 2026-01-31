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
                <!-- 已登录状态：显示修改信息和退出登录按钮 -->
                <template v-else>
                    <!-- 隐藏地址簿按钮 - 非本期开发内容 -->
                    <!-- <button class="action-btn" @click="goToAddressBook">
                        <el-icon><Location /></el-icon>
                        <span>地址簿</span>
                    </button> -->
                    <button class="action-btn" @click="goToEditProfile">
                        <el-icon><EditPen /></el-icon>
                        <span>修改信息</span>
                    </button>
                    <button class="action-btn logout-btn" @click="handleLogout">
                        <el-icon><SwitchButton /></el-icon>
                        <span>退出登录</span>
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
            <!-- 隐藏商城订单标签页 - 非本期开发内容 -->
            <!-- <div 
                class="tab-item" 
                :class="{ active: isMallActive, disabled: !isLoggedIn }"
                @click="isLoggedIn && switchTab('mall')"
            >
                我的商城订单
            </div> -->
        </div>

        <!-- 隐藏排序和筛选 - 非本期开发内容 -->
        <!-- <div class="filter-bar">
            <div class="sort-options">
                <span class="sort-label">排序</span>
                <span class="sort-separator">|</span>
                <span class="sort-value">开展时间最近优先</span>
            </div>
            <div class="filter-toggle">
                <span>仅展示未核销的</span>
            </div>
        </div> -->

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
                    <div class="order-content">
                        <h3 class="order-title">{{ order.title }}</h3>
                        <div class="order-no-wrapper" v-if="order.orderNo">
                            <span class="order-no">{{ order.orderNo }}</span>
                            <button class="copy-btn" @click.stop="copyOrderNo(order.orderNo)">
                                <el-icon><CopyDocument /></el-icon>
                            </button>
                        </div>
                        <p class="order-time">时间: {{ order.time }}</p>
                        <p class="order-price">¥{{ order.price }}元</p>
                    </div>
                    <div class="ticket-edge"></div>
                    <div class="order-action">
                        <div class="status-badge" :class="'status-' + order.status">
                            {{ order.statusText }}
                        </div>
                        <!-- 可删除订单的删除按钮 -->
                        <button 
                            v-if="canDeleteOrder(order.status)" 
                            class="delete-btn" 
                            @click.stop="handleDeleteOrder(order)"
                            title="删除订单"
                        >
                            <el-icon><Delete /></el-icon>
                        </button>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeMount, onActivated, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { Location, EditPen, CopyDocument, SwitchButton, Delete } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { userApi, type User } from '@/api/user';
import { ticketApi } from '@/api/ticket';
import { mallApi } from '@/api/mall';

// 定义订单数据类型
interface Order {
    id: number;
    orderNo?: string;
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
    if (!token) {
        isLoggedIn.value = false;
        return;
    }
    
    try {
        // 基本的token格式验证（JWT格式应该有两个点）
        const parts = token.split('.');
        if (parts.length !== 3) {
            // token格式不正确，清除它
            console.warn('Token格式无效，已清除');
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            isLoggedIn.value = false;
            return;
        }
        
        // 尝试解析token的payload检查过期时间
        const payload = JSON.parse(atob(parts[1]));
        const exp = payload.exp;
        if (exp && exp * 1000 < Date.now()) {
            // token已过期
            console.warn('Token已过期，已清除');
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            isLoggedIn.value = false;
            return;
        }
        
        // token有效
        isLoggedIn.value = true;
    } catch (e) {
        // 无法解析token，可能已损坏
        console.warn('Token无法解析，已清除', e);
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        isLoggedIn.value = false;
    }
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

// 移除了手动核销功能，订单只能在 B 端通过订单号核销

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
                orderNo: o.orderNo,
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
    } catch (e: any) {
        console.error('获取数据失败', e);
        // 如果是认证错误（token无效），跳转到登录页
        const errorMsg = e?.message || '';
        if (errorMsg.includes('Token') || errorMsg.includes('token') || errorMsg.includes('登录') || errorMsg.includes('授权')) {
            // request.ts已经清除了localStorage
            isLoggedIn.value = false;
            // 使用nextTick确保在当前生命周期完成后跳转
            nextTick(() => {
                router.push('/login');
            });
        }
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

// 当组件被激活时（从其他页面返回时）重新检查登录状态和加载数据
onActivated(() => {
    const wasLoggedIn = isLoggedIn.value;
    checkLoginStatus();
    
    // 如果没有token，直接返回，不尝试加载数据
    if (!isLoggedIn.value) {
        return;
    }
    
    // 如果之前未登录，现在已登录，则加载数据
    if (!wasLoggedIn && isLoggedIn.value) {
        loadUserData();
    } else if (isLoggedIn.value) {
        // 如果已登录，也重新加载数据（可能信息已更新）
        loadUserData();
    }
});

const getStatusText = (status: number) => {
    const map: Record<number, string> = {
        0: '待支付',
        1: '待使用',
        2: '已使用',
        3: '已取消',
        4: '已作废'
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

// 复制订单号
const copyOrderNo = async (orderNo: string) => {
    try {
        await navigator.clipboard.writeText(orderNo);
        ElMessage.success('订单号已复制');
    } catch (e) {
        // 降级方案
        const textarea = document.createElement('textarea');
        textarea.value = orderNo;
        textarea.style.position = 'fixed';
        textarea.style.opacity = '0';
        document.body.appendChild(textarea);
        textarea.select();
        try {
            document.execCommand('copy');
            ElMessage.success('订单号已复制');
        } catch (err) {
            ElMessage.error('复制失败，请手动复制');
        }
        document.body.removeChild(textarea);
    }
};

// 退出登录
const handleLogout = async () => {
    try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        });
        
        // 清除本地存储
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        
        // 重置状态
        isLoggedIn.value = false;
        ticketOrders.value = [];
        mallOrders.value = [];
        
        ElMessage.success('已退出登录');
        
        // 跳转到登录页
        router.push('/login');
    } catch (e) {
        // 用户取消
    }
};

// 判断订单是否可以删除
const canDeleteOrder = (status: number) => {
    // 可删除的订单状态：0=待支付, 2=已使用, 3=已取消, 4=已作废
    // 不可删除：1=待使用（未核销）
    return status === 0 || status === 2 || status === 3 || status === 4;
};

// 删除订单
const handleDeleteOrder = async (order: Order) => {
    try {
        const statusText = order.statusText;
        await ElMessageBox.confirm(
            `确定要删除这个${statusText}的订单吗？删除后将无法恢复。`,
            '删除订单',
            {
                confirmButtonText: '确定删除',
                cancelButtonText: '取消',
                type: 'warning',
                confirmButtonClass: 'el-button--danger'
            }
        );
        
        // 调用删除接口
        const type = isTicketsActive.value ? 'ticket' : 'mall';
        if (type === 'ticket') {
            await ticketApi.deleteOrder(order.id);
        } else {
            await mallApi.deleteOrder(order.id);
        }
        
        ElMessage.success('订单已删除');
        
        // 重新加载订单列表
        await loadUserData();
    } catch (e: any) {
        if (e !== 'cancel') {
            ElMessage.error(e.message || '删除失败');
        }
    }
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

/* 退出登录按钮样式 */
.logout-btn {
    background-color: #fef0f0 !important;
    color: #f56c6c !important;
    border-color: #fbc4c4 !important;
}

.logout-btn:hover {
    background-color: #f56c6c !important;
    color: white !important;
    border-color: #f56c6c !important;
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
    flex-direction: column;
    position: relative;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    border: 1px solid #e8e8e8;
    margin-bottom: 16px;
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;
    padding: 16px;
}

.order-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.order-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-width: 0;
}

.order-title {
    font-size: 18px;
    font-weight: bold;
    color: #333;
    margin: 0 0 8px 0;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
}

.order-no-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0 0 8px 0;
}

.order-no {
    font-size: 12px;
    color: #409eff;
    font-family: 'Courier New', monospace;
    font-weight: 600;
    letter-spacing: 0.5px;
    margin: 0;
}

.copy-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 2px 6px;
    background-color: #ecf5ff;
    border: 1px solid #b3d8ff;
    border-radius: 4px;
    color: #409eff;
    cursor: pointer;
    transition: all 0.2s;
    font-size: 12px;
}

.copy-btn:hover {
    background-color: #409eff;
    color: white;
}

.copy-btn .el-icon {
    font-size: 12px;
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
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 16px 12px;
    flex-shrink: 0;
    width: 90px;
    min-width: 90px;
    position: relative;
    box-sizing: border-box;
}

.status-badge {
    padding: 8px 16px;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    text-align: center;
    width: 100%;
}

/* 删除按钮 */
.delete-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    background-color: #fef0f0;
    border: 1px solid #fbc4c4;
    border-radius: 50%;
    color: #f56c6c;
    cursor: pointer;
    transition: all 0.3s ease;
    padding: 0;
}

.delete-btn:hover {
    background-color: #f56c6c;
    color: white;
    border-color: #f56c6c;
    transform: scale(1.1);
}

.delete-btn .el-icon {
    font-size: 16px;
}

.status-0 {
    background-color: #f0f0f0;
    color: #909399;
}

.status-1 {
    background-color: #e1f3d8;
    color: #67c23a;
}

.status-2 {
    background-color: #e6f7ff;
    color: #409eff;
}

.status-3 {
    background-color: #fef0f0;
    color: #f56c6c;
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

    .order-content {
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

    .status-badge {
        padding: 6px 12px;
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

    .status-badge {
        padding: 4px 8px;
        font-size: 11px;
    }

    .ticket-edge {
        right: 70px;
    }
}
</style>


<template>
    <div class="profile">
        <!-- 顶部背景区域 -->
        <div class="profile-header">
            <div class="header-content">
                <div class="user-info">
                    <div class="avatar" :style="{ backgroundImage: isLoggedIn && userInfo.avatar ? `url(${userInfo.avatar})` : '' }">
                        <span v-if="!isLoggedIn || !userInfo.avatar" class="avatar-placeholder">?</span>
                    </div>
                    <div class="user-details">
                        <h2 class="username">{{ isLoggedIn ? userInfo.username : '未登录' }}</h2>
                        <p class="uid">UID: {{ isLoggedIn ? userInfo.uid : '---' }}</p>
                    </div>
                </div>
                <div class="action-buttons">
                    <template v-if="!isLoggedIn">
                        <button class="header-btn" @click="goToLogin">登录</button>
                    </template>
                    <template v-else>
                        <button class="header-btn" @click="openEditInfoDialog">修改信息</button>
                        <button class="header-btn" @click="handleLogout">退出登录</button>
                    </template>
                </div>
            </div>
        </div>

        <!-- 标签页 -->
        <div class="tabs">
            <div 
                class="tab-item" 
                :class="{ active: true, disabled: !isLoggedIn }"
                @click="isLoggedIn && switchTab('tickets')"
            >
                <img src="/pageAssets/icon-ticket.svg" alt="" class="tab-icon" />
                <span>我的门票订单</span>
            </div>
        </div>

        <!-- 订单列表 -->
        <div class="order-list">
            <div v-if="!isLoggedIn" class="empty-state">
                <p class="empty-text">请先登录以查看订单信息</p>
            </div>
            <template v-else>
                <div 
                    v-for="order in ticketOrders" 
                    :key="order.id" 
                    class="order-card"
                    @click="goToOrderDetail(order)"
                >
                    <div class="order-cover" :style="{ backgroundImage: order.coverImage ? `url(${order.coverImage})` : 'url(/images/exhibition_current.jpg)' }"></div>
                    <div class="order-info">
                        <h3 class="order-title">{{ order.title }}</h3>
                        <p class="order-time">预定时间：{{ order.time }}</p>
                        <p class="order-price">¥{{ order.price }}</p>
                    </div>
                    <div class="order-actions">
                        <button 
                            class="action-btn status-btn" 
                            :class="'status-' + order.status"
                            @click.stop
                        >
                            {{ order.statusText }}
                        </button>
                        <!-- 待使用状态显示退款按钮 -->
                        <button 
                            v-if="order.status === 1" 
                            class="action-btn refund-btn" 
                            @click.stop="handleRequestRefund(order)"
                        >
                            退款
                        </button>
                        <!-- 退款中状态显示取消退款按钮 -->
                        <button 
                            v-if="order.status === 5" 
                            class="action-btn cancel-refund-btn" 
                            @click.stop="handleCancelRefund(order)"
                        >
                            取消退款
                        </button>
                        <!-- 已使用和已退款状态显示删除按钮 -->
                        <button 
                            v-if="order.status === 2 || order.status === 6" 
                            class="action-btn delete-btn" 
                            @click.stop="handleDeleteOrder(order)"
                        >
                            删除
                        </button>
                    </div>
                </div>
            </template>
        </div>

        <!-- 修改信息对话框 -->
        <el-dialog 
            v-model="showEditInfoDialog" 
            title="修改信息" 
            width="90%"
            :style="{ maxWidth: '420px' }"
        >
            <div class="edit-info-container">
                <el-form :model="editForm" label-width="70px">
                    <el-form-item label="用户名">
                        <el-input v-model="editForm.username" placeholder="请输入用户名" maxlength="30" />
                    </el-form-item>
                    <el-form-item label="头像">
                        <div class="avatar-edit-row">
                            <div class="avatar-edit-preview" :style="{ backgroundImage: editAvatarPreview ? `url(${editAvatarPreview})` : '' }">
                                <span v-if="!editAvatarPreview" class="avatar-placeholder">?</span>
                            </div>
                            <el-upload
                                action="#"
                                :show-file-list="false"
                                :before-upload="beforeAvatarUpload"
                                accept="image/*"
                            >
                                <el-button>修改头像</el-button>
                            </el-upload>
                        </div>
                    </el-form-item>
                </el-form>
                <div class="upload-tips">
                    <p>• 用户名会同步到后台用户列表</p>
                    <p>• 头像支持 JPG、PNG 格式，大小不超过 10MB</p>
                </div>
            </div>
            <template #footer>
                <el-button @click="showEditInfoDialog = false">取消</el-button>
                <el-button type="primary" @click="confirmEditInfo" :loading="uploading">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeMount, onActivated, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { userApi, type User } from '@/api/user';
import { ticketApi } from '@/api/ticket';
import request from '@/utils/request';

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
const isLoggedIn = ref(false);

// 头像上传相关
const showEditInfoDialog = ref(false);
const uploading = ref(false);
const editAvatarPreview = ref('');
const uploadedAvatarUrl = ref('');
const editForm = reactive({
    username: ''
});

const checkLoginStatus = () => {
    const token = localStorage.getItem('token');
    if (!token) {
        isLoggedIn.value = false;
        return;
    }
    
    try {
        const parts = token.split('.');
        if (parts.length !== 3) {
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            isLoggedIn.value = false;
            return;
        }
        
        const payload = JSON.parse(atob(parts[1]));
        const exp = payload.exp;
        if (exp && exp * 1000 < Date.now()) {
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            isLoggedIn.value = false;
            return;
        }
        
        isLoggedIn.value = true;
    } catch (e) {
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        isLoggedIn.value = false;
    }
};

const userInfo = reactive<User>({
    id: 0,
    username: '张三',
    uid: '---',
    avatar: '',
    balance: 0
});

const activeTab = ref<'tickets' | 'mall'>('tickets');
const ticketOrders = ref<Order[]>([]);

const switchTab = (tab: 'tickets' | 'mall') => {
    activeTab.value = tab;
};

const goToLogin = () => {
    router.push('/login');
};

const goToEditProfile = () => {
    router.push('/edit-profile');
};

const openEditInfoDialog = () => {
    editForm.username = userInfo.username || '';
    editAvatarPreview.value = userInfo.avatar || '';
    uploadedAvatarUrl.value = '';
    showEditInfoDialog.value = true;
};

// 头像上传前的验证
const beforeAvatarUpload = async (file: File) => {
    const isImage = file.type.startsWith('image/');
    const isLt10M = file.size / 1024 / 1024 < 10;

    if (!isImage) {
        ElMessage.error('只能上传图片文件!');
        return false;
    }
    if (!isLt10M) {
        ElMessage.error('图片大小不能超过 10MB!');
        return false;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
        editAvatarPreview.value = e.target?.result as string;
    };
    reader.readAsDataURL(file);

    const formData = new FormData();
    formData.append('file', file);

    try {
        uploading.value = true;
        const result: any = await request.post('/admin/file/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });

        if (result && result.url) {
            uploadedAvatarUrl.value = result.url;
            ElMessage.success('图片上传成功');
        }
    } catch (error: any) {
        ElMessage.error(error.message || '上传失败');
        editAvatarPreview.value = '';
    } finally {
        uploading.value = false;
    }

    return false;
};

// 确认修改信息
const confirmEditInfo = async () => {
    try {
        uploading.value = true;
        const username = editForm.username?.trim() || '';
        if (!username) {
            ElMessage.error('请输入用户名');
            return;
        }
        const payload: any = { username };
        if (uploadedAvatarUrl.value) {
            payload.avatar = uploadedAvatarUrl.value;
        } else if (editAvatarPreview.value && editAvatarPreview.value.startsWith('http')) {
            payload.avatar = editAvatarPreview.value;
        }
        await userApi.updateUserInfo(payload);
        ElMessage.success('信息修改成功');

        userInfo.username = username;
        if (payload.avatar) userInfo.avatar = payload.avatar;
        const storedUserInfo = localStorage.getItem('userInfo');
        if (storedUserInfo) {
            const parsed = JSON.parse(storedUserInfo);
            parsed.username = username;
            if (payload.avatar) parsed.avatar = payload.avatar;
            localStorage.setItem('userInfo', JSON.stringify(parsed));
        }

        showEditInfoDialog.value = false;
        editAvatarPreview.value = '';
        uploadedAvatarUrl.value = '';
    } catch (error: any) {
        ElMessage.error(error.message || '修改信息失败');
    } finally {
        uploading.value = false;
    }
};

const goToOrderDetail = (order: Order) => {
    router.push(`/order/${order.id}?type=ticket`);
};

const loadUserData = async () => {
    if (!isLoggedIn.value) {
        return;
    }

    try {
        const user = await userApi.getUserInfo();
        if (user) {
            Object.assign(userInfo, user);
            editForm.username = user.username || '';
        }

        const tOrders = await ticketApi.getOrderList();
        if (tOrders) {
            ticketOrders.value = tOrders
                .filter((o: any) => o.status === 1 || o.status === 2 || o.status === 5 || o.status === 6) // 显示待使用、已使用、退款中、已退款
                .map((o: any) => ({
                    id: o.id,
                    orderNo: o.orderNo,
                    title: o.exhibitionName || '门票订单',
                    time: o.createTime,
                    price: o.totalAmount,
                    status: o.status,
                    statusText: getStatusText(o.status),
                    coverImage: o.coverImage
                }));
        }
    } catch (e: any) {
        console.error('获取数据失败', e);
        const errorMsg = e?.message || '';
        if (errorMsg.includes('Token') || errorMsg.includes('token') || errorMsg.includes('登录') || errorMsg.includes('授权')) {
            isLoggedIn.value = false;
            nextTick(() => {
                router.push('/login');
            });
        }
    }
};

onBeforeMount(() => {
    checkLoginStatus();
});

onMounted(async () => {
    await loadUserData();
});

onActivated(() => {
    const wasLoggedIn = isLoggedIn.value;
    checkLoginStatus();
    
    if (!isLoggedIn.value) {
        return;
    }
    
    if (!wasLoggedIn && isLoggedIn.value) {
        loadUserData();
    } else if (isLoggedIn.value) {
        loadUserData();
    }
});

const getStatusText = (status: number) => {
    const map: Record<number, string> = {
        0: '待支付',
        1: '待使用',
        2: '已使用',
        3: '已取消',
        4: '已作废',
        5: '退款中',
        6: '已退款'
    };
    return map[status] || '未知';
};

const handleLogout = async () => {
    try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        });
        
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        
        isLoggedIn.value = false;
        ticketOrders.value = [];
        
        ElMessage.success('已退出登录');
        router.push('/login');
    } catch (e) {
        // 用户取消
    }
};

const handleDeleteOrder = async (order: Order) => {
    // 待使用状态不允许删除
    if (order.status === 1) {
        ElMessage.warning('待使用的订单不能删除');
        return;
    }
    
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
        
        await ticketApi.deleteOrder(order.id);
        ElMessage.success('订单已删除');
        await loadUserData();
    } catch (e: any) {
        if (e !== 'cancel') {
            ElMessage.error(e.message || '删除失败');
        }
    }
};

// 申请退款
const handleRequestRefund = async (order: Order) => {
    try {
        await ElMessageBox.confirm(
            '确定要申请退款吗？提交后需要等待管理员处理。',
            '申请退款',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        );
        
        await ticketApi.requestRefund(order.id);
        ElMessage.success('退款申请已提交');
        await loadUserData();
    } catch (e: any) {
        if (e !== 'cancel') {
            ElMessage.error(e.message || '申请退款失败');
        }
    }
};

// 取消退款申请
const handleCancelRefund = async (order: Order) => {
    try {
        await ElMessageBox.confirm(
            '确定要取消退款申请吗？',
            '取消退款',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        );
        
        await ticketApi.cancelRefund(order.id);
        ElMessage.success('已取消退款申请');
        await loadUserData();
    } catch (e: any) {
        if (e !== 'cancel') {
            ElMessage.error(e.message || '取消退款失败');
        }
    }
};
</script>

<style scoped>
.profile {
    min-height: 100vh;
    background-color: #f5f5f5;
}

/* 顶部背景区域 */
.profile-header {
    background: #1E3D73;
    position: relative;
}

.header-content {
    padding: 30px 20px 40px;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    position: relative;
    z-index: 1;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.avatar {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background-color: rgba(255, 255, 255, 0.3);
    background-size: cover;
    background-position: center;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
}

.avatar-placeholder {
    font-size: 28px;
    color: #fff;
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
    color: #fff;
    margin: 0;
}

.uid {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.9);
    margin: 0;
}

.action-buttons {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.header-btn {
    padding: 0;
    background-color: #fff;
    border: none;
    border-radius: 4px;
    color: #333;
    font-size: 13px;
    cursor: pointer;
    transition: opacity 0.2s;
    width: 88px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.header-btn:hover {
    opacity: 0.9;
}

/* 标签页 */
.tabs {
    background-color: #fff;
    padding: 0 20px;
    border-bottom: 2px solid #e0e0e0;
    display: flex;
    justify-content: center;
}

.tab-item {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 12px 0;
    font-size: 15px;
    color: #333;
    font-weight: 500;
    cursor: pointer;
    position: relative;
}

.tab-icon {
    width: 18px;
    height: 18px;
}

.tab-item.active::after {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 0;
    right: 0;
    height: 2px;
    background-color: #507bda;
}

.tab-item.disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

/* 订单列表 */
.order-list {
    padding: 16px;
}

.empty-state {
    background-color: #fff;
    border-radius: 8px;
    padding: 60px 20px;
    text-align: center;
    margin-top: 20px;
}

.empty-text {
    font-size: 15px;
    color: #999;
    margin: 0;
}

.order-card {
    background-color: #fff;
    border-radius: 8px;
    display: flex;
    gap: 12px;
    padding: 12px;
    margin-bottom: 12px;
    cursor: pointer;
    transition: box-shadow 0.2s;
}

.order-card:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.order-cover {
    width: 100px;
    aspect-ratio: 13 / 16;
    background-size: cover;
    background-position: center;
    border-radius: 4px;
    flex-shrink: 0;
    background-color: #f0f0f0;
}

.order-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-width: 0;
}

.order-title {
    font-size: 15px;
    font-weight: 600;
    color: #333;
    margin: 0 0 4px 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.order-time {
    font-size: 12px;
    color: #999;
    margin: 0 0 4px 0;
}

.order-price {
    font-size: 16px;
    font-weight: bold;
    color: #ff4d4f;
    margin: 0;
}

.order-actions {
    display: flex;
    flex-direction: column;
    gap: 6px;
    justify-content: center;
}

.action-btn {
    width: 70px;
    border-radius: 4px;
    font-size: 12px;
    cursor: pointer;
    transition: opacity 0.2s;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
}

.action-btn:hover {
    opacity: 0.85;
}

.status-btn {
    background-color: #fff;
    border: 2px solid;
    height: 60px;
}

.status-0 {
    border-color: #999;
    color: #666;
}

.status-1 {
    border-color: #507bda;
    color: #507bda;
}

.status-2 {
    border-color: #999;
    color: #666;
}

.status-3 {
    border-color: #999;
    color: #666;
}

.status-4 {
    border-color: #999;
    color: #666;
}

.status-5 {
    border-color: #e6a23c;
    color: #e6a23c;
}

.status-6 {
    border-color: #999;
    color: #666;
}

.delete-btn {
    background-color: #fff;
    border: 2px solid #e53327;
    color: #e53327;
    height: 20px;
}

.refund-btn {
    background-color: #fff;
    border: 2px solid #e6a23c;
    color: #e6a23c;
    height: 20px;
}

.cancel-refund-btn {
    background-color: #fff;
    border: 2px solid #909399;
    color: #909399;
    height: 20px;
}

/* 响应式 */
@media (max-width: 768px) {
    .header-content {
        padding: 20px 16px 30px;
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

    .header-btn {
        width: 80px;
        height: 22px;
        font-size: 12px;
    }

    .order-cover {
        width: 85px;
    }

    .order-title {
        font-size: 14px;
    }

    .order-time {
        font-size: 11px;
    }

    .order-price {
        font-size: 15px;
    }

    .action-btn {
        width: 65px;
        font-size: 11px;
    }
    
    .status-btn {
        height: 54px;
    }
    
    .delete-btn {
        height: 18px;
    }
}

/* 编辑用户名对话框 */
.edit-info-container {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.upload-tips {
    text-align: left;
    color: #666;
    font-size: 13px;
    line-height: 1.8;
}

.upload-tips p {
    margin: 4px 0;
}
</style>

<template>
    <div class="login-page">
        <!-- 关闭按钮 -->
        <button class="close-btn" @click="handleClose">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M15 5L5 15M5 5L15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
        </button>

        <!-- Logo -->
        <div class="logo-section">
            <img src="/pageAssets/logo-header.svg" alt="AI+艺术馆" class="logo" />
        </div>

        <!-- 表单区域 -->
        <div class="form-section">
            <!-- 手机号 -->
            <input 
                v-model="phone" 
                type="tel" 
                placeholder="手机号" 
                maxlength="11"
                class="input-field"
            />

            <!-- 验证码 -->
            <div class="code-row">
                <input 
                    v-model="code" 
                    type="text" 
                    placeholder="短信验证码" 
                    maxlength="6"
                    class="input-field code-input"
                />
                <button 
                    class="code-btn" 
                    :disabled="countdown > 0"
                    @click="sendCode"
                >
                    {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </button>
            </div>

            <!-- 协议 -->
            <div class="agreement-row">
                <el-checkbox v-model="agreed" />
                <span class="agreement-text">
                    我已阅读并同意
                    <router-link to="/service-terms" class="link">服务协议</router-link>
                </span>
            </div>

            <!-- 登录按钮 -->
            <button class="login-btn" @click="handleLogin">登录</button>

            <!-- 提示 -->
            <div class="tip">未注册的手机号点击登录后会自动注册</div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { userApi } from '@/api/user';

const router = useRouter();

const phone = ref('');
const code = ref('');
const agreed = ref(false);
const countdown = ref(0);

let countdownTimer: number | null = null;

const sendCode = async () => {
    if (!phone.value) {
        ElMessage.warning('请输入手机号');
        return;
    }

    if (!/^1[3-9]\d{9}$/.test(phone.value)) {
        ElMessage.warning('请输入正确的手机号');
        return;
    }

    try {
        await userApi.sendSmsCode(phone.value);
        ElMessage.success('验证码已发送');
        
        countdown.value = 60;
        countdownTimer = window.setInterval(() => {
            countdown.value--;
            if (countdown.value <= 0 && countdownTimer) {
                clearInterval(countdownTimer);
                countdownTimer = null;
            }
        }, 1000);
    } catch (e: any) {
        ElMessage.error(e.message || '发送验证码失败');
    }
};

const handleLogin = async () => {
    if (!phone.value) {
        ElMessage.warning('请输入手机号');
        return;
    }

    if (!/^1[3-9]\d{9}$/.test(phone.value)) {
        ElMessage.warning('请输入正确的手机号');
        return;
    }

    if (!code.value) {
        ElMessage.warning('请输入验证码');
        return;
    }

    if (!agreed.value) {
        ElMessage.warning('请阅读并同意服务协议');
        return;
    }

    try {
        const res = await userApi.loginWithSms(phone.value, code.value);
        if (res && res.token) {
            localStorage.setItem('token', res.token);
            if (res.userInfo) {
                localStorage.setItem('userInfo', JSON.stringify(res.userInfo));
            }
            ElMessage.success('登录成功');
            router.push('/');
        }
    } catch (e: any) {
        ElMessage.error(e.message || '登录失败');
    }
};

const handleClose = () => {
    router.push('/');
};
</script>

<style scoped>
* {
    box-sizing: border-box;
}

.login-page {
    min-height: 100vh;
    background: #ffffff;
    padding: 0 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
}

/* 关闭按钮 */
.close-btn {
    position: absolute;
    top: 24px;
    right: 24px;
    width: 32px;
    height: 32px;
    border: none;
    background: transparent;
    cursor: pointer;
    padding: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #000000;
}

.close-btn:hover {
    opacity: 0.7;
}

/* Logo区域 */
.logo-section {
    margin-top: 252px;
    margin-bottom: 144px;
}

.logo {
    height: 34px;
    width: auto;
}

/* 表单区域 */
.form-section {
    width: 100%;
    max-width: 360px;
}

/* 输入框 */
.input-field {
    width: 100%;
    height: 48px;
    padding: 0 16px;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    font-size: 15px;
    outline: none;
    background: #ffffff;
    margin-bottom: 16px;
}

.input-field::placeholder {
    color: #bfbfbf;
}

.input-field:focus {
    border-color: #5b8ff9;
}

/* 验证码行 */
.code-row {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
}

.code-input {
    flex: 1;
    margin-bottom: 0;
}

.code-btn {
    width: 110px;
    height: 48px;
    background: #5b8ff9;
    color: #ffffff;
    border: none;
    border-radius: 4px;
    font-size: 14px;
    cursor: pointer;
    flex-shrink: 0;
}

.code-btn:hover:not(:disabled) {
    background: #4a7de8;
}

.code-btn:disabled {
    background: #d9d9d9;
    cursor: not-allowed;
}

/* 协议行 */
.agreement-row {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 24px;
}

.agreement-text {
    font-size: 14px;
    color: #666666;
}

.link {
    color: #5b8ff9;
    text-decoration: none;
}

.link:hover {
    text-decoration: underline;
}

/* 登录按钮 */
.login-btn {
    width: 100%;
    height: 48px;
    background: #5b8ff9;
    color: #ffffff;
    border: none;
    border-radius: 4px;
    font-size: 16px;
    font-weight: 500;
    cursor: pointer;
    margin-bottom: 16px;
}

.login-btn:hover {
    background: #4a7de8;
}

.login-btn:active {
    transform: scale(0.98);
}

/* 提示文字 */
.tip {
    text-align: center;
    font-size: 13px;
    color: #999999;
}

/* 响应式 */
@media (max-width: 768px) {
    .logo-section {
        margin-top: 180px;
        margin-bottom: 108px;
    }
    
    .logo {
        height: 31px;
    }
}

@media (max-width: 480px) {
    .close-btn {
        top: 20px;
        right: 20px;
    }
    
    .logo-section {
        margin-top: 144px;
        margin-bottom: 90px;
    }
    
    .logo {
        height: 29px;
    }
    
    .input-field,
    .code-btn,
    .login-btn {
        height: 46px;
    }
    
    .code-btn {
        width: 100px;
        font-size: 13px;
    }
}
</style>

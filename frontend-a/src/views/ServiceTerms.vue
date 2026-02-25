<template>
    <div class="service-terms">
        <div class="header">
            <button class="back-button" @click="goBack">
                <el-icon><ArrowLeft /></el-icon>
            </button>
            <h1 class="page-title">服务协议</h1>
            <div class="header-placeholder"></div>
        </div>

        <div class="content">
            <p>测试内容测试内容测试内容</p>
            <p>测试内容测试内容测试内容</p>
            <p>测试内容测试内容测试内容</p>
        </div>
    </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { ArrowLeft } from '@element-plus/icons-vue';

const router = useRouter();

const goBack = () => {
    router.back();
};
</script>

<style scoped>
.service-terms {
    min-height: 100vh;
    background-color: #f5f5f5;
}

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

.content {
    padding: 20px;
    background-color: white;
    margin: 12px;
    border-radius: 8px;
}

.content p {
    font-size: 14px;
    color: #666;
    line-height: 1.8;
    margin: 0 0 12px 0;
}
</style>

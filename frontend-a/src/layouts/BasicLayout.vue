<template>
    <div class="layout-basic">
        <header></header>
        <main class="content">
            <router-view />
        </main>
        <footer>
            <nav class="bottom-nav">
                <ul>
                    <li v-for="item in navItems" :key="item.path">
                        <router-link :to="item.path" class="nav-item" :class="{ active: isActive(item.path) }">
                            <el-icon class="nav-icon">
                                <component :is="item.icon" />
                            </el-icon>
                            <span class="nav-text">{{ item.label }}</span>
                        </router-link>
                    </li>
                </ul>
            </nav>
        </footer>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { HomeFilled, Calendar, ShoppingBag, User } from '@element-plus/icons-vue'

const route = useRoute()

const navItems = [
    { path: '/', label: '首页', icon: HomeFilled },
    { path: '/exhibitions', label: '近期展览', icon: Calendar },
    { path: '/mall', label: '文创商城', icon: ShoppingBag },
    { path: '/profile', label: '我的', icon: User },
]

const isActive = (path: string) => {
    return route.path === path
}
</script>

<style scoped>
.layout-basic {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
}

.content {
    flex: 1;
    padding-bottom: 70px;
    /* 为底部导航栏预留空间，防止内容被遮挡 */
}

.bottom-nav {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    background-color: #f4f4f4;
    border-top: 1px solid #e0e0e0;
    z-index: 999;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
}

.bottom-nav ul {
    list-style-type: none;
    display: flex;
    justify-content: space-around;
    align-items: center;
    height: 60px;
    margin: 0;
    padding: 0;
    max-width: 100%;
}

.bottom-nav ul li {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
}

.nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-decoration: none;
    color: #666;
    padding: 8px 12px;
    transition: all 0.3s ease;
    width: 100%;
    position: relative;
}

.nav-icon {
    font-size: 22px;
    margin-bottom: 4px;
    transition: color 0.3s ease;
}

.nav-text {
    font-size: 12px;
    transition: color 0.3s ease;
}

.nav-item:hover {
    color: #409eff;
}

.nav-item.active {
    color: #409eff;
}

.nav-item.active::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 30px;
    height: 2px;
    background-color: #409eff;
    border-radius: 2px;
}

/* 响应式设计 */
@media (max-width: 480px) {
    .nav-text {
        font-size: 11px;
    }
    
    .nav-icon {
        font-size: 20px;
    }
}
</style>
<template>
    <div class="layout-basic">
        <header class="top-header" :class="{ 'dark-header': isDarkHeader }">
            <img 
                class="header-logo" 
                :src="isDarkHeader ? '/pageAssets/logo-header-dark.svg' : '/pageAssets/logo-header.svg'" 
                alt="AI+艺术馆" 
            />
        </header>
        <main class="content">
            <router-view />
        </main>
        <footer>
            <nav class="bottom-nav">
                <ul>
                    <li v-for="item in navItems" :key="item.path">
                        <router-link :to="item.path" class="nav-item" :class="{ active: isActive(item.path) }">
                            <img
                                class="nav-icon-img"
                                :src="isActive(item.path) ? item.iconActive : item.iconInactive"
                                :alt="item.label"
                            />
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

const route = useRoute()

const navItems = [
    { path: '/', label: '首页', iconActive: '/pageAssets/nav-home-on.svg', iconInactive: '/pageAssets/nav-home-off.svg' },
    { path: '/exhibitions', label: '展览', iconActive: '/pageAssets/nav-exhibition-on.svg', iconInactive: '/pageAssets/nav-exhibition-off.svg' },
    { path: '/profile', label: '我的', iconActive: '/pageAssets/nav-profile-on.svg', iconInactive: '/pageAssets/nav-profile-off.svg' },
]

const isActive = (path: string) => {
    return route.path === path
}

// 展览页面使用深色header
const isDarkHeader = computed(() => {
    return route.path === '/exhibitions'
})
</script>

<style scoped>
.layout-basic {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
}

.top-header {
    position: sticky;
    top: 0;
    left: 0;
    right: 0;
    height: 34px;
    background-color: #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 100;
    box-shadow: 0 1px 0 rgba(0, 0, 0, 0.06);
    transition: background-color 0.3s ease;
}

.top-header.dark-header {
    background-color: #213d7c;
    box-shadow: 0 1px 0 rgba(0, 0, 0, 0.2);
}

.header-logo {
    height: 20px;
    width: auto;
    object-fit: contain;
}

.content {
    flex: 1;
    padding-bottom: 60px;
    /* 为底部导航栏预留空间，防止内容被遮挡 */
    margin-bottom: 0;
}

.bottom-nav {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    background-color: #ffffff;
    border-top: 1px solid #e0e0e0;
    z-index: 999;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
    margin: 0;
    padding: 0;
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

.nav-icon-img {
    width: 26px;
    height: 26px;
    margin-bottom: 4px;
    object-fit: contain;
    transition: opacity 0.3s ease;
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

.nav-item.active .nav-icon-img {
    opacity: 1;
}

/* 响应式设计 */
@media (max-width: 480px) {
    .nav-text {
        font-size: 11px;
    }
    
    .nav-icon-img {
        width: 22px;
        height: 22px;
    }
}
</style>
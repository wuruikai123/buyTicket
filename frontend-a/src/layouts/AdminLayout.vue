<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="admin-aside">
      <div class="logo">
        <span v-if="!isCollapse">展览管理后台</span>
        <span v-else>管理</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        background-color="#001529"
        text-color="#ffffffa6"
        active-text-color="#fff"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据概览</span>
        </el-menu-item>

        <el-sub-menu index="exhibition">
          <template #title>
            <el-icon><Picture /></el-icon>
            <span>展览管理</span>
          </template>
          <el-menu-item index="/admin/exhibition">展览列表</el-menu-item>
          <el-menu-item index="/admin/exhibition/create">创建展览</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="inventory">
          <template #title>
            <el-icon><Ticket /></el-icon>
            <span>库存管理</span>
          </template>
          <el-menu-item index="/admin/inventory">库存列表</el-menu-item>
          <el-menu-item index="/admin/inventory/batch">批量创建</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="product">
          <template #title>
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </template>
          <el-menu-item index="/admin/product">商品列表</el-menu-item>
          <el-menu-item index="/admin/product/create">创建商品</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">管理员</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataAnalysis, Picture, Ticket, Goods, Fold, Expand } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta?.title as string
  }))
})

const handleLogout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}
.admin-aside {
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;
}
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #002140;
}
.admin-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.username {
  color: #333;
}
.admin-main {
  background-color: #f0f2f5;
  padding: 20px;
}
:deep(.el-menu) {
  border-right: none;
}
:deep(.el-sub-menu__title:hover),
:deep(.el-menu-item:hover) {
  background-color: #000c17 !important;
}
:deep(.el-menu-item.is-active) {
  background-color: #1890ff !important;
}
</style>

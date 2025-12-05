<template>
  <el-container class="main-layout">
    <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
      <div class="logo">
        <span v-if="!isCollapse">展览管理系统</span>
        <span v-else>展览</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>数据概览</template>
        </el-menu-item>
        
        <el-sub-menu index="user">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/user/list">用户列表</el-menu-item>
          <el-menu-item index="/user/statistics">用户统计</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="exhibition">
          <template #title>
            <el-icon><Picture /></el-icon>
            <span>展览管理</span>
          </template>
          <el-menu-item index="/exhibition/list">展览列表</el-menu-item>
          <el-menu-item index="/exhibition/create">创建展览</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="ticket">
          <template #title>
            <el-icon><Ticket /></el-icon>
            <span>门票管理</span>
          </template>
          <el-menu-item index="/ticket/inventory">库存管理</el-menu-item>
          <el-menu-item index="/ticket/warning">库存预警</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="order">
          <template #title>
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </template>
          <el-menu-item index="/order/ticket">门票订单</el-menu-item>
          <el-menu-item index="/order/mall">商城订单</el-menu-item>
          <el-menu-item index="/order/statistics">订单统计</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="product">
          <template #title>
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </template>
          <el-menu-item index="/product/list">商品列表</el-menu-item>
          <el-menu-item index="/product/create">创建商品</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="statistics">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>数据统计</span>
          </template>
          <el-menu-item index="/statistics/sales">销售报表</el-menu-item>
          <el-menu-item index="/statistics/user-analysis">用户分析</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </template>
          <el-menu-item index="/system/basic">基础设置</el-menu-item>
          <el-menu-item index="/system/content">内容管理</el-menu-item>
          <el-menu-item index="/system/log">操作日志</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-icon" @click="toggleCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              <span>{{ authStore.userInfo?.realName || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isCollapse = ref(false)

const activeMenu = computed(() => {
  const { path } = route
  // 如果路径包含子路由，返回父路径用于高亮菜单
  if (path.startsWith('/user')) {
    return path.includes('/statistics') ? '/user/statistics' : '/user/list'
  }
  if (path.startsWith('/exhibition')) {
    if (path.includes('/create')) return '/exhibition/create'
    if (path.includes('/edit')) return '/exhibition/list'
    return '/exhibition/list'
  }
  if (path.startsWith('/ticket')) {
    return path.includes('/warning') ? '/ticket/warning' : '/ticket/inventory'
  }
  if (path.startsWith('/order')) {
    if (path.includes('/mall')) return '/order/mall'
    if (path.includes('/statistics')) return '/order/statistics'
    return '/order/ticket'
  }
  if (path.startsWith('/product')) {
    if (path.includes('/create')) return '/product/create'
    if (path.includes('/edit')) return '/product/list'
    return '/product/list'
  }
  if (path.startsWith('/statistics')) {
    return path.includes('/user-analysis') ? '/statistics/user-analysis' : '/statistics/sales'
  }
  if (path.startsWith('/system')) {
    if (path.includes('/content')) return '/system/content'
    if (path.includes('/log')) return '/system/log'
    return '/system/basic'
  }
  return path
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.main-layout {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  transition: width 0.3s;
  
  .logo {
    height: 60px;
    line-height: 60px;
    text-align: center;
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    background-color: #2b3a4b;
  }

  .el-menu {
    border: none;
    height: calc(100vh - 60px);
    overflow-y: auto;
  }
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;

  .collapse-icon {
    font-size: 20px;
    cursor: pointer;
    color: #606266;
    
    &:hover {
      color: #409eff;
    }
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    color: #606266;
    
    &:hover {
      color: #409eff;
    }
  }
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>


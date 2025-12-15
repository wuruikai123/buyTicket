<template>
  <el-container class="main-layout">
    <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar">
      <div class="logo">
        <div class="logo-icon">🎨</div>
        <span v-if="!isCollapse" class="logo-text">展览管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        class="sidebar-menu"
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
          <el-menu-item index="/banner/list">轮播图管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="ticket">
          <template #title>
            <el-icon><Ticket /></el-icon>
            <span>门票管理</span>
          </template>
          <el-menu-item index="/ticket/inventory">库存管理</el-menu-item>
          <el-menu-item index="/ticket/warning">库存预警</el-menu-item>
          <el-menu-item index="/order/verification">订单核销</el-menu-item>
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
    
    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-icon" @click="toggleCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title || '页面' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-avatar">
              <el-avatar :size="36" class="avatar">
                {{ (authStore.userInfo?.realName || '管')[0] }}
              </el-avatar>
              <span class="user-name">{{ authStore.userInfo?.realName || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
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
  if (path.startsWith('/user')) {
    return path.includes('/statistics') ? '/user/statistics' : '/user/list'
  }
  if (path.startsWith('/exhibition')) {
    if (path.includes('/create')) return '/exhibition/create'
    if (path.includes('/edit')) return '/exhibition/list'
    return '/exhibition/list'
  }
  if (path.startsWith('/banner')) {
    return '/banner/list'
  }
  if (path.startsWith('/ticket')) {
    return path.includes('/warning') ? '/ticket/warning' : '/ticket/inventory'
  }
  if (path.startsWith('/order')) {
    return '/order/verification'
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
  background-color: #f5f7fa;
}

.sidebar {
  background-color: #ffffff;
  border-right: 1px solid #e8e8e8;
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  
  .logo {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    border-bottom: 1px solid #f0f0f0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    
    .logo-icon {
      font-size: 28px;
    }
    
    .logo-text {
      font-size: 16px;
      font-weight: 600;
      color: #ffffff;
      white-space: nowrap;
    }
  }

  .sidebar-menu {
    flex: 1;
    border: none;
    padding: 8px;
    overflow-y: auto;
    background-color: transparent;
    
    &::-webkit-scrollbar {
      width: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background-color: #e0e0e0;
      border-radius: 2px;
    }
  }
}

.main-container {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.header {
  height: 64px;
  background-color: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 20px;
    
    .collapse-icon {
      font-size: 20px;
      cursor: pointer;
      color: #606266;
      padding: 8px;
      border-radius: 6px;
      transition: all 0.3s;
      
      &:hover {
        color: #409eff;
        background-color: #f5f7fa;
      }
    }
  }
  
  .header-right {
    .user-avatar {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      padding: 6px 12px;
      border-radius: 8px;
      transition: all 0.3s;
      
      &:hover {
        background-color: #f5f7fa;
      }
      
      .avatar {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #ffffff;
        font-weight: 600;
      }
      
      .user-name {
        font-size: 14px;
        color: #303133;
        font-weight: 500;
      }
    }
  }
}

.main-content {
  background-color: #f5f7fa;
  padding: 24px;
  overflow-y: auto;
  min-height: calc(100vh - 64px);
}

// 菜单样式覆盖
:deep(.el-menu) {
  --el-menu-bg-color: transparent;
  --el-menu-hover-bg-color: #f5f7fa;
  --el-menu-active-color: #409eff;
  --el-menu-text-color: #606266;
  --el-menu-hover-text-color: #303133;
  --el-menu-item-height: 44px;
  
  .el-menu-item {
    margin: 4px 0;
    border-radius: 8px;
    
    &.is-active {
      background: linear-gradient(135deg, #ecf5ff 0%, #f0f7ff 100%);
      color: #409eff;
      font-weight: 500;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 20px;
        background-color: #409eff;
        border-radius: 0 2px 2px 0;
      }
    }
  }
  
  .el-sub-menu__title {
    margin: 4px 0;
    border-radius: 8px;
  }
}

:deep(.el-menu--collapse) {
  .el-sub-menu__title {
    padding-left: 20px !important;
  }
}
</style>

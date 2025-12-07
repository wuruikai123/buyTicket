<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #409eff">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.userCount }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #67c23a">
            <el-icon><Picture /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.exhibitionCount }}</div>
            <div class="stat-label">展览数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #e6a23c">
            <el-icon><Ticket /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.orderCount }}</div>
            <div class="stat-label">订单总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #f56c6c">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ stats.totalSales }}</div>
            <div class="stat-label">销售总额</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>近期销售趋势</span>
          </template>
          <div class="chart-placeholder">
            <el-empty description="图表功能开发中" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/admin/exhibition/create')">
              <el-icon><Plus /></el-icon>
              创建展览
            </el-button>
            <el-button type="success" @click="$router.push('/admin/inventory/batch')">
              <el-icon><DocumentAdd /></el-icon>
              批量创建库存
            </el-button>
            <el-button type="warning" @click="$router.push('/admin/product/create')">
              <el-icon><Goods /></el-icon>
              创建商品
            </el-button>
          </div>
        </el-card>

        <el-card style="margin-top: 20px">
          <template #header>
            <span>库存预警</span>
          </template>
          <div v-if="lowStockItems.length" class="warning-list">
            <div v-for="item in lowStockItems" :key="item.id" class="warning-item">
              <span class="warning-name">{{ item.exhibitionName }}</span>
              <span class="warning-date">{{ item.ticketDate }} {{ item.timeSlot }}</span>
              <el-tag type="danger" size="small">剩余 {{ item.remain }} 张</el-tag>
            </div>
          </div>
          <el-empty v-else description="暂无库存预警" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { User, Picture, Ticket, Money, Plus, DocumentAdd, Goods } from '@element-plus/icons-vue'

const stats = reactive({
  userCount: 0,
  exhibitionCount: 0,
  orderCount: 0,
  totalSales: '0.00'
})

interface LowStockItem {
  id: number
  exhibitionName: string
  ticketDate: string
  timeSlot: string
  remain: number
}

const lowStockItems = ref<LowStockItem[]>([])

// 模拟数据加载
onMounted(() => {
  // 实际项目中应调用API获取数据
  stats.userCount = 1256
  stats.exhibitionCount = 8
  stats.orderCount = 3420
  stats.totalSales = '156,800.00'

  lowStockItems.value = [
    { id: 1, exhibitionName: '印象派大师展', ticketDate: '2025-12-10', timeSlot: '14:00-16:00', remain: 5 },
    { id: 2, exhibitionName: '当代艺术双年展', ticketDate: '2025-12-11', timeSlot: '10:00-12:00', remain: 8 }
  ]
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 0;
}
.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
  margin-right: 16px;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.quick-actions .el-button {
  width: 100%;
  justify-content: flex-start;
}
.warning-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.warning-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #fef0f0;
  border-radius: 4px;
}
.warning-name {
  font-weight: 500;
  flex: 1;
}
.warning-date {
  font-size: 12px;
  color: #909399;
}
</style>

<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon user">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardData.todayUsers }}</div>
              <div class="stat-label">今日新增用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon order">
              <el-icon><ShoppingBag /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardData.todayOrders }}</div>
              <div class="stat-label">今日订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon sales">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatNumber(dashboardData.todaySales) }}</div>
              <div class="stat-label">今日销售额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon visit">
              <el-icon><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardData.todayVisits }}</div>
              <div class="stat-label">今日访问量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>
            <span>用户增长趋势</span>
          </template>
          <v-chart class="chart" :option="userGrowthOption" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>
            <span>订单趋势</span>
          </template>
          <v-chart class="chart" :option="orderTrendOption" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>
            <span>销售额趋势</span>
          </template>
          <v-chart class="chart" :option="salesTrendOption" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>
            <span>订单类型占比</span>
          </template>
          <v-chart class="chart" :option="orderTypeOption" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { statisticsApi } from '@/api/statistics'
import type { EChartsOption } from 'echarts'

use([
  CanvasRenderer,
  LineChart,
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const dashboardData = ref({
  todayUsers: 0,
  todayOrders: 0,
  todaySales: 0,
  todayVisits: 0,
  userGrowth: [] as any[],
  orderTrend: [] as any[],
  salesTrend: [] as any[],
  orderTypeRatio: [] as any[],
  orderStatusRatio: [] as any[]
})

const formatNumber = (num: number) => {
  return (num || 0).toLocaleString()
}

const userGrowthOption = computed<EChartsOption>(() => {
  const growthData = dashboardData.value.userGrowth || []
  return {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: growthData.map((item: any) => item.date ? item.date.split('-')[2] : '')
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        data: growthData.map((item: any) => item.value),
        type: 'line',
        smooth: true,
        areaStyle: {}
      }
    ]
  }
})

const orderTrendOption = computed<EChartsOption>(() => {
  const trendData = dashboardData.value.orderTrend || []
  return {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['门票订单', '商城订单']
    },
    xAxis: {
      type: 'category',
      data: trendData.map((item: any) => item.date ? item.date.split('-')[2] : '')
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '门票订单',
        data: trendData.map((item: any) => item.ticket),
        type: 'line',
        smooth: true
      },
      {
        name: '商城订单',
        data: trendData.map((item: any) => item.mall),
        type: 'line',
        smooth: true
      }
    ]
  }
})

const salesTrendOption = computed<EChartsOption>(() => {
  const salesData = dashboardData.value.salesTrend || []
  return {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: salesData.map((item: any) => item.date ? item.date.split('-')[2] : '')
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        data: salesData.map((item: any) => item.value),
        type: 'bar',
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  }
})

const orderTypeOption = computed<EChartsOption>(() => ({
  tooltip: {
    trigger: 'item'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      type: 'pie',
      radius: '50%',
      data: dashboardData.value.orderTypeRatio || [],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
}))

const loadData = async () => {
  try {
    const data = await statisticsApi.getDashboardData()
    dashboardData.value = data
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.dashboard {
  .stats-cards {
    margin-bottom: 24px;
  }

  .stat-card {
    border-radius: 16px !important;
    overflow: hidden;
    
    .stat-content {
      display: flex;
      align-items: center;
      gap: 20px;
      padding: 8px;

      .stat-icon {
        width: 64px;
        height: 64px;
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
        color: #fff;
        flex-shrink: 0;

        &.user {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          box-shadow: 0 4px 14px rgba(102, 126, 234, 0.4);
        }

        &.order {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          box-shadow: 0 4px 14px rgba(245, 87, 108, 0.4);
        }

        &.sales {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          box-shadow: 0 4px 14px rgba(79, 172, 254, 0.4);
        }

        &.visit {
          background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
          box-shadow: 0 4px 14px rgba(67, 233, 123, 0.4);
        }
      }

      .stat-info {
        flex: 1;
        min-width: 0;

        .stat-value {
          font-size: 28px;
          font-weight: 700;
          color: #303133;
          margin-bottom: 6px;
          line-height: 1.2;
        }

        .stat-label {
          font-size: 13px;
          color: #909399;
          font-weight: 500;
        }
      }
    }
  }

  .charts-row {
    margin-bottom: 24px;
  }

  .chart {
    height: 320px;
  }
}

// 响应式
@media (max-width: 768px) {
  .dashboard {
    .stat-card .stat-content {
      .stat-icon {
        width: 52px;
        height: 52px;
        font-size: 24px;
      }
      
      .stat-info .stat-value {
        font-size: 24px;
      }
    }
    
    .chart {
      height: 260px;
    }
  }
}
</style>


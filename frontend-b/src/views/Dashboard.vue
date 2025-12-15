<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <div class="ticket-stats">
        <div class="stat-item">
          <div class="stat-label">今日销售门票</div>
          <div class="stat-value">{{ dashboardData.todayTicketSales || 0 }}<span class="unit">张</span></div>
        </div>
        <div class="stat-item">
          <div class="stat-label">今日核销门票</div>
          <div class="stat-value">{{ dashboardData.todayTicketVerified || 0 }}<span class="unit">张</span></div>
        </div>
      </div>
      <div class="current-time">
        <div class="time-date">{{ currentDate }}</div>
        <div class="time-clock">{{ currentTime }}</div>
      </div>
    </div>

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
import { ref, onMounted, onUnmounted, computed } from 'vue'
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
  todayTicketSales: 0,
  todayTicketVerified: 0,
  userGrowth: [] as any[],
  orderTrend: [] as any[],
  salesTrend: [] as any[],
  orderTypeRatio: [] as any[],
  orderStatusRatio: [] as any[]
})

const currentDate = ref('')
const currentTime = ref('')
let timeInterval: any = null

const updateTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  
  currentDate.value = `${year}年${month}月${day}日`
  currentTime.value = `${hours}:${minutes}`
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
    const data: any = await statisticsApi.getDashboardData()
    // 后端返回的数据结构，需要适配前端
    dashboardData.value = {
      todayTicketSales: data.todayTicketOrders || 0,
      todayTicketVerified: 0, // 后端暂未提供核销数据
      todayUsers: data.todayUsers || 0,
      todayOrders: data.todayOrders || 0,
      todaySales: 0,
      todayVisits: 0,
      userGrowth: [],
      orderTrend: [],
      salesTrend: [],
      orderTypeRatio: [
        { name: '门票订单', value: data.totalTicketOrders || 0 },
        { name: '商城订单', value: data.totalMallOrders || 0 }
      ],
      orderStatusRatio: []
    }
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000) // 每秒更新一次时间
  loadData()
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style scoped lang="scss">
.dashboard {
  .dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px;
    background-color: #ffffff;
    border-radius: 8px;
    margin-bottom: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .ticket-stats {
      display: flex;
      gap: 60px;

      .stat-item {
        .stat-label {
          font-size: 14px;
          color: #606266;
          margin-bottom: 8px;
        }

        .stat-value {
          font-size: 32px;
          font-weight: 600;
          color: #303133;
          display: flex;
          align-items: baseline;
          gap: 4px;

          .unit {
            font-size: 16px;
            font-weight: 400;
            color: #909399;
          }
        }
      }
    }

    .current-time {
      text-align: right;

      .time-date {
        font-size: 16px;
        color: #303133;
        margin-bottom: 4px;
        font-weight: 500;
      }

      .time-clock {
        font-size: 20px;
        color: #606266;
        font-weight: 500;
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
    .dashboard-header {
      flex-direction: column;
      gap: 20px;
      align-items: flex-start;

      .ticket-stats {
        gap: 40px;
        width: 100%;

        .stat-item .stat-value {
          font-size: 28px;
        }
      }

      .current-time {
        text-align: left;
        width: 100%;
      }
    }
    
    .chart {
      height: 260px;
    }
  }
}
</style>


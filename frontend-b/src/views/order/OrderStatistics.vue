<template>
  <div class="order-statistics">
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">今日订单数</div>
            <div class="stat-value">{{ stats.todayOrders }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">今日销售额</div>
            <div class="stat-value">¥{{ stats.todaySales }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">门票订单</div>
            <div class="stat-value">{{ stats.ticketOrders }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">商城订单</div>
            <div class="stat-value">{{ stats.mallOrders }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>
            <span>订单趋势</span>
          </template>
          <v-chart class="chart" :option="orderTrendOption" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>
            <span>订单状态分布</span>
          </template>
          <v-chart class="chart" :option="orderStatusOption" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
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
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const stats = ref({
  todayOrders: 0,
  todaySales: 0,
  ticketOrders: 0,
  mallOrders: 0
})

const orderTrendOption = computed<EChartsOption>(() => ({
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['门票订单', '商城订单']
  },
  xAxis: {
    type: 'category',
    data: [] as string[]
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: '门票订单',
      data: [] as number[],
      type: 'line',
      smooth: true
    },
    {
      name: '商城订单',
      data: [] as number[],
      type: 'line',
      smooth: true
    }
  ]
}))

const orderStatusOption = computed<EChartsOption>(() => ({
  tooltip: {
    trigger: 'item'
  },
  series: [
    {
      type: 'pie',
      radius: '50%',
      data: [] as any[]
    }
  ]
}))

const loadData = async () => {
  try {
    const data = await statisticsApi.getDashboardData()
    
    // 安全获取数据，防止 undefined 报错
    const ticketOrderData = data.orderTypeRatio?.find((r: any) => r.name === '门票订单')
    const mallOrderData = data.orderTypeRatio?.find((r: any) => r.name === '商城订单')

    stats.value = {
      todayOrders: data.todayOrders || 0,
      todaySales: data.todaySales || 0,
      ticketOrders: ticketOrderData?.value || 0,
      mallOrders: mallOrderData?.value || 0
    }
    
    // 处理图表数据 (确保数组存在)
    const trends = data.orderTrend || []
    
    orderTrendOption.value.xAxis = {
      type: 'category',
      data: trends.map((item: any) => item.date.split('-')[2])
    } as any
    orderTrendOption.value.series = [
      {
        name: '门票订单',
        data: trends.map((item: any) => item.ticket),
        type: 'line',
        smooth: true
      },
      {
        name: '商城订单',
        data: trends.map((item: any) => item.mall),
        type: 'line',
        smooth: true
      }
    ] as any
    
    orderStatusOption.value.series = [{
      type: 'pie',
      radius: '50%',
      data: data.orderStatusRatio || []
    }] as any
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.order-statistics {
  .stats-row {
    margin-bottom: 20px;
  }

  .stat-item {
    text-align: center;

    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-bottom: 10px;
    }

    .stat-value {
      font-size: 32px;
      font-weight: bold;
      color: #303133;
    }
  }

  .chart {
    height: 400px;
  }
}
</style>


<template>
  <div class="sales-report">
    <el-card>
      <template #header>
        <span>销售报表</span>
      </template>

      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="20" class="summary-row">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card>
            <div class="summary-item">
              <div class="summary-label">总销售额</div>
              <div class="summary-value">¥{{ reportData.totalSales }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card>
            <div class="summary-item">
              <div class="summary-label">门票销售额</div>
              <div class="summary-value">¥{{ reportData.ticketSales }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card>
            <div class="summary-item">
              <div class="summary-label">商城销售额</div>
              <div class="summary-value">¥{{ reportData.mallSales }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card>
            <div class="summary-item">
              <div class="summary-label">总订单数</div>
              <div class="summary-value">{{ reportData.totalOrders }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card style="margin-top: 20px">
        <template #header>
          <span>销售趋势</span>
        </template>
        <v-chart class="chart" :option="salesTrendOption" />
      </el-card>

      <el-card style="margin-top: 20px">
        <template #header>
          <span>销售数据明细</span>
        </template>
        <el-table :data="reportData.dailyData" border>
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="sales" label="销售额">
            <template #default="{ row }">¥{{ row.sales }}</template>
          </el-table-column>
          <el-table-column prop="orders" label="订单数" />
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { statisticsApi } from '@/api/statistics'
import type { EChartsOption } from 'echarts'

use([
  CanvasRenderer,
  BarChart,
  TitleComponent,
  TooltipComponent,
  GridComponent
])

const searchForm = reactive({
  dateRange: [] as any[]
})

const reportData = ref({
  totalSales: 0,
  ticketSales: 0,
  mallSales: 0,
  totalOrders: 0,
  dailyData: [] as any[]
})

const salesTrendOption = computed<EChartsOption>(() => ({
  tooltip: {
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: reportData.value.dailyData.map(item => item.date.split('-')[2])
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      data: reportData.value.dailyData.map(item => item.sales),
      type: 'bar',
      itemStyle: {
        color: '#409eff'
      }
    }
  ]
}))

const loadData = async () => {
  try {
    const data = await statisticsApi.getSalesReport({})
    reportData.value = data
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

const handleSearch = () => {
  loadData()
}

const handleReset = () => {
  searchForm.dateRange = []
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.sales-report {
  .search-form {
    margin-bottom: 20px;
  }

  .summary-row {
    margin-bottom: 20px;
  }

  .summary-item {
    text-align: center;

    .summary-label {
      font-size: 14px;
      color: #909399;
      margin-bottom: 10px;
    }

    .summary-value {
      font-size: 24px;
      font-weight: bold;
      color: #303133;
    }
  }

  .chart {
    height: 400px;
  }
}
</style>


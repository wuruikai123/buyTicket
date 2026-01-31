<template>
  <div class="user-statistics">
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">总用户数</div>
            <div class="stat-value">{{ stats.totalUsers }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">今日新增</div>
            <div class="stat-value">{{ stats.todayUsers }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">活跃用户</div>
            <div class="stat-value">{{ stats.activeUsers }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">新增趋势</div>
            <div class="stat-value trend-up">↑ 12%</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="16">
        <el-card>
          <template #header>
            <span>用户注册趋势</span>
          </template>
          <v-chart class="chart" :option="userGrowthOption" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card>
          <template #header>
            <span>用户消费排行榜</span>
          </template>
          <el-table :data="consumptionRank" border>
            <el-table-column prop="rank" label="排名" width="60" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="amount" label="消费金额" width="100">
              <template #default="{ row }">¥{{ row.amount }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
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
  LineChart,
  TitleComponent,
  TooltipComponent,
  GridComponent
])

const stats = ref({
  totalUsers: 0,
  todayUsers: 0,
  activeUsers: 0
})

const consumptionRank = ref<any[]>([])

const userGrowthData = ref<any[]>([])

const userGrowthOption = computed<EChartsOption>(() => ({
  tooltip: {
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: userGrowthData.value.map(item => {
      // 显示完整日期：月-日
      if (item.date) {
        const parts = item.date.split('-')
        return parts.length >= 2 ? `${parts[1]}月${parts[2]}日` : item.date
      }
      return ''
    })
  },
  yAxis: {
    type: 'value',
    name: '注册用户数',
    minInterval: 1  // 用户数必须是整数
  },
  series: [
    {
      data: userGrowthData.value.map(item => item.value),
      type: 'line',
      smooth: true,
      areaStyle: {}
    }
  ]
}))

const loadData = async () => {
  try {
    const data = await statisticsApi.getUserAnalysis({})
    console.log('用户统计API返回数据:', data)
    stats.value = {
      totalUsers: data.totalUsers || 0,
      todayUsers: data.newUsers || 0,
      activeUsers: data.activeUsers || 0
    }
    consumptionRank.value = data.consumptionRank || []
    userGrowthData.value = data.userGrowth || []
    console.log('用户统计数据已更新:', { stats: stats.value, userGrowthData: userGrowthData.value })
  } catch (error) {
    console.error('加载数据失败', error)
    stats.value = {
      totalUsers: 0,
      todayUsers: 0,
      activeUsers: 0
    }
    consumptionRank.value = []
    userGrowthData.value = []
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.user-statistics {
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

      &.trend-up {
        color: #67c23a;
      }
    }
  }

  .chart {
    height: 400px;
  }
}
</style>


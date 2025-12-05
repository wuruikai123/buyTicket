<template>
  <div class="user-analysis">
    <el-card>
      <template #header>
        <span>用户分析</span>
      </template>

      <v-chart class="chart" :option="userGrowthOption" />
    </el-card>
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

const userGrowthOption = computed<EChartsOption>(() => ({
  tooltip: {
    trigger: 'axis'
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
      data: [] as number[],
      type: 'line',
      smooth: true,
      areaStyle: {}
    }
  ]
}))

const loadData = async () => {
  try {
    const data = await statisticsApi.getUserAnalysis({})
    userGrowthOption.value.xAxis = {
      type: 'category',
      data: data.userGrowth.map(item => item.date.split('-')[2])
    } as any
    userGrowthOption.value.series = [{
      data: data.userGrowth.map(item => item.value),
      type: 'line',
      smooth: true,
      areaStyle: {}
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
.user-analysis {
  .chart {
    height: 500px;
  }
}
</style>


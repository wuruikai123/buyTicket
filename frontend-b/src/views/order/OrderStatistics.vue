<template>
  <div class="order-statistics">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单统计</span>
        </div>
      </template>
      <el-empty description="订单统计功能开发中" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.order-statistics {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

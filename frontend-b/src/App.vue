<template>
  <router-view />
</template>

<script setup lang="ts">
</script>

<style>
#app {
  width: 100%;
  height: 100vh;
  background-color: #f5f7fa;
}

/* 全局强制白色表格背景 */
.el-table {
  --el-table-bg-color: #ffffff !important;
  --el-table-tr-bg-color: #ffffff !important;
  --el-table-header-bg-color: #fafafa !important;
  --el-table-text-color: #606266 !important;
  --el-table-header-text-color: #303133 !important;
  background-color: #ffffff !important;
  color: #606266 !important;
}

.el-table tr,
.el-table td,
.el-table th {
  background-color: inherit !important;
}

.el-table__body-wrapper,
.el-table__header-wrapper {
  background-color: #ffffff !important;
}

/* 表格文字颜色 */
.el-table .cell {
  color: #606266 !important;
}

.el-table th .cell {
  color: #303133 !important;
  font-weight: 600 !important;
}

/* 链接按钮在表格中的样式 */
.el-table .el-button--primary.is-link {
  color: #409eff !important;
}

.el-table .el-button--success.is-link {
  color: #67c23a !important;
}

.el-table .el-button--warning.is-link {
  color: #e6a23c !important;
}

.el-table .el-button--danger.is-link {
  color: #f56c6c !important;
}
</style>


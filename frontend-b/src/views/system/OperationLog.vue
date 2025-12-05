<template>
  <div class="operation-log">
    <el-card>
      <template #header>
        <span>操作日志</span>
      </template>

      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="全部" clearable>
            <el-option label="创建" value="CREATE" />
            <el-option label="更新" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="查看" value="VIEW" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源类型">
          <el-select v-model="searchForm.resourceType" placeholder="全部" clearable>
            <el-option label="用户" value="USER" />
            <el-option label="展览" value="EXHIBITION" />
            <el-option label="订单" value="ORDER" />
            <el-option label="商品" value="PRODUCT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="adminName" label="操作人" width="120" />
        <el-table-column prop="operationType" label="操作类型" width="100" />
        <el-table-column prop="resourceType" label="资源类型" width="120" />
        <el-table-column prop="resourceId" label="资源ID" width="100" />
        <el-table-column prop="operationDesc" label="操作描述" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="150" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])

const searchForm = reactive({
  operationType: '',
  resourceType: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const loadData = async () => {
  loading.value = true
  // Mock数据
  setTimeout(() => {
    tableData.value = Array.from({ length: 10 }, (_, i) => ({
      id: i + 1,
      adminName: '管理员',
      operationType: ['CREATE', 'UPDATE', 'DELETE'][i % 3],
      resourceType: ['USER', 'EXHIBITION', 'ORDER'][i % 3],
      resourceId: i + 1,
      operationDesc: '操作描述',
      ipAddress: '192.168.1.1',
      createTime: new Date().toISOString()
    }))
    pagination.total = 100
    loading.value = false
  }, 300)
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.operationType = ''
  searchForm.resourceType = ''
  handleSearch()
}

const handleSizeChange = () => {
  loadData()
}

const handlePageChange = () => {
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.operation-log {
  .search-form {
    margin-bottom: 20px;
  }
}
</style>


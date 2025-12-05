<template>
  <div class="user-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
        </div>
      </template>

      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="用户名/手机号/UID"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="balance" label="余额" width="100">
          <template #default="{ row }">
            ¥{{ row.balance }}
          </template>
        </el-table-column>
        <el-table-column prop="ticketOrderCount" label="门票订单" width="100" />
        <el-table-column prop="mallOrderCount" label="商城订单" width="100" />
        <el-table-column prop="totalAmount" label="总消费" width="100">
          <template #default="{ row }">
            ¥{{ row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">详情</el-button>
            <el-button
              link
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="primary" @click="handleBalance(row)">余额</el-button>
          </template>
        </el-table-column>
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

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="用户详情" width="600px">
      <el-descriptions :column="2" border v-if="currentUser">
        <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ currentUser.nickname }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
        <el-descriptions-item label="余额">¥{{ currentUser.balance }}</el-descriptions-item>
        <el-descriptions-item label="门票订单">{{ currentUser.ticketOrderCount }}</el-descriptions-item>
        <el-descriptions-item label="商城订单">{{ currentUser.mallOrderCount }}</el-descriptions-item>
        <el-descriptions-item label="总消费">¥{{ currentUser.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'">
            {{ currentUser.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ currentUser.registerTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 余额调整对话框 -->
    <el-dialog v-model="balanceDialogVisible" title="调整余额" width="400px">
      <el-form :model="balanceForm" label-width="80px">
        <el-form-item label="当前余额">
          <span>{{ currentUser?.balance }}</span>
        </el-form-item>
        <el-form-item label="调整金额" required>
          <el-input-number
            v-model="balanceForm.amount"
            :precision="2"
            :step="10"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="调整后">
          <span>{{ (currentUser?.balance || 0) + (balanceForm.amount || 0) }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="balanceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBalanceSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api/user'

const loading = ref(false)
const tableData = ref<any[]>([])
const detailDialogVisible = ref(false)
const balanceDialogVisible = ref(false)
const currentUser = ref<any>(null)

const searchForm = reactive({
  keyword: '',
  status: undefined as number | undefined
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const balanceForm = reactive({
  amount: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const data = await userApi.getList({
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    })
    tableData.value = data.records
    pagination.total = data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = undefined
  handleSearch()
}

const handleSizeChange = () => {
  loadData()
}

const handlePageChange = () => {
  loadData()
}

const handleView = async (row: any) => {
  try {
    const data = await userApi.getDetail(row.id)
    currentUser.value = data
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取用户详情失败')
  }
}

const handleToggleStatus = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.status === 1 ? '禁用' : '启用'}该用户吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await userApi.updateStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('操作成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleBalance = (row: any) => {
  currentUser.value = row
  balanceForm.amount = 0
  balanceDialogVisible.value = true
}

const handleBalanceSubmit = async () => {
  if (!currentUser.value) return
  try {
    await userApi.updateBalance(currentUser.value.id, balanceForm.amount)
    ElMessage.success('调整成功')
    balanceDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('调整失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.user-list {
  .card-header {
    font-weight: bold;
    font-size: 16px;
  }

  .search-form {
    margin-bottom: 20px;
  }
}
</style>


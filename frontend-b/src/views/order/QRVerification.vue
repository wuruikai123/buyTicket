<template>
  <div class="qr-verification">
    <el-card>
      <template #header>
        <span>扫码核销</span>
      </template>

      <el-tabs v-model="activeTab" class="verification-tabs">
        <!-- 手动输入核销 -->
        <el-tab-pane label="手动输入" name="manual">
          <div class="manual-section">
            <el-form :model="searchForm" inline class="search-form">
              <el-form-item label="订单号">
                <el-input
                  v-model="searchForm.orderNo"
                  placeholder="请输入订单号"
                  clearable
                  @keyup.enter="handleVerify"
                  style="width: 400px"
                  size="large"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="large" @click="handleVerify">核销</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 扫码核销 -->
        <el-tab-pane label="扫码核销" name="scan">
          <div class="scan-section">
            <div v-if="!isScanning" class="scan-placeholder">
              <el-button type="primary" size="large" @click="startScan">
                <el-icon><Camera /></el-icon>
                <span>开始扫码</span>
              </el-button>
              <p class="scan-tip">点击按钮打开摄像头扫描二维码</p>
            </div>
            <div v-else class="scanner-container">
              <qrcode-stream @decode="onDecode" @init="onInit" />
              <el-button @click="stopScan" class="stop-scan-btn">停止扫描</el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 核销记录 -->
      <div class="records-section">
        <h3>今日核销记录</h3>
        <el-table :data="records" style="width: 100%">
          <el-table-column prop="orderNo" label="订单号" width="200" />
          <el-table-column prop="exhibitionName" label="展览名称" />
          <el-table-column prop="contactName" label="联系人" width="120" />
          <el-table-column prop="verifyTime" label="核销时间" width="180" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag type="success">已核销</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { QrcodeStream } from 'qrcode-reader-vue3'
import request from '@/utils/request'

const activeTab = ref('manual')
const isScanning = ref(false)
const records = ref<any[]>([])

const searchForm = reactive({
  orderNo: ''
})

const handleVerify = async () => {
  if (!searchForm.orderNo.trim()) {
    ElMessage.warning('请输入订单号')
    return
  }
  
  try {
    const res = await request.post('/order/ticket/verify', { 
      orderNo: searchForm.orderNo.trim() 
    })
    ElMessage.success('核销成功')
    searchForm.orderNo = ''
    loadRecords()
  } catch (error: any) {
    ElMessage.error(error.message || '核销失败')
  }
}

// 扫码相关
const startScan = () => {
  isScanning.value = true
}

const stopScan = () => {
  isScanning.value = false
}

const onDecode = async (result: string) => {
  try {
    // 解析二维码数据
    const data = JSON.parse(result)
    if (data.orderNo) {
      stopScan()
      searchForm.orderNo = data.orderNo
      // 自动核销
      await handleVerify()
    } else {
      ElMessage.warning('无效的二维码')
    }
  } catch (e) {
    // 如果不是JSON格式，可能是纯订单号
    if (result && result.startsWith('T')) {
      stopScan()
      searchForm.orderNo = result
      await handleVerify()
    } else {
      ElMessage.error('二维码格式错误')
    }
  }
}

const onInit = async (promise: Promise<any>) => {
  try {
    await promise
  } catch (error: any) {
    if (error.name === 'NotAllowedError') {
      ElMessage.error('需要摄像头权限才能扫码')
    } else if (error.name === 'NotFoundError') {
      ElMessage.error('未检测到摄像头')
    } else {
      ElMessage.error('摄像头初始化失败')
    }
    stopScan()
  }
}

const loadRecords = async () => {
  try {
    const res = await request.get('/order/ticket/records', {
      params: { page: 1, size: 10 }
    })
    records.value = res.records || []
  } catch (e) {
    console.error('加载记录失败', e)
  }
}

onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.qr-verification {
  padding: 20px;
}

.verification-tabs {
  margin-bottom: 30px;
}

.manual-section {
  padding: 40px 20px;
  text-align: center;
}

.search-form {
  justify-content: center;
}

.scan-section {
  padding: 20px;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scan-placeholder {
  text-align: center;
}

.scan-placeholder .el-button {
  margin-bottom: 16px;
}

.scan-tip {
  color: #999;
  font-size: 14px;
}

.scanner-container {
  width: 100%;
  max-width: 500px;
  position: relative;
}

.scanner-container :deep(video) {
  width: 100%;
  border-radius: 8px;
}

.stop-scan-btn {
  margin-top: 16px;
  width: 100%;
}

.records-section {
  margin-top: 30px;
}

.records-section h3 {
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: bold;
}
</style>

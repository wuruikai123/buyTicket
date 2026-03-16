<template>
  <div class="special-ticket-generate">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>特殊票券生成</span>
        </div>
      </template>

      <div class="generate-container">
        <el-form :model="generateForm" label-width="120px" style="max-width: 500px;">
          <el-form-item label="生成数量">
            <el-input-number
              v-model="generateForm.count"
              :min="1"
              :max="10000"
              :step="100"
              style="width: 100%"
            />
            <div style="color: #999; font-size: 12px; margin-top: 8px;">
              建议每次生成不超过1000张，大批量请分多次生成
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              size="large"
              @click="handleGenerateAndDownload" 
              :loading="generating"
              style="width: 200px;"
            >
              {{ generating ? generatingText : '生成并下载二维码' }}
            </el-button>
          </el-form-item>
        </el-form>

        <el-alert
          title="说明"
          type="info"
          :closable="false"
          style="margin-top: 20px;"
        >
          <p>1. 点击按钮后将生成指定数量的特殊票券</p>
          <p>2. 系统会自动为每张票券生成二维码</p>
          <p>3. 所有二维码将打包成ZIP文件下载</p>
          <p>4. 生成的票券可在C端扫码核销</p>
          <p style="color: #e6a23c; font-weight: bold;">⚠️ 建议：单次生成不超过1000张，大批量请分多次生成</p>
        </el-alert>
      </div>
    </el-card>

    <!-- 隐藏的canvas用于生成二维码 -->
    <canvas ref="qrcodeCanvas" style="display: none;"></canvas>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { specialTicketApi } from '@/api/specialTicket'
import QRCode from 'qrcode'
import JSZip from 'jszip'

const generating = ref(false)
const generatingText = ref('')
const qrcodeCanvas = ref<HTMLCanvasElement | null>(null)

const generateForm = reactive({
  count: 1000
})

const handleGenerateAndDownload = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要生成 ${generateForm.count} 张特殊票券并下载二维码吗？生成过程可能需要1-2分钟，请耐心等待。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    generating.value = true
    generatingText.value = '正在生成票券...'
    
    // 1. 调用后端生成票券（增加超时时间）
    try {
      const result: any = await specialTicketApi.batchGenerate(generateForm.count)
      ElMessage.success('票券生成成功，正在准备二维码...')
    } catch (error: any) {
      if (error.message && error.message.includes('timeout')) {
        ElMessage.warning('生成请求已发送，请稍后刷新页面查看')
        return
      }
      throw error
    }
    
    // 2. 等待2秒让后端完成生成
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // 3. 分批获取票券列表（每次100条）
    generatingText.value = '正在获取票券列表...'
    const allTickets: any[] = []
    const pageSize = 100
    let currentPage = 1
    let hasMore = true
    
    while (hasMore && allTickets.length < generateForm.count) {
      try {
        const data: any = await specialTicketApi.getList({
          page: currentPage,
          size: pageSize,
          status: 0 // 只获取未使用的
        })
        
        if (data.records && data.records.length > 0) {
          allTickets.push(...data.records)
          currentPage++
          
          // 如果获取的数量少于pageSize，说明没有更多了
          if (data.records.length < pageSize) {
            hasMore = false
          }
        } else {
          hasMore = false
        }
      } catch (error) {
        console.error('获取票券列表失败', error)
        hasMore = false
      }
    }
    
    if (allTickets.length === 0) {
      ElMessage.error('没有可用的票券')
      return
    }
    
    // 4. 生成二维码并打包（只处理最新生成的票券）
    const ticketsToProcess = allTickets.slice(0, generateForm.count)
    generatingText.value = '正在生成二维码...'
    await generateQRCodesZip(ticketsToProcess)
    
    ElMessage.success('二维码生成并下载成功！')
    
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    generating.value = false
    generatingText.value = ''
  }
}

const generateQRCodesZip = async (tickets: any[]) => {
  const zip = new JSZip()
  const total = tickets.length
  
  for (let i = 0; i < total; i++) {
    const ticket = tickets[i]
    
    // 更新进度
    generatingText.value = `正在生成二维码 ${i + 1}/${total}...`
    
    try {
      // 生成二维码到canvas
      if (!qrcodeCanvas.value) continue
      
      await QRCode.toCanvas(qrcodeCanvas.value, ticket.ticketCode, {
        width: 300,
        margin: 2,
        color: {
          dark: '#000000',
          light: '#FFFFFF'
        }
      })
      
      // 将canvas转换为blob
      const blob = await new Promise<Blob>((resolve) => {
        qrcodeCanvas.value!.toBlob((blob) => {
          resolve(blob!)
        }, 'image/png')
      })
      
      // 添加到zip
      zip.file(`${ticket.ticketCode}.png`, blob)
      
    } catch (error) {
      console.error(`生成二维码失败: ${ticket.ticketCode}`, error)
    }
  }
  
  // 生成zip文件并下载
  generatingText.value = '正在打包下载...'
  const zipBlob = await zip.generateAsync({ type: 'blob' })
  
  const link = document.createElement('a')
  link.href = URL.createObjectURL(zipBlob)
  link.download = `特殊票券二维码_${new Date().getTime()}.zip`
  link.click()
}
</script>

<style scoped lang="scss">
.special-ticket-generate {
  .card-header {
    font-weight: bold;
    font-size: 16px;
  }

  .generate-container {
    padding: 20px;
  }

  :deep(.el-alert) {
    p {
      margin: 8px 0;
      line-height: 1.6;
    }
  }
}
</style>

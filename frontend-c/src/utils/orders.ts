import request from './request'

export type OrderRecord = {
  id: number
  orderNo: string
  exhibition: string
  validTime: string
  buyer: string
  verifyTime?: string
  status: number
}

/**
 * 核销订单
 * 逻辑：查询订单 → 验证状态 → 核销
 * @param orderNo 订单号
 * @returns 核销成功返回true，失败抛出错误
 */
export async function verifyOrder(orderNo: string): Promise<boolean> {
  if (!orderNo || !orderNo.trim()) {
    throw new Error('请输入订单号')
  }
  
  try {
    // 调用后端核销接口
    // 后端会自动：1.查询订单 2.验证状态 3.更新为已使用
    await request.post('/order/ticket/verify', { orderNo: orderNo.trim() })
    return true
  } catch (error: any) {
    console.error('核销失败:', error)
    // 抛出错误，让调用方处理
    throw error
  }
}

// 获取今日核销数量
export async function getTodayVerifiedCount(): Promise<number> {
  try {
    const data = await request.get<number>('/order/ticket/today-count')
    return data || 0
  } catch (error) {
    console.error('获取今日核销数量失败:', error)
    return 0
  }
}

// 获取核销记录列表
export async function listRecordsByDate(year: number, month: number, day: number): Promise<OrderRecord[]> {
  try {
    const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    const data = await request.get<any[]>(`/order/ticket/records?date=${dateStr}`)
    
    return (data || []).map(item => ({
      id: item.id,
      orderNo: item.orderNo,
      exhibition: item.exhibitionName || (item.contactName ? `${item.contactName}的门票订单` : '门票订单'),
      validTime: item.ticketDate && item.timeSlot ? `${item.ticketDate} ${item.timeSlot}` : formatDateTime(item.createTime),
      buyer: item.contactPhone || item.contactName || '未知',
      verifyTime: item.verifyTime ? formatDateTime(item.verifyTime) : undefined,
      status: item.status
    }))
  } catch (error) {
    console.error('获取核销记录失败:', error)
    return []
  }
}

// 格式化日期时间
function formatDateTime(dateTime: string): string {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}年${month}月${day}日 ${hours}:${minutes}`
}




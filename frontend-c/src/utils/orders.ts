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
 * 核销订单或特殊票券（统一接口）
 * 自动识别：T开头=普通订单，ST开头=特殊票券
 * @param code 订单号或票券编号
 * @returns 核销成功返回true，失败抛出错误
 */
export async function verifyOrder(code: string): Promise<boolean> {
  if (!code || !code.trim()) {
    throw new Error('请输入订单号或票券编号')
  }
  
  try {
    // 调用统一核销接口
    // 后端会自动识别类型并处理：
    // - ST开头：特殊票券核销
    // - T开头：普通订单核销
    const adminInfo = getAdminInfo()
    await request.post('/admin/verify/scan', {
      code: code.trim(),
      adminId: adminInfo.adminId,
      adminName: adminInfo.adminName
    })
    return true
  } catch (error: any) {
    console.error('核销失败:', error)
    // 抛出错误，让调用方处理
    throw error
  }
}

/**
 * 获取当前登录的管理员信息
 */
function getAdminInfo() {
  try {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      const user = JSON.parse(userStr)
      return {
        adminId: user.id || null,
        adminName: user.username || user.name || '核销员'
      }
    }
  } catch (error) {
    console.error('获取管理员信息失败:', error)
  }
  
  return {
    adminId: null,
    adminName: '核销员'
  }
}

// 获取今日核销数量
export async function getTodayVerifiedCount(): Promise<number> {
  try {
    const data: any = await request.get('/order/ticket/today-count')
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
    const data: any = await request.get(`/order/ticket/records?date=${dateStr}`)
    
    return (data || []).map((item: any) => ({
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




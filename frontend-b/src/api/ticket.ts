import request from '@/utils/request'

export const ticketApi = {
  getInventoryList(params: any) {
    return request.get('/ticket/inventory/list', { params })
  },

  getInventoryDetail(id: number) {
    return request.get(`/ticket/inventory/${id}`)
  },

  createInventory(data: any) {
    return request.post('/ticket/inventory/create', data)
  },

  updateInventory(data: any) {
    return request.post('/ticket/inventory/update', data)
  },

  deleteInventory(id: number) {
    return request.delete(`/ticket/inventory/${id}`)
  },

  batchCreateInventory(data: {
    exhibitionId: number
    startDate: string
    endDate: string
    timeSlots: string[]
    totalCount: number
  }) {
    return request.post('/ticket/inventory/batch-create', data)
  },

  getWarningList() {
    // 获取库存预警列表（剩余票数少于20%的）
    return request.get('/ticket/inventory/list', { params: { page: 1, size: 100 } })
      .then((data: any) => {
        // 前端过滤出预警数据
        return (data.records || []).filter((item: any) => {
          const remaining = item.totalCount - item.soldCount
          return remaining < item.totalCount * 0.2
        })
      })
  }
}


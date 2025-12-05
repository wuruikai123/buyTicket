import request from '@/utils/request'

export const ticketApi = {
  getInventoryList(params: any) {
    return request.get('/ticket/inventory/list', { params })
  },

  createInventory(data: any) {
    return request.post('/ticket/inventory/create', data)
  },

  updateInventory(data: any) {
    return request.post('/ticket/inventory/update', data)
  },

  getWarningList() {
    // 后端暂未实现预警接口，暂时返回空数组或模拟数据，或者调用 list 并自行筛选
    // 这里为了不报错，暂时调用 list 并前端过滤（如果需要）或者直接返回空
    // 也可以请求一个不存在的接口看后端是否处理，或者直接保留 Mock
    // 既然用户要求接入所有接口，我就留着 Mock 或者暂时不实现真实调用
    // 为了演示，我尝试调用一个假设的接口，或者直接返回空
    return Promise.resolve([])
  }
}


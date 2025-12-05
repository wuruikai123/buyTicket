import request from '@/utils/request'

export const userApi = {
  getList(params: any) {
    return request.get('/user/list', { params })
  },

  getDetail(id: number) {
    return request.get(`/user/${id}`)
  },

  // 后端暂未实现状态和余额修改，先留空或 mock 返回成功
  updateStatus(id: number, status: number) {
    // return request.put(`/user/${id}/status`, { status })
    return Promise.resolve({ success: true })
  },

  updateBalance(id: number, amount: number) {
    // return request.put(`/user/${id}/balance`, { amount })
    return Promise.resolve({ success: true })
  },

  getStatistics(id: number) {
    // return request.get(`/user/${id}/statistics`)
    return Promise.resolve({
      ticketOrderCount: 0,
      mallOrderCount: 0,
      totalAmount: 0
    })
  }
}


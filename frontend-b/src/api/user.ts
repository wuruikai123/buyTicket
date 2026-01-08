import request from '@/utils/request'

export const userApi = {
  getList(params: any) {
    return request.get('/api/v1/admin/user/list', { params })
  },

  getDetail(id: number) {
    return request.get(`/api/v1/admin/user/${id}`)
  },

  updateStatus(id: number, status: number) {
    return request.put(`/api/v1/admin/user/${id}/status?status=${status}`)
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


import request from '@/utils/request'

export const orderApi = {
  // 门票订单 - 管理端
  getTicketOrderList(params: any) {
    return request.get('/api/v1/admin/order/ticket/list', { params })
  },

  getTicketOrderDetail(id: number) {
    return request.get(`/api/v1/admin/order/ticket/${id}`)
  },

  cancelTicketOrder(id: number) {
    return request.post(`/api/v1/admin/order/ticket/${id}/cancel`, {})
  },

  verifyTicketOrder(id: number) {
    return request.post(`/api/v1/admin/order/ticket/${id}/verify`, {})
  },

  // 商城订单 - 管理端
  getMallOrderList(params: any) {
    return request.get('/api/v1/admin/order/mall/list', { params })
  },

  getMallOrderDetail(id: number) {
    return request.get(`/api/v1/admin/order/mall/${id}`)
  },

  cancelMallOrder(id: number) {
    return request.post(`/api/v1/admin/order/mall/${id}/cancel`, {})
  },

  shipMallOrder(id: number, data: { logisticsCompany: string; logisticsNo: string }) {
    return request.post(`/api/v1/admin/order/mall/${id}/ship`, data)
  },

  completeMallOrder(id: number) {
    return request.post(`/api/v1/admin/order/mall/${id}/complete`, {})
  }
}


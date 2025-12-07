import request from '@/utils/request'

export const orderApi = {
  // 门票订单（baseURL已经是/api/v1/admin，所以路径是/order/ticket/...）
  getTicketOrderList(params: any) {
    return request.get('/order/ticket/list', { params })
  },

  getTicketOrderDetail(id: number) {
    return request.get(`/order/ticket/${id}`)
  },

  cancelTicketOrder(id: number) {
    return request.put(`/order/ticket/${id}/cancel`)
  },

  verifyTicketOrder(id: number) {
    return request.put(`/order/ticket/${id}/verify`)
  },

  // 商城订单
  getMallOrderList(params: any) {
    return request.get('/order/mall/list', { params })
  },

  getMallOrderDetail(id: number) {
    return request.get(`/order/mall/${id}`)
  },

  cancelMallOrder(id: number) {
    return request.put(`/order/mall/${id}/cancel`)
  },

  shipMallOrder(id: number, data: { logisticsCompany: string; logisticsNo: string }) {
    return request.put(`/order/mall/${id}/ship`, data)
  },

  completeMallOrder(id: number) {
    return request.put(`/order/mall/${id}/complete`)
  }
}


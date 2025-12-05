import request from '@/utils/request'

export const orderApi = {
  // 门票订单
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
    return request.put(`/order/mall/${id}/ship`, data) // 后端接口可能不接收 body，只看 url，或者看 Controller 实现。
    // Controller 实现：public JsonData shipMallOrder(@PathVariable Long id) { ... } 没接收 body。
    // 为了匹配后端，可能需要修改后端 AdminOrderController 接收参数，或者暂时只改状态。
    // 这里的实现 AdminOrderController.java: shipMallOrder 只更新了 status，没更新物流信息。
    // 所以 data 参数其实没用上。
  },

  completeMallOrder(id: number) {
    return request.put(`/order/mall/${id}/complete`)
  }
}


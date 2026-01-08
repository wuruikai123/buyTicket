import request from '@/utils/request'

export interface AlipayCreateRequest {
  orderNo: string
  returnUrl?: string
}

export const paymentApi = {
  // 创建支付宝支付（PC网页支付）
  createAlipayPc(data: AlipayCreateRequest) {
    return request.post<any, string>('/payment/alipay/create', null, { 
      params: { orderNo: data.orderNo } 
    })
  },

  // 创建支付宝支付（手机网页支付）
  createAlipayWap(data: AlipayCreateRequest) {
    return request.post<any, string>('/payment/alipay/create-wap', null, { 
      params: { orderNo: data.orderNo } 
    })
  },

  // 查询支付状态
  queryPayment(params: { orderNo: string }) {
    return request.get<any, { status: string; tradeNo?: string }>('/payment/alipay/query', { params })
  }
}

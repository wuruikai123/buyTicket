import request from '@/utils/request'

export interface HuifuPayRequest {
  orderNo: string
  payType: 'ALIPAY'
}

export const huifuPayApi = {
  createPayment(orderNo: string, payType: 'ALIPAY') {
    return request.post<any, { pay_url: string; order_no: string }>(
      '/huifu-pay/create',
      null,
      {
        params: { orderNo, payType }
      }
    )
  },

  queryPaymentStatus(orderNo: string, payType?: 'ALIPAY') {
    return request.get<any, any>('/huifu-pay/query', {
      params: { orderNo }
    })
  },

  refund(orderNo: string, refundReason?: string) {
    return request.post<any, any>('/huifu-pay/refund', null, {
      params: { orderNo, refundReason }
    })
  }
}

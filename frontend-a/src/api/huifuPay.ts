import request from '@/utils/request'

export interface HuifuPayRequest {
  orderNo: string
  payType: 'WECHAT' | 'ALIPAY'
}

export const huifuPayApi = {
  /**
   * 创建支付订单（H5页面支付）
   * @param orderNo 订单号
   * @param payType 支付类型：WECHAT(微信) 或 ALIPAY(支付宝)
   * @returns 支付URL
   */
  createPayment(orderNo: string, payType: 'WECHAT' | 'ALIPAY') {
    return request.post<any, { pay_url: string; order_no: string }>(
      '/huifu-pay/create',
      null,
      {
        params: { orderNo, payType }
      }
    )
  },

  /**
   * 查询支付状态
   * @param orderNo 订单号
   * @returns 支付状态信息
   */
  queryPaymentStatus(orderNo: string) {
    return request.get<any, any>('/huifu-pay/query', {
      params: { orderNo }
    })
  },

  /**
   * 申请退款
   * @param orderNo 订单号
   * @param refundReason 退款原因
   * @returns 退款结果
   */
  refund(orderNo: string, refundReason?: string) {
    return request.post<any, any>('/huifu-pay/refund', null, {
      params: { orderNo, refundReason }
    })
  }
}

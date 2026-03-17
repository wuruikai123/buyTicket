import request from '@/utils/request'

export interface AlipayCreateRequest {
  orderNo: string
  returnUrl?: string
}

export interface WechatCreateRequest {
  orderNo: string
  returnUrl?: string
}

export const paymentApi = {
  // 创建支付（使用汇付宝 - 自动判断设备类型）
  createPayment(orderNo: string) {
    // 检查是否是移动设备
    const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
    
    // 使用汇付宝支付宝接口
    const payType = 'ALIPAY'
    return request.post<any, { pay_url: string; order_no: string }>('/huifu-pay/create', null, { 
      params: { orderNo, payType } 
    })
  },

  // 创建支付宝支付（PC网页支付 - 使用汇付宝）
  createAlipayPc(data: AlipayCreateRequest) {
    return request.post<any, { pay_url: string; order_no: string }>('/huifu-pay/create', null, { 
      params: { orderNo: data.orderNo, payType: 'ALIPAY' } 
    })
  },

  // 创建支付宝支付（手机网页支付 - 使用汇付宝）
  createAlipayWap(data: AlipayCreateRequest) {
    return request.post<any, { pay_url: string; order_no: string }>('/huifu-pay/create', null, { 
      params: { orderNo: data.orderNo, payType: 'ALIPAY' } 
    })
  },

  // 创建微信支付（使用汇付宝）
  createWechatPay(data: WechatCreateRequest) {
    return request.post<any, { pay_url: string; order_no: string }>('/huifu-pay/create', null, { 
      params: { orderNo: data.orderNo, payType: 'WECHAT' } 
    })
  },

  // 查询支付状态（使用汇付宝）
  queryPayment(params: { orderNo: string }) {
    return request.get<any, { status: string; tradeNo?: string }>('/huifu-pay/query', { params })
  }
}

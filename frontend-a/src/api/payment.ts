import request from '@/utils/request'

export interface AlipayCreateRequest {
  orderNo: string
  returnUrl?: string
}

export interface WechatJsapiCreateRequest {
  orderNo: string
  code: string
}

export const paymentApi = {
  // 创建支付（使用汇付宝 - 自动判断设备类型）
  createPayment(orderNo: string) {
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

  // 获取微信JSAPI授权地址
  getWechatOauthUrl(params: { orderNo: string; redirectUri: string; state?: string }) {
    return request.get<any, { oauth_url: string }>('/wechat-pay/oauth-url', { params })
  },

  // 获取微信授权地址（前端兼容别名）
  getWechatOAuthUrl(params: { orderNo: string; redirectUri: string; state?: string }) {
    return paymentApi.getWechatOauthUrl(params)
  },

  // 创建微信JSAPI支付订单，返回wx.chooseWXPay所需参数
  createWechatJsapiPay(data: WechatJsapiCreateRequest) {
    return request.post<any, {
      appId: string
      timeStamp: string
      nonceStr: string
      package: string
      signType: string
      paySign: string
      prepay_id: string
      order_no: string
    }>('/wechat-pay/create-jsapi', null, {
      params: {
        orderNo: data.orderNo,
        code: data.code
      }
    })
  },

  // 查询微信支付状态（直连微信支付）
  queryWechatPayment(params: { orderNo: string }) {
    return request.get<any, { paid: boolean; trade_state?: string; transaction_id?: string }>('/wechat-pay/query', { params })
  },

  // 查询支付状态（支付宝仍使用汇付）
  queryPayment(params: { orderNo: string }) {
    return request.get<any, { status: string; tradeNo?: string }>('/huifu-pay/query', { params })
  }
}

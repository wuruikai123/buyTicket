import request from '@/utils/request'

export interface AlipayCreateRequest {
  orderNo: string
  returnUrl?: string
}

export interface WechatCreateRequest {
  orderNo: string
  returnUrl?: string
}

const WECHAT_APP_ID = 'wxf5b4629dc8260f04'

// 发起微信网页授权，获取 code（会跳转离开当前页）
export function redirectToWechatAuth(orderNo: string) {
  const redirectUri = encodeURIComponent(`${window.location.origin}/wechat-pay-callback?orderNo=${orderNo}`)
  const authUrl = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${WECHAT_APP_ID}&redirect_uri=${redirectUri}&response_type=code&scope=snsapi_base&state=${orderNo}#wechat_redirect`
  window.location.href = authUrl
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

  // 用微信授权 code 换取 openid
  getWechatOpenId(code: string) {
    return request.get<any, { openid: string; sub_appid: string }>('/huifu-pay/wechat-openid', { params: { code } })
  },

  // 创建微信支付（需先调用 redirectToWechatAuth 获取 code，再换 openid，最后调此接口）
  createWechatPay(data: WechatCreateRequest & { subOpenId: string }) {
    return request.post<any, { pay_url: string; order_no: string }>('/huifu-pay/create', null, { 
      params: { 
        orderNo: data.orderNo, 
        payType: 'WECHAT',
        subAppId: WECHAT_APP_ID,
        subOpenId: data.subOpenId
      } 
    })
  },

  // 查询支付状态（使用汇付宝）
  queryPayment(params: { orderNo: string }) {
    return request.get<any, { status: string; tradeNo?: string }>('/huifu-pay/query', { params })
  }
}

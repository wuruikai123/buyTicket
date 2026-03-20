package com.buyticket.service;

import java.util.Map;

/**
 * 汇付宝支付服务接口
 */
public interface HuifuPayService {

    /**
     * 创建支付订单
     * @param orderNo 订单号
     * @param amount 支付金额（单位：元）
     * @param subject 商品标题
     * @param payType 支付类型：WECHAT(微信) 或 ALIPAY(支付宝)
     * @param subAppId 微信小程序AppID（T_MINIAPP时必填，其他可传null）
     * @param subOpenId 微信用户OpenID（T_MINIAPP时必填，其他可传null）
     * @return 支付URL或二维码字符串
     */
    String createPayment(String orderNo, String amount, String subject, String payType, String subAppId, String subOpenId);

    /**
     * 查询支付状态
     * @param orderNo 订单号
     * @return 支付状态信息
     */
    Map<String, Object> queryPaymentStatus(String orderNo);

    /**
     * 验证支付回调签名
     * @param params 回调参数
     * @return 签名是否有效
     */
    boolean verifyNotify(Map<String, String> params);

    /**
     * 申请退款
     * @param orderNo 订单号
     * @param refundAmount 退款金额
     * @param reason 退款原因
     * @return 退款结果
     */
    Map<String, Object> refund(String orderNo, String refundAmount, String reason);
}

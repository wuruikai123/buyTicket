package com.buyticket.service;

import java.util.Map;

/**
 * 支付宝支付服务接口
 */
public interface AlipayService {
    
    /**
     * 创建支付订单（PC端网页支付）
     * @param orderNo 订单号
     * @param subject 订单标题
     * @param totalAmount 订单金额
     * @param body 订单描述
     * @return 支付表单HTML
     */
    String createPayment(String orderNo, String subject, String totalAmount, String body) throws Exception;
    
    /**
     * 创建手机网站支付订单
     * @param orderNo 订单号
     * @param subject 订单标题
     * @param totalAmount 订单金额
     * @param body 订单描述
     * @return 支付表单HTML
     */
    String createWapPayment(String orderNo, String subject, String totalAmount, String body) throws Exception;
    
    /**
     * 查询订单支付状态
     * @param orderNo 订单号
     * @return 订单信息
     */
    Map<String, String> queryPayment(String orderNo) throws Exception;
    
    /**
     * 退款
     * @param orderNo 订单号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款结果
     */
    Map<String, String> refund(String orderNo, String refundAmount, String refundReason) throws Exception;
    
    /**
     * 验证支付宝回调签名
     * @param params 回调参数
     * @return 是否验证通过
     */
    boolean verifyNotify(Map<String, String> params) throws Exception;
}

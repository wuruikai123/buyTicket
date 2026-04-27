package com.buyticket.service.impl;

import com.buyticket.config.HuifuPayConfig;
import com.buyticket.service.HuifuPayService;
import com.buyticket.utils.RsaUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huifu.bspay.sdk.opps.core.BasePay;
import com.huifu.bspay.sdk.opps.core.config.MerConfig;
import com.huifu.bspay.sdk.opps.client.BasePayClient;
import com.huifu.bspay.sdk.opps.core.request.V3TradePaymentJspayRequest;
import com.huifu.bspay.sdk.opps.core.request.V2TradePaymentScanpayQueryRequest;
import com.huifu.bspay.sdk.opps.core.request.V2TradePaymentScanpayRefundRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class HuifuPayServiceImpl implements HuifuPayService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        try {
            MerConfig merConfig = new MerConfig();
            merConfig.setSysId(HuifuPayConfig.appId);
            merConfig.setProcutId(HuifuPayConfig.productId);
            merConfig.setRsaPrivateKey(HuifuPayConfig.merchantPrivateKey);
            merConfig.setRsaPublicKey(HuifuPayConfig.huifuPublicKey);
            BasePay.debug = true;
            BasePay.prodMode = BasePay.MODE_PROD;
            BasePay.initWithMerConfig(merConfig);
            log.info("Huifu SDK initialized: sys_id={}, product_id={}", HuifuPayConfig.appId, HuifuPayConfig.productId);
        } catch (Exception e) {
            log.error("Huifu SDK init failed", e);
        }
    }

    @Override
    public String createPayment(String orderNo, String amount, String subject, String payType, String ignoredSubAppId, String ignoredSubOpenId) {
        try {
            log.info("Creating payment: orderNo={}, amount={}, payType={}", orderNo, amount, payType);
            String tradeType;
            String productId;
            if ("ALIPAY".equals(payType)) {
                tradeType = "A_NATIVE";
                productId = "PAYUN";
            } else {
                throw new RuntimeException("Unsupported pay type: " +
                 payType);
            }
            String reqDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String reqSeqId = reqDate + orderNo; // 固定格式，便于查单时精确匹配

            // Re-init SDK with correct product_id for this pay type
            try {
                MerConfig merConfig = new MerConfig();
                merConfig.setSysId(HuifuPayConfig.appId);
                merConfig.setProcutId(productId);
                merConfig.setRsaPrivateKey(HuifuPayConfig.merchantPrivateKey);
                merConfig.setRsaPublicKey(HuifuPayConfig.huifuPublicKey);
                BasePay.initWithMerConfig(merConfig);
                log.info("SDK re-initialized with product_id={}", productId);
            } catch (Exception e) {
                log.warn("SDK re-init failed, continuing", e);
            }

            V3TradePaymentJspayRequest request = new V3TradePaymentJspayRequest();
            request.setReqDate(reqDate);
            request.setReqSeqId(reqSeqId);
            request.setHuifuId(HuifuPayConfig.merchantId);
            request.setGoodsDesc(subject);
            request.setTradeType(tradeType);
            request.setTransAmt(formatAmount(amount));
            request.addExtendInfo("notify_url", HuifuPayConfig.notifyUrl);

            log.info("Calling Huifu SDK jspay: reqSeqId={}, tradeType={}, amt={}", reqSeqId, tradeType, amount);
            Map<String, Object> response = BasePayClient.request(request);
            log.info("Huifu SDK response: {}", response);

            return parsePayUrl(response, tradeType);
        } catch (Exception e) {
            log.error("createPayment failed", e);
            throw new RuntimeException("createPayment failed: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> queryPaymentStatus(String orderNo) {
        try {
            String reqDate;
            try {
                long ts = Long.parseLong(orderNo.substring(1, 14));
                reqDate = new java.text.SimpleDateFormat("yyyyMMdd")
                        .format(new java.util.Date(ts));
            } catch (Exception ex) {
                reqDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
            String orgReqSeqId = reqDate + orderNo;

            // 查单前确保 SDK 用正确的 product_id 初始化
            try {
                MerConfig merConfig = new MerConfig();
                merConfig.setSysId(HuifuPayConfig.appId);
                merConfig.setProcutId(HuifuPayConfig.productId);
                merConfig.setRsaPrivateKey(HuifuPayConfig.merchantPrivateKey);
                merConfig.setRsaPublicKey(HuifuPayConfig.huifuPublicKey);
                BasePay.initWithMerConfig(merConfig);
            } catch (Exception e) {
                log.warn("SDK re-init for query failed", e);
            }

            V2TradePaymentScanpayQueryRequest request = new V2TradePaymentScanpayQueryRequest();
            request.setHuifuId(HuifuPayConfig.merchantId);
            request.setOrgReqDate(reqDate);
            request.setOrgReqSeqId(orgReqSeqId);
            Map<String, Object> response = BasePayClient.request(request);
            log.info("Query response for orderNo={}: {}", orderNo, response);
            return response;
        } catch (Exception e) {
            log.error("queryPaymentStatus failed", e);
            throw new RuntimeException("queryPaymentStatus failed: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyNotify(Map<String, String> params) {
        try {
            String sign = params.get("sign");
            if (sign == null || sign.isEmpty()) return false;
            String respData = params.get("resp_data");
            if (respData == null) return false;
            return RsaUtils.verify(HuifuPayConfig.huifuPublicKey, respData, sign);
        } catch (Exception e) {
            log.error("verifyNotify failed", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> refund(String orderNo, String refundAmount, String reason) {
        try {
            String reqDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String orgReqDate;
            try {
                long ts = Long.parseLong(orderNo.substring(1, 14));
                orgReqDate = new java.text.SimpleDateFormat("yyyyMMdd")
                        .format(new java.util.Date(ts));
            } catch (Exception ex) {
                orgReqDate = reqDate;
            }
            String orgReqSeqId = orgReqDate + orderNo;

            // 退款请求流水号必须全局唯一，否则汇付会返回“重复交易”
            String uniqueRefundSeqId = reqDate + "R" + System.currentTimeMillis() + new java.util.Random().nextInt(1000);

            V2TradePaymentScanpayRefundRequest request = new V2TradePaymentScanpayRefundRequest();
            request.setReqDate(reqDate);
            request.setReqSeqId(uniqueRefundSeqId);
            request.setHuifuId(HuifuPayConfig.merchantId);
            request.setOrgReqDate(orgReqDate);
            request.addExtendInfo("org_req_seq_id", orgReqSeqId);
            request.setOrdAmt(formatAmount(refundAmount));
            request.addExtendInfo("remark", reason);
            Map<String, Object> response = BasePayClient.request(request);
            log.info("Refund response for orderNo={}: {}", orderNo, response);
            return response;
        } catch (Exception e) {
            log.error("refund failed", e);
            throw new RuntimeException("refund failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String parsePayUrl(Map<String, Object> response, String tradeType) {
        // SDK returns the data fields at top level (resp_code, resp_desc, pay_info, etc.)
        // The outer response may also have a nested "data" map - check both
        Map<String, Object> data;
        Object dataObj = response.get("data");
        if (dataObj instanceof Map) {
            data = (Map<String, Object>) dataObj;
        } else if (dataObj instanceof String) {
            try {
                data = objectMapper.readValue((String) dataObj, Map.class);
            } catch (Exception e) {
                data = response; // fallback to top-level
            }
        } else {
            // No "data" field: fields are at top level
            data = response;
        }

        String respCode = (String) data.get("resp_code");
        String respDesc = (String) data.get("resp_desc");
        log.info("resp_code: {}, resp_desc: {}", respCode, respDesc);

        // 00000000 = success, 00000100 = processing, 20000000 = duplicate (treat as success)
        if (!"00000000".equals(respCode) && !"00000100".equals(respCode) && !"20000000".equals(respCode)) {
            throw new RuntimeException("Huifu error: " + respCode + " - " + respDesc);
        }

        // pay_info contains the redirect URL for H5 payments
        String payInfo = (String) data.get("pay_info");
        if (payInfo != null && !payInfo.isEmpty()) {
            if (payInfo.startsWith("http")) return payInfo;
            try {
                Map<String, Object> payInfoMap = objectMapper.readValue(payInfo, Map.class);
                for (String key : new String[]{"h5_url", "mweb_url", "pay_url", "url"}) {
                    Object v = payInfoMap.get(key);
                    if (v instanceof String && !((String) v).isEmpty()) return (String) v;
                }
            } catch (Exception ignored) {}
        }

        String qrCode = (String) data.get("qr_code");
        if (qrCode != null && !qrCode.isEmpty()) return qrCode;

        // 20000000 duplicate: the original payment was created successfully,
        // but we can't retrieve the URL again. Need a new req_seq_id.
        if ("20000000".equals(respCode)) {
            throw new RuntimeException("重复交易: 请使用新的订单号重试");
        }

        throw new RuntimeException("Cannot get pay URL from response: " + response);
    }

    private String formatAmount(String amount) {
        try {
            return String.format("%.2f", Double.parseDouble(amount));
        } catch (Exception e) {
            return amount;
        }
    }
}

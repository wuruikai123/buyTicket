package com.buyticket.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.buyticket.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {
    
    // 从配置文件读取阿里云AccessKey
    @Value("${aliyun.sms.accessKeyId:}")
    private String accessKeyId;
    
    @Value("${aliyun.sms.accessKeySecret:}")
    private String accessKeySecret;
    
    @Value("${aliyun.sms.signName:元星河文化}")
    private String signName;
    
    @Value("${aliyun.sms.templateCode:SMS_501865326}")
    private String templateCode;
    
    @Value("${aliyun.sms.enabled:false}")
    private boolean smsEnabled;
    
    // 存储验证码：key=手机号，value=验证码信息
    private final Map<String, CodeInfo> codeStore = new ConcurrentHashMap<>();
    
    // 验证码信息类
    private static class CodeInfo {
        String code;
        long expireTime;
        long sendTime;
        
        CodeInfo(String code, long expireTime, long sendTime) {
            this.code = code;
            this.expireTime = expireTime;
            this.sendTime = sendTime;
        }
    }
    
    @Override
    public boolean sendVerificationCode(String phone) {
        // 检查是否在60秒冷却期内
        CodeInfo existingCode = codeStore.get(phone);
        if (existingCode != null) {
            long now = System.currentTimeMillis();
            if (now - existingCode.sendTime < 60000) {
                log.warn("手机号 {} 请求过于频繁", phone);
                throw new RuntimeException("请求过于频繁，请稍后再试");
            }
        }
        
        // 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // 设置过期时间为5分钟
        long expireTime = System.currentTimeMillis() + 5 * 60 * 1000;
        long sendTime = System.currentTimeMillis();
        
        // 存储验证码
        codeStore.put(phone, new CodeInfo(code, expireTime, sendTime));
        
        // 发送短信
        if (smsEnabled && accessKeyId != null && !accessKeyId.isEmpty()) {
            // 生产环境：调用阿里云短信API
            try {
                sendSmsViaAliyun(phone, code);
                log.info("短信发送成功: phone={}", phone);
            } catch (Exception e) {
                log.error("短信发送失败: phone={}", phone, e);
                // 发送失败也返回true，因为验证码已经存储，可以用于测试
                // 生产环境可以改为 throw new RuntimeException("短信发送失败");
            }
        } else {
            // 开发环境：只打印日志
            log.info("【开发模式】发送验证码到手机号 {}: {}", phone, code);
            log.info("【开发模式】短信模板CODE: {}", templateCode);
        }
        
        return true;
    }
    
    /**
     * 调用阿里云短信API发送短信
     */
    private void sendSmsViaAliyun(String phone, String code) throws Exception {
        // 创建阿里云短信客户端
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint("dysmsapi.aliyuncs.com");

        Client client = new Client(config);

        // 构建请求，发送短信时需要明确包含签名和模板参数 JSON
        SendSmsRequest request = new SendSmsRequest()
                .setPhoneNumbers(phone)
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setTemplateParam("{\"code\":\"" + code + "\"}");

        try {
            // 发送短信
            SendSmsResponse response = client.sendSms(request);

            // 检查发送结果
            if (!"OK".equals(response.getBody().getCode())) {
                log.error("阿里云短信发送失败: phone={}, code={}, message={}",
                        phone, response.getBody().getCode(), response.getBody().getMessage());
                throw new RuntimeException("短信发送失败: " + response.getBody().getMessage());
            }

            log.info("阿里云短信发送成功: phone={}, requestId={}", phone, response.getBody().getRequestId());
        } catch (com.aliyun.tea.TeaException e) {
            log.error("阿里云短信签名/请求失败: phone={}, errorCode={}, message={}", phone, e.getCode(), e.getMessage(), e);
            throw new RuntimeException("短信签名或请求参数错误: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean verifyCode(String phone, String code) {
        CodeInfo codeInfo = codeStore.get(phone);
        
        if (codeInfo == null) {
            log.warn("手机号 {} 的验证码不存在", phone);
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > codeInfo.expireTime) {
            log.warn("手机号 {} 的验证码已过期", phone);
            codeStore.remove(phone);
            return false;
        }
        
        // 验证码匹配
        if (codeInfo.code.equals(code)) {
            // 验证成功后删除验证码
            codeStore.remove(phone);
            log.info("手机号 {} 验证码验证成功", phone);
            return true;
        }
        
        log.warn("手机号 {} 验证码错误", phone);
        return false;
    }
}

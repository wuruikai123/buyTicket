package com.buyticket.config;

import com.buyticket.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 配置JWT拦截器和CORS跨域
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtInterceptor())
                .addPathPatterns("/api/v1/**")
                // 排除不需要认证的接口
                .excludePathPatterns(
                        // 用户登录注册
                        "/api/v1/user/login",
                        "/api/v1/user/register",
                        "/api/v1/user/hello",
                        // 短信验证码登录
                        "/api/v1/user/sms/send",
                        "/api/v1/user/login/sms",
                        // 管理员登录
                        "/api/v1/admin/auth/login",
                        // 公开接口 - 展览
                        "/api/v1/exhibition/list",
                        "/api/v1/exhibition/detail/**",
                        // 公开接口 - 商城
                        "/api/v1/mall/product/list",
                        "/api/v1/mall/product/detail/**",
                        // 公开接口 - 轮播图
                        "/api/v1/banner/list",
                        // 公开接口 - 关于展厅
                        "/api/v1/about/**",
                        // 公开接口 - 票务查询
                        "/api/v1/ticket/availability",
                        // 公开接口 - 系统配置
                        "/api/v1/system-config/**",
                        // 支付宝回调接口（重要：支付宝服务器调用时没有Token）
                        "/api/v1/payment/alipay/notify",
                        "/api/v1/payment/alipay/return",
                        // 汇付宝回调接口（重要：汇付宝服务器调用时没有Token）
                        "/api/v1/huifu-pay/notify",
                        "/api/v1/huifu-pay/return",
                        // 汇付宝查单接口（前端轮询，用户可能未登录状态）
                        "/api/v1/huifu-pay/query",
                        "/api/v1/huifu-pay/create",
                        // 静态资源 - 上传的图片文件
                        "/uploads/**",
                        // Swagger文档
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

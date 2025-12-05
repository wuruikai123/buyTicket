package com.buyticket.config;

import com.buyticket.interceptor.AdminLoginInterceptor;
import com.buyticket.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Bean
    public LoginInterceptor loginInterceptor() {
        return new LoginInterceptor();
    }
    
    @Bean
    public AdminLoginInterceptor adminLoginInterceptor() {
        return new AdminLoginInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // C端用户拦截
        registry.addInterceptor(loginInterceptor())
                .addPathPatterns("/api/v1/user/**", "/api/v1/order/**", "/api/v1/cart/**")
                .excludePathPatterns(
                        "/api/v1/user/login", 
                        "/api/v1/user/register",
                        "/api/v1/user/hello",
                        "/api/v1/exhibition/**", 
                        "/api/v1/sys_product/**" 
                );
                
        // 管理端拦截
        registry.addInterceptor(adminLoginInterceptor())
                .addPathPatterns("/api/v1/admin/**")
                .excludePathPatterns(
                        "/api/v1/admin/auth/login"
                );
    }
}

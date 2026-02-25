package com.buyticket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 静态资源配置
 * 配置文件上传目录的访问路径
 */
@Configuration
public class ResourceConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取绝对路径
        File uploadDir = new File(uploadPath);
        if (!uploadDir.isAbsolute()) {
            // 如果是相对路径，转换为绝对路径
            uploadDir = new File(System.getProperty("user.dir"), uploadPath);
        }
        
        String absolutePath = uploadDir.getAbsolutePath();
        
        // 确保目录存在
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            System.out.println("创建上传目录: " + absolutePath);
        }
        
        // 配置上传文件的访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath + File.separator);
        
        System.out.println("========================================");
        System.out.println("静态资源配置:");
        System.out.println("  访问路径: /uploads/**");
        System.out.println("  物理路径: " + absolutePath);
        System.out.println("  目录存在: " + uploadDir.exists());
        System.out.println("  可读: " + uploadDir.canRead());
        System.out.println("========================================");
    }
}

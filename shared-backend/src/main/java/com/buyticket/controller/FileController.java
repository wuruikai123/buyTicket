package com.buyticket.controller;

import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/file")
public class FileController {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    /**
     * 上传图片（普通用户接口）
     */
    @PostMapping("/upload")
    public JsonData uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return JsonData.buildError("文件不能为空");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return JsonData.buildError("只能上传图片文件");
        }

        // 检查文件大小（10MB）
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return JsonData.buildError("图片大小不能超过10MB");
        }

        try {
            // 创建上传目录
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = Paths.get(uploadPath, filename);
            Files.copy(file.getInputStream(), filePath);

            // 返回文件访问URL
            Map<String, String> result = new HashMap<>();
            result.put("url", "/uploads/" + filename);
            result.put("filename", filename);

            return JsonData.buildSuccess(result);
        } catch (IOException e) {
            e.printStackTrace();
            return JsonData.buildError("文件上传失败：" + e.getMessage());
        }
    }
}

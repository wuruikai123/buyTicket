package com.buyticket.controller.admin;

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
@RequestMapping("/api/v1/admin/file")
public class AdminFileController {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    /**
     * 上传图片
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

    /**
     * 删除图片
     */
    @DeleteMapping("/delete")
    public JsonData deleteImage(@RequestParam("filename") String filename) {
        try {
            Path filePath = Paths.get(uploadPath, filename);
            Files.deleteIfExists(filePath);
            return JsonData.buildSuccess("删除成功");
        } catch (IOException e) {
            e.printStackTrace();
            return JsonData.buildError("删除失败：" + e.getMessage());
        }
    }

    /**
     * 测试接口 - 检查上传目录和文件
     */
    @GetMapping("/test")
    public JsonData testUpload() {
        Map<String, Object> result = new HashMap<>();
        
        // 检查上传目录
        File uploadDir = new File(uploadPath);
        result.put("uploadPath", uploadDir.getAbsolutePath());
        result.put("uploadDirExists", uploadDir.exists());
        result.put("uploadDirIsDirectory", uploadDir.isDirectory());
        
        // 列出目录中的文件
        if (uploadDir.exists() && uploadDir.isDirectory()) {
            File[] files = uploadDir.listFiles();
            result.put("fileCount", files != null ? files.length : 0);
            if (files != null && files.length > 0) {
                String[] fileNames = new String[Math.min(files.length, 10)];
                for (int i = 0; i < fileNames.length; i++) {
                    fileNames[i] = files[i].getName();
                }
                result.put("recentFiles", fileNames);
            }
        }
        
        return JsonData.buildSuccess(result);
    }
}

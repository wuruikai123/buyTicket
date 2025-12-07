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
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/upload")
public class AdminUploadController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * 上传单张图片
     */
    @PostMapping("/image")
    public JsonData uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return JsonData.buildError("请选择要上传的文件");
        }

        try {
            String result = saveFile(file);
            Map<String, String> data = new HashMap<>();
            data.put("url", result);
            data.put("filename", file.getOriginalFilename());
            return JsonData.buildSuccess(data);
        } catch (IOException e) {
            return JsonData.buildError("上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传多张图片
     */
    @PostMapping("/images")
    public JsonData uploadImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return JsonData.buildError("请选择要上传的文件");
        }

        List<Map<String, String>> results = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    String url = saveFile(file);
                    Map<String, String> item = new HashMap<>();
                    item.put("url", url);
                    item.put("filename", file.getOriginalFilename());
                    results.add(item);
                } catch (IOException e) {
                    // 跳过失败的文件
                }
            }
        }
        return JsonData.buildSuccess(results);
    }

    /**
     * 保存文件并返回URL
     */
    private String saveFile(MultipartFile file) throws IOException {
        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + extension;

        // 保存文件
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);

        // 返回访问URL
        return "/uploads/" + newFilename;
    }
}

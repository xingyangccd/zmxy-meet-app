package com.xingyang.controller;

import com.xingyang.common.Result;
import com.xingyang.service.MinioService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/file")
public class FileController {
    
    private final MinioService minioService;
    
    public FileController(MinioService minioService) {
        this.minioService = minioService;
    }
    
    /**
     * 上传图片
     */
    @PostMapping("/upload/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = minioService.uploadFile(file, "images");
        
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        
        return Result.success(result);
    }
    
    /**
     * 上传视频
     */
    @PostMapping("/upload/video")
    public Result<Map<String, String>> uploadVideo(@RequestParam("file") MultipartFile file) {
        String url = minioService.uploadFile(file, "videos");
        
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        
        return Result.success(result);
    }
    
    /**
     * 获取预签名上传 URL
     */
    @GetMapping("/presigned-url")
    public Result<Map<String, String>> getPresignedUrl(
            @RequestParam String fileName,
            @RequestParam(defaultValue = "images") String folder) {
        
        String url = minioService.generatePresignedUploadUrl(fileName, folder);
        
        Map<String, String> result = new HashMap<>();
        result.put("uploadUrl", url);
        
        return Result.success(result);
    }
}

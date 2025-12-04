package com.xingyang.config;

import com.xingyang.service.MinioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup implements CommandLineRunner {
    
    private final MinioService minioService;
    
    public ApplicationStartup(MinioService minioService) {
        this.minioService = minioService;
    }
    
    @Override
    public void run(String... args) {
        try {
            minioService.initBucket();
            System.out.println("MinIO 存储桶初始化成功");
        } catch (Exception e) {
            System.err.println("MinIO 存储桶初始化失败: " + e.getMessage());
        }
    }
}

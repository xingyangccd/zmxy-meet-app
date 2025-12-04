package com.xingyang.service;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {
    
    private final MinioClient minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    @Value("${minio.endpoint}")
    private String endpoint;
    
    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }
    
    /**
     * 初始化存储桶
     */
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
            );
            
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build()
                );
                
                // 设置公共读权限
                String policy = """
                    {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Effect": "Allow",
                                "Principal": {"AWS": ["*"]},
                                "Action": ["s3:GetObject"],
                                "Resource": ["arn:aws:s3:::%s/*"]
                            }
                        ]
                    }
                    """.formatted(bucketName);
                
                minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policy)
                        .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("初始化 MinIO 存储桶失败", e);
        }
    }
    
    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = folder + "/" + UUID.randomUUID() + extension;
            
            InputStream inputStream = file.getInputStream();
            
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            inputStream.close();
            
            // 将 localhost 替换为 10.0.2.2，以便 Android 模拟器访问
            String url = endpoint + "/" + bucketName + "/" + fileName;
            return url.replace("localhost", "10.0.2.2");
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }
    
    /**
     * 生成预签名上传 URL
     */
    public String generatePresignedUploadUrl(String fileName, String folder) {
        try {
            String objectName = folder + "/" + UUID.randomUUID() + "_" + fileName;
            
            String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(10, TimeUnit.MINUTES)
                    .build()
            );
            
            return url;
        } catch (Exception e) {
            throw new RuntimeException("生成预签名 URL 失败", e);
        }
    }
    
    /**
     * 删除文件
     */
    public void deleteFile(String fileUrl) {
        try {
            String objectName = fileUrl.replace(endpoint + "/" + bucketName + "/", "");
            
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败", e);
        }
    }
}

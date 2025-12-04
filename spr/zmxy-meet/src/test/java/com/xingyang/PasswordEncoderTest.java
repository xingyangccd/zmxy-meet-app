package com.xingyang;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后: " + encodedPassword);
        System.out.println();
        
        // 验证旧的哈希
        String oldHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHCYXfY2wNzYTOmOZJQW4e";
        boolean matches = encoder.matches(rawPassword, oldHash);
        System.out.println("旧哈希验证结果: " + matches);
        System.out.println("旧哈希: " + oldHash);
        System.out.println();
        
        // 验证新生成的哈希
        boolean newMatches = encoder.matches(rawPassword, encodedPassword);
        System.out.println("新哈希验证结果: " + newMatches);
        System.out.println("新哈希: " + encodedPassword);
    }
}

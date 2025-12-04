package com.xingyang.controller;

import com.xingyang.dto.LoginRequest;
import com.xingyang.dto.RegisterRequest;
import com.xingyang.dto.AuthResponse;
import com.xingyang.entity.User;
import com.xingyang.service.UserService;
import com.xingyang.util.JwtUtil;
import com.xingyang.common.Result;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public AuthController(UserService userService, 
                         PasswordEncoder passwordEncoder, 
                         JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * 临时测试：生成密码哈希
     */
    @GetMapping("/test-password")
    public String testPassword() {
        String rawPassword = "123456";
        String encoded = passwordEncoder.encode(rawPassword);
        String oldHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHCYXfY2wNzYTOmOZJQW4e";
        boolean oldMatches = passwordEncoder.matches(rawPassword, oldHash);
        boolean newMatches = passwordEncoder.matches(rawPassword, encoded);
        
        return "原始密码: " + rawPassword + "\n" +
               "旧哈希: " + oldHash + "\n" +
               "旧哈希验证: " + oldMatches + "\n" +
               "新哈希: " + encoded + "\n" +
               "新哈希验证: " + newMatches;
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<AuthResponse> login(@RequestBody LoginRequest request) {
        System.out.println("===== 登录请求 =====");
        System.out.println("用户名: [" + request.getUsername() + "]");
        System.out.println("密码长度: " + request.getPassword().length());
        System.out.println("密码内容: [" + request.getPassword() + "]");
        System.out.println("密码字节: " + java.util.Arrays.toString(request.getPassword().getBytes()));
        
        User user = userService.findByUsername(request.getUsername());
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        System.out.println("数据库密码哈希: " + user.getPassword());
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        System.out.println("密码匹配结果: " + matches);
        
        if (!matches) {
            return Result.error("密码错误");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUser(user);
        
        return Result.success(response);
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<AuthResponse> register(@RequestBody RegisterRequest request) {
        // 检查用户名是否已存在
        if (userService.findByUsername(request.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userService.findByEmail(request.getEmail()) != null) {
            return Result.error("邮箱已被注册");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setSchoolVerified(false);
        
        userService.save(user);
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUser(user);
        
        return Result.success(response);
    }
}

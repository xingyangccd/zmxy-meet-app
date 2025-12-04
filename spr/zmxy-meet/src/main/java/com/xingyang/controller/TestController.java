package com.xingyang.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @GetMapping("/hello")
    public String hello() {
        System.out.println("===== GET /api/test/hello 被调用 =====");
        return "Hello from backend!";
    }
    
    @PostMapping("/echo")
    public Map<String, Object> echo(@RequestBody Map<String, Object> data) {
        System.out.println("===== POST /api/test/echo 被调用 =====");
        System.out.println("收到数据: " + data);
        return Map.of(
            "success", true,
            "message", "Echo from backend",
            "receivedData", data
        );
    }
}

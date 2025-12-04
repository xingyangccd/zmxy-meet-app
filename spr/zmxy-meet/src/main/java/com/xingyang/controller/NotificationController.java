package com.xingyang.controller;

import com.xingyang.common.Result;
import com.xingyang.entity.Notification;
import com.xingyang.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 获取通知列表
     */
    @GetMapping
    public Result<List<Notification>> getNotifications(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            List<Notification> notifications = notificationService.getUserNotifications(userId);
            return Result.success(notifications);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取通知失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取未读数量
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            long count = notificationService.getUnreadCount(userId);
            
            Map<String, Long> result = new HashMap<>();
            result.put("count", count);
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取未读数量失败: " + e.getMessage());
        }
    }
    
    /**
     * 标记为已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return Result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("标记失败: " + e.getMessage());
        }
    }
    
    /**
     * 全部标记为已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            notificationService.markAllAsRead(userId);
            return Result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("标记失败: " + e.getMessage());
        }
    }
}

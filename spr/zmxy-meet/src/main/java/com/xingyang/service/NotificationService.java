package com.xingyang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xingyang.entity.Notification;

import java.util.List;

public interface NotificationService extends IService<Notification> {
    /**
     * 创建通知
     */
    void createNotification(Long userId, String type, String content, Long relatedId, Long senderId);
    
    /**
     * 获取用户通知列表
     */
    List<Notification> getUserNotifications(Long userId);
    
    /**
     * 标记为已读
     */
    void markAsRead(Long notificationId);
    
    /**
     * 标记全部为已读
     */
    void markAllAsRead(Long userId);
    
    /**
     * 获取未读数量
     */
    long getUnreadCount(Long userId);
}

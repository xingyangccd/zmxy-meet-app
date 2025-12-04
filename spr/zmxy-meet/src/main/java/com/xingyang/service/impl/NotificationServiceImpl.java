package com.xingyang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xingyang.entity.Notification;
import com.xingyang.mapper.NotificationMapper;
import com.xingyang.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    
    @Override
    public void createNotification(Long userId, String type, String content, Long relatedId, Long senderId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setSenderId(senderId);
        notification.setIsRead(false);
        notification.setDeleted(0);
        
        save(notification);
    }
    
    @Override
    public List<Notification> getUserNotifications(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getDeleted, 0)
               .orderByDesc(Notification::getCreateTime);
        
        return list(wrapper);
    }
    
    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = getById(notificationId);
        if (notification != null) {
            notification.setIsRead(true);
            updateById(notification);
        }
    }
    
    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, false);
        
        List<Notification> notifications = list(wrapper);
        notifications.forEach(n -> n.setIsRead(true));
        updateBatchById(notifications);
    }
    
    @Override
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, false)
               .eq(Notification::getDeleted, 0);
        
        return count(wrapper);
    }
}

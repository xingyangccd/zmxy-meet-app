package com.xingyang.controller;

import com.xingyang.common.Result;

import com.xingyang.dto.ConversationResponse;
import com.xingyang.dto.MessageResponse;
import com.xingyang.dto.SendMessageRequest;
import com.xingyang.entity.Message;
import com.xingyang.entity.User;
import com.xingyang.service.MessageService;
import com.xingyang.service.UserService;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    
    private final MessageService messageService;
    private final UserService userService;
    
    public MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }
    
    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    public Result<List<ConversationResponse>> getConversations(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        
        // 获取与当前用户相关的所有消息
        List<Message> messages = messageService.lambdaQuery()
                .and(wrapper -> wrapper
                        .eq(Message::getSenderId, userId)
                        .or()
                        .eq(Message::getReceiverId, userId))
                .orderByDesc(Message::getCreateTime)
                .list();
        
        // 按对话人分组，取每个对话的最后一条消息
        Map<Long, Message> conversationMap = new LinkedHashMap<>();
        for (Message message : messages) {
            Long otherId = message.getSenderId().equals(userId) 
                    ? message.getReceiverId() 
                    : message.getSenderId();
            
            if (!conversationMap.containsKey(otherId)) {
                conversationMap.put(otherId, message);
            }
        }
        
        // 构建会话列表
        List<ConversationResponse> conversations = new ArrayList<>();
        for (Map.Entry<Long, Message> entry : conversationMap.entrySet()) {
            Long otherId = entry.getKey();
            Message lastMessage = entry.getValue();
            
            User otherUser = userService.getById(otherId);
            if (otherUser != null) {
                ConversationResponse conv = new ConversationResponse();
                conv.setUserId(otherId);
                conv.setUsername(otherUser.getUsername());
                conv.setNickname(otherUser.getNickname());
                conv.setAvatarUrl(otherUser.getAvatarUrl());
                conv.setLastMessage(lastMessage.getContent());
                conv.setLastMessageTime(lastMessage.getCreateTime().toString());
                conv.setUnreadCount(messageService.lambdaQuery()
                        .eq(Message::getSenderId, otherId)
                        .eq(Message::getReceiverId, userId)
                        .eq(Message::getIsRead, false)
                        .count().intValue());
                
                conversations.add(conv);
            }
        }
        
        return Result.success(conversations);
    }
    
    /**
     * 获取与某人的聊天历史
     */
    @GetMapping("/history/{otherUserId}")
    public Result<List<MessageResponse>> getChatHistory(
            @PathVariable Long otherUserId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        
        List<Message> messages = messageService.getChatHistory(userId, otherUserId, page, size);
        
        // 获取发送者和接收者信息
        User currentUser = userService.getById(userId);
        User otherUser = userService.getById(otherUserId);
        
        List<MessageResponse> responses = messages.stream().map(msg -> {
            MessageResponse response = new MessageResponse();
            response.setId(msg.getId());
            response.setSenderId(msg.getSenderId());
            response.setReceiverId(msg.getReceiverId());
            response.setContent(msg.getContent());
            response.setType(msg.getType());
            response.setMediaUrls(msg.getMediaUrls());
            response.setIsRead(msg.getIsRead());
            response.setCreateTime(msg.getCreateTime().toString());
            
            // 设置发送者信息
            if (msg.getSenderId().equals(userId)) {
                response.setSenderName(currentUser.getUsername());
                response.setSenderAvatar(currentUser.getAvatarUrl());
            } else {
                response.setSenderName(otherUser.getUsername());
                response.setSenderAvatar(otherUser.getAvatarUrl());
            }
            
            return response;
        }).collect(Collectors.toList());
        
        // 标记消息为已读
        messageService.lambdaUpdate()
                .eq(Message::getSenderId, otherUserId)
                .eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, false)
                .set(Message::getIsRead, true)
                .update();
        
        return Result.success(responses);
    }
    
    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Result<MessageResponse> sendMessage(
            @RequestBody SendMessageRequest request,
            Authentication authentication) {
        
        Long senderId = (Long) authentication.getPrincipal();
        
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(request.getReceiverId());
        message.setContent(request.getContent());
        message.setType(request.getType() != null ? request.getType() : "text");
        message.setMediaUrls(request.getMediaUrls());
        message.setIsRead(false);
        
        messageService.save(message);
        
        // 返回消息详情
        User sender = userService.getById(senderId);
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setSenderId(senderId);
        response.setReceiverId(request.getReceiverId());
        response.setContent(request.getContent());
        response.setType(message.getType());
        response.setMediaUrls(request.getMediaUrls());
        response.setIsRead(false);
        response.setCreateTime(message.getCreateTime().toString());
        response.setSenderName(sender.getUsername());
        response.setSenderAvatar(sender.getAvatarUrl());
        
        return Result.success(response);
    }
    
    /**
     * 获取未读消息数
     */
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        
        int count = messageService.lambdaQuery()
                .eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, false)
                .count().intValue();
        
        return Result.success(count);
    }
}



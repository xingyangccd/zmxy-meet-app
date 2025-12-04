package com.xingyang.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingyang.entity.Message;
import com.xingyang.service.MessageService;
import com.xingyang.util.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    
    private final JwtUtil jwtUtil;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;
    
    // 存储用户 ID 和 WebSocket 会话的映射
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    public ChatWebSocketHandler(JwtUtil jwtUtil, MessageService messageService, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.messageService = messageService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket 连接建立: " + session.getId());
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        Map<String, Object> data = objectMapper.readValue(payload, Map.class);
        
        String type = (String) data.get("type");
        
        if ("auth".equals(type)) {
            // 认证消息
            String token = (String) data.get("token");
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                sessions.put(userId, session);
                session.getAttributes().put("userId", userId);
                
                // 发送认证成功消息
                Map<String, Object> response = Map.of(
                    "type", "auth_success",
                    "message", "认证成功"
                );
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                
                // 发送离线消息
                sendOfflineMessages(userId, session);
            }
        } else if ("message".equals(type)) {
            // 聊天消息
            Long senderId = (Long) session.getAttributes().get("userId");
            Long receiverId = Long.valueOf(data.get("receiverId").toString());
            String content = (String) data.get("content");
            
            // 保存消息到数据库
            Message msg = new Message();
            msg.setSenderId(senderId);
            msg.setReceiverId(receiverId);
            msg.setContent(content);
            msg.setType("text");
            msg.setIsRead(false);
            messageService.save(msg);
            
            // 如果接收者在线，立即发送
            WebSocketSession receiverSession = sessions.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                Map<String, Object> messageData = Map.of(
                    "type", "message",
                    "id", msg.getId(),
                    "senderId", senderId,
                    "content", content,
                    "timestamp", System.currentTimeMillis()
                );
                receiverSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageData)));
            }
            
            // 发送确认给发送者
            Map<String, Object> ack = Map.of(
                "type", "message_sent",
                "messageId", msg.getId()
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ack)));
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.remove(userId);
        }
        System.out.println("WebSocket 连接关闭: " + session.getId());
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket 错误: " + exception.getMessage());
    }
    
    /**
     * 发送离线消息
     */
    private void sendOfflineMessages(Long userId, WebSocketSession session) throws Exception {
        // TODO: 从数据库获取未读消息并发送
    }
}

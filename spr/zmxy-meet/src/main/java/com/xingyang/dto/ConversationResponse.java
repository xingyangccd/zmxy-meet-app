package com.xingyang.dto;

import lombok.Data;

@Data
public class ConversationResponse {
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String lastMessage;
    private String lastMessageTime;
    private Integer unreadCount;
}
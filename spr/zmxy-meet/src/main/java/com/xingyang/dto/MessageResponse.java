package com.xingyang.dto;

import lombok.Data;

@Data
public class MessageResponse {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private String type;
    private String mediaUrls;
    private Boolean isRead;
    private String createTime;
    private String senderName;
    private String senderAvatar;
}
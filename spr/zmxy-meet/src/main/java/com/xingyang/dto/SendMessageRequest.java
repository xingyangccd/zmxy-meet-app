package com.xingyang.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long receiverId;
    private String content;
    private String type;
    private String mediaUrls;
}

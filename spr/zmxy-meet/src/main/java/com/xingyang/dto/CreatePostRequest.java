package com.xingyang.dto;

import lombok.Data;

@Data
public class CreatePostRequest {
    private String content;
    private String mediaUrls;  // JSON 数组字符串
    private String type;  // normal, question
    private String visibility;  // public, circle_id
    private Long circleId;
}

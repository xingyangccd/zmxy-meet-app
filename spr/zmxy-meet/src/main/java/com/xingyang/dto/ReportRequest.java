package com.xingyang.dto;

import lombok.Data;

@Data
public class ReportRequest {
    private String reportedType;  // post, comment, user
    private Long reportedId;      // 被举报对象ID
    private String reason;        // 举报原因
    private String description;   // 详细描述（可选）
}

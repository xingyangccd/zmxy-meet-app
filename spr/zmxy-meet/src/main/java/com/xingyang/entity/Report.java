package com.xingyang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_report")
public class Report {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long reporterId;  // 举报人ID
    
    private String reportedType;  // post, comment, user
    
    private Long reportedId;  // 被举报对象ID
    
    private String reason;  // 举报原因
    
    private String description;  // 详细描述
    
    private String status;  // pending, reviewing, resolved, rejected
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    private LocalDateTime handleTime;
    
    private Long handlerId;  // 处理人ID
    
    private String handleResult;  // 处理结果
    
    @TableLogic
    private Integer deleted;
}

package com.xingyang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;  // 接收通知的用户ID
    
    private String type;  // like, comment, follow, system
    
    private String content;  // 通知内容
    
    private Long relatedId;  // 关联ID（帖子ID、评论ID等）
    
    private Long senderId;  // 发送者ID
    
    private Boolean isRead;  // 是否已读
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
}

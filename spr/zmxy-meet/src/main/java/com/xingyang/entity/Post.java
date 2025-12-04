package com.xingyang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String username;  // 用户名（冗余字段）
    
    private String content;
    
    private String mediaUrls;  // JSON 数组字符串
    
    private String type;  // normal, question
    
    private String visibility;  // public, circle_id
    
    private Long circleId;
    
    private Integer likesCount;
    
    private Integer commentsCount;
    
    private Integer sharesCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}

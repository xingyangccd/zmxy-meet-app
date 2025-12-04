package com.xingyang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_relation")
public class Relation {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userIdA;  // 关注者ID
    
    private Long userIdB;  // 被关注者ID
    
    private String relationType;  // follow, friend
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
}

package com.xingyang.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatarUrl;
    private Boolean schoolVerified;
    private String campus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public static UserDTO fromEntity(com.xingyang.entity.User user) {
        if (user == null) {
            return null;
        }
        
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setSchoolVerified(user.getSchoolVerified());
        dto.setCampus(user.getCampus());
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());
        
        return dto;
    }
}

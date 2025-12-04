package com.xingyang.dto;

import com.xingyang.entity.User;
import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private User user;
}

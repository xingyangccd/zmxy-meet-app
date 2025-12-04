package com.xingyang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xingyang.entity.User;

public interface UserService extends IService<User> {
    User findByUsername(String username);
    User findByEmail(String email);
}

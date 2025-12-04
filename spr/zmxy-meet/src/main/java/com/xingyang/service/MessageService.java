package com.xingyang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xingyang.entity.Message;

import java.util.List;

public interface MessageService extends IService<Message> {
    List<Message> getUnreadMessages(Long userId);
    List<Message> getChatHistory(Long userId1, Long userId2, int page, int size);
}

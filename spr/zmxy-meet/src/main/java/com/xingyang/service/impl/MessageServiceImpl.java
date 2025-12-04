package com.xingyang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xingyang.entity.Message;
import com.xingyang.mapper.MessageMapper;
import com.xingyang.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    
    @Override
    public List<Message> getUnreadMessages(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId)
               .eq(Message::getIsRead, false)
               .orderByDesc(Message::getCreateTime);
        return list(wrapper);
    }
    
    @Override
    public List<Message> getChatHistory(Long userId1, Long userId2, int page, int size) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
            .and(w1 -> w1.eq(Message::getSenderId, userId1).eq(Message::getReceiverId, userId2))
            .or(w2 -> w2.eq(Message::getSenderId, userId2).eq(Message::getReceiverId, userId1))
        ).orderByDesc(Message::getCreateTime);
        
        Page<Message> pageResult = page(new Page<>(page, size), wrapper);
        return pageResult.getRecords();
    }
}

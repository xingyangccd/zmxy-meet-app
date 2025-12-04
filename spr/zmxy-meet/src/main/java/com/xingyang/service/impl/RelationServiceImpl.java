package com.xingyang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xingyang.dto.UserDTO;
import com.xingyang.entity.Relation;
import com.xingyang.entity.User;
import com.xingyang.mapper.RelationMapper;
import com.xingyang.service.NotificationService;
import com.xingyang.service.RelationService;
import com.xingyang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RelationServiceImpl extends ServiceImpl<RelationMapper, Relation> implements RelationService {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    @Override
    public void followUser(Long followerId, Long followedId) {
        // 检查是否已经关注
        if (isFollowing(followerId, followedId)) {
            return;
        }
        
        Relation relation = new Relation();
        relation.setUserIdA(followerId);
        relation.setUserIdB(followedId);
        relation.setRelationType("follow");
        relation.setDeleted(0);
        
        save(relation);
        
        // 创建通知
        notificationService.createNotification(
            followedId,
            "follow",
            "有人关注了你",
            null,
            followerId
        );
    }
    
    @Override
    public void unfollowUser(Long followerId, Long followedId) {
        LambdaQueryWrapper<Relation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Relation::getUserIdA, followerId)
               .eq(Relation::getUserIdB, followedId)
               .eq(Relation::getDeleted, 0);
        
        remove(wrapper);
    }
    
    @Override
    public boolean isFollowing(Long followerId, Long followedId) {
        LambdaQueryWrapper<Relation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Relation::getUserIdA, followerId)
               .eq(Relation::getUserIdB, followedId)
               .eq(Relation::getDeleted, 0);
        
        return count(wrapper) > 0;
    }
    
    @Override
    public long getFollowersCount(Long userId) {
        // 获取关注该用户的人数（粉丝数）
        LambdaQueryWrapper<Relation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Relation::getUserIdB, userId)
               .eq(Relation::getDeleted, 0);
        
        return count(wrapper);
    }
    
    @Override
    public long getFollowingCount(Long userId) {
        // 获取该用户关注的人数（关注数）
        LambdaQueryWrapper<Relation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Relation::getUserIdA, userId)
               .eq(Relation::getDeleted, 0);
        
        return count(wrapper);
    }
    
    @Override
    public List<UserDTO> getFollowingList(Long userId) {
        // 获取该用户关注的所有用户
        LambdaQueryWrapper<Relation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Relation::getUserIdA, userId)
               .eq(Relation::getDeleted, 0);
        
        List<Relation> relations = list(wrapper);
        
        List<UserDTO> userList = new ArrayList<>();
        for (Relation relation : relations) {
            User user = userService.getById(relation.getUserIdB());
            if (user != null && user.getDeleted() == 0) {
                userList.add(UserDTO.fromEntity(user));
            }
        }
        
        return userList;
    }
    
    @Override
    public List<UserDTO> getFollowersList(Long userId) {
        // 获取关注该用户的所有用户（粉丝列表）
        LambdaQueryWrapper<Relation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Relation::getUserIdB, userId)
               .eq(Relation::getDeleted, 0);
        
        List<Relation> relations = list(wrapper);
        
        List<UserDTO> userList = new ArrayList<>();
        for (Relation relation : relations) {
            User user = userService.getById(relation.getUserIdA());
            if (user != null && user.getDeleted() == 0) {
                userList.add(UserDTO.fromEntity(user));
            }
        }
        
        return userList;
    }
}

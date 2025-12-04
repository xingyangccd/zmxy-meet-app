package com.xingyang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xingyang.dto.UserDTO;
import com.xingyang.entity.Relation;

import java.util.List;

public interface RelationService extends IService<Relation> {
    /**
     * 关注用户
     */
    void followUser(Long followerId, Long followedId);
    
    /**
     * 取消关注
     */
    void unfollowUser(Long followerId, Long followedId);
    
    /**
     * 检查是否已关注
     */
    boolean isFollowing(Long followerId, Long followedId);
    
    /**
     * 获取粉丝数
     */
    long getFollowersCount(Long userId);
    
    /**
     * 获取关注数
     */
    long getFollowingCount(Long userId);
    
    /**
     * 获取关注列表
     */
    List<UserDTO> getFollowingList(Long userId);
    
    /**
     * 获取粉丝列表
     */
    List<UserDTO> getFollowersList(Long userId);
}

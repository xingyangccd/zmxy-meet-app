package com.xingyang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xingyang.entity.Post;

import java.util.List;

public interface PostService extends IService<Post> {
    List<Post> getFeedPosts(Long userId, int page, int size);
    void likePost(Long postId, Long userId);
    void unlikePost(Long postId, Long userId);
    boolean isLiked(Long postId, Long userId);
    List<Post> getUserLikedPosts(Long userId);
}

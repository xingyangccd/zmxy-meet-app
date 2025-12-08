package com.xingyang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xingyang.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {

    /**
     * 获取帖子的所有评论（包含回复）
     */
    List<Comment> getPostComments(Long postId);

    /**
     * 添加评论
     */
    Comment addComment(Long postId, Long userId, String content);

    /**
     * 回复评论
     */
    Comment replyComment(Long commentId, Long userId, String content);

    /**
     * 删除评论（逻辑删除）
     */
    boolean deleteComment(Long commentId, Long userId);
}

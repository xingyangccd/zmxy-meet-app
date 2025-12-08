package com.xingyang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xingyang.entity.Comment;
import com.xingyang.entity.Post;
import com.xingyang.mapper.CommentMapper;
import com.xingyang.service.CommentService;
import com.xingyang.service.NotificationService;
import com.xingyang.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PostService postService;

    @Override
    public List<Comment> getPostComments(Long postId) {
        return baseMapper.selectByPostId(postId);
    }

    @Override
    public Comment addComment(Long postId, Long userId, String content) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreateTime(LocalDateTime.now());
        comment.setDeleted(0);

        save(comment);

        // 创建通知（不通知自己）
        Post post = postService.getById(postId);
        if (post != null && !post.getUserId().equals(userId)) {
            notificationService.createNotification(
                    post.getUserId(),
                    "comment",
                    "有人评论了你的动态",
                    postId,
                    userId);
        }

        return comment;
    }

    @Override
    public Comment replyComment(Long commentId, Long userId, String content) {
        Comment comment = new Comment();
        comment.setParentCommentId(commentId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreateTime(LocalDateTime.now());
        comment.setDeleted(0);

        // 获取父评论来设置postId
        Comment parentComment = getById(commentId);
        if (parentComment != null) {
            comment.setPostId(parentComment.getPostId());
        }

        save(comment);
        return comment;
    }

    @Override
    public boolean deleteComment(Long commentId, Long userId) {
        Comment comment = getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 只允许评论作者删除自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }

        // 使用UpdateWrapper进行逻辑删除,确保deleted字段被更新
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Comment> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.eq("id", commentId)
                .eq("deleted", 0)
                .set("deleted", 1);

        return update(updateWrapper);
    }
}

package com.xingyang.controller;

import com.xingyang.common.Result;
import com.xingyang.entity.Comment;
import com.xingyang.entity.User;
import com.xingyang.service.CommentService;
import com.xingyang.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    /**
     * 获取帖子的所有评论
     */
    @GetMapping("/posts/{postId}/comments")
    public Result<List<Map<String, Object>>> getPostComments(@PathVariable Long postId) {
        try {
            List<Comment> comments = commentService.getPostComments(postId);

            // 构建评论树结构
            Map<Long, Map<String, Object>> commentMap = new HashMap<>();
            List<Map<String, Object>> topComments = comments.stream()
                    .filter(c -> c.getParentCommentId() == null)
                    .map(c -> {
                        Map<String, Object> map = buildCommentMap(c);
                        commentMap.put(c.getId(), map);
                        return map;
                    })
                    .collect(Collectors.toList());

            // 添加回复
            comments.stream()
                    .filter(c -> c.getParentCommentId() != null)
                    .forEach(reply -> {
                        Map<String, Object> parentMap = commentMap.get(reply.getParentCommentId());
                        if (parentMap != null) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> replies = (List<Map<String, Object>>) parentMap.get("replies");
                            replies.add(buildCommentMap(reply));
                            parentMap.put("replyCount", replies.size());
                        }
                    });

            return Result.success(topComments);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取评论失败: " + e.getMessage());
        }
    }

    /**
     * 发布评论
     */
    @PostMapping("/posts/{postId}/comments")
    public Result<Map<String, Object>> addComment(
            @PathVariable Long postId,
            @RequestBody Map<String, String> request) {
        try {
            // 获取当前用户ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = Long.parseLong(auth.getName());
            User user = userService.getById(userId);

            if (user == null) {
                return Result.error("用户未登录");
            }

            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                return Result.error("评论内容不能为空");
            }

            Comment comment = commentService.addComment(postId, user.getId(), content);
            Map<String, Object> result = buildCommentMap(comment);
            result.put("username", user.getUsername());

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("发布评论失败: " + e.getMessage());
        }
    }

    /**
     * 回复评论
     */
    @PostMapping("/comments/{commentId}/reply")
    public Result<Map<String, Object>> replyComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request) {
        try {
            // 获取当前用户ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = Long.parseLong(auth.getName());
            User user = userService.getById(userId);

            if (user == null) {
                return Result.error("用户未登录");
            }

            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                return Result.error("回复内容不能为空");
            }

            Comment comment = commentService.replyComment(commentId, user.getId(), content);
            Map<String, Object> result = buildCommentMap(comment);
            result.put("username", user.getUsername());

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("回复评论失败: " + e.getMessage());
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        try {
            // 获取当前用户ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = Long.parseLong(auth.getName());
            User user = userService.getById(userId);

            if (user == null) {
                return Result.error("用户未登录");
            }

            boolean success = commentService.deleteComment(commentId, user.getId());
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("删除评论失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除评论失败: " + e.getMessage());
        }
    }

    /**
     * 构建评论Map
     */
    private Map<String, Object> buildCommentMap(Comment comment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", comment.getId());
        map.put("postId", comment.getPostId());
        map.put("userId", comment.getUserId());
        map.put("content", comment.getContent());
        map.put("parentCommentId", comment.getParentCommentId());
        map.put("createTime", comment.getCreateTime());
        map.put("replies", new java.util.ArrayList<>());
        map.put("replyCount", 0);

        // 获取用户名
        try {
            User user = userService.getById(comment.getUserId());
            if (user != null) {
                map.put("username", user.getUsername());
            }
        } catch (Exception e) {
            map.put("username", "未知用户");
        }

        return map;
    }
}

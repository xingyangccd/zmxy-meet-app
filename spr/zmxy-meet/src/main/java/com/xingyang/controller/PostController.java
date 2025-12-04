package com.xingyang.controller;

import com.xingyang.common.Result;
import com.xingyang.dto.CreatePostRequest;
import com.xingyang.entity.Post;
import com.xingyang.entity.User;
import com.xingyang.service.PostService;
import com.xingyang.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    
    private final PostService postService;
    private final UserService userService;
    
    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }
    
    /**
     * 获取动态流（首页）
     */
    @GetMapping(value = {"", "/feed"})
    public Result<List<Post>> getFeedPosts(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            Authentication authentication) {
        
        System.out.println("===== 获取动态列表 =====");
        System.out.println("Authentication: " + authentication);
        
        // 如果未登录，返回空列表
        if (authentication == null || authentication.getPrincipal() == null) {
            System.out.println("未认证，返回空列表");
            return Result.success(List.of());
        }
        
        try {
            Long userId = (Long) authentication.getPrincipal();
            System.out.println("用户ID: " + userId + ", page: " + page + ", size: " + size);
            
            List<Post> posts = postService.getFeedPosts(userId, page, size);
            System.out.println("查询到 " + posts.size() + " 条动态");
            
            return Result.success(posts);
        } catch (Exception e) {
            System.out.println("获取动态列表失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取动态列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建动态
     */
    @PostMapping
    public Result<Post> createPost(@RequestBody CreatePostRequest request, 
                                   Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        
        // 获取用户信息
        User user = userService.getById(userId);
        
        Post post = new Post();
        post.setUserId(userId);
        post.setUsername(user != null ? user.getUsername() : null);
        post.setContent(request.getContent());
        post.setMediaUrls(request.getMediaUrls());
        post.setType(request.getType());
        post.setVisibility(request.getVisibility());
        post.setCircleId(request.getCircleId());
        post.setLikesCount(0);
        post.setCommentsCount(0);
        post.setSharesCount(0);
        
        postService.save(post);
        
        return Result.success(post);
    }
    
    /**
     * 获取单个动态详情
     */
    @GetMapping("/{id}")
    public Result<Post> getPost(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.error("动态不存在");
        }
        return Result.success(post);
    }
    
    /**
     * 编辑动态
     */
    @PutMapping("/{id}")
    public Result<Post> updatePost(@PathVariable Long id, 
                                   @RequestBody CreatePostRequest request,
                                   Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Post post = postService.getById(id);
        
        if (post == null) {
            return Result.error("动态不存在");
        }
        
        if (!post.getUserId().equals(userId)) {
            return Result.error("无权编辑此动态");
        }
        
        // 更新内容
        post.setContent(request.getContent());
        post.setMediaUrls(request.getMediaUrls());
        post.setType(request.getType());
        post.setVisibility(request.getVisibility());
        
        postService.updateById(post);
        return Result.success(post);
    }
    
    /**
     * 删除动态
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Post post = postService.getById(id);
        
        if (post == null) {
            return Result.error("动态不存在");
        }
        
        if (!post.getUserId().equals(userId)) {
            return Result.error("无权删除此动态");
        }
        
        postService.removeById(id);
        return Result.success(null);
    }
    
    /**
     * 点赞动态
     */
    @PostMapping("/{id}/like")
    public Result<Void> likePost(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        postService.likePost(id, userId);
        return Result.success(null);
    }
    
    /**
     * 取消点赞
     */
    @DeleteMapping("/{id}/like")
    public Result<Void> unlikePost(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        postService.unlikePost(id, userId);
        return Result.success(null);
    }
    
    /**
     * 检查是否已点赞
     */
    @GetMapping("/{id}/like/status")
    public Result<Boolean> checkLikeStatus(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isLiked = postService.isLiked(id, userId);
        return Result.success(isLiked);
    }
}

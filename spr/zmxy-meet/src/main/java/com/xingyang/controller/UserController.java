package com.xingyang.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xingyang.common.Result;
import com.xingyang.dto.UserDTO;
import com.xingyang.entity.Post;
import com.xingyang.entity.User;
import com.xingyang.service.PostService;
import com.xingyang.service.RelationService;
import com.xingyang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    private final PostService postService;
    private final RelationService relationService;
    
    public UserController(UserService userService, PostService postService, RelationService relationService) {
        this.userService = userService;
        this.postService = postService;
        this.relationService = relationService;
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<UserDTO> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            UserDTO userDTO = UserDTO.fromEntity(user);
            return Result.success(userDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户统计信息
     */
    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> getUserStats(@PathVariable Long id) {
        try {
            // 获取用户发布的帖子数
            QueryWrapper<Post> postQuery = new QueryWrapper<>();
            postQuery.eq("user_id", id).eq("deleted", 0);
            long postsCount = postService.count(postQuery);
            
            // 获取粉丝数和关注数
            long followersCount = relationService.getFollowersCount(id);
            long followingCount = relationService.getFollowingCount(id);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("postsCount", postsCount);
            stats.put("followersCount", followersCount);
            stats.put("followingCount", followingCount);
            
            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取用户统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户发布的帖子
     */
    @GetMapping("/{id}/posts")
    public Result<List<Post>> getUserPosts(@PathVariable Long id) {
        try {
            QueryWrapper<Post> query = new QueryWrapper<>();
            query.eq("user_id", id)
                 .eq("deleted", 0)
                 .orderByDesc("create_time");
            
            List<Post> posts = postService.list(query);
            return Result.success(posts);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取用户帖子失败: " + e.getMessage());
        }
    }
    
    /**
     * 关注用户
     */
    @PostMapping("/{id}/follow")
    public Result<Void> followUser(@PathVariable Long id) {
        try {
            // 获取当前用户ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long currentUserId = Long.parseLong(auth.getName());
            User currentUser = userService.getById(currentUserId);
            
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            if (currentUser.getId().equals(id)) {
                return Result.error("不能关注自己");
            }
            
            // 关注用户
            relationService.followUser(currentUser.getId(), id);
            
            return Result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("关注失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消关注
     */
    @DeleteMapping("/{id}/follow")
    public Result<Void> unfollowUser(@PathVariable Long id) {
        try {
            // 获取当前用户ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long currentUserId = Long.parseLong(auth.getName());
            User currentUser = userService.getById(currentUserId);
            
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            // 取消关注
            relationService.unfollowUser(currentUser.getId(), id);
            
            return Result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("取消关注失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查是否已关注
     */
    @GetMapping("/{id}/follow/status")
    public Result<Boolean> checkFollowStatus(@PathVariable Long id) {
        try {
            // 获取当前用户ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long currentUserId = Long.parseLong(auth.getName());
            User currentUser = userService.getById(currentUserId);
            
            if (currentUser == null) {
                return Result.success(false);
            }
            
            // 检查关注状态
            boolean isFollowing = relationService.isFollowing(currentUser.getId(), id);
            
            return Result.success(isFollowing);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("检查关注状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取关注列表
     */
    @GetMapping("/{id}/following")
    public Result<List<UserDTO>> getFollowingList(@PathVariable Long id) {
        try {
            List<UserDTO> followingList = relationService.getFollowingList(id);
            return Result.success(followingList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取关注列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取粉丝列表
     */
    @GetMapping("/{id}/followers")
    public Result<List<UserDTO>> getFollowersList(@PathVariable Long id) {
        try {
            List<UserDTO> followersList = relationService.getFollowersList(id);
            return Result.success(followersList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取粉丝列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户点赞的帖子列表
     */
    @GetMapping("/{id}/liked-posts")
    public Result<List<Post>> getUserLikedPosts(@PathVariable Long id) {
        try {
            List<Post> likedPosts = postService.getUserLikedPosts(id);
            return Result.success(likedPosts);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取点赞列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户浏览历史
     * TODO: 需要实现浏览记录功能
     */
    @GetMapping("/{id}/browse-history")
    public Result<List<Post>> getUserBrowseHistory(@PathVariable Long id) {
        try {
            // 暂时返回空列表，待实现浏览记录功能
            // 可以通过在PostController的getPost方法中记录浏览历史
            return Result.success(List.of());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取浏览历史失败: " + e.getMessage());
        }
    }
}

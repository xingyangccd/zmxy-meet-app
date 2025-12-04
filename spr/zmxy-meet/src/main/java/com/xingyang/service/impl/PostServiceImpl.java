package com.xingyang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xingyang.entity.Comment;
import com.xingyang.entity.Post;
import com.xingyang.mapper.CommentMapper;
import com.xingyang.mapper.PostMapper;
import com.xingyang.service.NotificationService;
import com.xingyang.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    public PostServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 动态更新帖子的真实评论数
     */
    private void updateRealCommentsCount(Post post) {
        if (post != null) {
            LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Comment::getPostId, post.getId())
                   .eq(Comment::getDeleted, 0);
            long count = commentMapper.selectCount(wrapper);
            post.setCommentsCount((int) count);
        }
    }
    
    /**
     * 批量更新帖子的真实评论数
     */
    private void updateRealCommentsCount(List<Post> posts) {
        if (posts != null) {
            posts.forEach(this::updateRealCommentsCount);
        }
    }
    
    @Override
    public List<Post> getFeedPosts(Long userId, int page, int size) {
        // TODO: 实现更复杂的 Feed 流算法（关注用户、圈子动态、推荐等）
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getVisibility, "public")
               .orderByDesc(Post::getCreateTime);
        
        Page<Post> pageResult = page(new Page<>(page, size), wrapper);
        List<Post> posts = pageResult.getRecords();
        
        // 动态统计每个帖子的真实评论数
        updateRealCommentsCount(posts);
        
        return posts;
    }
    
    @Override
    public Post getById(java.io.Serializable id) {
        Post post = super.getById(id);
        
        // 动态统计真实评论数
        updateRealCommentsCount(post);
        
        return post;
    }
    
    @Override
    public void likePost(Long postId, Long userId) {
        String key = "post:like:" + postId + ":" + userId;
        
        // 检查是否已点赞
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return;
        }
        
        // 记录点赞
        redisTemplate.opsForValue().set(key, "1");
        
        // 增加点赞数
        Post post = getById(postId);
        if (post != null) {
            post.setLikesCount(post.getLikesCount() + 1);
            updateById(post);
            
            // 创建通知（不通知自己）
            if (!post.getUserId().equals(userId)) {
                notificationService.createNotification(
                    post.getUserId(),
                    "like",
                    "有人点赞了你的动态",
                    postId,
                    userId
                );
            }
        }
    }
    
    @Override
    public void unlikePost(Long postId, Long userId) {
        String key = "post:like:" + postId + ":" + userId;
        
        // 检查是否已点赞
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            return;
        }
        
        // 删除点赞记录
        redisTemplate.delete(key);
        
        // 减少点赞数
        Post post = getById(postId);
        if (post != null && post.getLikesCount() > 0) {
            post.setLikesCount(post.getLikesCount() - 1);
            updateById(post);
        }
    }
    
    @Override
    public boolean isLiked(Long postId, Long userId) {
        String key = "post:like:" + postId + ":" + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}

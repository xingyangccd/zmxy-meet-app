package com.xingyang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingyang.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    @Select("SELECT c.*, u.username FROM tb_comment c " +
            "LEFT JOIN tb_user u ON c.user_id = u.id " +
            "WHERE c.post_id = #{postId} AND c.deleted = 0 " +
            "ORDER BY c.create_time DESC")
    List<Comment> selectByPostId(Long postId);
    
    @Select("SELECT c.*, u.username FROM tb_comment c " +
            "LEFT JOIN tb_user u ON c.user_id = u.id " +
            "WHERE c.parent_comment_id = #{parentCommentId} AND c.deleted = 0 " +
            "ORDER BY c.create_time ASC")
    List<Comment> selectRepliesByCommentId(Long parentCommentId);
}

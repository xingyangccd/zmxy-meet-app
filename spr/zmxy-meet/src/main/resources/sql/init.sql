-- 创建数据库
CREATE DATABASE IF NOT EXISTS zmxy_meet DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE zmxy_meet;

/*
 Navicat Premium Dump SQL

 Source Server         : local-mysql
 Source Server Type    : MySQL
 Source Server Version : 80300 (8.3.0)
 Source Host           : localhost:3306
 Source Schema         : zmxy_meet

 Target Server Type    : MySQL
 Target Server Version : 80300 (8.3.0)
 File Encoding         : 65001

 Date: 08/12/2025 10:13:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_circle
-- ----------------------------
DROP TABLE IF EXISTS `tb_circle`;
CREATE TABLE `tb_circle` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '圈子ID',
                             `name` varchar(100) NOT NULL COMMENT '圈子名称',
                             `description` text COMMENT '圈子描述',
                             `creator_id` bigint NOT NULL COMMENT '创建者ID',
                             `type` varchar(20) DEFAULT 'interest' COMMENT '圈子类型：interest-兴趣, course-课程, official-官方',
                             `avatar_url` varchar(500) DEFAULT NULL COMMENT '圈子头像',
                             `members_count` int DEFAULT '0' COMMENT '成员数量',
                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `deleted` int DEFAULT '0' COMMENT '逻辑删除标记',
                             PRIMARY KEY (`id`),
                             KEY `idx_creator` (`creator_id`),
                             KEY `idx_type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='圈子/群组表';

-- ----------------------------
-- Records of tb_circle
-- ----------------------------
BEGIN;
INSERT INTO `tb_circle` (`id`, `name`, `description`, `creator_id`, `type`, `avatar_url`, `members_count`, `create_time`, `update_time`, `deleted`) VALUES (1, '编程爱好者', '一起学习编程，分享技术心得', 1, 'interest', NULL, 50, '2025-12-03 13:01:05', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_circle` (`id`, `name`, `description`, `creator_id`, `type`, `avatar_url`, `members_count`, `create_time`, `update_time`, `deleted`) VALUES (2, '羽毛球社', '校园羽毛球爱好者交流群', 2, 'interest', NULL, 30, '2025-12-03 13:01:05', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_circle` (`id`, `name`, `description`, `creator_id`, `type`, `avatar_url`, `members_count`, `create_time`, `update_time`, `deleted`) VALUES (3, '数据结构课程', '2024春季数据结构课程讨论组', 1, 'course', NULL, 120, '2025-12-03 13:01:05', '2025-12-03 13:01:05', 0);
COMMIT;

-- ----------------------------
-- Table structure for tb_circle_member
-- ----------------------------
DROP TABLE IF EXISTS `tb_circle_member`;
CREATE TABLE `tb_circle_member` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成员关系ID',
                                    `circle_id` bigint NOT NULL COMMENT '圈子ID',
                                    `user_id` bigint NOT NULL COMMENT '用户ID',
                                    `role` varchar(20) DEFAULT 'member' COMMENT '角色：admin-管理员, member-成员',
                                    `join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
                                    `deleted` int DEFAULT '0' COMMENT '逻辑删除标记',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_circle_member` (`circle_id`,`user_id`),
                                    KEY `idx_circle` (`circle_id`),
                                    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='圈子成员表';

-- ----------------------------
-- Records of tb_circle_member
-- ----------------------------
BEGIN;
INSERT INTO `tb_circle_member` (`id`, `circle_id`, `user_id`, `role`, `join_time`, `deleted`) VALUES (1, 1, 1, 'admin', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_circle_member` (`id`, `circle_id`, `user_id`, `role`, `join_time`, `deleted`) VALUES (2, 1, 2, 'member', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_circle_member` (`id`, `circle_id`, `user_id`, `role`, `join_time`, `deleted`) VALUES (3, 1, 3, 'member', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_circle_member` (`id`, `circle_id`, `user_id`, `role`, `join_time`, `deleted`) VALUES (4, 2, 2, 'admin', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_circle_member` (`id`, `circle_id`, `user_id`, `role`, `join_time`, `deleted`) VALUES (5, 2, 1, 'member', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_circle_member` (`id`, `circle_id`, `user_id`, `role`, `join_time`, `deleted`) VALUES (6, 3, 1, 'admin', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_circle_member` (`id`, `circle_id`, `user_id`, `role`, `join_time`, `deleted`) VALUES (7, 3, 2, 'member', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_circle_member` (`id`, `circle_id`, `user_id`, `role`, `join_time`, `deleted`) VALUES (8, 3, 3, 'member', '2025-12-03 13:01:05', 0);
COMMIT;

-- ----------------------------
-- Table structure for tb_comment
-- ----------------------------
DROP TABLE IF EXISTS `tb_comment`;
CREATE TABLE `tb_comment` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
                              `post_id` bigint NOT NULL COMMENT '动态ID',
                              `user_id` bigint NOT NULL COMMENT '用户ID',
                              `content` text NOT NULL COMMENT '评论内容',
                              `parent_comment_id` bigint DEFAULT NULL COMMENT '父评论ID（用于多级回复）',
                              `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `deleted` int DEFAULT '0' COMMENT '逻辑删除标记',
                              PRIMARY KEY (`id`),
                              KEY `idx_post` (`post_id`),
                              KEY `idx_user` (`user_id`),
                              KEY `idx_parent` (`parent_comment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论/回复表';

-- ----------------------------
-- Records of tb_comment
-- ----------------------------
BEGIN;
INSERT INTO `tb_comment` (`id`, `post_id`, `user_id`, `content`, `parent_comment_id`, `create_time`, `deleted`) VALUES (1, 1, 2, '欢迎欢迎！', NULL, '2025-12-03 13:01:05', 0);
INSERT INTO `tb_comment` (`id`, `post_id`, `user_id`, `content`, `parent_comment_id`, `create_time`, `deleted`) VALUES (2, 1, 3, '你好呀~', NULL, '2025-12-03 13:01:05', 0);
INSERT INTO `tb_comment` (`id`, `post_id`, `user_id`, `content`, `parent_comment_id`, `create_time`, `deleted`) VALUES (3, 3, 1, '我也想去，几点？', NULL, '2025-12-03 13:01:05', 0);
INSERT INTO `tb_comment` (`id`, `post_id`, `user_id`, `content`, `parent_comment_id`, `create_time`, `deleted`) VALUES (4, 3, 3, '算我一个！', NULL, '2025-12-03 13:01:05', 0);
INSERT INTO `tb_comment` (`id`, `post_id`, `user_id`, `content`, `parent_comment_id`, `create_time`, `deleted`) VALUES (5, 4, 3, '感谢分享，我试试看', NULL, '2025-12-03 13:01:05', 0);
INSERT INTO `tb_comment` (`id`, `post_id`, `user_id`, `content`, `parent_comment_id`, `create_time`, `deleted`) VALUES (6, 10, 4, '1111', NULL, '2025-12-03 13:53:16', 0);
INSERT INTO `tb_comment` (`id`, `post_id`, `user_id`, `content`, `parent_comment_id`, `create_time`, `deleted`) VALUES (7, 10, 4, '111', 6, '2025-12-03 13:53:50', 0);
INSERT INTO `tb_comment` (`id`, `post_id`, `user_id`, `content`, `parent_comment_id`, `create_time`, `deleted`) VALUES (8, 10, 4, '1111', NULL, '2025-12-03 13:57:06', 0);
INSERT INTO `tb_comment` (`id`, `post_id`, `user_id`, `content`, `parent_comment_id`, `create_time`, `deleted`) VALUES (9, 10, 4, '111111', NULL, '2025-12-03 13:59:03', 0);
INSERT INTO `tb_comment` (`id`, `post_id`, `user_id`, `content`, `parent_comment_id`, `create_time`, `deleted`) VALUES (10, 9, 4, '1111', NULL, '2025-12-03 19:52:02', 0);
INSERT INTO `tb_comment` (`id`, `post_id`, `user_id`, `content`, `parent_comment_id`, `create_time`, `deleted`) VALUES (11, 12, 9, 'asdsad', NULL, '2025-12-03 22:11:06', 0);
INSERT INTO `tb_comment` (`id`, `post_id`, `user_id`, `content`, `parent_comment_id`, `create_time`, `deleted`) VALUES (12, 11, 9, 'sadasd', NULL, '2025-12-03 22:15:22', 0);
COMMIT;

-- ----------------------------
-- Table structure for tb_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_message`;
CREATE TABLE `tb_message` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
                              `sender_id` bigint NOT NULL COMMENT '发送者ID',
                              `receiver_id` bigint NOT NULL COMMENT '接收者ID',
                              `content` text NOT NULL COMMENT '消息内容',
                              `type` varchar(20) DEFAULT 'text' COMMENT '消息类型：text-文本, image-图片, video-视频',
                              `media_urls` text COMMENT '媒体文件URL（JSON数组）',
                              `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读',
                              `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `deleted` int DEFAULT '0' COMMENT '逻辑删除标记',
                              PRIMARY KEY (`id`),
                              KEY `idx_sender` (`sender_id`),
                              KEY `idx_receiver` (`receiver_id`),
                              KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息表';

-- ----------------------------
-- Records of tb_message
-- ----------------------------
BEGIN;
INSERT INTO `tb_message` (`id`, `sender_id`, `receiver_id`, `content`, `type`, `media_urls`, `is_read`, `create_time`, `deleted`) VALUES (1, 4, 1, 'aaa1', 'text', NULL, 0, '2025-12-03 21:19:30', 0);
INSERT INTO `tb_message` (`id`, `sender_id`, `receiver_id`, `content`, `type`, `media_urls`, `is_read`, `create_time`, `deleted`) VALUES (2, 4, 1, 'ccc\n', 'text', NULL, 0, '2025-12-03 21:19:55', 0);
INSERT INTO `tb_message` (`id`, `sender_id`, `receiver_id`, `content`, `type`, `media_urls`, `is_read`, `create_time`, `deleted`) VALUES (3, 7, 4, 'aaaa', 'text', NULL, 0, '2025-12-03 22:03:25', 0);
COMMIT;

-- ----------------------------
-- Table structure for tb_notification
-- ----------------------------
DROP TABLE IF EXISTS `tb_notification`;
CREATE TABLE `tb_notification` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
                                   `user_id` bigint NOT NULL COMMENT '接收用户ID',
                                   `sender_id` bigint DEFAULT NULL COMMENT '发送者用户ID（触发通知的用户）',
                                   `type` varchar(20) NOT NULL COMMENT '通知类型：like-点赞, comment-评论, follow-关注, system-系统',
                                   `content` text NOT NULL COMMENT '通知内容',
                                   `related_id` bigint DEFAULT NULL COMMENT '关联ID（如动态ID、评论ID等）',
                                   `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `deleted` int DEFAULT '0' COMMENT '逻辑删除标记',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_user` (`user_id`),
                                   KEY `idx_is_read` (`is_read`),
                                   KEY `idx_sender` (`sender_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知表';

-- ----------------------------
-- Records of tb_notification
-- ----------------------------
BEGIN;
INSERT INTO `tb_notification` (`id`, `user_id`, `sender_id`, `type`, `content`, `related_id`, `is_read`, `create_time`, `deleted`) VALUES (1, 1, NULL, 'like', 'Alice 点赞了你的动态', 1, 0, '2025-12-03 13:01:05', 0);
INSERT INTO `tb_notification` (`id`, `user_id`, `sender_id`, `type`, `content`, `related_id`, `is_read`, `create_time`, `deleted`) VALUES (2, 1, NULL, 'comment', 'Bob 评论了你的动态', 1, 0, '2025-12-03 13:01:05', 0);
INSERT INTO `tb_notification` (`id`, `user_id`, `sender_id`, `type`, `content`, `related_id`, `is_read`, `create_time`, `deleted`) VALUES (3, 2, NULL, 'follow', 'testuser 关注了你', 1, 1, '2025-12-03 13:01:05', 0);
INSERT INTO `tb_notification` (`id`, `user_id`, `sender_id`, `type`, `content`, `related_id`, `is_read`, `create_time`, `deleted`) VALUES (4, 1, NULL, 'system', '欢迎使用知名校友社交平台！', NULL, 0, '2025-12-03 13:01:05', 0);
INSERT INTO `tb_notification` (`id`, `user_id`, `sender_id`, `type`, `content`, `related_id`, `is_read`, `create_time`, `deleted`) VALUES (5, 2, 4, 'like', '有人点赞了你的动态', 4, 0, '2025-12-03 21:45:06', 0);
INSERT INTO `tb_notification` (`id`, `user_id`, `sender_id`, `type`, `content`, `related_id`, `is_read`, `create_time`, `deleted`) VALUES (6, 4, 7, 'like', '有人点赞了你的动态', 12, 0, '2025-12-03 22:03:19', 0);
INSERT INTO `tb_notification` (`id`, `user_id`, `sender_id`, `type`, `content`, `related_id`, `is_read`, `create_time`, `deleted`) VALUES (7, 4, 9, 'like', '有人点赞了你的动态', 12, 0, '2025-12-03 22:11:03', 0);
INSERT INTO `tb_notification` (`id`, `user_id`, `sender_id`, `type`, `content`, `related_id`, `is_read`, `create_time`, `deleted`) VALUES (8, 4, 9, 'comment', '有人评论了你的动态', 12, 1, '2025-12-03 22:11:06', 0);
INSERT INTO `tb_notification` (`id`, `user_id`, `sender_id`, `type`, `content`, `related_id`, `is_read`, `create_time`, `deleted`) VALUES (9, 4, 9, 'like', '有人点赞了你的动态', 11, 1, '2025-12-03 22:15:19', 0);
INSERT INTO `tb_notification` (`id`, `user_id`, `sender_id`, `type`, `content`, `related_id`, `is_read`, `create_time`, `deleted`) VALUES (10, 4, 9, 'comment', '有人评论了你的动态', 11, 1, '2025-12-03 22:15:22', 0);
COMMIT;

-- ----------------------------
-- Table structure for tb_post
-- ----------------------------
DROP TABLE IF EXISTS `tb_post`;
CREATE TABLE `tb_post` (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '动态ID',
                           `user_id` bigint NOT NULL COMMENT '用户ID',
                           `username` varchar(50) DEFAULT NULL COMMENT '用户名（冗余字段，避免JOIN）',
                           `content` text NOT NULL COMMENT '内容',
                           `media_urls` text COMMENT '图片/视频URL（JSON数组）',
                           `type` varchar(20) DEFAULT 'normal' COMMENT '类型：normal-普通, question-提问',
                           `visibility` varchar(50) DEFAULT 'public' COMMENT '可见范围：public-公开, circle_id-圈子',
                           `circle_id` bigint DEFAULT NULL COMMENT '圈子ID',
                           `likes_count` int DEFAULT '0' COMMENT '点赞数',
                           `comments_count` int DEFAULT '0' COMMENT '评论数',
                           `shares_count` int DEFAULT '0' COMMENT '分享数',
                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `deleted` int DEFAULT '0' COMMENT '逻辑删除标记',
                           PRIMARY KEY (`id`),
                           KEY `idx_user_id` (`user_id`),
                           KEY `idx_circle_id` (`circle_id`),
                           KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='动态/帖子表';

-- ----------------------------
-- Records of tb_post
-- ----------------------------
BEGIN;
INSERT INTO `tb_post` (`id`, `user_id`, `username`, `content`, `media_urls`, `type`, `visibility`, `circle_id`, `likes_count`, `comments_count`, `shares_count`, `create_time`, `update_time`, `deleted`) VALUES (1, 1, 'testuser', '这是我的第一条动态！大家好！', NULL, 'normal', 'public', NULL, 10, 3, 0, '2025-12-03 13:01:05', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_post` (`id`, `user_id`, `username`, `content`, `media_urls`, `type`, `visibility`, `circle_id`, `likes_count`, `comments_count`, `shares_count`, `create_time`, `update_time`, `deleted`) VALUES (2, 1, 'testuser', '今天天气真不错，适合出去玩~', '[\"https://images.unsplash.com/photo-1506905925346-21bda4d32df4\"]', 'normal', 'public', NULL, 5, 1, 0, '2025-12-03 13:01:05', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_post` (`id`, `user_id`, `username`, `content`, `media_urls`, `type`, `visibility`, `circle_id`, `likes_count`, `comments_count`, `shares_count`, `create_time`, `update_time`, `deleted`) VALUES (3, 2, 'alice', '有人一起去图书馆学习吗？', NULL, 'question', 'public', NULL, 8, 5, 0, '2025-12-03 13:01:05', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_post` (`id`, `user_id`, `username`, `content`, `media_urls`, `type`, `visibility`, `circle_id`, `likes_count`, `comments_count`, `shares_count`, `create_time`, `update_time`, `deleted`) VALUES (4, 2, 'alice', '分享一个学习小技巧：番茄工作法真的很有用！', '[\"https://images.unsplash.com/photo-1434030216411-0b793f4b4173\", \"https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b\"]', 'normal', 'public', NULL, 16, 1, 0, '2025-12-03 13:01:05', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_post` (`id`, `user_id`, `username`, `content`, `media_urls`, `type`, `visibility`, `circle_id`, `likes_count`, `comments_count`, `shares_count`, `create_time`, `update_time`, `deleted`) VALUES (5, 3, 'bob', '食堂新出的菜品味道不错，推荐！', '[\"https://images.unsplash.com/photo-1504674900247-0877df9cc836\"]', 'normal', 'public', NULL, 12, 4, 0, '2025-12-03 13:01:05', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_post` (`id`, `user_id`, `username`, `content`, `media_urls`, `type`, `visibility`, `circle_id`, `likes_count`, `comments_count`, `shares_count`, `create_time`, `update_time`, `deleted`) VALUES (6, 1, 'testuser', '校园的秋天真美，分享几张照片~', '[\"https://images.unsplash.com/photo-1441974231531-c6227db76b6e\", \"https://images.unsplash.com/photo-1472214103451-9374bd1c798e\", \"https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05\"]', 'normal', 'public', NULL, 25, 8, 0, '2025-12-03 13:01:05', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_post` (`id`, `user_id`, `username`, `content`, `media_urls`, `type`, `visibility`, `circle_id`, `likes_count`, `comments_count`, `shares_count`, `create_time`, `update_time`, `deleted`) VALUES (7, 2, 'alice', '今天去咖啡店学习，环境超棒！', '[\"https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb\", \"https://images.unsplash.com/photo-1509042239860-f550ce710b93\"]', 'normal', 'public', NULL, 18, 6, 0, '2025-12-03 13:01:05', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_post` (`id`, `user_id`, `username`, `content`, `media_urls`, `type`, `visibility`, `circle_id`, `likes_count`, `comments_count`, `shares_count`, `create_time`, `update_time`, `deleted`) VALUES (8, 3, 'bob', '周末爬山活动圆满成功！感谢大家参与~', '[\"https://images.unsplash.com/photo-1506905925346-21bda4d32df4\", \"https://images.unsplash.com/photo-1454496522488-7a8e488e8606\", \"https://images.unsplash.com/photo-1477346611705-65d1883cee1e\", \"https://images.unsplash.com/photo-1464822759023-fed622ff2c3b\"]', 'normal', 'public', NULL, 31, 0, 0, '2025-12-03 13:01:05', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_post` (`id`, `user_id`, `username`, `content`, `media_urls`, `type`, `visibility`, `circle_id`, `likes_count`, `comments_count`, `shares_count`, `create_time`, `update_time`, `deleted`) VALUES (9, 4, '1111111', '刚注册，来打个卡！', NULL, 'normal', 'public', NULL, 3, 1, 0, '2025-12-03 13:01:05', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_post` (`id`, `user_id`, `username`, `content`, `media_urls`, `type`, `visibility`, `circle_id`, `likes_count`, `comments_count`, `shares_count`, `create_time`, `update_time`, `deleted`) VALUES (10, 4, '1111111', '1111', NULL, 'normal', 'public', NULL, 0, 2, 0, '2025-12-03 13:26:46', '2025-12-03 13:26:46', 0);
INSERT INTO `tb_post` (`id`, `user_id`, `username`, `content`, `media_urls`, `type`, `visibility`, `circle_id`, `likes_count`, `comments_count`, `shares_count`, `create_time`, `update_time`, `deleted`) VALUES (11, 4, '1111111', '1111', '[\"http://localhost:9000/zmxy-meet/images/cea6f65d-e206-4b8c-892c-eef613f240e7.jpg\",\"http://localhost:9000/zmxy-meet/images/a086ee94-6f90-4469-b9ef-c2ba0d8dee91.jpg\"]', 'normal', 'public', NULL, 1, 0, 0, '2025-12-03 19:52:36', '2025-12-03 19:52:36', 0);
INSERT INTO `tb_post` (`id`, `user_id`, `username`, `content`, `media_urls`, `type`, `visibility`, `circle_id`, `likes_count`, `comments_count`, `shares_count`, `create_time`, `update_time`, `deleted`) VALUES (12, 4, '1111111', '111112222', '[\"http://10.0.2.2:9000/zmxy-meet/images/464befbd-2dac-49d3-b705-61861446a722.jpg\"]', 'normal', 'public', NULL, 3, 0, 0, '2025-12-03 19:56:34', '2025-12-03 19:56:34', 0);
INSERT INTO `tb_post` (`id`, `user_id`, `username`, `content`, `media_urls`, `type`, `visibility`, `circle_id`, `likes_count`, `comments_count`, `shares_count`, `create_time`, `update_time`, `deleted`) VALUES (13, 4, '1111111', 'ccccdddd', '[\"http://10.0.2.2:9000/zmxy-meet/images/bc5d1b80-403f-4264-bcbb-1a3fc17b664d.jpg\",\"http://10.0.2.2:9000/zmxy-meet/images/28c05baa-826f-4e34-a95c-a4358bf661b7.jpg\",\"http://10.0.2.2:9000/zmxy-meet/images/290841fd-6949-4544-b6e3-3780770c86cd.jpg\"]', 'normal', 'public', NULL, 2, 0, 0, '2025-12-03 19:56:53', '2025-12-03 22:01:25', 1);
COMMIT;

-- ----------------------------
-- Table structure for tb_relation
-- ----------------------------
DROP TABLE IF EXISTS `tb_relation`;
CREATE TABLE `tb_relation` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关系ID',
                               `user_id_a` bigint NOT NULL COMMENT '用户A的ID',
                               `user_id_b` bigint NOT NULL COMMENT '用户B的ID',
                               `relation_type` varchar(20) NOT NULL COMMENT '关系类型：follow-关注, friend-好友',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `deleted` int DEFAULT '0' COMMENT '逻辑删除标记',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_relation` (`user_id_a`,`user_id_b`,`relation_type`),
                               KEY `idx_user_a` (`user_id_a`),
                               KEY `idx_user_b` (`user_id_b`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='关系链表';

-- ----------------------------
-- Records of tb_relation
-- ----------------------------
BEGIN;
INSERT INTO `tb_relation` (`id`, `user_id_a`, `user_id_b`, `relation_type`, `create_time`, `deleted`) VALUES (1, 1, 2, 'friend', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_relation` (`id`, `user_id_a`, `user_id_b`, `relation_type`, `create_time`, `deleted`) VALUES (2, 2, 1, 'friend', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_relation` (`id`, `user_id_a`, `user_id_b`, `relation_type`, `create_time`, `deleted`) VALUES (3, 1, 3, 'follow', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_relation` (`id`, `user_id_a`, `user_id_b`, `relation_type`, `create_time`, `deleted`) VALUES (4, 3, 1, 'follow', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_relation` (`id`, `user_id_a`, `user_id_b`, `relation_type`, `create_time`, `deleted`) VALUES (5, 2, 3, 'follow', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_relation` (`id`, `user_id_a`, `user_id_b`, `relation_type`, `create_time`, `deleted`) VALUES (6, 4, 2, 'follow', '2025-12-03 21:08:31', 0);
COMMIT;

-- ----------------------------
-- Table structure for tb_report
-- ----------------------------
DROP TABLE IF EXISTS `tb_report`;
CREATE TABLE `tb_report` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '举报ID',
                             `reporter_id` bigint NOT NULL COMMENT '举报人ID',
                             `reported_type` varchar(20) NOT NULL COMMENT '被举报类型：post-帖子, comment-评论, user-用户',
                             `reported_id` bigint NOT NULL COMMENT '被举报对象ID',
                             `reason` varchar(50) NOT NULL COMMENT '举报原因',
                             `description` text COMMENT '详细描述',
                             `status` varchar(20) DEFAULT 'pending' COMMENT '处理状态：pending-待处理, reviewing-审核中, resolved-已处理, rejected-已驳回',
                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
                             `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
                             `handler_id` bigint DEFAULT NULL COMMENT '处理人ID',
                             `handle_result` text COMMENT '处理结果',
                             `deleted` int DEFAULT '0' COMMENT '逻辑删除标记',
                             PRIMARY KEY (`id`),
                             KEY `idx_reporter` (`reporter_id`),
                             KEY `idx_reported` (`reported_type`,`reported_id`),
                             KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='举报表';

-- ----------------------------
-- Records of tb_report
-- ----------------------------
BEGIN;
INSERT INTO `tb_report` (`id`, `reporter_id`, `reported_type`, `reported_id`, `reason`, `description`, `status`, `create_time`, `handle_time`, `handler_id`, `handle_result`, `deleted`) VALUES (1, 5, 'post', 9, '诈骗信息', NULL, 'pending', '2025-12-03 22:10:18', NULL, NULL, NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user` (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                           `username` varchar(50) NOT NULL COMMENT '用户名',
                           `password` varchar(255) NOT NULL COMMENT '密码（加密）',
                           `nickname` varchar(50) NOT NULL COMMENT '昵称',
                           `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
                           `avatar_url` varchar(500) DEFAULT NULL COMMENT '头像URL',
                           `school_verified` tinyint(1) DEFAULT '0' COMMENT '学校认证状态',
                           `campus` varchar(100) DEFAULT NULL COMMENT '校区',
                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `deleted` int DEFAULT '0' COMMENT '逻辑删除标记',
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `username` (`username`),
                           UNIQUE KEY `email` (`email`),
                           KEY `idx_username` (`username`),
                           KEY `idx_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ----------------------------
-- Records of tb_user
-- ----------------------------
BEGIN;
INSERT INTO `tb_user` (`id`, `username`, `password`, `nickname`, `email`, `avatar_url`, `school_verified`, `campus`, `create_time`, `update_time`, `deleted`) VALUES (1, 'testuser', '$2a$10$YIHyRYhwmrqLnhxq4t04.u6HggScy8NAhJZIIpABHjmvIl3bhrtE2', '测试用户', 'test@example.com', NULL, 1, '主校区', '2025-12-03 13:01:05', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_user` (`id`, `username`, `password`, `nickname`, `email`, `avatar_url`, `school_verified`, `campus`, `create_time`, `update_time`, `deleted`) VALUES (2, 'alice', '$2a$10$YIHyRYhwmrqLnhxq4t04.u6HggScy8NAhJZIIpABHjmvIl3bhrtE2', 'Alice', 'alice@example.com', NULL, 1, '东校区', '2025-12-03 13:01:05', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_user` (`id`, `username`, `password`, `nickname`, `email`, `avatar_url`, `school_verified`, `campus`, `create_time`, `update_time`, `deleted`) VALUES (3, 'bob', '$2a$10$YIHyRYhwmrqLnhxq4t04.u6HggScy8NAhJZIIpABHjmvIl3bhrtE2', 'Bob', 'bob@example.com', NULL, 0, '西校区', '2025-12-03 13:01:05', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_user` (`id`, `username`, `password`, `nickname`, `email`, `avatar_url`, `school_verified`, `campus`, `create_time`, `update_time`, `deleted`) VALUES (4, '1111111', '$2a$10$YIHyRYhwmrqLnhxq4t04.u6HggScy8NAhJZIIpABHjmvIl3bhrtE2', '用户1111111', '1111111@example.com', NULL, 1, '主校区', '2025-12-03 13:01:05', '2025-12-03 13:01:05', 0);
INSERT INTO `tb_user` (`id`, `username`, `password`, `nickname`, `email`, `avatar_url`, `school_verified`, `campus`, `create_time`, `update_time`, `deleted`) VALUES (5, '111111', '$2a$10$zPIL8ZPwMLFjlo6jQRR0/OwZE7CYQpvAQBdqqq5gtvnvk7Zz1Zk8W', 'xingyang', '111111@qq.com', NULL, 0, NULL, '2025-12-03 21:28:24', '2025-12-03 21:28:24', 0);
INSERT INTO `tb_user` (`id`, `username`, `password`, `nickname`, `email`, `avatar_url`, `school_verified`, `campus`, `create_time`, `update_time`, `deleted`) VALUES (6, 'xingyang', '$2a$10$ciXa80nZJm4mqaF4SUbAwuAwqfWyB4een3RJ1xnBKJMCPso4pc3RC', 'nini', '1111@qq.com', NULL, 0, NULL, '2025-12-03 21:30:21', '2025-12-03 21:30:21', 0);
INSERT INTO `tb_user` (`id`, `username`, `password`, `nickname`, `email`, `avatar_url`, `school_verified`, `campus`, `create_time`, `update_time`, `deleted`) VALUES (7, 'xing', '$2a$10$2MjOk3nh0zf2l9teH7LdXOgnpVmjK4HPBwVPkOENpwge2a.DxecsC', 'sajidashd', 'wqeqw@qq.com', NULL, 0, NULL, '2025-12-03 21:32:10', '2025-12-03 21:32:10', 0);
INSERT INTO `tb_user` (`id`, `username`, `password`, `nickname`, `email`, `avatar_url`, `school_verified`, `campus`, `create_time`, `update_time`, `deleted`) VALUES (8, 'jijian', '$2a$10$xxzqvFUWx8MvEonTei1Kr.hgdXFfdvivrksmPBGKvoszWRtWdacae', 'sadsad', 'ijojioijo', NULL, 0, NULL, '2025-12-03 21:35:35', '2025-12-03 21:35:35', 0);
INSERT INTO `tb_user` (`id`, `username`, `password`, `nickname`, `email`, `avatar_url`, `school_verified`, `campus`, `create_time`, `update_time`, `deleted`) VALUES (9, '1111', '$2a$10$xr2ysiPrmin.S4Q6Z.l1Iekhy3/4YWlBSPF/brwmGuYDLHkd8sJE.', '1111', '1111', NULL, 0, NULL, '2025-12-03 21:38:30', '2025-12-03 21:38:30', 0);
INSERT INTO `tb_user` (`id`, `username`, `password`, `nickname`, `email`, `avatar_url`, `school_verified`, `campus`, `create_time`, `update_time`, `deleted`) VALUES (10, 'aaaa', '$2a$10$s7PJW8HHmFN69oDMyjzqnOGVpDpwaQUqFMxuTIdoqYXnT6Moni/BW', 'aaaa', 'aaaa', NULL, 0, NULL, '2025-12-03 21:40:54', '2025-12-03 21:40:54', 0);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

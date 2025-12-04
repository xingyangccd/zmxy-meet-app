-- 创建数据库
CREATE DATABASE IF NOT EXISTS zmxy_meet DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE zmxy_meet;

-- 用户表
CREATE TABLE IF NOT EXISTS tb_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    school_verified BOOLEAN DEFAULT FALSE COMMENT '学校认证状态',
    campus VARCHAR(100) COMMENT '校区',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 动态/帖子表
CREATE TABLE IF NOT EXISTS tb_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '动态ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名（冗余字段，避免JOIN）',
    content TEXT NOT NULL COMMENT '内容',
    media_urls TEXT COMMENT '图片/视频URL（JSON数组）',
    type VARCHAR(20) DEFAULT 'normal' COMMENT '类型：normal-普通, question-提问',
    visibility VARCHAR(50) DEFAULT 'public' COMMENT '可见范围：public-公开, circle_id-圈子',
    circle_id BIGINT COMMENT '圈子ID',
    likes_count INT DEFAULT 0 COMMENT '点赞数',
    comments_count INT DEFAULT 0 COMMENT '评论数',
    shares_count INT DEFAULT 0 COMMENT '分享数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_user_id (user_id),
    INDEX idx_circle_id (circle_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态/帖子表';

-- 关系链表
CREATE TABLE IF NOT EXISTS tb_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    user_id_a BIGINT NOT NULL COMMENT '用户A的ID',
    user_id_b BIGINT NOT NULL COMMENT '用户B的ID',
    relation_type VARCHAR(20) NOT NULL COMMENT '关系类型：follow-关注, friend-好友',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记',
    UNIQUE KEY uk_relation (user_id_a, user_id_b, relation_type),
    INDEX idx_user_a (user_id_a),
    INDEX idx_user_b (user_id_b)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关系链表';

-- 消息表
CREATE TABLE IF NOT EXISTS tb_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者ID',
    content TEXT NOT NULL COMMENT '消息内容',
    type VARCHAR(20) DEFAULT 'text' COMMENT '消息类型：text-文本, image-图片, video-视频',
    media_urls TEXT COMMENT '媒体文件URL（JSON数组）',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_sender (sender_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 圈子/群组表
CREATE TABLE IF NOT EXISTS tb_circle (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '圈子ID',
    name VARCHAR(100) NOT NULL COMMENT '圈子名称',
    description TEXT COMMENT '圈子描述',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    type VARCHAR(20) DEFAULT 'interest' COMMENT '圈子类型：interest-兴趣, course-课程, official-官方',
    avatar_url VARCHAR(500) COMMENT '圈子头像',
    members_count INT DEFAULT 0 COMMENT '成员数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_creator (creator_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子/群组表';

-- 圈子成员表
CREATE TABLE IF NOT EXISTS tb_circle_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成员关系ID',
    circle_id BIGINT NOT NULL COMMENT '圈子ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role VARCHAR(20) DEFAULT 'member' COMMENT '角色：admin-管理员, member-成员',
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记',
    UNIQUE KEY uk_circle_member (circle_id, user_id),
    INDEX idx_circle (circle_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子成员表';

-- 评论/回复表
CREATE TABLE IF NOT EXISTS tb_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    post_id BIGINT NOT NULL COMMENT '动态ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_comment_id BIGINT COMMENT '父评论ID（用于多级回复）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_post (post_id),
    INDEX idx_user (user_id),
    INDEX idx_parent (parent_comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论/回复表';

-- 通知表
CREATE TABLE IF NOT EXISTS tb_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    sender_id BIGINT COMMENT '发送者用户ID',
    type VARCHAR(20) NOT NULL COMMENT '通知类型：like-点赞, comment-评论, follow-关注, system-系统',
    content TEXT NOT NULL COMMENT '通知内容',
    related_id BIGINT COMMENT '关联ID（如动态ID、评论ID等）',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_user (user_id),
    INDEX idx_sender (sender_id),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 举报表
CREATE TABLE IF NOT EXISTS tb_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '举报ID',
    reporter_id BIGINT NOT NULL COMMENT '举报人ID',
    reported_type VARCHAR(20) NOT NULL COMMENT '被举报类型：post-帖子, comment-评论, user-用户',
    reported_id BIGINT NOT NULL COMMENT '被举报对象ID',
    reason VARCHAR(50) NOT NULL COMMENT '举报原因',
    description TEXT COMMENT '详细描述',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '处理状态：pending-待处理, reviewing-审核中, resolved-已处理, rejected-已驳回',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
    handle_time DATETIME COMMENT '处理时间',
    handler_id BIGINT COMMENT '处理人ID',
    handle_result TEXT COMMENT '处理结果',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_reporter (reporter_id),
    INDEX idx_reported (reported_type, reported_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- ============================================
-- 测试数据
-- ============================================

-- 清空现有测试数据（按依赖顺序删除）
DELETE FROM tb_notification;
DELETE FROM tb_comment;
DELETE FROM tb_relation;
DELETE FROM tb_circle_member;
DELETE FROM tb_circle;
DELETE FROM tb_post;
DELETE FROM tb_message;
DELETE FROM tb_user;

-- 重置自增ID
ALTER TABLE tb_user AUTO_INCREMENT = 1;
ALTER TABLE tb_post AUTO_INCREMENT = 1;
ALTER TABLE tb_circle AUTO_INCREMENT = 1;
ALTER TABLE tb_circle_member AUTO_INCREMENT = 1;
ALTER TABLE tb_comment AUTO_INCREMENT = 1;
ALTER TABLE tb_relation AUTO_INCREMENT = 1;
ALTER TABLE tb_notification AUTO_INCREMENT = 1;
ALTER TABLE tb_message AUTO_INCREMENT = 1;

-- 插入测试用户
-- 密码都是 "123456"，使用 BCrypt 加密后的值
INSERT INTO tb_user (username, password, nickname, email, school_verified, campus) VALUES
('testuser', '$2a$10$YIHyRYhwmrqLnhxq4t04.u6HggScy8NAhJZIIpABHjmvIl3bhrtE2', '测试用户', 'test@example.com', TRUE, '主校区'),
('alice', '$2a$10$YIHyRYhwmrqLnhxq4t04.u6HggScy8NAhJZIIpABHjmvIl3bhrtE2', 'Alice', 'alice@example.com', TRUE, '东校区'),
('bob', '$2a$10$YIHyRYhwmrqLnhxq4t04.u6HggScy8NAhJZIIpABHjmvIl3bhrtE2', 'Bob', 'bob@example.com', FALSE, '西校区'),
('1111111', '$2a$10$YIHyRYhwmrqLnhxq4t04.u6HggScy8NAhJZIIpABHjmvIl3bhrtE2', '用户1111111', '1111111@example.com', TRUE, '主校区');

-- 插入测试动态（包含带图片的帖子）
INSERT INTO tb_post (user_id, username, content, media_urls, type, visibility, likes_count, comments_count) VALUES
(1, 'testuser', '这是我的第一条动态！大家好！', NULL, 'normal', 'public', 10, 3),
(1, 'testuser', '今天天气真不错，适合出去玩~', '["https://images.unsplash.com/photo-1506905925346-21bda4d32df4"]', 'normal', 'public', 5, 1),
(2, 'alice', '有人一起去图书馆学习吗？', NULL, 'question', 'public', 8, 5),
(2, 'alice', '分享一个学习小技巧：番茄工作法真的很有用！', '["https://images.unsplash.com/photo-1434030216411-0b793f4b4173", "https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b"]', 'normal', 'public', 15, 7),
(3, 'bob', '食堂新出的菜品味道不错，推荐！', '["https://images.unsplash.com/photo-1504674900247-0877df9cc836"]', 'normal', 'public', 12, 4),
(1, 'testuser', '校园的秋天真美，分享几张照片~', '["https://images.unsplash.com/photo-1441974231531-c6227db76b6e", "https://images.unsplash.com/photo-1472214103451-9374bd1c798e", "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05"]', 'normal', 'public', 25, 8),
(2, 'alice', '今天去咖啡店学习，环境超棒！', '["https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb", "https://images.unsplash.com/photo-1509042239860-f550ce710b93"]', 'normal', 'public', 18, 6),
(3, 'bob', '周末爬山活动圆满成功！感谢大家参与~', '["https://images.unsplash.com/photo-1506905925346-21bda4d32df4", "https://images.unsplash.com/photo-1454496522488-7a8e488e8606", "https://images.unsplash.com/photo-1477346611705-65d1883cee1e", "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b"]', 'normal', 'public', 30, 12),
(4, '1111111', '刚注册，来打个卡！', NULL, 'normal', 'public', 2, 1);

-- 插入测试圈子
INSERT INTO tb_circle (name, description, creator_id, type, members_count) VALUES
('编程爱好者', '一起学习编程，分享技术心得', 1, 'interest', 50),
('羽毛球社', '校园羽毛球爱好者交流群', 2, 'interest', 30),
('数据结构课程', '2024春季数据结构课程讨论组', 1, 'course', 120);

-- 插入圈子成员
INSERT INTO tb_circle_member (circle_id, user_id, role) VALUES
(1, 1, 'admin'),
(1, 2, 'member'),
(1, 3, 'member'),
(2, 2, 'admin'),
(2, 1, 'member'),
(3, 1, 'admin'),
(3, 2, 'member'),
(3, 3, 'member');

-- 插入测试评论
INSERT INTO tb_comment (post_id, user_id, content) VALUES
(1, 2, '欢迎欢迎！'),
(1, 3, '你好呀~'),
(3, 1, '我也想去，几点？'),
(3, 3, '算我一个！'),
(4, 3, '感谢分享，我试试看');

-- 插入测试关系
INSERT INTO tb_relation (user_id_a, user_id_b, relation_type) VALUES
(1, 2, 'friend'),
(2, 1, 'friend'),
(1, 3, 'follow'),
(3, 1, 'follow'),
(2, 3, 'follow');

-- 插入测试通知
INSERT INTO tb_notification (user_id, type, content, related_id, is_read) VALUES
(1, 'like', 'Alice 点赞了你的动态', 1, FALSE),
(1, 'comment', 'Bob 评论了你的动态', 1, FALSE),
(2, 'follow', 'testuser 关注了你', 1, TRUE),
(1, 'system', '欢迎使用知名校友社交平台！', NULL, FALSE);

# 数据库和存储配置说明

## 📊 数据库表结构

### 核心表

1. **tb_user** - 用户表
   - 存储用户基本信息（用户名、密码、昵称、邮箱等）
   - `avatar_url` 字段存储头像URL（MinIO地址）

2. **tb_post** - 动态/帖子表
   - 存储用户发布的动态
   - `username` 字段：用户名（冗余字段，避免JOIN查询）
   - `media_urls` 字段：图片/视频URL列表，**JSON数组格式**
   - 示例：`["http://localhost:9000/zmxy-meet/images/abc.jpg", "http://localhost:9000/zmxy-meet/images/def.jpg"]`

3. **tb_message** - 消息表
   - 存储私信
   - 支持文本、图片、视频消息
   - `media_urls` 字段同样是JSON数组格式

4. **tb_circle** - 圈子表
   - 存储兴趣圈、课程群组等

5. **tb_comment** - 评论表
   - 存储动态评论
   - 支持多级回复

6. **tb_notification** - 通知表
   - 点赞、评论、关注等通知

7. **tb_relation** - 关系链表
   - 用户关注、好友关系

---

## 🗄️ MinIO 对象存储配置

### MinIO 服务配置

```yaml
# application.yml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: zmxy-meet
```

### MinIO 启动命令

```bash
# Docker 方式启动 MinIO
docker run -p 9000:9000 -p 9001:9001 \
  --name minio \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  -v ~/minio/data:/data \
  minio/minio server /data --console-address ":9001"
```

### 访问地址

- **API地址**: http://localhost:9000
- **管理控制台**: http://localhost:9001
- **默认用户名**: minioadmin
- **默认密码**: minioadmin

### 首次使用配置

1. 访问 http://localhost:9001 登录管理控制台
2. 创建 Bucket（桶）：`zmxy-meet`
3. 设置 Bucket 访问权限为 **public**（公开读取）
4. 在 Bucket 中创建文件夹：
   - `images/` - 存储图片
   - `videos/` - 存储视频
   - `avatars/` - 存储用户头像

---

## 📂 文件存储路径规范

### 图片存储

```
http://localhost:9000/zmxy-meet/images/2024/12/03/uuid.jpg
```

### 视频存储

```
http://localhost:9000/zmxy-meet/videos/2024/12/03/uuid.mp4
```

### 头像存储

```
http://localhost:9000/zmxy-meet/avatars/user-{userId}.jpg
```

---

## 🔧 数据库初始化

### 1. 创建数据库

```bash
# 连接 MySQL
mysql -u root -p

# 执行初始化脚本
source /path/to/init.sql
```

或者直接在 IDEA 中右键 `init.sql` → `Run`

### 2. 验证数据

```sql
-- 查看用户
SELECT * FROM tb_user;

-- 查看带图片的动态
SELECT id, username, content, media_urls, likes_count 
FROM tb_post 
WHERE media_urls IS NOT NULL;

-- 查看所有动态数量
SELECT COUNT(*) FROM tb_post;
```

---

## 📸 测试数据说明

### 已插入的测试用户

| 用户名 | 密码 | 昵称 | ID |
|--------|------|------|-----|
| testuser | 123456 | 测试用户 | 1 |
| alice | 123456 | Alice | 2 |
| bob | 123456 | Bob | 3 |
| 1111111 | 123456 | 用户1111111 | 4 |

### 已插入的测试动态

数据库中已插入 **9 条测试动态**，其中包括：

- ✅ 3条纯文本动态
- ✅ 1条单图动态
- ✅ 2条双图动态
- ✅ 1条三图动态
- ✅ 1条四图动态

### 图片说明

测试数据使用 **Unsplash 免费图片**作为示例：
- 这些是公开可访问的图片URL
- 用于测试图片显示功能
- 实际使用时会替换为 MinIO 存储的图片

---

## 🚀 完整启动流程

### 1. 启动 MySQL

```bash
# 确保 MySQL 运行在 localhost:3306
mysql.server start  # macOS
```

### 2. 启动 Redis

```bash
redis-server
```

### 3. 启动 MinIO

```bash
docker start minio
# 或
docker run -p 9000:9000 -p 9001:9001 --name minio ...
```

### 4. 初始化数据库

```bash
mysql -u root -p < init.sql
```

### 5. 启动后端

```bash
cd spr/zmxy-meet
mvn spring-boot:run
```

### 6. 启动 Android 应用

在 Android Studio 中运行应用

---

## 📱 API 端点说明

### 文件上传

```
POST /api/file/upload/image
Content-Type: multipart/form-data

返回格式：
{
  "code": 200,
  "data": {
    "url": "http://localhost:9000/zmxy-meet/images/2024/12/03/uuid.jpg"
  }
}
```

### 发布带图动态

```
POST /api/posts
Content-Type: application/json
Authorization: Bearer {token}

{
  "content": "今天天气真好！",
  "mediaUrls": "[\"http://localhost:9000/zmxy-meet/images/abc.jpg\"]",
  "type": "normal",
  "visibility": "public"
}
```

### 获取动态列表

```
GET /api/posts?page=1&size=20
Authorization: Bearer {token}

返回：
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "username": "testuser",
      "content": "动态内容",
      "mediaUrls": "[\"图片URL1\", \"图片URL2\"]",
      "likesCount": 10,
      "commentsCount": 3,
      "createTime": "2024-12-03T..."
    }
  ]
}
```

---

## ⚠️ 常见问题

### 1. 图片无法显示

**问题**: Android 应用中图片不显示

**原因**: MinIO Bucket 权限未设置为 public

**解决**: 
1. 登录 MinIO 控制台 http://localhost:9001
2. 进入 `zmxy-meet` bucket
3. 设置访问策略为 `public`

### 2. 文件上传失败

**问题**: 上传图片返回错误

**原因**: MinIO 服务未启动或 Bucket 不存在

**解决**:
```bash
# 检查 MinIO 是否运行
docker ps | grep minio

# 创建 Bucket
# 访问 http://localhost:9001 手动创建
```

### 3. Android 无法连接后端

**问题**: 网络请求超时

**原因**: Android 模拟器无法访问 localhost

**解决**: 使用 `10.0.2.2` 代替 `localhost`
```kotlin
// ApiModule.kt
private const val BASE_URL = "http://10.0.2.2:8081/"
```

### 4. 图片加载很慢

**问题**: Unsplash 图片加载慢

**原因**: 测试数据使用的是外部图片

**解决**: 上传图片到 MinIO，使用本地存储的图片

---

## 🔄 数据库更新记录

### 2024-12-03
- ✅ 添加 `tb_post.username` 字段（避免JOIN查询）
- ✅ 完善测试数据，添加带图片的帖子
- ✅ 更新测试数据中的图片URL格式

### 重要字段说明

- **media_urls**: 必须是 **JSON 数组字符串格式**
  - ✅ 正确: `'["url1", "url2"]'`
  - ❌ 错误: `'url1,url2'`
  - ❌ 错误: `'url1'`

---

## 📖 相关文档

- [MinIO 官方文档](https://min.io/docs/minio/linux/index.html)
- [Spring Boot MinIO 集成](https://docs.min.io/docs/java-client-quickstart-guide.html)
- [MyBatis-Plus 文档](https://baomidou.com/)

---

**最后更新**: 2024-12-03

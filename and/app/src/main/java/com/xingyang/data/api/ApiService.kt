package com.xingyang.data.api

import retrofit2.http.*

interface ApiService {
    companion object {
        // 使用 10.0.2.2 来访问宿主机的 localhost（模拟器专用）
        // 如果使用真机，请改为你电脑的 IP 地址
        const val BASE_URL = "http://10.0.2.2:8081/"
    }

    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Result<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Result<RegisterResponse>

    // User
    @GET("api/user/profile")
    suspend fun getUserProfile(): UserProfile

    @PUT("api/user/profile")
    suspend fun updateUserProfile(@Body request: UpdateProfileRequest): UserProfile

    // Posts
    @GET("api/posts")
    suspend fun getPosts(): Result<List<Post>>
    
    @GET("api/posts")
    suspend fun getFeedPosts(@Query("page") page: Int = 1, @Query("size") size: Int = 20): Result<List<Post>>

    @POST("api/posts")
    suspend fun createPost(@Body request: CreatePostRequest): Post
    
    @POST("api/posts/{id}/like")
    suspend fun likePost(@Path("id") postId: Long): Result<Void>
    
    @DELETE("api/posts/{id}/like")
    suspend fun unlikePost(@Path("id") postId: Long): Result<Void>
    
    @GET("api/posts/{id}/like/status")
    suspend fun checkLikeStatus(@Path("id") postId: Long): Result<Boolean>
    
    @GET("api/posts/{id}")
    suspend fun getPostDetail(@Path("id") postId: Long): Result<Post>
    
    @PUT("api/posts/{id}")
    suspend fun updatePost(@Path("id") postId: Long, @Body request: CreatePostRequest): Result<Post>
    
    @DELETE("api/posts/{id}")
    suspend fun deletePost(@Path("id") postId: Long): Result<Void>
    
    // Comments
    @GET("api/posts/{id}/comments")
    suspend fun getPostComments(@Path("id") postId: Long): Result<List<Comment>>
    
    @POST("api/posts/{id}/comments")
    suspend fun addComment(@Path("id") postId: Long, @Body request: AddCommentRequest): Result<Comment>
    
    @POST("api/comments/{id}/reply")
    suspend fun replyToComment(@Path("id") commentId: Long, @Body request: AddCommentRequest): Result<Comment>
    
    // User & Follow
    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") userId: Long): Result<User>
    
    @GET("api/users/{id}/posts")
    suspend fun getUserPosts(@Path("id") userId: Long): Result<List<Post>>
    
    @POST("api/users/{id}/follow")
    suspend fun followUser(@Path("id") userId: Long): Result<Void>
    
    @DELETE("api/users/{id}/follow")
    suspend fun unfollowUser(@Path("id") userId: Long): Result<Void>
    
    @GET("api/users/{id}/follow/status")
    suspend fun checkFollowStatus(@Path("id") userId: Long): Result<Boolean>
    
    @GET("api/users/{id}/stats")
    suspend fun getUserStats(@Path("id") userId: Long): Result<UserStats>
    
    @GET("api/users/{id}/following")
    suspend fun getFollowingList(@Path("id") userId: Long): Result<List<User>>
    
    @GET("api/users/{id}/followers")
    suspend fun getFollowersList(@Path("id") userId: Long): Result<List<User>>

    // Circles
    @GET("api/circles")
    suspend fun getCircles(): Result<List<Circle>>

    @POST("api/circles")
    suspend fun createCircle(@Body request: CreateCircleRequest): Result<Circle>
    
    // File Upload
    @Multipart
    @POST("api/file/upload/image")
    suspend fun uploadImage(@Part file: okhttp3.MultipartBody.Part): Result<UploadResponse>
    
    @Multipart
    @POST("api/file/upload/video")
    suspend fun uploadVideo(@Part file: okhttp3.MultipartBody.Part): Result<UploadResponse>
    
    @GET("api/file/presigned-url")
    suspend fun getPresignedUrl(
        @Query("fileName") fileName: String,
        @Query("folder") folder: String = "images"
    ): Result<PresignedUrlResponse>
    
    // Messages
    @GET("api/messages/conversations")
    suspend fun getConversations(): Result<List<Conversation>>
    
    @GET("api/messages/history/{otherUserId}")
    suspend fun getChatHistory(
        @Path("otherUserId") otherUserId: Long,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 50
    ): Result<List<ChatMessage>>
    
    // Notifications
    @GET("api/notifications")
    suspend fun getNotifications(): Result<List<Notification>>
    
    @GET("api/notifications/unread-count")
    suspend fun getUnreadCount(): Result<Map<String, Long>>
    
    @PUT("api/notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") id: Long): Result<Unit>
    
    @PUT("api/notifications/read-all")
    suspend fun markAllNotificationsAsRead(): Result<Unit>
    
    @POST("api/messages/send")
    suspend fun sendMessage(@Body request: SendMessageRequest): Result<ChatMessage>
    
    @GET("api/messages/unread/count")
    suspend fun getUnreadMessageCount(): Result<Int>
    
    // Reports
    @POST("api/reports")
    suspend fun createReport(@Body request: ReportRequest): Result<Void>
}

// Data classes for requests and responses

// 后端统一响应包装类
data class Result<T>(
    val code: Int,
    val message: String?,
    val data: T?
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: UserProfile
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val nickname: String
)

data class RegisterResponse(
    val token: String,
    val user: UserProfile
)

data class UserProfile(
    val id: Long,
    val username: String,
    val nickname: String,
    val avatarUrl: String?,
    val schoolVerified: Boolean,
    val campus: String?
)

data class UpdateProfileRequest(
    val nickname: String?,
    val avatarUrl: String?
)

data class CreatePostRequest(
    val content: String,
    val mediaUrls: String?,
    val type: String, // "normal" or "question"
    val visibility: String, // "public" or circle ID
    val circleId: Long?
)

data class Post(
    val id: Long,
    val userId: Long,
    val username: String? = null,
    val content: String,
    val mediaUrls: String?,
    val type: String,
    val visibility: String,
    val circleId: Long? = null,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val createTime: String?,
    val updateTime: String? = null,
    val deleted: Int = 0
)

data class User(
    val id: Long,
    val username: String,
    val nickname: String,
    val email: String?,
    val avatarUrl: String?,
    val schoolVerified: Boolean,
    val campus: String?,
    val createTime: String?,
    val updateTime: String?
)

data class Circle(
    val id: String,
    val name: String,
    val description: String,
    val creatorId: String,
    val type: String,
    val membersCount: Int,
    val joined: Boolean
)

data class CreateCircleRequest(
    val name: String,
    val description: String,
    val type: String
)

// File Upload Response
data class UploadResponse(
    val url: String
)

data class PresignedUrlResponse(
    val uploadUrl: String
)

// Message Data Classes
data class Conversation(
    val userId: Long,
    val username: String,
    val nickname: String,
    val avatarUrl: String?,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int
)

data class ChatMessage(
    val id: Long,
    val senderId: Long,
    val receiverId: Long,
    val content: String,
    val type: String,
    val mediaUrls: String?,
    val isRead: Boolean,
    val createTime: String,
    val senderName: String,
    val senderAvatar: String?
)

data class SendMessageRequest(
    val receiverId: Long,
    val content: String,
    val type: String = "text",
    val mediaUrls: String? = null
)

// Comment Data Classes
data class Comment(
    val id: Long,
    val postId: Long,
    val userId: Long,
    val username: String,
    val content: String,
    val parentCommentId: Long?,
    val createTime: String,
    val replies: List<Comment>? = null,
    val replyCount: Int = 0
)

data class AddCommentRequest(
    val content: String
)

// User Stats
data class UserStats(
    val postsCount: Int,
    val followersCount: Int,
    val followingCount: Int
)

// Notifications
data class Notification(
    val id: Long,
    val userId: Long,
    val type: String, // like, comment, follow, system
    val content: String,
    val relatedId: Long?,
    val senderId: Long?,
    val isRead: Boolean,
    val createTime: String
)

// Reports
data class ReportRequest(
    val reportedType: String,  // post, comment, user
    val reportedId: Long,
    val reason: String,
    val description: String? = null
)

# Android前端模块详细说明

## 📱 技术栈概览

- **开发语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构模式**: MVVM (Model-View-ViewModel)
- **依赖注入**: Koin
- **网络请求**: Retrofit 2 + OkHttp
- **图片加载**: Coil
- **导航**: Navigation Compose
- **异步处理**: Kotlin Coroutines + Flow

---

## 🏗️ 项目架构

### MVVM架构说明

```
┌─────────────────────────────────────────┐
│            View (Composable)            │
│  - UI渲染                                │
│  - 用户交互                              │
│  - 状态观察                              │
└──────────────┬──────────────────────────┘
               │ 观察State
               │ 触发Action
┌──────────────▼──────────────────────────┐
│          ViewModel (可选)                │
│  - 状态管理 (State)                      │
│  - 业务逻辑                              │
│  - 数据转换                              │
└──────────────┬──────────────────────────┘
               │ 调用Repository
               │ 获取数据
┌──────────────▼──────────────────────────┐
│         Repository (可选)                │
│  - 数据源协调                            │
│  - 缓存策略                              │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          Data Layer                     │
│  - ApiService (网络请求)                 │
│  - WebSocket (实时通信)                  │
│  - SharedPreferences (本地存储)          │
└─────────────────────────────────────────┘
```

**注**: 本项目采用简化的MVVM架构，部分页面直接在Composable中处理业务逻辑，适合中小型项目快速开发。

---

## 📂 模块结构

### 1. 数据层 (Data Layer)

#### 1.1 ApiService.kt
**位置**: `data/api/ApiService.kt`

**功能**: 定义所有后端API接口

**核心代码**:
```kotlin
interface ApiService {
    companion object {
        const val BASE_URL = "http://10.0.2.2:8081/"
    }
    
    // 认证接口
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Result<LoginResponse>
    
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Result<RegisterResponse>
    
    // 用户接口
    @GET("api/user/profile")
    suspend fun getUserProfile(): UserProfile
    
    // 动态接口
    @GET("api/posts")
    suspend fun getPosts(): Result<List<Post>>
    
    @POST("api/posts")
    suspend fun createPost(@Body request: CreatePostRequest): Post
    
    @POST("api/posts/{id}/like")
    suspend fun likePost(@Path("id") postId: Long): Result<Void>
    
    // ... 更多接口
}
```

**数据模型**:
```kotlin
// 统一响应包装
data class Result<T>(
    val code: Int,
    val message: String?,
    val data: T?
)

// 用户模型
data class User(
    val id: Long,
    val username: String,
    val nickname: String,
    val email: String?,
    val avatarUrl: String?,
    val schoolVerified: Boolean,
    val campus: String?
)

// 动态模型
data class Post(
    val id: Long,
    val userId: Long,
    val username: String?,
    val content: String,
    val mediaUrls: String?,
    val type: String,
    val likesCount: Int,
    val commentsCount: Int,
    val createTime: String?
)

// 评论模型
data class Comment(
    val id: Long,
    val postId: Long,
    val userId: Long,
    val username: String,
    val content: String,
    val parentCommentId: Long?,
    val createTime: String,
    val replies: List<Comment>?
)
```

#### 1.2 ChatWebSocket.kt
**位置**: `data/websocket/ChatWebSocket.kt`

**功能**: WebSocket实时通信客户端

**实现原理**:
- 使用OkHttp的WebSocket实现
- 自动重连机制
- 消息队列管理
- 心跳保活

---

### 2. 依赖注入 (DI Layer)

#### 2.1 AppModule.kt
**位置**: `di/AppModule.kt`

**功能**: Koin依赖注入配置

**核心代码**:
```kotlin
val appModule = module {
    // Retrofit
    single {
        Retrofit.Builder()
            .baseUrl(ApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val token = get<Context>()
                            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                            .getString("auth_token", null)
                        
                        val request = if (token != null) {
                            chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer $token")
                                .build()
                        } else {
                            chain.request()
                        }
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()
    }
    
    // ApiService
    single { get<Retrofit>().create(ApiService::class.java) }
}
```

**依赖项**:
- Retrofit实例（单例）
- ApiService实例（单例）
- 自动注入Token到请求头

---

### 3. UI层 (UI Layer)

### 3.1 认证模块 (Auth Module)

#### LoginScreen.kt
**位置**: `ui/screens/auth/LoginScreen.kt`

**功能**: 用户登录页面

**UI组件**:
- 用户名输入框
- 密码输入框（支持显示/隐藏）
- 登录按钮
- 注册跳转链接
- 错误提示

**核心实现**:
```kotlin
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Image(...)
        
        // 用户名输入
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Default.Person, null) }
        )
        
        // 密码输入
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) 
                VisualTransformation.None 
            else 
                PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility 
                        else Icons.Default.VisibilityOff,
                        null
                    )
                }
            }
        )
        
        // 登录按钮
        Button(
            onClick = {
                performLogin(
                    context, apiService, username, password,
                    onLoading = { isLoading = it },
                    onError = { errorMessage = it },
                    onSuccess = onLoginSuccess
                )
            },
            enabled = !isLoading && username.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Login")
            }
        }
    }
}

// 登录逻辑
suspend fun performLogin(
    context: Context,
    apiService: ApiService,
    username: String,
    password: String,
    onLoading: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onSuccess: () -> Unit
) {
    onLoading(true)
    try {
        val response = apiService.login(LoginRequest(username, password))
        if (response.code == 200 && response.data != null) {
            // 保存Token
            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("auth_token", response.data.token)
                .putLong("user_id", response.data.user.id)
                .apply()
            
            onSuccess()
        } else {
            onError(response.message ?: "Login failed")
        }
    } catch (e: Exception) {
        onError(e.message ?: "Network error")
    } finally {
        onLoading(false)
    }
}
```

**特点**:
- 表单验证
- 加载状态显示
- 错误处理
- Token持久化

#### RegisterScreen.kt
**位置**: `ui/screens/auth/RegisterScreen.kt`

**功能**: 用户注册页面

**UI组件**:
- 用户名输入框
- 邮箱输入框
- 昵称输入框
- 密码输入框
- 确认密码输入框
- 注册按钮

**实现逻辑**:
- 表单验证（邮箱格式、密码强度、密码一致性）
- 注册成功后自动登录
- 错误提示

---

### 3.2 动态流模块 (Feed Module)

#### FeedScreen.kt
**位置**: `ui/screens/feed/FeedScreen.kt`

**功能**: 主页动态流

**核心组件**:

1. **动态列表**
```kotlin
@Composable
fun FeedScreen(navController: NavHostController) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var likedPosts by remember { mutableStateOf<Set<Long>>(emptySet()) }
    
    // 加载动态
    LaunchedEffect(Unit) {
        loadPosts(context, apiService,
            onLoading = { isLoading = it },
            onSuccess = { posts = it },
            onError = { /* 处理错误 */ }
        )
    }
    
    LazyColumn {
        items(posts) { post ->
            PostCard(
                post = post,
                isLiked = likedPosts.contains(post.id),
                likesCount = post.likesCount,
                onLike = {
                    scope.launch {
                        toggleLike(context, apiService, post, 
                            likedPosts.contains(post.id),
                            onSuccess = { /* 更新状态 */ },
                            onError = { /* 处理错误 */ }
                        )
                    }
                },
                onComment = { /* 打开评论 */ },
                onShare = { sharePost(context, post) },
                onClick = { navController.navigate("post_detail/${post.id}") }
            )
        }
    }
}
```

2. **动态卡片**
```kotlin
@Composable
fun PostCard(
    post: Post,
    isLiked: Boolean,
    likesCount: Int,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 用户信息
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = post.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(post.username ?: "Unknown", fontWeight = FontWeight.Bold)
                    Text(formatTime(post.createTime), style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 动态内容
            Text(post.content)
            
            // 图片/视频
            if (!post.mediaUrls.isNullOrEmpty()) {
                PostImages(mediaUrls = post.mediaUrls)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 互动按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                InteractionButton(
                    icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    text = "$likesCount",
                    onClick = onLike,
                    tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
                InteractionButton(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    text = "${post.commentsCount}",
                    onClick = onComment
                )
                InteractionButton(
                    icon = Icons.Outlined.Share,
                    text = "${post.sharesCount}",
                    onClick = onShare
                )
            }
        }
    }
}
```

3. **图片展示**
```kotlin
@Composable
fun PostImages(mediaUrls: String) {
    val urls = try {
        Gson().fromJson<List<String>>(mediaUrls, object : TypeToken<List<String>>() {}.type)
    } catch (e: Exception) {
        emptyList()
    }
    
    when (urls.size) {
        1 -> {
            // 单图 - 大图展示
            AsyncImage(
                model = urls[0],
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
        2, 3 -> {
            // 2-3张 - 横向排列
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                urls.forEach { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        else -> {
            // 4张及以上 - 网格布局
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(urls.take(9)) { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
```

**功能特点**:
- 下拉刷新
- 上拉加载更多
- 点赞/取消点赞
- 评论功能
- 分享功能
- 图片预览
- 视频播放
- 举报功能

---

### 3.3 动态发布模块 (Post Module)

#### CreatePostScreen.kt
**位置**: `ui/screens/post/CreatePostScreen.kt`

**功能**: 创建/编辑动态

**核心实现**:
```kotlin
@Composable
fun CreatePostScreen(
    navController: NavHostController,
    postId: Long? = null  // 编辑模式
) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    
    var content by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedVideos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = uris
    }
    
    // 视频选择器
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedVideos = listOf(it) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (postId == null) "Create Post" else "Edit Post") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                // 1. 上传图片
                                val imageUrls = if (selectedImages.isNotEmpty()) {
                                    uploadImages(context, apiService, selectedImages,
                                        onUploading = { isUploading = it },
                                        onSuccess = { it },
                                        onError = { /* 处理错误 */ }
                                    )
                                } else emptyList()
                                
                                // 2. 上传视频
                                val videoUrls = if (selectedVideos.isNotEmpty()) {
                                    uploadVideos(context, apiService, selectedVideos,
                                        onUploading = { isUploading = it },
                                        onSuccess = { it },
                                        onError = { /* 处理错误 */ }
                                    )
                                } else emptyList()
                                
                                // 3. 发布动态
                                publishPost(context, apiService, content, imageUrls, videoUrls, postId,
                                    onLoading = { isUploading = it },
                                    onSuccess = { navController.popBackStack() },
                                    onError = { /* 处理错误 */ }
                                )
                            }
                        },
                        enabled = !isUploading && content.isNotBlank()
                    ) {
                        Text("Publish")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 内容输入框
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("What's on your mind?") },
                maxLines = 10
            )
            
            // 已选择的图片预览
            if (selectedImages.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedImages) { uri ->
                        Box {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            IconButton(
                                onClick = { selectedImages = selectedImages - uri },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White)
                            }
                        }
                    }
                }
            }
            
            // 工具栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Icon(Icons.Default.Image, "Add Image")
                }
                IconButton(onClick = { videoPickerLauncher.launch("video/*") }) {
                    Icon(Icons.Default.VideoLibrary, "Add Video")
                }
            }
        }
    }
}
```

**功能特点**:
- 多图上传（最多9张）
- 视频上传
- 图片预览
- 上传进度显示
- 草稿保存（待实现）

---

### 3.4 个人中心模块 (Profile Module)

#### ProfileScreen.kt
**位置**: `ui/screens/profile/ProfileScreen.kt`

**功能**: 个人信息展示

**核心实现**:
```kotlin
@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    
    var userInfo by remember { mutableStateOf<User?>(null) }
    var stats by remember { mutableStateOf<UserStats?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    // 加载用户信息
    fun loadUserInfo() {
        scope.launch {
            isLoading = true
            try {
                val userId = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    .getLong("user_id", 0)
                
                val userResponse = apiService.getUserById(userId)
                if (userResponse.code == 200) {
                    userInfo = userResponse.data
                }
                
                val statsResponse = apiService.getUserStats(userId)
                if (statsResponse.code == 200) {
                    stats = statsResponse.data
                }
            } catch (e: Exception) {
                // 处理错误
            } finally {
                isLoading = false
            }
        }
    }
    
    LaunchedEffect(Unit) {
        loadUserInfo()
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            // 用户信息卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 头像
                    AsyncImage(
                        model = userInfo?.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 昵称
                    Text(
                        text = userInfo?.nickname ?: "Loading...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // 用户名
                    Text(
                        text = "@${userInfo?.username ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 统计信息
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "Posts",
                            value = "${stats?.postsCount ?: 0}",
                            onClick = { /* 跳转到我的动态 */ }
                        )
                        StatItem(
                            label = "Following",
                            value = "${stats?.followingCount ?: 0}",
                            onClick = { navController.navigate("follow_list/following") }
                        )
                        StatItem(
                            label = "Followers",
                            value = "${stats?.followersCount ?: 0}",
                            onClick = { navController.navigate("follow_list/followers") }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 编辑资料按钮
                    Button(
                        onClick = { navController.navigate("edit_profile") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit Profile")
                    }
                }
            }
        }
        
        item {
            // 功能菜单
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    MenuItemRow(
                        icon = Icons.Default.Favorite,
                        title = "My Favorites",
                        onClick = { navController.navigate("my_favorites") }
                    )
                    Divider()
                    MenuItemRow(
                        icon = Icons.Default.History,
                        title = "Browse History",
                        onClick = { navController.navigate("browse_history") }
                    )
                    Divider()
                    MenuItemRow(
                        icon = Icons.Default.Settings,
                        title = "Settings",
                        onClick = { navController.navigate("settings") }
                    )
                    Divider()
                    MenuItemRow(
                        icon = Icons.Default.Info,
                        title = "About",
                        onClick = { navController.navigate("about") }
                    )
                }
            }
        }
    }
}
```

**子页面**:
1. **EditProfileScreen**: 编辑个人资料
2. **MyFavoritesScreen**: 我的收藏
3. **BrowseHistoryScreen**: 浏览历史
4. **FollowListScreen**: 关注/粉丝列表
5. **SettingsScreen**: 设置
6. **LanguageSettingsScreen**: 语言设置
7. **AboutScreen**: 关于

---

### 3.5 聊天模块 (Chat Module)

#### ChatListScreen.kt
**位置**: `ui/screens/chat/ChatListScreen.kt`

**功能**: 聊天列表 + 聊天详情

**核心实现**:

1. **会话列表**
```kotlin
@Composable
fun ChatListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    
    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        loadConversations(context, apiService,
            onLoading = { isLoading = it },
            onSuccess = { conversations = it },
            onError = { /* 处理错误 */ }
        )
    }
    
    LazyColumn {
        items(conversations) { conversation ->
            ConversationItem(
                conversation = conversation,
                onClick = {
                    navController.navigate("chat_detail/${conversation.userId}")
                }
            )
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(conversation.nickname) },
        supportingContent = { Text(conversation.lastMessage) },
        leadingContent = {
            Box {
                AsyncImage(
                    model = conversation.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                if (conversation.unreadCount > 0) {
                    Badge(
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text("${conversation.unreadCount}")
                    }
                }
            }
        },
        trailingContent = {
            Text(
                text = formatTime(conversation.lastMessageTime),
                style = MaterialTheme.typography.bodySmall
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
```

2. **聊天详情**
```kotlin
@Composable
fun ChatDetailScreen(
    otherUserId: Long,
    navController: NavHostController
) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var messageInput by remember { mutableStateOf("") }
    val currentUserId = getCurrentUserId(context)
    
    // 加载聊天记录
    LaunchedEffect(otherUserId) {
        val response = apiService.getChatHistory(otherUserId)
        if (response.code == 200) {
            messages = response.data ?: emptyList()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }
                )
                IconButton(
                    onClick = {
                        scope.launch {
                            val response = apiService.sendMessage(
                                SendMessageRequest(
                                    receiverId = otherUserId,
                                    content = messageInput,
                                    type = "text"
                                )
                            )
                            if (response.code == 200) {
                                messageInput = ""
                                // 刷新消息列表
                            }
                        }
                    },
                    enabled = messageInput.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, "Send")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                ChatMessageItem(
                    message = message,
                    currentUserId = currentUserId
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    currentUserId: Long
) {
    val isMe = message.senderId == currentUserId
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            AsyncImage(
                model = message.senderAvatar,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                when (message.type) {
                    "text" -> Text(message.content)
                    "image" -> {
                        AsyncImage(
                            model = message.mediaUrls,
                            contentDescription = null,
                            modifier = Modifier
                                .widthIn(max = 200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                    "video" -> {
                        VideoPlayer(videoUrl = message.mediaUrls ?: "")
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatMessageTime(message.createTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (isMe) {
            Spacer(modifier = Modifier.width(8.dp))
            AsyncImage(
                model = message.senderAvatar,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
        }
    }
}
```

**功能特点**:
- 实时消息推送（WebSocket）
- 消息历史记录
- 未读消息提醒
- 图片/视频消息
- 消息状态（已读/未读）

---

### 3.6 发现模块 (Discovery Module)

#### DiscoveryScreen.kt
**位置**: `ui/screens/discovery/DiscoveryScreen.kt`

**功能**: 内容发现与搜索

**核心实现**:
```kotlin
@Composable
fun DiscoveryScreen(navController: NavHostController) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    
    var searchQuery by remember { mutableStateOf("") }
    var hotPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var filteredPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) }
    
    val tabs = listOf("Hot", "Latest", "Following")
    
    // 搜索逻辑
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            filteredPosts = hotPosts.filter { post ->
                post.content.contains(searchQuery, ignoreCase = true) ||
                (post.username?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }
    
    Column {
        // 搜索框
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search users, posts, topics...") },
            leadingIcon = { Icon(Icons.Default.Search, null) }
        )
        
        // 分类标签
        ScrollableTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // 内容列表
        LazyColumn {
            items(if (searchQuery.isEmpty()) hotPosts else filteredPosts) { post ->
                HotPostCard(
                    post = post,
                    onClick = { navController.navigate("post_detail/${post.id}") }
                )
            }
        }
    }
}
```

**功能特点**:
- 搜索功能（用户、动态、话题）
- 热门推荐
- 最新动态
- 关注动态
- 分类浏览

---

### 3.7 圈子模块 (Circle Module)

#### CircleListScreen.kt
**位置**: `ui/screens/circle/CircleListScreen.kt`

**功能**: 圈子列表与详情

**核心实现**:
```kotlin
@Composable
fun CircleListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    
    var circles by remember { mutableStateOf<List<Circle>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        loadCircles(context, apiService,
            onLoading = { isLoading = it },
            onSuccess = { circles = it },
            onError = { /* 处理错误 */ }
        )
    }
    
    LazyColumn {
        items(circles) { circle ->
            CircleCard(
                circle = circle,
                onClick = { navController.navigate("circle_detail/${circle.id}") }
            )
        }
    }
}

@Composable
fun CircleCard(
    circle: Circle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = circle.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = circle.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${circle.membersCount} members")
                if (circle.joined) {
                    Text("Joined", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
```

---

### 3.8 通知模块 (Notification Module)

#### NotificationScreen.kt
**位置**: `ui/screens/notification/NotificationScreen.kt`

**功能**: 系统通知列表

**核心实现**:
```kotlin
@Composable
fun NotificationScreen(navController: NavHostController) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val response = apiService.getNotifications()
            if (response.code == 200) {
                notifications = response.data ?: emptyList()
            }
        } catch (e: Exception) {
            // 处理错误
        } finally {
            isLoading = false
        }
    }
    
    LazyColumn {
        items(notifications) { notification ->
            NotificationItem(
                notification = notification,
                onClick = {
                    scope.launch {
                        apiService.markNotificationAsRead(notification.id)
                        // 跳转到相关页面
                        when (notification.type) {
                            "like" -> navController.navigate("post_detail/${notification.relatedId}")
                            "comment" -> navController.navigate("post_detail/${notification.relatedId}")
                            "follow" -> navController.navigate("user_profile/${notification.senderId}")
                        }
                    }
                }
            )
        }
    }
}
```

---

### 3.9 导航模块 (Navigation)

#### NavGraph.kt
**位置**: `ui/navigation/NavGraph.kt`

**功能**: 应用导航配置

**核心实现**:
```kotlin
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 认证流程
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        // 主页面
        composable("main") {
            MainScreen(navController)
        }
        
        // 动态详情
        composable(
            route = "post_detail/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.LongType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getLong("postId") ?: 0
            PostDetailScreen(postId = postId, navController = navController)
        }
        
        // 创建动态
        composable("create_post") {
            CreatePostScreen(navController = navController)
        }
        
        // 编辑动态
        composable(
            route = "edit_post/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.LongType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getLong("postId")
            CreatePostScreen(navController = navController, postId = postId)
        }
        
        // 用户主页
        composable(
            route = "user_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0
            UserProfileScreen(userId = userId, navController = navController)
        }
        
        // 聊天详情
        composable(
            route = "chat_detail/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0
            ChatDetailScreen(otherUserId = userId, navController = navController)
        }
        
        // 个人中心子页面
        composable("edit_profile") {
            EditProfileScreen(navController = navController)
        }
        
        composable("my_favorites") {
            MyFavoritesScreen(navController = navController)
        }
        
        composable("browse_history") {
            BrowseHistoryScreen(navController = navController)
        }
        
        composable(
            route = "follow_list/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "following"
            FollowListScreen(type = type, navController = navController)
        }
        
        composable("settings") {
            SettingsScreen(navController = navController)
        }
        
        composable("language_settings") {
            LanguageSettingsScreen(navController = navController)
        }
        
        composable("about") {
            AboutScreen(navController = navController)
        }
    }
}
```

---

### 3.10 主页面 (Main Screen)

#### MainScreen.kt
**位置**: `ui/screens/main/MainScreen.kt`

**功能**: 底部导航主页面

**核心实现**:
```kotlin
@Composable
fun MainScreen(navController: NavHostController) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    
    var selectedTab by remember { mutableStateOf(0) }
    var unreadMessageCount by remember { mutableStateOf(0) }
    var unreadNotificationCount by remember { mutableStateOf(0) }
    
    // 定期获取未读数
    LaunchedEffect(Unit) {
        while (true) {
            try {
                val response = apiService.getUnreadCount()
                if (response.code == 200) {
                    unreadMessageCount = response.data?.get("messages")?.toInt() ?: 0
                    unreadNotificationCount = response.data?.get("notifications")?.toInt() ?: 0
                }
            } catch (e: Exception) {
                // 忽略错误
            }
            delay(30000) // 30秒刷新一次
        }
    }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Explore, "Discover") },
                    label = { Text("Discover") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { navController.navigate("create_post") },
                    icon = { Icon(Icons.Default.Add, "Post") },
                    label = { Text("Post") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadMessageCount > 0) {
                                    Badge { Text("$unreadMessageCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Chat, "Messages")
                        }
                    },
                    label = { Text("Messages") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationCount > 0) {
                                    Badge { Text("$unreadNotificationCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Person, "Me")
                        }
                    },
                    label = { Text("Me") }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { navController.navigate("create_post") }
                ) {
                    Icon(Icons.Default.Edit, "Create Post")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> FeedScreen(navController)
                1 -> DiscoveryScreen(navController)
                2 -> {} // 由导航处理
                3 -> ChatListScreen(navController)
                4 -> ProfileScreen(navController)
            }
        }
    }
}
```

---

## 🎨 主题系统

### Color.kt
**位置**: `ui/theme/Color.kt`

```kotlin
val md_theme_light_primary = Color(0xFF2196F3)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFBBDEFB)
val md_theme_light_onPrimaryContainer = Color(0xFF0D47A1)

val md_theme_dark_primary = Color(0xFF90CAF9)
val md_theme_dark_onPrimary = Color(0xFF0D47A1)
val md_theme_dark_primaryContainer = Color(0xFF1976D2)
val md_theme_dark_onPrimaryContainer = Color(0xFFE3F2FD)
```

### Theme.kt
**位置**: `ui/theme/Theme.kt`

```kotlin
@Composable
fun SocialAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = md_theme_dark_primary,
            onPrimary = md_theme_dark_onPrimary,
            // ... 更多颜色
        )
    } else {
        lightColorScheme(
            primary = md_theme_light_primary,
            onPrimary = md_theme_light_onPrimary,
            // ... 更多颜色
        )
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

---

## 🔧 工具类

### FileUploadHelper.kt
**位置**: `util/FileUploadHelper.kt`

**功能**: 文件上传辅助类

```kotlin
object FileUploadHelper {
    fun createImagePart(context: Context, uri: Uri): MultipartBody.Part {
        val file = uriToFile(context, uri)
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", file.name, requestBody)
    }
    
    fun createVideoPart(context: Context, uri: Uri): MultipartBody.Part {
        val file = uriToFile(context, uri)
        val requestBody = file.asRequestBody("video/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", file.name, requestBody)
    }
    
    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}")
        tempFile.outputStream().use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        return tempFile
    }
}
```

---

## 📊 状态管理策略

### 1. 本地状态
使用`remember`和`mutableStateOf`管理组件内部状态：
```kotlin
var username by remember { mutableStateOf("") }
var isLoading by remember { mutableStateOf(false) }
```

### 2. 共享状态
使用`SharedPreferences`持久化关键数据：
```kotlin
// 保存Token
context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    .edit()
    .putString("auth_token", token)
    .putLong("user_id", userId)
    .apply()

// 读取Token
val token = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    .getString("auth_token", null)
```

### 3. 副作用处理
使用`LaunchedEffect`处理副作用：
```kotlin
LaunchedEffect(Unit) {
    loadData()
}

LaunchedEffect(userId) {
    loadUserData(userId)
}
```

---

## 🔄 数据流

### 请求流程
```
User Action (点击按钮)
    ↓
Composable (触发事件)
    ↓
Coroutine Scope (启动协程)
    ↓
ApiService (发起网络请求)
    ↓
Retrofit + OkHttp (HTTP请求)
    ↓
Spring Boot Backend (处理请求)
    ↓
Response (返回数据)
    ↓
Update State (更新状态)
    ↓
Recomposition (UI重组)
```

### 错误处理
```kotlin
try {
    val response = apiService.someApi()
    if (response.code == 200) {
        // 成功处理
        onSuccess(response.data)
    } else {
        // 业务错误
        onError(response.message ?: "Unknown error")
    }
} catch (e: Exception) {
    // 网络错误
    onError(e.message ?: "Network error")
}
```

---

## 🚀 性能优化技巧

### 1. 列表优化
```kotlin
// 使用LazyColumn而不是Column + ScrollView
LazyColumn {
    items(posts) { post ->
        PostCard(post)
    }
}

// 使用key提高性能
LazyColumn {
    items(posts, key = { it.id }) { post ->
        PostCard(post)
    }
}
```

### 2. 图片加载优化
```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(imageUrl)
        .crossfade(true)
        .placeholder(R.drawable.placeholder)
        .error(R.drawable.error)
        .build(),
    contentDescription = null
)
```

### 3. 避免不必要的重组
```kotlin
// 使用derivedStateOf
val filteredList by remember {
    derivedStateOf {
        list.filter { it.matches(query) }
    }
}

// 使用stable参数
@Stable
data class Post(...)
```

---

## 📝 开发规范

### 1. 命名规范
- **文件名**: PascalCase (LoginScreen.kt)
- **函数名**: camelCase (loadUserData)
- **变量名**: camelCase (isLoading)
- **常量**: UPPER_SNAKE_CASE (BASE_URL)

### 2. 代码组织
```kotlin
@Composable
fun MyScreen() {
    // 1. Context和依赖注入
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    
    // 2. 状态变量
    var data by remember { mutableStateOf<Data?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    // 3. 副作用
    LaunchedEffect(Unit) {
        loadData()
    }
    
    // 4. UI渲染
    Scaffold { padding ->
        // UI内容
    }
}

// 5. 辅助函数
private suspend fun loadData() {
    // 实现
}
```

### 3. 注释规范
```kotlin
/**
 * 用户登录页面
 * 
 * @param onNavigateToRegister 跳转到注册页面的回调
 * @param onLoginSuccess 登录成功的回调
 */
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    // 实现
}
```

---

## 🐛 常见问题

### 1. 网络请求失败
**问题**: 无法连接到后端服务器
**解决**:
- 检查BASE_URL是否正确（模拟器使用10.0.2.2）
- 检查后端服务是否启动
- 检查网络权限是否添加

### 2. 图片加载失败
**问题**: 图片无法显示
**解决**:
- 检查图片URL是否正确
- 检查网络权限
- 添加占位图和错误图

### 3. Token过期
**问题**: 请求返回401
**解决**:
- 实现Token刷新机制
- 跳转到登录页面

---

## 📚 参考资料

- [Jetpack Compose官方文档](https://developer.android.com/jetpack/compose)
- [Kotlin协程指南](https://kotlinlang.org/docs/coroutines-guide.html)
- [Retrofit官方文档](https://square.github.io/retrofit/)
- [Material Design 3](https://m3.material.io/)
- [Coil图片加载库](https://coil-kt.github.io/coil/)

---

**最后更新**: 2024年12月8日

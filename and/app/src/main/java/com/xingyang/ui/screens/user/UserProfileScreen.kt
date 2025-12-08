package com.xingyang.ui.screens.user

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.xingyang.data.api.ApiService
import com.xingyang.data.api.Post
import com.xingyang.data.api.User
import com.xingyang.data.api.UserStats
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: Long,
    navController: NavHostController
) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    
    var user by remember { mutableStateOf<User?>(null) }
    var userStats by remember { mutableStateOf<UserStats?>(null) }
    var userPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isFollowing by remember { mutableStateOf(false) }
    var isCheckingFollow by remember { mutableStateOf(false) }
    
    // 获取当前用户ID
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val currentUserId = prefs.getString("user_id", null)?.toLongOrNull()
    val isOwnProfile = currentUserId == userId
    
    // 加载用户信息
    LaunchedEffect(userId) {
        isLoading = true
        try {
            // 加载用户信息
            val userResult = apiService.getUserById(userId)
            if (userResult.code == 200 && userResult.data != null) {
                user = userResult.data
            }
            
            // 加载统计信息
            val statsResult = apiService.getUserStats(userId)
            if (statsResult.code == 200 && statsResult.data != null) {
                userStats = statsResult.data
            }
            
            // 加载用户帖子
            val postsResult = apiService.getUserPosts(userId)
            if (postsResult.code == 200 && postsResult.data != null) {
                userPosts = postsResult.data
            }
            
            // 如果不是自己的主页，检查关注状态
            if (!isOwnProfile) {
                val followResult = apiService.checkFollowStatus(userId)
                if (followResult.code == 200 && followResult.data != null) {
                    isFollowing = followResult.data
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("UserProfile", "Load failed: ${e.message}")
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user?.username ?: "User Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        android.widget.Toast.makeText(context, "Coming soon", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.MoreVert, "More")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("User not found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 用户信息卡片
                item {
                    UserInfoCard(
                        user = user!!,
                        userStats = userStats,
                        isOwnProfile = isOwnProfile,
                        isFollowing = isFollowing,
                        onFollowClick = {
                            if (!isCheckingFollow) {
                                scope.launch {
                                    isCheckingFollow = true
                                    try {
                                        val result = if (isFollowing) {
                                            apiService.unfollowUser(userId)
                                        } else {
                                            apiService.followUser(userId)
                                        }
                                        
                                        if (result.code == 200) {
                                            isFollowing = !isFollowing
                                            android.widget.Toast.makeText(
                                                context,
                                                if (isFollowing) "Followed" else "Unfollowed",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                            
                                            // 重新加载统计信息
                                            val statsResult = apiService.getUserStats(userId)
                                            if (statsResult.code == 200 && statsResult.data != null) {
                                                userStats = statsResult.data
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Operation failed: ${e.message}",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    } finally {
                                        isCheckingFollow = false
                                    }
                                }
                            }
                        },
                        context = context,
                        onSendMessage = {
                            navController.navigate("chat/$userId")
                        }
                    )
                }
                
                // TA的动态标题
                item {
                    Text(
                        text = if (isOwnProfile) "My Posts" else "Posts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                
                // 用户帖子列表
                if (userPosts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isOwnProfile) "No posts yet" else "No posts yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(userPosts) { post ->
                        UserPostItem(
                            post = post,
                            onClick = {
                                navController.navigate("post_detail/${post.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserInfoCard(
    user: User,
    userStats: UserStats?,
    isOwnProfile: Boolean,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    context: Context,
    onSendMessage: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 头像
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.username.firstOrNull()?.toString() ?: "U",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 用户名
            Text(
                text = user.username,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            
            // 昵称
            if (user.nickname.isNotEmpty() && user.nickname != user.username) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.nickname,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 校区信息
            if (user.campus != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user.campus,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 统计信息
            if (userStats != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(label = "Posts", count = userStats.postsCount)
                    StatItem(label = "Followers", count = userStats.followersCount)
                    StatItem(label = "Following", count = userStats.followingCount)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 关注/编辑按钮
            if (isOwnProfile) {
                OutlinedButton(
                    onClick = {
                        android.widget.Toast.makeText(context, "Coming soon", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onFollowClick,
                        modifier = Modifier.weight(1f),
                        colors = if (isFollowing) {
                            ButtonDefaults.outlinedButtonColors()
                        } else {
                            ButtonDefaults.buttonColors()
                        }
                    ) {
                        Icon(
                            if (isFollowing) Icons.Default.CheckCircle else Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isFollowing) "Following" else "Follow")
                    }
                    
                    OutlinedButton(
                        onClick = onSendMessage,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Message")
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, count: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun UserPostItem(
    post: Post,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = post.content,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${post.likesCount}", fontSize = 12.sp)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ChatBubble,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${post.commentsCount}", fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = post.createTime ?: "Just now",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

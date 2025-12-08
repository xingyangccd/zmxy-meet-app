package com.xingyang.ui.screens.notification

import android.content.Context
import androidx.compose.foundation.clickable
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
import com.xingyang.data.api.Notification
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

// 将中文通知内容翻译为英文
private fun translateNotificationContent(content: String): String {
    return when {
        content.contains("关注了你") -> content.replace("关注了你", "followed you")
        content.contains("点赞了你的帖子") -> content.replace("点赞了你的帖子", "liked your post")
        content.contains("点赞了你的动态") -> content.replace("点赞了你的动态", "liked your post")
        content.contains("评论了你的帖子") -> content.replace("评论了你的帖子", "commented on your post")
        content.contains("评论了你的动态") -> content.replace("评论了你的动态", "commented on your post")
        content.contains("回复了你的评论") -> content.replace("回复了你的评论", "replied to your comment")
        content.contains("提到了你") -> content.replace("提到了你", "mentioned you")
        content.contains("分享了你的帖子") -> content.replace("分享了你的帖子", "shared your post")
        content.contains("有人") -> content.replace("有人", "Someone")
        else -> content
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavHostController) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Load notification list
    LaunchedEffect(Unit) {
        loadNotifications(
            context = context,
            apiService = apiService,
            onLoading = { isLoading = it },
            onSuccess = { notifications = it },
            onError = { errorMessage = it }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (notifications.any { !it.isRead }) {
                        TextButton(onClick = {
                            scope.launch {
                                try {
                                    apiService.markAllNotificationsAsRead()
                                    // Reload
                                    loadNotifications(context, apiService, 
                                        onLoading = { isLoading = it },
                                        onSuccess = { notifications = it },
                                        onError = { errorMessage = it }
                                    )
                                } catch (e: Exception) {
                                    android.util.Log.e("NotificationScreen", "Mark all as read failed", e)
                                }
                            }
                        }) {
                            Text("Mark all read")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage ?: "Load failed",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            scope.launch {
                                loadNotifications(context, apiService,
                                    onLoading = { isLoading = it },
                                    onSuccess = { notifications = it },
                                    onError = { errorMessage = it }
                                )
                            }
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
            notifications.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No notifications",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(notifications) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = {
                                scope.launch {
                                    try {
                                        if (!notification.isRead) {
                                            apiService.markNotificationAsRead(notification.id)
                                            // Update local state
                                            notifications = notifications.map {
                                                if (it.id == notification.id) it.copy(isRead = true) else it
                                            }
                                        }
                                        
                                        // Navigate based on notification type
                                        when (notification.type) {
                                            "like", "comment" -> {
                                                notification.relatedId?.let { postId ->
                                                    navController.navigate("post_detail/$postId")
                                                }
                                            }
                                            "follow" -> {
                                                notification.senderId?.let { userId ->
                                                    navController.navigate("user_profile/$userId")
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("NotificationScreen", "Mark as read failed", e)
                                    }
                                }
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = if (!notification.isRead) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when (notification.type) {
                        "like" -> Icons.Default.Favorite
                        "comment" -> Icons.Default.Comment
                        "follow" -> Icons.Default.PersonAdd
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = if (!notification.isRead)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = translateNotificationContent(notification.content),
                fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTime(notification.createTime),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Unread indicator
        if (!notification.isRead) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(8.dp)
            ) {}
        }
    }
}

private suspend fun loadNotifications(
    context: Context,
    apiService: ApiService,
    onLoading: (Boolean) -> Unit,
    onSuccess: (List<Notification>) -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)
    try {
        android.util.Log.d("NotificationScreen", "Starting to load notification list")
        val result = apiService.getNotifications()
        android.util.Log.d("NotificationScreen", "Response: code=${result.code}, data size=${result.data?.size}")
        
        if (result.code == 200 && result.data != null) {
            android.util.Log.d("NotificationScreen", "Loaded ${result.data.size} notifications")
            onSuccess(result.data)
        } else {
            android.util.Log.e("NotificationScreen", "Load failed: ${result.message}")
            onError(result.message ?: "Load failed")
        }
    } catch (e: Exception) {
        android.util.Log.e("NotificationScreen", "Load exception", e)
        onError(e.message ?: "Network error")
    } finally {
        onLoading(false)
    }
}

private fun formatTime(time: String): String {
    // TODO: Implement better time formatting
    return time.take(16).replace("T", " ")
}

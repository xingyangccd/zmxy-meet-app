package com.xingyang.ui.screens.chat

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.xingyang.data.api.ApiService
import com.xingyang.data.api.Conversation
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()

    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load conversation list
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val currentUserId = prefs.getString("user_id", null)
        android.util.Log.d("ChatListScreen", "Current user ID: $currentUserId")
        
        loadConversations(
            context = context,
            apiService = apiService,
            onLoading = { isLoading = it },
            onSuccess = { conversations = it },
            onError = { errorMessage = it }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage ?: "Load failed",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            scope.launch {
                                loadConversations(context, apiService,
                                    onLoading = { isLoading = it },
                                    onSuccess = { conversations = it },
                                    onError = { errorMessage = it }
                                )
                            }
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
            conversations.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No messages",
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
                    items(conversations) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            onClick = {
                                navController.navigate("chat/${conversation.userId}")
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
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Message content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.nickname.ifEmpty { conversation.username },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formatTime(conversation.lastMessageTime),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.lastMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (conversation.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(),
                                color = MaterialTheme.colorScheme.onError,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun loadConversations(
    context: Context,
    apiService: ApiService,
    onLoading: (Boolean) -> Unit,
    onSuccess: (List<Conversation>) -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)
    try {
        android.util.Log.d("ChatListScreen", "Starting to load conversation list")
        val result = apiService.getConversations()
        android.util.Log.d("ChatListScreen", "Response: code=${result.code}, message=${result.message}, data=${result.data}")
        
        if (result.code == 200) {
            if (result.data != null && result.data.isNotEmpty()) {
                android.util.Log.d("ChatListScreen", "Loaded ${result.data.size} conversations")
                result.data.forEach { conv ->
                    android.util.Log.d("ChatListScreen", "Conversation: userId=${conv.userId}, username=${conv.username}, unread=${conv.unreadCount}")
                }
                onSuccess(result.data)
            } else {
                android.util.Log.d("ChatListScreen", "Conversation list is empty")
                onSuccess(emptyList())
            }
        } else {
            android.util.Log.e("ChatListScreen", "Load failed: ${result.message}")
            onError(result.message ?: "Load failed")
        }
    } catch (e: Exception) {
        android.util.Log.e("ChatListScreen", "Load exception", e)
        onError(e.message ?: "Network error")
    } finally {
        onLoading(false)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(otherUserId: Long, navController: NavHostController) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    var messages by remember { mutableStateOf<List<com.xingyang.data.api.ChatMessage>>(emptyList()) }
    var messageInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    var otherUserName by remember { mutableStateOf("Chat") }
    
    // Load chat history
    LaunchedEffect(otherUserId) {
        isLoading = true
        try {
            // Get other user info
            val userResult = apiService.getUserById(otherUserId)
            if (userResult.code == 200 && userResult.data != null) {
                otherUserName = userResult.data.nickname
            }
            
            // Load chat history
            val historyResult = apiService.getChatHistory(otherUserId)
            if (historyResult.code == 200 && historyResult.data != null) {
                messages = historyResult.data
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatDetail", "Load failed", e)
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(otherUserName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    ChatMessageItem(message = message, currentUserId = getCurrentUserId(context))
                }
            }
            
            // 自动滚动到最新消息
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }
            
            if (isLoading && messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Input box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    maxLines = 4
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        if (messageInput.isNotBlank() && !isSending) {
                            scope.launch {
                                isSending = true
                                try {
                                    val result = apiService.sendMessage(
                                        com.xingyang.data.api.SendMessageRequest(
                                            receiverId = otherUserId,
                                            content = messageInput
                                        )
                                    )
                                    if (result.code == 200 && result.data != null) {
                                        messages = messages + result.data
                                        messageInput = ""
                                    } else {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Send failed: ${result.message}",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Send failed: ${e.message}",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } finally {
                                    isSending = false
                                }
                            }
                        }
                    },
                    enabled = messageInput.isNotBlank() && !isSending,
                    modifier = Modifier.height(56.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Send")
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: com.xingyang.data.api.ChatMessage, currentUserId: Long) {
    val isMe = message.senderId == currentUserId
    var showMediaPreview by remember { mutableStateOf(false) }
    var previewMediaUrl by remember { mutableStateOf<String?>(null) }
    var previewMediaType by remember { mutableStateOf<String?>(null) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // 根据消息类型显示不同内容
                when (message.type) {
                    "image" -> {
                        // 显示图片
                        message.mediaUrls?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = "Image",
                                modifier = Modifier
                                    .heightIn(max = 200.dp)
                                    .widthIn(max = 250.dp)
                                    .clickable {
                                        previewMediaUrl = url
                                        previewMediaType = "image"
                                        showMediaPreview = true
                                    }
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                        if (message.content.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message.content,
                                fontSize = 15.sp
                            )
                        }
                    }
                    "video" -> {
                        // 显示视频缩略图
                        message.mediaUrls?.let { url ->
                            Box(
                                modifier = Modifier
                                    .heightIn(max = 200.dp)
                                    .widthIn(max = 250.dp)
                                    .clickable {
                                        previewMediaUrl = url
                                        previewMediaType = "video"
                                        showMediaPreview = true
                                    }
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Video",
                                    modifier = Modifier.matchParentSize(),
                                    contentScale = ContentScale.Fit
                                )
                                // 播放按钮图标
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(48.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        if (message.content.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message.content,
                                fontSize = 15.sp
                            )
                        }
                    }
                    else -> {
                        // 默认显示文本
                        Text(
                            text = message.content,
                            fontSize = 15.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatMessageTime(message.createTime),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    // 媒体预览对话框
    if (showMediaPreview && previewMediaUrl != null) {
        MediaPreviewDialog(
            mediaUrl = previewMediaUrl!!,
            mediaType = previewMediaType ?: "image",
            onDismiss = { showMediaPreview = false }
        )
    }
}

private fun getCurrentUserId(context: Context): Long {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return prefs.getString("user_id", null)?.toLongOrNull() ?: 0L
}

private fun formatMessageTime(time: String): String {
    // TODO: Implement better time formatting
    return time.takeLast(8).take(5) // Display HH:MM
}

private fun formatTime(time: String): String {
    // TODO: Implement time formatting (just now, minutes ago, hours ago, etc.)
    return time.take(10) // Simple handling, only show date part
}

@Composable
fun MediaPreviewDialog(
    mediaUrl: String,
    mediaType: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() }
        ) {
            // 显示媒体内容
            when (mediaType) {
                "image" -> {
                    AsyncImage(
                        model = mediaUrl,
                        contentDescription = "Preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                "video" -> {
                    // 视频预览（简化版，实际应用中可能需要使用 ExoPlayer）
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = mediaUrl,
                            contentDescription = "Video Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            // 关闭按钮
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.padding(8.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

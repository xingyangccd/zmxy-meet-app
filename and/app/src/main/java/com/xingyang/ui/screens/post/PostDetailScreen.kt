package com.xingyang.ui.screens.post

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xingyang.data.api.ApiService
import com.xingyang.data.api.Comment
import com.xingyang.data.api.Post
import com.xingyang.data.api.AddCommentRequest
import com.xingyang.ui.components.VideoPlayer
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: Long,
    navController: NavHostController
) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    
    var post by remember { mutableStateOf<Post?>(null) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var replyToComment by remember { mutableStateOf<Comment?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }
    
    // Load post and comments
    LaunchedEffect(postId) {
        isLoading = true
        try {
            val postResult = apiService.getPostDetail(postId)
            if (postResult.code == 200 && postResult.data != null) {
                post = postResult.data
            }
            
            val commentsResult = apiService.getPostComments(postId)
            if (commentsResult.code == 200 && commentsResult.data != null) {
                comments = commentsResult.data
            }
        } catch (e: Exception) {
            android.util.Log.e("PostDetail", "Load failed: ${e.message}")
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            // Comment input box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (replyToComment != null) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reply to @${replyToComment!!.username}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Write your reply...") },
                                maxLines = 3,
                                enabled = !isSending
                            )
                        }
                        IconButton(onClick = { 
                            replyToComment = null
                            commentText = ""
                        }) {
                            Icon(Icons.Default.Close, "Cancel reply")
                        }
                    } else {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Write your comment...") },
                            maxLines = 3,
                            enabled = !isSending
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank() && !isSending) {
                                scope.launch {
                                    isSending = true
                                    try {
                                        val request = AddCommentRequest(commentText)
                                        val result = if (replyToComment != null) {
                                            apiService.replyToComment(replyToComment!!.id, request)
                                        } else {
                                            apiService.addComment(postId, request)
                                        }
                                        
                                        if (result.code == 200) {
                                            // Reload post info (update comment count)
                                            val postResult = apiService.getPostDetail(postId)
                                            if (postResult.code == 200 && postResult.data != null) {
                                                post = postResult.data
                                            }
                                            
                                            // Reload comments
                                            val commentsResult = apiService.getPostComments(postId)
                                            if (commentsResult.code == 200 && commentsResult.data != null) {
                                                comments = commentsResult.data
                                            }
                                            commentText = ""
                                            replyToComment = null
                                            android.widget.Toast.makeText(context, "Comment posted successfully", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Comment failed: ${result.message}", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Comment failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isSending = false
                                    }
                                }
                            }
                        },
                        enabled = commentText.isNotBlank() && !isSending
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Send, "Send")
                        }
                    }
                }
            }
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
        } else if (post == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Post not found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Post content
                item {
                    PostDetailContent(
                        post = post!!,
                        onUserClick = { userId ->
                            navController.navigate("user_profile/$userId")
                        },
                        context = context
                    )
                }
                
                // Comment header
                item {
                    Text(
                        text = "Comments (${comments.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                
                // Comment list
                items(comments) { comment ->
                    CommentItem(
                        comment = comment,
                        onReply = { replyToComment = it },
                        onDelete = { commentToDelete = it; showDeleteDialog = true },
                        onUserClick = { userId ->
                            navController.navigate("user_profile/$userId")
                        },
                        context = context
                    )
                }
                
                if (comments.isEmpty()) {
                    item {
                        Text(
                            text = "No comments yet, be the first to comment~",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    }
                }
            }
        }
    }
    
    // 删除评论确认对话框
    if (showDeleteDialog && commentToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Comment") },
            text = { Text("Are you sure you want to delete this comment?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                val result = apiService.deleteComment(commentToDelete!!.id)
                                if (result.code == 200) {
                                    // 重新加载评论列表
                                    val commentsResult = apiService.getPostComments(postId)
                                    if (commentsResult.code == 200 && commentsResult.data != null) {
                                        comments = commentsResult.data
                                    }
                                    // 重新加载帖子信息（更新评论数）
                                    val postResult = apiService.getPostDetail(postId)
                                    if (postResult.code == 200 && postResult.data != null) {
                                        post = postResult.data
                                    }
                                    android.widget.Toast.makeText(context, "Comment deleted", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Delete failed: ${result.message}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Delete failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PostDetailContent(
    post: Post,
    onUserClick: (Long) -> Unit,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // User info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUserClick(post.userId) }
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = post.username?.firstOrNull()?.toString() ?: "U",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.username ?: "Unknown user",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        
                        // Check if it's user's own post
                        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        val currentUserId = prefs.getString("user_id", null)?.toLongOrNull()
                        if (currentUserId == post.userId) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "My Post",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = post.createTime ?: "Just now",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content
            Text(
                text = post.content,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            
            // Display images
            if (!post.mediaUrls.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                PostImages(mediaUrls = post.mediaUrls)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Statistics
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${post.likesCount}")
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ChatBubble,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${post.commentsCount}")
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${post.sharesCount}")
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    onReply: (Comment) -> Unit,
    onDelete: (Comment) -> Unit,
    onUserClick: (Long) -> Unit,
    context: Context,
    isReply: Boolean = false,
    parentComment: Comment? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val currentUserId = prefs.getString("user_id", null)?.toLongOrNull()
    val isOwnComment = currentUserId == comment.userId
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (isReply) 40.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar
            Surface(
                modifier = Modifier
                    .size(if (isReply) 32.dp else 40.dp)
                    .clickable { onUserClick(comment.userId) },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = comment.username.firstOrNull()?.toString() ?: "U",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isReply) 14.sp else 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // Username
                Text(
                    text = comment.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onUserClick(comment.userId) }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Comment content
                Text(
                    text = comment.content,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Time, reply and delete button
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.createTime,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // 回复按钮 - 如果是二级回复，回复到顶级评论
                    TextButton(
                        onClick = { 
                            if (isReply && parentComment != null) {
                                // 如果是回复的回复，回复到顶级评论
                                onReply(parentComment)
                            } else {
                                onReply(comment)
                            }
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Reply", fontSize = 12.sp)
                    }
                    
                    // 删除按钮 - 只对自己的评论显示
                    if (isOwnComment) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { onDelete(comment) },
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete", fontSize = 12.sp)
                        }
                    }
                    
                    if (!isReply && comment.replyCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { isExpanded = !isExpanded },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (isExpanded) "Hide replies" else "${comment.replyCount} replies",
                                fontSize = 12.sp
                            )
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                // Display replies
                if (isExpanded && comment.replies != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        comment.replies.forEach { reply ->
                            CommentItem(
                                comment = reply,
                                onReply = onReply,
                                onDelete = onDelete,
                                onUserClick = onUserClick,
                                context = context,
                                isReply = true,
                                parentComment = comment
                            )
                        }
                    }
                }
            }
        }
        
        if (!isReply) {
            Spacer(modifier = Modifier.height(12.dp))
            Divider()
        }
    }
}

@Composable
fun PostImages(mediaUrls: String) {
    var showImagePreview by remember { mutableStateOf(false) }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }
    
    // Parse JSON array
    val imageUrls = try {
        val type = object : TypeToken<List<String>>() {}.type
        Gson().fromJson<List<String>>(mediaUrls, type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    
    if (imageUrls.isEmpty()) return
    
    when (imageUrls.size) {
        1 -> {
            // Single image - large display
            AsyncImage(
                model = imageUrls[0],
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clickable {
                        previewImageUrl = imageUrls[0]
                        showImagePreview = true
                    }
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
        2 -> {
            // Two images - horizontal layout
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                imageUrls.forEach { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp)
                            .clickable {
                                previewImageUrl = url
                                showImagePreview = true
                            }
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        3 -> {
            // Three images - first one takes one row, last two share a row
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = imageUrls[0],
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable {
                            previewImageUrl = imageUrls[0]
                            showImagePreview = true
                        }
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    imageUrls.drop(1).forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp)
                                .clickable {
                                    previewImageUrl = url
                                    showImagePreview = true
                                }
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
        else -> {
            // 4 or more - 2x2 grid or horizontal scroll
            if (imageUrls.size == 4) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        imageUrls.take(2).forEach { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(150.dp)
                                    .clickable {
                                        previewImageUrl = url
                                        showImagePreview = true
                                    }
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        imageUrls.drop(2).take(2).forEach { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(150.dp)
                                    .clickable {
                                        previewImageUrl = url
                                        showImagePreview = true
                                    }
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            } else {
                // 5-9 images - horizontal scroll
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(imageUrls) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier
                                .width(250.dp)
                                .height(250.dp)
                                .clickable {
                                    previewImageUrl = url
                                    showImagePreview = true
                                }
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
    
    // 图片预览对话框
    if (showImagePreview && previewImageUrl != null) {
        ImagePreviewDialog(
            imageUrl = previewImageUrl!!,
            onDismiss = { showImagePreview = false }
        )
    }
}

@Composable
fun ImagePreviewDialog(
    imageUrl: String,
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
            AsyncImage(
                model = imageUrl,
                contentDescription = "Preview",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )
            
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

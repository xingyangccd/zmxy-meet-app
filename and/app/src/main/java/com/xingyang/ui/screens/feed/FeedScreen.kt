package com.xingyang.ui.screens.feed

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xingyang.data.api.ApiService
import com.xingyang.data.api.Post
import com.xingyang.ui.components.VideoPlayer
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(navController: NavHostController) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var likedPosts by remember { mutableStateOf<Set<Long>>(emptySet()) } // Maintain like status
    var postLikeCounts by remember { mutableStateOf<Map<Long, Int>>(emptyMap()) } // Maintain like counts
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Unread notification count
    var unreadNotificationCount by remember { mutableStateOf(0) }
    
    // Comment related state
    var showCommentDialog by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    
    // Menu related state
    var showMenuDialog by remember { mutableStateOf(false) }
    var menuPost by remember { mutableStateOf<Post?>(null) }
    
    // Report related state
    var showReportDialog by remember { mutableStateOf(false) }
    var reportPost by remember { mutableStateOf<Post?>(null) }
    
    // Listen for navigation back, refresh data
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    
    // Initial load and refresh on each navigation back
    LaunchedEffect(navBackStackEntry) {
        loadPosts(
            context = context,
            apiService = apiService,
            onLoading = { isLoading = it },
            onSuccess = { loadedPosts ->
                posts = loadedPosts
                errorMessage = null
                
                // Load like status for each post
                scope.launch {
                    loadedPosts.forEach { post ->
                        try {
                            val result = apiService.checkLikeStatus(post.id)
                            if (result.code == 200 && result.data == true) {
                                likedPosts = likedPosts + post.id
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("FeedScreen", "Failed to check like status: ${e.message}")
                        }
                    }
                }
            },
            onError = { errorMessage = it }
        )
        
        // Load unread notification count
        scope.launch {
            try {
                val result = apiService.getUnreadCount()
                if (result.code == 200 && result.data != null) {
                    unreadNotificationCount = result.data["unreadCount"]?.toInt() ?: 0
                }
            } catch (e: Exception) {
                android.util.Log.e("FeedScreen", "Failed to get unread notification count: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Campus Feed",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("notifications") }) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationCount > 0) {
                                    Badge {
                                        Text(if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                !errorMessage.isNullOrEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                                    loadPosts(
                                        context = context,
                                        apiService = apiService,
                                        onLoading = { isLoading = it },
                                        onSuccess = { 
                                            posts = it
                                            errorMessage = null  // Clear error message on success
                                        },
                                        onError = { errorMessage = it }
                                    )
                                }
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                posts.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Article,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No posts yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Publish your first post!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                else -> {
                    // Add log to view data
                    android.util.Log.d("FeedScreen", "Preparing to render ${posts.size} posts")
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = 80.dp,  // Leave space for FAB
                            start = 0.dp,
                            end = 0.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            count = posts.size,
                            key = { index -> posts[index].id }
                        ) { index ->
                            val post = posts[index]
                            android.util.Log.d("FeedScreen", "Rendering post #$index: id=${post.id}")
                            val isLiked = likedPosts.contains(post.id)
                            val likesCount = postLikeCounts[post.id] ?: post.likesCount
                            
                            PostCard(
                                post = post,
                                isLiked = isLiked,
                                likesCount = likesCount,
                                context = context,
                                onUserClick = { userId ->
                                    navController.navigate("user_profile/$userId")
                                },
                                onMenu = {
                                    menuPost = post
                                    showMenuDialog = true
                                },
                                onLike = {
                                    scope.launch {
                                        toggleLike(
                                            context = context,
                                            apiService = apiService,
                                            post = post,
                                            isLiked = isLiked,
                                            onSuccess = {
                                                // Toggle like status
                                                likedPosts = if (isLiked) {
                                                    likedPosts - post.id
                                                } else {
                                                    likedPosts + post.id
                                                }
                                                // Update like count
                                                val newCount = if (isLiked) {
                                                    likesCount - 1
                                                } else {
                                                    likesCount + 1
                                                }
                                                postLikeCounts = postLikeCounts + (post.id to newCount)
                                            },
                                            onError = { msg ->
                                                errorMessage = msg
                                            }
                                        )
                                    }
                                },
                                onComment = {
                                    selectedPost = post
                                    showCommentDialog = true
                                },
                                onShare = {
                                    sharePost(context, post)
                                },
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
    
    // Comment dialog
    if (showCommentDialog && selectedPost != null) {
        CommentDialog(
            post = selectedPost!!,
            onDismiss = { showCommentDialog = false },
            context = context,
            apiService = apiService
        )
    }
    
    // Menu dialog
    if (showMenuDialog && menuPost != null) {
        PostMenuDialog(
            post = menuPost!!,
            onDismiss = { showMenuDialog = false },
            onDelete = {
                scope.launch {
                    try {
                        val result = apiService.deletePost(menuPost!!.id)
                        if (result.code == 200) {
                            android.widget.Toast.makeText(context, "Deleted successfully", android.widget.Toast.LENGTH_SHORT).show()
                            // Remove the post from list
                            posts = posts.filter { it.id != menuPost!!.id }
                        } else {
                            android.widget.Toast.makeText(context, "Delete failed: ${result.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Delete failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                showMenuDialog = false
            },
            onEdit = {
                // Navigate to edit page, pass post ID
                navController.navigate("edit_post/${menuPost!!.id}")
                showMenuDialog = false
            },
            onReport = {
                reportPost = menuPost
                showMenuDialog = false
                showReportDialog = true
            },
            context = context
        )
    }
    
    // Report dialog
    if (showReportDialog && reportPost != null) {
        ReportDialog(
            post = reportPost!!,
            onDismiss = { showReportDialog = false },
            onReport = { reason, description ->
                scope.launch {
                    try {
                        val result = apiService.createReport(
                            com.xingyang.data.api.ReportRequest(
                                reportedType = "post",
                                reportedId = reportPost!!.id,
                                reason = reason,
                                description = description
                            )
                        )
                        if (result.code == 200) {
                            android.widget.Toast.makeText(context, "Report submitted, we will process it soon", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, "Report failed: ${result.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Report failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                showReportDialog = false
            },
            context = context
        )
    }
}

@Composable
fun PostCard(
    post: Post,
    isLiked: Boolean,
    likesCount: Int,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    onClick: () -> Unit,
    onMenu: () -> Unit,
    onUserClick: (Long) -> Unit,
    context: Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            // User info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onUserClick(post.userId) },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = post.username?.firstOrNull()?.toString() ?: "U",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.username ?: "Unknown user",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = formatTime(post.createTime),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onMenu) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Content
            Text(
                text = post.content,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                maxLines = 10,
                overflow = TextOverflow.Ellipsis
            )
            
            // Display images
            if (!post.mediaUrls.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                PostImages(mediaUrls = post.mediaUrls)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Interaction buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InteractionButton(
                    icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    text = likesCount.toString(),
                    onClick = onLike,
                    tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                InteractionButton(
                    icon = Icons.Default.ChatBubbleOutline,
                    text = post.commentsCount.toString(),
                    onClick = onComment
                )
                
                InteractionButton(
                    icon = Icons.Default.Share,
                    text = post.sharesCount.toString(),
                    onClick = onShare
                )
            }
        }
    }
}

@Composable
fun InteractionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    TextButton(onClick = onClick) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = tint
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = tint)
    }
}

private suspend fun loadPosts(
    context: Context,
    apiService: ApiService,
    onLoading: (Boolean) -> Unit,
    onSuccess: (List<Post>) -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)
    try {
        android.util.Log.d("FeedScreen", "Starting to load posts list")
        val result = apiService.getPosts()
        android.util.Log.d("FeedScreen", "Response received: code=${result.code}, message=${result.message}, data size=${result.data?.size}")
        if (result.code == 200 && result.data != null) {
            android.util.Log.d("FeedScreen", "Successfully loaded ${result.data.size} posts")
            result.data.forEachIndexed { index, post ->
                android.util.Log.d("FeedScreen", "Post[$index]: id=${post.id}, content=${post.content}")
            }
            onSuccess(result.data)
            // Clear error message - don't set empty string
        } else {
            android.util.Log.e("FeedScreen", "Load failed: ${result.message}")
            onError(result.message ?: "Failed to get posts list")
        }
    } catch (e: Exception) {
        android.util.Log.e("FeedScreen", "Load exception", e)
        onError(e.message ?: "Load failed, please check network connection")
    } finally {
        onLoading(false)
    }
}

private suspend fun toggleLike(
    context: Context,
    apiService: ApiService,
    post: Post,
    isLiked: Boolean,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val result = if (isLiked) {
            apiService.unlikePost(post.id)
        } else {
            apiService.likePost(post.id)
        }
        
        if (result.code == 200) {
            onSuccess()
        } else {
            onError(result.message ?: "Operation failed")
        }
    } catch (e: Exception) {
        onError(e.message ?: "Network error")
    }
}

private fun sharePost(context: Context, post: Post) {
    val shareText = "${post.username ?: "Someone"} shared:\n${post.content}\n\nFrom Campus Social App"
    
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        putExtra(android.content.Intent.EXTRA_SUBJECT, "Campus Post Share")
    }
    
    context.startActivity(
        android.content.Intent.createChooser(intent, "Share to")
    )
}

@Composable
fun PostImages(mediaUrls: String) {
    var showImagePreview by remember { mutableStateOf(false) }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }
    
    // Parse JSON array
    val allUrls = try {
        val type = object : TypeToken<List<String>>() {}.type
        Gson().fromJson<List<String>>(mediaUrls, type) ?: emptyList()
    } catch (e: Exception) {
        android.util.Log.e("PostImages", "Failed to parse mediaUrls: $mediaUrls", e)
        emptyList()
    }
    
    if (allUrls.isEmpty()) return
    
    android.util.Log.d("PostImages", "Displaying ${allUrls.size} media items: $allUrls")
    
    // Separate images and videos
    val imageUrls = allUrls.filter { url ->
        url.contains("/images/") || url.endsWith(".jpg") || url.endsWith(".png") || url.endsWith(".jpeg") || url.endsWith(".webp")
    }
    val videoUrls = allUrls.filter { url ->
        url.contains("/videos/") || url.endsWith(".mp4") || url.endsWith(".mov")
    }
    
    android.util.Log.d("PostImages", "Images: ${imageUrls.size}, Videos: ${videoUrls.size}")
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Display videos
        videoUrls.forEach { videoUrl ->
            VideoPlayer(
                videoUrl = videoUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }
        
        // Display images
        when (imageUrls.size) {
            1 -> {
            // Single image - large display
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
                        .height(150.dp)
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
                                .height(110.dp)
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
            // 4 or more - 2x2 grid, horizontal scroll for extras
            if (imageUrls.size == 4) {
                // 4 images - 2x2 grid
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
                                    .height(120.dp)
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
                                    .height(120.dp)
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
                // 5-9 images - horizontal scroll display
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(imageUrls) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier
                                .width(200.dp)
                                .height(200.dp)
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
    }
    
    // 图片预览对话框
    if (showImagePreview && previewImageUrl != null) {
        ImagePreviewDialog(
            imageUrl = previewImageUrl!!,
            onDismiss = { showImagePreview = false }
        )
    }
}

// Post menu dialog
@Composable
fun PostMenuDialog(
    post: Post,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onReport: () -> Unit,
    context: Context
) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val currentUserId = prefs.getString("user_id", null)
    val isOwnPost = currentUserId == post.userId.toString()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post Options") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isOwnPost) {
                    // Own posts can be edited and deleted
                    TextButton(
                        onClick = onEdit,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Edit Post")
                        }
                    }
                    
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Delete Post")
                        }
                    }
                } else {
                    // Others' posts can be reported
                    TextButton(
                        onClick = onReport,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Report, contentDescription = "Report")
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Report Post")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// 评论对话框
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentDialog(
    post: Post,
    onDismiss: () -> Unit,
    context: Context,
    apiService: ApiService
) {
    var commentText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comment") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Comment on ${post.username}'s post",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Write your comment...") },
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (commentText.isNotBlank() && !isSubmitting) {
                        scope.launch {
                            isSubmitting = true
                            try {
                                val request = com.xingyang.data.api.AddCommentRequest(commentText)
                                val result = apiService.addComment(post.id, request)
                                if (result.code == 200) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Comment posted successfully!", 
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    onDismiss()
                                } else {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Comment failed: ${result.message}",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Comment failed: ${e.message}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } finally {
                                isSubmitting = false
                            }
                        }
                    }
                },
                enabled = commentText.isNotBlank() && !isSubmitting
            ) {
                Text(if (isSubmitting) "Publishing..." else "Publish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// 举报对话框
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDialog(
    post: Post,
    onDismiss: () -> Unit,
    onReport: (String, String?) -> Unit,
    context: Context
) {
    var selectedReason by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    val reportReasons = listOf(
        "Spam",
        "Sexual content",
        "Illegal content",
        "Scam",
        "Harassment",
        "Privacy violation",
        "Other"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Post", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Please select report reason",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Report reason options
                reportReasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = reason)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Detailed description
                Text(
                    text = "Detailed description (optional)",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    placeholder = { Text("Please describe the reason for reporting...") },
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedReason.isNotBlank()) {
                        onReport(selectedReason, description.ifBlank { null })
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            "Please select a report reason",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Text("Submit Report", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTime(time: String?): String {
    // TODO: Implement time formatting
    return time ?: "Just now"
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

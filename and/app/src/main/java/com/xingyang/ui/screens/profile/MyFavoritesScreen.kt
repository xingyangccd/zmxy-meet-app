package com.xingyang.ui.screens.profile

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.xingyang.data.api.ApiService
import com.xingyang.data.api.Post
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFavoritesScreen(navController: NavHostController) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    
    var likedPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Load liked posts
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val userId = prefs.getString("user_id", null)?.toLongOrNull()
            
            if (userId != null) {
                val result = apiService.getUserLikedPosts(userId)
                if (result.code == 200 && result.data != null) {
                    likedPosts = result.data
                } else {
                    errorMessage = result.message ?: "Failed to load favorites"
                }
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Network error"
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Favorites") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "Error",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                    val userId = prefs.getString("user_id", null)?.toLongOrNull()
                                    if (userId != null) {
                                        val result = apiService.getUserLikedPosts(userId)
                                        if (result.code == 200 && result.data != null) {
                                            likedPosts = result.data
                                        } else {
                                            errorMessage = result.message
                                        }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message
                                } finally {
                                    isLoading = false
                                }
                            }
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
            likedPosts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "No favorites yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Your favorite posts will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(likedPosts) { post ->
                        FavoritePostItem(
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
fun FavoritePostItem(
    post: Post,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "❤️ ${post.likesCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "💬 ${post.commentsCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

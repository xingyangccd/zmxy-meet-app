package com.xingyang.ui.screens.profile

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.xingyang.data.api.User
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    
    var user by remember { mutableStateOf<User?>(null) }
    var userStats by remember { mutableStateOf<Map<String, Int>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Add manual refresh functionality
    fun loadUserInfo() {
        scope.launch {
            isLoading = true
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val username = prefs.getString("username", null)
            val userId = prefs.getString("user_id", null)?.toLongOrNull() ?: 0L
            val nickname = prefs.getString("nickname", null)
            val campus = prefs.getString("campus", null)
            val schoolVerified = prefs.getBoolean("school_verified", false)
            
            android.util.Log.d("ProfileScreen", "Loading user info: username=$username, userId=$userId, nickname=$nickname")
            
            // If not logged in, navigate to login page
            if (userId <= 0 || username == null) {
                android.util.Log.d("ProfileScreen", "User not logged in, navigating to login page")
                isLoading = false
                navController.navigate("login") {
                    popUpTo("main") { inclusive = true }
                }
                return@launch
            }
            
            // Create user object
            user = User(
                id = userId,
                username = username,
                nickname = nickname ?: username,
                email = null,
                avatarUrl = null,
                schoolVerified = schoolVerified,
                campus = campus,
                createTime = null,
                updateTime = null
            )
            
            // Get user statistics
            try {
                val statsResult = apiService.getUserStats(userId)
                if (statsResult.code == 200 && statsResult.data != null) {
                    userStats = mapOf(
                        "postsCount" to statsResult.data.postsCount,
                        "followersCount" to statsResult.data.followersCount,
                        "followingCount" to statsResult.data.followingCount
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileScreen", "Failed to load statistics", e)
            }
            
            isLoading = false
        }
    }
    
    // Refresh on each page entry (using navigation state as key)
    val currentEntry = navController.currentBackStackEntry
    LaunchedEffect(currentEntry) {
        loadUserInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Center") },
                actions = {
                    IconButton(onClick = { loadUserInfo() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, "Settings")
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // User info card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar
                            Surface(
                                modifier = Modifier.size(80.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = user?.username?.firstOrNull()?.toString() ?: "U",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Username
                            Text(
                                text = user?.nickname ?: "Unknown user",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // User ID
                            Text(
                                text = "@${user?.username}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Statistics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(
                                    label = "Posts",
                                    value = "${userStats?.get("postsCount") ?: 0}",
                                    onClick = {
                                        // Navigate to current user profile
                                        val userId = user?.id ?: 0L
                                        if (userId > 0) {
                                            navController.navigate("user_profile/$userId")
                                        }
                                    }
                                )
                                Divider(
                                    modifier = Modifier
                                        .height(40.dp)
                                        .width(1.dp)
                                )
                                StatItem(
                                    label = "Following",
                                    value = "${userStats?.get("followingCount") ?: 0}",
                                    onClick = {
                                        val userId = user?.id ?: 0L
                                        if (userId > 0) {
                                            navController.navigate("following_list/$userId")
                                        }
                                    }
                                )
                                Divider(
                                    modifier = Modifier
                                        .height(40.dp)
                                        .width(1.dp)
                                )
                                StatItem(
                                    label = "Followers",
                                    value = "${userStats?.get("followersCount") ?: 0}",
                                    onClick = {
                                        val userId = user?.id ?: 0L
                                        if (userId > 0) {
                                            navController.navigate("followers_list/$userId")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Feature menu
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column {
                            MenuItemRow(
                                icon = Icons.Default.Person,
                                title = "Edit Profile",
                                onClick = { navController.navigate("edit_profile") }
                            )
                            Divider()
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
                        }
                    }
                }
                
                // Settings menu
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column {
                            // Dark mode (using Switch)
                            var isDarkMode by remember { 
                                mutableStateOf(
                                    context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                        .getBoolean("dark_mode", false)
                                )
                            }
                            Surface(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.DarkMode,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Dark Mode",
                                        modifier = Modifier.weight(1f)
                                    )
                                    Switch(
                                        checked = isDarkMode,
                                        onCheckedChange = { 
                                            isDarkMode = it
                                            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                                .edit().putBoolean("dark_mode", it).apply()
                                            android.widget.Toast.makeText(
                                                context,
                                                "Dark mode ${if (it) "enabled" else "disabled"}",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            }
                            Divider()
                            MenuItemRow(
                                icon = Icons.Default.Info,
                                title = "About Us",
                                onClick = { navController.navigate("about") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, onClick: (() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MenuItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

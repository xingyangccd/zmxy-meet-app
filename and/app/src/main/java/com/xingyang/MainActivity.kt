package com.xingyang

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.xingyang.ui.screens.auth.LoginScreen
import com.xingyang.ui.screens.auth.RegisterScreen
import com.xingyang.ui.screens.main.MainScreen
import com.xingyang.ui.screens.notification.NotificationScreen
import com.xingyang.ui.screens.chat.ChatDetailScreen
import com.xingyang.ui.screens.post.CreatePostScreen
import com.xingyang.ui.screens.post.PostDetailScreen
import com.xingyang.ui.screens.profile.*
import com.xingyang.ui.screens.user.UserProfileScreen
import com.xingyang.ui.theme.ZmxyMeetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            var darkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", false)) }
            
            // 监听深色模式变化
            DisposableEffect(Unit) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "dark_mode") {
                        darkMode = prefs.getBoolean("dark_mode", false)
                    }
                }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    prefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }
            
            ZmxyMeetTheme(darkTheme = darkMode) {
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    // 认证页面
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
                                // 注册成功后跳转到登录页面
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    // 主界面（包含底部导航）
                    composable("main") {
                        MainScreen(navController)
                    }
                    
                    // 其他页面
                    composable("create_post") {
                        CreatePostScreen(navController)
                    }
                    
                    // 编辑帖子
                    composable(
                        route = "edit_post/{postId}",
                        arguments = listOf(navArgument("postId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val postId = backStackEntry.arguments?.getLong("postId")
                        CreatePostScreen(navController = navController, postId = postId)
                    }
                    
                    composable("notifications") {
                        NotificationScreen(navController)
                    }
                    
                    // 帖子详情
                    composable(
                        route = "post_detail/{postId}",
                        arguments = listOf(navArgument("postId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val postId = backStackEntry.arguments?.getLong("postId") ?: 0L
                        PostDetailScreen(postId = postId, navController = navController)
                    }
                    
                    // 用户主页
                    composable(
                        route = "user_profile/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
                        UserProfileScreen(userId = userId, navController = navController)
                    }
                    
                    // 个人中心子页面
                    composable("edit_profile") {
                        EditProfileScreen(navController)
                    }
                    composable("my_favorites") {
                        MyFavoritesScreen(navController)
                    }
                    composable("browse_history") {
                        BrowseHistoryScreen(navController)
                    }
                    composable("about") {
                        AboutScreen(navController)
                    }
                    composable("settings") {
                        SettingsScreen(navController)
                    }
                    
                    // 关注/粉丝列表
                    composable(
                        route = "following_list/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
                        FollowingListScreen(userId = userId, navController = navController)
                    }
                    composable(
                        route = "followers_list/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
                        FollowersListScreen(userId = userId, navController = navController)
                    }
                    
                    // 聊天详情
                    composable(
                        route = "chat/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
                        ChatDetailScreen(otherUserId = userId, navController = navController)
                    }
                }
            }
        }
    }
}
package com.xingyang.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.xingyang.ui.screens.auth.LoginScreen
import com.xingyang.ui.screens.auth.RegisterScreen
import com.xingyang.ui.screens.chat.ChatDetailScreen
import com.xingyang.ui.screens.chat.ChatListScreen
import com.xingyang.ui.screens.circle.CircleDetailScreen
import com.xingyang.ui.screens.circle.CircleListScreen
import com.xingyang.ui.screens.discovery.DiscoveryScreen
import com.xingyang.ui.screens.feed.FeedScreen
import com.xingyang.ui.screens.notification.NotificationScreen
import com.xingyang.ui.screens.post.CreatePostScreen
import com.xingyang.ui.screens.profile.ProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "login"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 认证页面
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = { navController.navigate("feed") }
            )
        }
        
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = { navController.navigate("feed") }
            )
        }
        
        // 主要页面
        composable("feed") {
            FeedScreen(navController)
        }
        
        composable("discovery") {
            DiscoveryScreen(navController)
        }
        
        composable("chat_list") {
            ChatListScreen(navController)
        }
        
        composable(
            route = "chat_detail/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.LongType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getLong("chatId") ?: 0L
            ChatDetailScreen(chatId, navController)
        }
        
        composable("circle_list") {
            CircleListScreen(navController)
        }
        
        composable("circle_detail/{circleId}") { backStackEntry ->
            val circleId = backStackEntry.arguments?.getString("circleId") ?: ""
            CircleDetailScreen(circleId, navController)
        }
        
        composable("create_post") {
            CreatePostScreen(navController)
        }
        
        composable("profile") {
            ProfileScreen(navController)
        }
        
        composable("notifications") {
            NotificationScreen(navController)
        }
    }
}

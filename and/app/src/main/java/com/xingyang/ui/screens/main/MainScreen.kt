package com.xingyang.ui.screens.main

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.xingyang.data.api.ApiService
import com.xingyang.ui.screens.chat.ChatListScreen
import com.xingyang.ui.screens.discovery.DiscoveryScreen
import com.xingyang.ui.screens.feed.FeedScreen
import com.xingyang.ui.screens.profile.ProfileScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Feed : BottomNavItem("feed", Icons.Default.Home, "Home")
    object Discovery : BottomNavItem("discovery", Icons.Default.Search, "Discover")
    object Chat : BottomNavItem("chat", Icons.Default.Chat, "Messages")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainNavController: NavHostController) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Unread message and notification count
    var unreadMessageCount by remember { mutableStateOf(0) }
    var unreadNotificationCount by remember { mutableStateOf(0) }
    
    // Refresh unread counts on route change
    LaunchedEffect(currentRoute) {
        scope.launch {
            try {
                // Get unread message count
                val messageResult = apiService.getUnreadMessageCount()
                if (messageResult.code == 200 && messageResult.data != null) {
                    unreadMessageCount = messageResult.data
                }
                
                // Get unread notification count
                val notificationResult = apiService.getUnreadCount()
                if (notificationResult.code == 200 && notificationResult.data != null) {
                    unreadNotificationCount = notificationResult.data["unreadCount"]?.toInt() ?: 0
                }
            } catch (e: Exception) {
                android.util.Log.e("MainScreen", "Failed to get unread counts: ${e.message}")
            }
        }
    }
    
    val items = listOf(
        BottomNavItem.Feed,
        BottomNavItem.Discovery,
        BottomNavItem.Chat,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // First two buttons
                items.take(2).forEach { item ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                bottomNavController.navigate(item.route) {
                                    popUpTo(bottomNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
                
                // Middle create post button
                NavigationBarItem(
                    icon = { 
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Publish",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = { Text("Publish") },
                    selected = false,
                    onClick = { mainNavController.navigate("create_post") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                // Last two buttons (messages and profile)
                items.drop(2).forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { 
                            BadgedBox(
                                badge = {
                                    // Message badge
                                    if (item == BottomNavItem.Chat && unreadMessageCount > 0) {
                                        Badge {
                                            Text(if (unreadMessageCount > 99) "99+" else unreadMessageCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            }
                        },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                bottomNavController.navigate(item.route) {
                                    popUpTo(bottomNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Feed.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Feed.route) {
                FeedScreen(mainNavController)
            }
            composable(BottomNavItem.Discovery.route) {
                DiscoveryScreen(mainNavController)
            }
            composable(BottomNavItem.Chat.route) {
                ChatListScreen(mainNavController)
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(mainNavController)
            }
        }
    }
}

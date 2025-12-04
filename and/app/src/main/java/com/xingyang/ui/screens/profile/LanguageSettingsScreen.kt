package com.xingyang.ui.screens.profile

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

// 简单的多语言支持对象
object AppLanguage {
    private var currentLang = "zh_CN"
    
    fun setLanguage(lang: String) {
        currentLang = lang
    }
    
    fun getCurrentLanguage(): String = currentLang
    
    fun getString(key: String): String {
        return when (currentLang) {
            "zh_CN" -> getChineseSimplified(key)
            "zh_TW" -> getChineseTraditional(key)
            "en_US" -> getEnglish(key)
            else -> getChineseSimplified(key)
        }
    }
    
    private fun getChineseSimplified(key: String): String = when (key) {
        "app_name" -> "校萌遇"
        "home" -> "首页"
        "discovery" -> "发现"
        "chat" -> "消息"
        "profile" -> "我的"
        "posts" -> "动态"
        "following" -> "关注"
        "followers" -> "粉丝"
        "settings" -> "设置"
        "logout" -> "退出登录"
        else -> key
    }
    
    private fun getChineseTraditional(key: String): String = when (key) {
        "app_name" -> "校萌遇"
        "home" -> "首頁"
        "discovery" -> "發現"
        "chat" -> "消息"
        "profile" -> "我的"
        "posts" -> "動態"
        "following" -> "關注"
        "followers" -> "粉絲"
        "settings" -> "設置"
        "logout" -> "退出登錄"
        else -> key
    }
    
    private fun getEnglish(key: String): String = when (key) {
        "app_name" -> "Campus Meet"
        "home" -> "Home"
        "discovery" -> "Discover"
        "chat" -> "Messages"
        "profile" -> "Profile"
        "posts" -> "Posts"
        "following" -> "Following"
        "followers" -> "Followers"
        "settings" -> "Settings"
        "logout" -> "Logout"
        else -> key
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    
    var selectedLanguage by remember { 
        mutableStateOf(prefs.getString("language", "zh_CN") ?: "zh_CN") 
    }
    
    // 在启动时应用保存的语言
    LaunchedEffect(Unit) {
        val savedLanguage = prefs.getString("language", "zh_CN") ?: "zh_CN"
        AppLanguage.setLanguage(savedLanguage)
    }
    
    val languages = listOf(
        "zh_CN" to "简体中文",
        "zh_TW" to "繁體中文",
        "en_US" to "English"
    )

    // 根据当前语言获取标题文本
    val titleText = when (selectedLanguage) {
        "zh_CN" -> "语言设置"
        "zh_TW" -> "語言設置"
        "en_US" -> "Language Settings"
        else -> "语言设置"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleText) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, 
                            contentDescription = when (selectedLanguage) {
                                "zh_CN" -> "返回"
                                "zh_TW" -> "返回"
                                "en_US" -> "Back"
                                else -> "返回"
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            languages.forEach { (code, name) ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (selectedLanguage != code) {
                                selectedLanguage = code
                                prefs.edit().putString("language", code).apply()
                                AppLanguage.setLanguage(code)
                                
                                // 立即重新创建 Activity 以应用语言更改
                                (context as? Activity)?.let { activity ->
                                    activity.recreate()
                                }
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name)
                        if (selectedLanguage == code) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Divider()
            }
        }
    }
}

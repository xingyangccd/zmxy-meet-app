package com.xingyang.ui.screens.circle

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.People
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
import com.xingyang.data.api.Circle
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    
    var circles by remember { mutableStateOf<List<Circle>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // 加载圈子列表
    LaunchedEffect(Unit) {
        loadCircles(
            context = context,
            apiService = apiService,
            onLoading = { isLoading = it },
            onSuccess = { circles = it },
            onError = { errorMessage = it }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("圈子") },
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
                            text = errorMessage ?: "加载失败",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            scope.launch {
                                loadCircles(context, apiService,
                                    onLoading = { isLoading = it },
                                    onSuccess = { circles = it },
                                    onError = { errorMessage = it }
                                )
                            }
                        }) {
                            Text("重试")
                        }
                    }
                }
            }
            circles.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "暂无圈子",
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
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(circles) { circle ->
                        CircleCard(
                            circle = circle,
                            onClick = {
                                navController.navigate("circle/${circle.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CircleCard(
    circle: Circle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 圈子图标
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 圈子信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = circle.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = circle.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            
            // 成员数
            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    Icons.Default.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "0 成员",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleDetailScreen(circleId: String, navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("圈子详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("圈子详情页面 - ID: $circleId")
        }
    }
}

private suspend fun loadCircles(
    context: Context,
    apiService: ApiService,
    onLoading: (Boolean) -> Unit,
    onSuccess: (List<Circle>) -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)
    try {
        android.util.Log.d("CircleListScreen", "开始加载圈子列表")
        val result = apiService.getCircles()
        android.util.Log.d("CircleListScreen", "收到响应: code=${result.code}, data size=${result.data?.size}")
        
        if (result.code == 200 && result.data != null) {
            android.util.Log.d("CircleListScreen", "成功加载 ${result.data.size} 个圈子")
            onSuccess(result.data)
        } else {
            android.util.Log.e("CircleListScreen", "加载失败: ${result.message}")
            // 如果后端没有圈子数据，返回空列表而不是错误
            onSuccess(emptyList())
        }
    } catch (e: Exception) {
        android.util.Log.e("CircleListScreen", "加载异常", e)
        // 如果后端没有圈子接口，返回空列表
        onSuccess(emptyList())
    } finally {
        onLoading(false)
    }
}

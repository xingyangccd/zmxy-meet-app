package com.xingyang.ui.screens.discovery

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xingyang.data.api.ApiService
import com.xingyang.data.api.Post

@Composable
fun HotPostCard(
    post: Post,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 作者信息
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = post.username ?: "未知用户",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 内容
            Text(
                text = post.content,
                fontSize = 14.sp,
                maxLines = 3,
                lineHeight = 20.sp
            )
            
            // 图片缩略图
            if (!post.mediaUrls.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val imageUrls = try {
                    val type = object : TypeToken<List<String>>() {}.type
                    Gson().fromJson<List<String>>(post.mediaUrls, type) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
                if (imageUrls.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUrls[0]),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 统计信息
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "❤️ ${post.likesCount}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "💬 ${post.commentsCount}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

suspend fun loadHotPosts(
    context: Context,
    apiService: ApiService,
    onLoading: (Boolean) -> Unit,
    onSuccess: (List<Post>) -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)
    try {
        val result = apiService.getPosts()
        if (result.code == 200 && result.data != null) {
            // 按点赞数排序，取前20条
            val hotPosts = result.data.sortedByDescending { it.likesCount }.take(20)
            onSuccess(hotPosts)
        } else {
            onError(result.message ?: "加载失败")
        }
    } catch (e: Exception) {
        onError(e.message ?: "网络错误")
    } finally {
        onLoading(false)
    }
}

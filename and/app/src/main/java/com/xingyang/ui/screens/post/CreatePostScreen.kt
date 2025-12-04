package com.xingyang.ui.screens.post

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.xingyang.data.api.ApiService
import com.xingyang.data.api.CreatePostRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.koin.compose.koinInject
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavHostController,
    postId: Long? = null  // If postId exists, it's edit mode
) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    
    var content by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedVideos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var uploadedImageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var uploadedVideoUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val isEditMode = postId != null
    val canPublish = content.isNotBlank() && !isLoading && !isUploading
    
    // Edit mode: load existing post data
    LaunchedEffect(postId) {
        if (postId != null) {
            try {
                val result = apiService.getPostDetail(postId)
                if (result.code == 200 && result.data != null) {
                    content = result.data.content
                    uploadedImageUrls = try {
                        com.google.gson.Gson().fromJson(
                            result.data.mediaUrls,
                            object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                        ) ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load post: ${e.message}"
            }
        }
    }
    
    // Image picker (single selection to avoid duplicates)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            if (selectedImages.size < 9 && !selectedImages.contains(it)) {
                selectedImages = selectedImages + it
                
                // Automatically upload newly selected image
                scope.launch {
                    uploadImages(context, apiService, listOf(it), 
                        onUploading = { isUploading = it },
                        onSuccess = { urls -> 
                            uploadedImageUrls = uploadedImageUrls + urls
                        },
                        onError = { errorMessage = it }
                    )
                }
            }
        }
    }
    
    // Video picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            if (selectedVideos.size < 3 && !selectedVideos.contains(it)) {
                selectedVideos = selectedVideos + it
                
                // Automatically upload video
                scope.launch {
                    uploadVideos(context, apiService, listOf(it),
                        onUploading = { isUploading = it },
                        onSuccess = { urls ->
                            uploadedVideoUrls = uploadedVideoUrls + urls
                        },
                        onError = { errorMessage = it }
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Post" else "Create Post") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                publishPost(
                                    context = context,
                                    apiService = apiService,
                                    content = content,
                                    imageUrls = uploadedImageUrls,
                                    videoUrls = uploadedVideoUrls,
                                    postId = postId,  // Pass postId
                                    onLoading = { isLoading = it },
                                    onSuccess = {
                                        android.widget.Toast.makeText(
                                            context,
                                            if (isEditMode) "Updated successfully" else "Published successfully",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                        navController.popBackStack()
                                    },
                                    onError = { errorMessage = it }
                                )
                            }
                        },
                        enabled = canPublish
                    ) {
                        Text(
                            if (isEditMode) "Save" else "Publish",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Content input
            OutlinedTextField(
                value = content,
                onValueChange = { 
                    content = it
                    errorMessage = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                placeholder = { Text("Share your thoughts...") },
                shape = RoundedCornerShape(12.dp),
                maxLines = 15
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Character count
            Text(
                text = "${content.length} / 500",
                style = MaterialTheme.typography.bodySmall,
                color = if (content.length > 500) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Image preview
            if (selectedImages.isNotEmpty()) {
                Text("Images (${selectedImages.size}):", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(selectedImages) { uri ->
                        Box(modifier = Modifier.size(100.dp)) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp, 
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentScale = ContentScale.Crop
                            )
                            // Delete button
                            IconButton(
                                onClick = {
                                    val index = selectedImages.indexOf(uri)
                                    selectedImages = selectedImages.filter { it != uri }
                                    if (index < uploadedImageUrls.size) {
                                        uploadedImageUrls = uploadedImageUrls.filterIndexed { i, _ -> i != index }
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Video preview
            if (selectedVideos.isNotEmpty()) {
                Text("Videos (${selectedVideos.size}):", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(selectedVideos) { uri ->
                        Box(modifier = Modifier.size(100.dp)) {
                            // Video thumbnail
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Video",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            // Delete button
                            IconButton(
                                onClick = {
                                    val index = selectedVideos.indexOf(uri)
                                    selectedVideos = selectedVideos.filter { it != uri }
                                    if (index < uploadedVideoUrls.size) {
                                        uploadedVideoUrls = uploadedVideoUrls.filterIndexed { i, _ -> i != index }
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Error message
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Add image button
            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = selectedImages.size < 9 && !isUploading
            ) {
                Icon(Icons.Default.Image, "Add image")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isUploading) "Uploading..." 
                    else "Add image (${selectedImages.size}/9)"
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Add video button
            OutlinedButton(
                onClick = { videoPickerLauncher.launch("video/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = selectedVideos.size < 3 && !isUploading
            ) {
                Icon(Icons.Default.Videocam, "Add video")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isUploading) "Uploading..." 
                    else "Add video (${selectedVideos.size}/3)"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Publishing notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Publishing Guidelines",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "• Follow community guidelines\n• No illegal content\n• Respect others, communicate civilly",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            if (isLoading || isUploading) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                if (isUploading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Uploading files...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private suspend fun uploadImages(
    context: Context,
    apiService: ApiService,
    uris: List<Uri>,
    onUploading: (Boolean) -> Unit,
    onSuccess: (List<String>) -> Unit,
    onError: (String) -> Unit
) {
    onUploading(true)
    try {
        val urls = mutableListOf<String>()
        
        for (uri in uris) {
            try {
                // Convert Uri to File
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Create MultipartBody.Part
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
                
                // Upload image
                val result = apiService.uploadImage(part)
                if (result.code == 200 && result.data != null) {
                    urls.add(result.data.url)
                } else {
                    onError("Image upload failed: ${result.message}")
                    return
                }
                
                // Clean up temporary file
                file.delete()
            } catch (e: Exception) {
                onError("Image upload failed: ${e.message}")
                return
            }
        }
        
        onSuccess(urls)
    } catch (e: Exception) {
        onError("Image upload failed: ${e.message}")
    } finally {
        onUploading(false)
    }
}

private suspend fun uploadVideos(
    context: Context,
    apiService: ApiService,
    uris: List<Uri>,
    onUploading: (Boolean) -> Unit,
    onSuccess: (List<String>) -> Unit,
    onError: (String) -> Unit
) {
    onUploading(true)
    try {
        val urls = mutableListOf<String>()
        
        for (uri in uris) {
            try {
                // Convert Uri to File
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.mp4")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Create MultipartBody.Part
                val requestBody = file.asRequestBody("video/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
                
                // Upload video
                val result = apiService.uploadVideo(part)
                if (result.code == 200 && result.data != null) {
                    urls.add(result.data.url)
                } else {
                    onError("Video upload failed: ${result.message}")
                    return
                }
                
                // Clean up temporary file
                file.delete()
            } catch (e: Exception) {
                onError("Video upload failed: ${e.message}")
                return
            }
        }
        
        onSuccess(urls)
    } catch (e: Exception) {
        onError("Video upload failed: ${e.message}")
    } finally {
        onUploading(false)
    }
}

private suspend fun publishPost(
    context: Context,
    apiService: ApiService,
    content: String,
    imageUrls: List<String>,
    videoUrls: List<String>,
    postId: Long? = null,  // If postId exists, it's edit mode
    onLoading: (Boolean) -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    if (content.length > 500) {
        onError("Content cannot exceed 500 characters")
        return
    }
    
    onLoading(true)
    try {
        // Merge image and video URLs
        val allMediaUrls = imageUrls + videoUrls
        val mediaUrlsJson = if (allMediaUrls.isNotEmpty()) {
            com.google.gson.Gson().toJson(allMediaUrls)
        } else {
            null
        }
        
        if (postId != null) {
            // Edit mode: call update API
            val result = apiService.updatePost(
                postId,
                CreatePostRequest(
                    content = content,
                    mediaUrls = mediaUrlsJson,
                    type = "normal",
                    visibility = "public",
                    circleId = null
                )
            )
            if (result.code == 200) {
                onSuccess()
            } else {
                onError(result.message ?: "Update failed")
            }
        } else {
            // Create mode: call create API
            apiService.createPost(
                CreatePostRequest(
                    content = content,
                    mediaUrls = mediaUrlsJson,
                    type = "normal",
                    visibility = "public",
                    circleId = null
                )
            )
            onSuccess()
        }
    } catch (e: Exception) {
        onError(e.message ?: "Publish failed, please try again later")
    } finally {
        onLoading(false)
    }
}

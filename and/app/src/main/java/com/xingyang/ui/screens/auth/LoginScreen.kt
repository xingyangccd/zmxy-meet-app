package com.xingyang.ui.screens.auth

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xingyang.R
import com.xingyang.data.api.ApiService
import com.xingyang.data.api.LoginRequest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val apiService: ApiService = koinInject()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Form validation
    val isFormValid = username.isNotBlank() && password.length >= 6

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/Title
            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Join Campus Social",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Connect Your Campus Life",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = { 
                    username = it
                    errorMessage = null
                },
                label = { Text("Username") },
                placeholder = { Text("Enter username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                isError = errorMessage != null && username.isBlank()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    errorMessage = null
                },
                label = { Text("Password") },
                placeholder = { Text("Enter password (at least 6 characters)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { 
                        focusManager.clearFocus()
                        if (isFormValid && !isLoading) {
                            scope.launch {
                                performLogin(
                                    context = context,
                                    apiService = apiService,
                                    username = username,
                                    password = password,
                                    onLoading = { isLoading = it },
                                    onError = { errorMessage = it },
                                    onSuccess = onLoginSuccess
                                )
                            }
                        }
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                isError = errorMessage != null && password.length < 6
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Password hint
            if (password.isNotEmpty() && password.length < 6) {
                Text(
                    text = "Password must be at least 6 characters",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Error message
            AnimatedVisibility(visible = errorMessage != null) {
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
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Login button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    scope.launch {
                        performLogin(
                            context = context,
                            apiService = apiService,
                            username = username,
                            password = password,
                            onLoading = { isLoading = it },
                            onError = { errorMessage = it },
                            onSuccess = onLoginSuccess
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isFormValid && !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f))
                Text(
                    text = "OR",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Divider(modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Register link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Register Now",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigateToRegister() }
                        .padding(start = 4.dp)
                )
            }
        }
    }
}

// 将中文错误消息翻译为英文
private fun translateErrorMessage(message: String): String {
    return when {
        message.contains("密码错误") || message.contains("密码不正确") -> "Incorrect password"
        message.contains("用户不存在") || message.contains("用户名不存在") -> "User does not exist"
        message.contains("账号不存在") -> "Account does not exist"
        message.contains("账号已被禁用") -> "Account has been disabled"
        message.contains("用户名或密码错误") -> "Invalid username or password"
        message.contains("登录失败") -> "Login failed"
        message.contains("网络错误") -> "Network error"
        message.contains("服务器错误") -> "Server error"
        message.contains("参数错误") -> "Invalid parameters"
        else -> message // 如果不是中文或未匹配，返回原消息
    }
}

private suspend fun performLogin(
    context: Context,
    apiService: ApiService,
    username: String,
    password: String,
    onLoading: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onSuccess: () -> Unit
) {
    onLoading(true)
    try {
        android.util.Log.d("LoginScreen", "Starting login: username=$username")
        android.util.Log.d("LoginScreen", "BASE_URL=${com.xingyang.data.api.ApiService.BASE_URL}")
        
        val result = apiService.login(LoginRequest(username, password))
        
        android.util.Log.d("LoginScreen", "Response received: code=${result.code}, message=${result.message}")
        
        // Check if business logic succeeded
        if (result.code == 200 && result.data != null) {
            android.util.Log.d("LoginScreen", "Login successful: token=${result.data.token}")
            
            // Save token and user info (using commit for synchronous save)
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("auth_token", result.data.token)
            editor.putString("user_id", result.data.user.id.toString())
            editor.putString("username", result.data.user.username)
            editor.putString("nickname", result.data.user.nickname)
            editor.putBoolean("school_verified", result.data.user.schoolVerified)
            // Handle nullable fields
            result.data.user.campus?.let { editor.putString("campus", it) }
            editor.commit() // Use commit() for immediate synchronous save
            
            android.util.Log.d("LoginScreen", "User info saved")
            // 显示登录成功提示
            android.widget.Toast.makeText(
                context,
                "Login successful!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            onSuccess()
        } else {
            // Business logic failed
            val originalMsg = result.message ?: "Login failed"
            val errorMsg = translateErrorMessage(originalMsg)
            android.util.Log.e("LoginScreen", "Login failed: $originalMsg -> $errorMsg")
            onError(errorMsg)
        }
    } catch (e: retrofit2.HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        android.util.Log.e("LoginScreen", "HTTP error: code=${e.code()}, message=${e.message()}, body=$errorBody")
        onError("HTTP ${e.code()}: ${errorBody ?: e.message()}")
    } catch (e: java.net.UnknownHostException) {
        android.util.Log.e("LoginScreen", "Network error: Cannot connect to server", e)
        onError("Cannot connect to server, please check network")
    } catch (e: java.net.ConnectException) {
        android.util.Log.e("LoginScreen", "Connection error", e)
        onError("Connection failed: ${e.message}")
    } catch (e: Exception) {
        android.util.Log.e("LoginScreen", "Unknown error", e)
        onError("Login failed: ${e.javaClass.simpleName} - ${e.message}")
    } finally {
        onLoading(false)
    }
}

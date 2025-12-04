package com.xingyang.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object FileUploadHelper {
    
    /**
     * 将 Uri 转换为 MultipartBody.Part 用于上传
     */
    fun uriToMultipartBody(context: Context, uri: Uri, partName: String = "file"): MultipartBody.Part? {
        return try {
            val file = getFileFromUri(context, uri) ?: return null
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, file.name, requestBody)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 从 Uri 获取 File
     */
    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    val filePath = it.getString(columnIndex)
                    File(filePath)
                } else null
            }
        } catch (e: Exception) {
            // 如果无法直接获取文件路径，创建临时文件
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}")
                inputStream?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
        }
    }
    
    /**
     * 获取文件的 MIME 类型
     */
    fun getMimeType(context: Context, uri: Uri): String {
        return context.contentResolver.getType(uri) ?: "application/octet-stream"
    }
}

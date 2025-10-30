package com.chain.messaging.core.cloud

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudStorageManagerImpl @Inject constructor(
    private val context: Context,
    private val cloudAuthManager: CloudAuthManager,
    private val fileEncryption: FileEncryption,
    private val httpClient: OkHttpClient,
    private val json: Json
) : CloudStorageManager {
    
    companion object {
        private const val CHAIN_FOLDER_NAME = "ChainMessaging"
        private const val TEMP_FOLDER_NAME = "temp"
    }
    
    override suspend fun initialize() {
        // Create necessary directories
        val tempDir = File(context.cacheDir, TEMP_FOLDER_NAME)
        tempDir.mkdirs()
        
        // Initialize file encryption
        fileEncryption.initialize()
        
        // Clean up any leftover temporary files
        cleanupTempFiles()
    }
    
    override suspend fun initializeUserAccounts() {
        // Initialize cloud authentication for all services
        CloudService.values().forEach { service ->
            try {
                cloudAuthManager.initializeService(service)
            } catch (e: Exception) {
                // Continue with other services if one fails
            }
        }
    }
    
    private fun cleanupTempFiles() {
        try {
            val tempDir = File(context.cacheDir, TEMP_FOLDER_NAME)
            if (tempDir.exists()) {
                tempDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.lastModified() < System.currentTimeMillis() - 24 * 60 * 60 * 1000) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
    
    override suspend fun uploadFile(
        file: File,
        service: CloudService,
        expirationHours: Int
    ): Flow<UploadResult> = flow {
        try {
            // Check authentication
            val token = cloudAuthManager.getToken(service)
                ?: throw IllegalStateException("Not authenticated with ${service.displayName}")
            
            // Create temporary encrypted file
            val tempDir = File(context.cacheDir, TEMP_FOLDER_NAME)
            tempDir.mkdirs()
            val encryptedFile = File(tempDir, "${UUID.randomUUID()}.enc")
            
            // Encrypt the file
            val encryptionResult = fileEncryption.encryptFile(file, encryptedFile)
            if (encryptionResult !is EncryptionResult.Success) {
                emit(UploadResult.Error("File encryption failed"))
                return@flow
            }
            
            // Upload to cloud service
            val uploadUrl = getUploadUrl(service, file.name, token)
            val uploadResult = performUpload(encryptedFile, uploadUrl, token) { progress ->
                emit(UploadResult.Progress(progress.bytesUploaded, progress.totalBytes))
            }
            
            // Clean up temporary file
            encryptedFile.delete()
            
            if (uploadResult.isSuccess) {
                val encryptedLink = EncryptedLink(
                    url = uploadResult.url,
                    encryptionKey = encryptionResult.encryptionKey,
                    service = service,
                    expiresAt = Instant.now().plusSeconds(expirationHours * 3600L),
                    fileName = file.name,
                    fileSize = file.length(),
                    mimeType = getMimeType(file),
                    checksum = encryptionResult.checksum
                )
                
                emit(UploadResult.Success(encryptedLink))
            } else {
                emit(UploadResult.Error("Upload failed: ${uploadResult.error}"))
            }
            
        } catch (e: Exception) {
            emit(UploadResult.Error("Upload error: ${e.message}", e))
        }
    }.flowOn(Dispatchers.IO)
    
    override suspend fun downloadFile(
        encryptedLink: EncryptedLink,
        outputFile: File
    ): Flow<DownloadResult> = flow {
        try {
            // Check if link is expired
            if (encryptedLink.isExpired()) {
                emit(DownloadResult.Error("Link has expired"))
                return@flow
            }
            
            // Check authentication
            val token = cloudAuthManager.getToken(encryptedLink.service)
                ?: throw IllegalStateException("Not authenticated with ${encryptedLink.service.displayName}")
            
            // Create temporary file for encrypted download
            val tempDir = File(context.cacheDir, TEMP_FOLDER_NAME)
            tempDir.mkdirs()
            val encryptedFile = File(tempDir, "${UUID.randomUUID()}.enc")
            
            // Download encrypted file
            val downloadResult = performDownload(encryptedLink.url, encryptedFile, token) { progress ->
                emit(DownloadResult.Progress(progress.bytesDownloaded, progress.totalBytes))
            }
            
            if (!downloadResult.isSuccess) {
                emit(DownloadResult.Error("Download failed: ${downloadResult.error}"))
                return@flow
            }
            
            // Decrypt the file
            val decryptionResult = fileEncryption.decryptFile(
                encryptedFile,
                outputFile,
                encryptedLink.encryptionKey
            )
            
            // Clean up temporary file
            encryptedFile.delete()
            
            when (decryptionResult) {
                is DecryptionResult.Success -> {
                    // Verify checksum
                    if (fileEncryption.verifyChecksum(outputFile, encryptedLink.checksum)) {
                        emit(DownloadResult.Success(outputFile.absolutePath))
                    } else {
                        emit(DownloadResult.Error("File integrity check failed"))
                        outputFile.delete()
                    }
                }
                is DecryptionResult.Error -> {
                    emit(DownloadResult.Error("Decryption failed: ${decryptionResult.message}"))
                }
            }
            
        } catch (e: Exception) {
            emit(DownloadResult.Error("Download error: ${e.message}", e))
        }
    }.flowOn(Dispatchers.IO)
    
    override suspend fun deleteFile(encryptedLink: EncryptedLink): Boolean {
        return try {
            val token = cloudAuthManager.getToken(encryptedLink.service) ?: return false
            
            val deleteUrl = getDeleteUrl(encryptedLink.service, encryptedLink.url)
            val request = Request.Builder()
                .url(deleteUrl)
                .delete()
                .addHeader("Authorization", "Bearer ${token.accessToken}")
                .build()
            
            val response = httpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getStorageInfo(service: CloudService): StorageInfo? {
        return try {
            val token = cloudAuthManager.getToken(service) ?: return null
            
            val storageUrl = getStorageInfoUrl(service)
            val request = Request.Builder()
                .url(storageUrl)
                .addHeader("Authorization", "Bearer ${token.accessToken}")
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                parseStorageInfo(service, responseBody)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun cleanupExpiredFiles(): Int {
        var cleanedCount = 0
        
        CloudService.values().forEach { service ->
            try {
                val files = listFiles(service)
                val expiredFiles = files.filter { it.isExpired() }
                
                expiredFiles.forEach { file ->
                    if (deleteFile(file)) {
                        cleanedCount++
                    }
                }
            } catch (e: Exception) {
                // Continue with other services
            }
        }
        
        return cleanedCount
    }
    
    override suspend fun listFiles(service: CloudService): List<EncryptedLink> {
        return try {
            val token = cloudAuthManager.getToken(service) ?: return emptyList()
            
            val listUrl = getListFilesUrl(service)
            val request = Request.Builder()
                .url(listUrl)
                .addHeader("Authorization", "Bearer ${token.accessToken}")
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                parseFileList(service, responseBody)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun generateShareableLink(encryptedLink: EncryptedLink): String {
        val linkData = json.encodeToString(encryptedLink)
        val encodedData = android.util.Base64.encodeToString(
            linkData.toByteArray(),
            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
        )
        return "chain://file/$encodedData"
    }
    
    override suspend fun parseShareableLink(shareableLink: String): EncryptedLink? {
        return try {
            if (!shareableLink.startsWith("chain://file/")) return null
            
            val encodedData = shareableLink.removePrefix("chain://file/")
            val linkData = String(
                android.util.Base64.decode(encodedData, android.util.Base64.URL_SAFE)
            )
            
            json.decodeFromString<EncryptedLink>(linkData)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getUploadUrl(service: CloudService, fileName: String, token: AuthToken): String {
        return when (service) {
            CloudService.GOOGLE_DRIVE -> {
                "https://www.googleapis.com/upload/drive/v3/files?uploadType=media"
            }
            CloudService.ONEDRIVE -> {
                "https://graph.microsoft.com/v1.0/me/drive/root:/$CHAIN_FOLDER_NAME/$fileName:/content"
            }
            CloudService.DROPBOX -> {
                "https://content.dropboxapi.com/2/files/upload"
            }
            CloudService.ICLOUD -> {
                "https://www.icloud.com/documents/upload"
            }
        }
    }
    
    private fun getDeleteUrl(service: CloudService, fileUrl: String): String {
        return when (service) {
            CloudService.GOOGLE_DRIVE -> {
                // Extract file ID from URL and create delete URL
                val fileId = extractFileId(fileUrl)
                "https://www.googleapis.com/drive/v3/files/$fileId"
            }
            CloudService.ONEDRIVE -> {
                fileUrl // OneDrive uses the same URL for DELETE
            }
            CloudService.DROPBOX -> {
                "https://api.dropboxapi.com/2/files/delete_v2"
            }
            CloudService.ICLOUD -> {
                fileUrl // iCloud uses the same URL for DELETE
            }
        }
    }
    
    private fun getStorageInfoUrl(service: CloudService): String {
        return when (service) {
            CloudService.GOOGLE_DRIVE -> {
                "https://www.googleapis.com/drive/v3/about?fields=storageQuota"
            }
            CloudService.ONEDRIVE -> {
                "https://graph.microsoft.com/v1.0/me/drive"
            }
            CloudService.DROPBOX -> {
                "https://api.dropboxapi.com/2/users/get_space_usage"
            }
            CloudService.ICLOUD -> {
                "https://www.icloud.com/documents/storage"
            }
        }
    }
    
    private fun getListFilesUrl(service: CloudService): String {
        return when (service) {
            CloudService.GOOGLE_DRIVE -> {
                "https://www.googleapis.com/drive/v3/files?q=parents+in+'$CHAIN_FOLDER_NAME'"
            }
            CloudService.ONEDRIVE -> {
                "https://graph.microsoft.com/v1.0/me/drive/root:/$CHAIN_FOLDER_NAME:/children"
            }
            CloudService.DROPBOX -> {
                "https://api.dropboxapi.com/2/files/list_folder"
            }
            CloudService.ICLOUD -> {
                "https://www.icloud.com/documents/list"
            }
        }
    }
    
    private suspend fun performUpload(
        file: File,
        uploadUrl: String,
        token: AuthToken,
        onProgress: suspend (UploadProgress) -> Unit
    ): UploadResponse {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = file.asRequestBody("application/octet-stream".toMediaType())
                val request = Request.Builder()
                    .url(uploadUrl)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer ${token.accessToken}")
                    .build()
                
                val response = httpClient.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val fileUrl = parseUploadResponse(responseBody)
                    UploadResponse(true, fileUrl, null)
                } else {
                    UploadResponse(false, "", "HTTP ${response.code}: ${response.message}")
                }
            } catch (e: IOException) {
                UploadResponse(false, "", e.message ?: "Upload failed")
            }
        }
    }
    
    private suspend fun performDownload(
        downloadUrl: String,
        outputFile: File,
        token: AuthToken,
        onProgress: suspend (DownloadProgress) -> Unit
    ): DownloadResponse {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(downloadUrl)
                    .addHeader("Authorization", "Bearer ${token.accessToken}")
                    .build()
                
                val response = httpClient.newCall(request).execute()
                
                if (response.isSuccessful) {
                    response.body?.let { body ->
                        val contentLength = body.contentLength()
                        body.byteStream().use { input ->
                            outputFile.outputStream().use { output ->
                                val buffer = ByteArray(8192)
                                var bytesRead: Int
                                var totalBytesRead = 0L
                                
                                while (input.read(buffer).also { bytesRead = it } != -1) {
                                    output.write(buffer, 0, bytesRead)
                                    totalBytesRead += bytesRead
                                    
                                    onProgress(DownloadProgress(totalBytesRead, contentLength))
                                }
                            }
                        }
                        DownloadResponse(true, null)
                    } ?: DownloadResponse(false, "Empty response body")
                } else {
                    DownloadResponse(false, "HTTP ${response.code}: ${response.message}")
                }
            } catch (e: IOException) {
                DownloadResponse(false, e.message ?: "Download failed")
            }
        }
    }
    
    private fun parseUploadResponse(responseBody: String?): String {
        // Parse response based on service to extract file URL
        // This is a simplified implementation
        return responseBody?.let { body ->
            try {
                val jsonElement = json.parseToJsonElement(body)
                when {
                    jsonElement.jsonObject.containsKey("id") -> {
                        // Google Drive response
                        jsonElement.jsonObject["id"]?.jsonPrimitive?.content ?: ""
                    }
                    jsonElement.jsonObject.containsKey("webUrl") -> {
                        // OneDrive response
                        jsonElement.jsonObject["webUrl"]?.jsonPrimitive?.content ?: ""
                    }
                    else -> ""
                }
            } catch (e: Exception) {
                ""
            }
        } ?: ""
    }
    
    private fun parseStorageInfo(service: CloudService, responseBody: String?): StorageInfo? {
        return try {
            responseBody?.let { body ->
                val jsonElement = json.parseToJsonElement(body)
                val jsonObject = jsonElement.jsonObject
                
                when (service) {
                    CloudService.GOOGLE_DRIVE -> {
                        val quota = jsonObject["storageQuota"]?.jsonObject
                        val limit = quota?.get("limit")?.jsonPrimitive?.longOrNull ?: 0L
                        val usage = quota?.get("usage")?.jsonPrimitive?.longOrNull ?: 0L
                        
                        StorageInfo(
                            totalSpace = limit,
                            usedSpace = usage,
                            availableSpace = limit - usage,
                            service = service
                        )
                    }
                    CloudService.ONEDRIVE -> {
                        val quota = jsonObject["quota"]?.jsonObject
                        val total = quota?.get("total")?.jsonPrimitive?.longOrNull ?: 0L
                        val used = quota?.get("used")?.jsonPrimitive?.longOrNull ?: 0L
                        
                        StorageInfo(
                            totalSpace = total,
                            usedSpace = used,
                            availableSpace = total - used,
                            service = service
                        )
                    }
                    else -> null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseFileList(service: CloudService, responseBody: String?): List<EncryptedLink> {
        // This would parse the file list response from each service
        // For now, return empty list as this would require service-specific parsing
        return emptyList()
    }
    
    private fun extractFileId(fileUrl: String): String {
        // Extract file ID from various cloud service URLs
        return when {
            fileUrl.contains("drive.google.com") -> {
                // Extract Google Drive file ID
                fileUrl.substringAfter("/d/").substringBefore("/")
            }
            else -> fileUrl
        }
    }
    
    private fun getMimeType(file: File): String {
        val extension = file.extension.lowercase()
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "mp4" -> "video/mp4"
            "mp3" -> "audio/mpeg"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }
}

// Helper data classes
private data class UploadProgress(val bytesUploaded: Long, val totalBytes: Long)
private data class DownloadProgress(val bytesDownloaded: Long, val totalBytes: Long)
private data class UploadResponse(val isSuccess: Boolean, val url: String, val error: String?)
private data class DownloadResponse(val isSuccess: Boolean, val error: String?)
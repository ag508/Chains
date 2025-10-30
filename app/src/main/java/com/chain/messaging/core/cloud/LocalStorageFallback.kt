package com.chain.messaging.core.cloud

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.Instant
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides local storage fallback when cloud services are unavailable
 */
@Singleton
class LocalStorageFallback @Inject constructor(
    private val context: Context,
    private val fileEncryption: FileEncryption
) {
    
    companion object {
        private const val LOCAL_STORAGE_DIR = "local_files"
        private const val PENDING_UPLOADS_DIR = "pending_uploads"
        private const val MAX_LOCAL_STORAGE_MB = 500 // 500MB limit for local storage
    }
    
    private val localStorageDir: File by lazy {
        File(context.filesDir, LOCAL_STORAGE_DIR).apply { mkdirs() }
    }
    
    private val pendingUploadsDir: File by lazy {
        File(context.filesDir, PENDING_UPLOADS_DIR).apply { mkdirs() }
    }
    
    /**
     * Store file locally when cloud storage is unavailable
     */
    suspend fun storeFileLocally(
        file: File,
        expirationHours: Int = 24
    ): Flow<UploadResult> = flow {
        try {
            // Check available space
            val availableSpace = getAvailableLocalSpace()
            if (file.length() > availableSpace) {
                emit(UploadResult.Error("Insufficient local storage space"))
                return@flow
            }
            
            // Generate unique filename
            val uniqueId = UUID.randomUUID().toString()
            val encryptedFile = File(localStorageDir, "$uniqueId.enc")
            
            // Encrypt and store file
            val encryptionResult = fileEncryption.encryptFile(file, encryptedFile)
            if (encryptionResult !is EncryptionResult.Success) {
                emit(UploadResult.Error("File encryption failed"))
                return@flow
            }
            
            // Create local encrypted link
            val encryptedLink = EncryptedLink(
                url = "local://$uniqueId",
                encryptionKey = encryptionResult.encryptionKey,
                service = CloudService.GOOGLE_DRIVE, // Default service for later upload
                expiresAt = Instant.now().plusSeconds(expirationHours * 3600L),
                fileName = file.name,
                fileSize = file.length(),
                mimeType = getMimeType(file),
                checksum = encryptionResult.checksum
            )
            
            // Store metadata
            storeFileMetadata(uniqueId, encryptedLink)
            
            emit(UploadResult.Success(encryptedLink))
            
        } catch (e: Exception) {
            emit(UploadResult.Error("Local storage failed: ${e.message}", e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Retrieve file from local storage
     */
    suspend fun retrieveLocalFile(
        encryptedLink: EncryptedLink,
        outputFile: File
    ): Flow<DownloadResult> = flow {
        try {
            if (!encryptedLink.url.startsWith("local://")) {
                emit(DownloadResult.Error("Not a local file"))
                return@flow
            }
            
            val uniqueId = encryptedLink.url.removePrefix("local://")
            val encryptedFile = File(localStorageDir, "$uniqueId.enc")
            
            if (!encryptedFile.exists()) {
                emit(DownloadResult.Error("Local file not found"))
                return@flow
            }
            
            // Check if file has expired
            if (encryptedLink.isExpired()) {
                // Clean up expired file
                encryptedFile.delete()
                removeFileMetadata(uniqueId)
                emit(DownloadResult.Error("File has expired"))
                return@flow
            }
            
            // Decrypt file
            val decryptionResult = fileEncryption.decryptFile(
                encryptedFile,
                outputFile,
                encryptedLink.encryptionKey
            )
            
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
            emit(DownloadResult.Error("Local retrieval failed: ${e.message}", e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Queue file for upload when cloud service becomes available
     */
    suspend fun queueForUpload(
        file: File,
        targetService: CloudService,
        expirationHours: Int = 24
    ): String = withContext(Dispatchers.IO) {
        val uniqueId = UUID.randomUUID().toString()
        val queuedFile = File(pendingUploadsDir, uniqueId)
        
        // Copy file to pending uploads directory
        file.copyTo(queuedFile)
        
        // Store upload metadata
        val uploadMetadata = PendingUpload(
            id = uniqueId,
            originalFileName = file.name,
            targetService = targetService,
            expirationHours = expirationHours,
            queuedAt = Instant.now(),
            filePath = queuedFile.absolutePath
        )
        
        storeUploadMetadata(uniqueId, uploadMetadata)
        uniqueId
    }
    
    /**
     * Get all pending uploads
     */
    suspend fun getPendingUploads(): List<PendingUpload> = withContext(Dispatchers.IO) {
        val pendingUploads = mutableListOf<PendingUpload>()
        
        pendingUploadsDir.listFiles()?.forEach { file ->
            if (file.isFile && !file.name.endsWith(".meta")) {
                val uniqueId = file.nameWithoutExtension
                val metadata = getUploadMetadata(uniqueId)
                if (metadata != null) {
                    pendingUploads.add(metadata)
                }
            }
        }
        
        pendingUploads
    }
    
    /**
     * Remove pending upload after successful upload
     */
    suspend fun removePendingUpload(uploadId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(pendingUploadsDir, uploadId)
            val metaFile = File(pendingUploadsDir, "$uploadId.meta")
            
            file.delete()
            metaFile.delete()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Clean up expired local files
     */
    suspend fun cleanupExpiredFiles(): Int = withContext(Dispatchers.IO) {
        var cleanedCount = 0
        
        localStorageDir.listFiles()?.forEach { file ->
            if (file.isFile && file.name.endsWith(".enc")) {
                val uniqueId = file.nameWithoutExtension
                val metadata = getFileMetadata(uniqueId)
                
                if (metadata?.isExpired() == true) {
                    file.delete()
                    removeFileMetadata(uniqueId)
                    cleanedCount++
                }
            }
        }
        
        // Clean up expired pending uploads
        pendingUploadsDir.listFiles()?.forEach { file ->
            if (file.isFile && !file.name.endsWith(".meta")) {
                val uniqueId = file.nameWithoutExtension
                val metadata = getUploadMetadata(uniqueId)
                
                // Remove uploads older than 7 days
                if (metadata?.queuedAt?.isBefore(Instant.now().minusSeconds(7 * 24 * 3600)) == true) {
                    removePendingUpload(uniqueId)
                    cleanedCount++
                }
            }
        }
        
        cleanedCount
    }
    
    /**
     * Get local storage usage information
     */
    suspend fun getLocalStorageInfo(): LocalStorageInfo = withContext(Dispatchers.IO) {
        var totalSize = 0L
        var fileCount = 0
        
        fun calculateDirectorySize(dir: File) {
            dir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    totalSize += file.length()
                    fileCount++
                } else if (file.isDirectory) {
                    calculateDirectorySize(file)
                }
            }
        }
        
        calculateDirectorySize(localStorageDir)
        calculateDirectorySize(pendingUploadsDir)
        
        val maxSize = MAX_LOCAL_STORAGE_MB * 1024 * 1024L
        val availableSize = maxSize - totalSize
        
        LocalStorageInfo(
            totalSize = totalSize,
            availableSize = availableSize.coerceAtLeast(0),
            maxSize = maxSize,
            fileCount = fileCount,
            usagePercentage = if (maxSize > 0) (totalSize.toFloat() / maxSize.toFloat()) * 100f else 0f
        )
    }
    
    /**
     * Check if cloud service is available
     */
    suspend fun isCloudServiceAvailable(service: CloudService): Boolean {
        return try {
            // This would implement actual connectivity check
            // For now, return true as placeholder
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getAvailableLocalSpace(): Long {
        val maxSize = MAX_LOCAL_STORAGE_MB * 1024 * 1024L
        var usedSize = 0L
        
        fun calculateUsedSize(dir: File) {
            dir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    usedSize += file.length()
                } else if (file.isDirectory) {
                    calculateUsedSize(file)
                }
            }
        }
        
        calculateUsedSize(localStorageDir)
        calculateUsedSize(pendingUploadsDir)
        
        return (maxSize - usedSize).coerceAtLeast(0)
    }
    
    private fun storeFileMetadata(uniqueId: String, encryptedLink: EncryptedLink) {
        val metaFile = File(localStorageDir, "$uniqueId.meta")
        metaFile.writeText(encryptedLink.toString()) // In real implementation, use proper serialization
    }
    
    private fun getFileMetadata(uniqueId: String): EncryptedLink? {
        return try {
            val metaFile = File(localStorageDir, "$uniqueId.meta")
            if (metaFile.exists()) {
                // In real implementation, deserialize properly
                null // Placeholder
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun removeFileMetadata(uniqueId: String) {
        val metaFile = File(localStorageDir, "$uniqueId.meta")
        metaFile.delete()
    }
    
    private fun storeUploadMetadata(uniqueId: String, uploadMetadata: PendingUpload) {
        val metaFile = File(pendingUploadsDir, "$uniqueId.meta")
        metaFile.writeText(uploadMetadata.toString()) // In real implementation, use proper serialization
    }
    
    private fun getUploadMetadata(uniqueId: String): PendingUpload? {
        return try {
            val metaFile = File(pendingUploadsDir, "$uniqueId.meta")
            if (metaFile.exists()) {
                // In real implementation, deserialize properly
                null // Placeholder
            } else {
                null
            }
        } catch (e: Exception) {
            null
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

/**
 * Information about local storage usage
 */
data class LocalStorageInfo(
    val totalSize: Long,
    val availableSize: Long,
    val maxSize: Long,
    val fileCount: Int,
    val usagePercentage: Float
)

/**
 * Represents a file queued for upload
 */
data class PendingUpload(
    val id: String,
    val originalFileName: String,
    val targetService: CloudService,
    val expirationHours: Int,
    val queuedAt: Instant,
    val filePath: String
)
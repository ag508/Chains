package com.chain.messaging.data.local.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.chain.messaging.data.local.dao.MediaDao
import com.chain.messaging.data.local.entity.MediaEntity
import com.chain.messaging.data.local.entity.Media
import com.chain.messaging.data.local.entity.toDomain
import com.chain.messaging.data.local.entity.toEntity
import com.chain.messaging.core.security.FileEncryption
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing local file and media storage with encryption and compression
 */
@Singleton
class MediaStorageService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaDao: MediaDao,
    private val fileEncryption: FileEncryption,
    private val mediaCompressor: MediaCompressor,
    private val thumbnailGenerator: ThumbnailGenerator
) {
    
    companion object {
        private const val MEDIA_DIR = "media"
        private const val THUMBNAILS_DIR = "thumbnails"
        private const val TEMP_DIR = "temp"
        private const val MAX_STORAGE_SIZE_MB = 500L // 500MB default limit
    }
    
    private val mediaDirectory: File by lazy {
        File(context.filesDir, MEDIA_DIR).apply { mkdirs() }
    }
    
    private val thumbnailDirectory: File by lazy {
        File(context.filesDir, THUMBNAILS_DIR).apply { mkdirs() }
    }
    
    private val tempDirectory: File by lazy {
        File(context.cacheDir, TEMP_DIR).apply { mkdirs() }
    }
    
    /**
     * Store media file with encryption and compression
     */
    suspend fun storeMedia(
        messageId: String,
        sourceUri: Uri,
        fileName: String,
        mimeType: String,
        compress: Boolean = true
    ): Result<Media> = withContext(Dispatchers.IO) {
        try {
            val mediaId = UUID.randomUUID().toString()
            val fileExtension = fileName.substringAfterLast('.', "")
            val secureFileName = "${mediaId}.${fileExtension}"
            val mediaFile = File(mediaDirectory, secureFileName)
            
            // Copy and optionally compress the file
            val processedFile = if (compress && shouldCompress(mimeType)) {
                mediaCompressor.compressMedia(sourceUri, mimeType, tempDirectory)
            } else {
                copyFileFromUri(sourceUri, File(tempDirectory, secureFileName))
            }
            
            // Get media metadata
            val metadata = extractMediaMetadata(processedFile, mimeType)
            
            // Encrypt the file
            val encryptionKey = fileEncryption.encryptFile(processedFile, mediaFile)
            
            // Generate thumbnail if applicable
            val thumbnailPath = if (isImageOrVideo(mimeType)) {
                generateThumbnail(processedFile, mediaId, mimeType)
            } else null
            
            // Create media entity
            val media = Media(
                id = mediaId,
                messageId = messageId,
                fileName = fileName,
                filePath = mediaFile.absolutePath,
                mimeType = mimeType,
                fileSize = mediaFile.length(),
                width = metadata.width,
                height = metadata.height,
                duration = metadata.duration,
                thumbnailPath = thumbnailPath,
                isEncrypted = true,
                encryptionKey = encryptionKey
            )
            
            // Store in database
            mediaDao.insertMedia(media.toEntity())
            
            // Clean up temp file
            processedFile.delete()
            
            Result.success(media)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Retrieve media file and decrypt if necessary
     */
    suspend fun getMedia(mediaId: String): Result<File?> = withContext(Dispatchers.IO) {
        try {
            val mediaEntity = mediaDao.getMediaById(mediaId)
            if (mediaEntity == null) {
                return@withContext Result.success(null)
            }
            
            val encryptedFile = File(mediaEntity.filePath)
            if (!encryptedFile.exists()) {
                return@withContext Result.success(null)
            }
            
            if (mediaEntity.isEncrypted && mediaEntity.encryptionKey != null) {
                // Decrypt to temp file
                val tempFile = File(tempDirectory, "decrypted_${mediaEntity.id}")
                fileEncryption.decryptFile(encryptedFile, tempFile, mediaEntity.encryptionKey)
                Result.success(tempFile)
            } else {
                Result.success(encryptedFile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get media metadata without decrypting the file
     */
    suspend fun getMediaInfo(mediaId: String): Result<Media?> {
        return try {
            val mediaEntity = mediaDao.getMediaById(mediaId)
            Result.success(mediaEntity?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all media for a message
     */
    suspend fun getMediaForMessage(messageId: String): Result<List<Media>> {
        return try {
            val mediaEntities = mediaDao.getMediaByMessageId(messageId)
            val mediaList = mediaEntities.map { it.toDomain() }
            Result.success(mediaList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get thumbnail file
     */
    suspend fun getThumbnail(mediaId: String): Result<File?> = withContext(Dispatchers.IO) {
        try {
            val mediaEntity = mediaDao.getMediaById(mediaId)
            if (mediaEntity?.thumbnailPath == null) {
                return@withContext Result.success(null)
            }
            
            val thumbnailFile = File(mediaEntity.thumbnailPath)
            if (thumbnailFile.exists()) {
                Result.success(thumbnailFile)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete media file and its thumbnail
     */
    suspend fun deleteMedia(mediaId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val mediaEntity = mediaDao.getMediaById(mediaId)
            if (mediaEntity != null) {
                // Delete media file
                val mediaFile = File(mediaEntity.filePath)
                if (mediaFile.exists()) {
                    mediaFile.delete()
                }
                
                // Delete thumbnail
                mediaEntity.thumbnailPath?.let { thumbnailPath ->
                    val thumbnailFile = File(thumbnailPath)
                    if (thumbnailFile.exists()) {
                        thumbnailFile.delete()
                    }
                }
                
                // Remove from database
                mediaDao.deleteMediaById(mediaId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get storage usage statistics
     */
    suspend fun getStorageStats(): Result<StorageStats> {
        return try {
            val totalSize = mediaDao.getTotalStorageUsed() ?: 0L
            val mediaCount = mediaDao.getMediaCount()
            val availableSpace = getAvailableStorageSpace()
            
            val stats = StorageStats(
                totalSizeBytes = totalSize,
                totalFiles = mediaCount,
                availableSpaceBytes = availableSpace,
                maxStorageSizeBytes = MAX_STORAGE_SIZE_MB * 1024 * 1024
            )
            
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clean up old media files to free space
     */
    suspend fun cleanupOldMedia(olderThanDays: Int = 30): Result<CleanupResult> = withContext(Dispatchers.IO) {
        try {
            val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
            val oldMediaFiles = mediaDao.getOldMediaFiles(cutoffTime)
            
            var deletedFiles = 0
            var freedSpace = 0L
            
            oldMediaFiles.forEach { mediaEntity ->
                val mediaFile = File(mediaEntity.filePath)
                if (mediaFile.exists()) {
                    freedSpace += mediaFile.length()
                    mediaFile.delete()
                    deletedFiles++
                }
                
                // Delete thumbnail
                mediaEntity.thumbnailPath?.let { thumbnailPath ->
                    val thumbnailFile = File(thumbnailPath)
                    if (thumbnailFile.exists()) {
                        thumbnailFile.delete()
                    }
                }
            }
            
            // Remove from database
            val deletedCount = mediaDao.deleteOldMedia(cutoffTime)
            
            val result = CleanupResult(
                deletedFiles = deletedFiles,
                freedSpaceBytes = freedSpace,
                deletedFromDatabase = deletedCount
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clean up large files to free space
     */
    suspend fun cleanupLargeFiles(minSizeMB: Long = 10): Result<CleanupResult> = withContext(Dispatchers.IO) {
        try {
            val minSizeBytes = minSizeMB * 1024 * 1024
            val largeFiles = mediaDao.getLargeMediaFiles(minSizeBytes)
            
            var deletedFiles = 0
            var freedSpace = 0L
            
            largeFiles.forEach { mediaEntity ->
                val mediaFile = File(mediaEntity.filePath)
                if (mediaFile.exists()) {
                    freedSpace += mediaFile.length()
                    mediaFile.delete()
                    deletedFiles++
                }
                
                // Delete thumbnail
                mediaEntity.thumbnailPath?.let { thumbnailPath ->
                    val thumbnailFile = File(thumbnailPath)
                    if (thumbnailFile.exists()) {
                        thumbnailFile.delete()
                    }
                }
                
                // Remove from database
                mediaDao.deleteMediaById(mediaEntity.id)
            }
            
            val result = CleanupResult(
                deletedFiles = deletedFiles,
                freedSpaceBytes = freedSpace,
                deletedFromDatabase = largeFiles.size
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get media files by type
     */
    suspend fun getMediaByType(mimeTypePattern: String): Result<List<Media>> {
        return try {
            val mediaEntities = mediaDao.getMediaByType(mimeTypePattern)
            val mediaList = mediaEntities.map { it.toDomain() }
            Result.success(mediaList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun copyFileFromUri(sourceUri: Uri, destFile: File): File {
        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return destFile
    }
    
    private fun extractMediaMetadata(file: File, mimeType: String): MediaMetadata {
        return when {
            mimeType.startsWith("image/") -> extractImageMetadata(file)
            mimeType.startsWith("video/") -> extractVideoMetadata(file)
            mimeType.startsWith("audio/") -> extractAudioMetadata(file)
            else -> MediaMetadata()
        }
    }
    
    private fun extractImageMetadata(file: File): MediaMetadata {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
            MediaMetadata(width = options.outWidth, height = options.outHeight)
        } catch (e: Exception) {
            MediaMetadata()
        }
    }
    
    private fun extractVideoMetadata(file: File): MediaMetadata {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            
            retriever.release()
            MediaMetadata(width = width, height = height, duration = duration)
        } catch (e: Exception) {
            MediaMetadata()
        }
    }
    
    private fun extractAudioMetadata(file: File): MediaMetadata {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            
            retriever.release()
            MediaMetadata(duration = duration)
        } catch (e: Exception) {
            MediaMetadata()
        }
    }
    
    private suspend fun generateThumbnail(file: File, mediaId: String, mimeType: String): String? {
        return try {
            val thumbnailFile = File(thumbnailDirectory, "${mediaId}_thumb.jpg")
            val success = thumbnailGenerator.generateThumbnail(file, thumbnailFile, mimeType)
            if (success) thumbnailFile.absolutePath else null
        } catch (e: Exception) {
            null
        }
    }
    
    private fun shouldCompress(mimeType: String): Boolean {
        return mimeType.startsWith("image/") || mimeType.startsWith("video/")
    }
    
    private fun isImageOrVideo(mimeType: String): Boolean {
        return mimeType.startsWith("image/") || mimeType.startsWith("video/")
    }
    
    private fun getAvailableStorageSpace(): Long {
        return try {
            context.filesDir.freeSpace
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * Media metadata data class
 */
data class MediaMetadata(
    val width: Int? = null,
    val height: Int? = null,
    val duration: Long? = null
)

/**
 * Storage statistics data class
 */
data class StorageStats(
    val totalSizeBytes: Long,
    val totalFiles: Int,
    val availableSpaceBytes: Long,
    val maxStorageSizeBytes: Long
) {
    val usagePercentage: Float
        get() = if (maxStorageSizeBytes > 0) {
            (totalSizeBytes.toFloat() / maxStorageSizeBytes.toFloat()) * 100f
        } else 0f
    
    val isNearLimit: Boolean
        get() = usagePercentage > 80f
}

/**
 * Cleanup result data class
 */
data class CleanupResult(
    val deletedFiles: Int,
    val freedSpaceBytes: Long,
    val deletedFromDatabase: Int
)
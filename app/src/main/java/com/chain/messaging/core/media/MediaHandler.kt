package com.chain.messaging.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.chain.messaging.domain.model.MediaMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling media file operations
 */
@Singleton
class MediaHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Process a media URI and create a MediaMessage
     */
    suspend fun processMedia(uri: Uri): Result<MediaMessage> = withContext(Dispatchers.IO) {
        try {
            val mediaInfo = getMediaInfo(uri)
            val localFile = copyToLocalStorage(uri, mediaInfo.fileName)
            
            Result.success(
                MediaMessage(
                    uri = localFile.absolutePath,
                    fileName = mediaInfo.fileName,
                    mimeType = mediaInfo.mimeType,
                    fileSize = mediaInfo.fileSize,
                    duration = mediaInfo.duration,
                    width = mediaInfo.width,
                    height = mediaInfo.height,
                    thumbnailUri = null, // Will be generated if needed
                    isLocal = true
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get media information from URI
     */
    private fun getMediaInfo(uri: Uri): MediaInfo {
        val contentResolver = context.contentResolver
        
        // Get basic file info
        val cursor = contentResolver.query(uri, null, null, null, null)
        var fileName = "unknown_file"
        var fileSize = 0L
        
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(MediaStore.MediaColumns.SIZE)
                
                if (nameIndex >= 0) {
                    fileName = it.getString(nameIndex) ?: "unknown_file"
                }
                if (sizeIndex >= 0) {
                    fileSize = it.getLong(sizeIndex)
                }
            }
        }
        
        // Get MIME type
        val mimeType = contentResolver.getType(uri) ?: getMimeTypeFromFileName(fileName)
        
        // Get media-specific info
        var duration: Long? = null
        var width: Int? = null
        var height: Int? = null
        
        when {
            mimeType.startsWith("image/") -> {
                val (w, h) = getImageDimensions(uri)
                width = w
                height = h
            }
            mimeType.startsWith("video/") -> {
                val mediaInfo = getVideoInfo(uri)
                duration = mediaInfo.first
                width = mediaInfo.second
                height = mediaInfo.third
            }
            mimeType.startsWith("audio/") -> {
                duration = getAudioDuration(uri)
            }
        }
        
        return MediaInfo(
            fileName = fileName,
            mimeType = mimeType,
            fileSize = fileSize,
            duration = duration,
            width = width,
            height = height
        )
    }
    
    /**
     * Copy file from URI to local app storage
     */
    private fun copyToLocalStorage(uri: Uri, fileName: String): File {
        val mediaDir = File(context.filesDir, "media")
        if (!mediaDir.exists()) {
            mediaDir.mkdirs()
        }
        
        val localFile = File(mediaDir, "${System.currentTimeMillis()}_$fileName")
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(localFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        
        return localFile
    }
    
    /**
     * Get image dimensions
     */
    private fun getImageDimensions(uri: Uri): Pair<Int?, Int?> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val options = android.graphics.BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
                Pair(options.outWidth, options.outHeight)
            } ?: Pair(null, null)
        } catch (e: Exception) {
            Pair(null, null)
        }
    }
    
    /**
     * Get video information (duration, width, height)
     */
    private fun getVideoInfo(uri: Uri): Triple<Long?, Int?, Int?> {
        return try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            
            val duration = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            val width = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
            val height = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
            
            retriever.release()
            Triple(duration, width, height)
        } catch (e: Exception) {
            Triple(null, null, null)
        }
    }
    
    /**
     * Get audio duration
     */
    private fun getAudioDuration(uri: Uri): Long? {
        return try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            
            val duration = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            retriever.release()
            duration
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get MIME type from file name
     */
    private fun getMimeTypeFromFileName(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) ?: "application/octet-stream"
    }
    
    /**
     * Delete media file
     */
    suspend fun deleteMedia(mediaMessage: MediaMessage): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (mediaMessage.isLocal) {
                val file = File(mediaMessage.uri)
                if (file.exists()) {
                    file.delete()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get file from MediaMessage
     */
    fun getFile(mediaMessage: MediaMessage): File? {
        return if (mediaMessage.isLocal) {
            val file = File(mediaMessage.uri)
            if (file.exists()) file else null
        } else {
            null
        }
    }
    
    /**
     * Generate thumbnail for media
     */
    suspend fun generateThumbnail(mediaMessage: MediaMessage): Result<File?> = withContext(Dispatchers.IO) {
        try {
            val sourceFile = getFile(mediaMessage) ?: return@withContext Result.failure(
                IllegalArgumentException("Source file not found")
            )
            
            val thumbnailDir = File(context.filesDir, "thumbnails")
            if (!thumbnailDir.exists()) {
                thumbnailDir.mkdirs()
            }
            
            val thumbnailFile = File(thumbnailDir, "thumb_${System.currentTimeMillis()}.jpg")
            
            val success = when {
                mediaMessage.mimeType.startsWith("image/") -> generateImageThumbnail(sourceFile, thumbnailFile)
                mediaMessage.mimeType.startsWith("video/") -> generateVideoThumbnail(sourceFile, thumbnailFile)
                else -> false
            }
            
            if (success) {
                Result.success(thumbnailFile)
            } else {
                Result.failure(Exception("Failed to generate thumbnail"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate image thumbnail
     */
    private fun generateImageThumbnail(sourceFile: File, thumbnailFile: File): Boolean {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(sourceFile.absolutePath, options)
            
            val sampleSize = calculateThumbnailSampleSize(options.outWidth, options.outHeight)
            
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inJustDecodeBounds = false
            }
            
            val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, decodeOptions)
            bitmap?.let { bmp ->
                val thumbnail = android.media.ThumbnailUtils.extractThumbnail(
                    bmp,
                    200,
                    200,
                    android.media.ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                )
                
                FileOutputStream(thumbnailFile).use { outputStream ->
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                }
                
                thumbnail.recycle()
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate video thumbnail
     */
    private fun generateVideoThumbnail(sourceFile: File, thumbnailFile: File): Boolean {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(sourceFile.absolutePath)
            
            val bitmap = retriever.getFrameAtTime(
                1000000L, // 1 second
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )
            
            retriever.release()
            
            bitmap?.let { bmp ->
                val thumbnail = android.media.ThumbnailUtils.extractThumbnail(
                    bmp,
                    200,
                    200,
                    android.media.ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                )
                
                FileOutputStream(thumbnailFile).use { outputStream ->
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                }
                
                thumbnail.recycle()
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Calculate sample size for thumbnail generation
     */
    private fun calculateThumbnailSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        val thumbnailSize = 200
        
        if (height > thumbnailSize || width > thumbnailSize) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / sampleSize) >= thumbnailSize && 
                   (halfWidth / sampleSize) >= thumbnailSize) {
                sampleSize *= 2
            }
        }
        
        return sampleSize
    }
    
    /**
     * Compress media file
     */
    suspend fun compressMedia(uri: Uri, mimeType: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val outputDir = File(context.filesDir, "compressed")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            
            val compressedFile = when {
                mimeType.startsWith("image/") -> compressImageFile(uri, outputDir)
                mimeType.startsWith("video/") -> compressVideoFile(uri, outputDir)
                else -> {
                    // For other types, just copy the file
                    val fileName = "file_${System.currentTimeMillis()}"
                    val outputFile = File(outputDir, fileName)
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        FileOutputStream(outputFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    outputFile
                }
            }
            
            Result.success(compressedFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Compress image file
     */
    private fun compressImageFile(uri: Uri, outputDir: File): File {
        val outputFile = File(outputDir, "compressed_image_${System.currentTimeMillis()}.jpg")
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            
            val sampleSize = calculateCompressionSampleSize(options.outWidth, options.outHeight)
            
            context.contentResolver.openInputStream(uri)?.use { inputStream2 ->
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inJustDecodeBounds = false
                }
                
                val bitmap = BitmapFactory.decodeStream(inputStream2, null, decodeOptions)
                bitmap?.let { bmp ->
                    val resizedBitmap = resizeBitmapForCompression(bmp)
                    
                    FileOutputStream(outputFile).use { outputStream ->
                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                    }
                    
                    if (resizedBitmap != bmp) {
                        bmp.recycle()
                    }
                    resizedBitmap.recycle()
                }
            }
        }
        
        return outputFile
    }
    
    /**
     * Compress video file (simplified version)
     */
    private fun compressVideoFile(uri: Uri, outputDir: File): File {
        val outputFile = File(outputDir, "compressed_video_${System.currentTimeMillis()}.mp4")
        
        // For now, just copy the file - full video compression would require more complex implementation
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(outputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        
        return outputFile
    }
    
    /**
     * Calculate sample size for compression
     */
    private fun calculateCompressionSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        val maxWidth = 1920
        val maxHeight = 1080
        
        if (height > maxHeight || width > maxWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / sampleSize) >= maxHeight && 
                   (halfWidth / sampleSize) >= maxWidth) {
                sampleSize *= 2
            }
        }
        
        return sampleSize
    }
    
    /**
     * Resize bitmap for compression
     */
    private fun resizeBitmapForCompression(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val maxWidth = 1920
        val maxHeight = 1080
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val aspectRatio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        
        if (aspectRatio > 1) {
            newWidth = maxWidth
            newHeight = (maxWidth / aspectRatio).toInt()
        } else {
            newHeight = maxHeight
            newWidth = (maxHeight * aspectRatio).toInt()
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Check if media should be compressed
     */
    fun shouldCompress(mimeType: String, fileSizeBytes: Long): Boolean {
        return when {
            mimeType.startsWith("image/") -> fileSizeBytes > 1024 * 1024 // 1MB
            mimeType.startsWith("video/") -> fileSizeBytes > 10 * 1024 * 1024 // 10MB
            else -> false
        }
    }
    
    /**
     * Get media file size
     */
    suspend fun getMediaSize(uri: Uri): Long = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.available().toLong()
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * Data class for media information
 */
private data class MediaInfo(
    val fileName: String,
    val mimeType: String,
    val fileSize: Long,
    val duration: Long? = null,
    val width: Int? = null,
    val height: Int? = null
)
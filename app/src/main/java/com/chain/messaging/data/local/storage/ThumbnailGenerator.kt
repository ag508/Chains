package com.chain.messaging.data.local.storage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for generating thumbnails for media files
 */
@Singleton
class ThumbnailGenerator @Inject constructor() {
    
    companion object {
        private const val THUMBNAIL_SIZE = 200
        private const val THUMBNAIL_QUALITY = 80
        private const val VIDEO_THUMBNAIL_TIME_US = 1000000L // 1 second
    }
    
    /**
     * Generate thumbnail for media file
     */
    suspend fun generateThumbnail(
        sourceFile: File,
        thumbnailFile: File,
        mimeType: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            when {
                mimeType.startsWith("image/") -> generateImageThumbnail(sourceFile, thumbnailFile)
                mimeType.startsWith("video/") -> generateVideoThumbnail(sourceFile, thumbnailFile)
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate thumbnail for image file
     */
    private suspend fun generateImageThumbnail(
        sourceFile: File,
        thumbnailFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // First, get image dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(sourceFile.absolutePath, options)
            
            // Calculate sample size for efficient loading
            val sampleSize = calculateSampleSize(options.outWidth, options.outHeight)
            
            // Decode with sample size
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inJustDecodeBounds = false
            }
            
            val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, decodeOptions)
            bitmap?.let { bmp ->
                // Create thumbnail
                val thumbnail = ThumbnailUtils.extractThumbnail(
                    bmp,
                    THUMBNAIL_SIZE,
                    THUMBNAIL_SIZE,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                )
                
                // Save thumbnail
                FileOutputStream(thumbnailFile).use { outputStream ->
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, outputStream)
                }
                
                thumbnail.recycle()
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate thumbnail for video file
     */
    private suspend fun generateVideoThumbnail(
        sourceFile: File,
        thumbnailFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(sourceFile.absolutePath)
            
            // Get frame at 1 second (or first frame if video is shorter)
            val bitmap = retriever.getFrameAtTime(
                VIDEO_THUMBNAIL_TIME_US,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )
            
            retriever.release()
            
            bitmap?.let { bmp ->
                // Create thumbnail
                val thumbnail = ThumbnailUtils.extractThumbnail(
                    bmp,
                    THUMBNAIL_SIZE,
                    THUMBNAIL_SIZE,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                )
                
                // Save thumbnail
                FileOutputStream(thumbnailFile).use { outputStream ->
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, outputStream)
                }
                
                thumbnail.recycle()
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate thumbnail from bitmap
     */
    suspend fun generateThumbnailFromBitmap(
        sourceBitmap: Bitmap,
        thumbnailFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val thumbnail = ThumbnailUtils.extractThumbnail(
                sourceBitmap,
                THUMBNAIL_SIZE,
                THUMBNAIL_SIZE
            )
            
            FileOutputStream(thumbnailFile).use { outputStream ->
                thumbnail.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, outputStream)
            }
            
            thumbnail.recycle()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if thumbnail can be generated for the given mime type
     */
    fun canGenerateThumbnail(mimeType: String): Boolean {
        return mimeType.startsWith("image/") || mimeType.startsWith("video/")
    }
    
    /**
     * Get thumbnail dimensions
     */
    fun getThumbnailSize(): Pair<Int, Int> {
        return Pair(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
    }
    
    /**
     * Calculate sample size for efficient bitmap loading
     */
    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        
        if (height > THUMBNAIL_SIZE || width > THUMBNAIL_SIZE) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / sampleSize) >= THUMBNAIL_SIZE && 
                   (halfWidth / sampleSize) >= THUMBNAIL_SIZE) {
                sampleSize *= 2
            }
        }
        
        return sampleSize
    }
    
    /**
     * Generate multiple thumbnail sizes
     */
    suspend fun generateMultipleThumbnails(
        sourceFile: File,
        mimeType: String,
        sizes: List<Int>
    ): Map<Int, File> = withContext(Dispatchers.IO) {
        val thumbnails = mutableMapOf<Int, File>()
        
        try {
            when {
                mimeType.startsWith("image/") -> {
                    val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath)
                    bitmap?.let { bmp ->
                        sizes.forEach { size ->
                            val thumbnailFile = File(
                                sourceFile.parent,
                                "${sourceFile.nameWithoutExtension}_thumb_${size}.jpg"
                            )
                            
                            val thumbnail = ThumbnailUtils.extractThumbnail(
                                bmp,
                                size,
                                size
                            )
                            
                            FileOutputStream(thumbnailFile).use { outputStream ->
                                thumbnail.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, outputStream)
                            }
                            
                            thumbnail.recycle()
                            thumbnails[size] = thumbnailFile
                        }
                        bmp.recycle()
                    }
                }
                mimeType.startsWith("video/") -> {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(sourceFile.absolutePath)
                    
                    val bitmap = retriever.getFrameAtTime(
                        VIDEO_THUMBNAIL_TIME_US,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )
                    
                    retriever.release()
                    
                    bitmap?.let { bmp ->
                        sizes.forEach { size ->
                            val thumbnailFile = File(
                                sourceFile.parent,
                                "${sourceFile.nameWithoutExtension}_thumb_${size}.jpg"
                            )
                            
                            val thumbnail = ThumbnailUtils.extractThumbnail(
                                bmp,
                                size,
                                size
                            )
                            
                            FileOutputStream(thumbnailFile).use { outputStream ->
                                thumbnail.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, outputStream)
                            }
                            
                            thumbnail.recycle()
                            thumbnails[size] = thumbnailFile
                        }
                        bmp.recycle()
                    }
                }
            }
        } catch (e: Exception) {
            // Clean up any created thumbnails on failure
            thumbnails.values.forEach { file ->
                if (file.exists()) {
                    file.delete()
                }
            }
            thumbnails.clear()
        }
        
        thumbnails
    }
    
    /**
     * Get estimated thumbnail file size
     */
    fun getEstimatedThumbnailSize(): Long {
        // Rough estimate for JPEG thumbnail
        return (THUMBNAIL_SIZE.toDouble() * THUMBNAIL_SIZE.toDouble() * 0.1).toLong() // ~10KB for 200x200 thumbnail
    }
    
    /**
     * Generate thumbnail from URI
     */
    suspend fun generateThumbnailFromUri(
        sourceUri: android.net.Uri,
        thumbnailFile: File,
        mimeType: String,
        context: android.content.Context
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            when {
                mimeType.startsWith("image/") -> generateImageThumbnailFromUri(sourceUri, thumbnailFile, context)
                mimeType.startsWith("video/") -> generateVideoThumbnailFromUri(sourceUri, thumbnailFile, context)
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate image thumbnail from URI
     */
    private suspend fun generateImageThumbnailFromUri(
        sourceUri: android.net.Uri,
        thumbnailFile: File,
        context: android.content.Context
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(inputStream, null, options)
                
                val sampleSize = calculateSampleSize(options.outWidth, options.outHeight)
                
                context.contentResolver.openInputStream(sourceUri)?.use { inputStream2 ->
                    val decodeOptions = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inJustDecodeBounds = false
                    }
                    
                    val bitmap = BitmapFactory.decodeStream(inputStream2, null, decodeOptions)
                    bitmap?.let { bmp ->
                        val thumbnail = ThumbnailUtils.extractThumbnail(
                            bmp,
                            THUMBNAIL_SIZE,
                            THUMBNAIL_SIZE,
                            ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                        )
                        
                        FileOutputStream(thumbnailFile).use { outputStream ->
                            thumbnail.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, outputStream)
                        }
                        
                        thumbnail.recycle()
                        true
                    } ?: false
                }
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate video thumbnail from URI
     */
    private suspend fun generateVideoThumbnailFromUri(
        sourceUri: android.net.Uri,
        thumbnailFile: File,
        context: android.content.Context
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, sourceUri)
            
            val bitmap = retriever.getFrameAtTime(
                VIDEO_THUMBNAIL_TIME_US,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )
            
            retriever.release()
            
            bitmap?.let { bmp ->
                val thumbnail = ThumbnailUtils.extractThumbnail(
                    bmp,
                    THUMBNAIL_SIZE,
                    THUMBNAIL_SIZE,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT
                )
                
                FileOutputStream(thumbnailFile).use { outputStream ->
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, outputStream)
                }
                
                thumbnail.recycle()
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if file exists and is a valid thumbnail
     */
    fun isValidThumbnail(thumbnailFile: File): Boolean {
        return try {
            if (!thumbnailFile.exists() || thumbnailFile.length() == 0L) {
                false
            } else {
                // Try to decode the file to verify it's a valid image
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(thumbnailFile.absolutePath, options)
                options.outWidth > 0 && options.outHeight > 0
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Delete thumbnail file
     */
    fun deleteThumbnail(thumbnailFile: File): Boolean {
        return try {
            if (thumbnailFile.exists()) {
                thumbnailFile.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
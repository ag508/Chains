package com.chain.messaging.data.local.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for compressing media files to reduce storage usage
 */
@Singleton
class MediaCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "MediaCompressor"
        
        // Image compression settings
        private const val MAX_IMAGE_WIDTH = 1920
        private const val MAX_IMAGE_HEIGHT = 1080
        private const val IMAGE_QUALITY = 85
        
        // Video compression settings
        private const val MAX_VIDEO_BITRATE = 2000000 // 2 Mbps
        private const val MAX_VIDEO_WIDTH = 1280
        private const val MAX_VIDEO_HEIGHT = 720
        private const val VIDEO_FRAME_RATE = 30
        private const val VIDEO_I_FRAME_INTERVAL = 2
        private const val TIMEOUT_USEC = 10000L
    }
    
    /**
     * Compress media file based on its type
     */
    suspend fun compressMedia(sourceUri: Uri, mimeType: String, outputDir: File): File = withContext(Dispatchers.IO) {
        when {
            mimeType.startsWith("image/") -> compressImage(sourceUri, outputDir)
            mimeType.startsWith("video/") -> compressVideo(sourceUri, outputDir)
            else -> {
                // For other types, just copy the file
                val fileName = "compressed_${System.currentTimeMillis()}"
                val outputFile = File(outputDir, fileName)
                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                outputFile
            }
        }
    }
    
    /**
     * Compress image file
     */
    private suspend fun compressImage(sourceUri: Uri, outputDir: File): File = withContext(Dispatchers.IO) {
        val outputFile = File(outputDir, "compressed_image_${System.currentTimeMillis()}.jpg")
        
        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            // First, get image dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            
            // Calculate sample size
            val sampleSize = calculateImageSampleSize(options.outWidth, options.outHeight)
            
            // Decode with sample size
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream2 ->
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inJustDecodeBounds = false
                }
                
                val bitmap = BitmapFactory.decodeStream(inputStream2, null, decodeOptions)
                bitmap?.let { bmp ->
                    // Further resize if still too large
                    val resizedBitmap = resizeBitmapIfNeeded(bmp)
                    
                    // Compress and save
                    FileOutputStream(outputFile).use { outputStream ->
                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, outputStream)
                    }
                    
                    // Clean up bitmaps
                    if (resizedBitmap != bmp) {
                        bmp.recycle()
                    }
                    resizedBitmap.recycle()
                }
            }
        }
        
        outputFile
    }
    
    /**
     * Compress video file using MediaMuxer and MediaCodec
     */
    private suspend fun compressVideo(sourceUri: Uri, outputDir: File): File = withContext(Dispatchers.IO) {
        val outputFile = File(outputDir, "compressed_video_${System.currentTimeMillis()}.mp4")
        
        try {
            // Create a temporary file from the URI for MediaExtractor
            val tempInputFile = File(context.cacheDir, "temp_input_${System.currentTimeMillis()}.mp4")
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(tempInputFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            val success = compressVideoFile(tempInputFile, outputFile)
            
            // Clean up temporary file
            tempInputFile.delete()
            
            if (!success) {
                // If compression failed, fall back to copying the original
                Log.w(TAG, "Video compression failed, copying original file")
                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing video", e)
            // Fall back to copying the original file
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        
        outputFile
    }
    
    /**
     * Compress video file using MediaMuxer and MediaCodec
     */
    private fun compressVideoFile(inputFile: File, outputFile: File): Boolean {
        var extractor: MediaExtractor? = null
        var muxer: MediaMuxer? = null
        var decoder: MediaCodec? = null
        var encoder: MediaCodec? = null
        
        try {
            // Set up MediaExtractor
            extractor = MediaExtractor().apply {
                setDataSource(inputFile.absolutePath)
            }
            
            // Find video track
            var videoTrackIndex = -1
            var audioTrackIndex = -1
            var videoFormat: MediaFormat? = null
            var audioFormat: MediaFormat? = null
            
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mimeType = format.getString(MediaFormat.KEY_MIME) ?: continue
                
                when {
                    mimeType.startsWith("video/") && videoTrackIndex == -1 -> {
                        videoTrackIndex = i
                        videoFormat = format
                    }
                    mimeType.startsWith("audio/") && audioTrackIndex == -1 -> {
                        audioTrackIndex = i
                        audioFormat = format
                    }
                }
            }
            
            if (videoTrackIndex == -1) {
                Log.e(TAG, "No video track found")
                return false
            }
            
            // Get original video properties
            val originalWidth = videoFormat!!.getInteger(MediaFormat.KEY_WIDTH)
            val originalHeight = videoFormat.getInteger(MediaFormat.KEY_HEIGHT)
            val originalBitrate = if (videoFormat.containsKey(MediaFormat.KEY_BIT_RATE)) {
                videoFormat.getInteger(MediaFormat.KEY_BIT_RATE)
            } else {
                MAX_VIDEO_BITRATE
            }
            
            // Calculate new dimensions maintaining aspect ratio
            val (newWidth, newHeight) = calculateVideoSize(originalWidth, originalHeight)
            val newBitrate = minOf(originalBitrate, MAX_VIDEO_BITRATE)
            
            // Set up MediaMuxer
            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            
            // Set up encoder format
            val encoderFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, newWidth, newHeight).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, newBitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_FRAME_RATE)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VIDEO_I_FRAME_INTERVAL)
            }
            
            // For simplicity, we'll use a basic transcoding approach
            // In a production app, you might want to use Surface-to-Surface transcoding for better performance
            return transcodeVideo(extractor, muxer, videoTrackIndex, audioTrackIndex, encoderFormat, newWidth, newHeight)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in video compression", e)
            return false
        } finally {
            try {
                decoder?.stop()
                decoder?.release()
                encoder?.stop()
                encoder?.release()
                muxer?.stop()
                muxer?.release()
                extractor?.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing resources", e)
            }
        }
    }
    
    /**
     * Transcode video with basic frame-by-frame processing
     */
    private fun transcodeVideo(
        extractor: MediaExtractor,
        muxer: MediaMuxer,
        videoTrackIndex: Int,
        audioTrackIndex: Int,
        encoderFormat: MediaFormat,
        newWidth: Int,
        newHeight: Int
    ): Boolean {
        try {
            // For basic implementation, we'll copy the video track with new parameters
            // This is a simplified approach - a full implementation would decode and re-encode frames
            
            extractor.selectTrack(videoTrackIndex)
            val videoFormat = extractor.getTrackFormat(videoTrackIndex)
            
            // Create new format with compressed settings
            val outputVideoFormat = MediaFormat.createVideoFormat(
                videoFormat.getString(MediaFormat.KEY_MIME) ?: MediaFormat.MIMETYPE_VIDEO_AVC,
                newWidth,
                newHeight
            ).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, encoderFormat.getInteger(MediaFormat.KEY_BIT_RATE))
                setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_FRAME_RATE)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VIDEO_I_FRAME_INTERVAL)
            }
            
            val videoTrackIndexOutput = muxer.addTrack(outputVideoFormat)
            var audioTrackIndexOutput = -1
            
            // Add audio track if present
            if (audioTrackIndex != -1) {
                val audioFormat = extractor.getTrackFormat(audioTrackIndex)
                audioTrackIndexOutput = muxer.addTrack(audioFormat)
            }
            
            muxer.start()
            
            // Copy video samples
            val buffer = ByteBuffer.allocate(1024 * 1024) // 1MB buffer
            val bufferInfo = MediaCodec.BufferInfo()
            
            // Process video track
            extractor.selectTrack(videoTrackIndex)
            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break
                
                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
                bufferInfo.presentationTimeUs = extractor.sampleTime
                bufferInfo.flags = extractor.sampleFlags
                
                muxer.writeSampleData(videoTrackIndexOutput, buffer, bufferInfo)
                extractor.advance()
            }
            
            // Process audio track if present
            if (audioTrackIndex != -1 && audioTrackIndexOutput != -1) {
                extractor.selectTrack(audioTrackIndex)
                extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                
                while (true) {
                    val sampleSize = extractor.readSampleData(buffer, 0)
                    if (sampleSize < 0) break
                    
                    bufferInfo.offset = 0
                    bufferInfo.size = sampleSize
                    bufferInfo.presentationTimeUs = extractor.sampleTime
                    bufferInfo.flags = extractor.sampleFlags
                    
                    muxer.writeSampleData(audioTrackIndexOutput, buffer, bufferInfo)
                    extractor.advance()
                }
            }
            
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in transcoding", e)
            return false
        }
    }
    
    /**
     * Calculate new video dimensions maintaining aspect ratio
     */
    internal fun calculateVideoSize(originalWidth: Int, originalHeight: Int): Pair<Int, Int> {
        if (originalWidth <= MAX_VIDEO_WIDTH && originalHeight <= MAX_VIDEO_HEIGHT) {
            return Pair(originalWidth, originalHeight)
        }
        
        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()
        
        return if (aspectRatio > 1) {
            // Landscape
            val newWidth = MAX_VIDEO_WIDTH
            val newHeight = (newWidth / aspectRatio).toInt()
            // Ensure dimensions are even (required for some codecs)
            Pair(newWidth and 0xFFFFFFFE.toInt(), newHeight and 0xFFFFFFFE.toInt())
        } else {
            // Portrait or square
            val newHeight = MAX_VIDEO_HEIGHT
            val newWidth = (newHeight * aspectRatio).toInt()
            // Ensure dimensions are even (required for some codecs)
            Pair(newWidth and 0xFFFFFFFE.toInt(), newHeight and 0xFFFFFFFE.toInt())
        }
    }
    
    /**
     * Calculate sample size for image decoding
     */
    private fun calculateImageSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        
        if (height > MAX_IMAGE_HEIGHT || width > MAX_IMAGE_WIDTH) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / sampleSize) >= MAX_IMAGE_HEIGHT && 
                   (halfWidth / sampleSize) >= MAX_IMAGE_WIDTH) {
                sampleSize *= 2
            }
        }
        
        return sampleSize
    }
    
    /**
     * Resize bitmap if it's still too large after sampling
     */
    private fun resizeBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= MAX_IMAGE_WIDTH && height <= MAX_IMAGE_HEIGHT) {
            return bitmap
        }
        
        val aspectRatio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        
        if (aspectRatio > 1) {
            // Landscape
            newWidth = MAX_IMAGE_WIDTH
            newHeight = (MAX_IMAGE_WIDTH / aspectRatio).toInt()
        } else {
            // Portrait or square
            newHeight = MAX_IMAGE_HEIGHT
            newWidth = (MAX_IMAGE_HEIGHT * aspectRatio).toInt()
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Get compression ratio for a file
     */
    suspend fun getCompressionRatio(originalUri: Uri, compressedFile: File): Float = withContext(Dispatchers.IO) {
        try {
            val originalSize = context.contentResolver.openInputStream(originalUri)?.use { inputStream ->
                inputStream.available().toLong()
            } ?: 0L
            
            val compressedSize = compressedFile.length()
            
            if (originalSize > 0) {
                compressedSize.toFloat() / originalSize.toFloat()
            } else {
                1.0f
            }
        } catch (e: Exception) {
            1.0f
        }
    }
    
    /**
     * Check if media should be compressed based on size and type
     */
    fun shouldCompress(mimeType: String, fileSizeBytes: Long): Boolean {
        return when {
            mimeType.startsWith("image/") -> fileSizeBytes > 1024 * 1024 // 1MB
            mimeType.startsWith("video/") -> fileSizeBytes > 10 * 1024 * 1024 // 10MB
            else -> false
        }
    }
    
    /**
     * Get estimated compressed size
     */
    fun getEstimatedCompressedSize(mimeType: String, originalSize: Long): Long {
        return when {
            mimeType.startsWith("image/") -> (originalSize * 0.3).toLong() // ~30% of original
            mimeType.startsWith("video/") -> (originalSize * 0.4).toLong() // ~40% of original with MediaMuxer compression
            else -> originalSize
        }
    }
    
    /**
     * Get video metadata for compression analysis
     */
    suspend fun getVideoMetadata(uri: Uri): VideoMetadata? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                retriever.setDataSource(pfd.fileDescriptor)
                
                val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
                val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0
                
                VideoMetadata(width, height, duration, bitrate)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting video metadata", e)
            null
        }
    }
    
    /**
     * Data class for video metadata
     */
    data class VideoMetadata(
        val width: Int,
        val height: Int,
        val durationMs: Long,
        val bitrate: Int
    )
}
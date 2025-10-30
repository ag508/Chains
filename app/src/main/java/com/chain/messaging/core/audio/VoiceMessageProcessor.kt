package com.chain.messaging.core.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import com.chain.messaging.domain.model.MediaMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for processing voice messages - compression, optimization, and metadata extraction
 */
@Singleton
class VoiceMessageProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Process a recorded voice message
     */
    suspend fun processVoiceMessage(
        recordingResult: VoiceRecordingResult,
        compressionLevel: CompressionLevel = CompressionLevel.MEDIUM
    ): Result<MediaMessage> = withContext(Dispatchers.IO) {
        try {
            val processedFile = when (compressionLevel) {
                CompressionLevel.NONE -> recordingResult.file
                CompressionLevel.LOW -> compressAudio(recordingResult.file, 96000) // 96 kbps
                CompressionLevel.MEDIUM -> compressAudio(recordingResult.file, 64000) // 64 kbps
                CompressionLevel.HIGH -> compressAudio(recordingResult.file, 32000) // 32 kbps
            }
            
            val metadata = extractAudioMetadata(processedFile)
            val waveformData = generateWaveformData(processedFile)
            
            val mediaMessage = MediaMessage(
                uri = processedFile.absolutePath,
                fileName = processedFile.name,
                mimeType = "audio/mp4",
                fileSize = processedFile.length(),
                duration = metadata.duration,
                width = null,
                height = null,
                thumbnailUri = null,
                isLocal = true
            )
            
            // Store waveform data separately (in a real implementation, this might be stored in the database)
            storeWaveformData(processedFile.absolutePath, waveformData)
            
            Result.success(mediaMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Compress audio file (simplified implementation)
     * In a real implementation, this would use FFmpeg or similar
     */
    private suspend fun compressAudio(inputFile: File, bitRate: Int): File = withContext(Dispatchers.IO) {
        // For now, we'll just return the original file
        // In a production app, you would use FFmpeg or MediaMuxer to compress
        
        val outputFile = File(inputFile.parent, "compressed_${inputFile.name}")
        
        try {
            // Simulate compression by copying the file
            // In reality, you would re-encode with lower bitrate
            inputFile.copyTo(outputFile, overwrite = true)
            
            // Delete original file to save space
            inputFile.delete()
            
            outputFile
        } catch (e: Exception) {
            // If compression fails, return original file
            inputFile
        }
    }
    
    /**
     * Extract audio metadata
     */
    private fun extractAudioMetadata(file: File): AudioMetadata {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val bitRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0
            val sampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toIntOrNull() ?: 0
            
            retriever.release()
            
            AudioMetadata(
                duration = duration,
                bitRate = bitRate,
                sampleRate = sampleRate,
                fileSize = file.length()
            )
        } catch (e: Exception) {
            AudioMetadata(
                duration = 0L,
                bitRate = 0,
                sampleRate = 0,
                fileSize = file.length()
            )
        }
    }
    
    /**
     * Generate waveform data for visualization
     */
    private suspend fun generateWaveformData(file: File): List<Float> = withContext(Dispatchers.IO) {
        try {
            // Simplified waveform generation
            // In a real implementation, you would analyze the audio samples
            val duration = extractAudioMetadata(file).duration
            val sampleCount = minOf(100, (duration / 100).toInt()) // Max 100 samples
            
            // Generate mock waveform data
            (0 until sampleCount).map {
                (Math.random() * 0.8 + 0.1).toFloat() // Random values between 0.1 and 0.9
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Store waveform data (simplified implementation)
     */
    private fun storeWaveformData(filePath: String, waveformData: List<Float>) {
        try {
            val waveformFile = File("${filePath}.waveform")
            waveformFile.writeText(waveformData.joinToString(","))
        } catch (e: Exception) {
            // Ignore waveform storage errors
        }
    }
    
    /**
     * Load waveform data
     */
    suspend fun loadWaveformData(filePath: String): List<Float> = withContext(Dispatchers.IO) {
        try {
            val waveformFile = File("${filePath}.waveform")
            if (waveformFile.exists()) {
                waveformFile.readText()
                    .split(",")
                    .mapNotNull { it.toFloatOrNull() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Optimize voice message for sending
     */
    suspend fun optimizeForSending(mediaMessage: MediaMessage): Result<MediaMessage> = withContext(Dispatchers.IO) {
        try {
            val file = File(mediaMessage.uri)
            if (!file.exists()) {
                return@withContext Result.failure(IllegalArgumentException("File does not exist"))
            }
            
            // Check if file size is acceptable (e.g., under 10MB)
            val maxSize = 10 * 1024 * 1024 // 10MB
            
            if (mediaMessage.fileSize > maxSize) {
                // Compress further
                val compressedFile = compressAudio(file, 24000) // Very low bitrate
                val newMediaMessage = mediaMessage.copy(
                    uri = compressedFile.absolutePath,
                    fileName = compressedFile.name,
                    fileSize = compressedFile.length()
                )
                Result.success(newMediaMessage)
            } else {
                Result.success(mediaMessage)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get voice message quality info
     */
    suspend fun getQualityInfo(mediaMessage: MediaMessage): VoiceQualityInfo = withContext(Dispatchers.IO) {
        val file = File(mediaMessage.uri)
        val metadata = extractAudioMetadata(file)
        
        val quality = when {
            metadata.bitRate >= 128000 -> VoiceQuality.HIGH
            metadata.bitRate >= 64000 -> VoiceQuality.MEDIUM
            metadata.bitRate >= 32000 -> VoiceQuality.LOW
            else -> VoiceQuality.VERY_LOW
        }
        
        VoiceQualityInfo(
            quality = quality,
            bitRate = metadata.bitRate,
            fileSize = metadata.fileSize,
            duration = metadata.duration,
            compressionRatio = calculateCompressionRatio(metadata)
        )
    }
    
    /**
     * Calculate compression ratio
     */
    private fun calculateCompressionRatio(metadata: AudioMetadata): Float {
        // Estimate uncompressed size (44.1kHz, 16-bit, mono)
        val uncompressedSize = (metadata.duration / 1000.0) * 44100 * 2 // bytes
        return if (uncompressedSize > 0) {
            (metadata.fileSize / uncompressedSize).toFloat()
        } else {
            1.0f
        }
    }
}

/**
 * Compression levels for voice messages
 */
enum class CompressionLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Voice quality levels
 */
enum class VoiceQuality {
    VERY_LOW,
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Audio metadata
 */
data class AudioMetadata(
    val duration: Long,
    val bitRate: Int,
    val sampleRate: Int,
    val fileSize: Long
)

/**
 * Voice quality information
 */
data class VoiceQualityInfo(
    val quality: VoiceQuality,
    val bitRate: Int,
    val fileSize: Long,
    val duration: Long,
    val compressionRatio: Float
)
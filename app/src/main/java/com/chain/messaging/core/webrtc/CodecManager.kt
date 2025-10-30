package com.chain.messaging.core.webrtc

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Codec Manager for handling video/audio codec selection and configuration
 * Implements codec selection for call quality optimization
 */
interface CodecManager {
    /**
     * Get current codec information for a call
     */
    suspend fun getCurrentCodec(callId: String): CodecInfo
    
    /**
     * Get codec settings for a specific video quality
     */
    fun getCodecSettingsForQuality(quality: VideoQuality): CodecSettings
    
    /**
     * Apply codec settings to a call
     */
    suspend fun applyCodecSettings(callId: String, settings: CodecSettings)
    
    /**
     * Get available codecs
     */
    fun getAvailableCodecs(): List<SupportedCodec>
    
    /**
     * Select optimal codec based on network conditions
     */
    fun selectOptimalCodec(networkConditions: NetworkConditions): SupportedCodec
}

@Singleton
class CodecManagerImpl @Inject constructor() : CodecManager {
    
    private val activeCodecs = mutableMapOf<String, CodecInfo>()
    
    override suspend fun getCurrentCodec(callId: String): CodecInfo {
        return activeCodecs[callId] ?: CodecInfo(
            callId = callId,
            videoCodec = SupportedCodec.VP8,
            audioCodec = SupportedCodec.OPUS,
            settings = getCodecSettingsForQuality(VideoQuality.STANDARD)
        )
    }
    
    override fun getCodecSettingsForQuality(quality: VideoQuality): CodecSettings {
        return when (quality) {
            VideoQuality.HD -> CodecSettings(
                videoWidth = 1280,
                videoHeight = 720,
                videoFrameRate = 30,
                videoBitrate = 2000,
                audioBitrate = 128
            )
            VideoQuality.STANDARD -> CodecSettings(
                videoWidth = 640,
                videoHeight = 480,
                videoFrameRate = 30,
                videoBitrate = 1000,
                audioBitrate = 64
            )
            VideoQuality.LOW -> CodecSettings(
                videoWidth = 320,
                videoHeight = 240,
                videoFrameRate = 15,
                videoBitrate = 500,
                audioBitrate = 32
            )
            VideoQuality.AUDIO_ONLY -> CodecSettings(
                videoWidth = 0,
                videoHeight = 0,
                videoFrameRate = 0,
                videoBitrate = 0,
                audioBitrate = 64
            )
        }
    }
    
    override suspend fun applyCodecSettings(callId: String, settings: CodecSettings) {
        // This would apply the codec settings to the WebRTC peer connection
        // Implementation would involve configuring the video and audio tracks
        
        val currentCodec = getCurrentCodec(callId)
        val updatedCodec = currentCodec.copy(settings = settings)
        activeCodecs[callId] = updatedCodec
    }
    
    override fun getAvailableCodecs(): List<SupportedCodec> {
        return listOf(
            SupportedCodec.VP8,
            SupportedCodec.VP9,
            SupportedCodec.H264,
            SupportedCodec.OPUS,
            SupportedCodec.G722
        )
    }
    
    override fun selectOptimalCodec(networkConditions: NetworkConditions): SupportedCodec {
        return when {
            networkConditions.bandwidth > 2000 && networkConditions.packetLoss < 0.01 && networkConditions.rtt < 100 -> {
                SupportedCodec.H264 // Best quality for excellent conditions
            }
            networkConditions.bandwidth > 1500 && networkConditions.packetLoss < 0.02 -> {
                SupportedCodec.VP9 // Good quality with better compression
            }
            networkConditions.bandwidth > 800 && networkConditions.packetLoss < 0.03 -> {
                SupportedCodec.VP8 // Good balance for moderate conditions
            }
            networkConditions.bandwidth > 400 -> {
                SupportedCodec.VP8 // Most compatible for poor conditions
            }
            else -> {
                SupportedCodec.OPUS // Audio only for very poor conditions
            }
        }
    }
    
    /**
     * Get adaptive codec settings based on current network performance
     */
    fun getAdaptiveCodecSettings(
        currentQuality: VideoQuality,
        networkConditions: NetworkConditions
    ): CodecSettings {
        val baseSettings = getCodecSettingsForQuality(currentQuality)
        
        // Adjust settings based on network conditions
        return when {
            networkConditions.packetLoss > 0.05 -> {
                // High packet loss - reduce bitrate and frame rate
                baseSettings.copy(
                    videoBitrate = (baseSettings.videoBitrate.toDouble() * 0.7).toInt(),
                    videoFrameRate = minOf(baseSettings.videoFrameRate, 15),
                    audioBitrate = (baseSettings.audioBitrate.toDouble() * 0.8).toInt()
                )
            }
            networkConditions.rtt > 300L -> {
                // High latency - optimize for responsiveness
                baseSettings.copy(
                    videoBitrate = (baseSettings.videoBitrate.toDouble() * 0.8).toInt(),
                    videoFrameRate = minOf(baseSettings.videoFrameRate, 20)
                )
            }
            networkConditions.bandwidth < 1000 -> {
                // Low bandwidth - aggressive compression
                baseSettings.copy(
                    videoBitrate = minOf(baseSettings.videoBitrate, 500),
                    audioBitrate = minOf(baseSettings.audioBitrate, 32)
                )
            }
            else -> baseSettings
        }
    }
    
    /**
     * Get codec capabilities for a specific codec
     */
    fun getCodecCapabilities(codec: SupportedCodec): CodecCapabilities {
        return when (codec) {
            SupportedCodec.VP8 -> CodecCapabilities(
                maxWidth = 1920,
                maxHeight = 1080,
                maxFrameRate = 30,
                maxBitrate = 2000,
                supportedColorFormats = listOf("YUV420", "NV12")
            )
            SupportedCodec.VP9 -> CodecCapabilities(
                maxWidth = 3840,
                maxHeight = 2160,
                maxFrameRate = 60,
                maxBitrate = 5000,
                supportedColorFormats = listOf("YUV420", "NV12", "YUV444")
            )
            SupportedCodec.H264 -> CodecCapabilities(
                maxWidth = 1920,
                maxHeight = 1080,
                maxFrameRate = 30,
                maxBitrate = 3000,
                supportedColorFormats = listOf("YUV420", "NV12")
            )
            SupportedCodec.OPUS -> CodecCapabilities(
                maxWidth = 0,
                maxHeight = 0,
                maxFrameRate = 0,
                maxBitrate = 128,
                supportedColorFormats = emptyList()
            )
            SupportedCodec.G722 -> CodecCapabilities(
                maxWidth = 0,
                maxHeight = 0,
                maxFrameRate = 0,
                maxBitrate = 64,
                supportedColorFormats = emptyList()
            )
            SupportedCodec.PCMU, SupportedCodec.PCMA -> CodecCapabilities(
                maxWidth = 0,
                maxHeight = 0,
                maxFrameRate = 0,
                maxBitrate = 64,
                supportedColorFormats = emptyList()
            )
        }
    }
}

/**
 * Codec information data class
 */
data class CodecInfo(
    val callId: String,
    val videoCodec: SupportedCodec,
    val audioCodec: SupportedCodec,
    val settings: CodecSettings
)

/**
 * Codec settings data class
 */
data class CodecSettings(
    val videoWidth: Int,
    val videoHeight: Int,
    val videoFrameRate: Int,
    val videoBitrate: Int, // kbps
    val audioBitrate: Int // kbps
)

/**
 * Supported codec enumeration
 */
enum class SupportedCodec {
    // Video codecs
    VP8,
    VP9,
    H264,
    
    // Audio codecs
    OPUS,
    G722,
    PCMU,
    PCMA
}

/**
 * Network conditions data class
 */
data class NetworkConditions(
    val bandwidth: Int, // kbps
    val packetLoss: Double, // percentage (0.0 to 1.0)
    val rtt: Long, // milliseconds
    val jitter: Double // milliseconds
)

/**
 * Codec capabilities data class
 */
data class CodecCapabilities(
    val maxWidth: Int,
    val maxHeight: Int,
    val maxFrameRate: Int,
    val maxBitrate: Int,
    val supportedColorFormats: List<String>
)
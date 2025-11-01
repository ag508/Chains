package com.chain.messaging.core.webrtc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.webrtc.PeerConnection
import org.webrtc.RTCStatsReport
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resume

/**
 * Call quality manager for automatic quality adjustment and optimization
 * Implements requirement 6.4 for call quality optimization based on network conditions
 */
@Singleton
class CallQualityManager @Inject constructor(
    private val bandwidthMonitor: BandwidthMonitor,
    private val codecManager: CodecManager,
    private val callRecordingManager: CallRecordingManager,
    private val screenshotDetector: ScreenshotDetector
) {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private val activeMonitoringSessions = ConcurrentHashMap<String, QualityMonitoringSession>()
    
    private val _qualityMetrics = MutableStateFlow(CallQualityMetrics())
    val qualityMetrics: Flow<CallQualityMetrics> = _qualityMetrics.asStateFlow()
    
    private val _networkCondition = MutableStateFlow(NetworkCondition.GOOD)
    val networkCondition: Flow<NetworkCondition> = _networkCondition.asStateFlow()
    
    private val _qualityAdjustments = MutableSharedFlow<QualityAdjustment>()
    val qualityAdjustments: Flow<QualityAdjustment> = _qualityAdjustments.asSharedFlow()
    
    private val _callQualityEvents = MutableSharedFlow<CallQualityEvent>()
    val callQualityEvents: Flow<CallQualityEvent> = _callQualityEvents.asSharedFlow()
    
    /**
     * Start comprehensive quality monitoring for a call
     */
    suspend fun startQualityMonitoring(peerConnection: PeerConnection, callId: String, isVideo: Boolean) {
        if (activeMonitoringSessions.containsKey(callId)) {
            return // Already monitoring
        }
        
        val session = QualityMonitoringSession(
            callId = callId,
            peerConnection = peerConnection,
            isVideo = isVideo,
            startTime = System.currentTimeMillis(),
            monitoringJob = null
        )
        
        // Start bandwidth monitoring
        bandwidthMonitor.startMonitoring(callId)
        
        // Enable screenshot detection if video call
        if (isVideo) {
            screenshotDetector.startScreenshotDetection(callId)
            callRecordingManager.enableScreenshotDetection(callId)
        }
        
        // Start periodic quality monitoring
        val monitoringJob = scope.launch {
            monitorCallQuality(session)
        }
        
        activeMonitoringSessions[callId] = session.copy(monitoringJob = monitoringJob)
        
        _callQualityEvents.emit(CallQualityEvent.MonitoringStarted(callId))
    }
    
    /**
     * Stop quality monitoring for a call
     */
    suspend fun stopQualityMonitoring(callId: String) {
        val session = activeMonitoringSessions.remove(callId) ?: return
        
        // Stop monitoring job
        session.monitoringJob?.cancel()
        
        // Stop bandwidth monitoring
        bandwidthMonitor.stopMonitoring(callId)
        
        // Stop screenshot detection
        screenshotDetector.stopScreenshotDetection()
        callRecordingManager.disableScreenshotDetection(callId)
        
        _callQualityEvents.emit(CallQualityEvent.MonitoringStopped(callId))
    }
    
    /**
     * Get current quality metrics for a call
     */
    suspend fun getCurrentQualityMetrics(callId: String): CallQualityMetrics? {
        val session = activeMonitoringSessions[callId] ?: return null
        return collectQualityMetrics(session)
    }
    
    /**
     * Manually adjust call quality
     */
    suspend fun adjustCallQuality(callId: String, targetQuality: VideoQuality) {
        val session = activeMonitoringSessions[callId] ?: return
        
        val codecSettings = codecManager.getCodecSettingsForQuality(targetQuality)
        codecManager.applyCodecSettings(callId, codecSettings)
        
        val adjustment = QualityAdjustment(
            callId = callId,
            fromQuality = getCurrentVideoQuality(callId),
            toQuality = targetQuality,
            reason = "Manual adjustment",
            timestamp = System.currentTimeMillis()
        )
        
        _qualityAdjustments.emit(adjustment)
        _callQualityEvents.emit(CallQualityEvent.QualityAdjusted(adjustment))
    }
    
    /**
     * Get recommended video quality based on current network conditions
     */
    fun getRecommendedVideoQuality(): VideoQuality {
        return when (_networkCondition.value) {
            NetworkCondition.EXCELLENT -> VideoQuality.HD
            NetworkCondition.GOOD -> VideoQuality.STANDARD
            NetworkCondition.POOR -> VideoQuality.LOW
            NetworkCondition.BAD -> VideoQuality.AUDIO_ONLY
        }
    }
    
    /**
     * Get recommended audio codec based on network conditions
     */
    fun getRecommendedAudioCodec(): AudioCodec {
        return when (_networkCondition.value) {
            NetworkCondition.EXCELLENT, NetworkCondition.GOOD -> AudioCodec.OPUS
            NetworkCondition.POOR -> AudioCodec.G722
            NetworkCondition.BAD -> AudioCodec.G711
        }
    }
    
    /**
     * Observe combined quality events from all sources
     */
    fun observeQualityEvents(): Flow<CallQualityEvent> {
        return _callQualityEvents.asSharedFlow()
    }
    
    private suspend fun monitorCallQuality(session: QualityMonitoringSession) {
        while (activeMonitoringSessions.containsKey(session.callId)) {
            try {
                // Collect current metrics
                val metrics = collectQualityMetrics(session)
                _qualityMetrics.value = metrics
                
                // Determine network condition
                val networkCondition = determineNetworkCondition(metrics)
                if (networkCondition != _networkCondition.value) {
                    _networkCondition.value = networkCondition
                    _callQualityEvents.emit(CallQualityEvent.NetworkConditionChanged(session.callId, networkCondition))
                }
                
                // Check if quality adjustment is needed
                val shouldAdjust = shouldAdjustQuality(metrics, networkCondition)
                if (shouldAdjust) {
                    performAutomaticQualityAdjustment(session, metrics, networkCondition)
                }
                
                // Wait before next monitoring cycle
                delay(MONITORING_INTERVAL_MS)
                
            } catch (e: Exception) {
                _callQualityEvents.emit(CallQualityEvent.MonitoringError(session.callId, e.message ?: "Unknown error"))
                delay(ERROR_RETRY_INTERVAL_MS)
            }
        }
    }
    
    private suspend fun collectQualityMetrics(session: QualityMonitoringSession): CallQualityMetrics {
        // Get bandwidth information
        val bandwidthInfo = bandwidthMonitor.getCurrentBandwidth(session.callId)
        
        // Get WebRTC statistics
        val rtcStats = getWebRTCStats(session.peerConnection)
        
        return CallQualityMetrics(
            callId = session.callId,
            bandwidth = bandwidthInfo.availableBandwidth.toLong(),
            usedBandwidth = bandwidthInfo.usedBandwidth.toLong(),
            packetLoss = rtcStats.packetLoss,
            jitter = rtcStats.jitter,
            rtt = rtcStats.rtt,
            audioLevel = rtcStats.audioLevel,
            videoFrameRate = rtcStats.videoFrameRate,
            videoResolution = rtcStats.videoResolution,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private suspend fun getWebRTCStats(peerConnection: PeerConnection): WebRTCStats {
        // Collect actual WebRTC statistics using RTCStatsReport
        return try {
            suspendCoroutine<WebRTCStats> { continuation ->
                peerConnection.getStats { rtcStatsReport ->
                    try {
                        val stats = parseRTCStatsReport(rtcStatsReport)
                        continuation.resume(stats)
                    } catch (e: Exception) {
                        // Fallback to default values if parsing fails
                        continuation.resume(getDefaultWebRTCStats())
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback to default values if stats collection fails
            getDefaultWebRTCStats()
        }
    }
    
    private fun parseRTCStatsReport(report: RTCStatsReport): WebRTCStats {
        var packetLoss = 0.0
        var jitter = 0.0
        var rtt = 0L
        var audioLevel = 0.0
        var videoFrameRate = 0
        var videoResolution = ""
        
        // Parse the stats report
        for (stats in report.statsMap.values) {
            when (stats.type) {
                "inbound-rtp" -> {
                    // Parse inbound RTP statistics
                    stats.members["packetsLost"]?.let { lost ->
                        stats.members["packetsReceived"]?.let { received ->
                            val lostCount = (lost as? Number)?.toDouble() ?: 0.0
                            val receivedCount = (received as? Number)?.toDouble() ?: 1.0
                            packetLoss = lostCount / (lostCount + receivedCount)
                        }
                    }
                    
                    stats.members["jitter"]?.let { jitterValue ->
                        jitter = (jitterValue as? Number)?.toDouble() ?: 0.0
                    }
                    
                    // Video specific stats
                    stats.members["framesPerSecond"]?.let { fps ->
                        videoFrameRate = (fps as? Number)?.toInt() ?: 0
                    }
                    
                    // Audio level
                    stats.members["audioLevel"]?.let { level ->
                        audioLevel = (level as? Number)?.toDouble() ?: 0.0
                    }
                }
                
                "remote-inbound-rtp" -> {
                    // Parse remote inbound RTP statistics for RTT
                    stats.members["roundTripTime"]?.let { rttValue ->
                        rtt = ((rttValue as? Number)?.toDouble()?.times(1000))?.toLong() ?: 0L
                    }
                }
                
                "track" -> {
                    // Parse track statistics for video resolution
                    if (stats.members["kind"] == "video") {
                        val width = stats.members["frameWidth"] as? Number
                        val height = stats.members["frameHeight"] as? Number
                        if (width != null && height != null) {
                            videoResolution = "${width.toInt()}x${height.toInt()}"
                        }
                    }
                }
            }
        }
        
        return WebRTCStats(
            packetLoss = packetLoss,
            jitter = jitter,
            rtt = rtt,
            audioLevel = audioLevel,
            videoFrameRate = videoFrameRate,
            videoResolution = videoResolution.ifEmpty { "unknown" }
        )
    }
    
    private fun getDefaultWebRTCStats(): WebRTCStats {
        // Fallback default values when real stats are not available
        return WebRTCStats(
            packetLoss = 0.01, // 1% packet loss
            jitter = 20.0, // 20ms jitter
            rtt = 100, // 100ms RTT
            audioLevel = 0.8, // 80% audio level
            videoFrameRate = 30, // 30 FPS
            videoResolution = "640x480"
        )
    }
    
    private fun determineNetworkCondition(metrics: CallQualityMetrics): NetworkCondition {
        return when {
            metrics.bandwidth > 2000 && metrics.packetLoss < 0.01 && metrics.rtt < 100 -> NetworkCondition.EXCELLENT
            metrics.bandwidth > 1000 && metrics.packetLoss < 0.03 && metrics.rtt < 200 -> NetworkCondition.GOOD
            metrics.bandwidth > 500 && metrics.packetLoss < 0.05 && metrics.rtt < 400 -> NetworkCondition.POOR
            else -> NetworkCondition.BAD
        }
    }
    
    private fun shouldAdjustQuality(metrics: CallQualityMetrics, networkCondition: NetworkCondition): Boolean {
        val currentQuality = getCurrentVideoQuality(metrics.callId)
        val recommendedQuality = getRecommendedVideoQuality()
        
        // Adjust if recommended quality is different from current
        if (currentQuality != recommendedQuality) {
            return true
        }
        
        // Adjust if packet loss is too high
        if (metrics.packetLoss > PACKET_LOSS_THRESHOLD) {
            return true
        }
        
        // Adjust if RTT is too high
        if (metrics.rtt > RTT_THRESHOLD) {
            return true
        }
        
        return false
    }
    
    private suspend fun performAutomaticQualityAdjustment(
        session: QualityMonitoringSession,
        metrics: CallQualityMetrics,
        networkCondition: NetworkCondition
    ) {
        val currentQuality = getCurrentVideoQuality(session.callId)
        val targetQuality = determineTargetQuality(metrics, networkCondition)
        
        if (currentQuality != targetQuality) {
            val codecSettings = codecManager.getCodecSettingsForQuality(targetQuality)
            codecManager.applyCodecSettings(session.callId, codecSettings)
            
            val adjustment = QualityAdjustment(
                callId = session.callId,
                fromQuality = currentQuality,
                toQuality = targetQuality,
                reason = "Automatic adjustment based on network conditions",
                timestamp = System.currentTimeMillis()
            )
            
            _qualityAdjustments.emit(adjustment)
            _callQualityEvents.emit(CallQualityEvent.QualityAdjusted(adjustment))
        }
    }
    
    private fun determineTargetQuality(metrics: CallQualityMetrics, networkCondition: NetworkCondition): VideoQuality {
        // Consider multiple factors for quality determination
        return when {
            metrics.packetLoss > 0.05 -> VideoQuality.AUDIO_ONLY
            metrics.rtt > 500 -> VideoQuality.LOW
            networkCondition == NetworkCondition.BAD -> VideoQuality.AUDIO_ONLY
            networkCondition == NetworkCondition.POOR -> VideoQuality.LOW
            networkCondition == NetworkCondition.GOOD -> VideoQuality.STANDARD
            networkCondition == NetworkCondition.EXCELLENT -> VideoQuality.HD
            else -> VideoQuality.STANDARD
        }
    }
    
    private fun getCurrentVideoQuality(callId: String): VideoQuality {
        // Get the current video quality from codec manager based on bandwidth and performance
        val bandwidthInfo = try {
            kotlinx.coroutines.runBlocking {
                bandwidthMonitor.getCurrentBandwidth(callId)
            }
        } catch (e: Exception) {
            null
        }
        
        return when {
            bandwidthInfo == null -> VideoQuality.STANDARD
            bandwidthInfo.availableBandwidth > 2000 -> VideoQuality.HD
            bandwidthInfo.availableBandwidth > 800 -> VideoQuality.STANDARD
            bandwidthInfo.availableBandwidth > 300 -> VideoQuality.LOW
            else -> VideoQuality.AUDIO_ONLY
        }
    }
    
    companion object {
        private const val MONITORING_INTERVAL_MS = 3000L // Monitor every 3 seconds
        private const val ERROR_RETRY_INTERVAL_MS = 5000L // Retry after 5 seconds on error
        private const val PACKET_LOSS_THRESHOLD = 0.03 // 3% packet loss threshold
        private const val RTT_THRESHOLD = 300L // 300ms RTT threshold
    }
}

/**
 * Call quality metrics data class
 */
data class CallQualityMetrics(
    val callId: String = "",
    val bandwidth: Long = 0,
    val usedBandwidth: Long = 0,
    val packetLoss: Double = 0.0,
    val jitter: Double = 0.0,
    val rtt: Long = 0,
    val audioLevel: Double = 0.0,
    val videoFrameRate: Int = 0,
    val videoResolution: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Network condition enumeration
 */
enum class NetworkCondition {
    EXCELLENT, GOOD, POOR, BAD
}

/**
 * Video quality enumeration
 */
enum class VideoQuality {
    HD, STANDARD, LOW, AUDIO_ONLY
}

/**
 * Audio codec enumeration
 */
enum class AudioCodec {
    OPUS, G722, G711
}

/**
 * Quality monitoring session data class
 */
data class QualityMonitoringSession(
    val callId: String,
    val peerConnection: PeerConnection,
    val isVideo: Boolean,
    val startTime: Long,
    val monitoringJob: Job?
)

/**
 * Quality adjustment data class
 */
data class QualityAdjustment(
    val callId: String,
    val fromQuality: VideoQuality,
    val toQuality: VideoQuality,
    val reason: String,
    val timestamp: Long
)

/**
 * WebRTC statistics data class
 */
data class WebRTCStats(
    val packetLoss: Double,
    val jitter: Double,
    val rtt: Long,
    val audioLevel: Double,
    val videoFrameRate: Int,
    val videoResolution: String
)

/**
 * Call quality events
 */
sealed class CallQualityEvent {
    data class MonitoringStarted(val callId: String) : CallQualityEvent()
    data class MonitoringStopped(val callId: String) : CallQualityEvent()
    data class NetworkConditionChanged(val callId: String, val condition: NetworkCondition) : CallQualityEvent()
    data class QualityAdjusted(val adjustment: QualityAdjustment) : CallQualityEvent()
    data class MonitoringError(val callId: String, val error: String) : CallQualityEvent()
    data class BandwidthChanged(val bandwidthUpdate: BandwidthUpdate) : CallQualityEvent()
    data class ScreenshotDetected(val screenshotEvent: ScreenshotEvent) : CallQualityEvent()
    data class RecordingEvent(val recordingEvent: com.chain.messaging.core.webrtc.RecordingEvent) : CallQualityEvent()
}
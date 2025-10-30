package com.chain.messaging.domain.model

import java.io.File

/**
 * Call event sealed class for WebRTC call management
 */
sealed class CallEvent {
    data class IncomingCallEvent(val callSession: CallSession) : CallEvent()
    data class CallAccepted(val callId: String, val callSession: CallSession? = null) : CallEvent()
    data class CallDeclined(val callId: String) : CallEvent()
    data class CallEnded(val callId: String, val reason: String) : CallEvent()
    data class CallFailed(val callId: String, val error: String, val callSession: CallSession? = null) : CallEvent()
    data class RemoteStreamAdded(val callId: String, val stream: Any) : CallEvent()
    data class RemoteStreamRemoved(val callId: String, val stream: Any) : CallEvent()
    data class IceCandidateReceived(val callId: String, val candidate: Any) : CallEvent()

    // State machine events
    object InitiateCall : CallEvent()
    object IncomingCall : CallEvent()
    object AcceptCall : CallEvent()
    object DeclineCall : CallEvent()
    object RejectCall : CallEvent()
    object EndCall : CallEvent()
    object CallTimeout : CallEvent()
    object CallConnected : CallEvent()
    object ConnectionLost : CallEvent()
    data class CallRejected(val callId: String, val reason: String? = null) : CallEvent()
}

/**
 * Recording event sealed class for call recording management
 */
sealed class RecordingEvent {
    data class RecordingStarted(val callId: String, val session: RecordingSession) : RecordingEvent()
    data class RecordingStopped(val callId: String, val result: RecordingResult) : RecordingEvent()
    data class RecordingFailed(val callId: String, val error: String) : RecordingEvent()
    data class RecordingDetected(
        val callId: String,
        val type: RecordingType,
        val likelihood: RecordingLikelihood,
        val timestamp: Long,
        val details: String
    ) : RecordingEvent()
}

/**
 * Screenshot event sealed class for screenshot detection
 */
sealed class ScreenshotEvent {
    data class DetectionEnabled(val callId: String) : ScreenshotEvent()
    data class DetectionDisabled(val callId: String) : ScreenshotEvent()
    data class ScreenshotDetected(val callId: String, val timestamp: Long) : ScreenshotEvent()
}

/**
 * Recording session data class
 */
data class RecordingSession(
    val callId: String,
    val startTime: Long,
    val outputFile: File,
    val isActive: Boolean
)

/**
 * Recording result data class
 */
data class RecordingResult(
    val callId: String,
    val startTime: Long,
    val endTime: Long,
    val outputFile: File,
    val duration: Long,
    val fileSize: Long
)

/**
 * Types of recording that can be detected
 */
enum class RecordingType {
    AUDIO,
    SCREEN,
    UNKNOWN
}

/**
 * Likelihood that recording is actually happening
 */
enum class RecordingLikelihood {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Call session data class
 */
data class CallSession(
    val id: String,
    val peerId: String,
    val isVideo: Boolean,
    val status: CallStatus,
    val localStream: Any?, // Using Any to avoid WebRTC dependency in domain
    val remoteStream: Any?, // Using Any to avoid WebRTC dependency in domain
    val startTime: Long = System.currentTimeMillis()
)

/**
 * Call status enum
 */
enum class CallStatus {
    INITIATING,
    RINGING,
    CONNECTING,
    CONNECTED,
    ENDED,
    FAILED
}

/**
 * Call notification data class
 */
data class CallNotification(
    val id: String,
    val callId: String,
    val type: CallNotificationType,
    val peerName: String,
    val peerId: String,
    val timestamp: Long,
    val isVideo: Boolean,
    val duration: Long? = null
)

/**
 * Types of call notifications
 */
enum class CallNotificationType {
    INCOMING,
    MISSED,
    OUTGOING,
    REJECTED
}
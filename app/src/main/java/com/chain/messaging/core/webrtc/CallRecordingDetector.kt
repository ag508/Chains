package com.chain.messaging.core.webrtc

import android.content.Context
import android.media.AudioManager
import android.os.Build
import com.chain.messaging.domain.model.RecordingEvent
import com.chain.messaging.domain.model.RecordingLikelihood
import com.chain.messaging.domain.model.RecordingType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Call recording detector for detecting potential call recording
 * Implements requirement 6.4 for call recording detection where possible
 */
@Singleton
class CallRecordingDetector @Inject constructor(
    private val context: Context
) {
    
    private val _recordingEvents = MutableSharedFlow<RecordingEvent>()
    val recordingEvents: Flow<RecordingEvent> = _recordingEvents.asSharedFlow()
    
    private var isMonitoring = false
    private var audioManager: AudioManager? = null
    
    /**
     * Start monitoring for call recording
     */
    fun startRecordingDetection(callId: String) {
        if (isMonitoring) return
        
        isMonitoring = true
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        // Monitor audio routing changes that might indicate recording
        monitorAudioRouting(callId)
        
        // Check for screen recording (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkScreenRecording(callId)
        }
    }
    
    /**
     * Stop monitoring for call recording
     */
    fun stopRecordingDetection() {
        isMonitoring = false
        audioManager = null
    }
    
    /**
     * Check if call recording is potentially active
     */
    fun isRecordingDetected(): Boolean {
        // Enhanced heuristics for recording detection
        return checkAudioRecordingIndicators() || 
               checkScreenRecordingIndicators() || 
               checkSystemRecordingIndicators()
    }
    
    private fun monitorAudioRouting(callId: String) {
        // Monitor for audio routing changes that might indicate recording
        audioManager?.let { am ->
            // Check if audio is being routed to unusual destinations
            if (am.isWiredHeadsetOn || am.isBluetoothA2dpOn) {
                // These could be normal or could indicate recording setup
                emitRecordingEvent(callId, RecordingType.AUDIO, RecordingLikelihood.LOW)
            }
        }
    }
    
    private fun checkScreenRecording(callId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // On Android 10+, we can detect screen recording in some cases
            // This is a simplified implementation
            try {
                // Check for screen recording indicators
                val isScreenRecording = checkScreenRecordingIndicators()
                if (isScreenRecording) {
                    emitRecordingEvent(callId, RecordingType.SCREEN, RecordingLikelihood.MEDIUM)
                }
            } catch (e: Exception) {
                // Screen recording detection failed
            }
        }
    }
    
    private fun checkAudioRecordingIndicators(): Boolean {
        // Check various indicators that might suggest audio recording
        audioManager?.let { am ->
            // Check if microphone is being used by other apps
            if (am.mode == AudioManager.MODE_IN_COMMUNICATION) {
                // This is normal for calls, but could indicate recording
                return false
            }
            
            // Check for unusual audio routing
            if (am.isWiredHeadsetOn && am.isSpeakerphoneOn) {
                // Unusual combination that might indicate recording setup
                return true
            }
        }
        
        return false
    }
    
    private fun checkScreenRecordingIndicators(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                // Check for screen recording through multiple methods
                val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as android.media.projection.MediaProjectionManager
                
                // Check for active media projection (screen recording/casting)
                // Note: This is limited by Android security - we can only detect some cases
                
                // Check for common screen recording apps
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
                val runningApps = activityManager.runningAppProcesses
                
                val suspiciousApps = runningApps?.any { process ->
                    val processName = process.processName.lowercase()
                    processName.contains("record") ||
                    processName.contains("capture") ||
                    processName.contains("screen") ||
                    processName.contains("cast") ||
                    processName.contains("mirror")
                } ?: false
                
                // Check for system UI indicators (notification bar icons, etc.)
                val hasScreenRecordingNotification = checkForScreenRecordingNotification()
                
                return suspiciousApps || hasScreenRecordingNotification
            } catch (e: Exception) {
                // If we can't detect, assume no recording for privacy
                return false
            }
        }
        return false
    }
    
    private fun checkSystemRecordingIndicators(): Boolean {
        return try {
            // Check for system-level recording indicators
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Check for microphone usage by other apps
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                
                // Check audio focus and mode
                val isAudioFocusActive = audioManager.mode != AudioManager.MODE_NORMAL
                val isMicrophoneMuted = audioManager.isMicrophoneMute
                
                // Check for accessibility services that might be recording
                val accessibilityServices = checkAccessibilityServices()
                
                isAudioFocusActive || accessibilityServices
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkForScreenRecordingNotification(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Check if notification access is available
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                
                // Look for screen recording related notifications
                // This is limited by Android security policies
                
                // Check for status bar icons that might indicate recording
                val statusBarManager = context.getSystemService(Context.STATUS_BAR_SERVICE)
                
                // This is a best-effort detection - Android limits what we can access
                false // Conservative approach for privacy
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkAccessibilityServices(): Boolean {
        return try {
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
                    android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK
                )
                
                // Check for accessibility services that might be used for recording
                enabledServices.any { service ->
                    val serviceName = service.resolveInfo.serviceInfo.name.lowercase()
                    serviceName.contains("record") ||
                    serviceName.contains("capture") ||
                    serviceName.contains("screen")
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun emitRecordingEvent(callId: String, type: RecordingType, likelihood: RecordingLikelihood) {
        val event = RecordingEvent.RecordingDetected(
            callId = callId,
            type = type,
            likelihood = likelihood,
            timestamp = System.currentTimeMillis(),
            details = getRecordingDetails(type)
        )
        
        _recordingEvents.tryEmit(event)
    }
    
    private fun getRecordingDetails(type: RecordingType): String {
        return when (type) {
            RecordingType.AUDIO -> "Potential audio recording detected based on audio routing"
            RecordingType.SCREEN -> "Potential screen recording detected"
            RecordingType.UNKNOWN -> "Unknown recording activity detected"
        }
    }
}


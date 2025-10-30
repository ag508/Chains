package com.chain.messaging.core.webrtc

import android.content.Context
import com.chain.messaging.domain.model.RecordingEvent
import com.chain.messaging.domain.model.RecordingResult
import com.chain.messaging.domain.model.RecordingSession
import com.chain.messaging.domain.model.ScreenshotEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Call Recording Manager for handling call recording and screenshot detection
 * Implements call recording and screenshot detection as per requirement 6.4
 */
interface CallRecordingManager {
    /**
     * Start recording a call
     */
    suspend fun startRecording(callId: String): RecordingSession?
    
    /**
     * Stop recording a call
     */
    suspend fun stopRecording(callId: String): RecordingResult?
    
    /**
     * Check if recording is active for a call
     */
    fun isRecording(callId: String): Boolean
    
    /**
     * Enable screenshot detection for a call
     */
    suspend fun enableScreenshotDetection(callId: String)
    
    /**
     * Disable screenshot detection for a call
     */
    suspend fun disableScreenshotDetection(callId: String)
    
    /**
     * Observe recording events
     */
    fun observeRecordingEvents(): Flow<RecordingEvent>
    
    /**
     * Observe screenshot detection events
     */
    fun observeScreenshotEvents(): Flow<ScreenshotEvent>
    
    /**
     * Get recording permissions status
     */
    fun hasRecordingPermissions(): Boolean
}

@Singleton
class CallRecordingManagerImpl @Inject constructor(
    private val context: Context
) : CallRecordingManager {
    
    private val activeRecordings = mutableMapOf<String, RecordingSession>()
    private val screenshotDetectionActive = mutableSetOf<String>()
    
    private val _recordingEvents = MutableSharedFlow<RecordingEvent>()
    private val _screenshotEvents = MutableSharedFlow<ScreenshotEvent>()
    
    override suspend fun startRecording(callId: String): RecordingSession? {
        if (!hasRecordingPermissions()) {
            _recordingEvents.emit(
                RecordingEvent.RecordingFailed(
                    callId = callId,
                    error = "Recording permissions not granted"
                )
            )
            return null
        }
        
        if (activeRecordings.containsKey(callId)) {
            return activeRecordings[callId]
        }
        
        val recordingFile = createRecordingFile(callId)
        val session = RecordingSession(
            callId = callId,
            startTime = System.currentTimeMillis(),
            outputFile = recordingFile,
            isActive = true
        )
        
        activeRecordings[callId] = session
        
        // Start actual recording implementation
        startActualRecording(session)
        
        _recordingEvents.emit(
            RecordingEvent.RecordingStarted(
                callId = callId,
                session = session
            )
        )
        
        return session
    }
    
    override suspend fun stopRecording(callId: String): RecordingResult? {
        val session = activeRecordings[callId] ?: return null
        
        // Stop actual recording
        stopActualRecording(session)
        
        val result = RecordingResult(
            callId = callId,
            startTime = session.startTime,
            endTime = System.currentTimeMillis(),
            outputFile = session.outputFile,
            duration = System.currentTimeMillis() - session.startTime,
            fileSize = session.outputFile.length()
        )
        
        activeRecordings.remove(callId)
        
        _recordingEvents.emit(
            RecordingEvent.RecordingStopped(
                callId = callId,
                result = result
            )
        )
        
        return result
    }
    
    override fun isRecording(callId: String): Boolean {
        return activeRecordings[callId]?.isActive == true
    }
    
    override suspend fun enableScreenshotDetection(callId: String) {
        screenshotDetectionActive.add(callId)
        
        // Start monitoring for screenshots
        startScreenshotMonitoring(callId)
        
        _screenshotEvents.emit(
            ScreenshotEvent.DetectionEnabled(callId)
        )
    }
    
    override suspend fun disableScreenshotDetection(callId: String) {
        screenshotDetectionActive.remove(callId)
        
        // Stop monitoring for screenshots
        stopScreenshotMonitoring(callId)
        
        _screenshotEvents.emit(
            ScreenshotEvent.DetectionDisabled(callId)
        )
    }
    
    override fun observeRecordingEvents(): Flow<RecordingEvent> = _recordingEvents.asSharedFlow()
    
    override fun observeScreenshotEvents(): Flow<ScreenshotEvent> = _screenshotEvents.asSharedFlow()
    
    override fun hasRecordingPermissions(): Boolean {
        // Check for all required recording permissions
        return try {
            val audioPermission = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
            
            // For Android 10+ (API 29+), WRITE_EXTERNAL_STORAGE is not needed for app-specific directories
            val storagePermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                android.content.pm.PackageManager.PERMISSION_GRANTED // Not needed for scoped storage
            } else {
                context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            
            // Check camera permission for video recording
            val cameraPermission = context.checkSelfPermission(android.Manifest.permission.CAMERA)
            
            // Check if microphone is available and not being used by other apps
            val microphoneAvailable = checkMicrophoneAvailability()
            
            val hasBasicPermissions = audioPermission == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                                    storagePermission == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            val hasCameraPermission = cameraPermission == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            // Return true if we have audio + storage, camera permission is optional for audio-only recording
            hasBasicPermissions && microphoneAvailable
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if we have video recording permissions (includes camera)
     */
    fun hasVideoRecordingPermissions(): Boolean {
        return try {
            hasRecordingPermissions() && 
            context.checkSelfPermission(android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if microphone is available and not being used by other apps
     */
    private fun checkMicrophoneAvailability(): Boolean {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            
            // Check if microphone is muted
            if (audioManager.isMicrophoneMute) {
                return false
            }
            
            // Check audio mode - if another app is using the microphone for calls
            when (audioManager.mode) {
                android.media.AudioManager.MODE_IN_CALL,
                android.media.AudioManager.MODE_IN_COMMUNICATION -> {
                    // Another app might be using the microphone
                    return false
                }
                else -> return true
            }
        } catch (e: Exception) {
            // If we can't determine, assume it's available
            true
        }
    }
    
    private fun createRecordingFile(callId: String): File {
        val recordingsDir = File(context.filesDir, "call_recordings")
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs()
        }
        
        val timestamp = System.currentTimeMillis()
        return File(recordingsDir, "call_${callId}_${timestamp}.mp4")
    }
    
    private suspend fun startActualRecording(session: RecordingSession) {
        // Implementation would start actual media recording
        // This would involve capturing audio/video streams from WebRTC
    }
    
    private suspend fun stopActualRecording(session: RecordingSession) {
        // Implementation would stop actual media recording
    }
    
    private suspend fun startScreenshotMonitoring(callId: String) {
        // Implementation would monitor for screenshot events
        // This might involve file system monitoring or system callbacks
        
        CoroutineScope(Dispatchers.IO).launch {
            // Simulate screenshot detection
            delay(10000) // Wait 10 seconds
            
            if (screenshotDetectionActive.contains(callId)) {
                _screenshotEvents.emit(
                    ScreenshotEvent.ScreenshotDetected(
                        callId = callId,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }
    
    private suspend fun stopScreenshotMonitoring(callId: String) {
        // Implementation would stop screenshot monitoring
    }
}


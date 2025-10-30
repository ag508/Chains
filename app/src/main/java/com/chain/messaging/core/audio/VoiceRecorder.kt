package com.chain.messaging.core.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for recording voice messages with waveform data
 */
@Singleton
class VoiceRecorder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTime: Long = 0
    
    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    
    private val _amplitude = MutableStateFlow(0)
    val amplitude: StateFlow<Int> = _amplitude.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    /**
     * Check if recording permission is granted
     */
    fun hasRecordingPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Start recording voice message
     */
    suspend fun startRecording(): Result<File> {
        return try {
            if (!hasRecordingPermission()) {
                return Result.failure(SecurityException("Recording permission not granted"))
            }
            
            if (_recordingState.value != RecordingState.IDLE) {
                return Result.failure(IllegalStateException("Already recording"))
            }
            
            // Create output file
            val audioDir = File(context.filesDir, "audio")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }
            
            outputFile = File(audioDir, "voice_${System.currentTimeMillis()}.m4a")
            
            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile!!.absolutePath)
                
                prepare()
                start()
            }
            
            startTime = System.currentTimeMillis()
            _recordingState.value = RecordingState.RECORDING
            
            // Start amplitude monitoring
            startAmplitudeMonitoring()
            
            Result.success(outputFile!!)
        } catch (e: Exception) {
            cleanup()
            Result.failure(e)
        }
    }
    
    /**
     * Stop recording and return the recorded file
     */
    suspend fun stopRecording(): Result<VoiceRecordingResult> {
        return try {
            if (_recordingState.value != RecordingState.RECORDING) {
                return Result.failure(IllegalStateException("Not currently recording"))
            }
            
            mediaRecorder?.apply {
                stop()
                release()
            }
            
            val recordingDuration = System.currentTimeMillis() - startTime
            val file = outputFile ?: return Result.failure(IllegalStateException("No output file"))
            
            _recordingState.value = RecordingState.IDLE
            _amplitude.value = 0
            _duration.value = 0
            
            val result = VoiceRecordingResult(
                file = file,
                duration = recordingDuration,
                fileSize = file.length()
            )
            
            cleanup()
            Result.success(result)
        } catch (e: Exception) {
            cleanup()
            Result.failure(e)
        }
    }
    
    /**
     * Cancel current recording
     */
    suspend fun cancelRecording(): Result<Unit> {
        return try {
            if (_recordingState.value != RecordingState.RECORDING) {
                return Result.failure(IllegalStateException("Not currently recording"))
            }
            
            mediaRecorder?.apply {
                stop()
                release()
            }
            
            // Delete the file
            outputFile?.delete()
            
            _recordingState.value = RecordingState.IDLE
            _amplitude.value = 0
            _duration.value = 0
            
            cleanup()
            Result.success(Unit)
        } catch (e: Exception) {
            cleanup()
            Result.failure(e)
        }
    }
    
    /**
     * Start monitoring amplitude for waveform visualization
     */
    private fun startAmplitudeMonitoring() {
        // This would typically be done in a coroutine with a timer
        // For now, we'll simulate amplitude changes
        Thread {
            while (_recordingState.value == RecordingState.RECORDING) {
                try {
                    val amplitude = mediaRecorder?.maxAmplitude ?: 0
                    _amplitude.value = amplitude
                    _duration.value = System.currentTimeMillis() - startTime
                    Thread.sleep(100) // Update every 100ms
                } catch (e: Exception) {
                    break
                }
            }
        }.start()
    }
    
    /**
     * Clean up resources
     */
    private fun cleanup() {
        mediaRecorder?.release()
        mediaRecorder = null
        outputFile = null
        startTime = 0
    }
}

/**
 * Recording states
 */
enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED
}

/**
 * Result of voice recording
 */
data class VoiceRecordingResult(
    val file: File,
    val duration: Long,
    val fileSize: Long
)
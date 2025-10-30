package com.chain.messaging.presentation.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.messaging.core.audio.CompressionLevel
import com.chain.messaging.core.audio.RecordingState
import com.chain.messaging.core.audio.VoiceMessageProcessor
import com.chain.messaging.core.audio.VoiceRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for voice recording functionality
 */
@HiltViewModel
class VoiceRecorderViewModel @Inject constructor(
    private val voiceRecorder: VoiceRecorder,
    private val voiceMessageProcessor: VoiceMessageProcessor
) : ViewModel() {
    
    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()
    
    private val _waveformData = MutableStateFlow<List<Float>>(emptyList())
    val waveformData: StateFlow<List<Float>> = _waveformData.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Expose recorder states
    val recordingState = voiceRecorder.recordingState
    val amplitude = voiceRecorder.amplitude
    val duration = voiceRecorder.duration
    
    init {
        // Monitor amplitude changes to update waveform
        viewModelScope.launch {
            amplitude.collect { amp ->
                updateWaveform(amp)
            }
        }
    }
    
    /**
     * Check recording permissions
     */
    fun checkPermissions(context: Context) {
        val hasRecordPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        
        _hasPermission.value = hasRecordPermission
    }
    
    /**
     * Start recording
     */
    fun startRecording() {
        if (!_hasPermission.value) {
            _error.value = "Recording permission not granted"
            return
        }
        
        viewModelScope.launch {
            voiceRecorder.startRecording()
                .onFailure { exception ->
                    _error.value = "Failed to start recording: ${exception.message}"
                }
        }
    }
    
    /**
     * Stop recording and process the result
     */
    fun stopRecording(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            voiceRecorder.stopRecording()
                .onSuccess { recordingResult ->
                    // Process the recording
                    voiceMessageProcessor.processVoiceMessage(
                        recordingResult,
                        CompressionLevel.MEDIUM
                    ).onSuccess { mediaMessage ->
                        onComplete(mediaMessage.uri)
                        clearWaveform()
                    }.onFailure { exception ->
                        _error.value = "Failed to process recording: ${exception.message}"
                    }
                }
                .onFailure { exception ->
                    _error.value = "Failed to stop recording: ${exception.message}"
                }
        }
    }
    
    /**
     * Cancel current recording
     */
    fun cancelRecording() {
        viewModelScope.launch {
            voiceRecorder.cancelRecording()
                .onFailure { exception ->
                    _error.value = "Failed to cancel recording: ${exception.message}"
                }
            clearWaveform()
        }
    }
    
    /**
     * Update waveform data based on amplitude
     */
    private fun updateWaveform(amplitude: Int) {
        if (recordingState.value == RecordingState.RECORDING) {
            val normalizedAmplitude = (amplitude / 32767.0f).coerceIn(0.1f, 1.0f)
            val currentData = _waveformData.value.toMutableList()
            
            // Add new amplitude data
            currentData.add(normalizedAmplitude)
            
            // Keep only last 50 samples for visualization
            if (currentData.size > 50) {
                currentData.removeAt(0)
            }
            
            _waveformData.value = currentData
        }
    }
    
    /**
     * Clear waveform data
     */
    private fun clearWaveform() {
        _waveformData.value = emptyList()
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}
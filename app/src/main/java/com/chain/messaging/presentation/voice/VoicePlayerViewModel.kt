package com.chain.messaging.presentation.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chain.messaging.core.audio.VoiceMessageProcessor
import com.chain.messaging.core.audio.VoicePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for voice message playback functionality
 */
@HiltViewModel
class VoicePlayerViewModel @Inject constructor(
    private val voicePlayer: VoicePlayer,
    private val voiceMessageProcessor: VoiceMessageProcessor
) : ViewModel() {
    
    private val _waveformData = MutableStateFlow<List<Float>>(emptyList())
    val waveformData: StateFlow<List<Float>> = _waveformData.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Expose player states
    val playbackState = voicePlayer.playbackState
    val currentPosition = voicePlayer.currentPosition
    val duration = voicePlayer.duration
    val currentPlayingFile = voicePlayer.currentPlayingFile
    
    /**
     * Play a voice message file
     */
    fun play(file: File) {
        viewModelScope.launch {
            voicePlayer.play(file)
                .onFailure { exception ->
                    _error.value = "Failed to play voice message: ${exception.message}"
                }
        }
    }
    
    /**
     * Pause current playback
     */
    fun pause() {
        viewModelScope.launch {
            voicePlayer.pause()
                .onFailure { exception ->
                    _error.value = "Failed to pause playback: ${exception.message}"
                }
        }
    }
    
    /**
     * Resume paused playback
     */
    fun resume() {
        viewModelScope.launch {
            voicePlayer.resume()
                .onFailure { exception ->
                    _error.value = "Failed to resume playback: ${exception.message}"
                }
        }
    }
    
    /**
     * Stop current playback
     */
    fun stop() {
        viewModelScope.launch {
            voicePlayer.stop()
                .onFailure { exception ->
                    _error.value = "Failed to stop playback: ${exception.message}"
                }
        }
    }
    
    /**
     * Seek to specific position
     */
    fun seekTo(position: Long) {
        viewModelScope.launch {
            voicePlayer.seekTo(position)
                .onFailure { exception ->
                    _error.value = "Failed to seek: ${exception.message}"
                }
        }
    }
    
    /**
     * Check if currently playing a specific file
     */
    fun isPlayingFile(filePath: String): Boolean {
        return voicePlayer.isPlaying(File(filePath))
    }
    
    /**
     * Load waveform data for a voice message
     */
    fun loadWaveformData(filePath: String) {
        viewModelScope.launch {
            val waveform = voiceMessageProcessor.loadWaveformData(filePath)
            _waveformData.value = waveform
        }
    }
    
    /**
     * Set playback speed (for future implementation)
     */
    fun setPlaybackSpeed(speed: Float) {
        viewModelScope.launch {
            voicePlayer.setPlaybackSpeed(speed)
                .onFailure { exception ->
                    _error.value = "Failed to set playback speed: ${exception.message}"
                }
        }
    }
    
    /**
     * Get current playback speed
     */
    fun getPlaybackSpeed(): Float {
        return voicePlayer.getPlaybackSpeed()
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        // Stop any ongoing playback when ViewModel is cleared
        viewModelScope.launch {
            voicePlayer.stop()
        }
    }
}
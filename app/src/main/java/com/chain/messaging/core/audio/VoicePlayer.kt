package com.chain.messaging.core.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for playing voice messages with progress tracking
 */
@Singleton
class VoicePlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var mediaPlayer: MediaPlayer? = null
    private var currentFile: File? = null
    private var progressUpdateThread: Thread? = null
    
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _currentPlayingFile = MutableStateFlow<String?>(null)
    val currentPlayingFile: StateFlow<String?> = _currentPlayingFile.asStateFlow()
    
    /**
     * Start playing a voice message
     */
    suspend fun play(file: File): Result<Unit> {
        return try {
            if (!file.exists()) {
                return Result.failure(IllegalArgumentException("File does not exist"))
            }
            
            // Stop current playback if any
            stop()
            
            currentFile = file
            _currentPlayingFile.value = file.absolutePath
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.fromFile(file))
                setOnPreparedListener { player ->
                    _duration.value = player.duration.toLong()
                    player.start()
                    _playbackState.value = PlaybackState.PLAYING
                    startProgressTracking()
                }
                setOnCompletionListener {
                    _playbackState.value = PlaybackState.COMPLETED
                    _currentPosition.value = _duration.value
                    stopProgressTracking()
                }
                setOnErrorListener { _, what, extra ->
                    _playbackState.value = PlaybackState.ERROR
                    stopProgressTracking()
                    true
                }
                prepareAsync()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            cleanup()
            Result.failure(e)
        }
    }
    
    /**
     * Pause playback
     */
    suspend fun pause(): Result<Unit> {
        return try {
            if (_playbackState.value != PlaybackState.PLAYING) {
                return Result.failure(IllegalStateException("Not currently playing"))
            }
            
            mediaPlayer?.pause()
            _playbackState.value = PlaybackState.PAUSED
            stopProgressTracking()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Resume playback
     */
    suspend fun resume(): Result<Unit> {
        return try {
            if (_playbackState.value != PlaybackState.PAUSED) {
                return Result.failure(IllegalStateException("Not currently paused"))
            }
            
            mediaPlayer?.start()
            _playbackState.value = PlaybackState.PLAYING
            startProgressTracking()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Stop playback
     */
    suspend fun stop(): Result<Unit> {
        return try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            
            stopProgressTracking()
            cleanup()
            
            Result.success(Unit)
        } catch (e: Exception) {
            cleanup()
            Result.failure(e)
        }
    }
    
    /**
     * Seek to specific position
     */
    suspend fun seekTo(position: Long): Result<Unit> {
        return try {
            if (mediaPlayer == null) {
                return Result.failure(IllegalStateException("No media player initialized"))
            }
            
            mediaPlayer?.seekTo(position.toInt())
            _currentPosition.value = position
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get playback speed (for future implementation)
     */
    fun getPlaybackSpeed(): Float {
        return 1.0f // Normal speed
    }
    
    /**
     * Set playback speed (for future implementation)
     */
    suspend fun setPlaybackSpeed(speed: Float): Result<Unit> {
        return try {
            // This would require API level 23+ for MediaPlayer.setPlaybackParams()
            // For now, we'll just return success
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if currently playing a specific file
     */
    fun isPlaying(file: File): Boolean {
        return _currentPlayingFile.value == file.absolutePath && 
               _playbackState.value == PlaybackState.PLAYING
    }
    
    /**
     * Start tracking playback progress
     */
    private fun startProgressTracking() {
        stopProgressTracking() // Stop any existing tracking
        
        progressUpdateThread = Thread {
            while (_playbackState.value == PlaybackState.PLAYING) {
                try {
                    mediaPlayer?.let { player ->
                        if (player.isPlaying) {
                            _currentPosition.value = player.currentPosition.toLong()
                        }
                    }
                    Thread.sleep(100) // Update every 100ms
                } catch (e: Exception) {
                    break
                }
            }
        }.apply { start() }
    }
    
    /**
     * Stop tracking playback progress
     */
    private fun stopProgressTracking() {
        progressUpdateThread?.interrupt()
        progressUpdateThread = null
    }
    
    /**
     * Clean up resources
     */
    private fun cleanup() {
        mediaPlayer = null
        currentFile = null
        _playbackState.value = PlaybackState.IDLE
        _currentPosition.value = 0L
        _duration.value = 0L
        _currentPlayingFile.value = null
    }
}

/**
 * Playback states
 */
enum class PlaybackState {
    IDLE,
    PLAYING,
    PAUSED,
    COMPLETED,
    ERROR
}
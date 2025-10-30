package com.chain.messaging.core.webrtc

import android.content.Context
import org.webrtc.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory for creating WebRTC PeerConnection instances with proper configuration
 * Handles WebRTC initialization and provides configured peer connection factory
 */
@Singleton
class WebRTCPeerConnectionFactory @Inject constructor(
    private val context: Context
) {
    
    private var factory: PeerConnectionFactory? = null
    
    /**
     * Get or create the PeerConnectionFactory instance
     */
    fun getFactory(): PeerConnectionFactory {
        if (factory == null) {
            initializeFactory()
        }
        return factory ?: throw IllegalStateException("Failed to initialize PeerConnectionFactory")
    }
    
    /**
     * Initialize the WebRTC PeerConnectionFactory with proper configuration
     */
    private fun initializeFactory() {
        // Initialize WebRTC
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(false)
            .setFieldTrials("")
            .createInitializationOptions()
        
        PeerConnectionFactory.initialize(initializationOptions)
        
        // Create factory options
        val options = PeerConnectionFactory.Options().apply {
            disableEncryption = false
            disableNetworkMonitor = false
        }
        
        // Create encoders and decoders
        val videoEncoderFactory = DefaultVideoEncoderFactory(
            EglBase.create().eglBaseContext,
            true, // enableIntelVp8Encoder
            true  // enableH264HighProfile
        )
        
        val videoDecoderFactory = DefaultVideoDecoderFactory(
            EglBase.create().eglBaseContext
        )
        
        // Use default audio device module for now
        // JavaAudioDeviceModule is not available in all WebRTC versions
        val audioDeviceModule = null
        
        // Build the factory
        factory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(videoEncoderFactory)
            .setVideoDecoderFactory(videoDecoderFactory)
            .createPeerConnectionFactory()
    }
    
    /**
     * Clean up factory resources
     */
    fun dispose() {
        factory?.dispose()
        factory = null
    }
}
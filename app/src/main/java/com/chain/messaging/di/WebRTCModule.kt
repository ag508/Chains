package com.chain.messaging.di

import android.content.Context
import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.core.webrtc.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.webrtc.PeerConnectionFactory
import javax.inject.Singleton

/**
 * Dagger Hilt module for WebRTC dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object WebRTCModule {
    
    @Provides
    @Singleton
    fun provideWebRTCPeerConnectionFactory(
        @ApplicationContext context: Context
    ): WebRTCPeerConnectionFactory {
        return WebRTCPeerConnectionFactory(context)
    }
    
    @Provides
    @Singleton
    fun providePeerConnectionFactory(
        webRTCFactory: WebRTCPeerConnectionFactory
    ): PeerConnectionFactory {
        return webRTCFactory.getFactory()
    }
    
    @Provides
    @Singleton
    fun provideIceServerProvider(): IceServerProvider {
        return IceServerProvider()
    }
    
    @Provides
    @Singleton
    fun provideWebRTCManager(
        @ApplicationContext context: Context,
        peerConnectionFactory: PeerConnectionFactory
    ): WebRTCManager {
        return WebRTCManagerImpl(context, peerConnectionFactory)
    }
    
    @Provides
    @Singleton
    fun provideCallSignalingService(
        blockchainManager: BlockchainManager,
        encryptionService: SignalEncryptionService
    ): CallSignalingService {
        return CallSignalingService(blockchainManager, encryptionService)
    }
    
    @Provides
    @Singleton
    fun provideCallNotificationService(
        @ApplicationContext context: Context
    ): CallNotificationService {
        return CallNotificationService(context)
    }
    
    @Provides
    @Singleton
    fun provideCallManager(
        webRTCManager: WebRTCManager,
        callSignalingService: CallSignalingService,
        iceServerProvider: IceServerProvider
    ): CallManager {
        return CallManager(webRTCManager, callSignalingService, iceServerProvider)
    }
    
    @Provides
    @Singleton
    fun provideBandwidthMonitor(
        @ApplicationContext context: Context
    ): BandwidthMonitor {
        return BandwidthMonitorImpl(context)
    }
    
    @Provides
    @Singleton
    fun provideCodecManager(): CodecManager {
        return CodecManagerImpl()
    }
    
    @Provides
    @Singleton
    fun provideCallQualityManager(
        bandwidthMonitor: BandwidthMonitor,
        codecManager: CodecManager,
        callRecordingManager: CallRecordingManager,
        screenshotDetector: ScreenshotDetector
    ): CallQualityManager {
        return CallQualityManager(bandwidthMonitor, codecManager, callRecordingManager, screenshotDetector)
    }
    
    @Provides
    @Singleton
    fun provideCallRecordingDetector(
        @ApplicationContext context: Context
    ): CallRecordingDetector {
        return CallRecordingDetector(context)
    }
    
    @Provides
    @Singleton
    fun provideScreenshotDetector(
        @ApplicationContext context: Context
    ): ScreenshotDetector {
        return ScreenshotDetector(context)
    }
    
    @Provides
    @Singleton
    fun provideCallRecordingManager(
        @ApplicationContext context: Context
    ): CallRecordingManager {
        return CallRecordingManagerImpl(context)
    }
}
package com.chain.messaging.di

import com.chain.messaging.core.integration.ChainApplicationManager
import com.chain.messaging.core.integration.UserJourneyOrchestrator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object IntegrationModule {

    @Provides
    @Singleton
    fun provideChainApplicationManager(
        authenticationService: com.chain.messaging.core.auth.AuthenticationService,
        blockchainManager: com.chain.messaging.core.blockchain.BlockchainManager,
        encryptionService: com.chain.messaging.core.crypto.SignalEncryptionService,
        messagingService: com.chain.messaging.core.messaging.MessagingService,
        p2pManager: com.chain.messaging.core.p2p.P2PManager,
        webrtcManager: com.chain.messaging.core.webrtc.WebRTCManager,
        cloudStorageManager: com.chain.messaging.core.cloud.CloudStorageManager,
        notificationService: com.chain.messaging.core.notification.NotificationService,
        offlineMessageQueue: com.chain.messaging.core.offline.OfflineMessageQueue,
        crossDeviceSyncService: com.chain.messaging.core.sync.CrossDeviceSyncService,
        disappearingMessageManager: com.chain.messaging.core.privacy.DisappearingMessageManager,
        securityMonitoringManager: com.chain.messaging.core.security.SecurityMonitoringManager,
        performanceMonitor: com.chain.messaging.core.performance.PerformanceMonitor,
        networkMonitor: com.chain.messaging.core.network.NetworkMonitor
    ): ChainApplicationManager {
        return ChainApplicationManager(
            authenticationService,
            blockchainManager,
            encryptionService,
            messagingService,
            p2pManager,
            webrtcManager,
            cloudStorageManager,
            notificationService,
            offlineMessageQueue,
            crossDeviceSyncService,
            disappearingMessageManager,
            securityMonitoringManager,
            performanceMonitor,
            networkMonitor
        )
    }

    @Provides
    @Singleton
    fun provideUserJourneyOrchestrator(
        authenticationService: com.chain.messaging.core.auth.AuthenticationService,
        encryptionService: com.chain.messaging.core.crypto.SignalEncryptionService,
        messagingService: com.chain.messaging.core.messaging.MessagingService,
        identityVerificationManager: com.chain.messaging.core.security.IdentityVerificationManager
    ): UserJourneyOrchestrator {
        return UserJourneyOrchestrator(
            authenticationService,
            encryptionService,
            messagingService,
            identityVerificationManager
        )
    }
}
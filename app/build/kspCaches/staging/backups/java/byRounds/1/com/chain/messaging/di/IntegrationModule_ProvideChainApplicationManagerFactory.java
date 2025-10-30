package com.chain.messaging.di;

import com.chain.messaging.core.auth.AuthenticationService;
import com.chain.messaging.core.blockchain.BlockchainManager;
import com.chain.messaging.core.cloud.CloudStorageManager;
import com.chain.messaging.core.crypto.SignalEncryptionService;
import com.chain.messaging.core.integration.ChainApplicationManager;
import com.chain.messaging.core.messaging.MessagingService;
import com.chain.messaging.core.network.NetworkMonitor;
import com.chain.messaging.core.notification.NotificationService;
import com.chain.messaging.core.offline.OfflineMessageQueue;
import com.chain.messaging.core.p2p.P2PManager;
import com.chain.messaging.core.performance.PerformanceMonitor;
import com.chain.messaging.core.privacy.DisappearingMessageManager;
import com.chain.messaging.core.security.SecurityMonitoringManager;
import com.chain.messaging.core.sync.CrossDeviceSyncService;
import com.chain.messaging.core.webrtc.WebRTCManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class IntegrationModule_ProvideChainApplicationManagerFactory implements Factory<ChainApplicationManager> {
  private final Provider<AuthenticationService> authenticationServiceProvider;

  private final Provider<BlockchainManager> blockchainManagerProvider;

  private final Provider<SignalEncryptionService> encryptionServiceProvider;

  private final Provider<MessagingService> messagingServiceProvider;

  private final Provider<P2PManager> p2pManagerProvider;

  private final Provider<WebRTCManager> webrtcManagerProvider;

  private final Provider<CloudStorageManager> cloudStorageManagerProvider;

  private final Provider<NotificationService> notificationServiceProvider;

  private final Provider<OfflineMessageQueue> offlineMessageQueueProvider;

  private final Provider<CrossDeviceSyncService> crossDeviceSyncServiceProvider;

  private final Provider<DisappearingMessageManager> disappearingMessageManagerProvider;

  private final Provider<SecurityMonitoringManager> securityMonitoringManagerProvider;

  private final Provider<PerformanceMonitor> performanceMonitorProvider;

  private final Provider<NetworkMonitor> networkMonitorProvider;

  public IntegrationModule_ProvideChainApplicationManagerFactory(
      Provider<AuthenticationService> authenticationServiceProvider,
      Provider<BlockchainManager> blockchainManagerProvider,
      Provider<SignalEncryptionService> encryptionServiceProvider,
      Provider<MessagingService> messagingServiceProvider, Provider<P2PManager> p2pManagerProvider,
      Provider<WebRTCManager> webrtcManagerProvider,
      Provider<CloudStorageManager> cloudStorageManagerProvider,
      Provider<NotificationService> notificationServiceProvider,
      Provider<OfflineMessageQueue> offlineMessageQueueProvider,
      Provider<CrossDeviceSyncService> crossDeviceSyncServiceProvider,
      Provider<DisappearingMessageManager> disappearingMessageManagerProvider,
      Provider<SecurityMonitoringManager> securityMonitoringManagerProvider,
      Provider<PerformanceMonitor> performanceMonitorProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    this.authenticationServiceProvider = authenticationServiceProvider;
    this.blockchainManagerProvider = blockchainManagerProvider;
    this.encryptionServiceProvider = encryptionServiceProvider;
    this.messagingServiceProvider = messagingServiceProvider;
    this.p2pManagerProvider = p2pManagerProvider;
    this.webrtcManagerProvider = webrtcManagerProvider;
    this.cloudStorageManagerProvider = cloudStorageManagerProvider;
    this.notificationServiceProvider = notificationServiceProvider;
    this.offlineMessageQueueProvider = offlineMessageQueueProvider;
    this.crossDeviceSyncServiceProvider = crossDeviceSyncServiceProvider;
    this.disappearingMessageManagerProvider = disappearingMessageManagerProvider;
    this.securityMonitoringManagerProvider = securityMonitoringManagerProvider;
    this.performanceMonitorProvider = performanceMonitorProvider;
    this.networkMonitorProvider = networkMonitorProvider;
  }

  @Override
  public ChainApplicationManager get() {
    return provideChainApplicationManager(authenticationServiceProvider.get(), blockchainManagerProvider.get(), encryptionServiceProvider.get(), messagingServiceProvider.get(), p2pManagerProvider.get(), webrtcManagerProvider.get(), cloudStorageManagerProvider.get(), notificationServiceProvider.get(), offlineMessageQueueProvider.get(), crossDeviceSyncServiceProvider.get(), disappearingMessageManagerProvider.get(), securityMonitoringManagerProvider.get(), performanceMonitorProvider.get(), networkMonitorProvider.get());
  }

  public static IntegrationModule_ProvideChainApplicationManagerFactory create(
      Provider<AuthenticationService> authenticationServiceProvider,
      Provider<BlockchainManager> blockchainManagerProvider,
      Provider<SignalEncryptionService> encryptionServiceProvider,
      Provider<MessagingService> messagingServiceProvider, Provider<P2PManager> p2pManagerProvider,
      Provider<WebRTCManager> webrtcManagerProvider,
      Provider<CloudStorageManager> cloudStorageManagerProvider,
      Provider<NotificationService> notificationServiceProvider,
      Provider<OfflineMessageQueue> offlineMessageQueueProvider,
      Provider<CrossDeviceSyncService> crossDeviceSyncServiceProvider,
      Provider<DisappearingMessageManager> disappearingMessageManagerProvider,
      Provider<SecurityMonitoringManager> securityMonitoringManagerProvider,
      Provider<PerformanceMonitor> performanceMonitorProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    return new IntegrationModule_ProvideChainApplicationManagerFactory(authenticationServiceProvider, blockchainManagerProvider, encryptionServiceProvider, messagingServiceProvider, p2pManagerProvider, webrtcManagerProvider, cloudStorageManagerProvider, notificationServiceProvider, offlineMessageQueueProvider, crossDeviceSyncServiceProvider, disappearingMessageManagerProvider, securityMonitoringManagerProvider, performanceMonitorProvider, networkMonitorProvider);
  }

  public static ChainApplicationManager provideChainApplicationManager(
      AuthenticationService authenticationService, BlockchainManager blockchainManager,
      SignalEncryptionService encryptionService, MessagingService messagingService,
      P2PManager p2pManager, WebRTCManager webrtcManager, CloudStorageManager cloudStorageManager,
      NotificationService notificationService, OfflineMessageQueue offlineMessageQueue,
      CrossDeviceSyncService crossDeviceSyncService,
      DisappearingMessageManager disappearingMessageManager,
      SecurityMonitoringManager securityMonitoringManager, PerformanceMonitor performanceMonitor,
      NetworkMonitor networkMonitor) {
    return Preconditions.checkNotNullFromProvides(IntegrationModule.INSTANCE.provideChainApplicationManager(authenticationService, blockchainManager, encryptionService, messagingService, p2pManager, webrtcManager, cloudStorageManager, notificationService, offlineMessageQueue, crossDeviceSyncService, disappearingMessageManager, securityMonitoringManager, performanceMonitor, networkMonitor));
  }
}

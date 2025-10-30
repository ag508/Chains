package com.chain.messaging.core.offline;

import com.chain.messaging.core.messaging.MessagingService;
import com.chain.messaging.core.network.NetworkMonitor;
import com.chain.messaging.domain.repository.MessageRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlinx.coroutines.CoroutineScope;

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
public final class OfflineSyncServiceImpl_Factory implements Factory<OfflineSyncServiceImpl> {
  private final Provider<OfflineMessageQueue> offlineMessageQueueProvider;

  private final Provider<NetworkMonitor> networkMonitorProvider;

  private final Provider<MessagingService> messagingServiceProvider;

  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<ConflictResolver> conflictResolverProvider;

  private final Provider<CoroutineScope> coroutineScopeProvider;

  public OfflineSyncServiceImpl_Factory(Provider<OfflineMessageQueue> offlineMessageQueueProvider,
      Provider<NetworkMonitor> networkMonitorProvider,
      Provider<MessagingService> messagingServiceProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<ConflictResolver> conflictResolverProvider,
      Provider<CoroutineScope> coroutineScopeProvider) {
    this.offlineMessageQueueProvider = offlineMessageQueueProvider;
    this.networkMonitorProvider = networkMonitorProvider;
    this.messagingServiceProvider = messagingServiceProvider;
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.conflictResolverProvider = conflictResolverProvider;
    this.coroutineScopeProvider = coroutineScopeProvider;
  }

  @Override
  public OfflineSyncServiceImpl get() {
    return newInstance(offlineMessageQueueProvider.get(), networkMonitorProvider.get(), messagingServiceProvider.get(), messageRepositoryProvider.get(), conflictResolverProvider.get(), coroutineScopeProvider.get());
  }

  public static OfflineSyncServiceImpl_Factory create(
      Provider<OfflineMessageQueue> offlineMessageQueueProvider,
      Provider<NetworkMonitor> networkMonitorProvider,
      Provider<MessagingService> messagingServiceProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<ConflictResolver> conflictResolverProvider,
      Provider<CoroutineScope> coroutineScopeProvider) {
    return new OfflineSyncServiceImpl_Factory(offlineMessageQueueProvider, networkMonitorProvider, messagingServiceProvider, messageRepositoryProvider, conflictResolverProvider, coroutineScopeProvider);
  }

  public static OfflineSyncServiceImpl newInstance(OfflineMessageQueue offlineMessageQueue,
      NetworkMonitor networkMonitor, MessagingService messagingService,
      MessageRepository messageRepository, ConflictResolver conflictResolver,
      CoroutineScope coroutineScope) {
    return new OfflineSyncServiceImpl(offlineMessageQueue, networkMonitor, messagingService, messageRepository, conflictResolver, coroutineScope);
  }
}

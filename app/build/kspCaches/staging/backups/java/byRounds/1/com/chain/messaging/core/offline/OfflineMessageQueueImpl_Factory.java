package com.chain.messaging.core.offline;

import com.chain.messaging.core.messaging.MessagingService;
import com.chain.messaging.core.network.NetworkMonitor;
import com.chain.messaging.data.local.dao.QueuedMessageDao;
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
public final class OfflineMessageQueueImpl_Factory implements Factory<OfflineMessageQueueImpl> {
  private final Provider<QueuedMessageDao> queuedMessageDaoProvider;

  private final Provider<MessagingService> messagingServiceProvider;

  private final Provider<NetworkMonitor> networkMonitorProvider;

  private final Provider<BackoffStrategy> backoffStrategyProvider;

  private final Provider<CoroutineScope> coroutineScopeProvider;

  public OfflineMessageQueueImpl_Factory(Provider<QueuedMessageDao> queuedMessageDaoProvider,
      Provider<MessagingService> messagingServiceProvider,
      Provider<NetworkMonitor> networkMonitorProvider,
      Provider<BackoffStrategy> backoffStrategyProvider,
      Provider<CoroutineScope> coroutineScopeProvider) {
    this.queuedMessageDaoProvider = queuedMessageDaoProvider;
    this.messagingServiceProvider = messagingServiceProvider;
    this.networkMonitorProvider = networkMonitorProvider;
    this.backoffStrategyProvider = backoffStrategyProvider;
    this.coroutineScopeProvider = coroutineScopeProvider;
  }

  @Override
  public OfflineMessageQueueImpl get() {
    return newInstance(queuedMessageDaoProvider.get(), messagingServiceProvider.get(), networkMonitorProvider.get(), backoffStrategyProvider.get(), coroutineScopeProvider.get());
  }

  public static OfflineMessageQueueImpl_Factory create(
      Provider<QueuedMessageDao> queuedMessageDaoProvider,
      Provider<MessagingService> messagingServiceProvider,
      Provider<NetworkMonitor> networkMonitorProvider,
      Provider<BackoffStrategy> backoffStrategyProvider,
      Provider<CoroutineScope> coroutineScopeProvider) {
    return new OfflineMessageQueueImpl_Factory(queuedMessageDaoProvider, messagingServiceProvider, networkMonitorProvider, backoffStrategyProvider, coroutineScopeProvider);
  }

  public static OfflineMessageQueueImpl newInstance(QueuedMessageDao queuedMessageDao,
      MessagingService messagingService, NetworkMonitor networkMonitor,
      BackoffStrategy backoffStrategy, CoroutineScope coroutineScope) {
    return new OfflineMessageQueueImpl(queuedMessageDao, messagingService, networkMonitor, backoffStrategy, coroutineScope);
  }
}

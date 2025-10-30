package com.chain.messaging.core.sync;

import android.content.Context;
import com.chain.messaging.core.blockchain.BlockchainManager;
import com.chain.messaging.core.crypto.KeyManager;
import com.chain.messaging.data.local.dao.DeviceDao;
import com.chain.messaging.data.local.dao.MessageDao;
import com.chain.messaging.data.local.dao.SyncLogDao;
import com.chain.messaging.domain.repository.MessageRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class CrossDeviceSyncServiceImpl_Factory implements Factory<CrossDeviceSyncServiceImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<DeviceManager> deviceManagerProvider;

  private final Provider<DeviceDao> deviceDaoProvider;

  private final Provider<SyncLogDao> syncLogDaoProvider;

  private final Provider<MessageDao> messageDaoProvider;

  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<KeyManager> keyManagerProvider;

  private final Provider<BlockchainManager> blockchainManagerProvider;

  public CrossDeviceSyncServiceImpl_Factory(Provider<Context> contextProvider,
      Provider<DeviceManager> deviceManagerProvider, Provider<DeviceDao> deviceDaoProvider,
      Provider<SyncLogDao> syncLogDaoProvider, Provider<MessageDao> messageDaoProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<KeyManager> keyManagerProvider,
      Provider<BlockchainManager> blockchainManagerProvider) {
    this.contextProvider = contextProvider;
    this.deviceManagerProvider = deviceManagerProvider;
    this.deviceDaoProvider = deviceDaoProvider;
    this.syncLogDaoProvider = syncLogDaoProvider;
    this.messageDaoProvider = messageDaoProvider;
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.keyManagerProvider = keyManagerProvider;
    this.blockchainManagerProvider = blockchainManagerProvider;
  }

  @Override
  public CrossDeviceSyncServiceImpl get() {
    return newInstance(contextProvider.get(), deviceManagerProvider.get(), deviceDaoProvider.get(), syncLogDaoProvider.get(), messageDaoProvider.get(), messageRepositoryProvider.get(), keyManagerProvider.get(), blockchainManagerProvider.get());
  }

  public static CrossDeviceSyncServiceImpl_Factory create(Provider<Context> contextProvider,
      Provider<DeviceManager> deviceManagerProvider, Provider<DeviceDao> deviceDaoProvider,
      Provider<SyncLogDao> syncLogDaoProvider, Provider<MessageDao> messageDaoProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<KeyManager> keyManagerProvider,
      Provider<BlockchainManager> blockchainManagerProvider) {
    return new CrossDeviceSyncServiceImpl_Factory(contextProvider, deviceManagerProvider, deviceDaoProvider, syncLogDaoProvider, messageDaoProvider, messageRepositoryProvider, keyManagerProvider, blockchainManagerProvider);
  }

  public static CrossDeviceSyncServiceImpl newInstance(Context context, DeviceManager deviceManager,
      DeviceDao deviceDao, SyncLogDao syncLogDao, MessageDao messageDao,
      MessageRepository messageRepository, KeyManager keyManager,
      BlockchainManager blockchainManager) {
    return new CrossDeviceSyncServiceImpl(context, deviceManager, deviceDao, syncLogDao, messageDao, messageRepository, keyManager, blockchainManager);
  }
}

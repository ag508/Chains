package com.chain.messaging.core.messaging;

import com.chain.messaging.core.blockchain.BlockchainManager;
import com.chain.messaging.core.crypto.SignalEncryptionService;
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
public final class MessagingService_Factory implements Factory<MessagingService> {
  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<BlockchainManager> blockchainManagerProvider;

  private final Provider<SignalEncryptionService> encryptionServiceProvider;

  public MessagingService_Factory(Provider<MessageRepository> messageRepositoryProvider,
      Provider<BlockchainManager> blockchainManagerProvider,
      Provider<SignalEncryptionService> encryptionServiceProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.blockchainManagerProvider = blockchainManagerProvider;
    this.encryptionServiceProvider = encryptionServiceProvider;
  }

  @Override
  public MessagingService get() {
    return newInstance(messageRepositoryProvider.get(), blockchainManagerProvider.get(), encryptionServiceProvider.get());
  }

  public static MessagingService_Factory create(
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<BlockchainManager> blockchainManagerProvider,
      Provider<SignalEncryptionService> encryptionServiceProvider) {
    return new MessagingService_Factory(messageRepositoryProvider, blockchainManagerProvider, encryptionServiceProvider);
  }

  public static MessagingService newInstance(MessageRepository messageRepository,
      BlockchainManager blockchainManager, SignalEncryptionService encryptionService) {
    return new MessagingService(messageRepository, blockchainManager, encryptionService);
  }
}

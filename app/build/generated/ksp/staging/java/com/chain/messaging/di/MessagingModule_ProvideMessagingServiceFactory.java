package com.chain.messaging.di;

import com.chain.messaging.core.blockchain.BlockchainManager;
import com.chain.messaging.core.crypto.SignalEncryptionService;
import com.chain.messaging.core.messaging.MessagingService;
import com.chain.messaging.domain.repository.MessageRepository;
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
public final class MessagingModule_ProvideMessagingServiceFactory implements Factory<MessagingService> {
  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<BlockchainManager> blockchainManagerProvider;

  private final Provider<SignalEncryptionService> encryptionServiceProvider;

  public MessagingModule_ProvideMessagingServiceFactory(
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<BlockchainManager> blockchainManagerProvider,
      Provider<SignalEncryptionService> encryptionServiceProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.blockchainManagerProvider = blockchainManagerProvider;
    this.encryptionServiceProvider = encryptionServiceProvider;
  }

  @Override
  public MessagingService get() {
    return provideMessagingService(messageRepositoryProvider.get(), blockchainManagerProvider.get(), encryptionServiceProvider.get());
  }

  public static MessagingModule_ProvideMessagingServiceFactory create(
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<BlockchainManager> blockchainManagerProvider,
      Provider<SignalEncryptionService> encryptionServiceProvider) {
    return new MessagingModule_ProvideMessagingServiceFactory(messageRepositoryProvider, blockchainManagerProvider, encryptionServiceProvider);
  }

  public static MessagingService provideMessagingService(MessageRepository messageRepository,
      BlockchainManager blockchainManager, SignalEncryptionService encryptionService) {
    return Preconditions.checkNotNullFromProvides(MessagingModule.INSTANCE.provideMessagingService(messageRepository, blockchainManager, encryptionService));
  }
}

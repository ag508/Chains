package com.chain.messaging.core.group;

import com.chain.messaging.core.blockchain.BlockchainManager;
import com.chain.messaging.core.crypto.SignalEncryptionService;
import com.chain.messaging.core.p2p.P2PManager;
import com.chain.messaging.domain.repository.ChatRepository;
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
public final class GroupMessageDistributorImpl_Factory implements Factory<GroupMessageDistributorImpl> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<BlockchainManager> blockchainManagerProvider;

  private final Provider<SignalEncryptionService> encryptionServiceProvider;

  private final Provider<P2PManager> p2pManagerProvider;

  public GroupMessageDistributorImpl_Factory(Provider<ChatRepository> chatRepositoryProvider,
      Provider<BlockchainManager> blockchainManagerProvider,
      Provider<SignalEncryptionService> encryptionServiceProvider,
      Provider<P2PManager> p2pManagerProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.blockchainManagerProvider = blockchainManagerProvider;
    this.encryptionServiceProvider = encryptionServiceProvider;
    this.p2pManagerProvider = p2pManagerProvider;
  }

  @Override
  public GroupMessageDistributorImpl get() {
    return newInstance(chatRepositoryProvider.get(), blockchainManagerProvider.get(), encryptionServiceProvider.get(), p2pManagerProvider.get());
  }

  public static GroupMessageDistributorImpl_Factory create(
      Provider<ChatRepository> chatRepositoryProvider,
      Provider<BlockchainManager> blockchainManagerProvider,
      Provider<SignalEncryptionService> encryptionServiceProvider,
      Provider<P2PManager> p2pManagerProvider) {
    return new GroupMessageDistributorImpl_Factory(chatRepositoryProvider, blockchainManagerProvider, encryptionServiceProvider, p2pManagerProvider);
  }

  public static GroupMessageDistributorImpl newInstance(ChatRepository chatRepository,
      BlockchainManager blockchainManager, SignalEncryptionService encryptionService,
      P2PManager p2pManager) {
    return new GroupMessageDistributorImpl(chatRepository, blockchainManager, encryptionService, p2pManager);
  }
}

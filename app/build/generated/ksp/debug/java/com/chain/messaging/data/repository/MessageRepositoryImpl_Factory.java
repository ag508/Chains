package com.chain.messaging.data.repository;

import com.chain.messaging.core.auth.AuthenticationService;
import com.chain.messaging.core.blockchain.BlockchainManager;
import com.chain.messaging.core.crypto.SignalEncryptionService;
import com.chain.messaging.data.local.dao.MessageDao;
import com.chain.messaging.data.local.dao.ReactionDao;
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
public final class MessageRepositoryImpl_Factory implements Factory<MessageRepositoryImpl> {
  private final Provider<MessageDao> messageDaoProvider;

  private final Provider<ReactionDao> reactionDaoProvider;

  private final Provider<BlockchainManager> blockchainManagerProvider;

  private final Provider<SignalEncryptionService> encryptionServiceProvider;

  private final Provider<AuthenticationService> authenticationServiceProvider;

  public MessageRepositoryImpl_Factory(Provider<MessageDao> messageDaoProvider,
      Provider<ReactionDao> reactionDaoProvider,
      Provider<BlockchainManager> blockchainManagerProvider,
      Provider<SignalEncryptionService> encryptionServiceProvider,
      Provider<AuthenticationService> authenticationServiceProvider) {
    this.messageDaoProvider = messageDaoProvider;
    this.reactionDaoProvider = reactionDaoProvider;
    this.blockchainManagerProvider = blockchainManagerProvider;
    this.encryptionServiceProvider = encryptionServiceProvider;
    this.authenticationServiceProvider = authenticationServiceProvider;
  }

  @Override
  public MessageRepositoryImpl get() {
    return newInstance(messageDaoProvider.get(), reactionDaoProvider.get(), blockchainManagerProvider.get(), encryptionServiceProvider.get(), authenticationServiceProvider.get());
  }

  public static MessageRepositoryImpl_Factory create(Provider<MessageDao> messageDaoProvider,
      Provider<ReactionDao> reactionDaoProvider,
      Provider<BlockchainManager> blockchainManagerProvider,
      Provider<SignalEncryptionService> encryptionServiceProvider,
      Provider<AuthenticationService> authenticationServiceProvider) {
    return new MessageRepositoryImpl_Factory(messageDaoProvider, reactionDaoProvider, blockchainManagerProvider, encryptionServiceProvider, authenticationServiceProvider);
  }

  public static MessageRepositoryImpl newInstance(MessageDao messageDao, ReactionDao reactionDao,
      BlockchainManager blockchainManager, SignalEncryptionService encryptionService,
      AuthenticationService authenticationService) {
    return new MessageRepositoryImpl(messageDao, reactionDao, blockchainManager, encryptionService, authenticationService);
  }
}

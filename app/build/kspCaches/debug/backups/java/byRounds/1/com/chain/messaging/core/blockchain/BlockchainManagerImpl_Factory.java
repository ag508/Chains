package com.chain.messaging.core.blockchain;

import com.chain.messaging.core.auth.AuthenticationService;
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
public final class BlockchainManagerImpl_Factory implements Factory<BlockchainManagerImpl> {
  private final Provider<TransactionSigner> transactionSignerProvider;

  private final Provider<ConsensusHandler> consensusHandlerProvider;

  private final Provider<AuthenticationService> authenticationServiceProvider;

  public BlockchainManagerImpl_Factory(Provider<TransactionSigner> transactionSignerProvider,
      Provider<ConsensusHandler> consensusHandlerProvider,
      Provider<AuthenticationService> authenticationServiceProvider) {
    this.transactionSignerProvider = transactionSignerProvider;
    this.consensusHandlerProvider = consensusHandlerProvider;
    this.authenticationServiceProvider = authenticationServiceProvider;
  }

  @Override
  public BlockchainManagerImpl get() {
    return newInstance(transactionSignerProvider.get(), consensusHandlerProvider.get(), authenticationServiceProvider.get());
  }

  public static BlockchainManagerImpl_Factory create(
      Provider<TransactionSigner> transactionSignerProvider,
      Provider<ConsensusHandler> consensusHandlerProvider,
      Provider<AuthenticationService> authenticationServiceProvider) {
    return new BlockchainManagerImpl_Factory(transactionSignerProvider, consensusHandlerProvider, authenticationServiceProvider);
  }

  public static BlockchainManagerImpl newInstance(TransactionSigner transactionSigner,
      ConsensusHandler consensusHandler, AuthenticationService authenticationService) {
    return new BlockchainManagerImpl(transactionSigner, consensusHandler, authenticationService);
  }
}

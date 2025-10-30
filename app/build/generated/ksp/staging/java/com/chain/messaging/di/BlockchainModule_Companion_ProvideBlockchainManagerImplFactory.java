package com.chain.messaging.di;

import com.chain.messaging.core.auth.AuthenticationService;
import com.chain.messaging.core.blockchain.BlockchainManagerImpl;
import com.chain.messaging.core.blockchain.ConsensusHandler;
import com.chain.messaging.core.blockchain.TransactionSigner;
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
public final class BlockchainModule_Companion_ProvideBlockchainManagerImplFactory implements Factory<BlockchainManagerImpl> {
  private final Provider<TransactionSigner> transactionSignerProvider;

  private final Provider<ConsensusHandler> consensusHandlerProvider;

  private final Provider<AuthenticationService> authenticationServiceProvider;

  public BlockchainModule_Companion_ProvideBlockchainManagerImplFactory(
      Provider<TransactionSigner> transactionSignerProvider,
      Provider<ConsensusHandler> consensusHandlerProvider,
      Provider<AuthenticationService> authenticationServiceProvider) {
    this.transactionSignerProvider = transactionSignerProvider;
    this.consensusHandlerProvider = consensusHandlerProvider;
    this.authenticationServiceProvider = authenticationServiceProvider;
  }

  @Override
  public BlockchainManagerImpl get() {
    return provideBlockchainManagerImpl(transactionSignerProvider.get(), consensusHandlerProvider.get(), authenticationServiceProvider.get());
  }

  public static BlockchainModule_Companion_ProvideBlockchainManagerImplFactory create(
      Provider<TransactionSigner> transactionSignerProvider,
      Provider<ConsensusHandler> consensusHandlerProvider,
      Provider<AuthenticationService> authenticationServiceProvider) {
    return new BlockchainModule_Companion_ProvideBlockchainManagerImplFactory(transactionSignerProvider, consensusHandlerProvider, authenticationServiceProvider);
  }

  public static BlockchainManagerImpl provideBlockchainManagerImpl(
      TransactionSigner transactionSigner, ConsensusHandler consensusHandler,
      AuthenticationService authenticationService) {
    return Preconditions.checkNotNullFromProvides(BlockchainModule.Companion.provideBlockchainManagerImpl(transactionSigner, consensusHandler, authenticationService));
  }
}

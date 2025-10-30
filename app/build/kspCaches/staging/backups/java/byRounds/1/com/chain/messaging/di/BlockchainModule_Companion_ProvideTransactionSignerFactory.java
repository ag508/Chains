package com.chain.messaging.di;

import com.chain.messaging.core.blockchain.TransactionSigner;
import com.chain.messaging.core.crypto.KeyManager;
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
public final class BlockchainModule_Companion_ProvideTransactionSignerFactory implements Factory<TransactionSigner> {
  private final Provider<KeyManager> keyManagerProvider;

  public BlockchainModule_Companion_ProvideTransactionSignerFactory(
      Provider<KeyManager> keyManagerProvider) {
    this.keyManagerProvider = keyManagerProvider;
  }

  @Override
  public TransactionSigner get() {
    return provideTransactionSigner(keyManagerProvider.get());
  }

  public static BlockchainModule_Companion_ProvideTransactionSignerFactory create(
      Provider<KeyManager> keyManagerProvider) {
    return new BlockchainModule_Companion_ProvideTransactionSignerFactory(keyManagerProvider);
  }

  public static TransactionSigner provideTransactionSigner(KeyManager keyManager) {
    return Preconditions.checkNotNullFromProvides(BlockchainModule.Companion.provideTransactionSigner(keyManager));
  }
}

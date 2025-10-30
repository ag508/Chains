package com.chain.messaging.core.webrtc;

import com.chain.messaging.core.blockchain.BlockchainManager;
import com.chain.messaging.core.crypto.SignalEncryptionService;
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
public final class CallSignalingService_Factory implements Factory<CallSignalingService> {
  private final Provider<BlockchainManager> blockchainManagerProvider;

  private final Provider<SignalEncryptionService> encryptionServiceProvider;

  public CallSignalingService_Factory(Provider<BlockchainManager> blockchainManagerProvider,
      Provider<SignalEncryptionService> encryptionServiceProvider) {
    this.blockchainManagerProvider = blockchainManagerProvider;
    this.encryptionServiceProvider = encryptionServiceProvider;
  }

  @Override
  public CallSignalingService get() {
    return newInstance(blockchainManagerProvider.get(), encryptionServiceProvider.get());
  }

  public static CallSignalingService_Factory create(
      Provider<BlockchainManager> blockchainManagerProvider,
      Provider<SignalEncryptionService> encryptionServiceProvider) {
    return new CallSignalingService_Factory(blockchainManagerProvider, encryptionServiceProvider);
  }

  public static CallSignalingService newInstance(BlockchainManager blockchainManager,
      SignalEncryptionService encryptionService) {
    return new CallSignalingService(blockchainManager, encryptionService);
  }
}

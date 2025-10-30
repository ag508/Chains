package com.chain.messaging.di;

import com.chain.messaging.core.blockchain.BlockchainManager;
import com.chain.messaging.core.crypto.SignalEncryptionService;
import com.chain.messaging.core.webrtc.CallSignalingService;
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
public final class WebRTCModule_ProvideCallSignalingServiceFactory implements Factory<CallSignalingService> {
  private final Provider<BlockchainManager> blockchainManagerProvider;

  private final Provider<SignalEncryptionService> encryptionServiceProvider;

  public WebRTCModule_ProvideCallSignalingServiceFactory(
      Provider<BlockchainManager> blockchainManagerProvider,
      Provider<SignalEncryptionService> encryptionServiceProvider) {
    this.blockchainManagerProvider = blockchainManagerProvider;
    this.encryptionServiceProvider = encryptionServiceProvider;
  }

  @Override
  public CallSignalingService get() {
    return provideCallSignalingService(blockchainManagerProvider.get(), encryptionServiceProvider.get());
  }

  public static WebRTCModule_ProvideCallSignalingServiceFactory create(
      Provider<BlockchainManager> blockchainManagerProvider,
      Provider<SignalEncryptionService> encryptionServiceProvider) {
    return new WebRTCModule_ProvideCallSignalingServiceFactory(blockchainManagerProvider, encryptionServiceProvider);
  }

  public static CallSignalingService provideCallSignalingService(
      BlockchainManager blockchainManager, SignalEncryptionService encryptionService) {
    return Preconditions.checkNotNullFromProvides(WebRTCModule.INSTANCE.provideCallSignalingService(blockchainManager, encryptionService));
  }
}

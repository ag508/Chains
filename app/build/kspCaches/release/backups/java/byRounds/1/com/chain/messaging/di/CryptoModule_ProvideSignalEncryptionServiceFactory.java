package com.chain.messaging.di;

import com.chain.messaging.core.crypto.SignalEncryptionService;
import com.chain.messaging.core.crypto.SignalProtocolStoreAdapter;
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
public final class CryptoModule_ProvideSignalEncryptionServiceFactory implements Factory<SignalEncryptionService> {
  private final Provider<SignalProtocolStoreAdapter> protocolStoreAdapterProvider;

  public CryptoModule_ProvideSignalEncryptionServiceFactory(
      Provider<SignalProtocolStoreAdapter> protocolStoreAdapterProvider) {
    this.protocolStoreAdapterProvider = protocolStoreAdapterProvider;
  }

  @Override
  public SignalEncryptionService get() {
    return provideSignalEncryptionService(protocolStoreAdapterProvider.get());
  }

  public static CryptoModule_ProvideSignalEncryptionServiceFactory create(
      Provider<SignalProtocolStoreAdapter> protocolStoreAdapterProvider) {
    return new CryptoModule_ProvideSignalEncryptionServiceFactory(protocolStoreAdapterProvider);
  }

  public static SignalEncryptionService provideSignalEncryptionService(
      SignalProtocolStoreAdapter protocolStoreAdapter) {
    return Preconditions.checkNotNullFromProvides(CryptoModule.INSTANCE.provideSignalEncryptionService(protocolStoreAdapter));
  }
}

package com.chain.messaging.core.crypto;

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
public final class SignalEncryptionService_Factory implements Factory<SignalEncryptionService> {
  private final Provider<SignalProtocolStoreAdapter> protocolStoreProvider;

  public SignalEncryptionService_Factory(
      Provider<SignalProtocolStoreAdapter> protocolStoreProvider) {
    this.protocolStoreProvider = protocolStoreProvider;
  }

  @Override
  public SignalEncryptionService get() {
    return newInstance(protocolStoreProvider.get());
  }

  public static SignalEncryptionService_Factory create(
      Provider<SignalProtocolStoreAdapter> protocolStoreProvider) {
    return new SignalEncryptionService_Factory(protocolStoreProvider);
  }

  public static SignalEncryptionService newInstance(SignalProtocolStoreAdapter protocolStore) {
    return new SignalEncryptionService(protocolStore);
  }
}

package com.chain.messaging.core.group;

import com.chain.messaging.core.crypto.SignalEncryptionService;
import com.chain.messaging.core.crypto.SignalProtocolStoreAdapter;
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
public final class GroupEncryptionManagerImpl_Factory implements Factory<GroupEncryptionManagerImpl> {
  private final Provider<SignalEncryptionService> signalEncryptionServiceProvider;

  private final Provider<SignalProtocolStoreAdapter> signalProtocolStoreProvider;

  public GroupEncryptionManagerImpl_Factory(
      Provider<SignalEncryptionService> signalEncryptionServiceProvider,
      Provider<SignalProtocolStoreAdapter> signalProtocolStoreProvider) {
    this.signalEncryptionServiceProvider = signalEncryptionServiceProvider;
    this.signalProtocolStoreProvider = signalProtocolStoreProvider;
  }

  @Override
  public GroupEncryptionManagerImpl get() {
    return newInstance(signalEncryptionServiceProvider.get(), signalProtocolStoreProvider.get());
  }

  public static GroupEncryptionManagerImpl_Factory create(
      Provider<SignalEncryptionService> signalEncryptionServiceProvider,
      Provider<SignalProtocolStoreAdapter> signalProtocolStoreProvider) {
    return new GroupEncryptionManagerImpl_Factory(signalEncryptionServiceProvider, signalProtocolStoreProvider);
  }

  public static GroupEncryptionManagerImpl newInstance(
      SignalEncryptionService signalEncryptionService,
      SignalProtocolStoreAdapter signalProtocolStore) {
    return new GroupEncryptionManagerImpl(signalEncryptionService, signalProtocolStore);
  }
}

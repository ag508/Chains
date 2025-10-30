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
public final class SignalProtocolStore_Factory implements Factory<SignalProtocolStore> {
  private final Provider<KeyManager> keyManagerProvider;

  private final Provider<SessionStorage> sessionStorageProvider;

  private final Provider<IdentityStorage> identityStorageProvider;

  public SignalProtocolStore_Factory(Provider<KeyManager> keyManagerProvider,
      Provider<SessionStorage> sessionStorageProvider,
      Provider<IdentityStorage> identityStorageProvider) {
    this.keyManagerProvider = keyManagerProvider;
    this.sessionStorageProvider = sessionStorageProvider;
    this.identityStorageProvider = identityStorageProvider;
  }

  @Override
  public SignalProtocolStore get() {
    return newInstance(keyManagerProvider.get(), sessionStorageProvider.get(), identityStorageProvider.get());
  }

  public static SignalProtocolStore_Factory create(Provider<KeyManager> keyManagerProvider,
      Provider<SessionStorage> sessionStorageProvider,
      Provider<IdentityStorage> identityStorageProvider) {
    return new SignalProtocolStore_Factory(keyManagerProvider, sessionStorageProvider, identityStorageProvider);
  }

  public static SignalProtocolStore newInstance(KeyManager keyManager,
      SessionStorage sessionStorage, IdentityStorage identityStorage) {
    return new SignalProtocolStore(keyManager, sessionStorage, identityStorage);
  }
}

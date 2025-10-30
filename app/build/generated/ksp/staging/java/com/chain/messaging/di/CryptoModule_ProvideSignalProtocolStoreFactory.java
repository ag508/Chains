package com.chain.messaging.di;

import com.chain.messaging.core.crypto.IdentityStorage;
import com.chain.messaging.core.crypto.KeyManager;
import com.chain.messaging.core.crypto.SessionStorage;
import com.chain.messaging.core.crypto.SignalProtocolStore;
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
public final class CryptoModule_ProvideSignalProtocolStoreFactory implements Factory<SignalProtocolStore> {
  private final Provider<KeyManager> keyManagerProvider;

  private final Provider<SessionStorage> sessionStorageProvider;

  private final Provider<IdentityStorage> identityStorageProvider;

  public CryptoModule_ProvideSignalProtocolStoreFactory(Provider<KeyManager> keyManagerProvider,
      Provider<SessionStorage> sessionStorageProvider,
      Provider<IdentityStorage> identityStorageProvider) {
    this.keyManagerProvider = keyManagerProvider;
    this.sessionStorageProvider = sessionStorageProvider;
    this.identityStorageProvider = identityStorageProvider;
  }

  @Override
  public SignalProtocolStore get() {
    return provideSignalProtocolStore(keyManagerProvider.get(), sessionStorageProvider.get(), identityStorageProvider.get());
  }

  public static CryptoModule_ProvideSignalProtocolStoreFactory create(
      Provider<KeyManager> keyManagerProvider, Provider<SessionStorage> sessionStorageProvider,
      Provider<IdentityStorage> identityStorageProvider) {
    return new CryptoModule_ProvideSignalProtocolStoreFactory(keyManagerProvider, sessionStorageProvider, identityStorageProvider);
  }

  public static SignalProtocolStore provideSignalProtocolStore(KeyManager keyManager,
      SessionStorage sessionStorage, IdentityStorage identityStorage) {
    return Preconditions.checkNotNullFromProvides(CryptoModule.INSTANCE.provideSignalProtocolStore(keyManager, sessionStorage, identityStorage));
  }
}

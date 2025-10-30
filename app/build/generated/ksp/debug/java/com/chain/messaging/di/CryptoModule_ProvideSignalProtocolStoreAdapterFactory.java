package com.chain.messaging.di;

import com.chain.messaging.core.crypto.IdentityStorageImpl;
import com.chain.messaging.core.crypto.KeyManager;
import com.chain.messaging.core.crypto.SenderKeyStoreImpl;
import com.chain.messaging.core.crypto.SessionStorageImpl;
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
public final class CryptoModule_ProvideSignalProtocolStoreAdapterFactory implements Factory<SignalProtocolStoreAdapter> {
  private final Provider<IdentityStorageImpl> identityStorageProvider;

  private final Provider<SessionStorageImpl> sessionStorageProvider;

  private final Provider<SenderKeyStoreImpl> senderKeyStoreProvider;

  private final Provider<KeyManager> keyManagerProvider;

  public CryptoModule_ProvideSignalProtocolStoreAdapterFactory(
      Provider<IdentityStorageImpl> identityStorageProvider,
      Provider<SessionStorageImpl> sessionStorageProvider,
      Provider<SenderKeyStoreImpl> senderKeyStoreProvider,
      Provider<KeyManager> keyManagerProvider) {
    this.identityStorageProvider = identityStorageProvider;
    this.sessionStorageProvider = sessionStorageProvider;
    this.senderKeyStoreProvider = senderKeyStoreProvider;
    this.keyManagerProvider = keyManagerProvider;
  }

  @Override
  public SignalProtocolStoreAdapter get() {
    return provideSignalProtocolStoreAdapter(identityStorageProvider.get(), sessionStorageProvider.get(), senderKeyStoreProvider.get(), keyManagerProvider.get());
  }

  public static CryptoModule_ProvideSignalProtocolStoreAdapterFactory create(
      Provider<IdentityStorageImpl> identityStorageProvider,
      Provider<SessionStorageImpl> sessionStorageProvider,
      Provider<SenderKeyStoreImpl> senderKeyStoreProvider,
      Provider<KeyManager> keyManagerProvider) {
    return new CryptoModule_ProvideSignalProtocolStoreAdapterFactory(identityStorageProvider, sessionStorageProvider, senderKeyStoreProvider, keyManagerProvider);
  }

  public static SignalProtocolStoreAdapter provideSignalProtocolStoreAdapter(
      IdentityStorageImpl identityStorage, SessionStorageImpl sessionStorage,
      SenderKeyStoreImpl senderKeyStore, KeyManager keyManager) {
    return Preconditions.checkNotNullFromProvides(CryptoModule.INSTANCE.provideSignalProtocolStoreAdapter(identityStorage, sessionStorage, senderKeyStore, keyManager));
  }
}

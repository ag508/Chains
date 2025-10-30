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
public final class SignalProtocolStoreAdapter_Factory implements Factory<SignalProtocolStoreAdapter> {
  private final Provider<IdentityStorageImpl> chainIdentityStoreProvider;

  private final Provider<SessionStorageImpl> chainSessionStoreProvider;

  private final Provider<SenderKeyStoreImpl> chainSenderKeyStoreProvider;

  private final Provider<KeyManager> keyManagerProvider;

  public SignalProtocolStoreAdapter_Factory(
      Provider<IdentityStorageImpl> chainIdentityStoreProvider,
      Provider<SessionStorageImpl> chainSessionStoreProvider,
      Provider<SenderKeyStoreImpl> chainSenderKeyStoreProvider,
      Provider<KeyManager> keyManagerProvider) {
    this.chainIdentityStoreProvider = chainIdentityStoreProvider;
    this.chainSessionStoreProvider = chainSessionStoreProvider;
    this.chainSenderKeyStoreProvider = chainSenderKeyStoreProvider;
    this.keyManagerProvider = keyManagerProvider;
  }

  @Override
  public SignalProtocolStoreAdapter get() {
    return newInstance(chainIdentityStoreProvider.get(), chainSessionStoreProvider.get(), chainSenderKeyStoreProvider.get(), keyManagerProvider.get());
  }

  public static SignalProtocolStoreAdapter_Factory create(
      Provider<IdentityStorageImpl> chainIdentityStoreProvider,
      Provider<SessionStorageImpl> chainSessionStoreProvider,
      Provider<SenderKeyStoreImpl> chainSenderKeyStoreProvider,
      Provider<KeyManager> keyManagerProvider) {
    return new SignalProtocolStoreAdapter_Factory(chainIdentityStoreProvider, chainSessionStoreProvider, chainSenderKeyStoreProvider, keyManagerProvider);
  }

  public static SignalProtocolStoreAdapter newInstance(IdentityStorageImpl chainIdentityStore,
      SessionStorageImpl chainSessionStore, SenderKeyStoreImpl chainSenderKeyStore,
      KeyManager keyManager) {
    return new SignalProtocolStoreAdapter(chainIdentityStore, chainSessionStore, chainSenderKeyStore, keyManager);
  }
}

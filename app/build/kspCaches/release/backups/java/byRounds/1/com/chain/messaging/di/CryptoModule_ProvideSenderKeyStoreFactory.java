package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.crypto.SenderKeyStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class CryptoModule_ProvideSenderKeyStoreFactory implements Factory<SenderKeyStore> {
  private final Provider<Context> contextProvider;

  public CryptoModule_ProvideSenderKeyStoreFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SenderKeyStore get() {
    return provideSenderKeyStore(contextProvider.get());
  }

  public static CryptoModule_ProvideSenderKeyStoreFactory create(
      Provider<Context> contextProvider) {
    return new CryptoModule_ProvideSenderKeyStoreFactory(contextProvider);
  }

  public static SenderKeyStore provideSenderKeyStore(Context context) {
    return Preconditions.checkNotNullFromProvides(CryptoModule.INSTANCE.provideSenderKeyStore(context));
  }
}

package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.crypto.KeyManager;
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
public final class CryptoModule_ProvideKeyManagerFactory implements Factory<KeyManager> {
  private final Provider<Context> contextProvider;

  public CryptoModule_ProvideKeyManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public KeyManager get() {
    return provideKeyManager(contextProvider.get());
  }

  public static CryptoModule_ProvideKeyManagerFactory create(Provider<Context> contextProvider) {
    return new CryptoModule_ProvideKeyManagerFactory(contextProvider);
  }

  public static KeyManager provideKeyManager(Context context) {
    return Preconditions.checkNotNullFromProvides(CryptoModule.INSTANCE.provideKeyManager(context));
  }
}

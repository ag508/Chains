package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.crypto.SessionStorage;
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
public final class CryptoModule_ProvideSessionStorageFactory implements Factory<SessionStorage> {
  private final Provider<Context> contextProvider;

  public CryptoModule_ProvideSessionStorageFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SessionStorage get() {
    return provideSessionStorage(contextProvider.get());
  }

  public static CryptoModule_ProvideSessionStorageFactory create(
      Provider<Context> contextProvider) {
    return new CryptoModule_ProvideSessionStorageFactory(contextProvider);
  }

  public static SessionStorage provideSessionStorage(Context context) {
    return Preconditions.checkNotNullFromProvides(CryptoModule.INSTANCE.provideSessionStorage(context));
  }
}

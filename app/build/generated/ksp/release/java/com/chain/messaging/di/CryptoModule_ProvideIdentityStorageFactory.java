package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.crypto.IdentityStorage;
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
public final class CryptoModule_ProvideIdentityStorageFactory implements Factory<IdentityStorage> {
  private final Provider<Context> contextProvider;

  public CryptoModule_ProvideIdentityStorageFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public IdentityStorage get() {
    return provideIdentityStorage(contextProvider.get());
  }

  public static CryptoModule_ProvideIdentityStorageFactory create(
      Provider<Context> contextProvider) {
    return new CryptoModule_ProvideIdentityStorageFactory(contextProvider);
  }

  public static IdentityStorage provideIdentityStorage(Context context) {
    return Preconditions.checkNotNullFromProvides(CryptoModule.INSTANCE.provideIdentityStorage(context));
  }
}

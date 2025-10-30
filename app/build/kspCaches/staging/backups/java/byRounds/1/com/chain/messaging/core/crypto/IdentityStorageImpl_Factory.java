package com.chain.messaging.core.crypto;

import android.content.Context;
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
public final class IdentityStorageImpl_Factory implements Factory<IdentityStorageImpl> {
  private final Provider<Context> contextProvider;

  public IdentityStorageImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public IdentityStorageImpl get() {
    return newInstance(contextProvider.get());
  }

  public static IdentityStorageImpl_Factory create(Provider<Context> contextProvider) {
    return new IdentityStorageImpl_Factory(contextProvider);
  }

  public static IdentityStorageImpl newInstance(Context context) {
    return new IdentityStorageImpl(context);
  }
}

package com.chain.messaging.core.security;

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
public final class SecureStorageImpl_Factory implements Factory<SecureStorageImpl> {
  private final Provider<Context> contextProvider;

  public SecureStorageImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SecureStorageImpl get() {
    return newInstance(contextProvider.get());
  }

  public static SecureStorageImpl_Factory create(Provider<Context> contextProvider) {
    return new SecureStorageImpl_Factory(contextProvider);
  }

  public static SecureStorageImpl newInstance(Context context) {
    return new SecureStorageImpl(context);
  }
}

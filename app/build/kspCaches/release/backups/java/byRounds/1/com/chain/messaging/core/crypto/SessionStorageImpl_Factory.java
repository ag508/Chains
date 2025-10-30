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
public final class SessionStorageImpl_Factory implements Factory<SessionStorageImpl> {
  private final Provider<Context> contextProvider;

  public SessionStorageImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SessionStorageImpl get() {
    return newInstance(contextProvider.get());
  }

  public static SessionStorageImpl_Factory create(Provider<Context> contextProvider) {
    return new SessionStorageImpl_Factory(contextProvider);
  }

  public static SessionStorageImpl newInstance(Context context) {
    return new SessionStorageImpl(context);
  }
}

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
public final class SenderKeyStoreImpl_Factory implements Factory<SenderKeyStoreImpl> {
  private final Provider<Context> contextProvider;

  public SenderKeyStoreImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SenderKeyStoreImpl get() {
    return newInstance(contextProvider.get());
  }

  public static SenderKeyStoreImpl_Factory create(Provider<Context> contextProvider) {
    return new SenderKeyStoreImpl_Factory(contextProvider);
  }

  public static SenderKeyStoreImpl newInstance(Context context) {
    return new SenderKeyStoreImpl(context);
  }
}

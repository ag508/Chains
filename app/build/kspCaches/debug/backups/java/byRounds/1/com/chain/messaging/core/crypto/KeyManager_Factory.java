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
public final class KeyManager_Factory implements Factory<KeyManager> {
  private final Provider<Context> contextProvider;

  public KeyManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public KeyManager get() {
    return newInstance(contextProvider.get());
  }

  public static KeyManager_Factory create(Provider<Context> contextProvider) {
    return new KeyManager_Factory(contextProvider);
  }

  public static KeyManager newInstance(Context context) {
    return new KeyManager(context);
  }
}

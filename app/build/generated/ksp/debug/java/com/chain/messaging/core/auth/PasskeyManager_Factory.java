package com.chain.messaging.core.auth;

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
public final class PasskeyManager_Factory implements Factory<PasskeyManager> {
  private final Provider<Context> contextProvider;

  public PasskeyManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PasskeyManager get() {
    return newInstance(contextProvider.get());
  }

  public static PasskeyManager_Factory create(Provider<Context> contextProvider) {
    return new PasskeyManager_Factory(contextProvider);
  }

  public static PasskeyManager newInstance(Context context) {
    return new PasskeyManager(context);
  }
}

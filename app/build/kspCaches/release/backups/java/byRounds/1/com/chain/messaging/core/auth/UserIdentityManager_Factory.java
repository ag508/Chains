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
public final class UserIdentityManager_Factory implements Factory<UserIdentityManager> {
  private final Provider<Context> contextProvider;

  public UserIdentityManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public UserIdentityManager get() {
    return newInstance(contextProvider.get());
  }

  public static UserIdentityManager_Factory create(Provider<Context> contextProvider) {
    return new UserIdentityManager_Factory(contextProvider);
  }

  public static UserIdentityManager newInstance(Context context) {
    return new UserIdentityManager(context);
  }
}

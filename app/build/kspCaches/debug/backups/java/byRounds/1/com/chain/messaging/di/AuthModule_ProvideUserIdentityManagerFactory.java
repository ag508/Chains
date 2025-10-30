package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.auth.UserIdentityManager;
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
public final class AuthModule_ProvideUserIdentityManagerFactory implements Factory<UserIdentityManager> {
  private final Provider<Context> contextProvider;

  public AuthModule_ProvideUserIdentityManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public UserIdentityManager get() {
    return provideUserIdentityManager(contextProvider.get());
  }

  public static AuthModule_ProvideUserIdentityManagerFactory create(
      Provider<Context> contextProvider) {
    return new AuthModule_ProvideUserIdentityManagerFactory(contextProvider);
  }

  public static UserIdentityManager provideUserIdentityManager(Context context) {
    return Preconditions.checkNotNullFromProvides(AuthModule.INSTANCE.provideUserIdentityManager(context));
  }
}

package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.auth.OAuthManager;
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
public final class AuthModule_ProvideOAuthManagerFactory implements Factory<OAuthManager> {
  private final Provider<Context> contextProvider;

  public AuthModule_ProvideOAuthManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public OAuthManager get() {
    return provideOAuthManager(contextProvider.get());
  }

  public static AuthModule_ProvideOAuthManagerFactory create(Provider<Context> contextProvider) {
    return new AuthModule_ProvideOAuthManagerFactory(contextProvider);
  }

  public static OAuthManager provideOAuthManager(Context context) {
    return Preconditions.checkNotNullFromProvides(AuthModule.INSTANCE.provideOAuthManager(context));
  }
}

package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.auth.AuthenticationService;
import com.chain.messaging.core.auth.OAuthManager;
import com.chain.messaging.core.auth.PasskeyManager;
import com.chain.messaging.core.auth.UserIdentityManager;
import com.chain.messaging.core.crypto.KeyManager;
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
public final class AuthModule_ProvideAuthenticationServiceFactory implements Factory<AuthenticationService> {
  private final Provider<Context> contextProvider;

  private final Provider<KeyManager> keyManagerProvider;

  private final Provider<UserIdentityManager> userIdentityManagerProvider;

  private final Provider<OAuthManager> oAuthManagerProvider;

  private final Provider<PasskeyManager> passkeyManagerProvider;

  public AuthModule_ProvideAuthenticationServiceFactory(Provider<Context> contextProvider,
      Provider<KeyManager> keyManagerProvider,
      Provider<UserIdentityManager> userIdentityManagerProvider,
      Provider<OAuthManager> oAuthManagerProvider,
      Provider<PasskeyManager> passkeyManagerProvider) {
    this.contextProvider = contextProvider;
    this.keyManagerProvider = keyManagerProvider;
    this.userIdentityManagerProvider = userIdentityManagerProvider;
    this.oAuthManagerProvider = oAuthManagerProvider;
    this.passkeyManagerProvider = passkeyManagerProvider;
  }

  @Override
  public AuthenticationService get() {
    return provideAuthenticationService(contextProvider.get(), keyManagerProvider.get(), userIdentityManagerProvider.get(), oAuthManagerProvider.get(), passkeyManagerProvider.get());
  }

  public static AuthModule_ProvideAuthenticationServiceFactory create(
      Provider<Context> contextProvider, Provider<KeyManager> keyManagerProvider,
      Provider<UserIdentityManager> userIdentityManagerProvider,
      Provider<OAuthManager> oAuthManagerProvider,
      Provider<PasskeyManager> passkeyManagerProvider) {
    return new AuthModule_ProvideAuthenticationServiceFactory(contextProvider, keyManagerProvider, userIdentityManagerProvider, oAuthManagerProvider, passkeyManagerProvider);
  }

  public static AuthenticationService provideAuthenticationService(Context context,
      KeyManager keyManager, UserIdentityManager userIdentityManager, OAuthManager oAuthManager,
      PasskeyManager passkeyManager) {
    return Preconditions.checkNotNullFromProvides(AuthModule.INSTANCE.provideAuthenticationService(context, keyManager, userIdentityManager, oAuthManager, passkeyManager));
  }
}

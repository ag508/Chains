package com.chain.messaging.core.auth;

import android.content.Context;
import com.chain.messaging.core.crypto.KeyManager;
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
public final class AuthenticationService_Factory implements Factory<AuthenticationService> {
  private final Provider<Context> contextProvider;

  private final Provider<KeyManager> keyManagerProvider;

  private final Provider<UserIdentityManager> userIdentityManagerProvider;

  private final Provider<OAuthManager> oAuthManagerProvider;

  private final Provider<PasskeyManager> passkeyManagerProvider;

  public AuthenticationService_Factory(Provider<Context> contextProvider,
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
    return newInstance(contextProvider.get(), keyManagerProvider.get(), userIdentityManagerProvider.get(), oAuthManagerProvider.get(), passkeyManagerProvider.get());
  }

  public static AuthenticationService_Factory create(Provider<Context> contextProvider,
      Provider<KeyManager> keyManagerProvider,
      Provider<UserIdentityManager> userIdentityManagerProvider,
      Provider<OAuthManager> oAuthManagerProvider,
      Provider<PasskeyManager> passkeyManagerProvider) {
    return new AuthenticationService_Factory(contextProvider, keyManagerProvider, userIdentityManagerProvider, oAuthManagerProvider, passkeyManagerProvider);
  }

  public static AuthenticationService newInstance(Context context, KeyManager keyManager,
      UserIdentityManager userIdentityManager, OAuthManager oAuthManager,
      PasskeyManager passkeyManager) {
    return new AuthenticationService(context, keyManager, userIdentityManager, oAuthManager, passkeyManager);
  }
}

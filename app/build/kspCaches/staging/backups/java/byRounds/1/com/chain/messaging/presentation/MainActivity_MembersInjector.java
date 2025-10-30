package com.chain.messaging.presentation;

import com.chain.messaging.core.cloud.OAuthCallbackHandler;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<OAuthCallbackHandler> oAuthCallbackHandlerProvider;

  public MainActivity_MembersInjector(Provider<OAuthCallbackHandler> oAuthCallbackHandlerProvider) {
    this.oAuthCallbackHandlerProvider = oAuthCallbackHandlerProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<OAuthCallbackHandler> oAuthCallbackHandlerProvider) {
    return new MainActivity_MembersInjector(oAuthCallbackHandlerProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectOAuthCallbackHandler(instance, oAuthCallbackHandlerProvider.get());
  }

  @InjectedFieldSignature("com.chain.messaging.presentation.MainActivity.oAuthCallbackHandler")
  public static void injectOAuthCallbackHandler(MainActivity instance,
      OAuthCallbackHandler oAuthCallbackHandler) {
    instance.oAuthCallbackHandler = oAuthCallbackHandler;
  }
}

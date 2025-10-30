package com.chain.messaging.di;

import com.chain.messaging.core.cloud.OAuthCallbackHandler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class CloudModule_Companion_ProvideOAuthCallbackHandlerFactory implements Factory<OAuthCallbackHandler> {
  @Override
  public OAuthCallbackHandler get() {
    return provideOAuthCallbackHandler();
  }

  public static CloudModule_Companion_ProvideOAuthCallbackHandlerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OAuthCallbackHandler provideOAuthCallbackHandler() {
    return Preconditions.checkNotNullFromProvides(CloudModule.Companion.provideOAuthCallbackHandler());
  }

  private static final class InstanceHolder {
    private static final CloudModule_Companion_ProvideOAuthCallbackHandlerFactory INSTANCE = new CloudModule_Companion_ProvideOAuthCallbackHandlerFactory();
  }
}

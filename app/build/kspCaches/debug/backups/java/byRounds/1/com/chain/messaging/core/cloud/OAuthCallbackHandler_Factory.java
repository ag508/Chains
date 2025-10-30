package com.chain.messaging.core.cloud;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class OAuthCallbackHandler_Factory implements Factory<OAuthCallbackHandler> {
  @Override
  public OAuthCallbackHandler get() {
    return newInstance();
  }

  public static OAuthCallbackHandler_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OAuthCallbackHandler newInstance() {
    return new OAuthCallbackHandler();
  }

  private static final class InstanceHolder {
    private static final OAuthCallbackHandler_Factory INSTANCE = new OAuthCallbackHandler_Factory();
  }
}

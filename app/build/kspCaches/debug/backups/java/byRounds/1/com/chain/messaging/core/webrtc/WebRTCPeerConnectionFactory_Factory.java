package com.chain.messaging.core.webrtc;

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
public final class WebRTCPeerConnectionFactory_Factory implements Factory<WebRTCPeerConnectionFactory> {
  private final Provider<Context> contextProvider;

  public WebRTCPeerConnectionFactory_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public WebRTCPeerConnectionFactory get() {
    return newInstance(contextProvider.get());
  }

  public static WebRTCPeerConnectionFactory_Factory create(Provider<Context> contextProvider) {
    return new WebRTCPeerConnectionFactory_Factory(contextProvider);
  }

  public static WebRTCPeerConnectionFactory newInstance(Context context) {
    return new WebRTCPeerConnectionFactory(context);
  }
}

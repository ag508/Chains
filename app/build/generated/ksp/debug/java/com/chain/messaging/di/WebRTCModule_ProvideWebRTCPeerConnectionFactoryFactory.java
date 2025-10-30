package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.webrtc.WebRTCPeerConnectionFactory;
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
public final class WebRTCModule_ProvideWebRTCPeerConnectionFactoryFactory implements Factory<WebRTCPeerConnectionFactory> {
  private final Provider<Context> contextProvider;

  public WebRTCModule_ProvideWebRTCPeerConnectionFactoryFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public WebRTCPeerConnectionFactory get() {
    return provideWebRTCPeerConnectionFactory(contextProvider.get());
  }

  public static WebRTCModule_ProvideWebRTCPeerConnectionFactoryFactory create(
      Provider<Context> contextProvider) {
    return new WebRTCModule_ProvideWebRTCPeerConnectionFactoryFactory(contextProvider);
  }

  public static WebRTCPeerConnectionFactory provideWebRTCPeerConnectionFactory(Context context) {
    return Preconditions.checkNotNullFromProvides(WebRTCModule.INSTANCE.provideWebRTCPeerConnectionFactory(context));
  }
}

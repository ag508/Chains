package com.chain.messaging.di;

import com.chain.messaging.core.webrtc.WebRTCPeerConnectionFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import org.webrtc.PeerConnectionFactory;

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
public final class WebRTCModule_ProvidePeerConnectionFactoryFactory implements Factory<PeerConnectionFactory> {
  private final Provider<WebRTCPeerConnectionFactory> webRTCFactoryProvider;

  public WebRTCModule_ProvidePeerConnectionFactoryFactory(
      Provider<WebRTCPeerConnectionFactory> webRTCFactoryProvider) {
    this.webRTCFactoryProvider = webRTCFactoryProvider;
  }

  @Override
  public PeerConnectionFactory get() {
    return providePeerConnectionFactory(webRTCFactoryProvider.get());
  }

  public static WebRTCModule_ProvidePeerConnectionFactoryFactory create(
      Provider<WebRTCPeerConnectionFactory> webRTCFactoryProvider) {
    return new WebRTCModule_ProvidePeerConnectionFactoryFactory(webRTCFactoryProvider);
  }

  public static PeerConnectionFactory providePeerConnectionFactory(
      WebRTCPeerConnectionFactory webRTCFactory) {
    return Preconditions.checkNotNullFromProvides(WebRTCModule.INSTANCE.providePeerConnectionFactory(webRTCFactory));
  }
}

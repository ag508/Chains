package com.chain.messaging.core.webrtc;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class WebRTCManagerImpl_Factory implements Factory<WebRTCManagerImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<PeerConnectionFactory> peerConnectionFactoryProvider;

  public WebRTCManagerImpl_Factory(Provider<Context> contextProvider,
      Provider<PeerConnectionFactory> peerConnectionFactoryProvider) {
    this.contextProvider = contextProvider;
    this.peerConnectionFactoryProvider = peerConnectionFactoryProvider;
  }

  @Override
  public WebRTCManagerImpl get() {
    return newInstance(contextProvider.get(), peerConnectionFactoryProvider.get());
  }

  public static WebRTCManagerImpl_Factory create(Provider<Context> contextProvider,
      Provider<PeerConnectionFactory> peerConnectionFactoryProvider) {
    return new WebRTCManagerImpl_Factory(contextProvider, peerConnectionFactoryProvider);
  }

  public static WebRTCManagerImpl newInstance(Context context,
      PeerConnectionFactory peerConnectionFactory) {
    return new WebRTCManagerImpl(context, peerConnectionFactory);
  }
}

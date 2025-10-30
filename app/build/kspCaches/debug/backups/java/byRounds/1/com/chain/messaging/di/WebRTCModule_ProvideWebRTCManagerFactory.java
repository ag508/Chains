package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.webrtc.WebRTCManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import org.webrtc.PeerConnectionFactory;

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
public final class WebRTCModule_ProvideWebRTCManagerFactory implements Factory<WebRTCManager> {
  private final Provider<Context> contextProvider;

  private final Provider<PeerConnectionFactory> peerConnectionFactoryProvider;

  public WebRTCModule_ProvideWebRTCManagerFactory(Provider<Context> contextProvider,
      Provider<PeerConnectionFactory> peerConnectionFactoryProvider) {
    this.contextProvider = contextProvider;
    this.peerConnectionFactoryProvider = peerConnectionFactoryProvider;
  }

  @Override
  public WebRTCManager get() {
    return provideWebRTCManager(contextProvider.get(), peerConnectionFactoryProvider.get());
  }

  public static WebRTCModule_ProvideWebRTCManagerFactory create(Provider<Context> contextProvider,
      Provider<PeerConnectionFactory> peerConnectionFactoryProvider) {
    return new WebRTCModule_ProvideWebRTCManagerFactory(contextProvider, peerConnectionFactoryProvider);
  }

  public static WebRTCManager provideWebRTCManager(Context context,
      PeerConnectionFactory peerConnectionFactory) {
    return Preconditions.checkNotNullFromProvides(WebRTCModule.INSTANCE.provideWebRTCManager(context, peerConnectionFactory));
  }
}

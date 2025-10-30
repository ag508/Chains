package com.chain.messaging.di;

import com.chain.messaging.core.webrtc.CallManager;
import com.chain.messaging.core.webrtc.CallSignalingService;
import com.chain.messaging.core.webrtc.IceServerProvider;
import com.chain.messaging.core.webrtc.WebRTCManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class WebRTCModule_ProvideCallManagerFactory implements Factory<CallManager> {
  private final Provider<WebRTCManager> webRTCManagerProvider;

  private final Provider<CallSignalingService> callSignalingServiceProvider;

  private final Provider<IceServerProvider> iceServerProvider;

  public WebRTCModule_ProvideCallManagerFactory(Provider<WebRTCManager> webRTCManagerProvider,
      Provider<CallSignalingService> callSignalingServiceProvider,
      Provider<IceServerProvider> iceServerProvider) {
    this.webRTCManagerProvider = webRTCManagerProvider;
    this.callSignalingServiceProvider = callSignalingServiceProvider;
    this.iceServerProvider = iceServerProvider;
  }

  @Override
  public CallManager get() {
    return provideCallManager(webRTCManagerProvider.get(), callSignalingServiceProvider.get(), iceServerProvider.get());
  }

  public static WebRTCModule_ProvideCallManagerFactory create(
      Provider<WebRTCManager> webRTCManagerProvider,
      Provider<CallSignalingService> callSignalingServiceProvider,
      Provider<IceServerProvider> iceServerProvider) {
    return new WebRTCModule_ProvideCallManagerFactory(webRTCManagerProvider, callSignalingServiceProvider, iceServerProvider);
  }

  public static CallManager provideCallManager(WebRTCManager webRTCManager,
      CallSignalingService callSignalingService, IceServerProvider iceServerProvider) {
    return Preconditions.checkNotNullFromProvides(WebRTCModule.INSTANCE.provideCallManager(webRTCManager, callSignalingService, iceServerProvider));
  }
}

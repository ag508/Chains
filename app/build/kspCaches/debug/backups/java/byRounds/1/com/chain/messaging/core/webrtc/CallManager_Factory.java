package com.chain.messaging.core.webrtc;

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
public final class CallManager_Factory implements Factory<CallManager> {
  private final Provider<WebRTCManager> webRTCManagerProvider;

  private final Provider<CallSignalingService> callSignalingServiceProvider;

  private final Provider<IceServerProvider> iceServerProvider;

  public CallManager_Factory(Provider<WebRTCManager> webRTCManagerProvider,
      Provider<CallSignalingService> callSignalingServiceProvider,
      Provider<IceServerProvider> iceServerProvider) {
    this.webRTCManagerProvider = webRTCManagerProvider;
    this.callSignalingServiceProvider = callSignalingServiceProvider;
    this.iceServerProvider = iceServerProvider;
  }

  @Override
  public CallManager get() {
    return newInstance(webRTCManagerProvider.get(), callSignalingServiceProvider.get(), iceServerProvider.get());
  }

  public static CallManager_Factory create(Provider<WebRTCManager> webRTCManagerProvider,
      Provider<CallSignalingService> callSignalingServiceProvider,
      Provider<IceServerProvider> iceServerProvider) {
    return new CallManager_Factory(webRTCManagerProvider, callSignalingServiceProvider, iceServerProvider);
  }

  public static CallManager newInstance(WebRTCManager webRTCManager,
      CallSignalingService callSignalingService, IceServerProvider iceServerProvider) {
    return new CallManager(webRTCManager, callSignalingService, iceServerProvider);
  }
}

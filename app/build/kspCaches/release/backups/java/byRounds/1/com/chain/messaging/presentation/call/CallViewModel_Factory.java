package com.chain.messaging.presentation.call;

import com.chain.messaging.core.webrtc.CallManager;
import com.chain.messaging.core.webrtc.WebRTCManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class CallViewModel_Factory implements Factory<CallViewModel> {
  private final Provider<CallManager> callManagerProvider;

  private final Provider<WebRTCManager> webRTCManagerProvider;

  public CallViewModel_Factory(Provider<CallManager> callManagerProvider,
      Provider<WebRTCManager> webRTCManagerProvider) {
    this.callManagerProvider = callManagerProvider;
    this.webRTCManagerProvider = webRTCManagerProvider;
  }

  @Override
  public CallViewModel get() {
    return newInstance(callManagerProvider.get(), webRTCManagerProvider.get());
  }

  public static CallViewModel_Factory create(Provider<CallManager> callManagerProvider,
      Provider<WebRTCManager> webRTCManagerProvider) {
    return new CallViewModel_Factory(callManagerProvider, webRTCManagerProvider);
  }

  public static CallViewModel newInstance(CallManager callManager, WebRTCManager webRTCManager) {
    return new CallViewModel(callManager, webRTCManager);
  }
}

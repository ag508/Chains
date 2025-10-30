package com.chain.messaging.presentation.call;

import com.chain.messaging.core.webrtc.CallManager;
import com.chain.messaging.core.webrtc.CallNotificationService;
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
public final class IncomingCallViewModel_Factory implements Factory<IncomingCallViewModel> {
  private final Provider<CallManager> callManagerProvider;

  private final Provider<CallNotificationService> callNotificationServiceProvider;

  public IncomingCallViewModel_Factory(Provider<CallManager> callManagerProvider,
      Provider<CallNotificationService> callNotificationServiceProvider) {
    this.callManagerProvider = callManagerProvider;
    this.callNotificationServiceProvider = callNotificationServiceProvider;
  }

  @Override
  public IncomingCallViewModel get() {
    return newInstance(callManagerProvider.get(), callNotificationServiceProvider.get());
  }

  public static IncomingCallViewModel_Factory create(Provider<CallManager> callManagerProvider,
      Provider<CallNotificationService> callNotificationServiceProvider) {
    return new IncomingCallViewModel_Factory(callManagerProvider, callNotificationServiceProvider);
  }

  public static IncomingCallViewModel newInstance(CallManager callManager,
      CallNotificationService callNotificationService) {
    return new IncomingCallViewModel(callManager, callNotificationService);
  }
}

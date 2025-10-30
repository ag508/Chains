package com.chain.messaging.presentation.call;

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
public final class CallHistoryViewModel_Factory implements Factory<CallHistoryViewModel> {
  private final Provider<CallNotificationService> callNotificationServiceProvider;

  public CallHistoryViewModel_Factory(
      Provider<CallNotificationService> callNotificationServiceProvider) {
    this.callNotificationServiceProvider = callNotificationServiceProvider;
  }

  @Override
  public CallHistoryViewModel get() {
    return newInstance(callNotificationServiceProvider.get());
  }

  public static CallHistoryViewModel_Factory create(
      Provider<CallNotificationService> callNotificationServiceProvider) {
    return new CallHistoryViewModel_Factory(callNotificationServiceProvider);
  }

  public static CallHistoryViewModel newInstance(CallNotificationService callNotificationService) {
    return new CallHistoryViewModel(callNotificationService);
  }
}

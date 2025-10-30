package com.chain.messaging.core.webrtc;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class CallNotificationManager_Factory implements Factory<CallNotificationManager> {
  private final Provider<Context> contextProvider;

  private final Provider<CallNotificationService> callNotificationServiceProvider;

  public CallNotificationManager_Factory(Provider<Context> contextProvider,
      Provider<CallNotificationService> callNotificationServiceProvider) {
    this.contextProvider = contextProvider;
    this.callNotificationServiceProvider = callNotificationServiceProvider;
  }

  @Override
  public CallNotificationManager get() {
    return newInstance(contextProvider.get(), callNotificationServiceProvider.get());
  }

  public static CallNotificationManager_Factory create(Provider<Context> contextProvider,
      Provider<CallNotificationService> callNotificationServiceProvider) {
    return new CallNotificationManager_Factory(contextProvider, callNotificationServiceProvider);
  }

  public static CallNotificationManager newInstance(Context context,
      CallNotificationService callNotificationService) {
    return new CallNotificationManager(context, callNotificationService);
  }
}

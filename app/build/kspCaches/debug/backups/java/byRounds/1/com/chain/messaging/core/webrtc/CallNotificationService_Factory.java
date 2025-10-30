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
public final class CallNotificationService_Factory implements Factory<CallNotificationService> {
  private final Provider<Context> contextProvider;

  public CallNotificationService_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CallNotificationService get() {
    return newInstance(contextProvider.get());
  }

  public static CallNotificationService_Factory create(Provider<Context> contextProvider) {
    return new CallNotificationService_Factory(contextProvider);
  }

  public static CallNotificationService newInstance(Context context) {
    return new CallNotificationService(context);
  }
}

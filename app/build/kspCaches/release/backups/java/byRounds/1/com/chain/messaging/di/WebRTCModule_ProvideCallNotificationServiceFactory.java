package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.webrtc.CallNotificationService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class WebRTCModule_ProvideCallNotificationServiceFactory implements Factory<CallNotificationService> {
  private final Provider<Context> contextProvider;

  public WebRTCModule_ProvideCallNotificationServiceFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CallNotificationService get() {
    return provideCallNotificationService(contextProvider.get());
  }

  public static WebRTCModule_ProvideCallNotificationServiceFactory create(
      Provider<Context> contextProvider) {
    return new WebRTCModule_ProvideCallNotificationServiceFactory(contextProvider);
  }

  public static CallNotificationService provideCallNotificationService(Context context) {
    return Preconditions.checkNotNullFromProvides(WebRTCModule.INSTANCE.provideCallNotificationService(context));
  }
}

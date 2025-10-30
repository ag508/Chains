package com.chain.messaging.core.privacy;

import com.chain.messaging.core.messaging.MessagingService;
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
public final class DisappearingMessageNotificationServiceImpl_Factory implements Factory<DisappearingMessageNotificationServiceImpl> {
  private final Provider<MessagingService> messagingServiceProvider;

  private final Provider<DisappearingMessageManager> disappearingMessageManagerProvider;

  private final Provider<ScreenshotDetector> screenshotDetectorProvider;

  public DisappearingMessageNotificationServiceImpl_Factory(
      Provider<MessagingService> messagingServiceProvider,
      Provider<DisappearingMessageManager> disappearingMessageManagerProvider,
      Provider<ScreenshotDetector> screenshotDetectorProvider) {
    this.messagingServiceProvider = messagingServiceProvider;
    this.disappearingMessageManagerProvider = disappearingMessageManagerProvider;
    this.screenshotDetectorProvider = screenshotDetectorProvider;
  }

  @Override
  public DisappearingMessageNotificationServiceImpl get() {
    return newInstance(messagingServiceProvider.get(), disappearingMessageManagerProvider.get(), screenshotDetectorProvider.get());
  }

  public static DisappearingMessageNotificationServiceImpl_Factory create(
      Provider<MessagingService> messagingServiceProvider,
      Provider<DisappearingMessageManager> disappearingMessageManagerProvider,
      Provider<ScreenshotDetector> screenshotDetectorProvider) {
    return new DisappearingMessageNotificationServiceImpl_Factory(messagingServiceProvider, disappearingMessageManagerProvider, screenshotDetectorProvider);
  }

  public static DisappearingMessageNotificationServiceImpl newInstance(
      MessagingService messagingService, DisappearingMessageManager disappearingMessageManager,
      ScreenshotDetector screenshotDetector) {
    return new DisappearingMessageNotificationServiceImpl(messagingService, disappearingMessageManager, screenshotDetector);
  }
}

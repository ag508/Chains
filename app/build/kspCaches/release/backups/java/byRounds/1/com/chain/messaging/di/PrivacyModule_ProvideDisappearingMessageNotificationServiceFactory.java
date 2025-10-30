package com.chain.messaging.di;

import com.chain.messaging.core.messaging.MessagingService;
import com.chain.messaging.core.privacy.DisappearingMessageManager;
import com.chain.messaging.core.privacy.DisappearingMessageNotificationService;
import com.chain.messaging.core.privacy.ScreenshotDetector;
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
public final class PrivacyModule_ProvideDisappearingMessageNotificationServiceFactory implements Factory<DisappearingMessageNotificationService> {
  private final Provider<MessagingService> messagingServiceProvider;

  private final Provider<DisappearingMessageManager> disappearingMessageManagerProvider;

  private final Provider<ScreenshotDetector> screenshotDetectorProvider;

  public PrivacyModule_ProvideDisappearingMessageNotificationServiceFactory(
      Provider<MessagingService> messagingServiceProvider,
      Provider<DisappearingMessageManager> disappearingMessageManagerProvider,
      Provider<ScreenshotDetector> screenshotDetectorProvider) {
    this.messagingServiceProvider = messagingServiceProvider;
    this.disappearingMessageManagerProvider = disappearingMessageManagerProvider;
    this.screenshotDetectorProvider = screenshotDetectorProvider;
  }

  @Override
  public DisappearingMessageNotificationService get() {
    return provideDisappearingMessageNotificationService(messagingServiceProvider.get(), disappearingMessageManagerProvider.get(), screenshotDetectorProvider.get());
  }

  public static PrivacyModule_ProvideDisappearingMessageNotificationServiceFactory create(
      Provider<MessagingService> messagingServiceProvider,
      Provider<DisappearingMessageManager> disappearingMessageManagerProvider,
      Provider<ScreenshotDetector> screenshotDetectorProvider) {
    return new PrivacyModule_ProvideDisappearingMessageNotificationServiceFactory(messagingServiceProvider, disappearingMessageManagerProvider, screenshotDetectorProvider);
  }

  public static DisappearingMessageNotificationService provideDisappearingMessageNotificationService(
      MessagingService messagingService, DisappearingMessageManager disappearingMessageManager,
      ScreenshotDetector screenshotDetector) {
    return Preconditions.checkNotNullFromProvides(PrivacyModule.INSTANCE.provideDisappearingMessageNotificationService(messagingService, disappearingMessageManager, screenshotDetector));
  }
}

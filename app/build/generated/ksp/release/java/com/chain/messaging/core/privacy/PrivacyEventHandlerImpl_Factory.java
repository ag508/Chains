package com.chain.messaging.core.privacy;

import com.chain.messaging.core.notification.NotificationService;
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
public final class PrivacyEventHandlerImpl_Factory implements Factory<PrivacyEventHandlerImpl> {
  private final Provider<NotificationService> notificationServiceProvider;

  public PrivacyEventHandlerImpl_Factory(
      Provider<NotificationService> notificationServiceProvider) {
    this.notificationServiceProvider = notificationServiceProvider;
  }

  @Override
  public PrivacyEventHandlerImpl get() {
    return newInstance(notificationServiceProvider.get());
  }

  public static PrivacyEventHandlerImpl_Factory create(
      Provider<NotificationService> notificationServiceProvider) {
    return new PrivacyEventHandlerImpl_Factory(notificationServiceProvider);
  }

  public static PrivacyEventHandlerImpl newInstance(NotificationService notificationService) {
    return new PrivacyEventHandlerImpl(notificationService);
  }
}

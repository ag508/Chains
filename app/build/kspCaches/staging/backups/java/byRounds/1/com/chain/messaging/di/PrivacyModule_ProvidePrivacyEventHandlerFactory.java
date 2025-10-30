package com.chain.messaging.di;

import com.chain.messaging.core.notification.NotificationService;
import com.chain.messaging.core.privacy.PrivacyEventHandler;
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
public final class PrivacyModule_ProvidePrivacyEventHandlerFactory implements Factory<PrivacyEventHandler> {
  private final Provider<NotificationService> notificationServiceProvider;

  public PrivacyModule_ProvidePrivacyEventHandlerFactory(
      Provider<NotificationService> notificationServiceProvider) {
    this.notificationServiceProvider = notificationServiceProvider;
  }

  @Override
  public PrivacyEventHandler get() {
    return providePrivacyEventHandler(notificationServiceProvider.get());
  }

  public static PrivacyModule_ProvidePrivacyEventHandlerFactory create(
      Provider<NotificationService> notificationServiceProvider) {
    return new PrivacyModule_ProvidePrivacyEventHandlerFactory(notificationServiceProvider);
  }

  public static PrivacyEventHandler providePrivacyEventHandler(
      NotificationService notificationService) {
    return Preconditions.checkNotNullFromProvides(PrivacyModule.INSTANCE.providePrivacyEventHandler(notificationService));
  }
}

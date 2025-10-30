package com.chain.messaging.di;

import android.content.Context;
import androidx.core.app.NotificationManagerCompat;
import com.chain.messaging.core.security.AlertNotificationService;
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
public final class SecurityMonitoringModule_ProvideAlertNotificationServiceFactory implements Factory<AlertNotificationService> {
  private final Provider<Context> contextProvider;

  private final Provider<NotificationManagerCompat> notificationManagerProvider;

  public SecurityMonitoringModule_ProvideAlertNotificationServiceFactory(
      Provider<Context> contextProvider,
      Provider<NotificationManagerCompat> notificationManagerProvider) {
    this.contextProvider = contextProvider;
    this.notificationManagerProvider = notificationManagerProvider;
  }

  @Override
  public AlertNotificationService get() {
    return provideAlertNotificationService(contextProvider.get(), notificationManagerProvider.get());
  }

  public static SecurityMonitoringModule_ProvideAlertNotificationServiceFactory create(
      Provider<Context> contextProvider,
      Provider<NotificationManagerCompat> notificationManagerProvider) {
    return new SecurityMonitoringModule_ProvideAlertNotificationServiceFactory(contextProvider, notificationManagerProvider);
  }

  public static AlertNotificationService provideAlertNotificationService(Context context,
      NotificationManagerCompat notificationManager) {
    return Preconditions.checkNotNullFromProvides(SecurityMonitoringModule.INSTANCE.provideAlertNotificationService(context, notificationManager));
  }
}

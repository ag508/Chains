package com.chain.messaging.core.security;

import android.content.Context;
import androidx.core.app.NotificationManagerCompat;
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
public final class AlertNotificationServiceImpl_Factory implements Factory<AlertNotificationServiceImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<NotificationManagerCompat> notificationManagerProvider;

  public AlertNotificationServiceImpl_Factory(Provider<Context> contextProvider,
      Provider<NotificationManagerCompat> notificationManagerProvider) {
    this.contextProvider = contextProvider;
    this.notificationManagerProvider = notificationManagerProvider;
  }

  @Override
  public AlertNotificationServiceImpl get() {
    return newInstance(contextProvider.get(), notificationManagerProvider.get());
  }

  public static AlertNotificationServiceImpl_Factory create(Provider<Context> contextProvider,
      Provider<NotificationManagerCompat> notificationManagerProvider) {
    return new AlertNotificationServiceImpl_Factory(contextProvider, notificationManagerProvider);
  }

  public static AlertNotificationServiceImpl newInstance(Context context,
      NotificationManagerCompat notificationManager) {
    return new AlertNotificationServiceImpl(context, notificationManager);
  }
}

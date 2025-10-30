package com.chain.messaging.di;

import android.content.Context;
import androidx.core.app.NotificationManagerCompat;
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
public final class SecurityMonitoringModule_ProvideNotificationManagerCompatFactory implements Factory<NotificationManagerCompat> {
  private final Provider<Context> contextProvider;

  public SecurityMonitoringModule_ProvideNotificationManagerCompatFactory(
      Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NotificationManagerCompat get() {
    return provideNotificationManagerCompat(contextProvider.get());
  }

  public static SecurityMonitoringModule_ProvideNotificationManagerCompatFactory create(
      Provider<Context> contextProvider) {
    return new SecurityMonitoringModule_ProvideNotificationManagerCompatFactory(contextProvider);
  }

  public static NotificationManagerCompat provideNotificationManagerCompat(Context context) {
    return Preconditions.checkNotNullFromProvides(SecurityMonitoringModule.INSTANCE.provideNotificationManagerCompat(context));
  }
}

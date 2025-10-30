package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.notification.NotificationService;
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
public final class NotificationModule_ProvideNotificationServiceFactory implements Factory<NotificationService> {
  private final Provider<Context> contextProvider;

  public NotificationModule_ProvideNotificationServiceFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NotificationService get() {
    return provideNotificationService(contextProvider.get());
  }

  public static NotificationModule_ProvideNotificationServiceFactory create(
      Provider<Context> contextProvider) {
    return new NotificationModule_ProvideNotificationServiceFactory(contextProvider);
  }

  public static NotificationService provideNotificationService(Context context) {
    return Preconditions.checkNotNullFromProvides(NotificationModule.INSTANCE.provideNotificationService(context));
  }
}

package com.chain.messaging.di;

import com.chain.messaging.core.notification.NotificationActionHandler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class NotificationModule_ProvideNotificationActionHandlerFactory implements Factory<NotificationActionHandler> {
  @Override
  public NotificationActionHandler get() {
    return provideNotificationActionHandler();
  }

  public static NotificationModule_ProvideNotificationActionHandlerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NotificationActionHandler provideNotificationActionHandler() {
    return Preconditions.checkNotNullFromProvides(NotificationModule.INSTANCE.provideNotificationActionHandler());
  }

  private static final class InstanceHolder {
    private static final NotificationModule_ProvideNotificationActionHandlerFactory INSTANCE = new NotificationModule_ProvideNotificationActionHandlerFactory();
  }
}

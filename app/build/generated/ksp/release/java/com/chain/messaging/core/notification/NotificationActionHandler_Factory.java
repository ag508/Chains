package com.chain.messaging.core.notification;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class NotificationActionHandler_Factory implements Factory<NotificationActionHandler> {
  @Override
  public NotificationActionHandler get() {
    return newInstance();
  }

  public static NotificationActionHandler_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NotificationActionHandler newInstance() {
    return new NotificationActionHandler();
  }

  private static final class InstanceHolder {
    private static final NotificationActionHandler_Factory INSTANCE = new NotificationActionHandler_Factory();
  }
}

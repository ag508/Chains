package com.chain.messaging.core.notification;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class NotificationPermissionHelper_Factory implements Factory<NotificationPermissionHelper> {
  private final Provider<Context> contextProvider;

  public NotificationPermissionHelper_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NotificationPermissionHelper get() {
    return newInstance(contextProvider.get());
  }

  public static NotificationPermissionHelper_Factory create(Provider<Context> contextProvider) {
    return new NotificationPermissionHelper_Factory(contextProvider);
  }

  public static NotificationPermissionHelper newInstance(Context context) {
    return new NotificationPermissionHelper(context);
  }
}

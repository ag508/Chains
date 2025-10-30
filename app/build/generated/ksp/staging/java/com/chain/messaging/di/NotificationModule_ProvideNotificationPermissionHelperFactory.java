package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.notification.NotificationPermissionHelper;
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
public final class NotificationModule_ProvideNotificationPermissionHelperFactory implements Factory<NotificationPermissionHelper> {
  private final Provider<Context> contextProvider;

  public NotificationModule_ProvideNotificationPermissionHelperFactory(
      Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NotificationPermissionHelper get() {
    return provideNotificationPermissionHelper(contextProvider.get());
  }

  public static NotificationModule_ProvideNotificationPermissionHelperFactory create(
      Provider<Context> contextProvider) {
    return new NotificationModule_ProvideNotificationPermissionHelperFactory(contextProvider);
  }

  public static NotificationPermissionHelper provideNotificationPermissionHelper(Context context) {
    return Preconditions.checkNotNullFromProvides(NotificationModule.INSTANCE.provideNotificationPermissionHelper(context));
  }
}

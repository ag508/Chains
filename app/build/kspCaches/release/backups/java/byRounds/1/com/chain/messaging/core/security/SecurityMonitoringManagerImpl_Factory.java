package com.chain.messaging.core.security;

import android.content.Context;
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
public final class SecurityMonitoringManagerImpl_Factory implements Factory<SecurityMonitoringManagerImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<SecurityEventStorage> securityEventStorageProvider;

  private final Provider<ThreatDetector> threatDetectorProvider;

  private final Provider<AlertNotificationService> alertNotificationServiceProvider;

  public SecurityMonitoringManagerImpl_Factory(Provider<Context> contextProvider,
      Provider<SecurityEventStorage> securityEventStorageProvider,
      Provider<ThreatDetector> threatDetectorProvider,
      Provider<AlertNotificationService> alertNotificationServiceProvider) {
    this.contextProvider = contextProvider;
    this.securityEventStorageProvider = securityEventStorageProvider;
    this.threatDetectorProvider = threatDetectorProvider;
    this.alertNotificationServiceProvider = alertNotificationServiceProvider;
  }

  @Override
  public SecurityMonitoringManagerImpl get() {
    return newInstance(contextProvider.get(), securityEventStorageProvider.get(), threatDetectorProvider.get(), alertNotificationServiceProvider.get());
  }

  public static SecurityMonitoringManagerImpl_Factory create(Provider<Context> contextProvider,
      Provider<SecurityEventStorage> securityEventStorageProvider,
      Provider<ThreatDetector> threatDetectorProvider,
      Provider<AlertNotificationService> alertNotificationServiceProvider) {
    return new SecurityMonitoringManagerImpl_Factory(contextProvider, securityEventStorageProvider, threatDetectorProvider, alertNotificationServiceProvider);
  }

  public static SecurityMonitoringManagerImpl newInstance(Context context,
      SecurityEventStorage securityEventStorage, ThreatDetector threatDetector,
      AlertNotificationService alertNotificationService) {
    return new SecurityMonitoringManagerImpl(context, securityEventStorage, threatDetector, alertNotificationService);
  }
}

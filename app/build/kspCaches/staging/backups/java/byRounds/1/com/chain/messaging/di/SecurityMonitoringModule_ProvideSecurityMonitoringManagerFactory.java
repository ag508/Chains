package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.security.AlertNotificationService;
import com.chain.messaging.core.security.SecurityEventStorage;
import com.chain.messaging.core.security.SecurityMonitoringManager;
import com.chain.messaging.core.security.ThreatDetector;
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
public final class SecurityMonitoringModule_ProvideSecurityMonitoringManagerFactory implements Factory<SecurityMonitoringManager> {
  private final Provider<Context> contextProvider;

  private final Provider<SecurityEventStorage> securityEventStorageProvider;

  private final Provider<ThreatDetector> threatDetectorProvider;

  private final Provider<AlertNotificationService> alertNotificationServiceProvider;

  public SecurityMonitoringModule_ProvideSecurityMonitoringManagerFactory(
      Provider<Context> contextProvider,
      Provider<SecurityEventStorage> securityEventStorageProvider,
      Provider<ThreatDetector> threatDetectorProvider,
      Provider<AlertNotificationService> alertNotificationServiceProvider) {
    this.contextProvider = contextProvider;
    this.securityEventStorageProvider = securityEventStorageProvider;
    this.threatDetectorProvider = threatDetectorProvider;
    this.alertNotificationServiceProvider = alertNotificationServiceProvider;
  }

  @Override
  public SecurityMonitoringManager get() {
    return provideSecurityMonitoringManager(contextProvider.get(), securityEventStorageProvider.get(), threatDetectorProvider.get(), alertNotificationServiceProvider.get());
  }

  public static SecurityMonitoringModule_ProvideSecurityMonitoringManagerFactory create(
      Provider<Context> contextProvider,
      Provider<SecurityEventStorage> securityEventStorageProvider,
      Provider<ThreatDetector> threatDetectorProvider,
      Provider<AlertNotificationService> alertNotificationServiceProvider) {
    return new SecurityMonitoringModule_ProvideSecurityMonitoringManagerFactory(contextProvider, securityEventStorageProvider, threatDetectorProvider, alertNotificationServiceProvider);
  }

  public static SecurityMonitoringManager provideSecurityMonitoringManager(Context context,
      SecurityEventStorage securityEventStorage, ThreatDetector threatDetector,
      AlertNotificationService alertNotificationService) {
    return Preconditions.checkNotNullFromProvides(SecurityMonitoringModule.INSTANCE.provideSecurityMonitoringManager(context, securityEventStorage, threatDetector, alertNotificationService));
  }
}

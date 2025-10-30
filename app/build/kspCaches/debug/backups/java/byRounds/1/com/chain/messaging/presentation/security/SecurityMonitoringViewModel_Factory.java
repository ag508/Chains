package com.chain.messaging.presentation.security;

import com.chain.messaging.core.security.SecurityMonitoringManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SecurityMonitoringViewModel_Factory implements Factory<SecurityMonitoringViewModel> {
  private final Provider<SecurityMonitoringManager> securityMonitoringManagerProvider;

  public SecurityMonitoringViewModel_Factory(
      Provider<SecurityMonitoringManager> securityMonitoringManagerProvider) {
    this.securityMonitoringManagerProvider = securityMonitoringManagerProvider;
  }

  @Override
  public SecurityMonitoringViewModel get() {
    return newInstance(securityMonitoringManagerProvider.get());
  }

  public static SecurityMonitoringViewModel_Factory create(
      Provider<SecurityMonitoringManager> securityMonitoringManagerProvider) {
    return new SecurityMonitoringViewModel_Factory(securityMonitoringManagerProvider);
  }

  public static SecurityMonitoringViewModel newInstance(
      SecurityMonitoringManager securityMonitoringManager) {
    return new SecurityMonitoringViewModel(securityMonitoringManager);
  }
}

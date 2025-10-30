package com.chain.messaging.di;

import com.chain.messaging.core.security.SecurityEventStorage;
import com.chain.messaging.data.local.dao.SecurityEventDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class SecurityMonitoringModule_ProvideSecurityEventStorageFactory implements Factory<SecurityEventStorage> {
  private final Provider<SecurityEventDao> securityEventDaoProvider;

  public SecurityMonitoringModule_ProvideSecurityEventStorageFactory(
      Provider<SecurityEventDao> securityEventDaoProvider) {
    this.securityEventDaoProvider = securityEventDaoProvider;
  }

  @Override
  public SecurityEventStorage get() {
    return provideSecurityEventStorage(securityEventDaoProvider.get());
  }

  public static SecurityMonitoringModule_ProvideSecurityEventStorageFactory create(
      Provider<SecurityEventDao> securityEventDaoProvider) {
    return new SecurityMonitoringModule_ProvideSecurityEventStorageFactory(securityEventDaoProvider);
  }

  public static SecurityEventStorage provideSecurityEventStorage(
      SecurityEventDao securityEventDao) {
    return Preconditions.checkNotNullFromProvides(SecurityMonitoringModule.INSTANCE.provideSecurityEventStorage(securityEventDao));
  }
}

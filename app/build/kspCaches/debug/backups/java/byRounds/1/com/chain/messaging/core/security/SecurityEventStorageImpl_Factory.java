package com.chain.messaging.core.security;

import com.chain.messaging.data.local.dao.SecurityEventDao;
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
public final class SecurityEventStorageImpl_Factory implements Factory<SecurityEventStorageImpl> {
  private final Provider<SecurityEventDao> securityEventDaoProvider;

  public SecurityEventStorageImpl_Factory(Provider<SecurityEventDao> securityEventDaoProvider) {
    this.securityEventDaoProvider = securityEventDaoProvider;
  }

  @Override
  public SecurityEventStorageImpl get() {
    return newInstance(securityEventDaoProvider.get());
  }

  public static SecurityEventStorageImpl_Factory create(
      Provider<SecurityEventDao> securityEventDaoProvider) {
    return new SecurityEventStorageImpl_Factory(securityEventDaoProvider);
  }

  public static SecurityEventStorageImpl newInstance(SecurityEventDao securityEventDao) {
    return new SecurityEventStorageImpl(securityEventDao);
  }
}

package com.chain.messaging.core.security;

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
public final class SecurityAlertManager_Factory implements Factory<SecurityAlertManager> {
  @Override
  public SecurityAlertManager get() {
    return newInstance();
  }

  public static SecurityAlertManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SecurityAlertManager newInstance() {
    return new SecurityAlertManager();
  }

  private static final class InstanceHolder {
    private static final SecurityAlertManager_Factory INSTANCE = new SecurityAlertManager_Factory();
  }
}

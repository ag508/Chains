package com.chain.messaging.di;

import com.chain.messaging.core.security.SecurityAlertManager;
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
public final class SecurityModule_ProvideSecurityAlertManagerFactory implements Factory<SecurityAlertManager> {
  @Override
  public SecurityAlertManager get() {
    return provideSecurityAlertManager();
  }

  public static SecurityModule_ProvideSecurityAlertManagerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SecurityAlertManager provideSecurityAlertManager() {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideSecurityAlertManager());
  }

  private static final class InstanceHolder {
    private static final SecurityModule_ProvideSecurityAlertManagerFactory INSTANCE = new SecurityModule_ProvideSecurityAlertManagerFactory();
  }
}

package com.chain.messaging.di;

import com.chain.messaging.core.deployment.AppStoreManager;
import com.chain.messaging.core.deployment.BuildConfigManager;
import com.chain.messaging.core.util.Logger;
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
public final class DeploymentModule_ProvideAppStoreManagerFactory implements Factory<AppStoreManager> {
  private final Provider<BuildConfigManager> buildConfigManagerProvider;

  private final Provider<Logger> loggerProvider;

  public DeploymentModule_ProvideAppStoreManagerFactory(
      Provider<BuildConfigManager> buildConfigManagerProvider, Provider<Logger> loggerProvider) {
    this.buildConfigManagerProvider = buildConfigManagerProvider;
    this.loggerProvider = loggerProvider;
  }

  @Override
  public AppStoreManager get() {
    return provideAppStoreManager(buildConfigManagerProvider.get(), loggerProvider.get());
  }

  public static DeploymentModule_ProvideAppStoreManagerFactory create(
      Provider<BuildConfigManager> buildConfigManagerProvider, Provider<Logger> loggerProvider) {
    return new DeploymentModule_ProvideAppStoreManagerFactory(buildConfigManagerProvider, loggerProvider);
  }

  public static AppStoreManager provideAppStoreManager(BuildConfigManager buildConfigManager,
      Logger logger) {
    return Preconditions.checkNotNullFromProvides(DeploymentModule.INSTANCE.provideAppStoreManager(buildConfigManager, logger));
  }
}

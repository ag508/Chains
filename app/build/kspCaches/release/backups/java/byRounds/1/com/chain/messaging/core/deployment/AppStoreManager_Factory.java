package com.chain.messaging.core.deployment;

import com.chain.messaging.core.util.Logger;
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
public final class AppStoreManager_Factory implements Factory<AppStoreManager> {
  private final Provider<BuildConfigManager> buildConfigManagerProvider;

  private final Provider<Logger> loggerProvider;

  public AppStoreManager_Factory(Provider<BuildConfigManager> buildConfigManagerProvider,
      Provider<Logger> loggerProvider) {
    this.buildConfigManagerProvider = buildConfigManagerProvider;
    this.loggerProvider = loggerProvider;
  }

  @Override
  public AppStoreManager get() {
    return newInstance(buildConfigManagerProvider.get(), loggerProvider.get());
  }

  public static AppStoreManager_Factory create(
      Provider<BuildConfigManager> buildConfigManagerProvider, Provider<Logger> loggerProvider) {
    return new AppStoreManager_Factory(buildConfigManagerProvider, loggerProvider);
  }

  public static AppStoreManager newInstance(BuildConfigManager buildConfigManager, Logger logger) {
    return new AppStoreManager(buildConfigManager, logger);
  }
}

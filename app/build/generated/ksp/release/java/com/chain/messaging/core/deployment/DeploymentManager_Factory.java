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
public final class DeploymentManager_Factory implements Factory<DeploymentManager> {
  private final Provider<PerformanceOptimizer> performanceOptimizerProvider;

  private final Provider<BuildConfigManager> buildConfigManagerProvider;

  private final Provider<CodeCleanupManager> codeCleanupManagerProvider;

  private final Provider<DocumentationGenerator> documentationGeneratorProvider;

  private final Provider<AppStoreManager> appStoreManagerProvider;

  private final Provider<Logger> loggerProvider;

  public DeploymentManager_Factory(Provider<PerformanceOptimizer> performanceOptimizerProvider,
      Provider<BuildConfigManager> buildConfigManagerProvider,
      Provider<CodeCleanupManager> codeCleanupManagerProvider,
      Provider<DocumentationGenerator> documentationGeneratorProvider,
      Provider<AppStoreManager> appStoreManagerProvider, Provider<Logger> loggerProvider) {
    this.performanceOptimizerProvider = performanceOptimizerProvider;
    this.buildConfigManagerProvider = buildConfigManagerProvider;
    this.codeCleanupManagerProvider = codeCleanupManagerProvider;
    this.documentationGeneratorProvider = documentationGeneratorProvider;
    this.appStoreManagerProvider = appStoreManagerProvider;
    this.loggerProvider = loggerProvider;
  }

  @Override
  public DeploymentManager get() {
    return newInstance(performanceOptimizerProvider.get(), buildConfigManagerProvider.get(), codeCleanupManagerProvider.get(), documentationGeneratorProvider.get(), appStoreManagerProvider.get(), loggerProvider.get());
  }

  public static DeploymentManager_Factory create(
      Provider<PerformanceOptimizer> performanceOptimizerProvider,
      Provider<BuildConfigManager> buildConfigManagerProvider,
      Provider<CodeCleanupManager> codeCleanupManagerProvider,
      Provider<DocumentationGenerator> documentationGeneratorProvider,
      Provider<AppStoreManager> appStoreManagerProvider, Provider<Logger> loggerProvider) {
    return new DeploymentManager_Factory(performanceOptimizerProvider, buildConfigManagerProvider, codeCleanupManagerProvider, documentationGeneratorProvider, appStoreManagerProvider, loggerProvider);
  }

  public static DeploymentManager newInstance(PerformanceOptimizer performanceOptimizer,
      BuildConfigManager buildConfigManager, CodeCleanupManager codeCleanupManager,
      DocumentationGenerator documentationGenerator, AppStoreManager appStoreManager,
      Logger logger) {
    return new DeploymentManager(performanceOptimizer, buildConfigManager, codeCleanupManager, documentationGenerator, appStoreManager, logger);
  }
}

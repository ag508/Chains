package com.chain.messaging.di;

import com.chain.messaging.core.deployment.AppStoreManager;
import com.chain.messaging.core.deployment.BuildConfigManager;
import com.chain.messaging.core.deployment.CodeCleanupManager;
import com.chain.messaging.core.deployment.DeploymentManager;
import com.chain.messaging.core.deployment.DocumentationGenerator;
import com.chain.messaging.core.deployment.PerformanceOptimizer;
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
public final class DeploymentModule_ProvideDeploymentManagerFactory implements Factory<DeploymentManager> {
  private final Provider<PerformanceOptimizer> performanceOptimizerProvider;

  private final Provider<BuildConfigManager> buildConfigManagerProvider;

  private final Provider<CodeCleanupManager> codeCleanupManagerProvider;

  private final Provider<DocumentationGenerator> documentationGeneratorProvider;

  private final Provider<AppStoreManager> appStoreManagerProvider;

  private final Provider<Logger> loggerProvider;

  public DeploymentModule_ProvideDeploymentManagerFactory(
      Provider<PerformanceOptimizer> performanceOptimizerProvider,
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
    return provideDeploymentManager(performanceOptimizerProvider.get(), buildConfigManagerProvider.get(), codeCleanupManagerProvider.get(), documentationGeneratorProvider.get(), appStoreManagerProvider.get(), loggerProvider.get());
  }

  public static DeploymentModule_ProvideDeploymentManagerFactory create(
      Provider<PerformanceOptimizer> performanceOptimizerProvider,
      Provider<BuildConfigManager> buildConfigManagerProvider,
      Provider<CodeCleanupManager> codeCleanupManagerProvider,
      Provider<DocumentationGenerator> documentationGeneratorProvider,
      Provider<AppStoreManager> appStoreManagerProvider, Provider<Logger> loggerProvider) {
    return new DeploymentModule_ProvideDeploymentManagerFactory(performanceOptimizerProvider, buildConfigManagerProvider, codeCleanupManagerProvider, documentationGeneratorProvider, appStoreManagerProvider, loggerProvider);
  }

  public static DeploymentManager provideDeploymentManager(
      PerformanceOptimizer performanceOptimizer, BuildConfigManager buildConfigManager,
      CodeCleanupManager codeCleanupManager, DocumentationGenerator documentationGenerator,
      AppStoreManager appStoreManager, Logger logger) {
    return Preconditions.checkNotNullFromProvides(DeploymentModule.INSTANCE.provideDeploymentManager(performanceOptimizer, buildConfigManager, codeCleanupManager, documentationGenerator, appStoreManager, logger));
  }
}

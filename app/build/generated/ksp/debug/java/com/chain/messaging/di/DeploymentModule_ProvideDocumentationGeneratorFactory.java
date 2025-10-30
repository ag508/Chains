package com.chain.messaging.di;

import com.chain.messaging.core.deployment.BuildConfigManager;
import com.chain.messaging.core.deployment.DocumentationGenerator;
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
public final class DeploymentModule_ProvideDocumentationGeneratorFactory implements Factory<DocumentationGenerator> {
  private final Provider<BuildConfigManager> buildConfigManagerProvider;

  private final Provider<Logger> loggerProvider;

  public DeploymentModule_ProvideDocumentationGeneratorFactory(
      Provider<BuildConfigManager> buildConfigManagerProvider, Provider<Logger> loggerProvider) {
    this.buildConfigManagerProvider = buildConfigManagerProvider;
    this.loggerProvider = loggerProvider;
  }

  @Override
  public DocumentationGenerator get() {
    return provideDocumentationGenerator(buildConfigManagerProvider.get(), loggerProvider.get());
  }

  public static DeploymentModule_ProvideDocumentationGeneratorFactory create(
      Provider<BuildConfigManager> buildConfigManagerProvider, Provider<Logger> loggerProvider) {
    return new DeploymentModule_ProvideDocumentationGeneratorFactory(buildConfigManagerProvider, loggerProvider);
  }

  public static DocumentationGenerator provideDocumentationGenerator(
      BuildConfigManager buildConfigManager, Logger logger) {
    return Preconditions.checkNotNullFromProvides(DeploymentModule.INSTANCE.provideDocumentationGenerator(buildConfigManager, logger));
  }
}

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
public final class DocumentationGenerator_Factory implements Factory<DocumentationGenerator> {
  private final Provider<BuildConfigManager> buildConfigManagerProvider;

  private final Provider<Logger> loggerProvider;

  public DocumentationGenerator_Factory(Provider<BuildConfigManager> buildConfigManagerProvider,
      Provider<Logger> loggerProvider) {
    this.buildConfigManagerProvider = buildConfigManagerProvider;
    this.loggerProvider = loggerProvider;
  }

  @Override
  public DocumentationGenerator get() {
    return newInstance(buildConfigManagerProvider.get(), loggerProvider.get());
  }

  public static DocumentationGenerator_Factory create(
      Provider<BuildConfigManager> buildConfigManagerProvider, Provider<Logger> loggerProvider) {
    return new DocumentationGenerator_Factory(buildConfigManagerProvider, loggerProvider);
  }

  public static DocumentationGenerator newInstance(BuildConfigManager buildConfigManager,
      Logger logger) {
    return new DocumentationGenerator(buildConfigManager, logger);
  }
}

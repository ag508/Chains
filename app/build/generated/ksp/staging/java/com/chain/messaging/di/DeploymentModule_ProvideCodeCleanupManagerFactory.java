package com.chain.messaging.di;

import com.chain.messaging.core.deployment.CodeCleanupManager;
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
public final class DeploymentModule_ProvideCodeCleanupManagerFactory implements Factory<CodeCleanupManager> {
  private final Provider<Logger> loggerProvider;

  public DeploymentModule_ProvideCodeCleanupManagerFactory(Provider<Logger> loggerProvider) {
    this.loggerProvider = loggerProvider;
  }

  @Override
  public CodeCleanupManager get() {
    return provideCodeCleanupManager(loggerProvider.get());
  }

  public static DeploymentModule_ProvideCodeCleanupManagerFactory create(
      Provider<Logger> loggerProvider) {
    return new DeploymentModule_ProvideCodeCleanupManagerFactory(loggerProvider);
  }

  public static CodeCleanupManager provideCodeCleanupManager(Logger logger) {
    return Preconditions.checkNotNullFromProvides(DeploymentModule.INSTANCE.provideCodeCleanupManager(logger));
  }
}

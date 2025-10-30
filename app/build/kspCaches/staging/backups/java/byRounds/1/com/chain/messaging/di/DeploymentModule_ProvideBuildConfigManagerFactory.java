package com.chain.messaging.di;

import android.content.Context;
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
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DeploymentModule_ProvideBuildConfigManagerFactory implements Factory<BuildConfigManager> {
  private final Provider<Context> contextProvider;

  private final Provider<Logger> loggerProvider;

  public DeploymentModule_ProvideBuildConfigManagerFactory(Provider<Context> contextProvider,
      Provider<Logger> loggerProvider) {
    this.contextProvider = contextProvider;
    this.loggerProvider = loggerProvider;
  }

  @Override
  public BuildConfigManager get() {
    return provideBuildConfigManager(contextProvider.get(), loggerProvider.get());
  }

  public static DeploymentModule_ProvideBuildConfigManagerFactory create(
      Provider<Context> contextProvider, Provider<Logger> loggerProvider) {
    return new DeploymentModule_ProvideBuildConfigManagerFactory(contextProvider, loggerProvider);
  }

  public static BuildConfigManager provideBuildConfigManager(Context context, Logger logger) {
    return Preconditions.checkNotNullFromProvides(DeploymentModule.INSTANCE.provideBuildConfigManager(context, logger));
  }
}

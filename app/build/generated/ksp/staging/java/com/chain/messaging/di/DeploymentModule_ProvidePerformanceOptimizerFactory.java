package com.chain.messaging.di;

import android.content.Context;
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
public final class DeploymentModule_ProvidePerformanceOptimizerFactory implements Factory<PerformanceOptimizer> {
  private final Provider<Context> contextProvider;

  private final Provider<Logger> loggerProvider;

  public DeploymentModule_ProvidePerformanceOptimizerFactory(Provider<Context> contextProvider,
      Provider<Logger> loggerProvider) {
    this.contextProvider = contextProvider;
    this.loggerProvider = loggerProvider;
  }

  @Override
  public PerformanceOptimizer get() {
    return providePerformanceOptimizer(contextProvider.get(), loggerProvider.get());
  }

  public static DeploymentModule_ProvidePerformanceOptimizerFactory create(
      Provider<Context> contextProvider, Provider<Logger> loggerProvider) {
    return new DeploymentModule_ProvidePerformanceOptimizerFactory(contextProvider, loggerProvider);
  }

  public static PerformanceOptimizer providePerformanceOptimizer(Context context, Logger logger) {
    return Preconditions.checkNotNullFromProvides(DeploymentModule.INSTANCE.providePerformanceOptimizer(context, logger));
  }
}

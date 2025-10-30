package com.chain.messaging.core.deployment;

import android.content.Context;
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
public final class PerformanceOptimizer_Factory implements Factory<PerformanceOptimizer> {
  private final Provider<Context> contextProvider;

  private final Provider<Logger> loggerProvider;

  public PerformanceOptimizer_Factory(Provider<Context> contextProvider,
      Provider<Logger> loggerProvider) {
    this.contextProvider = contextProvider;
    this.loggerProvider = loggerProvider;
  }

  @Override
  public PerformanceOptimizer get() {
    return newInstance(contextProvider.get(), loggerProvider.get());
  }

  public static PerformanceOptimizer_Factory create(Provider<Context> contextProvider,
      Provider<Logger> loggerProvider) {
    return new PerformanceOptimizer_Factory(contextProvider, loggerProvider);
  }

  public static PerformanceOptimizer newInstance(Context context, Logger logger) {
    return new PerformanceOptimizer(context, logger);
  }
}

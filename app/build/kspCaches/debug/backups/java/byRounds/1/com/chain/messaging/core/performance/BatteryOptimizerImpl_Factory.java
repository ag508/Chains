package com.chain.messaging.core.performance;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class BatteryOptimizerImpl_Factory implements Factory<BatteryOptimizerImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<PerformanceMonitor> performanceMonitorProvider;

  public BatteryOptimizerImpl_Factory(Provider<Context> contextProvider,
      Provider<PerformanceMonitor> performanceMonitorProvider) {
    this.contextProvider = contextProvider;
    this.performanceMonitorProvider = performanceMonitorProvider;
  }

  @Override
  public BatteryOptimizerImpl get() {
    return newInstance(contextProvider.get(), performanceMonitorProvider.get());
  }

  public static BatteryOptimizerImpl_Factory create(Provider<Context> contextProvider,
      Provider<PerformanceMonitor> performanceMonitorProvider) {
    return new BatteryOptimizerImpl_Factory(contextProvider, performanceMonitorProvider);
  }

  public static BatteryOptimizerImpl newInstance(Context context,
      PerformanceMonitor performanceMonitor) {
    return new BatteryOptimizerImpl(context, performanceMonitor);
  }
}

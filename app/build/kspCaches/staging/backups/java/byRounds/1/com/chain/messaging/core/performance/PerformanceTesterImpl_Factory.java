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
public final class PerformanceTesterImpl_Factory implements Factory<PerformanceTesterImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<PerformanceMonitor> performanceMonitorProvider;

  private final Provider<MemoryManager> memoryManagerProvider;

  public PerformanceTesterImpl_Factory(Provider<Context> contextProvider,
      Provider<PerformanceMonitor> performanceMonitorProvider,
      Provider<MemoryManager> memoryManagerProvider) {
    this.contextProvider = contextProvider;
    this.performanceMonitorProvider = performanceMonitorProvider;
    this.memoryManagerProvider = memoryManagerProvider;
  }

  @Override
  public PerformanceTesterImpl get() {
    return newInstance(contextProvider.get(), performanceMonitorProvider.get(), memoryManagerProvider.get());
  }

  public static PerformanceTesterImpl_Factory create(Provider<Context> contextProvider,
      Provider<PerformanceMonitor> performanceMonitorProvider,
      Provider<MemoryManager> memoryManagerProvider) {
    return new PerformanceTesterImpl_Factory(contextProvider, performanceMonitorProvider, memoryManagerProvider);
  }

  public static PerformanceTesterImpl newInstance(Context context,
      PerformanceMonitor performanceMonitor, MemoryManager memoryManager) {
    return new PerformanceTesterImpl(context, performanceMonitor, memoryManager);
  }
}

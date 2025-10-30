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
public final class PerformanceMonitorImpl_Factory implements Factory<PerformanceMonitorImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<PerformanceStorage> performanceStorageProvider;

  public PerformanceMonitorImpl_Factory(Provider<Context> contextProvider,
      Provider<PerformanceStorage> performanceStorageProvider) {
    this.contextProvider = contextProvider;
    this.performanceStorageProvider = performanceStorageProvider;
  }

  @Override
  public PerformanceMonitorImpl get() {
    return newInstance(contextProvider.get(), performanceStorageProvider.get());
  }

  public static PerformanceMonitorImpl_Factory create(Provider<Context> contextProvider,
      Provider<PerformanceStorage> performanceStorageProvider) {
    return new PerformanceMonitorImpl_Factory(contextProvider, performanceStorageProvider);
  }

  public static PerformanceMonitorImpl newInstance(Context context,
      PerformanceStorage performanceStorage) {
    return new PerformanceMonitorImpl(context, performanceStorage);
  }
}

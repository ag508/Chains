package com.chain.messaging.core.performance;

import com.chain.messaging.data.local.dao.PerformanceDao;
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
public final class PerformanceStorageImpl_Factory implements Factory<PerformanceStorageImpl> {
  private final Provider<PerformanceDao> performanceDaoProvider;

  public PerformanceStorageImpl_Factory(Provider<PerformanceDao> performanceDaoProvider) {
    this.performanceDaoProvider = performanceDaoProvider;
  }

  @Override
  public PerformanceStorageImpl get() {
    return newInstance(performanceDaoProvider.get());
  }

  public static PerformanceStorageImpl_Factory create(
      Provider<PerformanceDao> performanceDaoProvider) {
    return new PerformanceStorageImpl_Factory(performanceDaoProvider);
  }

  public static PerformanceStorageImpl newInstance(PerformanceDao performanceDao) {
    return new PerformanceStorageImpl(performanceDao);
  }
}

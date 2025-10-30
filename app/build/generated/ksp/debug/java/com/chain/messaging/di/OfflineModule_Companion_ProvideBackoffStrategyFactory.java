package com.chain.messaging.di;

import com.chain.messaging.core.offline.BackoffStrategy;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class OfflineModule_Companion_ProvideBackoffStrategyFactory implements Factory<BackoffStrategy> {
  @Override
  public BackoffStrategy get() {
    return provideBackoffStrategy();
  }

  public static OfflineModule_Companion_ProvideBackoffStrategyFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static BackoffStrategy provideBackoffStrategy() {
    return Preconditions.checkNotNullFromProvides(OfflineModule.Companion.provideBackoffStrategy());
  }

  private static final class InstanceHolder {
    private static final OfflineModule_Companion_ProvideBackoffStrategyFactory INSTANCE = new OfflineModule_Companion_ProvideBackoffStrategyFactory();
  }
}

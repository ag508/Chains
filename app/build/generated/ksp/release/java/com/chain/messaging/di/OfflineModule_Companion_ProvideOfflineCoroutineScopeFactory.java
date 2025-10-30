package com.chain.messaging.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineScope;

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
public final class OfflineModule_Companion_ProvideOfflineCoroutineScopeFactory implements Factory<CoroutineScope> {
  @Override
  public CoroutineScope get() {
    return provideOfflineCoroutineScope();
  }

  public static OfflineModule_Companion_ProvideOfflineCoroutineScopeFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CoroutineScope provideOfflineCoroutineScope() {
    return Preconditions.checkNotNullFromProvides(OfflineModule.Companion.provideOfflineCoroutineScope());
  }

  private static final class InstanceHolder {
    private static final OfflineModule_Companion_ProvideOfflineCoroutineScopeFactory INSTANCE = new OfflineModule_Companion_ProvideOfflineCoroutineScopeFactory();
  }
}

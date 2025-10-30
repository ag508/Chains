package com.chain.messaging.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.serialization.json.Json;

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
public final class CloudModule_Companion_ProvideJsonFactory implements Factory<Json> {
  @Override
  public Json get() {
    return provideJson();
  }

  public static CloudModule_Companion_ProvideJsonFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static Json provideJson() {
    return Preconditions.checkNotNullFromProvides(CloudModule.Companion.provideJson());
  }

  private static final class InstanceHolder {
    private static final CloudModule_Companion_ProvideJsonFactory INSTANCE = new CloudModule_Companion_ProvideJsonFactory();
  }
}

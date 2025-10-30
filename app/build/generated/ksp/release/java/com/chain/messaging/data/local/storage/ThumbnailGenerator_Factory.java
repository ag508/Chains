package com.chain.messaging.data.local.storage;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ThumbnailGenerator_Factory implements Factory<ThumbnailGenerator> {
  @Override
  public ThumbnailGenerator get() {
    return newInstance();
  }

  public static ThumbnailGenerator_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ThumbnailGenerator newInstance() {
    return new ThumbnailGenerator();
  }

  private static final class InstanceHolder {
    private static final ThumbnailGenerator_Factory INSTANCE = new ThumbnailGenerator_Factory();
  }
}

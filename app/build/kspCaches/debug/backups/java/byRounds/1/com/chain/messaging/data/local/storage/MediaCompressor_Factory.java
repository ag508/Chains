package com.chain.messaging.data.local.storage;

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
public final class MediaCompressor_Factory implements Factory<MediaCompressor> {
  private final Provider<Context> contextProvider;

  public MediaCompressor_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MediaCompressor get() {
    return newInstance(contextProvider.get());
  }

  public static MediaCompressor_Factory create(Provider<Context> contextProvider) {
    return new MediaCompressor_Factory(contextProvider);
  }

  public static MediaCompressor newInstance(Context context) {
    return new MediaCompressor(context);
  }
}

package com.chain.messaging.core.privacy;

import android.content.Context;
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
public final class ScreenshotDetectorImpl_Factory implements Factory<ScreenshotDetectorImpl> {
  private final Provider<Context> contextProvider;

  public ScreenshotDetectorImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ScreenshotDetectorImpl get() {
    return newInstance(contextProvider.get());
  }

  public static ScreenshotDetectorImpl_Factory create(Provider<Context> contextProvider) {
    return new ScreenshotDetectorImpl_Factory(contextProvider);
  }

  public static ScreenshotDetectorImpl newInstance(Context context) {
    return new ScreenshotDetectorImpl(context);
  }
}

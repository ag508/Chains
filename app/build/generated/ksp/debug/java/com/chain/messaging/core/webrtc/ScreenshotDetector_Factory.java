package com.chain.messaging.core.webrtc;

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
public final class ScreenshotDetector_Factory implements Factory<ScreenshotDetector> {
  private final Provider<Context> contextProvider;

  public ScreenshotDetector_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ScreenshotDetector get() {
    return newInstance(contextProvider.get());
  }

  public static ScreenshotDetector_Factory create(Provider<Context> contextProvider) {
    return new ScreenshotDetector_Factory(contextProvider);
  }

  public static ScreenshotDetector newInstance(Context context) {
    return new ScreenshotDetector(context);
  }
}

package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.privacy.ScreenshotDetector;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class PrivacyModule_ProvideScreenshotDetectorFactory implements Factory<ScreenshotDetector> {
  private final Provider<Context> contextProvider;

  public PrivacyModule_ProvideScreenshotDetectorFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ScreenshotDetector get() {
    return provideScreenshotDetector(contextProvider.get());
  }

  public static PrivacyModule_ProvideScreenshotDetectorFactory create(
      Provider<Context> contextProvider) {
    return new PrivacyModule_ProvideScreenshotDetectorFactory(contextProvider);
  }

  public static ScreenshotDetector provideScreenshotDetector(Context context) {
    return Preconditions.checkNotNullFromProvides(PrivacyModule.INSTANCE.provideScreenshotDetector(context));
  }
}

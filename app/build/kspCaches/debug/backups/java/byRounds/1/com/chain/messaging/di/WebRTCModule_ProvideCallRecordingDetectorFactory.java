package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.webrtc.CallRecordingDetector;
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
public final class WebRTCModule_ProvideCallRecordingDetectorFactory implements Factory<CallRecordingDetector> {
  private final Provider<Context> contextProvider;

  public WebRTCModule_ProvideCallRecordingDetectorFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CallRecordingDetector get() {
    return provideCallRecordingDetector(contextProvider.get());
  }

  public static WebRTCModule_ProvideCallRecordingDetectorFactory create(
      Provider<Context> contextProvider) {
    return new WebRTCModule_ProvideCallRecordingDetectorFactory(contextProvider);
  }

  public static CallRecordingDetector provideCallRecordingDetector(Context context) {
    return Preconditions.checkNotNullFromProvides(WebRTCModule.INSTANCE.provideCallRecordingDetector(context));
  }
}

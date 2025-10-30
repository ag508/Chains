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
public final class CallRecordingDetector_Factory implements Factory<CallRecordingDetector> {
  private final Provider<Context> contextProvider;

  public CallRecordingDetector_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CallRecordingDetector get() {
    return newInstance(contextProvider.get());
  }

  public static CallRecordingDetector_Factory create(Provider<Context> contextProvider) {
    return new CallRecordingDetector_Factory(contextProvider);
  }

  public static CallRecordingDetector newInstance(Context context) {
    return new CallRecordingDetector(context);
  }
}

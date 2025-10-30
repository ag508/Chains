package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.webrtc.CallRecordingManager;
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
public final class WebRTCModule_ProvideCallRecordingManagerFactory implements Factory<CallRecordingManager> {
  private final Provider<Context> contextProvider;

  public WebRTCModule_ProvideCallRecordingManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CallRecordingManager get() {
    return provideCallRecordingManager(contextProvider.get());
  }

  public static WebRTCModule_ProvideCallRecordingManagerFactory create(
      Provider<Context> contextProvider) {
    return new WebRTCModule_ProvideCallRecordingManagerFactory(contextProvider);
  }

  public static CallRecordingManager provideCallRecordingManager(Context context) {
    return Preconditions.checkNotNullFromProvides(WebRTCModule.INSTANCE.provideCallRecordingManager(context));
  }
}

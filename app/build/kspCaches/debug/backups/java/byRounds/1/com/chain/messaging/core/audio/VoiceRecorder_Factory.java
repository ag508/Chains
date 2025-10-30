package com.chain.messaging.core.audio;

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
public final class VoiceRecorder_Factory implements Factory<VoiceRecorder> {
  private final Provider<Context> contextProvider;

  public VoiceRecorder_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public VoiceRecorder get() {
    return newInstance(contextProvider.get());
  }

  public static VoiceRecorder_Factory create(Provider<Context> contextProvider) {
    return new VoiceRecorder_Factory(contextProvider);
  }

  public static VoiceRecorder newInstance(Context context) {
    return new VoiceRecorder(context);
  }
}

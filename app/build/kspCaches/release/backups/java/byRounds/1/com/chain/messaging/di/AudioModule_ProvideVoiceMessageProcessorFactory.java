package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.audio.VoiceMessageProcessor;
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
public final class AudioModule_ProvideVoiceMessageProcessorFactory implements Factory<VoiceMessageProcessor> {
  private final Provider<Context> contextProvider;

  public AudioModule_ProvideVoiceMessageProcessorFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public VoiceMessageProcessor get() {
    return provideVoiceMessageProcessor(contextProvider.get());
  }

  public static AudioModule_ProvideVoiceMessageProcessorFactory create(
      Provider<Context> contextProvider) {
    return new AudioModule_ProvideVoiceMessageProcessorFactory(contextProvider);
  }

  public static VoiceMessageProcessor provideVoiceMessageProcessor(Context context) {
    return Preconditions.checkNotNullFromProvides(AudioModule.INSTANCE.provideVoiceMessageProcessor(context));
  }
}

package com.chain.messaging.presentation.voice;

import com.chain.messaging.core.audio.VoiceMessageProcessor;
import com.chain.messaging.core.audio.VoiceRecorder;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class VoiceRecorderViewModel_Factory implements Factory<VoiceRecorderViewModel> {
  private final Provider<VoiceRecorder> voiceRecorderProvider;

  private final Provider<VoiceMessageProcessor> voiceMessageProcessorProvider;

  public VoiceRecorderViewModel_Factory(Provider<VoiceRecorder> voiceRecorderProvider,
      Provider<VoiceMessageProcessor> voiceMessageProcessorProvider) {
    this.voiceRecorderProvider = voiceRecorderProvider;
    this.voiceMessageProcessorProvider = voiceMessageProcessorProvider;
  }

  @Override
  public VoiceRecorderViewModel get() {
    return newInstance(voiceRecorderProvider.get(), voiceMessageProcessorProvider.get());
  }

  public static VoiceRecorderViewModel_Factory create(Provider<VoiceRecorder> voiceRecorderProvider,
      Provider<VoiceMessageProcessor> voiceMessageProcessorProvider) {
    return new VoiceRecorderViewModel_Factory(voiceRecorderProvider, voiceMessageProcessorProvider);
  }

  public static VoiceRecorderViewModel newInstance(VoiceRecorder voiceRecorder,
      VoiceMessageProcessor voiceMessageProcessor) {
    return new VoiceRecorderViewModel(voiceRecorder, voiceMessageProcessor);
  }
}

package com.chain.messaging.presentation.voice;

import com.chain.messaging.core.audio.VoiceMessageProcessor;
import com.chain.messaging.core.audio.VoicePlayer;
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
public final class VoicePlayerViewModel_Factory implements Factory<VoicePlayerViewModel> {
  private final Provider<VoicePlayer> voicePlayerProvider;

  private final Provider<VoiceMessageProcessor> voiceMessageProcessorProvider;

  public VoicePlayerViewModel_Factory(Provider<VoicePlayer> voicePlayerProvider,
      Provider<VoiceMessageProcessor> voiceMessageProcessorProvider) {
    this.voicePlayerProvider = voicePlayerProvider;
    this.voiceMessageProcessorProvider = voiceMessageProcessorProvider;
  }

  @Override
  public VoicePlayerViewModel get() {
    return newInstance(voicePlayerProvider.get(), voiceMessageProcessorProvider.get());
  }

  public static VoicePlayerViewModel_Factory create(Provider<VoicePlayer> voicePlayerProvider,
      Provider<VoiceMessageProcessor> voiceMessageProcessorProvider) {
    return new VoicePlayerViewModel_Factory(voicePlayerProvider, voiceMessageProcessorProvider);
  }

  public static VoicePlayerViewModel newInstance(VoicePlayer voicePlayer,
      VoiceMessageProcessor voiceMessageProcessor) {
    return new VoicePlayerViewModel(voicePlayer, voiceMessageProcessor);
  }
}

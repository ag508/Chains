package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.audio.VoicePlayer;
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
public final class AudioModule_ProvideVoicePlayerFactory implements Factory<VoicePlayer> {
  private final Provider<Context> contextProvider;

  public AudioModule_ProvideVoicePlayerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public VoicePlayer get() {
    return provideVoicePlayer(contextProvider.get());
  }

  public static AudioModule_ProvideVoicePlayerFactory create(Provider<Context> contextProvider) {
    return new AudioModule_ProvideVoicePlayerFactory(contextProvider);
  }

  public static VoicePlayer provideVoicePlayer(Context context) {
    return Preconditions.checkNotNullFromProvides(AudioModule.INSTANCE.provideVoicePlayer(context));
  }
}

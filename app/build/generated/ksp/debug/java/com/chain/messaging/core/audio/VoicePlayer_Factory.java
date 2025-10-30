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
public final class VoicePlayer_Factory implements Factory<VoicePlayer> {
  private final Provider<Context> contextProvider;

  public VoicePlayer_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public VoicePlayer get() {
    return newInstance(contextProvider.get());
  }

  public static VoicePlayer_Factory create(Provider<Context> contextProvider) {
    return new VoicePlayer_Factory(contextProvider);
  }

  public static VoicePlayer newInstance(Context context) {
    return new VoicePlayer(context);
  }
}

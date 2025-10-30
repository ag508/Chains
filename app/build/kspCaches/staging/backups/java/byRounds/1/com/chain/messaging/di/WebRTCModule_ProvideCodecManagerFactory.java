package com.chain.messaging.di;

import com.chain.messaging.core.webrtc.CodecManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class WebRTCModule_ProvideCodecManagerFactory implements Factory<CodecManager> {
  @Override
  public CodecManager get() {
    return provideCodecManager();
  }

  public static WebRTCModule_ProvideCodecManagerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CodecManager provideCodecManager() {
    return Preconditions.checkNotNullFromProvides(WebRTCModule.INSTANCE.provideCodecManager());
  }

  private static final class InstanceHolder {
    private static final WebRTCModule_ProvideCodecManagerFactory INSTANCE = new WebRTCModule_ProvideCodecManagerFactory();
  }
}

package com.chain.messaging.di;

import com.chain.messaging.core.webrtc.IceServerProvider;
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
public final class WebRTCModule_ProvideIceServerProviderFactory implements Factory<IceServerProvider> {
  @Override
  public IceServerProvider get() {
    return provideIceServerProvider();
  }

  public static WebRTCModule_ProvideIceServerProviderFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static IceServerProvider provideIceServerProvider() {
    return Preconditions.checkNotNullFromProvides(WebRTCModule.INSTANCE.provideIceServerProvider());
  }

  private static final class InstanceHolder {
    private static final WebRTCModule_ProvideIceServerProviderFactory INSTANCE = new WebRTCModule_ProvideIceServerProviderFactory();
  }
}

package com.chain.messaging.di;

import android.content.Context;
import com.chain.messaging.core.webrtc.BandwidthMonitor;
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
public final class WebRTCModule_ProvideBandwidthMonitorFactory implements Factory<BandwidthMonitor> {
  private final Provider<Context> contextProvider;

  public WebRTCModule_ProvideBandwidthMonitorFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public BandwidthMonitor get() {
    return provideBandwidthMonitor(contextProvider.get());
  }

  public static WebRTCModule_ProvideBandwidthMonitorFactory create(
      Provider<Context> contextProvider) {
    return new WebRTCModule_ProvideBandwidthMonitorFactory(contextProvider);
  }

  public static BandwidthMonitor provideBandwidthMonitor(Context context) {
    return Preconditions.checkNotNullFromProvides(WebRTCModule.INSTANCE.provideBandwidthMonitor(context));
  }
}

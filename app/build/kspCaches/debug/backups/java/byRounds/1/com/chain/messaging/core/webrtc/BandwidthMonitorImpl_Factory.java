package com.chain.messaging.core.webrtc;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class BandwidthMonitorImpl_Factory implements Factory<BandwidthMonitorImpl> {
  private final Provider<Context> contextProvider;

  public BandwidthMonitorImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public BandwidthMonitorImpl get() {
    return newInstance(contextProvider.get());
  }

  public static BandwidthMonitorImpl_Factory create(Provider<Context> contextProvider) {
    return new BandwidthMonitorImpl_Factory(contextProvider);
  }

  public static BandwidthMonitorImpl newInstance(Context context) {
    return new BandwidthMonitorImpl(context);
  }
}

package com.chain.messaging.core.security;

import android.content.Context;
import android.net.ConnectivityManager;
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
public final class ThreatDetectorImpl_Factory implements Factory<ThreatDetectorImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<ConnectivityManager> connectivityManagerProvider;

  public ThreatDetectorImpl_Factory(Provider<Context> contextProvider,
      Provider<ConnectivityManager> connectivityManagerProvider) {
    this.contextProvider = contextProvider;
    this.connectivityManagerProvider = connectivityManagerProvider;
  }

  @Override
  public ThreatDetectorImpl get() {
    return newInstance(contextProvider.get(), connectivityManagerProvider.get());
  }

  public static ThreatDetectorImpl_Factory create(Provider<Context> contextProvider,
      Provider<ConnectivityManager> connectivityManagerProvider) {
    return new ThreatDetectorImpl_Factory(contextProvider, connectivityManagerProvider);
  }

  public static ThreatDetectorImpl newInstance(Context context,
      ConnectivityManager connectivityManager) {
    return new ThreatDetectorImpl(context, connectivityManager);
  }
}

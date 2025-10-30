package com.chain.messaging.di;

import android.content.Context;
import android.net.ConnectivityManager;
import com.chain.messaging.core.security.ThreatDetector;
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
public final class SecurityMonitoringModule_ProvideThreatDetectorFactory implements Factory<ThreatDetector> {
  private final Provider<Context> contextProvider;

  private final Provider<ConnectivityManager> connectivityManagerProvider;

  public SecurityMonitoringModule_ProvideThreatDetectorFactory(Provider<Context> contextProvider,
      Provider<ConnectivityManager> connectivityManagerProvider) {
    this.contextProvider = contextProvider;
    this.connectivityManagerProvider = connectivityManagerProvider;
  }

  @Override
  public ThreatDetector get() {
    return provideThreatDetector(contextProvider.get(), connectivityManagerProvider.get());
  }

  public static SecurityMonitoringModule_ProvideThreatDetectorFactory create(
      Provider<Context> contextProvider,
      Provider<ConnectivityManager> connectivityManagerProvider) {
    return new SecurityMonitoringModule_ProvideThreatDetectorFactory(contextProvider, connectivityManagerProvider);
  }

  public static ThreatDetector provideThreatDetector(Context context,
      ConnectivityManager connectivityManager) {
    return Preconditions.checkNotNullFromProvides(SecurityMonitoringModule.INSTANCE.provideThreatDetector(context, connectivityManager));
  }
}
